/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.io.provider.nio.ssl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.simulator.io.provider.nio.NioQueuedRequestProcessRunner;
import com.seagate.kinetic.simulator.io.provider.nio.RequestProcessRunner;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

/**
 *
 * @author chiaming
 *
 */
public class SslMessageServiceHandler extends
		SimpleChannelInboundHandler<KineticMessage> {

	private static final Logger logger = Logger
			.getLogger(SslMessageServiceHandler.class.getName());

	private MessageService lcservice = null;

	private boolean enforceOrdering = false;

	private NioQueuedRequestProcessRunner queuedRequestProcessRunner = null;

	public SslMessageServiceHandler(MessageService lcservice2) {
		this.lcservice = lcservice2;

		this.enforceOrdering = lcservice.getServiceConfiguration()
				.getMessageOrderingEnforced();

		if (this.enforceOrdering) {
			this.queuedRequestProcessRunner = new NioQueuedRequestProcessRunner(
					lcservice);
		}
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		logger.fine("Kinetic ssl channel is active ...");
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx,
			KineticMessage request)
			throws Exception {

		if (enforceOrdering) {
			// process request sequentially
			queuedRequestProcessRunner.processRequest(ctx, request);
		} else {
			RequestProcessRunner rpr = new RequestProcessRunner(lcservice, ctx,
					request);
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

		if (this.queuedRequestProcessRunner != null) {
			logger.info("removing/closing ssl nio queued request process runner ...");
			this.queuedRequestProcessRunner.close();
		}
	}

}
