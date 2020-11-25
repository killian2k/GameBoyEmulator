package ch.epfl.gameboj;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.component.cpu.Alu;

@Disabled class ___RegisterFileTest {
    final int MAX_16=(int)(Math.pow(2, 16)-1);
    final int MAX_12=(int)(Math.pow(2, 12)-1);
    final int MAX_8=(int)(Math.pow(2, 8)-1);
    final int MAX_4=(int)(Math.pow(2, 4)-1);

    private enum bits implements Bit {
        B0,B1,B2,B3,B4,B5,B6,B7
    }

    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    @Test
    void setAndget() {
        RegisterFile<Reg> regFile = new RegisterFile<>(Reg.values());

        assertEquals(0,regFile.get(Reg.A));
        assertEquals(0,regFile.get(Reg.F));
        assertEquals(0,regFile.get(Reg.B));
        assertEquals(0,regFile.get(Reg.C));
        assertEquals(0,regFile.get(Reg.D));
        assertEquals(0,regFile.get(Reg.E));
        assertEquals(0,regFile.get(Reg.H));
        assertEquals(0,regFile.get(Reg.L));

        regFile.set(Reg.A, 10);
        assertEquals(10, regFile.get(Reg.A));

        Random rng = newRandom();
        int test;
        for(int i = 0; i < RANDOM_ITERATIONS; ++i) {
            test=rng.nextInt(MAX_8);
            regFile.set(Reg.A, test);
            regFile.set(Reg.F, test);
            regFile.set(Reg.B, test);
            regFile.set(Reg.C, test);
            regFile.set(Reg.D, test);
            regFile.set(Reg.E, test);
            regFile.set(Reg.H, test);
            regFile.set(Reg.L, test);

            assertEquals(test,regFile.get(Reg.A));
            assertEquals(test,regFile.get(Reg.F));
            assertEquals(test,regFile.get(Reg.B));
            assertEquals(test,regFile.get(Reg.C));
            assertEquals(test,regFile.get(Reg.D));
            assertEquals(test,regFile.get(Reg.E));
            assertEquals(test,regFile.get(Reg.H));
            assertEquals(test,regFile.get(Reg.L));

        }

        assertThrows(IllegalArgumentException.class, () -> {
            regFile.set(Reg.A, 256);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            regFile.set(Reg.A, -1);
        });

    }

    @Test
    void testBit() {
        RegisterFile<Reg> regFile = new RegisterFile<>(Reg.values());
        Random rng = newRandom();
        int test,index;
        Bit bit = null;

        for(int i = 0; i < RANDOM_ITERATIONS; ++i) {
            test=rng.nextInt(MAX_8);
            index=rng.nextInt(8);
            switch(index) {
            case 0: bit = bits.B0; break;
            case 1: bit = bits.B1; break;
            case 2: bit = bits.B2; break;
            case 3: bit = bits.B3; break;
            case 4: bit = bits.B4; break;
            case 5: bit = bits.B5; break;
            case 6: bit = bits.B6; break;
            case 7: bit = bits.B7; break;
            }

            regFile.set(Reg.A, test);
            regFile.set(Reg.F, test);
            regFile.set(Reg.B, test);
            regFile.set(Reg.C, test);
            regFile.set(Reg.D, test);
            regFile.set(Reg.E, test);
            regFile.set(Reg.H, test);
            regFile.set(Reg.L, test);

            for(Reg reg : Reg.values())
            {
                assertEquals(Alu.unpackFlags(Alu.testBit(test, index)) == 0b0010_0000, regFile.testBit(reg, bit));
            }
        }
    }

    @Test
    void setBit() {
        RegisterFile<Reg> regFile = new RegisterFile<>(Reg.values());
        Random rng = newRandom();
        int test,index;
        boolean testb;
        Bit bit = null;
        for(int i = 0; i < RANDOM_ITERATIONS; ++i) {
            regFile.set(Reg.A,0);
            regFile.set(Reg.F, 0);
            regFile.set(Reg.B, 0);
            regFile.set(Reg.C, 0);
            regFile.set(Reg.D, 0);
            regFile.set(Reg.E, 0);
            regFile.set(Reg.H, 0);
            regFile.set(Reg.L, 0);

            test=rng.nextInt(MAX_8);
            index=rng.nextInt(8);
            testb=rng.nextBoolean();
            switch(index) {
                case 0: bit = bits.B0; break;
                case 1: bit = bits.B1; break;
                case 2: bit = bits.B2; break;
                case 3: bit = bits.B3; break;
                case 4: bit = bits.B4; break;
                case 5: bit = bits.B5; break;
                case 6: bit = bits.B6; break;
                case 7: bit = bits.B7; break;
            }
            System.out.println("Value = "+test+" || index = "+index+" || Bit changé = "+testb);
            
            for(Reg reg : Reg.values())
            {
                regFile.setBit(reg,bit, testb);
                assertEquals(testb,regFile.testBit(reg, bit));
            }
           
        }





    }

}
