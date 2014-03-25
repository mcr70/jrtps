package net.sf.jrtps.rtps;

/**
 * Enumeration for different kind of changes made to an instance.
 * 
 * @author mcr70
 */
public enum ChangeKind {
    /**
     * Writer updates an instance.
     */
    WRITE,
    /**
     * Writer disposes an instance.
     */
    DISPOSE,
    /**
     * Writer unregisters an instance.
     */
    UNREGISTER;
}