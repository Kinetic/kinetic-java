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

import com.google.protobuf.InvalidProtocolBufferException;
import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.client.io.provider.spi.ClientTransportProvider;
import com.seagate.kinetic.common.lib.KineticMessage;

import com.seagate.kinetic.proto.Kinetic.Command;
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

			dis = new DataInputStream(is);
			
			os = socket.getOutputStream();

			this.myThread = new Thread(this);

			this.myThread.setName("IoHandler-" + config.getHost() + "-"
					+ config.getPort());

			this.myThread.start();

			logger.info("tcp-non-nio transport initialized ...");

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
    public synchronized void write(KineticMessage km) throws IOException {

        Message.Builder message = (Builder) km.getMessage();

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("writing message: " + message);
        }

        try {

            // get value to write separately
            byte[] value = km.getValue();

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
			this.dis.close();
			this.os.close();
			this.socket.close();

			logger.info("tcp non-nio transport closed ...");
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	@Override
    public void run() {

        while (this.isRunning) {

            try {

                Message message = null;
                
                KineticMessage km = new KineticMessage();

                // 1. Read magic number.
                int magicNumber = dis.readByte();

                if (magicNumber != 'F') {
                    throw new CorruptedFrameException("Invalid magic number: "
                            + magicNumber);
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

                message = builder.build();

                // set message to kietic message
                km.setMessage(message);

                // build command
                Command.Builder commandBuilder = Command.newBuilder();

                try {
                    commandBuilder.mergeFrom(message.getCommandBytes());
                    km.setCommand(commandBuilder.build());
                } catch (InvalidProtocolBufferException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }

                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("read message: " + message);
                }

                this.mservice.routeMessage(km);
            } catch (Exception e) {
                this.isRunning = false;
            }
        }
    }

}
