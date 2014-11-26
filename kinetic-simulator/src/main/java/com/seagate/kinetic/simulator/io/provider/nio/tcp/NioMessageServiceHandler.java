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
package com.seagate.kinetic.simulator.io.provider.nio.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.simulator.internal.ConnectionInfo;
import com.seagate.kinetic.simulator.internal.FaultInjectedCloseConnectionException;
import com.seagate.kinetic.simulator.internal.SimulatorEngine;
import com.seagate.kinetic.simulator.io.provider.nio.BatchQueue;
import com.seagate.kinetic.simulator.io.provider.nio.NioConnectionStateManager;
import com.seagate.kinetic.simulator.io.provider.nio.NioQueuedRequestProcessRunner;
import com.seagate.kinetic.simulator.io.provider.nio.RequestProcessRunner;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

/**
 *
 * @author chiaming
 */
public class NioMessageServiceHandler extends
		SimpleChannelInboundHandler<KineticMessage> {

	private static final Logger logger = Logger
			.getLogger(NioMessageServiceHandler.class.getName());

	private MessageService lcservice = null;

	private boolean enforceOrdering = false;

	private NioQueuedRequestProcessRunner queuedRequestProcessRunner = null;

    private BatchQueue batchQueue = null;

	private static boolean faultInjectCloseConnection = Boolean
			.getBoolean(FaultInjectedCloseConnectionException.FAULT_INJECT_CLOSE_CONNECTION);

	public NioMessageServiceHandler(MessageService lcservice2) {
		this.lcservice = lcservice2;

		this.enforceOrdering = lcservice.getServiceConfiguration()
				.getMessageOrderingEnforced();

		if (this.enforceOrdering) {
			this.queuedRequestProcessRunner = new NioQueuedRequestProcessRunner(
					lcservice);
		}
	}
	
	@Override
	public void channelActive (ChannelHandlerContext ctx) throws Exception {
	    super.channelActive(ctx);
	    
	    // register connection info with the channel handler context
        @SuppressWarnings("unused")
        ConnectionInfo info = this.lcservice.registerNewConnection(ctx);
	    
	    //logger.info("***** connection registered., sent UNSOLICITEDSTATUS with cid = " + info.getConnectionId());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx,
			KineticMessage request)
			throws Exception {

		if (faultInjectCloseConnection) {
			throw new FaultInjectedCloseConnectionException(
					"Fault injected for the simulator");
		}
		
		// set ssl channel flag to false
		request.setIsSecureChannel(false);
		
		// check if conn id is set
		NioConnectionStateManager.checkIfConnectionIdSet(ctx, request);

        // add to queue if batchQueue has started
        if (this.shouldAddToBatch(request)) {

            this.addToBatchQueue(request);

            // the commands are queued until END_BATCH is received
            return;
        }

        // check if this is a start batchQueue message
        if (this.isStartBatch(request)) {
            this.createBatchQueue(request);
        } else if (this.isEndBatch(request)) {
            this.processBatchQueue(ctx);
        }

        // process regular request
        processRequest(ctx, request);
    }

    private void processRequest(ChannelHandlerContext ctx,
            KineticMessage request) throws InterruptedException {

        if (enforceOrdering) {
            // process request sequentially
            queuedRequestProcessRunner.processRequest(ctx, request);
        } else {
            // each request is independently processed
            RequestProcessRunner rpr = null;
            rpr = new RequestProcessRunner(lcservice, ctx, request);
            this.lcservice.execute(rpr);
        }
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.log(Level.WARNING, "Unexpected exception from downstream.",
				cause);

		// close process runner
		if (this.queuedRequestProcessRunner != null) {
			this.queuedRequestProcessRunner.close();
		}

		// close context
		ctx.close();
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
	    
	 // remove connection info of the channel handler context from conn info map
	    @SuppressWarnings("unused")
        ConnectionInfo info = SimulatorEngine.removeConnectionInfo(ctx);
	   
	    //logger.info("connection info is removed, id=" + info.getConnectionId() );
	    
		// close process runner
		if (this.queuedRequestProcessRunner != null) {
			logger.fine("removing/closing nio queued request process runner ...");
			this.queuedRequestProcessRunner.close();
		}
	}

    @SuppressWarnings("unused")
    private boolean isBatchMessage(KineticMessage request) {

        MessageType mtype = request.getCommand().getHeader().getMessageType();

        switch (mtype) {
        case START_BATCH:
        case END_BATCH:
            return true;
        default:
            return false;
        }
    }

    private boolean isStartBatch(KineticMessage request) {

        if (request.getCommand().getHeader().getMessageType() == MessageType.START_BATCH) {
            return true;
        }

        return false;
    }

    private boolean isEndBatch(KineticMessage request) {

        if (request.getCommand().getHeader().getMessageType() == MessageType.END_BATCH) {
            return true;
        }
        return false;
    }

    /**
     * check if batch has started.
     * 
     * @return
     */
    @SuppressWarnings("unused")
    private boolean isBatchStarted() {
        return (batchQueue != null);
    }

    private synchronized void addToBatchQueue(KineticMessage request) {
        batchQueue.add(request);
    }

    private synchronized void createBatchQueue(KineticMessage request) {
        if (batchQueue == null) {
            this.batchQueue = new BatchQueue(request);
        }
    }

    private boolean shouldAddToBatch(KineticMessage request) {

        boolean flag = false;

        if (batchQueue != null && batchQueue.isSameClient(request)) {

            MessageType mtype = request.getCommand().getHeader()
                    .getMessageType();

            if (mtype == MessageType.PUT || mtype == MessageType.DELETE) {
                flag = true;
            }
        }

        return flag;
    }

    private synchronized void processBatchQueue(ChannelHandlerContext ctx)
            throws InterruptedException {
        
        try {

            for (KineticMessage request : batchQueue.getMessageList()) {
                this.processRequest(ctx, request);
            }

        } finally {
            this.batchQueue = null;
        }
    }
}
