/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
