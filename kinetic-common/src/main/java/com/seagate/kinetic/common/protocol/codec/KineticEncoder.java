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
package com.seagate.kinetic.common.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.common.lib.ProtocolMessageUtil;

import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;

/**
 *
 * Kinetic protocol buffer message encoder (version2 protocol).
 * <p>
 * The client library, simulator, or a Kinetic drive uses the following protocol
 * to encode a message.
 * <p>
 * <ul>
 * <li>1. write magic byte 'F'
 * <li>2. write protocol buffer message length.
 * <li>3. write value length (0 if no value)
 * <li>4. write protobuf message byte[]
 * <li>5. write value byte[] if any
 * </ul>
 * <p>
 *
 * To log/print the encoded message, set the "kinetic.io.out" Java System
 * property to true.
 * <p>
 * For example, set the following Java VM argument when running a Kinetic
 * application or Simulator.
 * <p>
 * -D"kinetic.io.out"=true
 *
 * @author chiaming
 */
public class KineticEncoder extends MessageToByteEncoder<KineticMessage> {

	private final Logger logger = Logger.getLogger(KineticEncoder.class
			.getName());

	private static boolean printMessage = Boolean.getBoolean("kinetic.io.out");

	@Override
	protected void encode(ChannelHandlerContext ctx, KineticMessage km,
			ByteBuf out) throws Exception {

		try {
	
			// get value to write separately
			// byte[] value = builder.getValue().toByteArray();
			byte[] value = km.getValue();

			// 1. write magic number
			out.writeByte((byte) 'F');
			
			//get message
			Message.Builder messageBuilder = (Builder) km.getMessage();
			
			// build message
			Message msg = messageBuilder.build();

			// get proto message bytes
			byte[] protoMessageBytes = msg.toByteArray();

			// 2. write protobuf message message size, 4 byte
			out.writeInt(protoMessageBytes.length);

			// 3. write attached value size, 4 byte
			if (value != null) {
				out.writeInt(value.length);
			} else {
				out.writeInt(0);
			}

			// 4. write protobuf message byte[]
			out.writeBytes(protoMessageBytes);

			// 5 (optional) write attached value if any
			if (value != null && value.length > 0) {
				// write value
				out.writeBytes(value);
			}

			// log message out
			if (printMessage) {

				logger.info("outbound protocol message:");
				
				String printMsg = ProtocolMessageUtil.toString(km);

				logger.info(printMsg);
			}

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			throw e;
		}
	}

}
