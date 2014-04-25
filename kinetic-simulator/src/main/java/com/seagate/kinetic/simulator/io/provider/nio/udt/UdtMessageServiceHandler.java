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
