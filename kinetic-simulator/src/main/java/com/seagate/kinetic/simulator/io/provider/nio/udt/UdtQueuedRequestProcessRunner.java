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
package com.seagate.kinetic.simulator.io.provider.nio.udt;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

/**
 * 
 * Queued request message process runner for simulator nio service.
 * <p>
 * Command messages are processed in sequential order within the same
 * connection.
 * 
 * @author chiaming
 * 
 */
public class UdtQueuedRequestProcessRunner implements Runnable {

	private static final Logger logger = Logger
			.getLogger(UdtQueuedRequestProcessRunner.class.getName());

	private MessageService service = null;

	// my message queue for the current connection
	private final LinkedBlockingQueue<UdtRequestMessageContext> lbqueue = new LinkedBlockingQueue<UdtRequestMessageContext>();

	// flag running flag
	private volatile boolean isRunning = false;

	// close flag
	private volatile boolean isClosed = false;

	// reference to current running flag
	private Thread currentThread = null;

	public UdtQueuedRequestProcessRunner(MessageService service) {
		this.service = service;

		logger.info("nio queued process runner instantiated.  message ordering is enforced.");
	}

	/**
	 * process request message from IoHandler.
	 * 
	 * @param message
	 *            request message.
	 * 
	 * @throws InterruptedException
	 *             if interrupted.
	 */
	public void processRequest(ChannelHandlerContext ctx, byte[] message)
			throws InterruptedException {

		// request context
		UdtRequestMessageContext requestContext = new UdtRequestMessageContext(
				ctx, message);

		// put to queue
		this.lbqueue.put(requestContext);

		// check if there is a thread running. if not, submit to thread pool
		// for execution.
		checkRunning();
	}

	/**
	 * Check if there is a thread running and processing the queue. If not,
	 * submit myself to the executor service.
	 */
	private void checkRunning() {

		if (this.isRunning == false) {

			synchronized (this) {

				if (this.isRunning == false) {

					if (this.isClosed) {
						return;
					}

					// set flag to true
					this.isRunning = true;

					// execute by the thread pool
					this.service.execute(this);
				}
			}

		}
	}

	@Override
	public void run() {

		try {

			while (isRunning()) {

				// save the current thread reference
				this.currentThread = Thread.currentThread();

				// poll message from queue
				UdtRequestMessageContext context = this.lbqueue.poll(6,
						TimeUnit.SECONDS);

				if (context != null) {
					// process message
					doProcessMessage(context);
				} else {
					// break out of loop
					this.isRunning = false;
				}

			}
		} catch (InterruptedException ie) {
			// interrupted when closed
			;
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			// set running to false
			isRunning = false;

			// thread returning to pool
			this.currentThread = null;
		}

	}

	/**
	 * check if there is a thread running.
	 * 
	 * @return true if the thread is polling the message.
	 */
	private boolean isRunning() {
		return (isRunning && (!isClosed));
	}

	public void close() {
		// set closed flag

		if (this.isClosed) {
			return;
		}

		this.isClosed = true;

		// wake up lbqueue
		if (this.currentThread != null) {
			this.currentThread.interrupt();
		}

		logger.fine("nio queued request process runner closed.");
	}

	public void doProcessMessage(UdtRequestMessageContext context) {

		UdtRequestProcessRunner rpr = new UdtRequestProcessRunner(service,
				context.getChannelHandlerContext(), context.getRequestMessage());

		rpr.run();
	}

}
