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
package com.seagate.kinetic.client.io.provider.nio;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory for kinetic client nio transport services.
 *
 * @author chiaming
 */
public class NioClientThreadFactory implements ThreadFactory {

	// thread name counter
	private static final AtomicInteger counter = new AtomicInteger();

	// thread name prefix
	private final String namePrefix;

	// by default, use daemon threads for nio
	private boolean isDaemon = true;

	public NioClientThreadFactory(final String name) {
		this.namePrefix = name;
	}

	public NioClientThreadFactory(final String name, boolean isDaemon) {
		this.namePrefix = name;
		this.isDaemon = isDaemon;
	}

	/**
	 * {@inheritDoc}
	 */
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
