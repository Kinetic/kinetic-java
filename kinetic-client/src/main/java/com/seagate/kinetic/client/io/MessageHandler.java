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
package com.seagate.kinetic.client.io;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.AsyncKineticException;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.EntryNotFoundException;
import kinetic.client.KineticException;

import com.seagate.kinetic.client.internal.CallbackContext;
import com.seagate.kinetic.client.internal.ClientProxy;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.client.internal.async.DeleteAsyncCallbackHandler;
import com.seagate.kinetic.client.internal.async.GetAsyncCallbackHandler;
import com.seagate.kinetic.client.internal.async.GetKeyRangeAsyncCallbackHandler;
import com.seagate.kinetic.client.internal.async.GetMetadataAsyncCallbackHandler;
import com.seagate.kinetic.client.internal.async.PutAsyncCallbackHandler;
import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.common.lib.ProtocolMessageUtil;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Message.AuthType;

/**
 *
 * Kinetic Client message handler.
 * <p>
 *
 * @author chiaming Yang
 * @param <AsyncCallbackHandler>
 *
 */
public class MessageHandler implements ClientMessageService, Runnable {

	// my logger
	private final static Logger logger = Logger.getLogger(MessageHandler.class
			.getName());

	// XXX 07172013 chiaming: make it elastic.
	private Thread myThread = null;

	// my message queue
	private final LinkedBlockingQueue<KineticMessage> asyncQueue = new LinkedBlockingQueue<KineticMessage>();

	private final Map<Long, Object> ackmap = new ConcurrentHashMap<Long, Object>();

	// flag running flag
	private volatile boolean isRunning = false;

	// close flag
	private volatile boolean isClosed = false;

	private ClientProxy client = null;
	// io handler
	private IoHandler iohandler = null;

	private int asyncQueuedSize = 10;

	// request timeout
	private long requestTimeout = 30000;

	private final Object syncObj = new Object();
	
	private boolean isStatusMessageReceived = false;

	/**
	 * Constructor.
	 *
	 * @param transport
	 *            the iohandler associated with this message handler.
	 */
	public MessageHandler(IoHandler iohandler) {

		this.iohandler = iohandler;

		this.client = iohandler.getClient();

		this.asyncQueuedSize = this.client.getConfiguration()
				.getAsyncQueueSize();

		this.requestTimeout = this.client.getConfiguration().getRequestTimeoutMillis();
	}
	

	/**
	 * process message from IoHandler.
	 *
	 * @param message
	 *            message from IoHandler
	 *
	 * @throws InterruptedException
	 *             if interrupted.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void routeMessage(KineticMessage message)
			throws InterruptedException {

		if (logger.isLoggable(Level.FINEST)) {
			logger.info("read/routing message: " + message);
		}
		
		/**
		 * check status message has received.
		 */
		if (this.isStatusMessageReceived == false) {
		    
		    if (message.getMessage().getAuthType() == AuthType.UNSOLICITEDSTATUS) {

                // set cid
		        this.client.setConnectionId(message);
		        this.isStatusMessageReceived = true;

                // notify listener
                this.notifyListener(message);
		        return;
		    } else {
		        if (this.iohandler.shouldWaitForStatusMessage()) {
		            logger.warning("received unexpected message ..." + message.getMessage() + ", command=" + message.getCommand());
		        }
		    }
		}

		Long seq = Long.valueOf(message.getCommand().getHeader()
				.getAckSequence());

		Object obj = this.ackmap.get(seq);

		// check if sync request
		if (obj != null && (obj instanceof LinkedBlockingQueue)) {
			// sync request
			((LinkedBlockingQueue<KineticMessage>) obj).put(message);
		} else {
			// async request
			// this.asyncQueue.put(message);
			this.putAndCheckRunning(message);
		}

	}

	@Override
    public void run() {

        try {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("thread started, name=" + this.myThread.getName());
            }

            while (isRunning && !isClosed) {
                // poll message from queue
                KineticMessage msg;
                try {
                    msg = this.asyncQueue.poll(7000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.info("Interrupted");
                    isClosed = true;
                    exitRunning();
                    break;
                }

                if (msg != null) {
                    // process message
                    doProcessMessage(msg);
                } else {
                    // exit thread
                    exitRunning();
                }
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Main run loop failed", t);
            isClosed = true;
        } finally {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("thread exited ." + this.myThread.getName());
            }
        }
    }

	/**
	 * process message.
	 *
	 * @param message
	 *            message from IoHandler.
	 * @throws InterruptedException
	 */
	private void doProcessMessage(KineticMessage message)
			throws InterruptedException {

		// get ack seq
		Long seq = Long.valueOf(message.getCommand().getHeader()
				.getAckSequence());
		// get callback instance
		Object context = this.ackmap.get(seq);

		if (context != null) {
			try {
				if (context instanceof CallbackContext) {
					// invoke callback handler
					invokeCallbackHandler(context, message);
				} else {
					logger.warning("received unknown message: " + message);
				}
			} finally {
				// this.ackmap.remove(seq);
				this.asyncDelivered(seq);
			}
		} else {

            if (message.getMessage().getAuthType() == AuthType.UNSOLICITEDSTATUS) {

                /**
                 * log unsolicited status message.
                 */
                logger.warning("received unsolicited message: "
                        + message.getCommand().getStatus().getCode() + ":"
                        + message.getCommand().getStatus().getStatusMessage());

                /**
                 * XXX chiaming 01/28/2015: The only possible behavior from the
                 * drive is to close the current connection. So the API needs to
                 * handle this accordingly.
                 */
                this.client.close();

                /**
                 * Call listener if one is set.
                 */
                this.notifyListener(message);

            } else {
                logger.warning("message cannot be delivered., please verify request timeout set in the configurtion., message="
                        + ProtocolMessageUtil.toString(message));
            }
		}
	}

    private void notifyListener(KineticMessage message) {

        try {
            if (this.client.getConfiguration().getConnectionListener() != null) {

                // call listener
                this.client.getConfiguration().getConnectionListener()
                        .onMessage(message);
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

	public KineticMessage write(KineticMessage message) throws IOException,
	InterruptedException {

		LinkedBlockingQueue<KineticMessage> lbq = new LinkedBlockingQueue<KineticMessage>(
				1);

		KineticMessage respond = null;

        Long seq = 0L;

		try {

            synchronized (this) {

                this.client.finalizeHeader(message);

                seq = Long.valueOf(message.getCommand().getHeader()
                        .getSequence());

                this.ackmap.put(seq, lbq);

                // this.iohandler.write(message);
                this.doWrite(message);
            }

            if (this.isClosed) {
				throw new IOException("Connection is closed.");
			} else {
				respond = lbq.poll(this.requestTimeout, TimeUnit.MILLISECONDS);
			}
			
		} finally {
			this.ackmap.remove(seq);
		}

		return respond;
	}

    public synchronized void writeAsync(KineticMessage message, Object context)
			throws IOException,
			InterruptedException {

        this.client.finalizeHeader(message);

		Long seq = Long.valueOf(message.getCommand().getHeader()
				.getSequence());

		while (ackmap.size() >= asyncQueuedSize && (isClosed == false)) {
			this.wait();
		}

		this.ackmap.put(seq, context);
		
		this.doWrite(message);
	}

    public synchronized void writeNoAck(KineticMessage message)
            throws IOException {

        this.client.finalizeHeader(message);

        this.doWrite(message);
    }

	@SuppressWarnings("rawtypes")
	private void invokeCallbackHandler(Object cbContext, KineticMessage response) {

		MessageType type = response.getCommand().getHeader()
				.getMessageType();

		AsyncKineticException exception = this
				.asyncResponseHmacCheck(response);

		switch (type) {
		case PUT_RESPONSE:

			new PutAsyncCallbackHandler().onAsyncMessage(cbContext,
					response, exception);

			break;
		case GET_RESPONSE:
			boolean isMetadataOnly = ((CallbackContext) cbContext)
					.getRequestMessage().getCommand().getBody()
					.getKeyValue().getMetadataOnly();
			if (isMetadataOnly)
			{
				new GetMetadataAsyncCallbackHandler(MessageType.GET_RESPONSE).onAsyncMessage(
						cbContext, response,
						exception);
			}else
			{
				new GetAsyncCallbackHandler(MessageType.GET_RESPONSE).onAsyncMessage(
						cbContext, response,
						exception);
			}
			break;
		case GETKEYRANGE_RESPONSE:
			new GetKeyRangeAsyncCallbackHandler(MessageType.GETKEYRANGE_RESPONSE).onAsyncMessage(
					cbContext, response,
					exception);
			break;
		case GETNEXT_RESPONSE:
			new GetAsyncCallbackHandler(MessageType.GETNEXT_RESPONSE)
			.onAsyncMessage(cbContext, response, exception);

			break;

		case GETPREVIOUS_RESPONSE:
			new GetAsyncCallbackHandler(MessageType.GETPREVIOUS_RESPONSE)
			.onAsyncMessage(cbContext, response, exception);
			break;

		case DELETE_RESPONSE:
			new DeleteAsyncCallbackHandler().onAsyncMessage(cbContext,
					response, exception);
			break;

		default:
			break;
		}
	}

	public static AsyncKineticException checkPutReply(
			CallbackContext<Entry> context) {

		AsyncKineticException lce = null;

		try {
			MessageFactory.checkReply(context.getRequestMessage(), context.getResponseMessage());
		} catch (KineticException e) {
			lce = getAsyncKineticException(context, e);
		}

		return lce;
	}

	public static AsyncKineticException checkGetReply(
			CallbackContext<Entry> context, MessageType messageType) {

		AsyncKineticException lce = null;

		try {
			MessageFactory.checkReply(context.getRequestMessage(), context.getResponseMessage());
		} catch (EntryNotFoundException enfe) {
		    //entry not found will return null entry to applications
		    ;
		} catch (KineticException e) {

			lce = new AsyncKineticException(lce);

			lce.setRequestMessage(context.getRequestMessage());
			lce.setResponseMessage(context.getResponseMessage());
		}

		return lce;
	}

	public static AsyncKineticException checkGetKeyRangeReply(
			CallbackContext<List<byte[]>> context, MessageType messageType) {

		AsyncKineticException lce = null;

		try {
			MessageFactory.checkReply(context.getRequestMessage(), context.getResponseMessage());
		} catch (KineticException e) {

			lce = new AsyncKineticException(lce);

			lce.setRequestMessage(context.getRequestMessage());
			lce.setResponseMessage(context.getResponseMessage());
		}

		return lce;
	}

	public static AsyncKineticException checkGetMetadataReply(
			CallbackContext<EntryMetadata> context, MessageType messageType) {

		AsyncKineticException lce = null;

		try {
			MessageFactory.checkReply(context.getRequestMessage(), context.getResponseMessage());
		} catch (EntryNotFoundException enfe) {
		    //entry not found will return null to applications
		    ;
		} catch (KineticException e) {

			lce = new AsyncKineticException(lce);

			lce.setRequestMessage(context.getRequestMessage());
			lce.setResponseMessage(context.getResponseMessage());
		}

		return lce;
	}

	public static AsyncKineticException checkDeleteReply(
			CallbackContext<Boolean> context) {

		AsyncKineticException lce = null;

		try {
			MessageFactory.checkDeleteReply(context.getRequestMessage(), context.getResponseMessage());
		} catch (KineticException e) {

            lce = new AsyncKineticException(e);

			lce.setRequestMessage(context.getRequestMessage());
			lce.setResponseMessage(context.getResponseMessage());
		}

		return lce;
	}

	private static AsyncKineticException getAsyncKineticException(
			CallbackContext<?> context, KineticException lce) {

		AsyncKineticException alce = new AsyncKineticException(lce);

		alce.setRequestMessage(context.getRequestMessage());
		alce.setResponseMessage(context.getResponseMessage());

		return alce;
	}

	private AsyncKineticException asyncResponseHmacCheck(KineticMessage response) {

		AsyncKineticException asyncException = null;

		/**
		 * Pin Auth does not require Hmac calculation.
		 */
		if (response.getMessage().getAuthType() == AuthType.HMACAUTH) {
		    if (this.client.checkHmac(response) == false) {
		        asyncException = new AsyncKineticException(
		                "Hmac did not compare");
		    }
		}

		return asyncException;
	}

	@Override
	public ClientConfiguration getConfiguration() {
		return this.client.getConfiguration();
	}

	private void doWrite(KineticMessage message) throws IOException {

		if (logger.isLoggable(Level.FINEST)) {
			logger.info("writing message: " + message);
		}

		this.iohandler.write(message);
	}


	/**
	 * Close the message handler.
	 */
	@Override
	public void close() {

		this.isClosed = true;
		this.isRunning = false;

		if (this.myThread != null) {
			this.myThread.interrupt();
		}

		// wake up sync callers
		this.wakeupSyncCallers();

		// wakes up all wait threads for this instance.
		synchronized (this) {
			this.notifyAll();
		}

	}

	@SuppressWarnings("unchecked")
	private void wakeupSyncCallers() {

		// logger.info("waking up sync callers ...");

		for (Long id : this.ackmap.keySet().toArray(new Long[0])) {

			Object obj = this.ackmap.get(id);

			try {
				if (obj instanceof LinkedBlockingQueue) {
					// the connection is closed, unblock callers
				    ((LinkedBlockingQueue<KineticMessage>) obj).put(new KineticMessage());
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}

	private synchronized void asyncDelivered(Long key) {
		this.ackmap.remove(key);
		this.notifyAll();
	}

	/**
	 * put message in queue and start a new thread to dispatch if none is
	 * started.
	 *
	 * @param message
	 *            message to put in the async queue
	 * @throws InterruptedException
	 *             if interrupted.
	 */
	private void putAndCheckRunning(KineticMessage message)
			throws InterruptedException {

		// check if the client is closed already.
		if (this.isClosed) {
			return;
		}

		this.asyncQueue.put(message);

		synchronized (syncObj) {

			// client is still open, check if there is a thread running and
			// dispatching messages
			// if not, created one and start it.
			if (isRunning == false) {

				// instantiate a new thread
				this.myThread = new Thread(this);

				// set identity
				this.myThread.setName("ClientMessageHandler-"
						+ client.getConfiguration().getHost() + "-"
						+ client.getConfiguration().getPort());

				// set running flag
				this.isRunning = true;

				// start dispatching messages
				this.myThread.start();
			}
		}
	}

	/**
	 * check if the thread should exit.
	 */
	private void exitRunning() {

		if (this.isClosed) {
			return;
		}

		synchronized (syncObj) {
			// only set flag to false if queue is empty
			if (this.asyncQueue.isEmpty()) {
				this.isRunning = false;
			}
		}
	}

}
