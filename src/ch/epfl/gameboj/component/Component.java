package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * The interface to represent the components of the Gameboy connected to the bus
 * and to the data.
 */
public interface Component {
    public static final int NO_DATA=0x100;

    /**
     * Method for reading the value in the argument address of the component.
     * @param address the address of value location of the component.
     * @return an integer corresponding to the value read or NO_DATA if not contained by the component.
     * @throws IllegalArgumentException iff the address is not a 16bits number.  
     */
    int read(int address) throws IllegalArgumentException;

    /**
     * Method for writing the argument "data" in the argument "address" of the component.
     * @param address the address where to store the data.
     * @param data the data to write at the address.
     * @throws IllegalArgumentException if the argument address given is not a 16bits number .
     * or if data is not a 8bits value.
     */
    void write(int address, int data) throws IllegalArgumentException;

    /**
     * Default method to attach the given bus with the component.
     * @param bus the bus on which the component is attached.
     */
    default void attachTo(Bus bus){
        bus.attach(this);
    }
}