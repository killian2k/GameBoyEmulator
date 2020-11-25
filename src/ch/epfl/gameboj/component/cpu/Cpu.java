package ch.epfl.gameboj.component.cpu;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.cpu.Opcode.Kind;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * Class representing the CPU of the Gameboy. A part of his job is to dispatch the instructions and manage the interruptions.
 */
public final class Cpu implements Component, Clocked{
    // Private members
    private final RegisterFile<Register> reg8 = new RegisterFile<>(Reg.values());
    private final Ram highRam = new Ram(AddressMap.HIGH_RAM_SIZE);
    private Bus bus;
    private boolean regIME = false;
    private int regPC = 0, regSP = 0, regIE = 0, regIF = 0;
    private long nextNonIdleCycle = 0;

    // Constants
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.PREFIXED);
    private static final FlagSrc V0 = FlagSrc.V0;
    private static final FlagSrc V1 = FlagSrc.V1;
    private static final FlagSrc ALU = FlagSrc.ALU;
    private static final FlagSrc CPU = FlagSrc.CPU;
    private static final int PREFIX_OPCODE_VALUE = 0xCB;
    private static final int ADDRESSE_EXCEPTION = 0x10000;
    private static final int LAST_ADDRESS = 0xFFFF;
    private static final int CYCLES_TO_SKIP_INTERUP = 5;
    private static final int BITS_BY_MEMORY_ADDRESS = 8;
    private static final int NUMBER_OF_OPCODE_BY_FAMILY = 256;
    private static final int MASK_4_MSB_8_BITS_NUMBER = 0xF0;
    private static final int LENGTH_SIZE_REG16 = 2;
    private static final int LENGTH_REG8_OPCODE = 3;
    private static final int LENGTH_N3_OPCODE = 3;

    // Enums of the register and of flags
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }
    private enum Reg16 implements Register {
        BC, DE, HL, AF
    }
    private enum FlagSrc {
        V0, V1, ALU, CPU
    }

    // Public members
    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
    }


    //Public methods
    @Override
    public void cycle(long cycle) {
        if(nextNonIdleCycle == Long.MAX_VALUE && regIF != 0 && regIE != 0)
            nextNonIdleCycle = cycle;

        if(nextNonIdleCycle == cycle)    
            reallyCycle();
    }


    //Give access to the high memory and to the register IE and IF only.    
    @Override
    public int read(int address) throws IllegalArgumentException {
        Preconditions.checkBits16(address);
        if(address >= AddressMap.HIGH_RAM_START && address < AddressMap.HIGH_RAM_END)
            return highRam.read(address-AddressMap.HIGH_RAM_START);
        if(address == AddressMap.REG_IE)
            return regIE;
        if(address == AddressMap.REG_IF)
            return regIF;

        return NO_DATA;
    }

    @Override
    public void write(int address, int data) throws IllegalArgumentException {
        Preconditions.checkBits16(address);        
        Preconditions.checkBits8(data);
        if(address == AddressMap.REG_IE)
            regIE = data;
        if(address == AddressMap.REG_IF)
            regIF = data;
        if(address >= AddressMap.HIGH_RAM_START && address < AddressMap.HIGH_RAM_END)
            highRam.write(address-AddressMap.HIGH_RAM_START, data);
    }

    /**
     * Get an array with value in order : {PC, SP, A, F, B, C, D, E, H, L}.
     * @return the integer array with the corresponding values.
     */
    public int[] _testGetPcSpAFBCDEHL() {
        return new int[] {regPC, regSP, getRegA(), reg8.get(Reg.F), reg8.get(Reg.B), reg8.get(Reg.C),
                reg8.get(Reg.D), reg8.get(Reg.E), reg8.get(Reg.H), reg8.get(Reg.L)};
    }

    @Override
    public void attachTo(Bus bus){
        this.bus = bus;
        Component.super.attachTo(bus);
    }

    /**
     * Raise the given interruption i = put to 1 the corresponding bit
     * @param i : the interruption to raise
     */
    public void requestInterrupt(Interrupt i) {
        regIF = Bits.set(regIF, i.index(), true);

    }
    
    
    
    public void writeRamIntofile(File f) throws IOException {
        highRam.writeRamIntoFile(f);
    }
    
    


    //    PRIVATE

    /*
     * Read the 8 bits value from the bus in the given address
     * address : The 16-bits address of the data we want.
     */
    private int read8(int address) {
        Preconditions.checkBits16(address);
        return get8Lsb(bus.read(address));
    }

    /*
     * Get the 8bits value from the address stored in HL register pair
     */
    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }

    /*
     * Read 8bits value from bus from the address following the one in regPC 
     */
    private int read8AfterOpcode() {
        if(regPC == LAST_ADDRESS)
            bus.read(ADDRESSE_EXCEPTION);// We want an exception
        return read8(get16Lsb(regPC + 1));
    }

    /*
     * Read the 16 bits value from the bus in the given address
     * address is the 16-bits address of the data we want.
     */
    private int read16(int address) {
        Preconditions.checkBits16(address);
        if(address == LAST_ADDRESS)
            bus.read(ADDRESSE_EXCEPTION);
        return Bits.make16(read8(address+1), read8(address));
    }

    /*
     * Read 16 bits value from bus from the address following the one in regPC
     */
    private int read16AfterOpcode() {
        return read16(regPC+1);
    }

    /*
     * Write on the bus at the given address the 8 bits value "v"
     * address : the 16 bits address on the bus where to store the value
     * v : the value 8 bits we want to store 
     */
    private void write8(int address, int v) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(v);
        bus.write(address, v);
    }

    /*
     * Write on the bus at the given address the 16 bits value "v"
     * address : the address on the bus where to store the value
     * v : the value 16 bits we want to store 
     */
    private void write16(int address, int v) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits16(v);
        Preconditions.checkArgument(address < LAST_ADDRESS);
        //Little Endian in memory 
        bus.write(address+1, (v>>BITS_BY_MEMORY_ADDRESS));
        bus.write(address, get8Lsb(v));
    }

    /*
     * Write on the bus at the address stored in HL register the 8 bits given value
     */
    private void write8AtHl(int v) {
        write8(reg16(Reg16.HL),v);
    }

    /*
     * Push a 16bits value in the Stack Pile
     */
    private void push16(int v) {
        regSP-=2;
        Preconditions.checkArgument(regSP != LAST_ADDRESS);
        regSP = get16Lsb(regSP);
        write16(regSP, v);
    }

    /*
     * Pop a 16 bits value from the Stack Pile
     */
    private int pop16() {
        int value = regSP;
        regSP=get16Lsb(regSP+2);
        return read16(value);
    }

    /*
     * Modify the value in the pair of register given in param.
     * r is the 16 bits register to set
     * newV is the 16 bits value to store in the register
     */
    private void setReg16(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);

        int hBits = get8MsbOf16b(newV);
        int lBits = get8Lsb(newV);
        Reg regH = Reg.H, regL = Reg.L;
        switch(r) {
        case HL:
            break;
        case BC:
            regH = Reg.B;
            regL = Reg.C;
            break;
        case DE:
            regH = Reg.D;
            regL = Reg.E;
            break;
        case AF:
            regH = Reg.A;
            regL = Reg.F;
            lBits &= (MASK_4_MSB_8_BITS_NUMBER);
            break;
        default: Preconditions.checkArgument(false);
        }

        reg8.set(regH, hBits);
        reg8.set(regL, lBits);
    }

    /*
     * Do the same as setReg16() except if r == AF then is set the value to regSP only
     * r is the register to set
     * newV is the new 16 bits value to set 
     */
    private void setReg16SP(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        if(r == Reg16.AF) {
            regSP = newV;
            return;
        }
        setReg16(r, newV);
    }

    /*
     * Extract and return the identity of the 8 bits register of the given opcode starting from the bit in the given index
     * opcode : The Opcode where we get identity
     * startBit : The index of the bit where we want to start
     */
    private Reg extractReg(Opcode opcode, int startBit) {
        Preconditions.checkArgument(startBit >= 0 && startBit <= 8-LENGTH_REG8_OPCODE);

        switch(Bits.extract(opcode.encoding, startBit, LENGTH_REG8_OPCODE)){
        default:
            Preconditions.checkArgument(false);
        case 0b000:
            return Reg.B;
        case 0b001:
            return Reg.C;
        case 0b010:
            return Reg.D;
        case 0b011:
            return Reg.E;
        case 0b100:
            return Reg.H;
        case 0b101:
            return Reg.L;
        case 0b111:
            return Reg.A;
        }
    }

    private Reg extractRegBit0(Opcode opcode) {
        return extractReg(opcode, 0);
    }

    private Reg extractRegBit3(Opcode opcode) {
        return extractReg(opcode, 3);
    }

    /*
     * Extract and return the identity of the 16 bits pair of register
     * of the given opcode starting from the bit in the index 4
     * opcode is the Opcode where we get identity
     */
    private Reg16 extractReg16(Opcode opcode) {
        return Reg16.values()[Bits.extract(opcode.encoding, 4, LENGTH_SIZE_REG16)];
    }

    /*
     * Return -1 (bit=1) or 1 (bit=0) whether the value of the bit at index 4 from the encoding of the given opcode 
     * opcode is the opcode to analyze. 
     * +/-1 (used for increment/decrement)
     */
    private int extractHlIncrement(Opcode opcode) {
        return (Bits.test(opcode.encoding, 4))?-1:1;
    }

    /*
     * Extract the value from the pack value/flags "vf" and store it in the register given
     * r is the register where store the value
     * vf is The pack value/flags where the value is extract 
     */
    private void setRegFromAlu(Reg r, int vf) {
        reg8.set(r, Alu.unpackValue(vf));
    }

    /*
     * Combine the flags from reg F with the one in VF given each of the parameter z,n,h,c.
     * The values for these last parameters can be: V0-> flag = 0, V1-> flag = 1, ALU-> flag from vf, CPU-> flag from Reg.F
     * vf is The pack value/flags
     * z, n, h c are the choices of the flags
     */
    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        int vectV1 = Alu.maskZNHC(z == V1, n == V1, h == V1, c == V1);
        int vectAlu = Alu.maskZNHC(z == ALU, n == ALU, h == ALU, c == ALU);
        int vectCpu = Alu.maskZNHC(z == CPU, n == CPU, h == CPU, c == CPU);
        int combine1 = vf&vectAlu;
        int combine2 = reg8.get(Reg.F)&vectCpu;
        reg8.set(Reg.F, get8Lsb(combine1|combine2|vectV1));
    }

    /*
     * give the direction (1 = Right, 0 = left) of the rotation for every instruction using a rotation
     */
    private RotDir getDirectionRot(Opcode opcode) {
        final Opcode.Family[] familyRot = new Opcode.Family[]{Opcode.Family.ROT_HLR,Opcode.Family.ROT_R8,Opcode.Family.ROTA, Opcode.Family.ROTC_HLR, Opcode.Family.ROTC_HLR,Opcode.Family.ROTC_R8,Opcode.Family.ROTCA};
        Preconditions.checkArgument(Arrays.asList(familyRot).contains(opcode.family));
        return (Bits.test(opcode.encoding, 3))?RotDir.RIGHT:RotDir.LEFT;
    }

    /*
     * Give the index of the bit to test or modify using the instructions BIT, RES or SET
     * opcode : The opcode of the instruction where we extract the index
     */
    private int getN3FromOpcode(Opcode opcode) {
        final Opcode.Family[] familySetResBit = new Opcode.Family[]{Opcode.Family.BIT_U3_HLR,Opcode.Family.BIT_U3_R8, Opcode.Family.CHG_U3_HLR, Opcode.Family.CHG_U3_R8, Opcode.Family.RST_U3};
        Preconditions.checkArgument(Arrays.asList(familySetResBit).contains(opcode.family));
        return Bits.extract(opcode.encoding, 3, LENGTH_N3_OPCODE);      
    }

    /*
     * Return the value of the bit 6 depending on the instruction: iff this is a RES instruction bit6=0
     *  iff this is a SET bit6=1
     */
    private boolean getValueBit6SetRes(Opcode opcode) {
        final Opcode.Family[] familySetResBit = new Opcode.Family[]{Opcode.Family.CHG_U3_HLR, Opcode.Family.CHG_U3_R8};
        Preconditions.checkArgument(Arrays.asList(familySetResBit).contains(opcode.family));
        return Bits.test(opcode.encoding, 6);
    }

    /*
     * Combine the flag C and the bit 3 from the opcode using AND or NAND logical operation depending the Opcode
     * opcode : The opcode where extract bit 3 (must be a ADD, SUB or SCCF instr)
     * isAddOrSub : True if opcode argument is a Add or Sub instruction (AND op) 
     *  otherwise it is a SCCF instruction (NAND op)
     */
    private boolean combineFlagCBit3(Opcode opcode, boolean isAddOrSub) {
        if(isAddOrSub) {
            final Opcode.Family[] familyAddSub = new Opcode.Family[]{Opcode.Family.ADD_A_N8, Opcode.Family.ADD_A_R8, Opcode.Family.ADD_A_HLR,Opcode.Family.SUB_A_R8, Opcode.Family.SUB_A_N8, Opcode.Family.SUB_A_HLR};
            Preconditions.checkArgument(Arrays.asList(familyAddSub).contains(opcode.family));
        }
        else
            Preconditions.checkArgument(opcode.family == Opcode.Family.SCCF);

        // SCF - bit3 = 0 --> return true
        // CCF - bit3 = 1 --> return the inverse of F
        boolean resAnd = Bits.test(opcode.encoding, 3) & getCarryValue();
        return (isAddOrSub)?resAnd:!resAnd;
    }

    private int getRegA() {
        return reg8.get(Reg.A);
    }

    private void setRegA(int value) {
        Preconditions.checkBits8(value);
        reg8.set(Reg.A, value);
    }

    private void setRegAFromAlu(int value) {
        setRegA(Alu.unpackValue(value));
    }

    private boolean getFlagValue(Alu.Flag flag) {
        return Bits.test(reg8.get(Reg.F),flag.index());
    }

    private boolean getCarryValue() {
        return getFlagValue(Alu.Flag.C);
    }

    private boolean getValueBit3Opcode(Opcode opcode) {
        return Bits.test(opcode.encoding, 3);
    }

    private boolean getValueBit4Opcode(Opcode opcode) {
        return Bits.test(opcode.encoding, 4);
    }

    /*
     * Build an Opcode Table depending on the selected kind. 
     * To get a specific element of the array, you have to use as index the encoding of the opcode.
     */
    private static Opcode[] buildOpcodeTable(Kind kind) {
        Opcode[] opcodes = new Opcode[NUMBER_OF_OPCODE_BY_FAMILY];
        for (Opcode o: Opcode.values()) {
            if(o.kind == kind)
                opcodes[o.encoding] = o;
        }

        return opcodes;
    }

    // Get the value from a 16 bits register
    private int reg16(Reg16 reg16) {
        Reg regHigh = null, regLow = null;
        switch (reg16) {
        case AF:
            regHigh = Reg.A;
            regLow = Reg.F;
            break;
        case BC:
            regHigh = Reg.B;
            regLow = Reg.C;
            break;
        case DE:
            regHigh = Reg.D;
            regLow = Reg.E;
            break;
        case HL:
            regHigh = Reg.H;
            regLow = Reg.L;
            break;
        default:
            Preconditions.checkArgument(false);
        }

        return Bits.make16(reg8.get(regHigh), reg8.get(regLow));
    }


    //Get the value from a 16 bits register but reg16 is AF return value of SP
    private int reg16SP(Reg16 reg16) {
        return (reg16 == Reg16.AF)?regSP:reg16(reg16);
    }

    private static int get8MsbOf16b(int v) {
        return Bits.extract(v, 8, 8);
    }

    private static int get8Lsb(int v) {
        return Bits.clip(8, v);
    }

    private static int get16Lsb(int v) {
        return Bits.clip(16, v);
    }

    /*
     * Check the condition of the opcode and return the value of the condition.
     * Adapt the number of cycles because if true we have to add additional cycles.
     */
    private boolean testConditionOpcode(Opcode opcode) {
        boolean cond = false;
        switch(Bits.extract(opcode.encoding, 3, 2)) {
        case 0b00: // NZ
            cond = !getFlagValue(Flag.Z);
            break;            
        case 0b01: // Z
            cond = getFlagValue(Flag.Z);
            break;
        case 0b10: // NC
            cond = !getCarryValue();
            break;
        case 0b11: // C
            cond = getCarryValue();
            break;
        default:
            Preconditions.checkArgument(false);
        }
        if(cond)
            nextNonIdleCycle+=opcode.additionalCycles;
        return cond;
    }

    //Get the e8 number = 8 bits signed number in memory
    private int getE8() {
        return Bits.signExtend8(read8AfterOpcode());
    }

    /**
     * Manage the interruptions if necessary otherwise continue the program as usual with the next instruction using dispatch()
     */
    private void reallyCycle() {
        // Analyze if a interruption to manage
        int errors = Bits.clip(5, (regIE & regIF));
        if(regIME && errors != 0) {
            // to avoid to be interrupted.  
            regIME = false; 
            int errorToTreat = Integer.lowestOneBit(errors);
            int inError = Integer.numberOfTrailingZeros(errorToTreat);
            regIF = Bits.set(regIF, inError, false);
//            System.out.println("At cycle : " + nextNonIdleCycle + ", handling interrupt " + Interrupt.values()[inError]);

            push16(regPC);
            regPC = AddressMap.INTERRUPTS[inError];
            nextNonIdleCycle+=CYCLES_TO_SKIP_INTERUP;
            return;
        }
        

        int nextInstr = read8(regPC);
        dispatch(nextInstr);
    }

    // Method that dispatch the operations to do depending the opcode
    private void dispatch(int byteOpcode) {       
        Preconditions.checkBits8(byteOpcode);
        Opcode opcode;
        if(byteOpcode == PREFIX_OPCODE_VALUE) {
            byteOpcode = read8AfterOpcode();
            opcode = PREFIXED_OPCODE_TABLE[byteOpcode];
        }
        else
            opcode = DIRECT_OPCODE_TABLE[byteOpcode];
        int nextRegPC = regPC + opcode.totalBytes;
        nextNonIdleCycle += opcode.cycles;

        switch(opcode.family) {
        case NOP: {            
        } break;
        case LD_R8_HLR: {
            reg8.set(extractRegBit3(opcode),read8AtHl());
        } break;
        case LD_A_HLRU: {
            reg8.set(Reg.A,read8AtHl());
            int newV = reg16(Reg16.HL) + extractHlIncrement(opcode);
            setReg16(Reg16.HL, get16Lsb(newV));
        } break;
        case LD_A_N8R: {
            int n8 = read8AfterOpcode();
            setRegA(read8(AddressMap.REGS_START + n8));
        } break;
        case LD_A_CR: {
            setRegA(read8(AddressMap.REGS_START + reg8.get(Reg.C)));
        } break;
        case LD_A_N16R: { 
            int adrN16 = read16AfterOpcode();
            setRegA(read8(adrN16));
        } break;
        case LD_A_BCR: {
            setRegA(read8(reg16(Reg16.BC)));
        } break;
        case LD_A_DER: {
            setRegA(read8(reg16(Reg16.DE)));
        } break;
        case LD_R8_N8: {
            reg8.set(extractRegBit3(opcode), read8AfterOpcode());
        } break;
        case LD_R16SP_N16: {
            Reg16 r = extractReg16(opcode);
            int n16 = read16AfterOpcode();
            setReg16SP(r,n16);
        } break;
        case POP_R16: {
            setReg16(extractReg16(opcode),pop16());
        } break;
        case LD_HLR_R8: {
            Reg r = extractRegBit0(opcode);
            write8AtHl(reg8.get(r));
        } break;
        case LD_HLRU_A: {
            write8AtHl(getRegA());
            setReg16(Reg16.HL, get16Lsb((reg16(Reg16.HL) + extractHlIncrement(opcode))));
        } break;
        case LD_N8R_A: {
            int addr = read8AfterOpcode();
            write8(AddressMap.REGS_START + addr, getRegA());
        } break;
        case LD_CR_A: {
            write8((AddressMap.REGS_START + reg8.get(Reg.C)), getRegA());
        } break;
        case LD_N16R_A: {
            int n16Adr = read16AfterOpcode();
            write8(n16Adr, getRegA());
        } break;
        case LD_BCR_A: {
            write8(reg16(Reg16.BC), getRegA());
        } break;
        case LD_DER_A: {
            write8(reg16(Reg16.DE), getRegA());
        } break;
        case LD_HLR_N8: {
            write8AtHl(read8AfterOpcode());
        } break;
        case LD_N16R_SP: {
            int n16 = read16AfterOpcode();
            write16(n16, regSP);
        } break;
        case PUSH_R16: {
            push16(reg16(extractReg16(opcode)));
        } break;
        case LD_R8_R8: {
            Reg regS = extractRegBit0(opcode);
            Reg regR = extractRegBit3(opcode);
            if(regS != regR)
                reg8.set(regR, reg8.get(regS));
        } break;
        case LD_SP_HL: {
            regSP = this.reg16(Reg16.HL);
        } break;
        case ADD_A_N8: {
            int op = Alu.add(getRegA(), read8AfterOpcode(),combineFlagCBit3(opcode,true));
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,ALU,ALU);
        } break;
        case ADD_A_R8: {
            Reg r = extractRegBit0(opcode);
            int op = Alu.add(getRegA(), reg8.get(r),combineFlagCBit3(opcode,true));
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,ALU,ALU);
        } break;
        case ADD_A_HLR: {
            int op = Alu.add(getRegA(), read8AtHl(),combineFlagCBit3(opcode,true));
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,ALU,ALU);
        } break;
        case INC_R8: {
            Reg r = extractRegBit3(opcode);
            int op = Alu.add(reg8.get(r), 1);
            setRegFromAlu(r,op);
            combineAluFlags(op,ALU,V0,ALU,CPU);
        } break;
        case INC_HLR: {
            int op = Alu.add(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(op));
            combineAluFlags(op,ALU,V0,ALU,CPU);
        } break;
        case INC_R16SP: {
            Reg16 r = extractReg16(opcode);
            int op = Alu.add16H(reg16SP(r), 1);
            setReg16SP(r, Alu.unpackValue(op));
            //The flags do not change
        } break;
        case ADD_HL_R16SP: {
            Reg16 r = extractReg16(opcode);
            int op = Alu.add16H(reg16(Reg16.HL), reg16SP(r));   
            setReg16(Reg16.HL, Alu.unpackValue(op));
            combineAluFlags(op,CPU,V0,ALU,ALU);
        } break;
        case LD_HLSP_S8: {
            int op = Alu.add16L(reg16SP(Reg16.AF), get16Lsb(getE8()));
            Reg16 rToStore = (getValueBit4Opcode(opcode))?Reg16.HL:Reg16.AF;
            setReg16SP(rToStore, Alu.unpackValue(op));
            combineAluFlags(op,V0,V0,ALU,ALU);
        } break;
        case SUB_A_R8: {
            Reg r = extractRegBit0(opcode);
            int op = Alu.sub(getRegA(), reg8.get(r),combineFlagCBit3(opcode,true));
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V1,ALU,ALU);
        } break;
        case SUB_A_N8: {
            int op = Alu.sub(getRegA(), read8AfterOpcode(),combineFlagCBit3(opcode,true));
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V1,ALU,ALU);
        } break;
        case SUB_A_HLR: {
            int op = Alu.sub(getRegA(), read8AtHl(),combineFlagCBit3(opcode,true));
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V1,ALU,ALU);
        } break;
        case DEC_R8: {
            Reg r = extractRegBit3(opcode);
            int op = Alu.sub(reg8.get(r), 1);
            setRegFromAlu(r,op);
            combineAluFlags(op,ALU,V1,ALU,CPU);
        } break;
        case DEC_HLR: {
            int op = Alu.sub(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(op));
            combineAluFlags(op,ALU,V1,ALU,CPU);
        } break;
        case CP_A_R8: {
            Reg r = extractRegBit0(opcode);
            int op = Alu.sub(getRegA(), reg8.get(r)); 
            combineAluFlags(op,ALU,V1,ALU,ALU);
        } break;
        case CP_A_N8: {
            int op = Alu.sub(getRegA(), read8AfterOpcode());
            combineAluFlags(op,ALU,V1,ALU,ALU);
        } break;
        case CP_A_HLR: {
            int op = Alu.sub(getRegA(), read8AtHl());
            combineAluFlags(op,ALU,V1,ALU,ALU);
        } break;
        case DEC_R16SP: {
            Reg16 r = extractReg16(opcode);
            int op = get16Lsb(reg16SP(r)-1); 
            setReg16SP(r, op); 
            //The flags do not change
        } break;
        case AND_A_N8: {
            int op = Alu.and(getRegA(), read8AfterOpcode());
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,V1,V0);
        } break;
        case AND_A_R8: {
            Reg r = extractRegBit0(opcode);
            int op = Alu.and(getRegA(), reg8.get(r));
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,V1,V0);
        } break;
        case AND_A_HLR: {
            int op = Alu.and(getRegA(), read8AtHl());
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,V1,V0);
        } break;
        case OR_A_R8: {
            Reg r = extractRegBit0(opcode);
            int op = Alu.or(getRegA(), reg8.get(r));
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,V0,V0);
        } break;
        case OR_A_N8: {
            int op = Alu.or(getRegA(), read8AfterOpcode());
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,V0,V0);
        } break;
        case OR_A_HLR: {
            int op = Alu.or(getRegA(), read8AtHl());
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,V0,V0);
        } break;
        case XOR_A_R8: {
            Reg r = extractRegBit0(opcode);
            int op = Alu.xor(getRegA(), reg8.get(r));
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,V0,V0);
        } break;
        case XOR_A_N8: {
            int op = Alu.xor(getRegA(), read8AfterOpcode());
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,V0,V0);
        } break;
        case XOR_A_HLR: {
            int op = Alu.xor(getRegA(), read8AtHl());
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,V0,V0,V0);
        } break;
        case CPL: {
            reg8.set(Reg.A,Bits.complement8(getRegA()));
            combineAluFlags(0,CPU,V1,V1,CPU);
        } break;
        case ROTCA: {
            Alu.RotDir r = getDirectionRot(opcode);
            int op = Alu.rotate(r, getRegA()); 
            setRegAFromAlu(op);
            combineAluFlags(op,V0,V0,V0,ALU);
        } break;
        case ROTA: {
            Alu.RotDir r = getDirectionRot(opcode);
            int op = Alu.rotate(r, getRegA(), getCarryValue());
            setRegAFromAlu(op);
            combineAluFlags(op,V0,V0,V0,ALU);
        } break;
        case ROTC_R8: {
            Alu.RotDir r = getDirectionRot(opcode);
            Reg reg = extractRegBit0(opcode);
            int op = Alu.rotate(r, reg8.get(reg)); 
            setRegFromAlu(reg,op);
            combineAluFlags(op,ALU,V0,V0,ALU);
        } break;
        case ROT_R8: {
            Alu.RotDir r = getDirectionRot(opcode);
            Reg reg = extractRegBit0(opcode);
            int op = Alu.rotate(r, reg8.get(reg), getCarryValue()); 
            reg8.set(reg, Alu.unpackValue(op));
            combineAluFlags(op,ALU,V0,V0,ALU);
        } break;
        case ROTC_HLR: {
            Alu.RotDir r = getDirectionRot(opcode);
            int op = Alu.rotate(r, read8AtHl()); 
            write8AtHl(Alu.unpackValue(op));
            combineAluFlags(op,ALU,V0,V0,ALU);
        } break;
        case ROT_HLR: {
            Alu.RotDir r = getDirectionRot(opcode);
            int op = Alu.rotate(r, read8AtHl(), getCarryValue()); 
            write8AtHl(Alu.unpackValue(op));
            combineAluFlags(op,ALU,V0,V0,ALU);
        } break;
        case SWAP_R8: {
            Reg r = extractRegBit0(opcode);
            int op = Alu.swap(reg8.get(r));
            setRegFromAlu(r,op);
            combineAluFlags(op,ALU,V0,V0,V0);
        } break;
        case SWAP_HLR: {
            int op = Alu.swap(read8AtHl());
            write8AtHl(Alu.unpackValue(op));
            combineAluFlags(op,ALU,V0,V0,V0);
        } break;
        case SLA_R8: {
            Reg r = extractRegBit0(opcode);
            int op = Alu.shiftLeft(reg8.get(r));
            setRegFromAlu(r,op);
            combineAluFlags(op,ALU,V0,V0,ALU);
        } break;
        case SRA_R8: {
            Reg r = extractRegBit0(opcode);
            int op = Alu.shiftRightA(reg8.get(r));
            setRegFromAlu(r,op);
            combineAluFlags(op,ALU,V0,V0,ALU);
        } break;
        case SRL_R8: {
            Reg r = extractRegBit0(opcode);
            int op = Alu.shiftRightL(reg8.get(r));
            setRegFromAlu(r,op);
            combineAluFlags(op,ALU,V0,V0,ALU);
        } break;
        case SLA_HLR: {
            int op = Alu.shiftLeft(read8AtHl());
            write8AtHl(Alu.unpackValue(op));
            combineAluFlags(op,ALU,V0,V0,ALU);
        } break;
        case SRA_HLR: {
            int op = Alu.shiftRightA(read8AtHl());
            write8AtHl(Alu.unpackValue(op));
            combineAluFlags(op,ALU,V0,V0,ALU);
        } break;
        case SRL_HLR: {
            int op = Alu.shiftRightL(read8AtHl());
            write8AtHl(Alu.unpackValue(op));
            combineAluFlags(op,ALU,V0,V0,ALU);
        } break;
        case BIT_U3_R8: {
            Reg r = extractRegBit0(opcode);
            boolean op = Bits.test(reg8.get(r), getN3FromOpcode(opcode));
            //op = false -> Bit=0
            combineAluFlags(0,(op?V0:V1),V0,V1,CPU);
        } break;
        case BIT_U3_HLR: {
            boolean op = Bits.test(read8AtHl(), getN3FromOpcode(opcode));
            combineAluFlags(0,(op?V0:V1),V0,V1,CPU);
        } break;
        case CHG_U3_R8: {
            Reg r = extractRegBit0(opcode);
            reg8.set(r, Bits.set(reg8.get(r), getN3FromOpcode(opcode), getValueBit6SetRes(opcode)));
        } break;
        case CHG_U3_HLR: {
            write8AtHl(Bits.set(read8AtHl(), getN3FromOpcode(opcode), getValueBit6SetRes(opcode)));
        } break;
        case DAA: {
            int op = Alu.bcdAdjust(getRegA(), getFlagValue(Alu.Flag.N),
                    getFlagValue(Alu.Flag.H),getFlagValue(Alu.Flag.C));
            setRegAFromAlu(op);
            combineAluFlags(op,ALU,CPU,V0,ALU);
        } break;
        case SCCF: {
            combineAluFlags(0,CPU,V0,V0,(combineFlagCBit3(opcode, false)?V1:V0));
        } break;
        case JP_N16: {
            nextRegPC = read16AfterOpcode();
        } break;
        case JP_HL: {
            nextRegPC = reg16(Reg16.HL);
        } break;
        case JP_CC_N16: {
            if(testConditionOpcode(opcode))
                nextRegPC = read16AfterOpcode();
        } break;
        case JR_E8: {
            nextRegPC += getE8();
        } break;
        case JR_CC_E8: {
            if(testConditionOpcode(opcode))
                nextRegPC = nextRegPC + getE8();
        } break;
        // Calls and returns
        case CALL_N16: {
            push16(nextRegPC);
            nextRegPC = read16AfterOpcode();
        } break;
        case CALL_CC_N16: {
            if(testConditionOpcode(opcode)) {
                push16(nextRegPC);
                nextRegPC = read16AfterOpcode();
            }
        } break;
        case RST_U3: {
            push16(nextRegPC);
            nextRegPC = AddressMap.RESETS[getN3FromOpcode(opcode)];
        } break;
        case RET: {
            nextRegPC = pop16();
        } break;
        case RET_CC: {
            if(testConditionOpcode(opcode))
                nextRegPC = pop16();
        } break;
        case EDI: {
            //if bit3=1->EI else bit3=0->DI
            regIME = getValueBit3Opcode(opcode);
        } break;
        case RETI: {
            regIME = true;
            nextRegPC = pop16();
        } break;
        case HALT: {
            nextNonIdleCycle = Long.MAX_VALUE;
        } break;
        case STOP:
            throw new Error("STOP is not implemented");
        }

        // Update of the counter        
        regPC = get16Lsb(nextRegPC);
    }
}
