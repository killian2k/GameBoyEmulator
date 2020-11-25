package ch.epfl.gameboj;


/**
 * Interface that contains useful methods to check conditions.
 */
public interface Preconditions{

    /**
     * Checking a condition in argument that must be true
     * @param b the condition to check
     * @throws IllegalArgumentException if the condition b is false
     */
    public static void checkArgument(boolean b) {
        if(!b)
            throw new IllegalArgumentException();
    }

    /**
     * Checking if the value is a 8 bits or less value or not
     * @param v the integer to check if it is a 8bits value 
     * @return v if in the range [0,0xFF]( = 8bit range) or generate an IllegalArgumentException
     * @throws IllegalArgumentException The exception generated when v is not a 8bits value
     */
    public static int checkBits8(int v) {
        checkArgument(v >= 0 && v <=0xFF);
        return v;
    }

    /**
     * Checking if the value is in the defined range or not [0,0xFFFF]
     * @param v the integer to check if it is a 16bits value
     * @return v if in the range [0,0xFFFF]( = 16bit range) or generate an IllegalArgumentException
     * @throws IllegalArgumentException The exception generated when v is not a 8bits value
     */
    public static int checkBits16(int v) {
        checkArgument(v >= 0 && v <=0xFFFF);
        return v;
    }
}
