package com.seagate.kinetic.simulator.internal;

/**
 * Exception to be thrown when security checks fail.
 *
 * @author Andrew Mitchell
 */
public class KVSecurityException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 7804638863266767621L;

    public KVSecurityException(String message) {
        super(message);
    }
}
