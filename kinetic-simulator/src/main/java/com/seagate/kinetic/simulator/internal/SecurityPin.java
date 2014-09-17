package com.seagate.kinetic.simulator.internal;

import com.google.protobuf.ByteString;

public class SecurityPin {
    
    private ByteString lockPin = null;
    
    private ByteString erasePin = null;

    public SecurityPin() {
        // TODO Auto-generated constructor stub
    }
    
    public void setLockPin (ByteString pin) {
        this.lockPin = pin;
    }
    
    public ByteString getLockPin() {
        return this.lockPin;
    }
    
    public void setErasePin (ByteString pin) {
        this.erasePin = pin;
    }
    
    public ByteString getErasePin() {
        return this.erasePin;
    }

}
