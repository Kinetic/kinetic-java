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
package com.seagate.kinetic.simulator.io.provider.tcp;

import io.netty.handler.codec.CorruptedFrameException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.InvalidProtocolBufferException;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;
import com.seagate.kinetic.simulator.internal.MessageHandler;

/**
 *
 * TCP message service i/o handler.
 * <p>
 *
 * @author James Hughes
 * @author chiaming Yang
 *
 */
public class IoHandler implements Runnable {

	private final static Logger logger = Logger.getLogger(IoHandler.class
			.getName());

	private boolean useV2Protocol = true;

	// client paired socket
	private Socket socket = null;

	// input stream of the socket
	private InputStream is = null;

	// output stream of the socket
	private OutputStream os = null;

	// my io service
	private TcpTransportProvider ioService = null;

	// message handler.
	private MessageHandler msgHandler = null;

	/**
	 * Constructor
	 *
	 * @param s
	 *            client paired socket
	 * @param ioService
	 *            my io service
	 */
	IoHandler(Socket s, TcpTransportProvider ioService) {
		this.socket = s;
		this.ioService = ioService;

		this.useV2Protocol = true;
	}

	/**
	 * Get io service associated to this instance.
	 *
	 * @return io service associated to this instance
	 */
	public TcpTransportProvider getIoService() {
		return this.ioService;
	}

	/**
	 * Called by MessageHandler to send response message.
	 *
	 * @param out
	 *            response message.
	 */
	public void sendResponse(KineticMessage out) {
		try {

			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("writing respond message: " + out);
			}


			if (this.useV2Protocol) {

				try {

					// get value to write separately
					byte[] value = out.getValue();

					// write 9 byte header
					ByteArrayOutputStream baos = new ByteArrayOutputStream(9);
					DataOutputStream dos = new DataOutputStream(baos);

					// magic
					dos.writeByte((byte) 'F');
					// set value to an empty value
					// out.setValue(ByteString.EMPTY);

					// build message (without value) to write
					Message msg = ((Builder) out.getMessage()).build();

					// get proto message bytes
					byte[] protoMessageBytes = msg.toByteArray();

					// write message len
					dos.writeInt(protoMessageBytes.length);

					// write attached value size, 4 byte
					int valueLen = 0;
					if ((value != null)) {
						valueLen = value.length;
					}
					dos.writeInt(valueLen);

					dos.flush();

					// 9 byte header
					byte[] header = baos.toByteArray();

					dos.close();
					baos.close();

					// 1. write header bytes (9 bytes)
					os.write(header);

					// 2. write protobuf message byte[]
					os.write(protoMessageBytes);

					// 3 (optional) write attached value if any
					if (value != null && value.length > 0) {
						// write value
						os.write(value);
					}

					os.flush();

				} catch (IOException e) {
					logger.log(Level.WARNING, e.getMessage(), e);
					throw e;
				}

			} else {

				((Builder) out.getMessage()).build().writeDelimitedTo(os);
			}
		} catch (IOException e) {
			// LOG.severe("Sending Response failed");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// Message out = null;

		DataInputStream dis = null;

		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
			// LOG.fine("individual connection has streams");

			dis = new DataInputStream(is);

			this.msgHandler = new MessageHandler(this);

			// This will get individual commands and processes them.
			// it is either a blocking interface, or not. If process
			// message returns null, that routine must call
			// this.sendResponse()
			// themselves, possibly from anther thread.
			for (;;) {

				Message request = null;
				KineticMessage km = new KineticMessage();

				if (this.useV2Protocol) {

					// 1. Read magic number.
					int magicNumber = dis.readByte();

					if (magicNumber != 'F') {
						throw new CorruptedFrameException(
								"Invalid magic number: " + magicNumber);
					}

					// 2. protobuf message size
					int protoMessageLength = dis.readInt();

					// 3. attched value size
					int attachedValueLength = dis.readInt();

					// 4. read protobuf message
					byte[] decoded = new byte[protoMessageLength];
					dis.read(decoded);

					// construct protobuf message
					Message.Builder builder = Message.newBuilder();

					try {
						builder.mergeFrom(decoded);
					} catch (Exception e) {
						logger.log(Level.WARNING, e.getMessage(), e);

						throw new RuntimeException(e);
					}

					// 5. read attched value if any
					if (attachedValueLength > 0) {
						// construct byte[]
						byte[] attachedValue = new byte[attachedValueLength];
						// read from buffer
						dis.read(attachedValue);
						// set to message
						// builder.setValue(ByteString.copyFrom(attachedValue));
						km.setValue(attachedValue);
					}

					// v2 message
					request = builder.build();
				} else {
					request = Message.parseDelimitedFrom(is);
				}

				if (request == null) {
					break;
				}
				
				// build command
                Command.Builder commandBuilder = Command.newBuilder();

                try {
                    commandBuilder.mergeFrom(request.getCommandBytes());
                    km.setCommand(commandBuilder.build());
                } catch (InvalidProtocolBufferException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }

				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("received request message: " + request);
				}

				km.setMessage(request);
				this.msgHandler.processRequest(km);
			}
		} catch (Exception e) {

			if (e instanceof EOFException) {
				logger.fine(e.getMessage());
			} else {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}

		try {

			if (this.msgHandler != null) {
				this.msgHandler.close();
			}

			if (is != null) {
				is.close();
				os.close();
			}

			if (dis != null) {
				dis.close();
			}

			socket.close();
			// LOG.fine("Individual connection closed");
		} catch (IOException e) {
			// LOG.severe("individual connection close failed");
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		logger.info("IoHandler closed, ioservice=" + this.ioService.getName()
				+ ", use v2 protocol=" + this.useV2Protocol);
	}

}
