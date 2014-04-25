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
