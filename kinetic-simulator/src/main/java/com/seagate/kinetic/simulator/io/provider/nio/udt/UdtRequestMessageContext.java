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
package com.seagate.kinetic.simulator.io.provider.nio.udt;

import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * Class to hold ChannelHandlerContext and request message for simulator nio
 * service.
 * 
 * Please note: This class is for evaluation only and in prototype state.
 * 
 * @author chiaming
 * 
 */
public class UdtRequestMessageContext {

	private ChannelHandlerContext ctx = null;
	private byte[] request = null;

	public UdtRequestMessageContext(ChannelHandlerContext ctx,
			byte[] request) {
		this.ctx = ctx;
		this.request = request;
	}

	public ChannelHandlerContext getChannelHandlerContext() {
		return this.ctx;
	}

	public byte[] getRequestMessage() {
		return this.request;
	}
}
