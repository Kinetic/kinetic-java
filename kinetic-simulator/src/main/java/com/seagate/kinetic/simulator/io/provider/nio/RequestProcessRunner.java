/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.simulator.io.provider.nio;

import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

public class RequestProcessRunner implements Runnable {

	private static final Logger logger = Logger
			.getLogger(RequestProcessRunner.class.getName());

	private MessageService service = null;
	private ChannelHandlerContext ctx = null;
	private KineticMessage request = null;

	public RequestProcessRunner(MessageService service,
			ChannelHandlerContext ctx, KineticMessage request) {
		this.service = service;
		this.ctx = ctx;
		this.request = request;
	}

	@Override
	public void run() {

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("received request: " + request);
		}

		KineticMessage response = this.service.processRequest(request);

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("writing response: "
					+ ((Message.Builder) response.getMessage()).build());
		}

		ctx.writeAndFlush(response);
	}

}
