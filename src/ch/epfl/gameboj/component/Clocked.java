package ch.epfl.gameboj.component;

/**
 * Interface to implement for every component driven by the system clock
 *
 */
public interface Clocked {
    
    /**
     * The component must execute his commands during the cycle given in parameter
     * @param cycle representing the cycle to execute
     */
    abstract void cycle(long cycle);
}