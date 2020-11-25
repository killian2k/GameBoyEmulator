package ch.epfl.gameboj.component.cpu;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.Test;
import org.junit.jupiter.api.Disabled;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;


@Disabled 
class SimpleComponent implements Component {
    private final int address;
    private int value;
    private boolean wasRead, wasWritten;

    public SimpleComponent(int address, int initialValue) {
        this.address = address;
        this.value = initialValue;
    }

    boolean wasRead() { return wasRead; }
    boolean wasWritten() { return wasWritten; }

    @Override
    public int read(int a) {
        wasRead = true;
        return a == address ? value : Component.NO_DATA;
    }

    @Override
    public void write(int a, int d) {
        wasWritten = true;
        if (a == address)
            value = d;
    }
}


@Disabled 
public class ___CpuTest {
    final int MAX_16=(int)(Math.pow(2, 16)-1);
    final int MAX_8=(int)(Math.pow(2, 8)-1);

    private static SimpleComponent[] newComponents(int n) {
        SimpleComponent[] cs = new SimpleComponent[n];
        for (int i = 0; i < cs.length; ++i) {
            cs[i] = new SimpleComponent(i, i);
        }
        return cs;
    }

    /*@Test
    void test1() {
        Cpu c = new Cpu();
        Bus b = new Bus();
        b.attach(new RamController(new Ram(512), 0));
        c.attachTo(b);
        Component cp = c;
        Clocked cl = c;
        System.out.println("Avant opérations: B:" + c._testGetPcSpAFBCDEHL()[4]);
        int v0 = 0b00110110;
        b.write(0, v0); // LD [HL], n8 --> BUS[HL] = n8 On stocke 15 dans HL
        b.write(1, 0b00001111);// n8 = 15
        b.write(2, 0b01000110); // instr is:  LD B, [HL] --> B = BUS[HL] On place la valeur de HL dans B
        System.out.println("Adresse 0:" + b.read(0));
        System.out.println("Adresse 1:" + b.read(1));
        System.out.println("Adresse 2:" + b.read(2));
        c.cycle(0);
        //System.out.println("B:" + c._testGetPcSpAFBCDEHL()[4]);
        System.out.println("Adresse 0:" + b.read(0));
        System.out.println("Adresse 1:" + b.read(1));
        System.out.println("Adresse 2:" + b.read(2));
        c.cycle(3);
        System.out.println("Adresse 0:" + b.read(0));
        System.out.println("Adresse 1:" + b.read(1));
        System.out.println("Adresse 2:" + b.read(2));
        printValues(c._testGetPcSpAFBCDEHL(), b);
        assertValues(c._testGetPcSpAFBCDEHL(), b, 3, 0, v0, v0, 15, v0, v0, v0, v0, v0);
    }*/

    @Test
    void test_NOP() {
        Cpu c = new Cpu();
        Bus b = new Bus();
        b.attach(new RamController(new Ram(512), 0));
        c.attachTo(b);
        Component cp = c;
        Clocked cl = c;
        b.write(0, 0b00000000);
        c.cycle(0);
        assertValues(c._testGetPcSpAFBCDEHL(), b, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    void test_NOP2() {
        Cpu c = new Cpu();
        Bus b = new Bus();
        b.attach(new RamController(new Ram(512), 0));
        c.attachTo(b);
        Component cp = c;
        Clocked cl = c;
        int v0 = 0b01000000;
        b.write(0, v0);
        b.write(1, 0b01001001);
        b.write(2, 0b01010010);
        b.write(3, 0b01011011);
        b.write(4, 0b01100100);
        b.write(5, 0b01101101);
        b.write(6, 0b01111111);
        assertValues(c._testGetPcSpAFBCDEHL(), b, 0, 0, v0, v0, v0,v0,v0,v0,v0,v0);
        c.cycle(0);
        assertValues(c._testGetPcSpAFBCDEHL(), b, 1, 0, v0, v0, v0,v0,v0,v0,v0,v0);
        c.cycle(1);
        assertValues(c._testGetPcSpAFBCDEHL(), b, 2, 0, v0, v0, v0,v0,v0,v0,v0,v0);
        c.cycle(2);
        assertValues(c._testGetPcSpAFBCDEHL(), b, 3, 0, v0, v0, v0,v0,v0,v0,v0,v0);
        c.cycle(3);
        assertValues(c._testGetPcSpAFBCDEHL(), b, 4, 0, v0, v0, v0,v0,v0,v0,v0,v0);
        c.cycle(4);
        assertValues(c._testGetPcSpAFBCDEHL(), b, 5, 0, v0, v0, v0,v0,v0,v0,v0,v0);
        c.cycle(5);
        assertValues(c._testGetPcSpAFBCDEHL(), b, 6, 0, v0, v0, v0,v0,v0,v0,v0,v0);
        c.cycle(6);
        assertValues(c._testGetPcSpAFBCDEHL(), b, 7, 0, v0, v0, v0,v0,v0,v0,v0,v0);

    }
    /*
    @Test
    void test_LDR8HL_LD_r16_n16() {
        // Store in HL 38 and store in bus[38]= 77
        Cpu c = new Cpu();
        Bus b = new Bus();
        b.attach(new RamController(new Ram(0xFFFF), 0));
        c.attachTo(b);
        Component cp = c;
        Clocked cl = c;
        //store the data in HL
        b.write(0, 0b00100001); // Store HL = 42
        b.write(1, 0);
        b.write(2, 42);

        assertValuesReg(c._testGetPcSpAFBCDEHL(),0,0,0,0,0,0,0,0,0,0);

        c.cycle(0);
        assertValuesReg(c._testGetPcSpAFBCDEHL(),3,0,0,0,0,0,0,0,0,42);
        b.write(3, 0b00100001); // Store HL = 0xFFFF
        b.write(4, 0xFE);
        b.write(5, 0xFF);
        c.cycle(3);
        assertValuesReg(c._testGetPcSpAFBCDEHL(),6,0,0,0,0,0,0,0,0xFE,0xFF);

        b.write(6,0b00110110); // bus[HL] = 77
        b.write(7, 77); // Store from HL to register A
        c.cycle(6);
        assertEquals(77, b.read(0xFEFF));
        assertValuesReg(c._testGetPcSpAFBCDEHL(),8,0,0,0,0,0,0,0,0xFE,0xFF);

    }*/


    private void printValues(int[] array, Bus bus) {
        System.out.print(" Pc:" + array[0]);
        System.out.print(" Sp:" + array[1]);
        System.out.print(" A:"  + bus.read(array[2]) + " adresse: " + array[2]);
        System.out.print(" F:" + bus.read(array[3]) + " adresse: " + array[3]);
        System.out.print(" B:" + bus.read(array[4]) + " adresse: " + array[4]);
        System.out.print(" C:" + bus.read(array[5]) + " adresse: " + array[5]);
        System.out.print(" D:" + bus.read(array[6]) + " adresse: " + array[6]);
        System.out.print(" E:" + bus.read(array[7]) + " adresse: " + array[7]);
        System.out.print(" H:" + bus.read(array[8]) + " adresse: " + array[8]);
        System.out.print(" L:" + bus.read(array[9]) + " adresse: " + array[9]);
    }



    private void assertValues(int[] array, Bus bus, int pc, int sp, int a, int f, int b, int c, int d, int e, int h, int l) {

        assertEquals(pc, array[0]);
        assertEquals(sp, array[1]);
        assertEquals(a, bus.read(array[2]));
        assertEquals(f, bus.read(array[3]));
        assertEquals(b, bus.read(array[4]));
        assertEquals(c, bus.read(array[5]));
        assertEquals(d, bus.read(array[6]));
        assertEquals(e, bus.read(array[7]));
        assertEquals(h, bus.read(array[8]));
        assertEquals(l, bus.read(array[9]));
    }

    private void assertValuesReg(int[] array, int pc, int sp, int a, int f, int b, int c, int d, int e, int h, int l) {

        assertEquals(pc, array[0]);
        assertEquals(sp, array[1]);
        assertEquals(a, array[2]);
        assertEquals(f, array[3]);
        assertEquals(b, array[4]);
        assertEquals(c, array[5]);
        assertEquals(d, array[6]);
        assertEquals(e, array[7]);
        assertEquals(h, array[8]);
        assertEquals(l, array[9]);
    }

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

    @Test
    void nopDoesNothing() { //OK
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        b.write(0, Opcode.NOP.encoding);
        cycleCpu(c, Opcode.NOP.cycles);
        assertArrayEquals(new int[] {1,0,0,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();
        r = new Ram(10);
        b = connect(c, r);
        b.write(0, Opcode.LD_A_A.encoding);
        cycleCpu(c, Opcode.NOP.cycles);
        assertArrayEquals(new int[] {1,0,0,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R8_HLR() { //OK
        Random rng = newRandom();
        int msb,lsb,address,value;
        Cpu c;
        Ram r;
        Bus b;

        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-20);
            lsb+=20; //escaping to programm issue
            address=(msb<<8)+lsb;
            r.write(address, value);

            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, Opcode.LD_A_HLR.encoding ); //Placing 20 - 16 in BC


            cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.LD_A_HLR.cycles);

            assertArrayEquals(new int[] {4,0,value,0,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());


        }
    }

    @Test
    void LD_A_HLRI() { // Increment OK
        Cpu c;
        Ram r;
        Bus b;

        Random rng = newRandom();
        int msb,msbInc,lsb,lsbInc,address,value,addressInc;

        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;
            //Hl is incremented
            addressInc=address+1;
            lsbInc=addressInc & MAX_8;
            msbInc=addressInc>>8;
            r.write(address, value);

            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, Opcode.LD_A_HLRI.encoding);

            cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.LD_A_HLRI.cycles);

            assertArrayEquals(new int[] {4,0,value,0,0,0,0,0,msbInc,lsbInc}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);

        }


    }

    @Test
    void LD_A_HLRD() { // Decrement OK
        Cpu c;
        Ram r;
        Bus b;

        Random rng = newRandom();
        int msb,msbInc,lsb,lsbInc,address,value,addressInc;

        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;
            //Hl is decremented
            addressInc=address-1;
            lsbInc=addressInc & MAX_8;
            msbInc=addressInc>>8;
            r.write(address, value);

            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, Opcode.LD_A_HLRD.encoding);

            cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.LD_A_HLRD.cycles);

            assertArrayEquals(new int[] {4,0,value,0,0,0,0,0,msbInc,lsbInc}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);

        }



    }

    @Test
    void LD_A_N8R() {  //OK
        Random rng = newRandom();
        int addressParam,value,finAddress;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            addressParam=rng.nextInt(MAX_8);
            finAddress=AddressMap.REGS_START +addressParam;

            r.write(finAddress,value);
            b.write(0, Opcode.LD_A_N8R.encoding);
            b.write(1, addressParam);

            cycleCpu(c, Opcode.LD_A_N8R.cycles);

            assertArrayEquals(new int[] {2,0,value,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(finAddress),value);


        }

    }

    @Test
    void LD_A_CR() { //OK
        Random rng = newRandom();
        int addressParam,value,finAddress;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            addressParam=rng.nextInt(MAX_8-8)+8;
            finAddress=AddressMap.REGS_START+addressParam;
            r.write(finAddress, value);

            b.write(0, Opcode.LD_C_N8.encoding);
            b.write(1, addressParam);
            b.write(2, Opcode.LD_A_CR.encoding);


            cycleCpu(c, Opcode.LD_A_N8R.cycles+Opcode.LD_A_CR.cycles);

            assertArrayEquals(new int[] {4,0,value,0,0,addressParam,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(finAddress),value);
        }
    }

    @Test
    void LD_A_N16R() { //OK
        Random rng = newRandom();
        int address,value,msb,lsb;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-8)+8;
            msb=rng.nextInt(MAX_8);

            address=(msb<<8)+lsb;


            r.write(address, value);

            b.write(0, Opcode.LD_A_N16R.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            cycleCpu(c, Opcode.LD_A_N8R.cycles+Opcode.LD_A_CR.cycles);

            assertArrayEquals(new int[] {4,0,value,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);
        }
    }

    @Test
    void LD_A_BCR() { // OK
        Random rng = newRandom();
        int address,value,msb,lsb;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-8)+8;
            msb=rng.nextInt(MAX_8);

            address=(msb<<8)+lsb;

            r.write(address, value);

            b.write(0, Opcode.LD_BC_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, Opcode.LD_A_BCR.encoding);
            cycleCpu(c, Opcode.LD_BC_N16.cycles+Opcode.LD_A_BCR.cycles);

            assertArrayEquals(new int[] {4,0,value,0,msb,lsb,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);
        }
    }

    @Test
    void LD_A_DER() { //OK
        Random rng = newRandom();
        int address,value,msb,lsb;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8-8)+8;
            msb=rng.nextInt(MAX_8);

            address=(msb<<8)+lsb;

            r.write(address, value);

            b.write(0, Opcode.LD_DE_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, Opcode.LD_A_DER.encoding);
            cycleCpu(c, Opcode.LD_DE_N16.cycles+Opcode.LD_A_DER.cycles);

            assertArrayEquals(new int[] {4,0,value,0,0,0,msb,lsb,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);
        }
    }

    @Test
    void LD_R8_N8() { //OK
        Random rng = newRandom();
        int value;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, value);
            cycleCpu(c, Opcode.LD_A_N8.cycles);

            assertArrayEquals(new int[] {2,0,value,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }

    }

    @Test
    void LD_R16_N16() { //OK
        Random rng = newRandom();
        int value,msb,lsb;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {

            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);


            lsb=rng.nextInt(MAX_8-8)+8;
            msb=rng.nextInt(MAX_8);
            value=(msb<<8)+lsb;

            b.write(0, Opcode.LD_BC_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);

            cycleCpu(c, Opcode.LD_BC_N16.cycles);

            assertArrayEquals(new int[] {3,0,0,0,msb,lsb,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void POP_R16() { //OK
        Random rng = newRandom();
        int value,msb,lsb,msbV,lsbV,address,finValue;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);


            lsb=rng.nextInt(MAX_8-8)+8;
            msb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;



            finValue=address+2;

            value=rng.nextInt(MAX_8);
            
            r.write(address, value);

            b.write(0, Opcode.LD_SP_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, Opcode.POP_BC.encoding);

            cycleCpu(c, Opcode.LD_BC_N16.cycles+Opcode.POP_BC.cycles);

            assertArrayEquals(new int[] {4,finValue,0,0,0,value,0,0,0,0}, c._testGetPcSpAFBCDEHL());
        }
    }

    @Test
    void LD_HLR_R8() { //OK
        Random rng = newRandom();
        int msb,lsb,address,value;
        Cpu c;
        Ram r;
        Bus b;

        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;


            b.write(0, Opcode.LD_B_N8.encoding); //Placing 20 - 16 in BC
            b.write(1, value);
            b.write(2, Opcode.LD_HL_N16.encoding);
            b.write(3, lsb);
            b.write(4, msb);
            b.write(5, Opcode.LD_HLR_B.encoding);


            cycleCpu(c, Opcode.LD_B_N8.cycles+Opcode.LD_HL_N16.cycles+Opcode.LD_HLR_B.cycles);

            assertEquals(r.read(address),value);

        }
    }

    @Test
    void LD_HLRI_A() { // OK
        //Increment
        Random rng = newRandom();
        int msb,msbInc,lsb,lsbInc,address,value,addressInc;
        Cpu c;
        Ram r;
        Bus b;

        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;
            //Hl is incremented
            addressInc=address+1;
            lsbInc=addressInc & MAX_8;
            msbInc=addressInc>>8;

            b.write(0, Opcode.LD_A_N8.encoding); //Placing 20 - 16 in BC
            b.write(1, value);
            b.write(2, Opcode.LD_HL_N16.encoding);
            b.write(3, lsb);
            b.write(4, msb);
            b.write(5, Opcode.LD_HLRI_A.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.LD_HL_N16.cycles+Opcode.LD_HLRI_A.cycles);

            assertArrayEquals(new int[] {6,0,value,0,0,0,0,0,msbInc,lsbInc}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);

        }
    }

    @Test
    void LD_HLRD_A() { // OK
        //Decrement
        Random rng = newRandom();
        int msb,msbInc,lsb,lsbInc,address,value,addressInc;
        Cpu c;
        Ram r;
        Bus b;

        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;
            //Hl is incremented
            addressInc=address-1;
            lsbInc=addressInc & MAX_8;
            msbInc=addressInc>>8;

            b.write(0, Opcode.LD_A_N8.encoding); //Placing 20 - 16 in BC
            b.write(1, value);
            b.write(2, Opcode.LD_HL_N16.encoding);
            b.write(3, lsb);
            b.write(4, msb);
            b.write(5, Opcode.LD_HLRD_A.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.LD_HL_N16.cycles+Opcode.LD_HLRD_A.cycles);

            assertArrayEquals(new int[] {6,0,value,0,0,0,0,0,msbInc,lsbInc}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);

        }
    }

    @Test
    void LD_N8R_A() { // OK
        Random rng = newRandom();
        int addressParam,value,finAddress;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            addressParam=rng.nextInt(MAX_8);
            finAddress=AddressMap.REGS_START +addressParam;

            b.write(0, Opcode.LD_A_N8.encoding); 
            b.write(1, value);
            b.write(2, Opcode.LD_N8R_A.encoding);
            b.write(3, addressParam);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.LD_N8R_A.cycles);

            assertArrayEquals(new int[] {4,0,value,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(finAddress),value);


        }
    }

    @Test
    void LD_CR_A() { // OK
        Random rng = newRandom();
        int addressParam,value,finAddress,cAddress;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            cAddress=rng.nextInt(MAX_8);
            value=rng.nextInt(MAX_8); //The value in C
            finAddress=AddressMap.REGS_START+cAddress; //the value in C + 0xFF00

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, value);
            b.write(2, Opcode.LD_C_N8.encoding);
            b.write(3, cAddress);
            b.write(4, Opcode.LD_CR_A.encoding);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.LD_C_N8.cycles+Opcode.LD_CR_A.cycles);

            assertArrayEquals(new int[] {5,0,value,0,0,cAddress,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(finAddress),value);


        }
    }

    @Test
    void LD_N16R_A() { // OK
        Random rng = newRandom();
        int address,msb,lsb,value;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;

            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, value);
            b.write(2, Opcode.LD_N16R_A.encoding);
            b.write(3, lsb);
            b.write(4, msb);

            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.LD_N16R_A.cycles);


            assertArrayEquals(new int[] {5,0,value,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);
        }
    }

    @Test
    void LD_BCR_A() { // OK
        Random rng = newRandom();
        int address,msb,lsb,value;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;


            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, value);
            b.write(2, Opcode.LD_BC_N16.encoding);
            b.write(3, lsb);
            b.write(4, msb);
            b.write(5, Opcode.LD_BCR_A.encoding);


            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.LD_BC_N16.cycles+Opcode.LD_BCR_A.cycles);

            assertArrayEquals(new int[] {6,0,value,0,msb,lsb,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);
        }
    }

    @Test
    void LD_DER_A() { // OK
        Random rng = newRandom();
        int address,msb,lsb,value;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;


            b.write(0, Opcode.LD_A_N8.encoding);
            b.write(1, value);
            b.write(2, Opcode.LD_DE_N16.encoding);
            b.write(3, lsb);
            b.write(4, msb);
            b.write(5, Opcode.LD_DER_A.encoding);


            cycleCpu(c, Opcode.LD_A_N8.cycles+Opcode.LD_DE_N16.cycles+Opcode.LD_BCR_A.cycles);

            assertArrayEquals(new int[] {6,0,value,0,0,0,msb,lsb,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);
        }
    }

    @Test
    void LD_HLR_N8() { // OK
        Random rng = newRandom();
        int address,msb,lsb,value;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;
            b.write(0, Opcode.LD_HL_N16.encoding);
            b.write(1, lsb);
            b.write(2, msb);
            b.write(3, Opcode.LD_HLR_N8.encoding);
            b.write(4, value);

            cycleCpu(c, Opcode.LD_HL_N16.cycles+Opcode.LD_HLR_N8.cycles);

            assertArrayEquals(new int[] {5,0,0,0,0,0,0,0,msb,lsb}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);
        }
    }

    @Test
    void LD_N16R_SP() { // OK
        Random rng = newRandom();
        int address,msb,lsb,value;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);

            value=rng.nextInt(MAX_8);
            msb=rng.nextInt(MAX_8);
            lsb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;
            b.write(0, Opcode.LD_SP_N16.encoding);
            b.write(1, value);
            b.write(3, Opcode.LD_N16R_SP.encoding);
            b.write(4, lsb);
            b.write(5, msb);

            cycleCpu(c, Opcode.LD_SP_N16.cycles+Opcode.LD_N16R_SP.cycles);

            assertArrayEquals(new int[] {6,value,0,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(r.read(address),value);
        }
    }

    @Test
    void PUSH_R16() { //OK
        Random rng = newRandom();
        int value16,value8,msb,lsb,msbV,lsbV,address,finValue;
        Cpu c;
        Ram r;
        Bus b;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            c = new Cpu();
            r = new Ram(0xFFFF);
            b = connect(c, r);


            lsb=rng.nextInt(MAX_8-8)+8;
            msb=rng.nextInt(MAX_8);
            address=(msb<<8)+lsb;
            
            lsbV=rng.nextInt(MAX_8-8)+8;
            msbV=rng.nextInt(MAX_8);
            value16=(msbV<<8)+lsb;


            finValue=(address-2);
            
            b.write(0, Opcode.LD_BC_N16.encoding);
            b.write(1, lsbV);
            b.write(2, msbV);
            b.write(3, Opcode.LD_SP_N16.encoding);
            b.write(4, lsb);
            b.write(5, msb);
            b.write(6, Opcode.PUSH_BC.encoding);
            

            cycleCpu(c, Opcode.LD_BC_N16.cycles+Opcode.LD_SP_N16.cycles+Opcode.PUSH_BC.cycles);

            assertArrayEquals(new int[] {7,finValue,0,0,msbV,lsbV,0,0,0,0}, c._testGetPcSpAFBCDEHL());
            assertEquals(lsbV,r.read(finValue));
            assertEquals(msbV,r.read(finValue+1));
        }
    }
    
    
    
    ///////////////////////////////////////////////////////////
    // Part. 4 
    ///////////////////////////////////////////////////////////
    
//  *************
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
      System.out.println((byte)v);
      System.out.println((byte)v + i);
  }
    
    
}
