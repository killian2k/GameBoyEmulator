package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * Class that simulates the LCD of the Gameboy.
 */
public final class LcdController implements Clocked, Component{
    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;
    public static final int EDGE_TILE = 8;

    private static final int BG_HEIGHT = 256;
    private static final int BG_WIDTH = 256;
    private static final int INDEX_LINE_MAX = LCD_HEIGHT-1;
    private static final int WINDOW_WIDTH = 160;
    private static final int MAX_INDEX_TILE_LINE = 7;
    private static final int TILE_PER_LINE = 32;
    private static final int TILE_PER_WINDOW_LINE = 20;
    private static final int CYCLES_PER_IMAGE = 17556;
    private static final int CYCLES_MODE_0 = 51;
    private static final int CYCLES_MODE_2 = 20;
    private static final int CYCLES_MODE_3 = 43;
    private static final int CYCLES_PER_LINE = CYCLES_MODE_0 + CYCLES_MODE_2 + CYCLES_MODE_3;
    private static final int CYCLE_START_MODE_0 = CYCLES_MODE_2 + CYCLES_MODE_3;
    private static final int CYCLE_START_MODE_2 = 0;
    private static final int CYCLE_START_MODE_3 = CYCLES_MODE_2;
    private static final int CYCLES_PER_COPY_TO_OAM = 160;
    private static final int BYTE_PER_TILE = 16;
    private static final int BYTE_PER_TILE_lINE = 2;
    private static final int BYTE_PER_SPRITE = 4;
    private static final int INDEX_PALETTE = 4;
    private static final int INDEX_FLIP_H = 5;
    private static final int INDEX_FLIP_V = 6;
    private static final int INDEX_BEHIND_BG = 7;
    private static final int MAX_SPRITES_PER_LINE = 10;
    private static final int SHIFT_WX_MEMORY = 7; 
    private static final int SHIFT_Y_SPRITE_MEMORY = 16;
    private static final int SHIFT_X_SPRITE_MEMORY = 8;
    private final static int NUMBER_OF_TILES_IN_MEMORY = 384;
    private final static int NUMBER_OF_TILES_PER_LINE = 16;
    private final static int NUMBER_OF_TILES_PER_COL = NUMBER_OF_TILES_IN_MEMORY/NUMBER_OF_TILES_PER_LINE;
    private final static int NUMBER_OF_PIXEL_PER_LINE = NUMBER_OF_TILES_PER_LINE* EDGE_TILE;
    private final static int NUMBER_OF_PIXEL_PER_COL = NUMBER_OF_TILES_PER_COL* EDGE_TILE;
    private final static int NUMBER_OF_TILES_AFFORDABLE = 256;
    private final static int SIZE_MEMORY_TILE1 = 0x80;
    private static final LcdImageLine NEW_LINE = LcdImageLine.newEmptyLine(LCD_WIDTH);
    private static final LcdImage BLANK_IMAGE = new LcdImage(LCD_HEIGHT, LCD_WIDTH, Collections.nCopies(LCD_HEIGHT, NEW_LINE));

    private final Cpu cpu;
    private final RegisterFile<Reg> reg = new RegisterFile<>(Reg.values());
    private final RamController ramVideoController = new RamController(new Ram(AddressMap.VIDEO_RAM_SIZE), AddressMap.VIDEO_RAM_START);
    private final RamController ramOamController = new RamController(new Ram(AddressMap.OAM_RAM_SIZE), AddressMap.OAM_START);
    private LcdImage.Builder nextImageBuilder = new LcdImage.Builder(LCD_HEIGHT, LCD_WIDTH);
    private LcdImage currentImage = BLANK_IMAGE;
    private Bus bus;
    private int winY;
    private long lcdOnCycle;
    private long nextNonIdleCycle = Long.MAX_VALUE;
    private int indexCopy;
    private int startAdrToCopy;
    private boolean isCopying = false;

    // Enums of the registers
    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }
    private enum LcdcAspects implements Bit{
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }
    private enum StatBits implements Bit{
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC 
    }
    private enum AttributSprite {
        Y_POS, X_POS, INDEX_TILE, BINARY_CARACT 
    }

    /**
     * Constructor of the class.
     * @param cpu is the CPU of the gameboy.
     * @throws NullPointerException if the cpu given is null.
     */
    public LcdController(Cpu cpu) {
        if(cpu == null)
            throw new NullPointerException();
        this.cpu = cpu;
    }

    /**
     * Get the image currently displayed on the screen.
     * @return the image.
     * If no image has been displayed yet, return a blank LcdImage (only 0 bits).
     */
    public LcdImage currentImage() {
        return currentImage;
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if(address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END)
            return reg.get(Reg.values()[address-AddressMap.REGS_LCDC_START]);
        if(address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END)
            return ramVideoController.read(address);
        if(address >= AddressMap.OAM_START && address < AddressMap.OAM_END)
            return ramOamController.read(address);

        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits8(data);
        Preconditions.checkBits16(address);
        if(address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            if(address == AddressMap.REG_STAT)
                reg.set(Reg.STAT, (data & 0b1111_1000) | (reg.get(Reg.STAT) & 0b111));
            else {
                if(address == AddressMap.REG_LY)
                    return;

                //Extinction of the screen
                if(address == AddressMap.REG_LCDC && testLcdcAspect(LcdcAspects.LCD_STATUS) && !Bits.test(data, LcdcAspects.LCD_STATUS)) {
                    enableMode(0);
                    modifyLy(0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }

                reg.set(Reg.values()[address-AddressMap.REGS_LCDC_START], data);
                if(address == AddressMap.REG_LYC)
                    modifyLyLyc();
                if(address == AddressMap.REG_DMA) {
                    startCopy(data);
                }
            }

            return;
        }
        if(address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            ramVideoController.write(address, data);
            return;
        }
        if(address >= AddressMap.OAM_START && address < AddressMap.OAM_END)
            ramOamController.write(address, data);
    }

    @Override
    public void cycle(long cycle) {
        if(isCopying) {
            if(indexCopy == CYCLES_PER_COPY_TO_OAM)
                isCopying = false;
            else {
                ramOamController.write(AddressMap.OAM_START + indexCopy, bus.read(startAdrToCopy+indexCopy));
                ++indexCopy;
                return;
            }
        }

        // Power one the screen
        if(nextNonIdleCycle == Long.MAX_VALUE && testLcdcAspect(LcdcAspects.LCD_STATUS)) {
            lcdOnCycle = cycle;
            nextNonIdleCycle = cycle;
        }

        if(nextNonIdleCycle == cycle)    
            reallyCycle();
    }

    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }


    // Private methods

    private void reallyCycle() {
        int cyclesUsedCurrentImage = (int)(nextNonIdleCycle - lcdOnCycle)%CYCLES_PER_IMAGE;
        int cyclesUsedCurrentLine = cyclesUsedCurrentImage % CYCLES_PER_LINE;
        int currentLine = cyclesUsedCurrentImage/CYCLES_PER_LINE;

        if(currentLine < LCD_HEIGHT) {
            switch(cyclesUsedCurrentLine) {
            case CYCLE_START_MODE_2:
                modifyLy(currentLine);
                enableMode(2);
                if(currentLine == 0) {
                    nextImageBuilder = new LcdImage.Builder(LCD_HEIGHT, LCD_WIDTH);
                    winY = 0;
                }
                nextNonIdleCycle+=CYCLES_MODE_2;
                return;

            case CYCLE_START_MODE_3:
                nextImageBuilder.setLine(currentLine, computeLine(currentLine));
                enableMode(3);
                nextNonIdleCycle+=CYCLES_MODE_3;
                return;

            case CYCLE_START_MODE_0:
                if(nextImageBuilder != null && currentLine == INDEX_LINE_MAX) {
                    currentImage = nextImageBuilder.build();
                }
                enableMode(0);
                nextNonIdleCycle+=CYCLES_MODE_0;
                return;
            default:
                return;
            }
        }        

        modifyLy(currentLine);
        enableMode(1);
        nextNonIdleCycle+=CYCLES_PER_LINE;
    }

    private void enableMode(int numberMode) {
        Preconditions.checkArgument(numberMode >= 0 && numberMode <= 3);
        if(Bits.clip(2, reg.get(Reg.STAT)) == numberMode)
            return;
        setStatBit(StatBits.MODE0, Bits.test(numberMode, 0));
        setStatBit(StatBits.MODE1, Bits.test(numberMode, 1));
        if(numberMode < 3 && reg.testBit(Reg.STAT, StatBits.INT_MODE0.index() + numberMode))
            cpu.requestInterrupt(Interrupt.LCD_STAT);
        if(numberMode == 1)
            cpu.requestInterrupt(Interrupt.VBLANK);
    }

    // Return a 16 bits integer with 8 bits for msb and 8 bits for lsb.
    private int getLineOfTile(int indexTile, int indexLine, boolean isTileSource0) {
        // Manage Tile addresses
        if(isTileSource0) {
            indexTile +=0x80;
            indexTile = Bits.clip(8, indexTile);
        }
        int index = AddressMap.TILE_SOURCE[isTileSource0?0:1] + indexTile*BYTE_PER_TILE + indexLine*BYTE_PER_TILE_lINE;
        int lsb = Bits.reverse8(read(index));
        int msb = Bits.reverse8(read(index + 1));
        return Bits.make16(msb, lsb);
    }

    private LcdImageLine computeLine(int indexLineOfScreen) {
        //Background
        LcdImageLine line = (testLcdcAspect(LcdcAspects.BG))?computeLayerOfLine(indexLineOfScreen, false):NEW_LINE;

        //Window
        int wxM = Math.max(0, reg.get(Reg.WX) - SHIFT_WX_MEMORY);
        if(testLcdcAspect(LcdcAspects.WIN) && wxM >= 0 && wxM < WINDOW_WIDTH && reg.get(Reg.WY) <= reg.get(Reg.LY) && winY < LCD_HEIGHT) {
            line = line.join(computeLayerOfLine(winY, true).shift(-wxM), wxM);
            ++winY;
        }

        if(!testLcdcAspect(LcdcAspects.OBJ))
            return line;

        //Sprites
        LcdImageLine spritesLineForward = NEW_LINE;
        LcdImageLine spritesLineBehind = NEW_LINE;
        int[] sprites = spritesIntersectingLine(indexLineOfScreen);
        for(int indexSprite : sprites) {
            spritesLineBehind = calculateSpriteLineBehind(indexSprite, indexLineOfScreen, spritesLineBehind).below(spritesLineBehind);
            spritesLineForward = calculateSpriteLineForward(indexSprite, indexLineOfScreen, spritesLineForward).below(spritesLineForward);
        }
        BitVector opacity = line.opacity().not().and(spritesLineBehind.opacity()).not();
        line = spritesLineBehind.below(line, opacity);
        return line.below(spritesLineForward);
    }

    private LcdImageLine computeLayerOfLine(int indexLineOfScreen, boolean isWindow) {
        if(!isWindow)
            indexLineOfScreen = (indexLineOfScreen + reg.get(Reg.SCY))%BG_HEIGHT;
        int indexLineOfTile = indexLineOfScreen / EDGE_TILE;
        int indexLineInTile = indexLineOfScreen % EDGE_TILE;

        LcdcAspects aspectToTest = (isWindow)?LcdcAspects.WIN_AREA:LcdcAspects.BG_AREA;
        int startAdrTileRange = AddressMap.BG_DISPLAY_DATA[!(testLcdcAspect(aspectToTest))?0:1];

        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(BG_WIDTH);
        for(int i = 0;i<((isWindow)?TILE_PER_WINDOW_LINE:TILE_PER_LINE);++i) {
            int tileLine = getLineOfTile(read(startAdrTileRange + TILE_PER_LINE*indexLineOfTile + i), indexLineInTile,!testLcdcAspect(LcdcAspects.TILE_SOURCE));
            lineBuilder.setBytes(i, Bits.get8MsbFrom16Bits(tileLine), Bits.get8Lsb(tileLine));
        }
        return lineBuilder.build().extractWrapped((!isWindow)?reg.get(Reg.SCX):0, LCD_WIDTH).mapColors(reg.get(Reg.BGP)); 
    }

    private LcdImageLine calculateSpriteLineBehind(int indexSprite, int indexLine, LcdImageLine upperLine) {
        return calculateSpriteLine(indexSprite, indexLine, upperLine, true);
    }

    private LcdImageLine calculateSpriteLineForward(int indexSprite, int indexLine, LcdImageLine backLine) {
        return calculateSpriteLine(indexSprite, indexLine, backLine, false);
    }

    private LcdImageLine calculateSpriteLine(int indexSprite, int indexLine, LcdImageLine line, boolean isBehind) {
        indexSprite = AddressMap.OAM_START + BYTE_PER_SPRITE * indexSprite;
        int carac = getAttributSprite(AttributSprite.BINARY_CARACT, indexSprite, false);
        if(isBehind != Bits.test(carac, INDEX_BEHIND_BG))
            return line;

        boolean flipH = Bits.test(carac, INDEX_FLIP_H);
        boolean flipV = Bits.test(carac, INDEX_FLIP_V);
        int indexTile = getAttributSprite(AttributSprite.INDEX_TILE, indexSprite, false);
        int palette = reg.get(Bits.test(carac, INDEX_PALETTE)?Reg.OBP1:Reg.OBP0);
        int indexLineOfTile = indexLine - getAttributSprite(AttributSprite.Y_POS, indexSprite, true);


        if(testLcdcAspect(LcdcAspects.OBJ_SIZE))
            indexTile += ((indexSprite&1) == 0 && !flipV)?0:1;
        if(flipV)        
            indexLineOfTile = MAX_INDEX_TILE_LINE - indexLineOfTile;

        return getSpriteLine(getLineOfTile(indexTile, indexLineOfTile,false), flipH)
                .shift(-getAttributSprite(AttributSprite.X_POS, indexSprite, true))
                .mapColors(palette).below(line);
    }

    private LcdImageLine getSpriteLine(int lineToAdd, boolean flipHor) {
        Preconditions.checkBits16(lineToAdd);
        LcdImageLine.Builder b = new LcdImageLine.Builder(LCD_WIDTH);
        int msb = Bits.get8MsbFrom16Bits(lineToAdd);
        int lsb = Bits.get8Lsb(lineToAdd);
        if(!flipHor)
            b.setBytes(0, msb, lsb);
        else
            b.setBytes(0, Bits.reverse8(msb), Bits.reverse8(lsb));
        return b.build();
    }

    private void modifyLy(int newValue) {
        reg.set(Reg.LY, newValue);
        modifyLyLyc();
    }

    private void modifyLyLyc() {
        boolean equalityLy = (reg.get(Reg.LY) == reg.get(Reg.LYC));
        setStatBit(StatBits.LYC_EQ_LY, equalityLy);
        if(equalityLy && testStatBit(StatBits.INT_LYC))
            cpu.requestInterrupt(Interrupt.LCD_STAT);
    }

    private void startCopy(int msbAdr) {
        isCopying = true;        
        indexCopy = 0;
        startAdrToCopy = Bits.make16(msbAdr, 0);
        if(nextNonIdleCycle != Long.MAX_VALUE) {
            nextNonIdleCycle += CYCLES_PER_COPY_TO_OAM;
            lcdOnCycle += CYCLES_PER_COPY_TO_OAM;
        }
    }

    private int[] spritesIntersectingLine(int lineIndex) {
        int[] indexes = new int[MAX_SPRITES_PER_LINE];
        int counter = 0;
        int heightSprite = EDGE_TILE;
        if(testLcdcAspect(LcdcAspects.OBJ_SIZE))
            heightSprite *= 2;
        for(int i = 0;i < AddressMap.OAM_RAM_SIZE && counter < MAX_SPRITES_PER_LINE;i+=BYTE_PER_SPRITE) {
            int adr = AddressMap.OAM_START + i;
            int ySprite = getAttributSprite(AttributSprite.Y_POS, adr, true);
            if(lineIndex >= ySprite && lineIndex < ySprite + heightSprite) {
                indexes[counter] = Bits.make16(getAttributSprite(AttributSprite.X_POS, adr, false), i/BYTE_PER_SPRITE);
                ++counter;
            }
        }
        Arrays.sort(indexes, 0, counter);
        int[] arraySprites = new int[counter];
        for(int j = 0;j<counter;++j)
            arraySprites[j] = Bits.get8Lsb(indexes[j]);

        return arraySprites;
    }

    private boolean testLcdcAspect(LcdcAspects aspect) {
        return reg.testBit(Reg.LCDC, aspect);
    }

    private boolean testStatBit(StatBits statBit) {
        return reg.testBit(Reg.STAT, statBit);
    }

    private void setStatBit(StatBits statBit, boolean value) {
        reg.setBit(Reg.STAT, statBit, value);
    }

    private int getAttributSprite(AttributSprite attribut, int indexSprite, boolean applyPosShift) {
        int val = ramOamController.read(indexSprite + attribut.ordinal());
        if(applyPosShift && attribut.ordinal() <= AttributSprite.X_POS.ordinal())
            val -= (attribut==AttributSprite.X_POS)?SHIFT_X_SPRITE_MEMORY:SHIFT_Y_SPRITE_MEMORY;
        return val;
    }
    
    
    /**
     * Get all the tiles that are stored in the memory in a LcdImage object.
     * @return a image with all the tiles displayed.
     */
    public LcdImage getAllTilesImage() {
        List<LcdImageLine> lines = new ArrayList<>();
        for(int i = 0;i < NUMBER_OF_PIXEL_PER_COL;++i) {
            int indexTileLine = i/EDGE_TILE;
            int indexLineOfTile = i%EDGE_TILE;
            LcdImageLine.Builder newLine = new LcdImageLine.Builder(NUMBER_OF_PIXEL_PER_LINE);
            for(int l = 0;l< NUMBER_OF_TILES_PER_LINE;++l) {
                int bytes = getLineOfTile((indexTileLine*NUMBER_OF_TILES_PER_LINE + l)%NUMBER_OF_TILES_AFFORDABLE, indexLineOfTile, indexTileLine*NUMBER_OF_TILES_PER_LINE + l>=SIZE_MEMORY_TILE1);
                newLine.setBytes(l, Bits.get8MsbFrom16Bits(bytes), Bits.get8Lsb(bytes));
            }
            lines.add(newLine.build());
        }

               
        return new LcdImage(NUMBER_OF_PIXEL_PER_COL, NUMBER_OF_PIXEL_PER_LINE, lines);
    }
}