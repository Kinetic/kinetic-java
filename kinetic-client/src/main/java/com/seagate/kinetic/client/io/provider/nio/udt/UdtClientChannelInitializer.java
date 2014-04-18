/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */

package com.seagate.kinetic.client.io.provider.nio.udt;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.udt.UdtChannel;

import java.util.logging.Logger;

import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;

/**
 *
 * Please note: This class is for evaluation only and in prototype state.
 *
 * @author chiaming
 *
 */
public class UdtClientChannelInitializer extends
ChannelInitializer<UdtChannel> {

	private final Logger logger = Logger
			.getLogger(UdtClientChannelInitializer.class.getName());

	private ClientMessageService mservice = null;

	public UdtClientChannelInitializer(ClientMessageService mservice) {
		this.mservice = mservice;
	}

	@Override
	protected void initChannel(UdtChannel ch) throws Exception {

		if (mservice.getConfiguration().getConnectTimeoutMillis() > 0) {
			ch.config()
			.setConnectTimeoutMillis(
					mservice.getConfiguration()
					.getConnectTimeoutMillis());
		}

		ChannelPipeline p = ch.pipeline();

		p.addLast("handler", new UdtClientMessageServiceHandler(mservice));

		logger.info("UDT client nio channel initilized ...");
	}
}
