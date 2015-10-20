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
