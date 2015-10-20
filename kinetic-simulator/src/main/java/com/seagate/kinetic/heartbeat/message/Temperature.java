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
