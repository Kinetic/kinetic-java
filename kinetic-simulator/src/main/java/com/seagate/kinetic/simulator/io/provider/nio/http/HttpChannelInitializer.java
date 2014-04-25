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
