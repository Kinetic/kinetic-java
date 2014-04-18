/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */

package com.seagate.kinetic.simulator.io.provider.nio.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.logging.Logger;

import com.seagate.kinetic.common.protocol.codec.KineticDecoder;
import com.seagate.kinetic.common.protocol.codec.KineticEncoder;
import com.seagate.kinetic.proto.Kinetic;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;

public class NioChannelInitializer extends
ChannelInitializer<SocketChannel> {

	private final Logger logger = Logger
			.getLogger(NioChannelInitializer.class.getName());

	private MessageService lcservice = null;

	private boolean useV2Protocol = false;

	public NioChannelInitializer(MessageService lcservice2) {
		this.lcservice = lcservice2;
		this.useV2Protocol = this.lcservice.getServiceConfiguration()
				.getUseV2Protocol();
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		ChannelPipeline p = ch.pipeline();

		if (this.useV2Protocol) {
			// decoder
			p.addLast("decoder", new KineticDecoder());
			// encoder
			p.addLast("encoder", new KineticEncoder());
		} else {
			p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
			p.addLast("protobufDecoder",
					new ProtobufDecoder(Kinetic.Message.getDefaultInstance()));

			p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
			p.addLast("protobufEncoder", new ProtobufEncoder());
		}

		p.addLast("handler", new NioMessageServiceHandler(lcservice));

		logger.info("nio channel initialized., using v2 protocol="
				+ this.useV2Protocol);
	}
}
