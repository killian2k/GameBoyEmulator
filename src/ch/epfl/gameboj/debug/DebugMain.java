package ch.epfl.gameboj.debug;

import java.io.File;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class DebugMain {
    
    /**
     * Method to run the Blargg tests
     * @param args : unused
     * @throws Exception: A error can happen when reading the romFile
     */
    public static void main(String[] args) throws Exception {
    	//String[] filesnames = {"01-special.gb", "02-interrupts.gb" , "03-op sp,hl.gb", "04-op r,imm.gb", "05-op rp.gb", "06-ld r,r.gb", "07-jr,jp,call,ret,rst.gb", "08-misc instrs.gb", "09-op r,r.gb", "10-bit ops.gb", "11-op a,(hl).gb", "instr_timing.gb"};
        String[] filesnames = {"games/Blargg/cpu_instrs.gb"};
        String arg2 = "30000000";
        for(int i = 0;i<filesnames.length;++i) { 
            File romFile = new File(filesnames[i]);
            long cycles = Long.parseLong(arg2);
            GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));            
            Component printer = new DebugPrintComponent();
            printer.attachTo(gb.bus());
            while (gb.cycles() < cycles) {
                long nextCycles = Math.min(gb.cycles() + 17556, cycles);
                gb.runUntil(nextCycles);
                gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
            }
        }
    }
}