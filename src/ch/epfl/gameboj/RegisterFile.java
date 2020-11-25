package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Represent a 8 bits register file.
 * @param <E> the kind of Register we will use.
 */
public final class RegisterFile<E extends Register> {
    private final byte[] register;

    /**
     * Constructor of the class. Create the empty Register.
     * @param allRegs is the array allRegs length is used to determine the size of this register file.
     */
    public RegisterFile(E[] allRegs){
        register = new byte[allRegs.length]; 
    }

    /**
     * Get the 8bits value from the given register.
     * @param reg is the register whose we want the value.
     * @return The value corresponding to the parameter [0;255].
     */
    public int get(E reg) {
        return Bits.clip(8, register[reg.index()]);
    }

    /**
     * Modify the content of the given register with the eight bits value given in parameter.
     * @param reg is the register which gonna store the new value.
     * @param newValue is the new eight bits value for the register.
     * @throws IllegalArgumentException if the newValue arg is not a 8bits value.
     */
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        register[reg.index()] = (byte)newValue;
    }

    /**
     * Return the value of the given bit from the given register.
     * @param reg is the register where we want to test a bit.
     * @param b is the bit we want to test.
     * @return true iff bit = 1, false iff bit = 0.
     */
    public boolean testBit(E reg, Bit b) {
        return Bits.test(get(reg), b);
    }
    
    /**
     * Return the value of the given bit from the given register.
     * @param reg is the register where we want to test a bit.
     * @param index is the bit index we want to test.
     * @return true iff bit = 1, false iff bit = 0.
     * @throws IndexOutOfBoundsException if the index is not valid ([0;32[).
     */
    public boolean testBit(E reg, int index) {
        return Bits.test(get(reg), index);
    }

    /**
     * Set the boolean value as "newValue" of a given bit from a given register.
     * @param reg is the register where we want to define a specific bit.
     * @param bit is the specific bit we want to define. 
     * @param newValue is the value we want to set to the bit of the register. True = 1, False = 0.
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        set(reg, Bits.set(get(reg), bit.index(), newValue));
    }
}
