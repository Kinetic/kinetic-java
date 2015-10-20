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
package com.seagate.kinetic.simulator.io.provider.nio;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.simulator.io.provider.nio.tcp.NioRequestMessageContext;
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
public class NioQueuedRequestProcessRunner implements Runnable {

	private static final Logger logger = Logger
			.getLogger(NioQueuedRequestProcessRunner.class.getName());

	private MessageService service = null;

	// my message queue for the current connection
	private final LinkedBlockingQueue<NioRequestMessageContext> lbqueue = new LinkedBlockingQueue<NioRequestMessageContext>();

	// flag running flag
	private volatile boolean isRunning = false;

	// close flag
	private volatile boolean isClosed = false;

	// reference to current running flag
	private Thread currentThread = null;

	public NioQueuedRequestProcessRunner(MessageService engine) {
		this.service = engine;

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
	public void processRequest(ChannelHandlerContext ctx, KineticMessage message)
			throws InterruptedException {

		// request context
		NioRequestMessageContext requestContext = new NioRequestMessageContext(
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
				NioRequestMessageContext context = this.lbqueue.poll(6,
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

	public void doProcessMessage(NioRequestMessageContext context) {

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("received request: " + context.getRequestMessage());
		}

		KineticMessage response = this.service.processRequest(context
				.getRequestMessage());

        if (shouldSendResponse(response)) {

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("writing response: "
                        + ((Message.Builder) response.getMessage()).build());
            }

            context.getChannelHandlerContext().writeAndFlush(response);
        } else {
            logger.info("*** in no ack mode, response message is not sent ...");
		}

	}

    /**
     * no response is sent for batch PUT/DELETE.
     * 
     * @param response
     *            response message.
     * 
     * @return false if batch PUT/DELETE. Otherwise return true.
     */
    private boolean shouldSendResponse(KineticMessage response) {

        boolean flag = true;

        if (response.getIsBatchMessage() == true) {
            MessageType mt = response.getCommand().getHeader().getMessageType();
            if (mt == MessageType.PUT_RESPONSE
                    || mt == MessageType.DELETE_RESPONSE) {
                flag = false;
            }
        }

        return flag;
    }

}
