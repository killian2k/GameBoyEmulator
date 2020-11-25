package ch.epfl.gameboj.component.cpu;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

@Disabled class ___CpuTest4 {
    final int MAX_16=0b1111_1111_1111_1111;
    final int MAX_15=0b111_1111_1111_1111;
    final int MAX_8=0b1111_1111;
    final int MAX_7=0b111_1111;
    final int MAX_4=0b1111;


    @Test
    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }

    /////////////////////////////////////////
    //             Additions               //
    /////////////////////////////////////////

    @Test
    void ADD_A_N8() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueAdd,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            valueAdd=valueA+valueN;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueN))>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {
                flag+=1; //C flags ?
                valueAdd=Bits.clip(8, valueAdd);
            }
            if((valueAdd)==0) flag+=8; //Z flags ?


            //System.out.println("Value A = "+valueA+" || value N ="+valueN);


            flag = (flag)<<4;
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);

            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueN); //8 bits value parameter


            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_A_N8.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {4,0,valueAdd,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void ADD_A_R8() { //OK

        Random rng = newRandom();
        int valueA,valueR,valueAdd,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueA=rng.nextInt(MAX_8);
            valueR=rng.nextInt(MAX_8);



            valueAdd=valueA+valueR;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueR))>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {
                flag+=1; //C flags ?
                valueAdd-=MAX_8+1;
            }
            if((valueAdd)==0) flag+=8; //Z flags ?


            flag = (flag)<<4;


            b.write(0, Opcode.LD_B_N8.encoding);
            b.write(1, valueR);
            b.write(2, Opcode.LD_A_N8.encoding);
            b.write(3, valueA);

            b.write(4, Opcode.ADD_A_B.encoding);


            cycleCpu(c, Opcode.ADD_A_B.cycles+Opcode.LD_B_N8.cycles+Opcode.LD_A_N8.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {5,0,valueAdd,flag,valueR,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void ADD_A_HLR() { //OK

        Random rng = newRandom();
        int valueA,valueR,valueAdd,flag,address,msb,lsb;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS ; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueA=rng.nextInt(MAX_8);
            valueR=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;

            r.write(address, valueR);

            valueAdd=valueA+valueR;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueR))>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {
                flag+=1; //C flags ?
                valueAdd=Bits.clip(8, valueAdd);
            }
            if((valueAdd)==0) flag+=8; //Z flags ?


            flag = (flag)<<4;


            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, Opcode.LD_A_N8.encoding);
            b.write(4, valueA);
            b.write(5, Opcode.ADD_A_HLR.encoding);


            cycleCpu(c, Opcode.ADD_A_HLR.cycles+Opcode.LD_A_N8.cycles+Opcode.LD_HL_N16.cycles);

            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println("\n msb ="+msb+" || lsb="+lsb+" || valueR ="+valueR+" || valueA ="+valueA);
             */

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {6,0,valueAdd,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void ADC_A_N8() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueAdd,flag,carry,valueCarry_A,valueCarry_Added;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);

            //Creating a random carry for testing
            carry=0;
            valueCarry_A=rng.nextInt(MAX_8);
            valueCarry_Added=rng.nextInt(MAX_8);

            if(valueCarry_A+valueCarry_Added>MAX_8) carry=1;
            //Active ou pas le carry (random)
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueCarry_A);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueCarry_Added);
            b.write(4, Opcode.LD_A_N8.encoding);
            b.write(5, valueA);
            b.write(6, Opcode.ADC_A_N8.encoding);
            b.write(7, valueN);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+2*Opcode.LD_A_N8.cycles+Opcode.ADC_A_N8.cycles);


            valueAdd=valueA+valueN+carry;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueN)+carry)>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {
                flag+=1; //C flags ?
                valueAdd=Bits.clip(8, valueAdd);
            }
            if((valueAdd)==0) flag+=8; //Z flags ?
            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }*/
            flag=(flag<<4);
            //System.out.println("\nflag = "+flag);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {8,0,valueAdd,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void ADC_A_R8() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueAdd,flag,carry,valueCarry_A,valueCarry_Added;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);

            //Creating a random carry for testing
            carry=0;
            valueCarry_A=rng.nextInt(MAX_8);
            valueCarry_Added=rng.nextInt(MAX_8);

            if(valueCarry_A+valueCarry_Added>MAX_8) carry=1;
            //Active ou pas le carry (random)
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueCarry_A);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueCarry_Added);
            b.write(4, Opcode.LD_B_N8.encoding);
            b.write(5, valueN);
            b.write(6, Opcode.LD_A_N8.encoding);
            b.write(7, valueA);
            b.write(8, Opcode.ADC_A_B.encoding);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+2*Opcode.LD_A_N8.cycles+Opcode.ADC_A_B.cycles+Opcode.LD_B_N8.cycles);


            valueAdd=valueA+valueN+carry;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueN)+carry)>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {
                flag+=1; //C flags ?
                valueAdd=Bits.clip(8, valueAdd);
            }
            if((valueAdd)==0) flag+=8; //Z flags ?
            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }*/
            flag=(flag<<4);
            //System.out.println("\nflag = "+flag);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {9,0,valueAdd,flag,valueN,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void ADC_A_HLR() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueAdd,flag,carry,valueCarry_A,valueCarry_Added,address,msb,lsb;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;

            r.write(address, valueN);

            //Creating a random carry for testing
            carry=0;
            valueCarry_A=rng.nextInt(MAX_8);
            valueCarry_Added=rng.nextInt(MAX_8);

            if(valueCarry_A+valueCarry_Added>MAX_8) carry=1;
            //Active ou pas le carry (random)
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueCarry_A);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueCarry_Added);
            b.write(4, Opcode.LD_HL_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);
            b.write(7, Opcode.LD_A_N8.encoding);
            b.write(8, valueA);
            b.write(9, Opcode.ADC_A_HLR.encoding);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+2*Opcode.LD_A_N8.cycles+Opcode.ADC_A_HLR.cycles+Opcode.LD_HL_N16.cycles);


            valueAdd=valueA+valueN+carry;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueN)+carry)>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {
                flag+=1; //C flags ?
                valueAdd=Bits.clip(8, valueAdd);
            }
            if((valueAdd)==0) flag+=8; //Z flags ?
            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }*/
            flag=(flag<<4);
            //System.out.println("\nflag = "+flag);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {10,0,valueAdd,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void ADD_HL_R16() { //Probleme H // pas de bits signés pour celle là !

        Random rng = newRandom();
        int valueHL,valueBC,valueAdd,flag,msb1,msb2,msb3,lsb1,lsb2,lsb3,valueRandomA,valueRandom_Added;
        int valueRandomFin;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            //Computing both 16 bits values and their msb + lsb
            //HL
            msb1=rng.nextInt(MAX_8);
            lsb1=rng.nextInt(MAX_8-10)+10;
            valueHL=(msb1<<8)+lsb1;
            //BC
            msb2=rng.nextInt(MAX_8);
            lsb2=rng.nextInt(MAX_8-10)+10;
            valueBC=(msb2<<8)+lsb2;
            //Final added
            valueAdd=valueHL+valueBC;
            //System.out.println("HLB ="+Bits.clip(8,valueHL)+" || BCB ="+Bits.clip(8,valueBC));

            /*System.out.println(valueHL+ "(HL) = "+Integer.toBinaryString(valueHL)+"\n"
                    +valueBC+"(BC) = "+Integer.toBinaryString(valueBC)
                    +"\nMsb1 = "+msb1+" = "+Integer.toBinaryString(msb1)+
                    "\nLsb1 = "+lsb1+" = "+Integer.toBinaryString(lsb1)+
                    "\nMsb2 = "+msb2+" = "+Integer.toBinaryString(msb2)
                    +"\nLsb2 = "+lsb2+" = "+Integer.toBinaryString(lsb2)
                    );*/

            if((Bits.clip(4,msb1)+Bits.clip(4,msb2) + ((lsb2+lsb1)>0xFF?1:0))>MAX_4) {
                flag+=2; //H flags on the MSBS ?
            }
            if(((msb1+msb2 + ((lsb2+lsb1)>0xFF?1:0))>MAX_8)) {
                flag+=1; //C flags on the MSBS ?
                valueAdd=Bits.clip(16,valueAdd);
                System.out.println("c");
            }
            msb3=valueAdd>>8;
            lsb3=valueAdd & MAX_8;

            //System.out.println("Valeur Added ="+Integer.toBinaryString(valueAdd)+" = "+valueAdd);
            //System.out.println("msb3 ="+Integer.toBinaryString(msb3)+" = "+msb3 + " || lsb3 ="+Integer.toBinaryString(lsb3)+" = "+lsb3);




            //Change les flags random
            valueRandomA=rng.nextInt(MAX_8);
            valueRandom_Added=rng.nextInt(MAX_8);
            valueRandomFin=valueRandomA+valueRandom_Added;
            valueRandomFin=Bits.clip(8,valueRandomFin);

            if(Bits.clip(8, valueRandomA+valueRandom_Added)==0) flag+=8; //Keep Z flag
            //if (Bits.clip(8, msb3 + msb2) == 0)flag+=8;

            flag = (flag)<<4;


            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueRandomA);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueRandom_Added);

            //Write in HL
            b.write(4, Opcode.LD_HL_N16.encoding);
            b.write(5, lsb1);
            b.write(6, msb1);
            //Write in BC
            b.write(7, Opcode.LD_BC_N16.encoding);
            b.write(8, lsb2);
            b.write(9, msb2);
            //Adding both
            b.write(10, Opcode.ADD_HL_BC.encoding);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_A_N8.cycles+Opcode.LD_HL_N16.cycles+Opcode.LD_BC_N16.cycles+Opcode.ADD_HL_BC.cycles);

            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println();
             */


            //A get the addition value, F the flags
            /*System.out.println(Integer.toBinaryString(msb1));
            System.out.println(Integer.toBinaryString(msb2));
            System.out.println(Integer.toBinaryString(msb1+msb2));
            System.out.println(((lsb1+lsb2)>MAX_8?1:0));
            System.out.println(((msb1+msb2 + ((lsb1+lsb2)>MAX_8?1:0)>MAX_8)));
            System.out.println(Integer.toBinaryString(flag));*/
            System.out.println("re def:" + Integer.toBinaryString(msb2*256 + lsb2));
            System.out.println("HL def:" + Integer.toBinaryString(msb1*256 + lsb1));
            System.out.println("sum:" + Integer.toBinaryString(msb1*256 + lsb1 + msb2*256 + lsb2));
            System.out.println("flags = " + Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[3]));
            System.out.println("voulu = " + Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[3]));
            assertArrayEquals(new int[] {11,0,valueRandomFin,flag,msb2,lsb2,0,0,msb3,lsb3}, c._testGetPcSpAFBCDEHL());
            System.out.println("--");
        }
    }

    @Test
    void INC_R8() { //OK

        Random rng = newRandom();
        int valueA,valueR,valueAdd,flag,valueComparedRandom;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueR=rng.nextInt(MAX_8);
            valueA=rng.nextInt(MAX_8);
            valueComparedRandom=rng.nextInt(MAX_8);
            if((valueA+valueComparedRandom)>MAX_8) flag+=1;
            valueAdd=valueA+valueComparedRandom;


            //System.out.println("ValueA = "+valueA+ " || ValueB= "+valueComparedRandom+" || tot ="+valueAdd);

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueComparedRandom);
            b.write(4, Opcode.LD_C_N8.encoding);
            b.write(5, valueR);

            if(valueAdd>MAX_8) {
                valueAdd=valueAdd-256;
            }

            //System.out.println("Cliped ="+valueAdd);





            b.write(6, Opcode.INC_C.encoding);


            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_C_N8.cycles+Opcode.LD_A_N8.cycles+Opcode.INC_C.cycles);

            if((Bits.clip(4,valueR)+1>MAX_4)) flag+=2; //H flags 

            if(valueR==MAX_8) {
                valueR=0;
            }else {
                valueR+=1;
            }
            if((valueR)==0) flag+=8; //Z flags ?
            flag = (flag)<<4;
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {7,0,valueAdd,flag,0,valueR,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void INC_HLR() { //OK

        Random rng = newRandom();
        int valueA,valueR,valueAdd,flag,valueComparedRandom,address,msb,lsb;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;
            valueR=rng.nextInt(MAX_8);
            valueA=rng.nextInt(MAX_8);
            valueComparedRandom=rng.nextInt(MAX_8);
            if((valueA+valueComparedRandom)>MAX_8) flag+=1;
            valueAdd=valueA+valueComparedRandom;
            r.write(address, valueR);


            //System.out.println("ValueA = "+valueA+ " || ValueB= "+valueComparedRandom+" || tot ="+valueAdd);
            r.write(address, valueR);
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueComparedRandom);
            b.write(4, Opcode.LD_HL_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);
            b.write(7, Opcode.INC_HLR.encoding);
            if(valueAdd>MAX_8) {
                valueAdd=valueAdd-256;
            }

            //System.out.println("Cliped ="+valueAdd);








            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_HL_N16.cycles+Opcode.LD_A_N8.cycles+Opcode.INC_HLR.cycles);

            if((Bits.clip(4,valueR)+1>MAX_4)) flag+=2; //H flags 

            if(valueR==MAX_8) {
                valueR=0;
            }else {
                valueR+=1;
            }
            if((valueR)==0) flag+=8; //Z flags ?
            flag = (flag)<<4;
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {8,0,valueAdd,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
            assertEquals(valueR,r.read(address));
        }
    }

    @Test
    void INC_R16() { //OK

        Random rng = newRandom();
        int valueA_1,valueA_2,valueR,valueAdd,flag,valueComparedRandom,address,msb,lsb,msb1,lsb1;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            valueR=(msb<<8)+lsb;
            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueAdd=valueA_1+valueA_2;

            if((Bits.clip(4,valueA_1)+Bits.clip(4,valueA_2))>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {
                flag+=1; //C flags ?
                valueAdd-=(MAX_8+1);
            }
            if((valueAdd)==0) flag+=8; //Z flags ?


            flag = (flag)<<4;


            //System.out.println("ValueA = "+valueA+ " || ValueB= "+valueComparedRandom+" || tot ="+valueAdd);
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            b.write(4, Opcode.LD_BC_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);
            b.write(7, Opcode.INC_BC.encoding);

            //System.out.println("Cliped ="+valueAdd);
            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_BC_N16.cycles+Opcode.LD_A_N8.cycles+Opcode.INC_BC.cycles);
            if(valueR==MAX_16) {
                valueR=0;
            }else {
                valueR+=1;
            }
            lsb1=Bits.clip(8, valueR);
            msb1=(valueR>>8);
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {8,0,valueAdd,flag,msb1,lsb1,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        }
    }

    @Test
    void ADD_SP_E8() { //OK

        Random rng = newRandom();
        int valueSP,valueE8,valueAdd,flag,msb,lsb,testLsb2,testLsb1;
        boolean negative;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            negative=rng.nextBoolean();
            valueSP=rng.nextInt(MAX_15);
            if(negative) valueSP=-valueSP;
            negative=rng.nextBoolean();
            valueE8=rng.nextInt(MAX_7);
            if(negative) valueE8=-valueE8;
            valueAdd=valueE8+valueSP;
            //System.out.print(Integer.toBinaryString(valueAdd)+"\n");

            //Cliping the value on 8 bits
            valueE8=Bits.clip(8, valueE8);
            valueSP=Bits.clip(16, valueSP);
            //System.out.print(Integer.toBinaryString(Bits.clip(8,valueSP))+" = "+Bits.clip(8,valueSP)+"\n");
            msb=valueSP>>8;
        lsb=valueSP&MAX_8;
        /*
        System.out.println(valueSP+"\n"+valueE8
                +" || value SP + E8 = "+(Bits.clip(8,valueSP)+(valueE8))
                ); */

        testLsb1=Bits.clip(4,valueSP);
        testLsb2=Bits.clip(4,valueE8);

        if(testLsb1+testLsb2>MAX_4) flag+=2; //H flags ?
        if(lsb+valueE8>MAX_8){
            flag+=1; //C flags ?
        }
        //Z flags is OFF
        flag = (flag)<<4;
        //System.out.println(flag);


        //valueAdd=Bits.clip(16, valueAdd);

        b.write(0, Opcode.LD_SP_N16.encoding);
        b.write(1, lsb);
        b.write(2, msb);

        b.write(4, Opcode.ADD_SP_N.encoding);
        b.write(5, valueE8);

        valueAdd=Bits.clip(16, valueAdd);


        cycleCpu(c, Opcode.ADD_SP_N.cycles+Opcode.ADD_SP_N.cycles);
        /*
        for(int v : c._testGetPcSpAFBCDEHL()) {
            System.out.print(v+"|");
        }*/
        //System.out.println();
        //System.out.println("\n"+Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[1])+" = "+Integer.toBinaryString(valueAdd));

        //A get the addition value, F the flags
        assertArrayEquals(new int[] {6,valueAdd,0,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
        //Test pour la valeur -128 et -256

        c = new Cpu();
        r = new Ram(0xFFFF);
        b = connect(c, r);
        flag=0;


        valueSP=-MAX_15-1;
        valueE8=-128;
        valueAdd=valueE8+valueSP;
        //System.out.print(Integer.toBinaryString(valueAdd)+"\n");

        //Cliping the value on 8 bits
        valueE8=Bits.clip(8, valueE8);
        valueSP=Bits.clip(16, valueSP);
        //System.out.print(Integer.toBinaryString(Bits.clip(8,valueSP))+" = "+Bits.clip(8,valueSP)+"\n");
        msb=valueSP>>8;
        lsb=valueSP&MAX_8;
        /*System.out.println(valueSP+"\n"+valueE8
                +" || value SP + E8 = "+(Bits.clip(8,valueSP)+(valueE8))
                );*/

        testLsb1=Bits.clip(4,valueSP);
        testLsb2=Bits.clip(4,valueE8);

        if(testLsb1+testLsb2>MAX_4) flag+=2; //H flags ?
        if(lsb+valueE8>MAX_8){
            flag+=1; //C flags ?
        }
        //Z flags is OFF
        flag = (flag)<<4;
        //System.out.println(flag);


        //valueAdd=Bits.clip(16, valueAdd);

        b.write(0, Opcode.LD_SP_N16.encoding);
        b.write(1, lsb);
        b.write(2, msb);

        b.write(4, Opcode.ADD_SP_N.encoding);
        b.write(5, valueE8);

        valueAdd=Bits.clip(16, valueAdd);


        cycleCpu(c, Opcode.ADD_SP_N.cycles+Opcode.ADD_SP_N.cycles);
        /*
        for(int v : c._testGetPcSpAFBCDEHL()) {
            System.out.print(v+"|");
        }
         */
        //System.out.println();
        //System.out.println("\n"+Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[1])+" = "+Integer.toBinaryString(valueAdd));

        //A get the addition value, F the flags
        assertArrayEquals(new int[] {6,valueAdd,0,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

    }

    @Test
    void LD_HL_SP_N8() { //OK

        Random rng = newRandom();
        int valueSP,valueE8,flag,msb,lsb,valueAdd;
        boolean negative;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            //Donc SP + E8 a déjà été testé , on va tester le loading
            valueSP=rng.nextInt(MAX_15);
            negative=rng.nextBoolean();
            if(negative) valueSP=-valueSP;
            valueE8=rng.nextInt(MAX_7);
            negative=rng.nextBoolean();
            if(negative) valueE8=-valueE8;
            msb=valueSP>>8;
        msb=Bits.clip(8, msb);
        lsb=Bits.clip(8, valueSP);

        valueAdd=valueSP+valueE8;
        valueSP=Bits.clip(16, valueSP);
        valueE8=Bits.clip(8, valueE8);

        //System.out.println("valueSP = "+valueSP+" || valueE8 ="+valueE8);

        valueAdd=Bits.clip(16, valueAdd);

        b.write(0, Opcode.LD_SP_N16.encoding);
        b.write(1, lsb);
        b.write(2, msb);

        b.write(3, Opcode.LD_HL_SP_N8.encoding);
        b.write(4, valueE8);



        cycleCpu(c, Opcode.ADD_SP_N.cycles+Opcode.LD_HL_SP_N8.cycles);
        /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println();*/

        //System.out.println("\n"+Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[1])+" = "+Integer.toBinaryString(valueAdd));
        lsb=Bits.clip(8, valueAdd);
        msb=valueAdd>>8;
        //A get the addition value, F the flags
        assertEquals(valueSP,c._testGetPcSpAFBCDEHL()[1]);
        assertEquals(msb,c._testGetPcSpAFBCDEHL()[8]);
        assertEquals(lsb,c._testGetPcSpAFBCDEHL()[9]);

        //assertArrayEquals(new int[] {6,valueSP,0,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
        }
    }

    /////////////////////////////////////////
    //           Subbstractions            //
    /////////////////////////////////////////

    @Test
    void SUB_A_N8() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueSub,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            valueSub=valueA-valueN;

            //System.out.println("valueA = "+valueA+" || valueN = "+valueN+" || A-N ="+valueSub);
            //System.out.println(Integer.toBinaryString(valueA>>4)+"\n"+Integer.toBinaryString(valueN>>4));

            if(((valueA&MAX_4)-(valueN&MAX_4))<0) flag+=2; //H flags ?
            if(((valueSub)<0)) {
                flag+=1; //C flags ?
                valueSub+=MAX_8+1;
            }
            if((valueSub)==0) flag+=8; //Z flags ?
            flag+=4; //N flag on !


            //System.out.println("Value A = "+valueA+" || value N ="+valueN);


            flag = (flag)<<4;
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);

            b.write(2, Opcode.SUB_A_N8.encoding);
            b.write(3, valueN); //8 bits value parameter


            cycleCpu(c, Opcode.SUB_A_N8.cycles+Opcode.LD_A_N8.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {4,0,valueSub,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void SUB_A_R8() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueSub,flag,randomNB;
        Opcode[] tabOP = {Opcode.SUB_A_B,Opcode.SUB_A_C,Opcode.SUB_A_D,Opcode.SUB_A_E,
                Opcode.SUB_A_H,Opcode.SUB_A_L};

        Opcode[] tabR = {Opcode.LD_B_N8,Opcode.LD_C_N8,Opcode.LD_D_N8,Opcode.LD_E_N8,
                Opcode.LD_H_N8,Opcode.LD_L_N8};

        Opcode randomOpcode;
        Opcode randomRegister;

        Cpu c;
        Ram r;
        Bus b;

        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            randomNB=rng.nextInt(6);
            randomOpcode=tabOP[randomNB];
            randomRegister=tabR[randomNB];
            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            valueSub=valueA-valueN;

            //System.out.println("valueA = "+valueA+" || valueN = "+valueN+" || A-N ="+valueSub);
            //System.out.println(Integer.toBinaryString(valueA>>4)+"\n"+Integer.toBinaryString(valueN>>4));

            if(((valueA&MAX_4)-(valueN&MAX_4))<0) flag+=2; //H flags ?
            if(((valueSub)<0)) {
                flag+=1; //C flags ?
                valueSub+=MAX_8+1;
            }
            if((valueSub)==0) flag+=8; //Z flags ?
            flag+=4; //N flag on !


            //System.out.println("Value A = "+valueA+" || value R ="+valueN);


            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, randomRegister.encoding);
            b.write(3, valueN);
            b.write(4, randomOpcode.encoding);


            cycleCpu(c, randomRegister.cycles+Opcode.LD_A_N8.cycles+randomOpcode.cycles);
            /*
            System.out.println("Random number = "+randomNB);
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }*/

            //A get the addition value, F the flags
            assertEquals(valueSub,c._testGetPcSpAFBCDEHL()[2]);
            assertEquals(flag,c._testGetPcSpAFBCDEHL()[3]);
            assertEquals(valueN,c._testGetPcSpAFBCDEHL()[randomNB+4]);
            //assertArrayEquals(new int[] {5,0,valueSub,flag,valueN,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void SUB_A_HLR() { //OK

        Random rng = newRandom();
        int valueA,valueR,valueSub,flag,address,msb,lsb;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS ; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueA=rng.nextInt(MAX_8);
            valueR=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;

            r.write(address, valueR);

            valueSub=valueA-valueR;

            if((Bits.clip(4,valueA)-Bits.clip(4,valueR))<0) flag+=2; //H flags ?
            if(((valueSub)<0)) {
                flag+=1; //C flags ?
                valueSub+=MAX_8+1;
            }
            if((valueSub)==0) flag+=8; //Z flags ?
            flag+=4; //N flags


            flag = (flag)<<4;


            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, Opcode.LD_A_N8.encoding);
            b.write(4, valueA);
            b.write(5, Opcode.SUB_A_HLR.encoding);


            cycleCpu(c, Opcode.ADD_A_HLR.cycles+Opcode.LD_A_N8.cycles+Opcode.LD_HL_N16.cycles);


            /*for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println("\n msb ="+msb+" || lsb="+lsb+" || valueR ="+valueR+" || valueA ="+valueA);
             */

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {6,0,valueSub,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void SBC_A_N8() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueSub,flag,carry,valueCarry_A,valueCarry_Added;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);

            //Creating a random carry for testing
            carry=0;
            valueCarry_A=rng.nextInt(MAX_8);
            valueCarry_Added=rng.nextInt(MAX_8);

            if(valueCarry_A+valueCarry_Added>MAX_8) carry=1;

            //Active ou pas le carry (random)
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueCarry_A);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueCarry_Added);

            b.write(4, Opcode.LD_A_N8.encoding);
            b.write(5, valueA);
            b.write(6, Opcode.SBC_A_N8.encoding);
            b.write(7, valueN);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+2*Opcode.LD_A_N8.cycles+Opcode.ADC_A_N8.cycles);


            valueSub=valueA-valueN-carry;


            if((Bits.clip(4,valueA)-Bits.clip(4,valueN)-carry)<0) flag+=2; //H flags ?
            if(((valueSub)<0)) {
                flag+=1; //C flags ?
                valueSub+=MAX_8+1;
            }
            if((valueSub)==0) flag+=8; //Z flags ?
            flag+=4; // N flag
            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }*/
            flag=(flag<<4);
            //System.out.println("\nflag = "+flag);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {8,0,valueSub,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void SBC_A_R8() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueSub,flag,carry,valueCarry_A,valueCarry_Added;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);

            //Creating a random carry for testing
            carry=0;
            valueCarry_A=rng.nextInt(MAX_8);
            valueCarry_Added=rng.nextInt(MAX_8);

            if(valueCarry_A+valueCarry_Added>MAX_8) carry=1;

            //Active ou pas le carry (random)
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueCarry_A);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueCarry_Added);

            b.write(4, Opcode.LD_A_N8.encoding);
            b.write(5, valueA);
            b.write(6, Opcode.LD_B_N8.encoding);
            b.write(7, valueN);
            b.write(8, Opcode.SBC_A_B.encoding);


            cycleCpu(c, Opcode.ADD_A_N8.cycles+2*Opcode.LD_A_N8.cycles+Opcode.SBC_A_B.cycles+Opcode.LD_B_N8.cycles);


            valueSub=valueA-valueN-carry;


            if((Bits.clip(4,valueA)-Bits.clip(4,valueN)-carry)<0) flag+=2; //H flags ?
            if(((valueSub)<0)) {
                flag+=1; //C flags ?
                valueSub+=MAX_8+1;
            }
            if((valueSub)==0) flag+=8; //Z flags ?
            flag+=4; // N flag
            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }*/
            flag=(flag<<4);
            //System.out.println("\nflag = "+flag);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {9,0,valueSub,flag,valueN,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void SBC_A_HLR() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueSub,flag,carry,valueCarry_A,valueCarry_Added,address,msb,lsb;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;

            r.write(address, valueN);

            //Creating a random carry for testing
            carry=0;
            valueCarry_A=rng.nextInt(MAX_8);
            valueCarry_Added=rng.nextInt(MAX_8);

            if(valueCarry_A+valueCarry_Added>MAX_8) carry=1;
            //Active ou pas le carry (random)
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueCarry_A);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueCarry_Added);

            b.write(4, Opcode.LD_HL_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);
            b.write(7, Opcode.LD_A_N8.encoding);
            b.write(8, valueA);
            b.write(9, Opcode.SBC_A_HLR.encoding);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+2*Opcode.LD_A_N8.cycles+Opcode.SBC_A_HLR.cycles+Opcode.LD_HL_N16.cycles);


            valueSub=valueA-valueN-carry;

            if((Bits.clip(4,valueA)-Bits.clip(4,valueN)-carry)<0) flag+=2; //H flags ?
            if(((valueSub)<0)) {
                flag+=1; //C flags ?
                valueSub+=MAX_8+1;
            }
            if((valueSub)==0) flag+=8; //Z flags ?
            flag+=4; //N flag
            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }*/
            flag=(flag<<4);
            //System.out.println("\nflag = "+flag);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {10,0,valueSub,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void DEC_R8() { //OK

        Random rng = newRandom();
        int valueA,valueR,valueAdd,flag,valueComparedRandom;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueR=rng.nextInt(MAX_8);
            valueA=rng.nextInt(MAX_8);
            valueComparedRandom=rng.nextInt(MAX_8);
            if((valueA+valueComparedRandom)>MAX_8) flag+=1; //Save old carry
            valueAdd=valueA+valueComparedRandom;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueComparedRandom);
            b.write(4, Opcode.LD_C_N8.encoding);
            b.write(5, valueR);

            if(valueAdd>MAX_8) {
                valueAdd=valueAdd-256;
            }

            //System.out.println("Cliped ="+valueAdd);





            b.write(6, Opcode.DEC_C.encoding);


            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_C_N8.cycles+Opcode.LD_A_N8.cycles+Opcode.INC_C.cycles);

            if((Bits.clip(4,valueR)-1<0)) flag+=2; //H flags 

            if(valueR==0) {
                valueR=MAX_8;
            }else {
                valueR-=1;
            }
            if((valueR)==0) flag+=8; //Z flags ?
            flag+=4; //N flag
            flag = (flag)<<4;
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {7,0,valueAdd,flag,0,valueR,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void DEC_HLR() { //OK

        Random rng = newRandom();
        int valueA,valueR,valueAdd,flag,valueComparedRandom,address,msb,lsb;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;
            valueR=rng.nextInt(MAX_8);
            valueA=rng.nextInt(MAX_8);
            valueComparedRandom=rng.nextInt(MAX_8);
            if((valueA+valueComparedRandom)>MAX_8) flag+=1; //Save the random flag
            valueAdd=valueA+valueComparedRandom;
            r.write(address, valueR);


            //System.out.println("ValueA = "+valueA+ " || ValueB= "+valueComparedRandom+" || tot ="+valueAdd);
            r.write(address, valueR);
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueComparedRandom);
            b.write(4, Opcode.LD_HL_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);
            b.write(7, Opcode.DEC_HLR.encoding);
            if(valueAdd>MAX_8) {
                valueAdd=valueAdd-256;
            }

            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_HL_N16.cycles+Opcode.LD_A_N8.cycles+Opcode.INC_HLR.cycles);

            if((Bits.clip(4,valueR)-1<0)) flag+=2; //H flags 

            if(valueR==0) {
                valueR=MAX_8;
            }else {
                valueR-=1;
            }
            if((valueR)==0) flag+=8; //Z flags ?
            flag+=4; //N flag
            flag = (flag)<<4;

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {8,0,valueAdd,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
            assertEquals(valueR,r.read(address));
        }
    }

    @Test
    void CP_A_N8() {  //OK , Attention ! Ajouts de familly dans combinFlag ! Pour les CP_...

        Random rng = newRandom();
        int valueA,valueN,valueSub,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            valueSub=valueA-valueN;

            //System.out.println("valueA = "+valueA+" || valueN = "+valueN+" || A-N ="+valueSub);
            //System.out.println(Integer.toBinaryString(valueA>>4)+"\n"+Integer.toBinaryString(valueN>>4));

            if(((valueA&MAX_4)-(valueN&MAX_4))<0) flag+=2; //H flags ?
            if(((valueSub)<0)) {
                flag+=1; //C flags ?
                valueSub+=MAX_8+1;
            }
            if((valueSub)==0) flag+=8; //Z flags ?
            flag+=4; //N flag on !


            //System.out.println("Value A = "+valueA+" || value N ="+valueN);


            flag = (flag)<<4;
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);

            b.write(2, Opcode.CP_A_N8.encoding);
            b.write(3, valueN); //8 bits value parameter


            cycleCpu(c, Opcode.CP_A_N8.cycles+Opcode.LD_A_N8.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {4,0,valueA,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void CP_A_R8() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueSub,flag,randomNB;
        Opcode[] tabOP = {Opcode.CP_A_B,Opcode.CP_A_C,Opcode.CP_A_D,Opcode.CP_A_E,
                Opcode.CP_A_H,Opcode.CP_A_L};

        Opcode[] tabR = {Opcode.LD_B_N8,Opcode.LD_C_N8,Opcode.LD_D_N8,Opcode.LD_E_N8,
                Opcode.LD_H_N8,Opcode.LD_L_N8};

        Opcode randomOpcode;
        Opcode randomRegister;

        Cpu c;
        Ram r;
        Bus b;

        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            randomNB=rng.nextInt(6);
            randomOpcode=tabOP[randomNB];
            randomRegister=tabR[randomNB];
            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            valueSub=valueA-valueN;

            //System.out.println("valueA = "+valueA+" || valueN = "+valueN+" || A-N ="+valueSub);
            //System.out.println(Integer.toBinaryString(valueA>>4)+"\n"+Integer.toBinaryString(valueN>>4));

            if(((valueA&MAX_4)-(valueN&MAX_4))<0) flag+=2; //H flags ?
            if(((valueSub)<0)) {
                flag+=1; //C flags ?
                valueSub+=MAX_8+1;
            }
            if((valueSub)==0) flag+=8; //Z flags ?
            flag+=4; //N flag on !


            //System.out.println("Value A = "+valueA+" || value R ="+valueN);


            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, randomRegister.encoding);
            b.write(3, valueN);
            b.write(4, randomOpcode.encoding);


            cycleCpu(c, randomRegister.cycles+Opcode.LD_A_N8.cycles+randomOpcode.cycles);
            /*
            System.out.println("Random number = "+randomNB);
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }*/

            //A get the addition value, F the flags
            assertEquals(valueA,c._testGetPcSpAFBCDEHL()[2]);
            assertEquals(flag,c._testGetPcSpAFBCDEHL()[3]);
            assertEquals(valueN,c._testGetPcSpAFBCDEHL()[randomNB+4]);
        }
    }

    @Test
    void CP_A_HLR() { //OK

        Random rng = newRandom();
        int valueA,valueR,valueSub,flag,address,msb,lsb;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS ; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;


            valueA=rng.nextInt(MAX_8);
            valueR=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;

            r.write(address, valueR);

            valueSub=valueA-valueR;

            if((Bits.clip(4,valueA)-Bits.clip(4,valueR))<0) flag+=2; //H flags ?
            if(((valueSub)<0)) {
                flag+=1; //C flags ?
                valueSub+=MAX_8+1;
            }
            if((valueSub)==0) flag+=8; //Z flags ?
            flag+=4; //N flags


            flag = (flag)<<4;


            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, Opcode.LD_A_N8.encoding);
            b.write(4, valueA);
            b.write(5, Opcode.CP_A_HLR.encoding);


            cycleCpu(c,Opcode.CP_A_HLR.cycles+Opcode.LD_A_N8.cycles+Opcode.LD_HL_N16.cycles);


            /*for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println("\n msb ="+msb+" || lsb="+lsb+" || valueR ="+valueR+" || valueA ="+valueA);
             */

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {6,0,valueA,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void DEC_R16() { //OK

        Random rng = newRandom();
        int valueA_1,valueA_2,valueR,valueAdd,flag,msb,lsb,msb1,lsb1;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            valueR=(msb<<8)+lsb;
            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueAdd=valueA_1+valueA_2;

            if((Bits.clip(4,valueA_1)+Bits.clip(4,valueA_2))>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {
                flag+=1; //C flags ?
                valueAdd-=(MAX_8+1);
            }
            if((valueAdd)==0) flag+=8; //Z flags ?

            flag = (flag)<<4;


            //System.out.println("ValueA = "+valueA+ " || ValueB= "+valueComparedRandom+" || tot ="+valueAdd);
            //Random flags
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);


            b.write(4, Opcode.LD_BC_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);
            b.write(7, Opcode.DEC_BC.encoding);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_BC_N16.cycles+Opcode.LD_A_N8.cycles+Opcode.DEC_BC.cycles);
            if(valueR==0) {
                valueR=MAX_8+1;
            }else {
                valueR-=1;
            }
            lsb1=Bits.clip(8, valueR);
            msb1=(valueR>>8);
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {8,0,valueAdd,flag,msb1,lsb1,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        }
    }

    /////////////////////////////////////////
    //         Bits Operations 1           //
    /////////////////////////////////////////

    @Test
    void AND_A_N8() { 
        Random rng = newRandom();
        int valueA,valueN,valueAND,valueAdd,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            valueAND=valueA&valueN;

            //System.out.println("value R = "+Integer.toBinaryString(valueR));

            //System.out.println("value R slam = "+Integer.toBinaryString(valueRslam));
            //System.out.println("Wanted = "+Integer.toBinaryString(25));
            //System.out.println(Opcode.SLA_A.family);

            if(valueAND==0)flag+=8; //Z flag ?
            flag+=2; //H flag is ON
            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.AND_A_N8.encoding);
            b.write(3, valueN);


            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.AND_A_N8.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {4,0,valueAND,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }
    
    @Test
    void AND_A_R8() { 
        Random rng = newRandom();
        int valueA,valueN,valueAND,valueAdd,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            valueAND=valueA&valueN;

            //System.out.println("value R = "+Integer.toBinaryString(valueR));

            //System.out.println("value R slam = "+Integer.toBinaryString(valueRslam));
            //System.out.println("Wanted = "+Integer.toBinaryString(25));
            //System.out.println(Opcode.SLA_A.family);

            if(valueAND==0)flag+=8; //Z flag ?
            flag+=2; //H flag is ON
            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.LD_L_N8.encoding);
            b.write(3, valueN);
            b.write(4, Opcode.AND_A_L.encoding);


            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.AND_A_N8.cycles+Opcode.LD_B_N8.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {6,0,valueAND,flag,0,0,0,0,0,valueN}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test    
    void OR_A_N8() { 
        Random rng = newRandom();
        int valueA,valueN,valueOR,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            valueOR=(valueA)|(valueN);

            //System.out.println("value R slam = "+Integer.toBinaryString(valueRslam));
            //System.out.println("Wanted = "+Integer.toBinaryString(25));
            //System.out.println(Opcode.SLA_A.family);

            if(valueOR==0)flag+=8; //Z flag ?
            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.OR_A_N8.encoding);
            b.write(3, valueN);


            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.OR_A_N8.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {4,0,valueOR,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    
    /////////////////////////////////////////
    //              Décalage               //
    /////////////////////////////////////////

    @Test
    void SLA_R8() { //OK
        Random rng = newRandom();
        int valueR,valueRslam,valueAdd,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            boolean flagZ=false,flagC=false;

            valueR=rng.nextInt(MAX_8);
            //System.out.println("value R = "+Integer.toBinaryString(valueR));
            valueRslam=Bits.clip(8, (valueR<<1));
            //System.out.println(Integer.toBinaryString(valueR));
            //System.out.println(Integer.toBinaryString(valueRslam));
            //System.out.println("value R slam = "+Integer.toBinaryString(valueRslam));
            //System.out.println("Wanted = "+Integer.toBinaryString(218));
            //System.out.println(Opcode.SLA_A.family);


            if(valueR>MAX_7) {
                flag+=1; //C flags ?
                flagC=true;
            }
            if(valueRslam==0) {
                flag+=8; //Z flag ?
                flagZ=true;
            }


            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueR);
            b.write(3, 0xCB);
            b.write(4, Opcode.SLA_A.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.SLA_A.cycles);



            //A get the addition value, F the flags
            assertArrayEquals(new int[] {5,0,valueRslam,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            //assertArrayEquals(new int[] {5,0,valueRslam,Alu.maskZNHC(flagZ, false, false, flagC),0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void SLA_HL() { //OK
        Random rng = newRandom();
        int valueR,valueRslam,flag,msb,lsb,address;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;
            if(address < 7) {
                address = 7;
                msb = 0;
                lsb = 7;
            }
            valueR=rng.nextInt(MAX_8);
            r.write(address, valueR);

            //System.out.println("value R = "+Integer.toBinaryString(valueR));
            valueRslam=(valueR<<1);
            valueRslam=Bits.clip(8, valueRslam);
            //System.out.println("value R slam = "+Integer.toBinaryString(valueRslam));
            //System.out.println("Wanted = "+Integer.toBinaryString(181));
            //System.out.println(Opcode.SLA_A.family);


            if(valueR>MAX_7) {
                flag+=1; //C flags ?
            }
            if(valueRslam==0)flag+=8; //Z flag ?
            flag = (flag)<<4;

            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, 0xCB);
            b.write(4, Opcode.SLA_HLR.encoding);

            cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.SLA_HLR.cycles);

            //A get the addition value, F the flags
            /*System.out.println("flag");
            System.out.println("voulu: "+Integer.toBinaryString(flag));
            System.out.println("obten: "+Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[3]));*/
            assertEquals(flag,c._testGetPcSpAFBCDEHL()[3]);
            /*System.out.println("val");
            System.out.println("voulu: "+Integer.toBinaryString(valueRslam));
            System.out.println("obten: "+Integer.toBinaryString(r.read(address)));
            System.out.println(address);*/
            assertEquals(valueRslam,r.read(address));
        }
    }

    @Test
    void SRA_R8() { //OK
        Random rng = newRandom();
        int valueR,valueRslam,valueAdd,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            boolean flagZ=false,flagC=false;

            valueR=rng.nextInt(MAX_8);
            //System.out.println("value R = "+Integer.toBinaryString(valueR));
            valueRslam=Bits.clip(8, Bits.signExtend8(valueR)>>1);
            //System.out.println(Integer.toBinaryString(valueR));
            //System.out.println(Integer.toBinaryString(valueRslam));
            //System.out.println("value R slam = "+Integer.toBinaryString(valueRslam));
            //System.out.println("Wanted = "+Integer.toBinaryString(218));
            //System.out.println(Opcode.SLA_A.family);


            if(valueR%2==1) {
                flag+=1; //C flags ?
                flagC=true;
            }
            if(valueRslam==0) {
                flag+=8; //Z flag ?
                flagZ=true;
            }


            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueR);
            b.write(3, 0xCB);
            b.write(4, Opcode.SRA_A.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.SRA_A.cycles);



            //A get the addition value, F the flags
            assertArrayEquals(new int[] {5,0,valueRslam,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            //assertArrayEquals(new int[] {5,0,valueRslam,Alu.maskZNHC(flagZ, false, false, flagC),0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void SRA_HL() { //OK
        Random rng = newRandom();
        int valueR,valueRslam,flag,msb,lsb,address;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;

            if(address < 7) {
                address = 7;
                msb = 0;
                lsb = 7;
            }
            
            valueR=rng.nextInt(MAX_8);
            r.write(address, valueR);

            //System.out.println("value R = "+Integer.toBinaryString(valueR));
            valueRslam=Bits.clip(8, (Bits.signExtend8(valueR)>>1));
            //System.out.println("value R slam = "+Integer.toBinaryString(valueRslam));
            //System.out.println("Wanted = "+Integer.toBinaryString(218));


            if(valueR%2==1) {
                flag+=1; //C flags ?
            }
            if(valueRslam==0)flag+=8; //Z flag ?
            flag = (flag)<<4;

            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, 0xCB);
            b.write(4, Opcode.SRA_HLR.encoding);

            cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.SRA_HLR.cycles);

            //A get the addition value, F the flags
            assertEquals(flag,c._testGetPcSpAFBCDEHL()[3]);
            assertEquals(valueRslam,r.read(address));
        }
    }

    @Test
    void SRL_R8() { //OK
        Random rng = newRandom();
        int valueR,valueRslam,valueAdd,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            valueR=rng.nextInt(MAX_8);
            //System.out.println("value R = "+Integer.toBinaryString(valueR));
            valueRslam=valueR>>>1;
            valueRslam=Bits.clip(8, (valueR>>>1));
            //System.out.println("value R slam = "+Integer.toBinaryString(valueRslam));
            //System.out.println("Wanted = "+Integer.toBinaryString(230));
            //System.out.println(Opcode.SLA_A.family);


            if(valueR%2==1) {
                flag+=1; //C flags ?
            }
            if(valueRslam==0)flag+=8; //Z flag ?
            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueR);
            b.write(2, 0xCB);
            b.write(3, Opcode.SRL_A.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.SRL_A.cycles);



            //A get the addition value, F the flags
            assertArrayEquals(new int[] {4,0,valueRslam,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void SRL_HL() { //OK
        Random rng = newRandom();
        int valueR,valueRslam,flag,msb,lsb,address;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;

            if(address < 7) {
                address = 7;
                msb = 0;
                lsb = 7;
            }
            
            
            valueR=rng.nextInt(MAX_8);
            r.write(address, valueR);

            //System.out.println("value R = "+Integer.toBinaryString(valueR));
            valueRslam=(Bits.clip(8, valueR)>>>1);
            //System.out.println("value R slam = "+Integer.toBinaryString(valueRslam));
            //System.out.println("Wanted = "+Integer.toBinaryString(218));
            //System.out.println(Opcode.SRA_HLR.family);


            if(valueR%2==1) {
                flag+=1; //C flags ?
            }
            if(valueRslam==0)flag+=8; //Z flag ?
            flag = (flag)<<4;

            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, 0xCB);
            b.write(4, Opcode.SRL_HLR.encoding);

            cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.SRL_HLR.cycles);

            //A get the addition value, F the flags
            assertEquals(flag,c._testGetPcSpAFBCDEHL()[3]);
            //System.out.println(Integer.toBinaryString(r.read(address)));
            assertEquals(valueRslam,r.read(address));
        }
    }

    /////////////////////////////////////////
    //              Rotation               //
    /////////////////////////////////////////

    @Test
    void RLCA() { //OK
        Random rng = newRandom();
        int valueA,valueArotate,valueAdd,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            valueA=rng.nextInt(MAX_8);
            //System.out.println("value A = "+Integer.toBinaryString(valueA));
            valueArotate=Alu.unpackValue(Alu.rotate(RotDir.LEFT, valueA));
            //System.out.println("value A rot. = "+Integer.toBinaryString(valueArotate));


            if(valueA>MAX_7) {
                flag+=1; //C flags ?
            }
            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);

            b.write(2, Opcode.RLCA.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.RLCA.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {3,0,valueArotate,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void RRCA() { //OK
        Random rng = newRandom();
        int valueA,valueArotate,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;

            valueA=rng.nextInt(MAX_8);
            //System.out.println("value A = "+Integer.toBinaryString(valueA));
            valueArotate=Alu.unpackValue(Alu.rotate(RotDir.RIGHT, valueA));
            //System.out.println("value A rot. = "+Integer.toBinaryString(valueArotate));


            if(valueA%2==1) {
                flag+=1; //C flags ?
            }
            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);

            b.write(2, Opcode.RRCA.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.RRCA.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {3,0,valueArotate,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void RLA() { //OK
        Random rng = newRandom();
        int valueA,valueArotate,flag,valueA_1,valueA_2;
        boolean carry;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            carry=false;

            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueA=valueA_1+valueA_2;
            if(valueA>MAX_8) {
                carry=true;
                valueA=Bits.clip(8, valueA);
            }

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            b.write(4, Opcode.RLA.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.ADD_A_N8.cycles+Opcode.RLA.cycles);
            valueArotate=Alu.unpackValue(Alu.rotate(RotDir.LEFT, valueA,carry));
            if(valueA>MAX_7) {
                flag+=1; //C flags ?
            }
            flag = (flag)<<4;
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {5,0,valueArotate,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void RRA() { //OK
        Random rng = newRandom();
        int valueA,valueArotate,valueAdd,flag,valueA_1,valueA_2;
        boolean carry;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            carry=false;

            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueA=valueA_1+valueA_2;
            if(valueA>MAX_8) {
                carry=true;
                valueA=Bits.clip(8, valueA);
            }

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            b.write(4, Opcode.RRA.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.ADD_A_N8.cycles+Opcode.RRA.cycles);
            valueArotate=Alu.unpackValue(Alu.rotate(RotDir.RIGHT, valueA,carry));
            if(valueA%2==1) {
                flag+=1; //C flags ?
            }
            flag = (flag)<<4;
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {5,0,valueArotate,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void RLC_R8() { //OK
        Random rng = newRandom();
        int valueR,valueRrotate,flag,randomNB;
        boolean carry;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            randomNB=rng.nextInt(6);
            valueR=rng.nextInt(MAX_8);
            b.write(0, Opcode.LD_E_N8.encoding);
            //b.write(0, tabOP[randomNB].encoding);
            b.write(1, valueR);
            b.write(2, 0xCB);
            b.write(3, Opcode.RLC_E.encoding);
            //b.write(3, tabRegister[randomNB].encoding);

            cycleCpu(c,Opcode.LD_E_N8.cycles+Opcode.RLC_E.cycles);//tabOP[randomNB].cycles+tabRegister[randomNB].cycles)

            //System.out.println("ValueR ="+Integer.toBinaryString(valueR));
            valueRrotate=Alu.unpackValue(Alu.rotate(RotDir.LEFT, valueR));

            //System.out.println("ValueRrotate ="+Integer.toBinaryString(valueRrotate));
            //System.out.println("Wanted = "+Integer.toBinaryString(203));
            if(valueR>MAX_7) {
                flag+=1; //C flags ?
            }
            if(valueRrotate==0)flag+=8;
            flag = (flag)<<4;
            //A get the addition value, F the flags
            /*for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println();*/
            //assertEquals(valueRrotate,c._testGetPcSpAFBCDEHL()[randomNB+3]);
            //assertEquals(c._testGetPcSpAFBCDEHL()[3],flag);
            assertArrayEquals(new int[] {4,0,0,flag,0,0,0,valueRrotate,0,0},c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void RRC_R8() { //OK
        Random rng = newRandom();
        int valueR,valueRrotate,flag;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            valueR=rng.nextInt(MAX_8);
            //System.out.println(randomNB+" || "+valueR);
            b.write(0, Opcode.LD_D_N8.encoding);
            b.write(1, valueR);
            b.write(2, 0xCB);
            b.write(3, Opcode.RRC_D.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.RRC_A.cycles);

            valueRrotate=Alu.unpackValue(Alu.rotate(RotDir.RIGHT, valueR));
            if(valueR%2==1) {
                flag+=1; //C flags ?
            }
            if(valueRrotate==0)flag+=8;
            flag = (flag)<<4;
            //A get the addition value, F the flags
            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println();*/
            assertArrayEquals(new int[] {4,0,0,flag,0,0,valueRrotate,0,0,0},c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void RLC_HLR() { //OK
        Random rng = newRandom();
        int valueR,valueRrotate,flag,msb,lsb,address;


        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;
            if(address < 7) {
                address = 7;
                msb = 0;
                lsb = 7;
            }
            
            valueR=rng.nextInt(MAX_8);
            r.write(address, valueR);

            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, 0xCB);
            b.write(4, Opcode.RLC_HLR.encoding);

            cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.RLC_HLR.cycles);
            //System.out.println(Opcode.RLC_A.family);
            valueRrotate=Alu.unpackValue(Alu.rotate(RotDir.LEFT, valueR));
            if(valueR>MAX_7) {
                flag+=1; //C flags ?
            }
            if(valueRrotate==0)flag+=8;
            flag = (flag)<<4;
            //A get the addition value, F the flags
            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }*/
            //System.out.println();
            assertEquals(flag,c._testGetPcSpAFBCDEHL()[3]);
            assertEquals(valueRrotate,r.read(address));
        }
    }

    @Test
    void RRC_HLR() { //OK
        Random rng = newRandom();
        int valueR,valueRrotate,flag,msb,lsb,address;


        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;
            valueR=rng.nextInt(MAX_8);
            r.write(address, valueR);

            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, 0xCB);
            b.write(4, Opcode.RRC_HLR.encoding);

            cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.RRC_HLR.cycles);
            valueRrotate=Alu.unpackValue(Alu.rotate(RotDir.RIGHT, valueR));
            if(valueR%2==1) {
                flag+=1; //C flags ?
            }
            if(valueRrotate==0)flag+=8;
            flag = (flag)<<4;
            //A get the addition value, F the flags
            /*
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }*/
            //System.out.println();
            //assertEquals(flag,c._testGetPcSpAFBCDEHL()[3]);
            assertEquals(valueRrotate,r.read(address));
        }
    }

    @Test
    void RL_R8() { //OK
        Random rng = newRandom();
        int valueR,valueA,valueRrotate,flag,valueA_1,valueA_2;
        boolean carry;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            carry=false;

            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueA=valueA_1+valueA_2;
            if(valueA>MAX_8) {
                carry=true;
                valueA=Bits.clip(8, valueA);
            }
            //System.out.println(valueA_1+" + "+valueA_2+" = "+valueA+" ==> carry = "+carry);
            
            valueR=rng.nextInt(MAX_8+1);
            //System.out.println("valueR = "+Integer.toBinaryString(valueR));
            //System.out.println("Wanted ="+c._testGetPcSpAFBCDEHL()[3]);
            valueRrotate=Alu.unpackValue(Alu.rotate(RotDir.LEFT, valueR,carry));
            
            if(valueR>MAX_7) {
                flag+=1; //C flags ?
            }
            if(valueRrotate==0) flag+=8;
            flag = (flag)<<4;
            
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            b.write(4, Opcode.LD_B_N8.encoding);
            b.write(5, valueR);
            b.write(6, 0xCB);
            b.write(7, Opcode.RL_B.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.LD_A_N8.cycles+Opcode.ADD_A_N8.cycles+Opcode.RL_B.cycles);
            
            
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {8,0,valueA,flag,valueRrotate,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }
    
    @Test
    void RR_R8() { //OK
        Random rng = newRandom();
        int valueR,valueA,valueRrotate,flag,valueA_1,valueA_2;
        boolean carry;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            carry=false;

            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueA=valueA_1+valueA_2;
            if(valueA>MAX_8) {
                carry=true;
                valueA=Bits.clip(8, valueA);
            }
            //System.out.println(valueA_1+" + "+valueA_2+" = "+valueA+" ==> carry = "+carry);
            
            valueR=rng.nextInt(MAX_8+1);
            //System.out.println("valueR = "+Integer.toBinaryString(valueR));
            //System.out.println("Wanted ="+c._testGetPcSpAFBCDEHL()[3]);
            valueRrotate=Alu.unpackValue(Alu.rotate(RotDir.RIGHT, valueR,carry));
            
            if(valueR%2==1) {
                flag+=1; //C flags ?
            }
            if(valueRrotate==0) flag+=8;
            flag = (flag)<<4;
            
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            b.write(4, Opcode.LD_B_N8.encoding);
            b.write(5, valueR);
            b.write(6, 0xCB);
            b.write(7, Opcode.RR_B.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.LD_A_N8.cycles+Opcode.ADD_A_N8.cycles+Opcode.RL_B.cycles);
            
            
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {8,0,valueA,flag,valueRrotate,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void RL_HLR() { //OK
        Random rng = newRandom();
        int valueR,valueA,valueRrotate,flag,valueA_1,valueA_2,msb,lsb,address;
        boolean carry;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            carry=false;
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;
            valueR=rng.nextInt(MAX_8);
            //Random carry
            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueA=valueA_1+valueA_2;
            if(valueA>MAX_8) {
                carry=true;
                valueA=Bits.clip(8, valueA);
            }
            //Writing at HL address
            r.write(address, valueR);
            //In A - carry
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            
            b.write(4, Opcode.LD_HL_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);
            b.write(7, 0xCB);
            b.write(8, Opcode.RL_HLR.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.ADD_A_N8.cycles+Opcode.LD_HL_N16.cycles+Opcode.RL_HLR.cycles);
            
            
            //System.out.println(valueA_1+" + "+valueA_2+" = "+valueA+" ==> carry = "+carry);
            
            //System.out.println("valueR = "+Integer.toBinaryString(valueR));
            //System.out.println("Wanted ="+c._testGetPcSpAFBCDEHL()[3]);
            valueRrotate=Alu.unpackValue(Alu.rotate(RotDir.LEFT, valueR,carry));
            if(valueR>MAX_7) {
                flag+=1; //C flags ?
            }
            if(valueRrotate==0) flag+=8;
            flag = (flag)<<4;
            
            
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {9,0,valueA,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
            assertEquals(valueRrotate,r.read(address));
        }
    }

    @Test
    void RR_HLR() { //OK
        Random rng = newRandom();
        int valueR,valueA,valueRrotate,flag,valueA_1,valueA_2,msb,lsb,address;
        boolean carry;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            carry=false;
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;
            valueR=rng.nextInt(MAX_8);
            //Random carry
            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueA=valueA_1+valueA_2;
            if(valueA>MAX_8) {
                carry=true;
                valueA=Bits.clip(8, valueA);
            }
            //Writing at HL address
            r.write(address, valueR);
            //In A - carry
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            
            b.write(4, Opcode.LD_HL_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);
            b.write(7, 0xCB);
            b.write(8, Opcode.RR_HLR.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.ADD_A_N8.cycles+Opcode.LD_HL_N16.cycles+Opcode.RR_HLR.cycles);
            
            
            //System.out.println(valueA_1+" + "+valueA_2+" = "+valueA+" ==> carry = "+carry);
            
            //System.out.println("valueR = "+Integer.toBinaryString(valueR));
            //System.out.println("Wanted ="+c._testGetPcSpAFBCDEHL()[3]);
            valueRrotate=Alu.unpackValue(Alu.rotate(RotDir.RIGHT, valueR,carry));
            if(valueR%2==1) {
                flag+=1; //C flags ?
            }
            if(valueRrotate==0) flag+=8;
            flag = (flag)<<4;
            
            
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {9,0,valueA,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
            assertEquals(valueRrotate,r.read(address));
        }
    }

    @Test
    void SWAP_R8() { //OK
        Random rng = newRandom();
        int valueR,valueA,valueRswap,flag,msb,lsb;
        boolean carry;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            carry=false;

            msb=rng.nextInt(MAX_4+1);
            lsb=rng.nextInt(MAX_4+1);
            
            valueR=(msb<<4)+lsb;
            valueRswap=(lsb<<4)+msb;
            
            if(valueRswap==0) flag+=8;
            flag = (flag)<<4;
            
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueR);
            b.write(2, 0xCB);
            b.write(3, Opcode.SWAP_A.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.SWAP_A.cycles);
            
            
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {4,0,valueRswap,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void SWAP_HLR() { //OK
        Random rng = newRandom();
        int valueR,valueA,valueRswap,flag,msbR,lsbR,msb,lsb,address;
        boolean carry;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            carry=false;
            //HL addres
            msb=rng.nextInt(MAX_8+1);
            lsb=rng.nextInt(MAX_8-9)+9;
            address=(msb<<8)+lsb;
            //Swaping
            msbR=rng.nextInt(MAX_4+1);
            lsbR=rng.nextInt(MAX_4+1);
            
            valueR=(msbR<<4)+lsbR;
            valueRswap=(lsbR<<4)+msbR;
            //Bus writing
            r.write(address, valueR);
            //Editing flag
            if(valueRswap==0) flag+=8;
            flag = (flag)<<4;
            
            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, 0xCB);
            b.write(4, Opcode.SWAP_HLR.encoding);

            cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.SWAP_HLR.cycles);
            //System.out.println(Integer.toBinaryString(valueRswap)+" ?= "+Integer.toBinaryString(r.read(address)));
            //System.out.println(Integer.toBinaryString(flag)+" ?= "+Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[3]) );
            
            
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {5,0,0,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
            assertEquals(valueRswap,r.read(address));
        }
    }
    
    /////////////////////////////////////////
    //         Bits Operations 2           //
    /////////////////////////////////////////

    @Test
    void BIT_N3_R8() { //OK
        Random rng = newRandom();
        int valueA,valueArotate,randomNB,flag,z,carry;
        Opcode[] tabOP = {Opcode.BIT_0_A,Opcode.BIT_1_A,Opcode.BIT_2_A,Opcode.BIT_3_A,Opcode.BIT_4_A,
                Opcode.BIT_5_A,Opcode.BIT_6_A,Opcode.BIT_7_A};

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=carry=0;
            z=1;

            randomNB=rng.nextInt(8);
            valueA=rng.nextInt(8);
            if(valueA>MAX_8) {
                carry=1;
                valueA-=MAX_8+1;
            }
            /*System.out.println("Carry ="+carry);
            System.out.println("randomNB ="+randomNB);
            System.out.println("valueA ="+Integer.toBinaryString(valueA));*/
            //if(valueA>Math.pow(2, randomNB)) z=0;
            if((valueA & Bits.set(0, randomNB, true)) > 0) // Si le bit voule est a 1 dans A
                z=0;

            //System.out.println(z);
            flag+=2;
            //flag+=carry;
            flag+=(8*z); //Z flag 0 or 1
            flag=(flag<<4);
            //System.out.println("flag = "+flag);



            //Active ou pas le carry (random
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            //b.write(2, Opcode.ADD_A_N8.encoding);
            //b.write(3, valueA_2);
            b.write(2, 0xCB);
            b.write(3, tabOP[randomNB].encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+tabOP[randomNB].cycles);//+Opcode.ADD_A_N8.cycles);
            /*for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println();*/
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {4,0,valueA,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void BIT_N3_HLR() { //OK
        Random rng = newRandom();
        int valueR,randomNB,flag,z,msb,lsb,address;
        Opcode[] tabOP = {Opcode.BIT_0_HLR,Opcode.BIT_1_HLR,Opcode.BIT_2_HLR,Opcode.BIT_3_HLR,Opcode.BIT_4_HLR,
                Opcode.BIT_5_HLR,Opcode.BIT_6_HLR,Opcode.BIT_7_HLR};


        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            z=1;

            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;
            randomNB=rng.nextInt(8);
            valueR=rng.nextInt(8);


            //System.out.println("Carry ="+carry);
            //System.out.println("randomNB ="+randomNB);
            //System.out.println("valueR ="+Integer.toBinaryString(valueR));
            //if(valueA>Math.pow(2, randomNB)) z=0;
            if((valueR & Bits.set(0, randomNB, true)) > 0) // Si le bit voule est a 1 dans HLR
                z=0;

            //System.out.println(z);
            flag+=2;
            //flag+=carry;
            flag+=(8*z); //Z flag 0 or 1
            flag=(flag<<4);
            //System.out.println("flag = "+flag);


            r.write(address, valueR);
            //Active ou pas le carry (random
            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            //b.write(2, Opcode.ADD_A_N8.encoding);
            //b.write(3, valueA_2);
            b.write(3, 0xCB);
            b.write(4, tabOP[randomNB].encoding);

            cycleCpu(c, Opcode.LD_HL_N16.cycles+tabOP[randomNB].cycles);//+Opcode.ADD_A_N8.cycles);
            /*for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println();*/
            //A get the addition value, F the flags

            //System.out.println("flag ="+Integer.toBinaryString(flag));
            //System.out.println("Wanted flag ="+Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[3]));
            assertArrayEquals(new int[] {5,0,0,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
            assertEquals(valueR,r.read(address));
        }
    }

    @Test
    void monTestBit_n3_R8() { //OK
        Cpu c;
        Ram r;
        Bus b;

        c = new Cpu();
        r = new Ram(0xFFFF);
        b = connect(c, r);

        //Active ou pas le carry (random
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0b00100);
        //b.write(2, Opcode.ADD_A_N8.encoding);
        //b.write(3, valueA_2);
        b.write(2, 0xCB);
        b.write(3, 0b01001111);//b.write(3, 0b01010111); cas ou z false
        cycleCpu(c, 3);
        assertArrayEquals(new int[] {4,0,0b100,Alu.maskZNHC(true, false, true, false),0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void SET_N3_R8() { //OK
        Random rng = newRandom();
        int valueA,valueA_1,valueA_2,randomNB,flag,z,carry;
        Opcode[] tabOP = {Opcode.SET_0_A,Opcode.SET_1_A,Opcode.SET_2_A,Opcode.SET_3_A,Opcode.SET_4_A,
                Opcode.SET_5_A,Opcode.SET_6_A,Opcode.SET_7_A};

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=carry=0;
            z=1;
            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueA=valueA_1+valueA_2;
            if(valueA>MAX_8) {
                flag+=1;
                valueA-=MAX_8+1;
            }
            //System.out.println("ValueA_1 = "+Integer.toBinaryString(valueA_1)+" || valueA_2 = "+Integer.toBinaryString(valueA_2)+" || tot = "+valueA);
            if(valueA==0)flag+=8;
            if((valueA_1&MAX_4)+(valueA_2&MAX_4)>MAX_4) {
                flag+=2;
            }
            flag=(flag<<4);
            randomNB=3;
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            b.write(4, 0xCB);
            b.write(5, tabOP[randomNB].encoding);
            //System.out.println(tabOP[randomNB].name());
            //System.out.println(Opcode.SET_0_A.family);
            //System.out.println("valueA ="+Integer.toBinaryString(valueA));
            valueA |= (1<<randomNB);
            //System.out.println("valueA finale ="+Integer.toBinaryString(valueA));
            cycleCpu(c, Opcode.LD_A_N8.cycles+tabOP[randomNB].cycles+Opcode.ADD_A_N8.cycles);//+Opcode.ADD_A_N8.cycles);
            //System.out.println("Wanted = "+Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[2]));



            //System.out.println("Carry ="+carry);
            //System.out.println("randomNB ="+randomNB);

            /*System.out.println(z);
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println();*/
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {6,0,valueA,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void SET_N3_HLR() { //OK
        Random rng = newRandom();
        int valueA,valueA_1,valueA_2,randomNB,flag,z,carry,msb,lsb,address,valueR;
        Opcode[] tabOP = {Opcode.SET_0_HLR,Opcode.SET_1_HLR,Opcode.SET_2_HLR,Opcode.SET_3_HLR,Opcode.SET_4_HLR,
                Opcode.SET_5_HLR,Opcode.SET_6_HLR,Opcode.SET_7_HLR};

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            //Creating a random carry
            valueA_1=rng.nextInt(8);
            valueA_2=rng.nextInt(8);
            valueA=valueA_1+valueA_2;
            if(valueA>MAX_8) {
                flag+=1;
                valueA-=MAX_8+1;
            }
            if(valueA==0)flag+=8;
            if((valueA_1>>4)+(valueA_2>>4)>MAX_4)flag+=2;
            flag=(flag<<4);
            //We now have our actual flag

            //Creating the random address
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;
            //the value to be tested
            valueR=rng.nextInt(MAX_8);
            //Random number for the command
            randomNB=rng.nextInt(8);
            r.write(address, valueR);
            //Adding for random carry
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            //Seting the HLR address's value
            b.write(4, Opcode.LD_HL_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);

            b.write(7, 0xCB);
            b.write(8, tabOP[randomNB].encoding);
            //Or with the choosen bit
            valueR |= (1<<randomNB);
            //Running cycles
            cycleCpu(c, Opcode.LD_A_N8.cycles+tabOP[randomNB].cycles+Opcode.ADD_A_N8.cycles+Opcode.LD_HL_N16.cycles);//+Opcode.ADD_A_N8.cycles);
            //A get the addition value, F the flags, HL the address, [address] the OR valueR
            assertArrayEquals(new int[] {9,0,valueA,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
            assertEquals(valueR,r.read(address));
        }
    }

    @Test
    void RES_N3_R8() { //OK
        Random rng = newRandom();
        int valueA,valueA_1,valueA_2,valueARES,randomNB,flag,z,carry;
        Opcode[] tabOP = {Opcode.RES_0_A,Opcode.RES_1_A,Opcode.RES_2_A,Opcode.RES_3_A,Opcode.RES_4_A,
                Opcode.RES_5_A,Opcode.RES_6_A,Opcode.RES_7_A};

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=carry=valueARES=0;
            z=1;
            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueA=valueA_1+valueA_2;
            if(valueA>MAX_8) {
                flag+=1;
                valueA-=MAX_8+1;
            }
            if(valueA==0)flag+=8;
            if((valueA_1&MAX_4)+(valueA_2&MAX_4)>MAX_4)flag+=2;
            flag=(flag<<4);
            randomNB=2;
            
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            b.write(4, 0xCB);
            b.write(5, tabOP[randomNB].encoding);
            
            //System.out.println(tabOP[randomNB].name());
            //System.out.println(Opcode.SET_0_A.family);
            //System.out.println(Integer.toBinaryString(Bits.reverse8(0b11110000)));
            valueARES = valueA & Bits.complement8(1<<randomNB);
            
            //System.out.println("valueA finale ="+Integer.toBinaryString(valueA));
            cycleCpu(c, Opcode.LD_A_N8.cycles+tabOP[randomNB].cycles+Opcode.ADD_A_N8.cycles);//+Opcode.ADD_A_N8.cycles);
            //System.out.println("Wanted = "+Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[2]));



            //System.out.println("Carry ="+carry);
            //System.out.println("randomNB ="+randomNB);

            /*System.out.println(z);
            for(int v : c._testGetPcSpAFBCDEHL()) {
                System.out.print(v+"|");
            }
            System.out.println();*/
            //A get the addition value, F the flags
            //System.out.println("expected : "+Integer.toBinaryString(valueARES)+" from :"+Integer.toBinaryString(valueA)+" || Wanted :"+Integer.toBinaryString(c._testGetPcSpAFBCDEHL()[2]));
            assertArrayEquals(new int[] {6,0,valueARES,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            
        }
    }

    @Test
    void RES_N3_HLR() { //OK
        Random rng = newRandom();
        int valueA,valueA_1,valueA_2,randomNB,flag,z,carry,msb,lsb,address,valueR;
        Opcode[] tabOP = {Opcode.RES_0_HLR,Opcode.RES_1_HLR,Opcode.RES_2_HLR,Opcode.RES_3_HLR,Opcode.RES_4_HLR,
                Opcode.RES_5_HLR,Opcode.RES_6_HLR,Opcode.RES_7_HLR};

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            //Creating a random carry
            valueA_1=rng.nextInt(8);
            valueA_2=rng.nextInt(8);
            valueA=valueA_1+valueA_2;
            if(valueA>MAX_8) {
                flag+=1;
                valueA-=MAX_8+1;
            }
            if(valueA==0)flag+=8;
            if((valueA_1>>4)+(valueA_2>>4)>MAX_4)flag+=2;
            flag=(flag<<4);
            //We now have our actual flag

            //Creating the random address
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-10)+10;
            address=(msb<<8)+lsb;
            //the value to be tested
            valueR=rng.nextInt(MAX_8);
            //Random number for the command
            randomNB=rng.nextInt(8);
            r.write(address, valueR);
            //Adding for random carry
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            //Seting the HLR address's value
            b.write(4, Opcode.LD_HL_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);
            b.write(7, 0xCB);
            b.write(8, tabOP[randomNB].encoding);
            //Or with the choosen bit
            valueR &= Bits.complement8(1<<randomNB);
            //Running cycles
            cycleCpu(c, Opcode.LD_A_N8.cycles+tabOP[randomNB].cycles+Opcode.ADD_A_N8.cycles+Opcode.LD_HL_N16.cycles);//+Opcode.ADD_A_N8.cycles);
            //A get the addition value, F the flags, HL the address, [address] the OR valueR
            assertArrayEquals(new int[] {9,0,valueA,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
            assertEquals(valueR,r.read(address));
        }
    }

    /////////////////////////////////////////
    //              Autres                 //
    /////////////////////////////////////////


    //@Test
    void DAA() { //OK
        Random rng = newRandom();
        int valueR,valueA,valueRrotate,flag,valueA_1,valueA_2;
        boolean carry,h;

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            carry=h=false;
            
            valueR=rng.nextInt(MAX_8);
            //Random carry
            valueA_1=rng.nextInt(MAX_8);
            valueA_2=rng.nextInt(MAX_8);
            valueA=valueA_1+valueA_2;
            if((valueA_1&MAX_4)+(valueA_2&MAX_4)>MAX_4) h=true;
            if(valueA>MAX_8) {
                carry=true;
                valueA=Bits.clip(8, valueA);
            }
            
            //In A - carry
            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA_1);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueA_2);
            b.write(4, Opcode.DAA.encoding);
            
            valueA=Alu.bcdAdjust(valueA, false, h, carry);
            //Setting the flag
            if(valueA==0) flag+=8;
            if(carry) flag++;
            
            flag = (flag)<<4;

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.ADD_A_N8.cycles+Opcode.DAA.cycles);
            
            
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {5,0,valueA,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }
    
    
    
    @Test
    void addAn8() { //Not prefixed
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        b.write(0xFFFE,0X20);
        //b.write(0, 0xCB);
        b.write(0, Opcode.ADD_A_N8.encoding); // 198
        b.write(2, 0xFF);
        cycleCpu(c, 2);
    }

    @Test
    void byteInt() {
        int v = 0b11110000;
        int i = 22;

    }


    @Test
    void toto() {
        
        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);
        int flag=0;


        int valueR=12;
        int valueA=14;
        int valueComparedRandom=34;
        if((valueA+valueComparedRandom)>MAX_8) flag+=1;
        int valueAdd=valueA+valueComparedRandom;


        //System.out.println("ValueA = "+valueA+ " || ValueB= "+valueComparedRandom+" || tot ="+valueAdd);

        b.write(0, Opcode.SLA_A.encoding);
        b.write(1, valueA);
        b.write(2, Opcode.ADD_A_N8.encoding);
        b.write(3, valueComparedRandom);
        b.write(4, Opcode.LD_C_N8.encoding);
        b.write(5, valueR);

        if(valueAdd>MAX_8) {
            valueAdd=valueAdd-256;
        }

        //System.out.println("Cliped ="+valueAdd);





        b.write(6, Opcode.INC_C.encoding);


        cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_C_N8.cycles+Opcode.LD_A_N8.cycles+Opcode.INC_C.cycles);

    }
}
