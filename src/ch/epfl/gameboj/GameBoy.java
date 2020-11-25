package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * Class representing the Gameboy where there are all main components.
 */
public final class GameBoy {
    public static final long CYCLES_PER_SECOND = 1 << 20;
    public static final double CYCLES_PER_NANOSEC = CYCLES_PER_SECOND/1_000_000_000.;

    private final Bus busGameBoy;
    private final Ram workRam;
    private final RamController ramCtrl;
    private final RamController ramCtrlEcho;
    private final BootRomController bootRomController;
    private final Cpu cpu;
    private final Timer timer;
    private final Joypad joypad;
    private final LcdController lcdController;
    private long cyclesSimulated = 0;

    /**
     * Unique constructor of the class.
     * @param The cartridge that contains the game ROM.
     * @throws NullPointerException if the cartridge is null.
     **/
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);        

        busGameBoy = new Bus();
        cpu = new Cpu();
        workRam = new Ram(AddressMap.WORK_RAM_SIZE);
        ramCtrl = new RamController(workRam,AddressMap.WORK_RAM_START,AddressMap.WORK_RAM_END);
        ramCtrlEcho = new RamController(workRam,AddressMap.ECHO_RAM_START,AddressMap.ECHO_RAM_END);
        bootRomController = new BootRomController(cartridge);
        timer = new Timer(cpu);
        lcdController = new LcdController(cpu);
        joypad = new Joypad(cpu);

        ramCtrl.attachTo(busGameBoy);
        ramCtrlEcho.attachTo(busGameBoy);
        bootRomController.attachTo(busGameBoy);
        cpu.attachTo(busGameBoy);
        timer.attachTo(busGameBoy);
        lcdController.attachTo(busGameBoy);
        joypad.attachTo(busGameBoy);
    }

    /**
     * Method that return the Bus of the GameBoy.
     * @return The bus of the Gameboy.
     **/
    public Bus bus() {
        return busGameBoy;
    }

    /**
     * Getter of the Cpu of the Gameboy.
     * @return The CPU of the gameboy.
     */
    public Cpu cpu() {
        return cpu;
    }

    /**
     * Getter of the joypad of the Gameboy.
     * @return the Joypad of the Gameboy.
     */
    public Joypad joypad() {
        return joypad;
    }

    /**
     * Simulate the functioning of the Gameboy till "cycle" (exclude).
     * @param cycle is the cycle we want to attain with the CPU (but not execute it)
     * @throws IllegalArgumentException if the given cycle is already simulated or negative
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycle >= cyclesSimulated && cycle >= 0);
        for(long i = cyclesSimulated;i<cycle;++i) {
            timer.cycle(i);
            lcdController.cycle(i);
            cpu.cycle(i);
            ++cyclesSimulated;
        }
    }

    /**
     * Getter of the cycles simulated.
     * @return the number of simulated cycles by the CPU.
     */
    public long cycles() {
        return cyclesSimulated;
    }

    /**
     * Getter of the timer object.
     * @return the timer of the Gameboy.
     */
    public Timer timer() {
        return timer;
    }

    /**
     * Getter of the lcdController object.
     * @return the lcdController of the Gameboy.
     */
    public LcdController lcdController() {
        return lcdController;
    }
    /*
    public void save(File f) {
        workRam.writeRamIntoFile(f);
        //CPU highRam
        cpu
    }*/
}
