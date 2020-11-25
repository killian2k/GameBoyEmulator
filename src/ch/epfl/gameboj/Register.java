package ch.epfl.gameboj;

public interface Register {

    /**
     * {@link java.lang.Enum#ordinal()}
     */
    abstract int ordinal();

    /**
     * same as {@link java.lang.Enum#ordinal()} but renamed
     */
    public default int index() {
        return ordinal();
    }
}