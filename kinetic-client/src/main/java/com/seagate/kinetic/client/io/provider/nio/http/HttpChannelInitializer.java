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

package com.seagate.kinetic.client.io.provider.nio.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;

import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import com.seagate.kinetic.client.io.provider.nio.ssl.SslContextFactory;
import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;

/**
 * Http transport prototype.
 *
 * @author chiaming
 *
 */
public class HttpChannelInitializer extends
ChannelInitializer<SocketChannel> {

	private final Logger logger = Logger
			.getLogger(HttpChannelInitializer.class.getName());

	private ClientMessageService mservice = null;

	public HttpChannelInitializer(ClientMessageService mservice) {
		this.mservice = mservice;

	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		boolean ssl = Boolean.getBoolean("kinetic.io.https");

		ChannelPipeline p = ch.pipeline();

		// Enable HTTPS if necessary.
		if (ssl) {
			SSLEngine engine = SslContextFactory.getClientContext()
					.createSSLEngine();

			engine.setUseClientMode(true);

			p.addLast("ssl", new SslHandler(engine));
		}

		p.addLast("codec", new HttpClientCodec(1024, 4 * 1024, 4 * 1024 * 1024));

		p.addLast("aggregator", new HttpObjectAggregator(4 * 1024 * 1024));

		p.addLast("handler", new HttpMessageServiceHandler(mservice));

		logger.info("http/s channel initialized, use ssl handler=" + ssl);
	}
}
