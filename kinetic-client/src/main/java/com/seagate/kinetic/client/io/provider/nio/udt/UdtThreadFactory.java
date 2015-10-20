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
package com.seagate.kinetic.client.io.provider.nio.udt;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Please note: This class is for evaluation only and in prototype state.
 * 
 * @author chiaming
 */
public class UdtThreadFactory implements ThreadFactory {

	// thread name counter
	private static final AtomicInteger counter = new AtomicInteger();

	// thread name prefix
	private final String namePrefix;

	// set to true to use daemon threads for nio
	private boolean isDaemon = Boolean.getBoolean("kinetic.nio.daemon");

	public UdtThreadFactory(final String name) {
		this.namePrefix = name;
	}

	public UdtThreadFactory(final String name, boolean isDaemon) {
		this.namePrefix = name;
		this.isDaemon = isDaemon;
	}

	@Override
	public Thread newThread(final Runnable runnable) {

		// create new thread
		Thread thread = new Thread(runnable, namePrefix + '-'
				+ counter.getAndIncrement());

		// set as daemon thread
		thread.setDaemon(isDaemon);

		return thread;
	}
}
