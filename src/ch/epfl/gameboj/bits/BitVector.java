package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;


import ch.epfl.gameboj.Preconditions;

/**
 * Immutable Class to represent bits vectors which size is a multiple of 32.
 */
public final class BitVector {
    private final int[] vector;
    /**
     * Constructor of the BitVector.
     * @param bitSize is the number of bits that compose BitVector.
     * @param initValue : True=1, False=0 define the initial value of all bits.
     * @throws IllegalArgumentException if the bitSize is not positive or not a multiple of 32. 
     */
    public BitVector(int bitSize, boolean initValue) {
        checkBitSize(bitSize);
        vector = new int[bitSize/32];
        if(initValue)
            Arrays.fill(vector, 0xFFFFFFFF);
    }

    /**
     * Constructor of bitVector that set the bits at 0.
     * @param bitSize is the number of bits that compose BitVector.
     * @throws IllegalArgumentException if the bitSize is not positive or not a multiple of 32.
     */
    public BitVector(int bitSize) {
        this(bitSize, false);
    }

    private BitVector(int[] elements) {
        Objects.requireNonNull(elements);
        vector = elements;
    }

    /**.
     * Get the size in bit of the BitVector.
     * @return the size.
     */
    public int size() {
        return vector.length * 32;
    }

    /**
     * Method that test the bit given by the index of the vector.
     * @param index: The index of the the bit.
     * @return true=1/false=0 depending on the bit value.
     * @throws IllegalArgumentException if the index is not positive.
     */
    public boolean testBit(int index) {
        Preconditions.checkArgument(index >= 0);
        return Bits.test(vector[index/32], index%32);
    }

    /**
     * Invert the value of every bit of the vector.
     * @return a new BitVector representing the inverse.
     */
    public BitVector not() {
        int[] notArray = new int[vector.length];
        for(int i=0;i<notArray.length;++i)
            notArray[i] = ~vector[i];
        return new BitVector(notArray);
    }

    /**
     * Achieve Or operator on this vector and the one given in argument and return the result in a new instance.
     * @param v2 : The second vector to use for the operation.
     * @return The new BitVector.
     * @throws nullPointerException if the argument v2 is null.
     */
    public BitVector or(BitVector v2) {
        return manageOrAnd(v2, true);
    }

    /**
     * Achieve And operator on this vector and the one given in argument and return the result in a new instance.
     * @param v2 : The second vector to use for the operation.
     * @return The new BitVector.
     * @throws nullPointerException if the argument v2 is null.
     */
    public BitVector and(BitVector v2) {
        return manageOrAnd(v2, false);
    }

    /**
     * Use the zero extended way to extract a BitVector.
     * @param index : The index where to start the extraction.
     * @param size : The size of the new BitVector.
     * @return The new BitVector extracted.
     * @throws IllegalArgumentException if size is not a positive multiple of 32.
     */
    public BitVector extractZeroExtended(int index, int size) {
        return extract(index, size, true);
    }

    /**
     * Use the wrapped extended way to extract a BitVector.
     * @param index : The index where to start the extraction.
     * @param size : The size of the new BitVector.
     * @return The new BitVector extracted.
     * @throws IllegalArgumentException if size is not a positive multiple of 32.
     */
    public BitVector extractWrapped(int index, int size) {
        return extract(index, size, false);
    }

    /**
     * Shift all the bits of the vector.
     * @param distance is the number of shift we want. Positive = to the left, Negative = to the right
     * @return the result in a new Instance of BitVector.
     */
    public BitVector shift(int distance) {
        return extractZeroExtended(distance, vector.length*32);
    }

    /**
     * {@link java.lang.Object#equals(Object)}
     */
    public boolean equals(Object that) {
        return that instanceof BitVector &&
                Arrays.equals(vector, ((BitVector)that).vector);
    }

    /**
     * {@link java.lang.Object#hashCode()}
     */
    public int hashCode() {
        return Arrays.hashCode(vector);
    }

    /**
     * {@link java.lang.Object#toString()}
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(int n : vector) {
            s.insert(0, String.format("%32s", Integer.toBinaryString(n)).replace(' ', '0'));
        }
        return s.toString();
    }

    /**
     * Set a selected byte of the BitVector and return the result.
     * @param byteIndex : the index of the new byte.
     * @param value : the value to set
     * @return the new BitVector object with the byte changed.
     * @throws IllegalArgumentException if the value is not a 8bits value.
     * @throws IndexOutOfBoundsException if the byteIndex given is not valid [0:size_vector[.
     */
    public BitVector setByte(int byteIndex, int value) {
        Objects.checkIndex(byteIndex*Byte.SIZE, size());
        int indexBit = (byteIndex%Integer.BYTES)*Byte.SIZE;
        int[] newVect = vector.clone();
        newVect[byteIndex/Integer.BYTES] = setByteOfInt(newVect[byteIndex/Integer.BYTES], indexBit, value);
        return new BitVector(newVect);
    }
    
    private static int setByteOfInt(int toSet, int indexBit, int newVal) {
        Preconditions.checkBits8(newVal);
        return  (toSet & ~(0xFF << indexBit)) | (newVal << indexBit);
    }

    private static void checkBitSize(int bitSize) {
        Preconditions.checkArgument(bitSize > 0 && (bitSize%32) == 0);
    }

    private BitVector extract(int index, int size, boolean isZeroExtended) {
        checkBitSize(size);
        int[] grpsOfBits = new int[size/32];

        if(Math.floorMod(index, 32) == 0) {
            for(int i=0;i < grpsOfBits.length;++i)
                grpsOfBits[i] = getValueGrpDependingExtension(i +  Math.floorDiv(index, 32), isZeroExtended);
        }
        else {
            int numberOfBitsV0 = 32 - Math.floorMod(index, 32);
            for(int i=0;i < grpsOfBits.length;++i)
                grpsOfBits[i] = (getValueGrpDependingExtension(i + Math.floorDiv(index, 32)+1, isZeroExtended) << numberOfBitsV0) | 
                Bits.extract(getValueGrpDependingExtension(i + Math.floorDiv(index, 32), isZeroExtended), 32 - numberOfBitsV0, numberOfBitsV0);
        }       

        return new BitVector(grpsOfBits);
    }

    private int getValueGrpDependingExtension(int index, boolean isZeroExtended) {
        if(isZeroExtended)
            return (index >= 0 && index < vector.length)?vector[index]:0;
        return vector[Math.floorMod(index, vector.length)];
    }

    private BitVector manageOrAnd(BitVector v2, boolean isOr) {
        Objects.requireNonNull(v2);
        int max = Math.max(vector.length, v2.vector.length);
        int min = Math.min(vector.length, v2.vector.length);
        int[] newVect = new int[max];
        boolean isV1Max = (max==vector.length);
        for(int i = 0;i<min;++i)
            newVect[i] = (isOr)?(vector[i] | v2.vector[i]): (vector[i] & v2.vector[i]);
        if(isOr)
            System.arraycopy((isV1Max)?vector:v2.vector, min, newVect, min, max-min);
        return new BitVector(newVect);
    }

    /**
     * Intern class to have a builder of BitVector.
     */
    public static final class Builder {
        private int[] vectorValues;

        /**
         * Constructor of the Builder of StringVector.
         * @param bitSize the number of bit of the new Vector. Must be a multiple of 32.
         * @throws IllegalArgumentException if the bitSize is not positive or not a multiple of 32.
         */
        public Builder(int bitSize) {
            checkBitSize(bitSize);
            vectorValues = new int[bitSize/32];
        }

        /**
         * Set a byte of the Vector to build.
         * @param byteIndex : the index of the byte we want to modify.
         * @param value : The value to put in the byte.
         * @return The builder with the setted byte.
         * @throws IllegalStateException if the object has already been builded.
         * @throws IllegalArgumentException if the value is not a 8 bits value.
         * @throws IndexOutOfBoundsException if the byte index do not correspond to the size of the future vector.
         */
        public Builder setByte(int byteIndex, int value) {
            if(vectorValues == null)
                throw new IllegalStateException();
            Preconditions.checkBits8(value);
            Objects.checkIndex(byteIndex, vectorValues.length*32);
            int indexBit = (byteIndex%Integer.BYTES)*Byte.SIZE;
            vectorValues[byteIndex/Integer.BYTES] = setByteOfInt(vectorValues[byteIndex/Integer.BYTES], indexBit, value);
            return this;
        }

        /**
         * Build the BitVector.
         * @return The bitVector builded.
         * @throws IllegalStateException if the object has already been builded.
         */
        public BitVector build() {
            if(vectorValues == null)
                throw new IllegalStateException();
            BitVector toRet = new BitVector(vectorValues);
            vectorValues = null;
            return toRet;
        }
    }
}
