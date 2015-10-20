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
