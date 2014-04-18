/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.lib;

/**
 * 
 * Setup info
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */
public class SetupInfo {
	private Long clusterVersion = Long.valueOf(0);

	// pin used for default.
	private byte[] pin = new byte[] {};

	public Long getClusterVersion() {
		return clusterVersion;
	}

	public void setClusterVersion(Long clusterVersion) {
		this.clusterVersion = clusterVersion;
	}

	public byte[] getPin() {
		return pin;
	}

	public void setPin(byte[] pin) {
		this.pin = pin;
	}
}
