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
package com.seagate.kinetic.example.monitor;

import java.io.IOException;

import com.seagate.kinetic.monitor.HeartbeatMonitor;
import com.seagate.kinetic.monitor.MonitorConfiguration;

/**
 * 
 * Start a heartbeat monitor example.
 * <p>
 * This example starts one instance of heartbeat monitor with default
 * configurations.
 * <p>
 * The started heartbeat monitor listens on port 8080, start simulators(run
 * MultiKineticSimulatorsForHeartbeatMonitor at the same package), then go to
 * your browser(suggested firefox) http://localhost:8080 to see more detail
 * heartbeat information.
 * 
 */
public class HeartbeatMonitorExample {
	public static void main(String[] args) throws IOException {
		MonitorConfiguration config = new MonitorConfiguration();
		HeartbeatMonitor monitor = new HeartbeatMonitor(config);
		monitor.start();
	}
}
