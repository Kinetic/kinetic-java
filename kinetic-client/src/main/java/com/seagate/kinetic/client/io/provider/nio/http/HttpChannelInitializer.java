/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
