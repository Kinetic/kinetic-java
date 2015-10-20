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
	private long clusterVersion = 0L;

	// pin used for default.
	private byte[] pin = new byte[] {};

	public long getClusterVersion() {
		return clusterVersion;
	}

	public void setClusterVersion(long clusterVersion) {
		this.clusterVersion = clusterVersion;
	}

	public byte[] getPin() {
		return pin;
	}

	public void setPin(byte[] pin) {
		this.pin = pin;
	}
}
