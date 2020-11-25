package ch.epfl.gameboj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled class ___GameBoyTest {
    // Gameboy
    

    // Bus
    @Test
    void BusWriteAtBeginOfMemory() {
        GameBoy gb = new GameBoy(null);
        Bus bus = gb.bus();
        /*bus.write(0 , 0);
        bus.write(4, 5);
        assertEquals(0, bus.read(0));
        bus.read(4));*/
        bus.write(0xC000, 3);
        assertEquals(3, bus.read(0xC000));
        assertEquals(3, bus.read(0xE000));
        assertNotEquals(3, bus.read(0xC001));
        bus.write(0xE000, 250);
        assertEquals(250, bus.read(0xC000));
        assertEquals(250, bus.read(0xE000));
        assertNotEquals(250, bus.read(0xC001));
    }
    
    @Test
    void BusWriteAtEndOfMemory() {
        GameBoy gb = new GameBoy(null);
        Bus bus = gb.bus();
        bus.write((0xC000+8191), 7);
        assertEquals(7, bus.read(0xC000+8191));
        assertNotEquals(7, bus.read(0xE000+8191));
        assertNotEquals(7, bus.read(0xC001+8191));
        bus.write(0xE000+8191, 250);
        assertNotEquals(250, bus.read(0xC000+8191));
        assertNotEquals(250, bus.read(0xE000+8191));
        assertNotEquals(250, bus.read(0xC001+8191));
    }
    
    @Test // because only 7680 bytes are available from E0000 
    void BusWriteAtEndOfMemory2() {
        GameBoy gb = new GameBoy(null);
        Bus bus = gb.bus();
        bus.write((0xC000+7679), 7);
        assertEquals(7, bus.read(0xC000+7679));
        assertEquals(7, bus.read(0xE000+7679));
        assertNotEquals(7, bus.read(0xC001+7679));
        bus.write(0xE000+7679, 250);
        assertEquals(250, bus.read(0xC000+7679));
        assertEquals(250, bus.read(0xE000+7679));
        assertNotEquals(250, bus.read(0xC001+7679));
    }
    
    @Test  
    void BusWriteOverEndOfMemory2() {
        GameBoy gb = new GameBoy(null);
        Bus bus = gb.bus();
        bus.write((0xC000+8192), 7); //= 0xE0000
        assertEquals(7, bus.read(0xE000));
        assertEquals(7, bus.read(0xC000));
        assertNotEquals(7, bus.read(0xC001+8193));
        bus.write(0xE000+7680, 221);
        assertNotEquals(221, bus.read(0xC000+7680));
        assertNotEquals(221, bus.read(0xE000+7680));
        assertNotEquals(221, bus.read(0xC001+7680));
    }
    
}
