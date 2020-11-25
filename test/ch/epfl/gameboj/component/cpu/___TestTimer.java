package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cartridge.MBC0;
import ch.epfl.gameboj.component.memory.BootRom;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Rom;
import ch.epfl.gameboj.debug.DebugPrintComponent;

@Disabled
class ___TestTimer {

    @Test
    void checkMBC0() {
        Rom r = null;
        MBC0 m = new MBC0(r);
        Component c = m;
        System.out.println(c);
    }
    
    @Test
    void checkCartridge() throws IOException {
        Cartridge c = Cartridge.ofFile(new File(""));
        Component c2 = c;
        System.out.println(c2);
    }
    
    @Test
    void checkBootRomController() {
        Cartridge c = null;
        BootRomController b = new BootRomController(c);
        Component c2 = b;
        System.out.println(c2);
    }
    
    @Test
    void checkTimer() {
        Cpu c = null;
        Timer t = new Timer(c);
        Component c2 = t;
        Clocked c3 = t;
        System.out.println(c2 + "" + c3);
    }
    
    @Test
    void checkGameBoy() {
        Cartridge c = null;
        GameBoy g = new GameBoy(c);
        Timer t = g.timer();
        System.out.println(t);
    }
    
    @Test
    void checkBootRomImport() {
        byte[] d = BootRom.DATA;
        System.out.println(d);
    }
    
    
    //@Test
    void test() throws Exception {
        String[] filesnames = {"01-special.gb", "02-interrupts.gb" , "03-op sp,hl.gb", "04-op r,imm.gb", "05-op rp.gb", "06-ld r,r.gb", "07-jr,jp,call,ret,rst.gb", "08-misc instrs.gb", "09-op r,r.gb", "10-bit ops.gb", "11-op a,(hl).gb", "instr_timing.gb"};
        String arg2 = "30000000";
        //32int i = 11;
        for(int i = 0;i<filesnames.length;++i) { 
            System.out.println(filesnames[i]);
            File romFile = new File(filesnames[i]);
            long cycles = Long.parseLong(arg2);
            int c;
            boolean b=false;
            
            
            GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
            //Affiche la cartouche
            /*
            System.out.println("Entering the cartridge :");
            for(int j=0;j<0x147;j++) {
                c=Cartridge.ofFile(romFile).read(j);
                if(c!=0) {
                    for(Opcode op : buildOpcodeTable(Opcode.Kind.PREFIXED)) {
                        if((op.encoding==c) && !b) {
                            System.out.printf("%10s |",op.toString());
                            b=true;
                        }
                    }
                    if(!b) {
                        System.out.printf("%10d |", c);
                    }else {
                        b=false;
                    }
                }else{
                    System.out.printf("%10d |", c);
                }
                if(j%10==0) {
                    System.out.println();
                }
            }*/
            
            Component printer = new DebugPrintComponent();
            printer.attachTo(gb.bus());
            
            while (gb.cycles() < cycles) {
                long nextCycles = Math.min(gb.cycles() + 17556, cycles);
                gb.runUntil(nextCycles);
                gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
                System.out.println(gb.cycles());
                //System.out.println("fini");
            }
        }
    }

}
