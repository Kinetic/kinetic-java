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
package com.seagate.kinetic.simulator.internal;

/**
 * Device power down exception.
 * <p>
 * This exception is thrown if a request is received when the device is in power
 * down state.
 * 
 * @author chiaming
 *
 */
public class DevicePowerDownException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -7954081264866180271L;

    public DevicePowerDownException() {
        ;
    }

    public DevicePowerDownException(String message) {
        super(message);
    }

    public DevicePowerDownException(Throwable cause) {
        super(cause);
    }

    public DevicePowerDownException(String message, Throwable cause) {
        super(message, cause);
    }

    public DevicePowerDownException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
