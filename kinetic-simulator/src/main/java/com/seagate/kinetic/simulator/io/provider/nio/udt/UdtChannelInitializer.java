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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.udt.UdtChannel;

import java.util.logging.Logger;

import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

/**
 * 
 * Please note: This class is for evaluation only and in prototype state.
 * 
 * @author chiaming
 * 
 */
public class UdtChannelInitializer extends ChannelInitializer<UdtChannel> {

	private final Logger logger = Logger
			.getLogger(UdtChannelInitializer.class.getName());

	private MessageService lcservice = null;

	public UdtChannelInitializer(MessageService lcservice2) {
		this.lcservice = lcservice2;
	}

	@Override
	protected void initChannel(UdtChannel ch) throws Exception {

		ChannelPipeline p = ch.pipeline();

		p.addLast("handler", new UdtMessageServiceHandler(lcservice));

		logger.info("UDT nio channel initialized ...");
	}
}
