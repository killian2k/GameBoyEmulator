package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Class containing in majority static methods to do operations on 8 or 16 bits numbers
 * and obtain together the result and the flags corresponding.
 */
public final class Alu {
    private Alu() {}
    private static final int FLAG_H_INDEX = 5;
    private static final int FLAG_C_INDEX = 4;
    private static final int INDEX_VALUE_PACK = 8;
    /**
     * The enumerations of the 4 flags represented as 8 bits values.
     */
    public enum Flag implements Bit {UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z};
    //Masks:
    //UNUSED_0 = 1, UNUSED_1 = 10, UNUSED_2 = 100, UNUSED_3 = 1000
    //C = 10000, H = 100000, N = 1000000, Z = 10000000

    /**
     * The enum of the direction of the rotation.
     */
    public enum RotDir {LEFT, RIGHT};

    /**
     * Get the integer whose bits corresponds to the flags. bit = 1 if flag = true.
     * @param z : Z - Zero flag.
     * @param n : N - Subtraction flag.
     * @param h : H - half Carry flag.
     * @param c : C - Carry flag.
     * @return The mask corresponding of the arguments.
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        int mask = 0;
        if(z) mask += Flag.Z.mask();
        if(n) mask += Flag.N.mask();
        if(h) mask += Flag.H.mask();
        if(c) mask += Flag.C.mask();

        return mask;
    }

    /**
     * Obtain the value from the packed int valueFlags.
     * @param valueFlags: The packed informations : value + flags.
     * @return The given value (without flags).
     * @throws IllegalArgumentException if the valueFlags do 
     * not respect the format [16b Value][v][z][n][h][c][0000].
     */
    public static int unpackValue(int valueFlags) {
        checkPackedValueFlags(valueFlags);
        return Bits.extract(valueFlags,INDEX_VALUE_PACK,16);
    }

    /**
     * Obtain the values about the flags from the integer valueFlags.
     * @param valueFlags: The packed informations : value + flags.
     * @return The given flags (without value) in an integer.
     * @throws IllegalArgumentException if the valueFlags do 
     * not respect the format [16b Value][v][z][n][h][c][0000].
     */
    public static int unpackFlags(int valueFlags) {
        checkPackedValueFlags(valueFlags);
        return get8Lsb(valueFlags);
    }

    /**
     * Get the sum from the two 8 bits l and r and the possible carry c0.
     * @param l : a 8 bit number.
     * @param r : a 8 bit number.
     * @param c0 : the carry True = 1, False = 0.
     * @return the sum of the addition with the flags as a package valueFlag.
     * @throws IllegalArgumentException if one of the arguments r or l is not a 8 bits value. 
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int carry= (c0)?1:0;
        int sum = (l+r+carry);
        int sum8b = get8Lsb(sum);
        boolean c = (sum > 0xFF);
        boolean h = (get4Lsb(l) + get4Lsb(r) + carry) > 0xF;
        return packZNHC(sum8b,(sum8b==0), false, h, c);
    }

    /**
     * Get the sum of the two 8 bits values. The carry is equal to 0.
     * @param l : a 8 bit number.
     * @param r : a 8 bit number.
     * @return the sum of the addition with the flags as a package valueFlag.
     * @throws IllegalArgumentException if one of the arguments r or l is not a 8 bits value.
     */
    public static int add(int l, int r) {
        return add(l,r,false);
    }

    /**
     * Get the sum of the two 16 bits values with the flags of the 8 LSB addition (c, h).
     * @param l : a 16 bit number.
     * @param r : a 16 bit number.
     * @return the sum of the addition with the flags as a package valueFlag (flags values from the low 8 bits additions).
     * @throws IllegalArgumentException if one of the arguments r or l is not a 16 bits value. 
     */
    public static int add16L(int l, int r) {
        return add16HL(l,r,false); 
    }

    /**
     * Get the sum of the two 16 bits values with the flags of the 8 MSB addition (c, h).
     * @param l : a 16 bit number.
     * @param r : a 16 bit number.
     * @return the sum of the addition with the flags as a package valueFlag (flags values from the higher 8 bits additions).
     * @throws IllegalArgumentException if one of the arguments r or l is not a 16 bits.
     */
    public static int add16H(int l, int r) {
        return add16HL(l,r,true);
    }

    /**
     * Method to do the subtraction.
     * @param l : first 8 bits term.
     * @param r : second 8 bits term.
     * @param b0 boolean: true if there's a carry on the left bits false if there is not.
     * @return The difference of l and r and the carry.
     * @throws IllegalArgumentException if one of the arguments r or l is not a 8 bits value. 
     */
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int ca = (b0)?1:0;
        int dif = l-r-ca;
        int dif8b = get8Lsb(dif);
        boolean c = (l-r-ca)<0;
        boolean h = (get4Lsb(l)-get4Lsb(r)-ca)<0;
        return packZNHC(dif8b, (dif8b==0), true, h, c);
    }

    /**
     * Method to do the subtraction without carry.
     * @param l : first 8 bits term.
     * @param r : second 8 bits term.
     * @return The difference of l and r and the carry.
     * @throws IllegalArgumentException if one of the arguments r or l is not a 8 bits value. 
     */
    public static int sub(int l, int r) {
        return sub(l,r,false);
    }

    /**
     * Give the integer with the value in bcd format and the last 8 bits for the flags.
     * @param v : the value in 8 bits to convert to BCD format.
     * @param n : true/false depending on N flag.
     * @param h : true/false depending on H flag.
     * @param c : true/false depending on C flag.
     * @return the v value in bcd format and the mask in the last 8 bits.
     * @throws IllegalArgumentException if v is not a 8 bits value. 
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        Preconditions.checkBits8(v);
        //Algorithm given.
        boolean fixL = h || (!n && get4Lsb(v) > 9);
        boolean fixH = c || (!n && v > 0x99);
        int fix = 0x60 * (fixH?1:0) + 0x6 * (fixL?1:0);
        int va = 0;
        va = n ? sub(v, fix) : add(v, fix);
        va = unpackValue(va);
        return packZNHC(va, (va==0), n, false, fixH);
    }

    /**
     * Get the "AND" operation bit to bit between l and r.
     * @param l : a 8 bit number.
     * @param r : a 8 bit number.
     * @return the result with the flags packed.
     * @throws IllegalArgumentException if one of the arguments r or l is not a 8 bits value. 
     */
    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int res = l & r;
        return packZNHC(res,(res==0), false, true, false);
    }

    /**
     * Get the "OR" operation bit to bit between l and r.
     * @param l : a 8 bit number.
     * @param r : a 8 bit number.
     * @return the result with the flags packed.
     * @throws IllegalArgumentException if one of the arguments r or l is not a 8 bits value .
     */
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int res = l | r;
        return packZNHC(res,(res==0), false, false, false);
    }

    /**
     * Get the "XOR - Exclusive or" operation bit to bit between l and r.
     * @param l : a 8 bit number.
     * @param r : a 8 bit number.
     * @return the result with the flags packed.
     * @throws IllegalArgumentException if one of the arguments r or l is not a 8 bits value. 
     */
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int res = l ^ r;
        return packZNHC(res,(res==0), false, false, false);
    }

    /**
     * get the 8 bits value given in parameter with a left shift. The ejected bit is the value of c-Flag
     * @param v : a 8 bit number which is going to be shifted on the left.
     * @return The new value packed with the flags.
     * @throws IllegalArgumentException if the argument given is not a 8bits value.
     */
    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);
        int res = v << 1;
        int res8b = get8Lsb(res);
        return packZNHC(res8b,(res8b==0), false, false, (v>0b0111_1111));
    }

    /**
     * get the 8 bits value given in parameter with a arithmetic right shift (The new bits depends of the value of the 8th bit).
     * The ejected bit is the value of c-Flag.
     * @param v : a 8 bit number which is going to be shifted on the right.
     * @return The new value packed with the flags.
     * @throws IllegalArgumentException if the argument given is not a 8 bits value.
     */
    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);
        int shiftedRes = get8Lsb(Bits.signExtend8(v) >> 1);
        return packZNHC(shiftedRes, shiftedRes==0, false, false, v%2==1);
    }

    /**
     * get the 8 bits value given in parameter with a logical right shift (the new bits are 0s). The ejected bit is the value of c-Flag returned.
     * @param v : a 8 bit number which is going to be shifted on the right.
     * @return The new value packed with the flags.
     * @throws IllegalArgumentException if the argument given is not a 8 bits value.
     */
    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);
        int shiftedRes = v >>> 1;
        return packZNHC(Bits.clip(8, shiftedRes), shiftedRes==0, false, false, v%2==1);
    }

    /**
     * Do the rotation of the 8 bits value "v" in the direction of "d" with a movement of 1. 
     * The value of C-flag is the value of the bit who is changing side.
     * @param d : The direction of the rotation: Right or Left.
     * @param v : The 8 bits number we want to rotate.
     * @return the packed value with the flags.
     * @throws IllegalArgumentException if the argument v given is not a 8 bits value.
     */
    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);
        return rotate(d, v,(d==RotDir.LEFT? (v>0b0111_1111):(v%2==1)));
    }

    /**
     * Rotation of 9 bits: the 8 bit "v" value and the 9th bit is the value of c.
     * The value of C-Flag is the value of the 9th bit. The rotation is of only one movement and 
     * the direction depends on "d".
     * @param d : The direction of the rotation.
     * @param v : The 8 bits value.
     * @param c : The value of the 9th bit (true = 1, false = 0).
     * @return the packed value with the flags.
     * @throws IllegalArgumentException if the argument v given is not a 8 bits value.
     */
    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);
        int dist = (d == RotDir.RIGHT)?-1:1;
        int bits9Value = get8Lsb(v);
        bits9Value = Bits.set(bits9Value, 8, c);
        bits9Value = Bits.rotate(9, bits9Value, dist);
        int bits8Value = get8Lsb(bits9Value);
        return packZNHC(bits8Value, bits8Value==0, false, false, Bits.test(bits9Value, 8));
    }

    /**
     * Get the value of the inverted 4 left and 4 right packs of bits of the 8 bits "v" value.
     * @param v : The 8 bits value.
     * @return The packed inverted "v" value and the flags.
     * @throws IllegalArgumentException if the argument given is not a 8 bits value.
     */
    public static int swap(int v) {
        Preconditions.checkBits8(v);
        int value = Bits.rotate(8, v, 4);
        return Alu.packZNHC(value, (value==0), false, false, false);
    }

    /**
     * Get the value of a specific bit in a 8bits number.
     * @param v : The 8 bits value where to analyze the bit value.
     * @param bitIndex : The index of the bit we want to analyze (between 0 and 7 included).
     * @return the value 0 and the flags Z010 packed together. Z is the value of the v integer
     *  at the given index.
     *  /!\ Warning, the Z is 0 if the bit is 1 or 1 if the bit is 0 ! (inverted)
     * @throws IllegalArgumentException if one of the arguments v or bitIndex given is not a 8 bits value.
     */
    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        Objects.checkIndex(bitIndex, 8);
        return packZNHC(0, !Bits.test(v, bitIndex), false, true, false);
    }

    /*
     * Get the sum of the two 16 bits values.
     * isH : False if flags from lower bits addition, True if from higher bits addition.
     */
    private static int add16HL(int l, int r, boolean isH) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        int lsb = add(get8Lsb(l), get8Lsb(r));
        boolean carrylsb=isCarry(lsb);
        int msb = add(get8MsbOf16b(l), get8MsbOf16b(r), carrylsb);
        boolean h,c;
        if(isH) {
            h = isHalfCarry(msb);
            c = isCarry(msb);
        } else {
            h = isHalfCarry(lsb);
            c = carrylsb;  
        }
        int ms8b = unpackValue(msb);
        int ls8b = unpackValue(lsb);
        return packZNHC(Bits.make16(ms8b, ls8b), false, false, h, c);
    }

    /**
     * Get the value of the flag H.
     */
    private static boolean isHalfCarry(int valueFlags) {
        return Bits.test(valueFlags, FLAG_H_INDEX);
    }

    /*
     * Get the value of the flag C.
     */
    private static boolean isCarry(int valueFlags) {
        return Bits.test(valueFlags, FLAG_C_INDEX);
    }

    /*
     * Receive the value and the different values of the flags and packed all in an integer returned.
     * v: The value of the packed value. Must be a 16 bits value at most.
     * z,n,h,c: The value of the Z/N/H/C flag.
     * return The packedValue in this format [v][z][n][h][c][0000].
     */
    private static int packZNHC(int v, boolean z, boolean n, boolean h, boolean c) {
        Preconditions.checkBits16(v);
        return (v<<INDEX_VALUE_PACK)+ maskZNHC(z, n, h, c);
    }

    /*
     * Check that the Packed values are correct. This means that some bits must always be 0.
     * These are: the 8 bits on the right side and the 4 bits on the left side.
     */
    private static void checkPackedValueFlags(int packedValue) throws IllegalArgumentException {
        Preconditions.checkArgument(get4Lsb(packedValue) <= 0);
        Preconditions.checkArgument(Bits.extract(packedValue, 24, 8) <= 0);
    }

    private static int get8MsbOf16b(int v) {
        return Bits.extract(v, 8, 8);
    }

    private static int get8Lsb(int v) {
        return Bits.clip(8, v);
    }

    private static int get4Lsb(int v) {
        return Bits.clip(4,v);
    }
}