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
 * Base (super) class for all fault injected exceptions for the simulator.
 * 
 * @author chiaming
 * 
 */
public class FaultInjectedException extends RuntimeException {

	private static final long serialVersionUID = -5384109392179754675L;

	public FaultInjectedException() {
		;
	}

	public FaultInjectedException(String message) {
		super(message);

	}

	public FaultInjectedException(Throwable cause) {
		super(cause);

	}

	public FaultInjectedException(String message, Throwable cause) {
		super(message, cause);

	}

}
