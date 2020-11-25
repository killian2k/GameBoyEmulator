package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * Simulates the joypad of the Gameboy. Composed of 8 buttons (Key enum).
 */
public final class Joypad implements Component {
    private final int LINE_0_BIT_INDEX = 4;
    private final int LINE_1_BIT_INDEX = 5;
    private final int NUMBER_OF_ROWS_MATRIX = 4;
    private final int INDEX_BIT_MATRIX_LINE_NUMBER = 2;
    private final int SIZE_INT_MATRIX_ROW_NUMBER = 2;

    private final Cpu cpu;
    private int lineButtons0Arrows = 0; // =(right left up down)
    private int lineButtons1Others = 0; // =(A B Select Start)
    private boolean isLine0Enabled = false;
    private boolean isLine1Enabled = false;

    /**
     * The 8 buttons represented by the enum.
     * The third bit represents the line number of the matrix of buttons.
     * The 2 lsb bits represents the row number of the matrix of buttons
     */
    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }


    /**
     * Constructor of the class.
     * @param cpu is the CPU of the gameboy.
     */
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }

    @Override
    public int read(int address) {
        if(address == AddressMap.REG_P1)
            return Bits.complement8(getRegBits());
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        if(address != AddressMap.REG_P1)
            return;
        data = Bits.complement8(data);
        isLine0Enabled = Bits.test(data, LINE_0_BIT_INDEX);
        isLine1Enabled = Bits.test(data, LINE_1_BIT_INDEX);
    }

    /**
     * The key pressed by the player.
     * @param k is the key.
     * @throws NullPointerException if the key k given is null.
     */
    public void keyPressed(Key k) {
        int actualState = Bits.clip(NUMBER_OF_ROWS_MATRIX, getRegBits());
        setBitButton(k, true);
        if(Bits.clip(NUMBER_OF_ROWS_MATRIX, getRegBits()) != actualState)
            cpu.requestInterrupt(Interrupt.JOYPAD);
    }

    /**
     * The key released by the player
     * @param k is the key.
     * @throws NullPointerException if the key k given is null.
     */
    public void keyReleased(Key k) {
        setBitButton(k,false);
    }

    private int getRegBits() {
        int reg = 0;
        reg = Bits.set(reg, LINE_0_BIT_INDEX, isLine0Enabled);
        reg = Bits.set(reg, LINE_1_BIT_INDEX, isLine1Enabled);
        return reg + Bits.clip(NUMBER_OF_ROWS_MATRIX,
                ((isLine1Enabled)?lineButtons1Others:0) | ((isLine0Enabled)?lineButtons0Arrows:0));
    }

    private void setBitButton(Key k, boolean value) {
        int indexKey = k.ordinal();

        if(Bits.test(indexKey, INDEX_BIT_MATRIX_LINE_NUMBER))
            lineButtons1Others = Bits.set(lineButtons1Others, Bits.clip(SIZE_INT_MATRIX_ROW_NUMBER, indexKey), value);
        else
            lineButtons0Arrows = Bits.set(lineButtons0Arrows, Bits.clip(SIZE_INT_MATRIX_ROW_NUMBER, indexKey), value);
    }
}