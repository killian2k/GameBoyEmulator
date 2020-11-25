package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Represents the cartridge of the Gameboy.
 * Implements Component but must not be connected to the bus.
 */
public final class Cartridge implements Component{
    private static final int ADDRESS_TYPE_CARTRIDGE = 0x147;
    private static final int[] RAM_SIZE = new int[] {0, 2048, 8192, 32768};
    private final Component bankController;

    //Private constructor of the class. Used by static method ofFile.
    private Cartridge(Component bankMemoryController) {
        this.bankController = bankMemoryController;
    }

    /**
     * Static method that return a new Object Cartridge using the romFile given.
     * The purpose is to use this method like a constructor.
     * @param romFile is the ROM we want to emulate.
     * @return The Cartridge created.
     * @throws IOException : If there is a error while reading the romFile.
     */
    @SuppressWarnings("resource")
    public static Cartridge ofFile(File romFile) throws IOException {
        InputStream s = new FileInputStream(romFile);
        byte[] bytes = s.readAllBytes();
        if(bytes[ADDRESS_TYPE_CARTRIDGE] == 0)
            return new Cartridge(new MBC0(new Rom(bytes)));
        return new Cartridge(new MBC1(new Rom(bytes), RAM_SIZE[bytes[MBC1.ADDRESS_RAM_SIZE]]));
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return bankController.read(address);
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        bankController.write(address, data);
    }
}