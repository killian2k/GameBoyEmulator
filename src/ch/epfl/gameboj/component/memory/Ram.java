package ch.epfl.gameboj.component.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;


import ch.epfl.gameboj.Preconditions;

/**
 * Represents the RAM of the Gameboy.
 */
public final class Ram {
    //private final byte[] data;
    private byte[] data;

    /**
     * Unique constructor of the class.
     * @param size the integer size of the memory. must be greater or equal to 0.
     * @throws IllegalArgumentException if the size is a negative number.
     */
    public Ram(int size) {
        Preconditions.checkArgument(size >= 0);
        data = new byte[size];
    }

    /**
     * Get the size of the memory.
     * @return The integer size of the memory (in byte).
     */
    public int size() {
        return data.length;
    }

    /**
     * Read the byte value in the memory at the address "index".
     * @param index the address of the RAM where we want to read the data.
     * @return an integer representing the byte we asked.
     * @throws IndexOutOfBoundsException if index is not pointing to a valid data index: [0;size()[.
     */
    public int read(int index) {
        Objects.checkIndex(index, data.length);
        return Byte.toUnsignedInt(data[index]);  
    }

    /**
     * Write a value in a address of the memory.
     * @param index the address of the RAM where we want to store our data. 
     *          Must be in the range of the RAM memory:[0;size()[.
     * @param value the data we want to store. Must be a 8bit value.
     * @throws IllegalArgumentException if value is not a 8bits value.
     * @throws IndexOutOfBoundsException if index is not pointing to a valid data index.
     */
    public void write(int index, int value) {
        Preconditions.checkBits8(value);
        Objects.checkIndex(index, data.length);
        data[index] = (byte)value;
    }
    
    
    
    
    public void writeRamIntoFile(File f) throws IOException {
        Preconditions.checkArgument(f != null);
        f.mkdirs();
        f.createNewFile();
        ByteArrayOutputStream output = new ByteArrayOutputStream(data.length);
        output.write(data);
        try(OutputStream outputStream = new FileOutputStream(f)) {
            output.writeTo(outputStream);
        }
    }
    
    public void readRamFromFile(File f, int startIndex, int size) throws IOException {
        Preconditions.checkArgument(f != null && startIndex >= 0 && size > 0);
        System.arraycopy(Files.readAllBytes(f.toPath()), startIndex, data, 0, size);
        
    }
}
