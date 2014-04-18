/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
