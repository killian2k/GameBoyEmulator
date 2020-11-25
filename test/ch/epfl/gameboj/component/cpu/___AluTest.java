package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;

//OK
@Disabled class ___AluTest {
    final int MAX_16=(int)(Math.pow(2, 16)-1);
    final int MAX_12=(int)(Math.pow(2, 12)-1);
    final int MAX_8=(int)(Math.pow(2, 8)-1);
    final int MAX_4=(int)(Math.pow(2, 4)-1);

    @Test
    void maskZNHC() {
        final boolean T=true;
        final boolean F=false;
        assertEquals(0b00000000, Alu.maskZNHC(F, F, F, F));
        assertEquals(0b00010000, Alu.maskZNHC(F, F, F, T));
        assertEquals(0b00100000, Alu.maskZNHC(F, F, T, F));
        assertEquals(0b00110000, Alu.maskZNHC(F, F, T, T));
        assertEquals(0b01000000, Alu.maskZNHC(F, T, F, F));
        assertEquals(0b01010000, Alu.maskZNHC(F, T, F, T));
        assertEquals(0b01100000, Alu.maskZNHC(F, T, T, F));
        assertEquals(0b01110000, Alu.maskZNHC(F, T, T, T));
        assertEquals(0b10000000, Alu.maskZNHC(T, F, F, F));
        assertEquals(0b10010000, Alu.maskZNHC(T, F, F, T));
        assertEquals(0b10100000, Alu.maskZNHC(T, F, T, F));
        assertEquals(0b10110000, Alu.maskZNHC(T, F, T, T));
        assertEquals(0b11000000, Alu.maskZNHC(T, T, F, F));
        assertEquals(0b11010000, Alu.maskZNHC(T, T, F, T));
        assertEquals(0b11100000, Alu.maskZNHC(T, T, T, F));
        assertEquals(0b11110000, Alu.maskZNHC(T, T, T, T));
    }

    @Test //OK
    void unpackValue() {
        assertThrows(IllegalArgumentException.class,() -> Alu.unpackValue(0b000111111111111111111111111111)); //25 bits
        Random rng = newRandom();
        int MAX_16=(int)(Math.pow(2, 16)-1);
        int MAX_8=(int)(Math.pow(2, 8)-1);
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v1_16 = rng.nextInt(MAX_16);
            int v1_8 = rng.nextInt(MAX_8);
            int v2_16=(v1_16<<8);
            int v2_8=(v1_8<<8);
            assertEquals(v1_16, Alu.unpackValue(v2_16));
            assertEquals(v1_8, Alu.unpackValue(v2_8));
        }
    }

    @Test //OK
    void unpackFlags() {
        assertThrows(IllegalArgumentException.class,() -> Alu.unpackValue(0b000111111111111111111111111111)); //25 bits
        Random rng = newRandom();
        int MAX_16=(int)(Math.pow(2, 16)-1);
        int MAX_8=(int)(Math.pow(2, 8)-1);
        int MAX_4=(int)(Math.pow(2, 4)-1);
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v1_16 = rng.nextInt(MAX_16);
            int v1_8 = rng.nextInt(MAX_8);
            int v2_16=(v1_16<<4);
            int v2_8=(v1_8<<4);
            int f = rng.nextInt(MAX_4);
            //Flag part Adding random flag propoerties (8 or 16) "xxxxx..."+"xxxx" (4) random flags + "0000"
            v2_16+=f;
            v2_8+=f;
            v2_16=v2_16<<4;
            v2_8=v2_8<<4;
            assertEquals(f<<4, Alu.unpackFlags(v2_16));
            assertEquals(f<<4, Alu.unpackFlags(v2_8));
        }
        assertEquals(0x70, Alu.unpackFlags(0xFF70));
    }

    @Test //OK
    void add() {
        assertThrows(IllegalArgumentException.class,() -> Alu.add(0b1000000000, 0b10, true));
        assertThrows(IllegalArgumentException.class,() -> Alu.add(0b10, 0b1000000000, true));
        assertThrows(IllegalArgumentException.class,() -> Alu.add(0b1000000000, 0b10, false));
        assertThrows(IllegalArgumentException.class,() -> Alu.add(0b10, 0b1000000000, false));
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int l = rng.nextInt(MAX_8);
            int r = rng.nextInt(MAX_8);
            boolean c = rng.nextBoolean(); //represent the boolean
            boolean h=false;
            //Half carry
            int tot;
            if(c) { 
                tot =(l+r+1); 
                if((l & 0b1111) + (r & 0b1111) + 1 > MAX_4) { h=true; }
            }else { 
                tot = l+r; 
                if((l & 0b1111) + (r & 0b1111) > MAX_4) { h=true; }
            }
            if(tot-1==MAX_8) {
                if(h) { assertEquals(0b10110000, Alu.unpackFlags(Alu.add(l, r,c))); } else {
                    assertEquals(0b10010000, Alu.unpackFlags(Alu.add(l, r,c)));
                }
            }else {

                if(tot>MAX_8){
                    assertEquals(tot-(MAX_8+1),Alu.unpackValue(Alu.add(l, r,c) ));
                    if(h) { assertEquals(0b00110000, Alu.unpackFlags(Alu.add(l, r,c))); } else {
                        assertEquals(0b00010000, Alu.unpackFlags(Alu.add(l, r,c)));
                    }
                }else {
                    assertEquals(tot,Alu.unpackValue(Alu.add(l, r,c) ));
                    if(tot==0) {
                        if(h) { assertEquals(0b10100000, Alu.unpackFlags(Alu.add(l, r,c))); } else {
                            assertEquals(0b10000000, Alu.unpackFlags(Alu.add(l, r,c)));
                        }
                    }else {
                        if(h) { 
                            assertEquals(0b00100000, Alu.unpackFlags(Alu.add(l, r,c)));
                        } else {
                            assertEquals(0b00000000, Alu.unpackFlags(Alu.add(l, r,c)));
                        }
                    }
                }
            }

        }
    }

    @Test //OK
    void add16L() {
        assertThrows(IllegalArgumentException.class,() -> Alu.add16L(0b100000000000000000, 0b10));
        assertThrows(IllegalArgumentException.class,() -> Alu.add(0b10, 0b10000000000000000));

        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int l=rng.nextInt(MAX_16);
            int r=rng.nextInt(MAX_16);
            int tot=l+r;
            int c = 0;
            int h = 0;

            if((l & 0b11111111) + (r & 0b11111111) > MAX_8) { c=1; }
            if((l & 0b1111) + (r & 0b1111) > MAX_4) { h=2; }

            int mask = (h+c)<<4;

            if(tot>MAX_16) {
                assertEquals(tot-(MAX_16+1),Alu.unpackValue(Alu.add16L(l, r)));
                assertEquals(mask,Alu.unpackFlags(Alu.add16L(l, r)));
            }else {
                assertEquals(tot,Alu.unpackValue(Alu.add16L(l, r)));
                assertEquals(mask,Alu.unpackFlags(Alu.add16L(l, r)));
            }

        }

    }

    @Test //OK
    void add16H() {
        assertThrows(IllegalArgumentException.class,() -> Alu.add16L(0b100000000000000000, 0b10));
        assertThrows(IllegalArgumentException.class,() -> Alu.add(0b10, 0b10000000000000000));
        assertEquals(0b110000,Alu.unpackFlags(Alu.add16H(0b1111111100000000,0b100000000)));


        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int l=rng.nextInt(MAX_16);
            int r=rng.nextInt(MAX_16);
            int tot=l+r;
            int c = 0;
            int h = 0;

            if(tot>MAX_16) {
                c=1;
            }
            if((l & 0b111111111111) + (r & 0b111111111111) > MAX_12) { 
                h=2; 
            }
            int mask = (h+c)<<4;

            if(tot>MAX_16) {
                assertEquals(tot-(MAX_16+1),Alu.unpackValue(Alu.add16H(l, r)));
                assertEquals(mask,Alu.unpackFlags(Alu.add16H(l, r)));
            }else {
                assertEquals(tot,Alu.unpackValue(Alu.add16H(l, r)));
                assertEquals(mask,Alu.unpackFlags(Alu.add16H(l, r)));

            }

        }



    }

    @Test //OK
    void sub() {
        assertThrows(IllegalArgumentException.class,() -> Alu.add(0b1000000000, 0b10, true));
        assertThrows(IllegalArgumentException.class,() -> Alu.add(0b10, 0b1000000000, true));
        assertThrows(IllegalArgumentException.class,() -> Alu.add(0b1000000000, 0b10, false));
        assertThrows(IllegalArgumentException.class,() -> Alu.add(0b10, 0b1000000000, false));
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int l = rng.nextInt(MAX_8);
            int r = rng.nextInt(MAX_8);
            boolean b = rng.nextBoolean();
            int c_ini=0;
            if(b) { c_ini=1; }
            int tot = l-r-c_ini;
            int z=0;
            int h=0;
            int c=0;
            if((l&0b1111)-(r&0b1111)-c_ini<0) h=2;
            if(tot<0) c=1;
            if(tot==0) z=8;
            int mask=(z+4+h+c)<<4;
            if(tot<0) {
                assertEquals(mask,Alu.unpackFlags(Alu.sub(l, r,b)));
                assertEquals((tot+MAX_8+1),Alu.unpackValue(Alu.sub(l, r,b)));
            }else {
                assertEquals(mask,Alu.unpackFlags(Alu.sub(l, r,b)));
                assertEquals(tot,Alu.unpackValue(Alu.sub(l, r,b)));
            }
        }



        assertEquals(0x00, Alu.unpackValue(Alu.sub(0x10, 0x10, false)));
        assertEquals(0xC0, Alu.unpackFlags(Alu.sub(0x10, 0x10, false)));
        assertEquals(0x90, Alu.unpackValue(Alu.sub(0x10, 0x80, false)));
        assertEquals(0x50, Alu.unpackFlags(Alu.sub(0x10, 0x80, false)));
        assertEquals(0b011, Alu.unpackValue(Alu.sub(0b100, 1, false)));
        assertEquals(0xff, Alu.unpackValue(Alu.sub(0x01, 0x01, true)));
        assertEquals(0x70, Alu.unpackFlags(Alu.sub(0x01, 0x01, true)));
    }

    @Test //Not OK
    void bcdAdjust() {
        assertEquals(0x73,Alu.unpackValue(Alu.bcdAdjust(0x6D, false, false, false)));
        assertEquals(0x09,Alu.unpackValue(Alu.bcdAdjust(0x0F, true, true, false)));
        assertEquals(0,Alu.unpackFlags(Alu.bcdAdjust(0x6D, false, false, false)));
        assertEquals((4)<<4,Alu.unpackFlags(Alu.bcdAdjust(0x0F, true, true, false)));



        /*try {
            System.out.println(Integer.toBinaryString(Alu.bcdAdjust(99, false,false,false)));
        }catch(Exception e) {
            System.out.println(e.getClass());
        }
        Random rng = newRandom();
        int l,r,n,h,c,z,combinaison,mask,answer;
        boolean b_h,b_c,b_n;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            h=n=c=z=0;
            l = rng.nextInt(9);
            r = rng.nextInt(9);
            combinaison = l*10+r;
            b_h=rng.nextBoolean();
            b_c=rng.nextBoolean();
            b_n=rng.nextBoolean();
            if(b_h) h=2;
            if(b_c) c=1;
            if(b_n) n=4;
            if(combinaison==0) z=8;
            mask=(c+h+n+z)<<4;
            answer=(l<<4)+r;
            System.out.println(Integer.toBinaryString(l)+" - "+Integer.toBinaryString(r)+" // " +Integer.toBinaryString(Alu.bcdAdjust(99, false, true, false)));
            assertEquals(answer, Alu.unpackValue(Alu.bcdAdjust(combinaison, b_n, b_h, b_c)));
            assertEquals(mask, Alu.unpackFlags(Alu.bcdAdjust(combinaison, b_n, b_h, b_c)));
        }

        assertEquals(0x73, Alu.unpackValue(Alu.bcdAdjust(0x6d, false, false, false)));
        assertEquals(0x00, Alu.unpackFlags(Alu.bcdAdjust(0x6d, false, false, false)));
        assertEquals(0x09, Alu.unpackValue(Alu.bcdAdjust(0x0F, true, true, false)));
        assertEquals(0x40, Alu.unpackFlags(Alu.bcdAdjust(0x0F, true, true, false))); */
    }

    @Test //OK
    void and() {
        Random rng = newRandom();
        int l,r,and,fin;
        int mask;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            mask=2;
            l = rng.nextInt(MAX_8);
            r = rng.nextInt(MAX_8);
            and=l & r;
            if(and==0)mask+=8;
            fin=(and<<8)+(mask<<4);
            assertEquals(fin,Alu.and(l, r));
        }
    }

    @Test //OK
    void or() {
        Random rng = newRandom();
        int l,r,or,fin;
        int mask;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            mask=0;
            l = rng.nextInt(MAX_8);
            r = rng.nextInt(MAX_8);
            or=l | r;
            if(or==0) mask=8;
            fin=(or<<8)+(mask<<4);
            assertEquals(fin,Alu.or(l, r));
        }
    }

    @Test //OK
    void xor() {
        Random rng = newRandom();
        int l,r,xor,fin;
        int mask;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            mask=0;
            l = rng.nextInt(MAX_8);
            r = rng.nextInt(MAX_8);
            xor=l^r;
            if(xor==0) mask=8;
            fin=(xor<<8)+(mask<<4);
            assertEquals(fin,Alu.xor(l, r));
        }
    }

    @Test //OK
    void shiftLeft() {
        Random rng = newRandom();
        int v,v_fin,c,mask,z;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c=0;
            z=0;
            mask=0;
            v = rng.nextInt(MAX_8);
            v_fin = (v<<1);
            if(v_fin>MAX_8) {
                c=1;
                v_fin-=MAX_8+1;
            }
            if(v==0)z=8;
            mask=c+z;
            assertEquals(v_fin,Alu.unpackValue(Alu.shiftLeft(v)));
        }
    }

    @Test //OK
    void shiftRightA() {
        Random rng = newRandom();
        int v,v_fin,c,mask,z;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c=0;
            z=0;
            mask=0;
            v = rng.nextInt(MAX_8);
            if(v%2==1){
                c=1;
            }
            if(v>127) {
                v_fin = (v >> 1)+128;;
            }else {
                v_fin=(v >> 1);
            }
            if(v_fin==0)z=8;
            mask=c+z;
            v_fin=(v_fin<<8)+(mask<<4);
            assertEquals(v_fin,Alu.shiftRightA(v));
        }
    }

    @Test //OK
    void shiftRightL() {
        Random rng = newRandom();
        int v,v_fin,c,mask,z;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c=0;
            z=0;
            mask=0;
            v = rng.nextInt(MAX_8);
            if(v%2==1){
                c=1;
            }
            v_fin = (v>>1);
            if(v_fin==0)z=8;
            mask=c+z;
            v_fin=(v_fin<<8)+(mask<<4);
            assertEquals(v_fin,Alu.shiftRightL(v));
        }
    }

    @Test //OK
    void rotate() {
        Random rng = newRandom();
        int v,v_fin,c,mask,z;
        //Left
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c=z=0;
            v=rng.nextInt(MAX_8);
            if(v==0)z=8;
            if(v>127){
                c=1;
                v_fin=v-128;
                v_fin=(v_fin<<1)+1;
            }else {
                v_fin=(v<<1);
            }
            mask=c+z;
            v_fin=(v_fin<<8)+(mask<<4);
            assertEquals(v_fin, Alu.rotate(Alu.RotDir.LEFT, v));
        }
        //Right
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c=z=0;
            v=rng.nextInt(MAX_8);
            if(v==0)z=8;
            if(v%2==1){
                c=1;
                v_fin=(v>>1)+128;
            }else {
                v_fin=(v>>1);
            }
            mask=c+z;
            v_fin=(v_fin<<8)+(mask<<4);
            assertEquals(v_fin, Alu.rotate(Alu.RotDir.RIGHT, v));
        }
    }

    @Test //OK
    void rotateCarry() {
        Random rng = newRandom();
        int v,c,v_fin,mask,z;
        boolean carry;
        //Left
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            z=c=0;
            carry=rng.nextBoolean();
            v=rng.nextInt(MAX_8);
            if(v>127) {
                c=1;
                v_fin=v-128;
            }else {
                v_fin=v;
            }
            v_fin=(v_fin<<1);
            if(carry) v_fin++;
            if(v_fin==0) z=8;
            mask=c+z;
            v_fin=(v_fin<<8)+(mask<<4);
            assertEquals(v_fin,Alu.rotate(RotDir.LEFT, v,carry) );
        }
        //Right
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            z=c=0;
            carry=rng.nextBoolean();
            v=rng.nextInt(MAX_8);
            if(carry){
                v_fin=v+256;
            }else {
                v_fin=v;
            }
            if(v_fin%2==1) c=1;
            v_fin=v_fin>>1;
        if(v_fin==0) z=8;
        mask=c+z;
        v_fin=(v_fin<<8)+(mask<<4);
        assertEquals(v_fin,Alu.rotate(RotDir.RIGHT, v,carry) );
        }

    }

    @Test //OK
    void swap() {
        Random rng = newRandom();
        int v,v_fin,mask;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            mask=0;
            v=rng.nextInt(MAX_8);
            if(v==0) mask=8;
            v_fin=((v&0b1111)<<4)+((v&0b11110000)>>4);
            v_fin=(v_fin<<8)+(mask<<4);
            assertEquals(v_fin, Alu.swap(v));
        }
        assertEquals(0b00001010, Alu.unpackValue(Alu.swap(0b10100000)));
        assertEquals(0b0000, Alu.unpackFlags(Alu.swap(0b10100000)));
        assertEquals(0b0, Alu.unpackValue(Alu.swap(0b0)));
        assertEquals(0b10000000, Alu.unpackFlags(Alu.swap(0b0)));
    }

    @Test //OK
    void testBit() {
        assertThrows(IndexOutOfBoundsException.class,() -> Alu.testBit(0b10101, 8));
        Random rng = newRandom();
        int v,v_fin,mask,index,z;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
               z=0;
               index=rng.nextInt(7);
               v=rng.nextInt(MAX_8);
               if(((v & (int)Math.pow(2, index))>>index)==0) z=8;
               System.out.println((v & (int)Math.pow(2, index))>>index);
               mask=z+2;
               v_fin=(mask<<4);
               assertEquals(v_fin,Alu.testBit(v, index));
        }
    }

}
