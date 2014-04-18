/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.io.provider.nio.udt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.udt.UdtMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

/**
 * Please note: This class is for evaluation only and in prototype state.
 * 
 * @author chiaming
 * 
 */
public class UdtMessageServiceHandler extends
SimpleChannelInboundHandler<UdtMessage> {

	private static final Logger logger = Logger
			.getLogger(UdtMessageServiceHandler.class.getName());

	private MessageService lcservice = null;

	private boolean enforceOrdering = false;

	private UdtQueuedRequestProcessRunner queuedRequestProcessRunner = null;

	public UdtMessageServiceHandler(MessageService lcservice2) {
		this.lcservice = lcservice2;

		this.enforceOrdering = lcservice.getServiceConfiguration()
				.getMessageOrderingEnforced();

		if (this.enforceOrdering) {
			this.queuedRequestProcessRunner = new UdtQueuedRequestProcessRunner(
					lcservice);

			logger.info("**** UDT message order enforced ...");
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, UdtMessage request)
			throws Exception {

		byte[] dst = new byte[request.content().readableBytes()];
		request.content().getBytes(0, dst);

		if (enforceOrdering) {
			// process request sequentially
			queuedRequestProcessRunner.processRequest(ctx, dst);
		} else {

			// new instance of process runner
			UdtRequestProcessRunner rpr = new UdtRequestProcessRunner(lcservice,
					ctx, dst);
			// run it
			lcservice.execute(rpr);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {

		logger.log(Level.WARNING, "Unexpected exception from downstream.");

		ctx.close();
	}

}
