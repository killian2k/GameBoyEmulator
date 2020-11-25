package ch.epfl.gameboj.component.lcd;

import java.util.Objects;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * Immutable class representing an image line of the Gameboy.
 */
public final class LcdImageLine {
    private final static int UNCHANGED_COLORS_MAP_VALUE = 0b11_10_01_00;

    private final BitVector msb;
    private final BitVector lsb;
    private final BitVector opacity;

    /**
     * Constructor of the class. The object created is immutable.
     * @param msb : BitVector representing the MSB bits of the line.
     * @param lsb : BitVector representing the LSB bits of the line
     * @param opacity : BitVector for the opacity. Each bit is the opacity of the corresponding pixel.
     * @throws IllegalArgumentException if the size is not the same for the three vectors.
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(msb.size() == lsb.size() && msb.size() == opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    /**
     * Get the size in pixel of the line.
     * @return the pixel size.
     */
    public int size() {
        return msb.size();
    }

    /**
     * Get the MSB BitVector.
     * @return the BitVector corresponding.
     */
    public BitVector msb() {
        return msb;
    }

    /**
     * Get the LSB BitVector.
     * @return the BitVector corresponding.
     */
    public BitVector lsb() {
        return lsb;
    }

    /**
     * Get the opacity BitVector.
     * @return the BitVector corresponding.
     */
    public BitVector opacity() {
        return opacity;
    }

    /**
     * shifting the line of a given distance.
     * @param distance : the distance of the shift operation (in pixel).
     * @return the new lcdImageLine Object with the operation done. 
     */
    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), lsb.shift(distance), opacity.shift(distance));
    }

    /**
     * Extract with the infinite wrapped method a Line depending on size and on the index.
     * @param index : the index where to start the extraction (included). 
     * @param size : The size of the new Line.
     * @return the new lcdImageLine Object with the operation done.
     * @throws IllegalArgumentException if size is not a positive multiple of 32.
     */
    public LcdImageLine extractWrapped(int index, int size) {
        return new LcdImageLine(msb.extractWrapped(index, size), lsb.extractWrapped(index, size), opacity.extractWrapped(index, size));
    }

    /**
     * Change the LcdImageLine using the map given in argument.
     * @param map is the map of correspondence.
     * The argument is used like this: map = 0bab_cd_ef_gh 
     * The color's transformation: 11->ab, 10->cd, 01->ef, 00->gh
     * @return The new LcdImageLine with the transformation of colors
     * @throws IllegalArgumentException if the map is not a 8 bits value ([0:255]).
     */
    public LcdImageLine mapColors(int map) {
        if(map == UNCHANGED_COLORS_MAP_VALUE)
            return new LcdImageLine(msb, lsb, opacity);

        Preconditions.checkBits8(map);
        //c[0] = 00, c[1] = 01, c[2] = 10, c[3] = 11
        BitVector[] c  = new BitVector[]{msb.not().and(lsb.not()), msb.not().and(lsb), msb.and(lsb.not()), msb.and(lsb)};
        BitVector newLsb = new BitVector(size(), false);
        BitVector newMsb = new BitVector(size(), false);
        for(int bitIndex = 0;bitIndex< Byte.SIZE;++bitIndex) {
            if(Bits.test(map, bitIndex)) {
                if((bitIndex&1) == 0)
                    newLsb = newLsb.or(c[bitIndex/2]);
                else 
                    newMsb = newMsb.or(c[bitIndex/2]);
            }    			
        }

        return new LcdImageLine(newMsb, newLsb, opacity);
    }

    /**
     * Compose a new line using this one as the under one and the one given in argument as the upper one.
     * The opaque bits of upper will be displayed and these are transparent, the bits
     * of the line of this instance are displayed. 
     * @param lineUpper is the upper line. 
     * @return The result of the operation.
     * @throws IllegalArgumentException if the lineUpper is not the same size.
     */
    public LcdImageLine below(LcdImageLine lineUpper) {
        return below(lineUpper, lineUpper.opacity);
    }

    /**
     * Compose a new line using this one as the under one and the one given in argument as the upper one.
     * The opaque bits defined by opacity of upper will be displayed and these are transparent, the bits
     * of the line of this instance are displayed. 
     * @param lineUpper is the upper line. 
     * @param opacity is the opacity of the upper line.
     * @return The result of the operation.
     * @throws IllegalArgumentException if the lineUpper is not the same size.
     */
    public LcdImageLine below(LcdImageLine lineUpper, BitVector opacity) {
        Preconditions.checkArgument(size() == lineUpper.size());
        // Opacity: bit 0 is transparent
        BitVector newMsb = lineUpper.msb.and(opacity).or(msb.and(opacity.not().and(this.opacity)));
        BitVector newLsb = lineUpper.lsb.and(opacity).or(lsb.and(opacity.not().and(this.opacity)));
        return new LcdImageLine(newMsb, newLsb, opacity.or(this.opacity));
    }

    /**
     * Join this line with the line given at a specific index. 
     * The result will be: the index firsts bits from this and the size-index bits from l2.
     * if this = abcd and l2 = efgh and index=2. The result will be efcd.
     * @param l2 is the line to use for the operation.
     * @param index is the index of the bit where there is the join between the two lines.
     * @return The new line with the join operation done.
     * @throws IllegalArgumentException if the line given in argument is not the same size.
     * @throws IndexOutOfBoundsException if the index of the join is not valid (must be smaller than size()).
     */
    public LcdImageLine join(LcdImageLine l2, int index) {
        Preconditions.checkArgument(l2.size() == size());
        Objects.checkIndex(index, size());
        BitVector mask = new BitVector(size(), true).shift(size() - index);
        BitVector newMsb = msb.and(mask).or(l2.msb.and(mask.not()));
        BitVector newLsb = lsb.and(mask).or(l2.lsb.and(mask.not()));
        return new LcdImageLine(newMsb, newLsb, new BitVector(size(), true));
    }

    /**
     * Get the pixel color of a given pixel index
     * @param pixelIndex is the pixel's index in the line.
     * @return the color integer with 2 bits used.
     * @throws IllegalArgumentException if the index is not positive.
     */
    public int getColor(int pixelIndex) {
        if(!opacity.testBit(pixelIndex))
            return 0;

        int color= 0;
        if(msb.testBit(pixelIndex))
            color += 0b10;
        if(lsb.testBit(pixelIndex))
            color += 0b1;
        return color;
    }

    /**
     * Create a new LcdImageLine with only 0 for msb, lsb and opacity vectors.
     * @param bitSize is the size of the line.
     * @return the object created.
     * @throws IllegalArgumentException if the bitSize is not positive or not a multiple of 32.
     */
    public static LcdImageLine newEmptyLine(int bitSize) {
        return new LcdImageLine(new BitVector(bitSize), new BitVector(bitSize), new BitVector(bitSize));
    }

    /**
     * {@link java.lang.Object#equals(Object)}
     */
    public boolean equals(Object that) {
        if(!(that instanceof LcdImageLine))
            return false;
        LcdImageLine l2 = (LcdImageLine)that;
        return msb.equals(l2.msb) && lsb.equals(l2.lsb) && opacity.equals(l2.opacity);
    }

    /**
     * {@link java.lang.Object#toString()}
     */
    public String toString() {
        return "msb:" + msb.toString() + "\nlsb:" + lsb.toString() + "\nopa:" + opacity.toString(); 
    }

    /**
     * {@link java.lang.Object#hashCode()}
     */
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }

    /**
     * Intern class to have a builder of LcdImageLine.
     */
    public static final class Builder {
        private BitVector msb;
        private BitVector lsb;

        /**
         * Constructor of the builder.
         * @param size is the size of the future line.
         * @throws IllegalArgumentException if the bitSize is not positive or not a multiple of 32.         *  
         */
        public Builder(int size) {
            lsb = new BitVector(size);
            msb = new BitVector(size);
        }

        /**
         * Set the given byte of pixels of the line. One argument for the MSB bits of the pixels and one for the LSB bits.
         * @param byteIndex is the index of the byte to set.
         * @param newMsb is the msb byte to set at the given position.
         * @param newLsb is the lsb byte to set at the given position.
         * @return The builder itself.
         * @throws IllegalStateException if the line has already been built.
         * @throws IllegalArgumentException if newMsb or newLsb is not 8 bits value.
         * @throws IndexOutOfBoundsException if the index of the byte is not valid (byteIndex*8 must be smaller than size() of the lines). 
         */
        public Builder setBytes(int byteIndex, int newMsb, int newLsb) {
            if(msb == null)
                throw new IllegalStateException();
            Preconditions.checkBits8(newMsb);
            Preconditions.checkBits8(newLsb);
            Objects.checkIndex(byteIndex*Byte.SIZE, msb.size());
            msb = msb.setByte(byteIndex, newMsb);
            lsb = lsb.setByte(byteIndex, newLsb);
            return this;
        }

        /**
         * Build the LcdImageLine using this constructor.
         * @return the new LcdImageLine.
         * @throws IllegalStateException if the line has already been built.
         */
        public LcdImageLine build() {
            if(msb == null)
                throw new IllegalStateException();
            BitVector opacity = msb.not().and(lsb.not()).not();
            LcdImageLine l = new LcdImageLine(msb, lsb, opacity);
            msb = null;
            return l;
        }
    }
}
