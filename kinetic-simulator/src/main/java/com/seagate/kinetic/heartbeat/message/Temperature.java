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

public class Temperature {

    private String name = null;

    // The current temperature in degrees c
    private float current = 0;
    private float minimum = 0;
    private float maximum = 0;
    private float target = 0;

    public Temperature() {
        // TODO Auto-generated constructor stub
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setCurrent(float v) {
        this.current = v;
    }

    public float getCurrent() {
        return this.current;
    }

    public float getMinimum() {
        return this.minimum;
    }

    public void setMinimum(float v) {
        this.minimum = v;
    }

    public float getMaximum() {
        return this.maximum;
    }

    public void setMaximum(float v) {
        this.maximum = v;
    }

    public float getTarget() {
        return this.target;
    }

    public void setTarget(float v) {
        this.target = v;
    }

}
