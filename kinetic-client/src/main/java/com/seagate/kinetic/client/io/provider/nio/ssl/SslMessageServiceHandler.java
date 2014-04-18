/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.io.provider.nio.ssl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.common.lib.KineticMessage;

/**
 * Kinetic SSL message service handler.
 *
 * @author chiaming
 *
 */
public class SslMessageServiceHandler extends
SimpleChannelInboundHandler<KineticMessage> {

	private static final Logger logger = Logger
			.getLogger(SslMessageServiceHandler.class.getName());

	private ClientMessageService mservice = null;

	public SslMessageServiceHandler(ClientMessageService mservice) {
		this.mservice = mservice;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx,
			KineticMessage message)
					throws Exception {
		// logger.info("ssl channel received message: " + message);

		this.mservice.routeMessage(message);
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
		ctx.close();

		this.mservice.close();
	}

}
