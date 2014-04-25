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
