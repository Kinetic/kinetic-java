/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
