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
package com.seagate.kinetic.simulator.io.provider.nio.tcp;

import io.netty.channel.ChannelHandlerContext;

import com.seagate.kinetic.common.lib.KineticMessage;

/**
 *
 * Class to hold ChannelHandlerContext and request message for simulator nio
 * service.
 *
 * @author chiaming
 *
 */
public class NioRequestMessageContext {

	private ChannelHandlerContext ctx = null;
	private KineticMessage request = null;

	public NioRequestMessageContext(ChannelHandlerContext ctx,
			KineticMessage request) {
		this.ctx = ctx;
		this.request = request;
	}

	public ChannelHandlerContext getChannelHandlerContext() {
		return this.ctx;
	}

	public KineticMessage getRequestMessage() {
		return this.request;
	}
}
