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
