package ch.epfl.gameboj.bits;

/**
 * Interface only for the Enum who represents set of bit
 */
public interface Bit {
    /**
     * {@link java.lang.Enum#ordinal()}
     */
    abstract int ordinal();

    /**
     * {@link java.lang.Enum#ordinal()}
     */
    default int index(){
        return ordinal();
    }

    /**
     * Method returning the bit mask of the bit represented by this instance.
     * @return the Binary mask (0b10000, f.e). Only the bit of the same index equal 1
     */
    default int mask(){
        return Bits.mask(index());
    }    
}