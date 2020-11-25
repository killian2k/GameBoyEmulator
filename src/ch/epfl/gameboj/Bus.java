package ch.epfl.gameboj;


import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * Class for the bus who is used to connect the components together
 */
public final class Bus {
    //Dynamic array of attached component with the Bus
    private final ArrayList<Component> arrayComp=new ArrayList<Component>();

    private static final int DEFAULT_READ_VALUE = 0xFF;

    /**
     * Method to link the bus with a new component 
     * @param component the component to attach
     * @throws NullPointerException if the component is null
     */
    public void attach(Component component) throws NullPointerException {
        Objects.requireNonNull(component);
        arrayComp.add(component);
    }

    /**
     * Method that read the value of an Address on all attached component
     * returning the first found 
     * @param address the address of value location of the components
     * @throws IllegalArgumentException if the address is not a 16bit value
     */
    public int read(int address) throws IllegalArgumentException {
        Preconditions.checkBits16(address);
        for(Component c : arrayComp) {
            int read = c.read(address);
            if(read!=Component.NO_DATA) {
                return read;
            }
        }

        return DEFAULT_READ_VALUE;
    }

    /**
     * Method that store the value to the given address on every attached component
     * @param address the address in memory where to store data
     * @param data the data to store at the address
     * @throws IllegalArgumentException if the address is not a 16 bit value
     *          or the data is not a 8bit value
     */
    public void write(int address, int data) throws IllegalArgumentException{
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        for(Component c : arrayComp) {
            c.write(address,data);
        }
    }	
}