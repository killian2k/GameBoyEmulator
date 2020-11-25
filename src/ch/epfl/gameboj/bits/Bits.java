package ch.epfl.gameboj.bits;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * Class that contains ONLY useful methods to manipulate bits (is not instanciable).
 */
public final class Bits {
    private final static int[] TABLE_REVERSE_8BITS=new int[] {
            0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0,
            0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0,
            0x08, 0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8,
            0x18, 0x98, 0x58, 0xD8, 0x38, 0xB8, 0x78, 0xF8,
            0x04, 0x84, 0x44, 0xC4, 0x24, 0xA4, 0x64, 0xE4,
            0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74, 0xF4,
            0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC,
            0x1C, 0x9C, 0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC,
            0x02, 0x82, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2,
            0x12, 0x92, 0x52, 0xD2, 0x32, 0xB2, 0x72, 0xF2,
            0x0A, 0x8A, 0x4A, 0xCA, 0x2A, 0xAA, 0x6A, 0xEA,
            0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA,
            0x06, 0x86, 0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6,
            0x16, 0x96, 0x56, 0xD6, 0x36, 0xB6, 0x76, 0xF6,
            0x0E, 0x8E, 0x4E, 0xCE, 0x2E, 0xAE, 0x6E, 0xEE,
            0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E, 0xFE,
            0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1,
            0x11, 0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1,
            0x09, 0x89, 0x49, 0xC9, 0x29, 0xA9, 0x69, 0xE9,
            0x19, 0x99, 0x59, 0xD9, 0x39, 0xB9, 0x79, 0xF9,
            0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65, 0xE5,
            0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5,
            0x0D, 0x8D, 0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED,
            0x1D, 0x9D, 0x5D, 0xDD, 0x3D, 0xBD, 0x7D, 0xFD,
            0x03, 0x83, 0x43, 0xC3, 0x23, 0xA3, 0x63, 0xE3,
            0x13, 0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73, 0xF3,
            0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB,
            0x1B, 0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB,
            0x07, 0x87, 0x47, 0xC7, 0x27, 0xA7, 0x67, 0xE7,
            0x17, 0x97, 0x57, 0xD7, 0x37, 0xB7, 0x77, 0xF7,
            0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F, 0xEF,
            0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF,
    };


    // Unique constructor of this not-instanciable class.
    private Bits() {}

    /**
     * Method that return a integer with only one bit=1.
     * @param index the position of the 1's bit must be in 0 (included) and 32 (excluded).
     * @return the int value with only 1 at the index position, 0 otherwise.
     * @throws IndexOutOfBoundsException if the index is not valid ([0;32[).
     */
    public static int mask(int index){
        Objects.checkIndex(index, Integer.SIZE);
        return 1 << index;
    }

    /**
     * Method to test if the bit at the index's position is 1 or 0 for the number represented by bits.
     * @param bits the number to test.
     * @param index the bit-index of bits to test.
     * @return true iff the bit is 1 false otherwise.
     * @throws IndexOutOfBoundsException if the index is not valid ([0;32[).
     */
    public static boolean test(byte bits, int index) {
    	Objects.checkIndex(index, Byte.SIZE);
    	return ((bits & mask(index)) != 0);
    }
    
    /**
     * Method to test if the bit at the index's position is 1 or 0 for the number represented by bits.
     * @param bits the number to test.
     * @param index the bit-index of bits to test.
     * @return true iff the bit is 1 false otherwise.
     * @throws IndexOutOfBoundsException if the index is not valid ([0;32[).
     */
    public static boolean test(int bits, int index){
        return ((bits & mask(index)) != 0);
    }

    /**
     * Method to test if the bit at the index of bit position is 1 or 0 for the number represented by bits.
     * @param bits the number to test.
     * @param bit the bit to test.
     * @return true iff the bit is 1 false otherwise.
     * @throws IndexOutOfBoundsException if the index is not valid ([0;32[).
     */
    public static boolean test(int bits, Bit bit){
        return test(bits, bit.index());
    }    

    /**
     * Method to set the value of a bit in a byte.
     * @param bits the Initial bits.
     * @param index the bit's position of the bit to set.
     * @param newValue the value of the index's bit true=1 false = 0.
     * @throws IndexOutOfBoundsException if the index is not valid ([0;32[). 
     */
    public static int set(int bits, int index, boolean newValue){
        int mask = mask(index);
        return newValue ? (bits | mask) : (bits & (~mask)) ;
    }

    /**
     * Method to extract the "size" LSB bits of "bits".
     * @param size the number of LSB bits we want to keep (between [1:32]).
     * @param bits the number where we extract the bits.
     * @return The integer corresponding to the extracted bits.
     * @throws IndexOutOfBoundsException if the size is not valid. 
     */
    public static int clip(int size, int bits) {
        Preconditions.checkArgument(size>=0 && size<=Integer.SIZE);
        // The << 32 is not possible with the left shift  
        if(size == Integer.SIZE)
            return bits;
        int mask = ~(0xFFFFFFFF << size);
        return bits & mask;
    }

    /**
     * Extract bits from an integer. from "start" (LSB) and of length "size" (to the MSB)
     * @param bits the number where the bits are extracted.
     * @param start the start position where we extract the bits (0 corresponds to the LSB bit).
     * @param size the number of LSB bits we want to keep (between [1:32]).
     * @return The integer corresponding to the extracted bits.
     * @throws IndexOutOfBoundsException if the size is not valid must be in [0;32[.
     */
    public static int extract(int bits, int start, int size) {
        Objects.checkFromIndexSize(start, size, Integer.SIZE);
        bits = bits >>> start;
        return clip(size, bits);
    }

    /**
     * Method rotating bits in the way of the distance sign (the last come the first).
     * @param size number of bit that are taken from the start.
     * @param bits the number we use for the rotation.
     * @param distance the distance of rotation. positive = to the left, negative = to the right.
     * @return the integer corresponding to the rotated bits.
     * @throws IllegalArgumentException if the size is not valid (from 0 (excluded) to 32 (included))
     * or if "bits" is a value of less than "size" bit.
     */
    public static int rotate(int size, int bits, int distance) {
        Preconditions.checkArgument(size > 0 && size <= 32 && Integer.toBinaryString(bits).length() <= size);
        int decal = Math.floorMod(distance, size);
        int bitsToRotate = clip(size,bits);
        return clip(size, (bitsToRotate << decal | bitsToRotate>>>(size-decal)));
    }

    /**
     * Method extending the sign of the number as binary to 32 bits.
     * @param b is the 8 bit value to treat.
     * @return the integer with the sign bit extended to 32 bits.
     * @throws IllegalArgumentException if the given b is not a 8bit value.
     */
    public static int signExtend8(int b) {
        Preconditions.checkBits8(b);
        return (byte)b;
    }

    /**
     * Method to obtain the 8 bits of the argument reversed (MSB become LSB and vice-versa).
     * @param b the 8 bits to manage.
     * @return The integer with the 8 bits reversed.    
     * @throws IllegalArgumentException if the given b is not a 8bits value.
     */
    public static int reverse8(int b) {
        Preconditions.checkBits8(b);
        return TABLE_REVERSE_8BITS[b];
    }

    /**
     * Inverse the 8 bits of the argument.
     * @param b the 8 bits to inverse.
     * @return an integer with 8 LSB bits inversed from "b"
     * @throws IllegalArgumentException if the given b is not a 8bits value.
     */
    public static int complement8(int b) {
        Preconditions.checkBits8(b);
        return 0xFF ^ b;
    }

    /**
     * Method concatenating 2 8 bits values into a 16 bits value returned.
     * @param highB : the 8 MSB bits of the 16 bits returned.
     * @param lowB : the 8 LSB bits of the 16 bits returned.
     * @return The final 16 bits binary.
     * @throws IllegalArgumentException iff "highB" or "lowB" is not a 8bits value.
     */
    public static int make16(int highB, int lowB) {
        Preconditions.checkBits8(lowB);
        Preconditions.checkBits8(highB);
        return (highB << 8) | lowB;
    }    
    
    /**
     * Get the 8 msb from a 16 bits integer.
     * @param The integer where to extract the 8 bits
     * @return the 8 bits extracted
     * @throws IllegalArgumentException if the integer is not a 16 bits value.
     */
    public static int get8MsbFrom16Bits(int b) {
        Preconditions.checkBits16(b);
        return Bits.extract(b, 8, 8);
    }
    
    /**
     * Get the 8 lsb from an integer
     * @param The integer where to extract the 8 bits
     * @return the 8 bits extracted
     */
    public static int get8Lsb(int b) {
        return Bits.clip(8, b);
    }
}