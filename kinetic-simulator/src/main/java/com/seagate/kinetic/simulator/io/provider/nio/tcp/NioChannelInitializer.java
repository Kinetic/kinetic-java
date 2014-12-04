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
