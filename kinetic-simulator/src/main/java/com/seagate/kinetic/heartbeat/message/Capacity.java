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
package com.seagate.kinetic.heartbeat.message;

public class Capacity {

    private float remaining = 0;
    private float total = 0;

    public Capacity() {
        ;
    }

    public void setRemaining(float remaining) {
        this.remaining = remaining;
    }

    public float getRemaining() {
        return this.remaining;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getTotal() {
        return this.total;
    }

}
