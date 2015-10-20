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

package com.seagate.kinetic.simulator.io.provider.nio.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;

import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import com.seagate.kinetic.simulator.io.provider.nio.ssl.SslContextFactory;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

/**
 * 
 * Http transport prototype.
 * 
 * @author chiaming
 * 
 */
public class HttpChannelInitializer extends
ChannelInitializer<SocketChannel> {

	private final Logger logger = Logger
			.getLogger(HttpChannelInitializer.class.getName());

	private MessageService lcservice = null;

	public HttpChannelInitializer(MessageService lcservice2) {
		this.lcservice = lcservice2;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		ChannelPipeline p = ch.pipeline();

		boolean isSsl = Boolean.getBoolean("kinetic.io.https");

		if (isSsl) {
			SSLEngine engine = SslContextFactory.getServerContext()
					.createSSLEngine();

			engine.setUseClientMode(false);

			p.addLast("ssl", new SslHandler(engine));

			logger.info("ssl handler added, https is enabled ...");
		}

		p.addLast("decoder", new HttpRequestDecoder(1024, 4 * 1024,
				4 * 1024 * 1024));

		p.addLast("encoder", new HttpResponseEncoder());

		p.addLast("aggregator", new HttpObjectAggregator(4 * 1024 * 1024));

		p.addLast("handler", new HttpMessageServiceHandler(lcservice));

		logger.info("http channel initialized. ssl/tls enabled=" + isSsl);
	}
}
