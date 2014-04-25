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
 * 
 * This exception is thrown if {@link #FAULT_INJECT_CLOSE_CONNECTION} system
 * property is set to true.
 * 
 * @author chiaming
 * 
 */
public class FaultInjectedCloseConnectionException extends
FaultInjectedException {

	private static final long serialVersionUID = -2803993376541258500L;

	/**
	 * define this property to true to generate a fault and close connection
	 * when received a command.
	 */
	public static final String FAULT_INJECT_CLOSE_CONNECTION = "kinetic.fault.inject.closeConnection";

	public FaultInjectedCloseConnectionException() {
		;
	}

	public FaultInjectedCloseConnectionException(String message) {
		super(message);

	}

	public FaultInjectedCloseConnectionException(Throwable cause) {
		super(cause);

	}

	public FaultInjectedCloseConnectionException(String message, Throwable cause) {
		super(message, cause);

	}

}
