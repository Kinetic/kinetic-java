/**
 * Copyright 2013-2015 Seagate Technology LLC.
 *
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at
 * https://mozilla.org/MP:/2.0/.
 * 
 * This program is distributed in the hope that it will be useful,
 * but is provided AS-IS, WITHOUT ANY WARRANTY; including without 
 * the implied warranty of MERCHANTABILITY, NON-INFRINGEMENT or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the Mozilla Public 
 * License for more details.
 *
 * See www.openkinetic.org for more project information
 */
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
