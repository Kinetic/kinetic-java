/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
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
