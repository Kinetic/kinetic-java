/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */

package com.seagate.kinetic.simulator.io.provider.nio.ssl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslHandler;

import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import com.seagate.kinetic.common.lib.TlsUtil;
import com.seagate.kinetic.common.protocol.codec.KineticDecoder;
import com.seagate.kinetic.common.protocol.codec.KineticEncoder;
import com.seagate.kinetic.proto.Kinetic;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

public class SslChannelInitializer extends
ChannelInitializer<SocketChannel> {

	private static final Logger logger = Logger
			.getLogger(SslChannelInitializer.class.getName());

	private MessageService lcservice = null;

	private boolean useV2Protocol = false;

	public SslChannelInitializer(MessageService lcservice2) {
		this.lcservice = lcservice2;
		this.useV2Protocol = this.lcservice.getServiceConfiguration()
				.getUseV2Protocol();
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		ChannelPipeline pipeline = ch.pipeline();

		SSLEngine engine = SslContextFactory.getServerContext()
				.createSSLEngine();

		engine.setUseClientMode(false);

		// enable TLS v1.x protocols.
		TlsUtil.enableSupportedProtocols(engine);

		// add ssl handler
		pipeline.addLast("ssl", new SslHandler(engine));

		if (this.useV2Protocol) {
			// decoder
			pipeline.addLast("decoder", new KineticDecoder());
			// encoder
			pipeline.addLast("encoder", new KineticEncoder());
		} else {

			pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
			pipeline.addLast("protobufDecoder",
					new ProtobufDecoder(Kinetic.Message.getDefaultInstance()));

			pipeline.addLast("frameEncoder",
					new ProtobufVarint32LengthFieldPrepender());
			pipeline.addLast("protobufEncoder", new ProtobufEncoder());
		}

		pipeline.addLast("handler", new SslMessageServiceHandler(lcservice));

		logger.info("ssl nio channel initialized, use v2 protocol = "
				+ this.useV2Protocol);
	}
}
