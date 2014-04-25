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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.simulator.io.provider.nio.NioThreadFactory;

/**
 * Simulator thread pooling service.
 * <p>
 * 
 * @author chiaming
 * 
 */
public class ThreadPoolService {

	private int referenceCount = 0;

	private ExecutorService pool = null;

	// private HeartbeatTimer internalRunner = null;

	private Timer timer = null;

	private final Map<SimulatorEngine, SimulatorEngine> map = new HashMap<SimulatorEngine, SimulatorEngine>();

	public synchronized void register(SimulatorEngine engine) {
		referenceCount++;

		this.map.put(engine, engine);

		if (referenceCount == 1) {
			init(engine.getServiceConfiguration());
		}

		// schedule heart beat
		if (engine.getServiceConfiguration().getTickTime() > 0) {
			this.timer.scheduleAtFixedRate(engine.getHearBeat(), 1000,
					engine
					.getServiceConfiguration().getTickTime());
		}
	}

	public synchronized void deregister(SimulatorEngine engine) {
		referenceCount--;

		this.map.remove(engine);

		// stop heart beat
		if (engine.getHearBeat() != null) {
			engine.getHearBeat().close();
		}

		if (referenceCount == 0) {
			close();
		}
	}

	private void init(SimulatorConfiguration config) {

		// internal user thread
		// internalRunner = new HeartbeatTimer();
		this.timer = new Timer("simulator-heartbeat", false);

		// all threads created from this factory are daemon threads
		ThreadFactory tfactory = new NioThreadFactory("Simulator", true);

		// thread executor service
		pool = Executors.newCachedThreadPool(tfactory);
	}

	public synchronized void execute(Runnable runnable) {
		pool.execute(runnable);
	}

	private synchronized void close() {

		this.map.clear();

		// stop internal thread
		// internalRunner.notifyClose();

		timer.cancel();

		// shutdown simulator pool
		try {
			// stop serving new request
			pool.shutdown();
			pool.awaitTermination(100, TimeUnit.MILLISECONDS);

		} catch (Exception e) {
			;
		}
	}

}
