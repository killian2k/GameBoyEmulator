package ch.epfl.gameboj.component.lcd;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;

class LcdImageLineTest {

	//@Test
	void testBuildererror() {
		for(int d = 1;d <= 10000000;d*=32)
			for(int i = d+1;i<d+31;++i) {
				final int a = i;
				System.out.println(a);
				assertThrows(IllegalArgumentException.class, () -> {
					LcdImageLine.Builder b = new LcdImageLine.Builder(a);
				});
			}
		
	}
	
	//@Test
	void testBuilderSetByte() {
		LcdImageLine.Builder b = new LcdImageLine.Builder(256);
		int indexByte = 31;
		BitVector lsb = new BitVector(256);
		for(int i = 0; i<= 31;++i)
			b.setBytes(i, 0b11111111-i, i);
		LcdImageLine l = b.build();
		System.out.println(l.toString());
	}


	//@Test 
	void testImageLineConstructor(){
		BitVector.Builder b1 = new BitVector.Builder(32);
		b1.setByte(0, 0b11111111);
		b1.setByte(1, 0b0000_0000);
		BitVector.Builder b2 = new BitVector.Builder(32);
		b2.setByte(0, 0b11110000);
		b2.setByte(1, 0b0000_1111);
		BitVector.Builder b3 = new BitVector.Builder(32);
		b3.setByte(0, 0b11110000);
		b3.setByte(1, 0b0000_1111);
		
		BitVector lsb = b1.build();
		BitVector msb = b2.build();
		BitVector opacity = b3.build();
		
		LcdImageLine im = new LcdImageLine(msb, lsb, opacity);
		System.out.println(im.toString());
		assertEquals(true, im.mapColors((byte)0b00_01_10_11).toString().equals("msb:11111111111111111111000000001111\nlsb:11111111111111111111111100000000\nopa:00000000000000000000111111110000"));
		//System.out.println(im.mapColors((byte)0b11111100)); Okay
		//System.out.println(im.mapColors((byte)0b11_11_01_00)); Okay
		//System.out.println(im.mapColors((byte)0b00_11_00_00)); Okay
		//System.out.println(im.mapColors((byte)0b00_01_10_11)); Okay - Tested
	}
	
	//@Test
	void testImageLineBelow() {
		BitVector.Builder b1 = new BitVector.Builder(32);
		b1.setByte(0, 0b11001100);
		b1.setByte(1, 0b0000_0000);
		BitVector.Builder b2 = new BitVector.Builder(32);
		b2.setByte(0, 0b10101010);
		b2.setByte(1, 0b0000_1111);
		BitVector.Builder b3 = new BitVector.Builder(32);
		b3.setByte(0, 0b11110000);
		b3.setByte(1, 0b0000_1111);
		
		BitVector lsb = b1.build();
		BitVector msb = b2.build();
		BitVector opacity = b3.build();

		LcdImageLine im = new LcdImageLine(msb, lsb, opacity);
		BitVector.Builder b21 = new BitVector.Builder(32);
		b21.setByte(0, 0b1111_1100);
		b21.setByte(1, 0b0000_0000);
		BitVector.Builder b22 = new BitVector.Builder(32);
		b22.setByte(0, 0b1111_1010);
		b22.setByte(1, 0b0000_1111);
		BitVector.Builder b23 = new BitVector.Builder(32);
		b23.setByte(0, 0b1111_1111);
		b23.setByte(1, 0b0000_1111);
		
		BitVector lsb2 = b21.build();
		BitVector msb2 = b22.build();
		BitVector opacity2 = b23.build();
		LcdImageLine im2 = new LcdImageLine(msb2, lsb2, opacity2);
		System.out.println(im);
		System.out.println(im2);
		System.out.println(im2.below(im));
		
	}
	
	@Test
	void testImageLineJoin() {
		BitVector.Builder b1 = new BitVector.Builder(32);
		b1.setByte(0, 0b1010_1010);
		b1.setByte(1, 0b10101010);
		b1.setByte(2, 0b10010011);
		b1.setByte(3, 0b001010000);
		BitVector.Builder b2 = new BitVector.Builder(32);
		b2.setByte(0, 0b0000_0000);
		b2.setByte(1, 0b0000_0000);
		BitVector.Builder b3 = new BitVector.Builder(32);
		b3.setByte(0, 0b0000_0000);
		b3.setByte(1, 0b0000_0000);
		
		BitVector lsb = b1.build();
		BitVector msb = b2.build();
		BitVector opacity = b3.build();

		LcdImageLine im = new LcdImageLine(msb, lsb, opacity);
		BitVector.Builder b21 = new BitVector.Builder(32);
		b21.setByte(0, 0b1111_1111);
		b21.setByte(1, 0b1111_1111);
		BitVector.Builder b22 = new BitVector.Builder(32);
		b22.setByte(0, 0b1111_1111);
		b22.setByte(1, 0b1111_1111);
		BitVector.Builder b23 = new BitVector.Builder(32);
		b23.setByte(0, 0b1111_1111);
		b23.setByte(1, 0b1111_1111);
		
		BitVector lsb2 = b21.build();
		BitVector msb2 = b22.build();
		BitVector opacity2 = b23.build();
		LcdImageLine im2 = new LcdImageLine(msb2, lsb2, opacity2);
		System.out.println(im);
		System.out.println(im2);
		System.out.println(im.join(im2,2));
		
	}
}
