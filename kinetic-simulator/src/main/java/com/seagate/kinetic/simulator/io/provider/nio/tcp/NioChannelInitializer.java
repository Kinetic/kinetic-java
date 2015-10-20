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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.logging.Logger;

import com.seagate.kinetic.common.protocol.codec.KineticDecoder;
import com.seagate.kinetic.common.protocol.codec.KineticEncoder;
import com.seagate.kinetic.simulator.io.provider.nio.NioMessageServiceHandler;
//import com.seagate.kinetic.proto.Kinetic;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

public class NioChannelInitializer extends
ChannelInitializer<SocketChannel> {

	private final Logger logger = Logger
			.getLogger(NioChannelInitializer.class.getName());

	private MessageService lcservice = null;

	public NioChannelInitializer(MessageService lcservice2) {
		this.lcservice = lcservice2;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		ChannelPipeline p = ch.pipeline();

		
			// decoder
			p.addLast("decoder", new KineticDecoder());
			// encoder
			p.addLast("encoder", new KineticEncoder());
		

        p.addLast("handler", new NioMessageServiceHandler(lcservice, false));

        logger.info("nio channel initialized., is secure channel=false");
	}
}
