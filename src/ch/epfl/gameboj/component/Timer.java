package ch.epfl.gameboj.component;

import java.util.Objects;
import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * Class representing the Timer.
 * This is a component of the Gameboy and is directed by the Clock
 */
public final class Timer implements Component, Clocked {
    private final Cpu cpu;
    private int regDiv = 0, regTima = 0, regTMA = 0, regTAC = 0;
    private static final int INDEX_BIT_STATE_TIMER = 2;

    /**
     * The unique constructor of the class Timer.
     * @param cpu is the Cpu linked to the timer.
     * @throws NullPointerException if the cpu given is null.
     */
    public Timer(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
    }

    @Override 
    public void cycle(long cycle) {
        boolean s0 = state();
        regDiv = Bits.clip(16, regDiv + 4);
        incIfChange(s0);
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        switch(address) {
        case AddressMap.REG_DIV: return getRegDivHigh();
        case AddressMap.REG_TIMA: return regTima;
        case AddressMap.REG_TMA: return regTMA;
        case AddressMap.REG_TAC: return regTAC;
        }

        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        boolean s0 = state();
        switch(address) {
        case AddressMap.REG_DIV:
            regDiv = 0;
            break;
        case AddressMap.REG_TIMA:
            regTima = data;
            return;
        case AddressMap.REG_TMA:
            regTMA = data;
            return;
        case AddressMap.REG_TAC:
            regTAC = data;
            break;
        }

        incIfChange(s0);
    }
    //Get the state of the timer (True/False) depending on the TAC and the DIV register.
    private boolean state() {
        return Bits.test(regTAC, INDEX_BIT_STATE_TIMER) && Bits.test(regDiv, getIndexFromTAC2Bits());
    }

    //Increment the TIMA register if the state has changed.
    private void incIfChange(boolean lastState) {
        if(lastState && !state())
            incTIMA();
    }

    //Increment Tima register and if TIMA = 0xFF create an interruption.
    private void incTIMA(){
        if(regTima != 0xFF)
            ++regTima;
        else {
            cpu.requestInterrupt(Interrupt.TIMER);
            regTima = regTMA;
        }
    }

    /*
     * Get the value from the bus in the address of the register DIV.
     * Corresponds to the 8 MSB bits of register DIV.
     */
    private int getRegDivHigh() {
        return Bits.extract(regDiv, 8, 8);
    }

    private int getIndexFromTAC2Bits(){
        int bits = Bits.clip(2, regTAC);
        switch (bits) {
        case 0b00: return 9;
        case 0b01: return 3;
        case 0b10: return 5;
        default:
        case 0b11: return 7;
        }
    }
}
