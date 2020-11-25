# GameBoy Emulator
The project is a complete Nintendo Game Boy of 1989 emulator written from scratch in Java and using unit test to ensure that every component is working correctly. The game boy games .gb are playable using the emulator.  The
original Sharp LR35902 processor is emulated by simulating every cycle of the CPU. The components impelmented are: Bus controller, ROM, RAM,
Direct Memory Accesses (DMA), screen controller etc... are implemented according to the
original Nintendo specs.

## Emulated parts

* Bus, ROM and RAM controllers
* Arithmetic logic unit (ALU) of the CPU
* Registers and "high RAM" of the CPU
* Decoding of arithmetic, logic and control instructions
* Direct memory access (DMA)
* Screen controller
* Timer and keyboard interrupts
* Booting from a cartridge

## Ameliorations
* Ability to Pause and restart the emulator
* Ability to accelerate the emulation with a 2x speed
* Adapt the size of the screen
* Save screenshots of the games
* Display the tiles in memory that are currently used in the game


## Screenshot of the game
![The Legend of Zelda: Link's Awakening](screenshot/img_2018-05-29_22-19-15-998.png "
The Legend of Zelda: Link's Awakening") &nbsp;