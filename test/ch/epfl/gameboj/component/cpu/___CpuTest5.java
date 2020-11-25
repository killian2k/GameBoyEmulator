package ch.epfl.gameboj.component.cpu;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

@Disabled 
class ___CpuTest5 {
    final int MAX_16=0b1111_1111_1111_1111;
    final int MAX_15=0b111_1111_1111_1111;
    final int MAX_8=0b1111_1111;
    final int MAX_7=0b111_1111;
    final int MAX_4=0b1111;


    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }

    @Test
    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    @Test
    void test() {

        byte[] fib = new byte[] {
                (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
                (byte)0x0B, (byte)0xCD, (byte)0x0A, (byte)0x00,
                (byte)0x76, (byte)0x00, (byte)0xFE, (byte)0x02,
                (byte)0xD8, (byte)0xC5, (byte)0x3D, (byte)0x47,
                (byte)0xCD, (byte)0x0A, (byte)0x00, (byte)0x4F,
                (byte)0x78, (byte)0x3D, (byte)0xCD, (byte)0x0A,
                (byte)0x00, (byte)0x81, (byte)0xC1, (byte)0xC9,
              };
        
        //GameBoy g = new GameBoy(null);
        Cpu c = new Cpu();
        Bus b = connect(c, new Ram(0xFFFF));
        
        for(int i=0;i<fib.length;++i) {
            //g.bus().write(i + 0xC000, Bits.clip(8, fib[i]));
            b.write(i, Bits.clip(8, fib[i]));
            //System.out.println(b.read(i));
            //System.out.println(g.bus().read(i + 0xC000) + " " + Bits.clip(8, fib[i]));
        }
        //g.runUntil(9);
        //System.out.println(c.cycle(0));
        for(int i = 0;i<=10000;++i) {
            c.cycle(i);
            System.out.println("A= " + c._testGetPcSpAFBCDEHL()[2]);
            if(c._testGetPcSpAFBCDEHL()[0] == 8) {
                
                System.out.println("stop");
                break;
            }
                
        }
        System.out.println("regs");
        System.out.println("PC: " + c._testGetPcSpAFBCDEHL()[0]);
        System.out.println("SP: " + c._testGetPcSpAFBCDEHL()[1]);
        System.out.println("A: " + c._testGetPcSpAFBCDEHL()[2]);
        System.out.println(c._testGetPcSpAFBCDEHL()[3]);
        System.out.println(c._testGetPcSpAFBCDEHL()[4]);
        System.out.println(c._testGetPcSpAFBCDEHL()[5]);
        System.out.println(c._testGetPcSpAFBCDEHL()[6]);
        System.out.println(c._testGetPcSpAFBCDEHL()[7]);
        System.out.println(c._testGetPcSpAFBCDEHL()[8]);
        System.out.println(c._testGetPcSpAFBCDEHL()[9]);

        //System.out.println(g.cpu()._testGetPcSpAFBCDEHL()[10]);

    }

    /////////////////////////////////////////
    //               Jumps                 //
    /////////////////////////////////////////

    @Test
    void JP_N16() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueAdd,flag,msb,lsb,address;

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
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;

            valueAdd=valueA+valueN;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueN))>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {
                flag+=1; //C flags ?
                valueAdd=Bits.clip(8, valueAdd);
            }
            if((valueAdd)==0) flag+=8; //Z flags ?

            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueN); 

            //Jump opcode
            b.write(4, Opcode.JP_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_A_N8.cycles+Opcode.JP_N16.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {address,0,valueAdd,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void JP_HL() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueAdd,flag,msb,lsb,address;

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
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;

            valueAdd=valueA+valueN;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueN))>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {
                flag+=1; //C flags ?
                valueAdd=Bits.clip(8, valueAdd);
            }
            if((valueAdd)==0) flag+=8; //Z flags ?

            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueN); 

            //Change HL
            b.write(4, Opcode.LD_HL_N16.encoding);
            b.write(5, lsb);
            b.write(6, msb);

            //Jump opcode
            b.write(7, Opcode.JP_HL.encoding);



            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_A_N8.cycles+Opcode.LD_HL_N16.cycles+Opcode.JP_HL.cycles);

            //A get the addition value, F the flags
            assertArrayEquals(new int[] {address,0,valueAdd,flag,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void JP_CC_N16() {  //

        Random rng = newRandom();
        int valueA,valueN,valueAdd,flag,msb,lsb,address,randomNB;
        //Opcode table
        Opcode[] tabOP = {Opcode.JP_NZ_N16,Opcode.JP_Z_N16,Opcode.JP_NC_N16,Opcode.JP_C_N16};
        boolean NZ,Z,C,NC,testOK;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            NZ=Z=C=NC=testOK=false;
            randomNB=rng.nextInt(4);
            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;

            valueAdd=valueA+valueN;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueN))>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {//C flags ?
                flag+=1; 
                valueAdd=Bits.clip(8, valueAdd);
                C=true;
            }else {
                NC=true;
            }

            if((valueAdd)==0) {//Z flags ?
                flag+=8; 
                Z=true;
            }else {
                NZ=true;
            }

            flag = (flag)<<4;


            switch(randomNB) {
            case 0 :    //NZ
                testOK=NZ;
                break;
            case 1:     //Z
                testOK=Z;
                break;
            case 2:     //NC
                testOK=NC;
                break;
            case 3:     //C
                testOK=C;
                break;
            }


            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueN); 

            //Jump random value
            b.write(4, tabOP[randomNB].encoding);
            b.write(5, lsb);
            b.write(6, msb);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_A_N8.cycles+tabOP[randomNB].cycles);

            //A get the addition value, F the flags
            if(testOK) {
                assertArrayEquals(new int[] {address,0,valueAdd,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            }else {
                assertArrayEquals(new int[] {8,0,valueAdd,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            }
        }
    }

    @Test
    void JR_E8() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueAdd,flag,randomNB,E8,newPC,valueE8;
        boolean negative;
        //Opcode table
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=E8=newPC=0;
            negative=rng.nextBoolean();
            randomNB=rng.nextInt(4);
            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            E8=rng.nextInt(MAX_7);
            if(negative) E8=-E8;
            valueE8=E8;
            E8=Bits.clip(8, E8);

            valueAdd=valueA+valueN;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueN))>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {//C flags ?
                flag+=1; 
                valueAdd=Bits.clip(8, valueAdd);
            }

            if((valueAdd)==0) {//Z flags ?
                flag+=8; 
            }

            flag = (flag)<<4;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueN); 

            //Jump random value
            b.write(4, Opcode.JR_E8.encoding);
            b.write(5, E8);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_A_N8.cycles+Opcode.JR_E8.cycles);

            newPC=6+valueE8;
            if(newPC<0) newPC+=MAX_16+1;
            //A get the addition value, F the flags
            assertArrayEquals(new int[] {newPC,0,valueAdd,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        }
    }

    @Test
    void JR_CC_E8() {  //OK

        Random rng = newRandom();
        int valueA,valueN,valueAdd,flag,msb,lsb,address,randomNB,newPC,valueE8,E8;
        //Opcode table
        Opcode[] tabOP = {Opcode.JR_NZ_E8,Opcode.JR_Z_E8,Opcode.JR_NC_E8,Opcode.JR_C_E8};
        boolean NZ,Z,C,NC,testOK,negative;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            flag=0;
            NZ=Z=C=NC=testOK=false;
            randomNB=rng.nextInt(4);
            valueA=rng.nextInt(MAX_8);
            valueN=rng.nextInt(MAX_8);
            negative=rng.nextBoolean();
            valueE8=rng.nextInt(MAX_7);
            if(negative) valueE8=-valueE8;
            E8=Bits.clip(8, valueE8);


            valueAdd=valueA+valueN;

            if((Bits.clip(4,valueA)+Bits.clip(4,valueN))>MAX_4) flag+=2; //H flags ?
            if(((valueAdd)>MAX_8)) {//C flags ?
                flag+=1; 
                valueAdd=Bits.clip(8, valueAdd);
                C=true;
            }else {
                NC=true;
            }

            if((valueAdd)==0) {//Z flags ?
                flag+=8; 
                Z=true;
            }else {
                NZ=true;
            }

            flag = (flag)<<4;


            switch(randomNB) {
            case 0 :    //NZ
                testOK=NZ;
                break;
            case 1:     //Z
                testOK=Z;
                break;
            case 2:     //NC
                testOK=NC;
                break;
            case 3:     //C
                testOK=C;
                break;
            }


            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, valueA);
            b.write(2, Opcode.ADD_A_N8.encoding);
            b.write(3, valueN); 

            //Jump random value
            b.write(4, tabOP[randomNB].encoding);
            b.write(5, E8);

            cycleCpu(c, Opcode.ADD_A_N8.cycles+Opcode.LD_A_N8.cycles+tabOP[randomNB].cycles);

            newPC=6+valueE8;
            if(newPC<0) newPC+=MAX_16+1;

            //A get the addition value, F the flags
            if(testOK) {
                assertArrayEquals(new int[] {newPC,0,valueAdd,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            }else {
                assertArrayEquals(new int[] {7,0,valueAdd,flag,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            }
        }
    }

    /////////////////////////////////////////
    //           Interuptions              //
    /////////////////////////////////////////

    @Test
    void Inter1() {  //

        Random rng = newRandom();
        int INDEX_IF=AddressMap.REG_IF;
        int INDEX_IE=AddressMap.REG_IE;
        int randomNB,value;

        Interrupt[] tab= {Interrupt.VBLANK,Interrupt.LCD_STAT,Interrupt.TIMER,Interrupt.SERIAL,Interrupt.JOYPAD};

        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);
            //System.out.println("VALUES::::::" + b.read(INDEX_IE) + "....." + b.read(INDEX_IF));
            randomNB=rng.nextInt(5);
            value=1<<randomNB;
            int randomNB2=rng.nextInt(0xFF);
            //c.setIME(true);
            c.requestInterrupt(tab[randomNB]);
            //b.write(INDEX_IF, 0b100000 + (1<<randomNB));
            b.write(INDEX_IE, 1<<randomNB);
            b.write(0, Opcode.LD_A_B.encoding);
            
            b.write(AddressMap.INTERRUPTS[tab[randomNB].ordinal()], Opcode.LD_A_N8.encoding);
            b.write(AddressMap.INTERRUPTS[tab[randomNB].ordinal()]+1, randomNB2);
            
            cycleCpu(c, Opcode.LD_A_B.cycles);
            b.write(1, Opcode.LD_A_B.encoding);
            //c.setIME(true);
            b.write(INDEX_IF, 0b100000 + (1<<randomNB));
            c.cycle(7);

            //c.requestInterrupt(Interrupt.VBLANK);

            //Assert that PC = 40hex + (8*i)
            //assertEquals(0x40+(8*randomNB),c._testGetPcSpAFBCDEHL()[0]);
            
            //Assert that the PC is stored in the SP
            //assertEquals(1,c._testGetPcSpAFBCDEHL()[1]);
            System.out.println(i);
            System.out.println(randomNB2);
            System.out.println("PC: " + c._testGetPcSpAFBCDEHL()[0] + "\nSP: " + c._testGetPcSpAFBCDEHL()[1] + " content " + b.read(c._testGetPcSpAFBCDEHL()[1]));
            assertEquals(randomNB2, c._testGetPcSpAFBCDEHL()[2]);
            //assertEquals(value,c.read(INDEX_IE));

        }
    }


    @Test
    void EI_TRUE() {  //OK

        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.EI.encoding);
        cycleCpu(c,Opcode.EI.cycles);

        //assertEquals(true,c.getIME());

    }

    @Test
    void DI_FALSE() {  //OK

        Cpu c = new Cpu();
        Ram r = new Ram(0xFFFF);
        Bus b = connect(c, r);

        b.write(0, Opcode.DI.encoding);
        cycleCpu(c,Opcode.DI.cycles);

        //assertEquals(false,c.getIME());
    }

    

}
