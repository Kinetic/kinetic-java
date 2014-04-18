/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.io.provider.tcp;

import io.netty.handler.codec.CorruptedFrameException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.client.io.provider.spi.ClientTransportProvider;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Builder;

/**
 *
 * Tcp transport provider.This class provides TCP transport support for the
 * Kinetic client runtime.
 * <p>
 *
 * @see com.seagate.kinetic.client.io.provider.nio.tcp.TcpNioTransportProvider
 *
 * @author James Hughes
 * @author chiaming yang
 */
public class TcpTransportProvider implements ClientTransportProvider, Runnable {

	// my logger
	private final Logger logger = Logger
			.getLogger(TcpTransportProvider.class.getName());

	private boolean useV2Protocol = false;

	// input socket read thread
	private Thread myThread = null;

	// client socket
	private Socket socket = null;

	// input stream
	private InputStream is = null;

	// for v2 protocol
	DataInputStream dis = null;

	// output stream
	private OutputStream os = null;

	// flag running flag
	private volatile boolean isRunning = true;

	private ClientMessageService mservice = null;

	public TcpTransportProvider() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(ClientMessageService mservice)
			throws KineticException {

		ClientConfiguration config = mservice.getConfiguration();

		this.useV2Protocol = config.getUseV2Protocol();

		this.mservice = mservice;

		/**
		 * Create socket to server and get I/O streams.
		 *
		 * @throws KineticException
		 *             if any I/O or internal exception occurred.
		 */
		try {

			SocketAddress address = new InetSocketAddress(config.getHost(),
					config.getPort());

			socket = new Socket();

			socket.connect(address, config.getConnectTimeoutMillis());

			is = socket.getInputStream();

			if (this.useV2Protocol) {
				dis = new DataInputStream(is);
			}

			os = socket.getOutputStream();

			this.myThread = new Thread(this);

			this.myThread.setName("IoHandler-" + config.getHost() + "-"
					+ config.getPort());

			this.myThread.start();

			logger.info("tcp-non-nio is used, use v2 protocol = "
					+ this.useV2Protocol);

		} catch (IOException e) {

			this.close();

			logger.log(Level.SEVERE, e.getMessage(), e);

			throw new KineticException(e);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void write(KineticMessage im) throws IOException {

		Message.Builder message = (Builder) im.getMessage();

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("writing message: " + message);
		}

		if (this.useV2Protocol) {

			try {

				// get value to write separately
				byte[] value = im.getValue();

				// write 9 byte header
				ByteArrayOutputStream baos = new ByteArrayOutputStream(9);
				DataOutputStream dos = new DataOutputStream(baos);

				// magic
				dos.writeByte((byte) 'F');
				// set value to an empty value
				// message.setValue(ByteString.EMPTY);

				// build message (without value) to write
				Message msg = message.build();

				// get proto message bytes
				byte[] protoMessageBytes = msg.toByteArray();

				// write message len
				dos.writeInt(protoMessageBytes.length);

				// write attached value size, 4 byte
				int valueLen = 0;
				if (value != null) {
					valueLen = value.length;
				}
				dos.writeInt(valueLen);

				dos.flush();

				// 9 byte header
				byte[] beader = baos.toByteArray();

				dos.close();
				baos.close();

				// 1. write header bytes (9 bytes)
				os.write(beader);

				// 2. write protobuf message byte[]
				os.write(protoMessageBytes);

				// 3 (optional) write attached value if any
				if (valueLen > 0) {
					// write value
					os.write(value);
				}

				os.flush();

			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				throw e;
			}

		} else {
			message.build().writeDelimitedTo(os);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {

		try {
			this.isRunning = false;
			this.mservice.close();
			this.is.close();

			if (this.useV2Protocol) {
				this.dis.close();
			}

			this.os.close();
			this.socket.close();

			logger.info("tcp non-nio transport closed, use v2 protocol="
					+ this.useV2Protocol);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	@Override
	public void run() {

		while (this.isRunning) {

			try {

				Message message = null;
				KineticMessage im = new KineticMessage();

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
						im.setValue(attachedValue);
					}

					// v2 message
					message = builder.build();
				} else {
					// v1 message
					message = Kinetic.Message.parseDelimitedFrom(is);
				}

				// set message to kietic message
				im.setMessage(message);

				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("read message: " + message);
				}

				this.mservice.routeMessage(im);
			} catch (Exception e) {
				this.isRunning = false;
			}
		}
	}

}
