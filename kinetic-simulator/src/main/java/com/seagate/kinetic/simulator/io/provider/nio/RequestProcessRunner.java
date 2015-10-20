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
