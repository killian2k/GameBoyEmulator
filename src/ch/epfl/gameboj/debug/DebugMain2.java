package ch.epfl.gameboj.debug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdImage;

public final class DebugMain2 {
	private static final int[] COLOR_MAP = new int[] {
			0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
	};

	public static void main(String[] args) throws IOException {
		
		//String[] filesname = {"01-special.gb", "02-interrupts.gb" , "03-op sp,hl.gb", "04-op r,imm.gb", "05-op rp.gb", "06-ld r,r.gb", "07-jr,jp,call,ret,rst.gb", "08-misc instrs.gb", "09-op r,r.gb", "10-bit ops.gb", "11-op a,(hl).gb", "instr_timing.gb", "flappyboy.gb", "Tetris (World).gb", "tasmaniaStory.gb"};
		//String[] filesname = {"01-special.gb", "flappyboy.gb", "Tetris (World).gb"};
		//String[] filesname = {"flappyboy.gb"}; //tile 0 0x9c00
		
	    String[] filesname = {"Tetris (World).gb"}; //tile 1 0x9800
		//String[] filesname = {"tasmaniaStory.gb"}; //tile 0 0x9800
	    //String[] filesname = {"sprite_priority.gb"};
		//String flappy = "flappyboy.gb";
		//String flappy = "Tetris (World).gb";
		for(String s : filesname) {
			//File romFile = new File(filesname[1]);
		//File romFile = new File(flappy);
			File romFile = new File(s);
			long cycles = 30_000_000l;
	
			
			GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
			gb.runUntil(cycles);
	
			System.out.println("+--------------------+");
			for (int y = 0; y < 18; ++y) {
				System.out.print("|");
				for (int x = 0; x < 20; ++x) {
					char c = (char) gb.bus().read(0x9800 + 32*y + x);
					System.out.print(Character.isISOControl(c) ? " " : c);
				}
				System.out.println("|");
			}
			System.out.println("+--------------------+");
	
			LcdImage li = gb.lcdController().currentImage();
			BufferedImage i =
					new BufferedImage(li.width(),
							li.height(),
							BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < li.height(); ++y)
				for (int x = 0; x < li.width(); ++x)
					i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
			//ImageIO.write(i, "png", new File("gb" + u + ".png"));
			ImageIO.write(i, "png", new File("gb_" + s + ".png"));
		}
	}
}
