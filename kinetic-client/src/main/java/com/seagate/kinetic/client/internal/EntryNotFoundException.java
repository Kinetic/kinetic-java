package com.seagate.kinetic.client.internal;

import kinetic.client.KineticException;

public class EntryNotFoundException extends KineticException {

    /**
     * 
     */
    private static final long serialVersionUID = -2377497794808030692L;

    public EntryNotFoundException() {
        ;
    }

    public EntryNotFoundException(String message) {
        super(message);
    }

    public EntryNotFoundException(Throwable cause) {
        super(cause);
    }

    public EntryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
