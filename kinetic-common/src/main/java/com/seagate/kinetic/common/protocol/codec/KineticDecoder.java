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
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.common.lib.ProtocolMessageUtil;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Message;

/**
 *
 * Kinetic protocol buffer message decoder (version2 protocol).
 * <p>
 * The client library, simulator, or a Kinetic drive uses the following protocol
 * to decode a message.
 *
 * <ul>
 * <li>1. read magic byte 'F'
 * <li>2. read protobuf message length.
 * <li>3. read value length (0 if no value)
 * <li>4. read protobuf message byte[]
 * <li>5. read value byte[] if any
 * </ul>
 * <p>
 * To log/print the decoded message, set the "kinetic.io.in" Java System
 * property to true.
 * <p>
 * For example, set the following Java VM argument when running a Kinetic
 * application or Simulator.
 * <p>
 * -D"kinetic.io.in"=true
 *
 * @author chiaming
 */
public class KineticDecoder extends ByteToMessageDecoder {

	private final Logger logger = Logger.getLogger(KineticDecoder.class
			.getName());

	private static boolean printMessage = Boolean.getBoolean("kinetic.io.in");

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) {

		in.markReaderIndex();

		// Wait until the length prefix is available
		// (magic ('F') + proto-msg-size + value-size)
		if (in.readableBytes() < 9) {
			in.resetReaderIndex();
			return;
		}

		// 1. Read magic number.
		int magicNumber = in.readUnsignedByte();
		if (magicNumber != 'F') {
			in.resetReaderIndex();
			throw new CorruptedFrameException("Invalid magic number: "
					+ magicNumber);
		}

		// 2. protobuf message size
		int protoMessageLength = in.readInt();

		// 3. attched value size
		int attachedValueLength = in.readInt();

		// wait until whole message is available
		if (in.readableBytes() < (protoMessageLength + attachedValueLength)) {
			in.resetReaderIndex();
			return;
		}

		// 4. read protobuf message
		byte[] decoded = new byte[protoMessageLength];
		in.readBytes(decoded);

		// kinetic message
		KineticMessage km = new KineticMessage();

		// construct protobuf message
		Message.Builder mbuilder = Message.newBuilder();

		try {
			mbuilder.mergeFrom(decoded);
		} catch (Exception e) {
			in.resetReaderIndex();

			logger.log(Level.WARNING, e.getMessage(), e);

			throw new RuntimeException(e);
		}

		// 5. read attched value if any
		if (attachedValueLength > 0) {
			// construct byte[]
			byte[] attachedValue = new byte[attachedValueLength];
			// read from buffer
			in.readBytes(attachedValue);
			// set to message
			// mbuilder.setValue(ByteString.copyFrom(attachedValue));
			km.setValue(attachedValue);
		}

		Message message = mbuilder.build();

		km.setMessage(message);
		
		// get command bytes
		ByteString commandBytes = message.getCommandBytes();
		
		// build command
		Command.Builder commandBuilder = Command.newBuilder();
		
		try {
            commandBuilder.mergeFrom(commandBytes);
            km.setCommand(commandBuilder.build());
        } catch (InvalidProtocolBufferException e) {
           logger.log(Level.WARNING, e.getMessage(), e);
        }

		// the whole message
		out.add(km);

		// print inbound message
		if (printMessage) {

			logger.info("Inbound protocol message: ");

			String printMsg = ProtocolMessageUtil.toString(km);

			logger.info(printMsg);
		}
	}
}

