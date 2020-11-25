package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Represent a type-0 bank memory controller(32768 bytes).
 * Implements Component but must not be connected to the bus.
 */
public final class MBC0 implements Component{
    private final Rom rom;
    public static final int SIZE_ROM_MCB = 0x8000;

    /**
     * Constructor of the class corresponding to a type-0 bank memory controller(32768 bytes).
     * @param rom : the rom corresponding to this object. Must be of type-0.
     * @throws NullPointerException if the rom is null.
     * @throws IllegalArgumentException if the Rom size is not equal to 0x8000.
     */
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == SIZE_ROM_MCB);

        this.rom = rom;
    }

    @Override
    public int read(int address){
        Preconditions.checkBits16(address);
        if(address < SIZE_ROM_MCB)
            return rom.read(address);

        return NO_DATA;
    }

    //Read only memory
    @Override
    public void write(int address, int data) throws IllegalArgumentException {
        return;
    }
}
