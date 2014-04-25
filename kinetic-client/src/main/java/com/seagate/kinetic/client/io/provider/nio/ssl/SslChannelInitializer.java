/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.seagate.kinetic.client.io.provider.nio.ssl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import io.netty.handler.ssl.SslHandler;

import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.common.lib.TlsUtil;
import com.seagate.kinetic.common.protocol.codec.KineticDecoder;
import com.seagate.kinetic.common.protocol.codec.KineticEncoder;
//import com.seagate.kinetic.proto.Kinetic;

/**
 *
 * @author chiaming
 *
 */
public class SslChannelInitializer extends
ChannelInitializer<SocketChannel> {

	private static final Logger logger = Logger
			.getLogger(SslChannelInitializer.class.getName());

	private ClientMessageService mservice = null;

	public SslChannelInitializer(ClientMessageService mservice) {
		this.mservice = mservice;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		if (mservice.getConfiguration().getConnectTimeoutMillis() > 0) {
			ch.config()
			.setConnectTimeoutMillis(
					mservice.getConfiguration()
					.getConnectTimeoutMillis());
		}

		ChannelPipeline pipeline = ch.pipeline();

		SSLEngine engine = SslContextFactory.getClientContext()
				.createSSLEngine();

		engine.setUseClientMode(true);

		/**
		 * enable TLS V1.x protocols.
		 */
		TlsUtil.enableSupportedProtocols(engine);

		// add ssl handler
		pipeline.addLast("ssl", new SslHandler(engine));

		// decoder
		pipeline.addLast("decoder", new KineticDecoder());
		// encoder
		pipeline.addLast("encoder", new KineticEncoder());
		
		pipeline.addLast("handler", new SslMessageServiceHandler(mservice));

		logger.info("ssl channel initialized ... ");
	}
}
