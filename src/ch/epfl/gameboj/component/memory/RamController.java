package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * Represents a component which has access to the RAM
 */
public final class RamController implements Component, Preconditions{
    private final int startAddress;
    private final int endAddress;
    private final Ram ram;

    /**
     * Constructor of the RamController.
     * @param ram The Ram linked to this controller.
     * @param startAddress The start address (included) in the ram linked to this controller.
     * @param endAddress  The end address (excluded) in the ram linked to the controller.
     * @throws NullPointerException if ram is null.
     * @throws IllegalArgumentException if startAddress or endAddress is not a 16bits value or
     * if endAddress < startAddress or if the difference between end- and startAddress is greater than ram size.
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        Objects.requireNonNull(ram);
        Preconditions.checkBits16(startAddress);
        Preconditions.checkBits16(endAddress);
        Preconditions.checkArgument(((endAddress - startAddress) >= 0) && (endAddress - startAddress) <= ram.size());
        this.ram = ram;
        this.startAddress = startAddress;
        this.endAddress = endAddress;

    }

    /**
     * Constructor of RamController with no end address specified
     * @param ram: The Ram object linked to this controller
     * @param startAddress: The start address (included) in the RAM for the controller
     * @throws IllegalArgumentException if startAddress or endAddress is not a 16bits value or
     * if endAddress < startAddress or if the difference between end- and startAddress is greater than ram size.
     */
    public RamController(Ram ram, int startAddress) {
        this(ram,startAddress,startAddress + ram.size());
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if(checkRangeAddress(address))
            return ram.read(address-startAddress);

        return Component.NO_DATA;        
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if(checkRangeAddress(address))
            ram.write(address-startAddress,data);        
    }

    // Check if the address in parameter is in the range from startAddress to endAddress
    private boolean checkRangeAddress(int address) {
        return (address >= startAddress && address < endAddress);
    }
}
