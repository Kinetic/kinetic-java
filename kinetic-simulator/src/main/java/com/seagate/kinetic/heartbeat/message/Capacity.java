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
