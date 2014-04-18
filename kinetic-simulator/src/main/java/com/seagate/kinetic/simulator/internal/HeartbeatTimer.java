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

import java.util.Timer;

/**
 * 
 * 
 * @author chiaming
 */
class HeartbeatTimer {

	private Timer timer = null;

	public HeartbeatTimer() {

		timer = new Timer("Simulator-Heart-Beat", false);
	}


	/**
	 * called when thread service is shutdown (all simulator instances called
	 * close).
	 */
	public void notifyClose() {
		this.timer.cancel();
	}

}
