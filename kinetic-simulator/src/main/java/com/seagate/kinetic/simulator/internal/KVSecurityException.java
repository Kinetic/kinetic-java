package com.seagate.kinetic.simulator.internal;

/**
 * Exception to be thrown when security checks fail.
 *
 * @author Andrew Mitchell
 */
public class KVSecurityException extends Exception {
    public KVSecurityException(String message) {
        super(message);
    }
}
