package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

/**
 * The Controller of the BootRom.
 * Control the access to all the Cartridge and intercept only a part if boot.
 */
public final class BootRomController implements Component{
    private boolean bootRangeDisabled = false;
    private final Cartridge cartridge;

    /**
     * The unique constructor of the class.
     * @param cartridge : The cartridge where we read the data (except boot-data).
     * @throws NullPointerException if the cartridge object given is null. 
     */
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.cartridge = cartridge;
    }

    @Override
    public int read(int address) {
        //Intercept the readings if boot
        if(address >= AddressMap.BOOT_ROM_START && address < AddressMap.BOOT_ROM_END 
                && !bootRangeDisabled)
            return Bits.clip(8, BootRom.DATA[address]);
        return cartridge.read(address);
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        if(!bootRangeDisabled && address == AddressMap.REG_BOOT_ROM_DISABLE) {
            bootRangeDisabled = true;
            return;
        }
        cartridge.write(address, data);            
    }
}
