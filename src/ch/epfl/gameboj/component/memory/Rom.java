/*
 * Author : 	Bastien Lüscher & Killian Kämpf
 * Date   :  19 févr. 2018
 */
package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * Class representing the read only memory
 */
public final class Rom implements Preconditions {
    private byte[] data;

    /**
     * The unique constructor of the class rom
     * @param data byte[] the content of the ROM
     * @throws NullPointerException if the array given is null
     *      
     */
    public Rom(byte[] data) {
        Objects.requireNonNull(data);
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Return the size (in byte) of the ROM
     * @return an integer representing the size
     */
    public int size() {
        return data.length;
    }

    /**
     * Read a byte from the memory
     * @param index correspond to the address of the byte to read
     * @return the byte as integer corresponding to the address if the index is ok
     * @throws IndexOutOfBoundsException if the index is not in the good range
     */
    public int read(int index) {
        Objects.checkIndex(index, data.length);
        // Manage to have only positive numbers
        return Byte.toUnsignedInt(data[index]);        
    }
}
