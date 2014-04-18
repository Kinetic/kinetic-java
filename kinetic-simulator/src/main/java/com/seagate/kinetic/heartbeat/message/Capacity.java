/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
