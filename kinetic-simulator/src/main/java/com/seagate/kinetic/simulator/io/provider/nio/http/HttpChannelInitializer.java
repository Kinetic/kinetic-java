/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
