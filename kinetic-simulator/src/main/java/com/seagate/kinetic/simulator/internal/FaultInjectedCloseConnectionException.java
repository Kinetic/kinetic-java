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
