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
package com.seagate.kinetic.client.io;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

import com.seagate.kinetic.client.internal.ClientProxy;
import com.seagate.kinetic.client.io.provider.spi.ClientTransportProvider;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic;

/**
 *
 * Kinetic client I/O handler.
 * <p>
 *
 * @author James Hughes
 * @author Chiaming Yang
 *
 */
public class IoHandler {

	// my logger
	private final Logger logger = Logger.getLogger(IoHandler.class.getName());

	// client configuration
	private ClientConfiguration config = null;

	// Transpoprt
	private ClientTransportProvider transport = null;

	private MessageHandler messageHandler = null;

	private ClientProxy client = null;

	private final boolean useHttp = Boolean.getBoolean("kinetic.io.http");

	private final boolean useHttps = Boolean.getBoolean("kinetic.io.https");

	// udt transport
	private final boolean useUdt = Boolean.getBoolean("kinetic.io.udt");

	// define this to load the user defined transport provider
	private final boolean isLoadTransportPlugIn = Boolean
			.getBoolean("kinetic.client.io.plugin");

	// if isLoadTransportPlugIn is defined to true, specified the transport
	// provider class name with the following property.
	private final String TRANSPORT_PLUGIN_CLASS = "kinetic.client.io.plugin.class";

	private static final String UDT_TRANSPORT = "com.seagate.kinetic.client.io.provider.nio.udt.UdtTransportProvider";

	private static final String HTTP_TRANSPORT = "com.seagate.kinetic.client.io.provider.nio.http.HttpTransportProvider";

	private static final String TCP_TRANSPORT = "com.seagate.kinetic.client.io.provider.tcp.TcpTransportProvider";

	private static final String TCP_NIO_TRANSPORT = "com.seagate.kinetic.client.io.provider.nio.tcp.TcpNioTransportProvider";

	private static final String SSL_NIO_TRANSPORT = "com.seagate.kinetic.client.io.provider.nio.ssl.SslNioTransportProvider";

	/**
	 * Construct a new instance of IoHandle with the specified configuration.
	 *
	 * @param config
	 *            client configuration.
	 *
	 * @throws KineticException
	 *             if any I/O or internal exception occurred.
	 */
	public IoHandler(ClientProxy client)
			throws KineticException {

		// kinetic client proxy
		this.client = client;

		// config
		this.config = client.getConfiguration();

		// initialize
		this.init();
	}

	/**
	 * Create socket to server and get I/O streams.
	 *
	 * @throws KineticException
	 *             if any I/O or internal exception occurred.
	 */
    private void init() throws KineticException {

        try {
            // get the transport provider for this kinetic instance.
            getTransport();

            // message handler to route messages
            this.messageHandler = new MessageHandler(this);

            // init transport
            this.transport.init(messageHandler);
            
            logger.info("connected to device., protocol version: " + Kinetic.Local.getDefaultInstance().getProtocolVersion());
        } catch (KineticException ke) {
            close();
            throw ke;
        } catch (Exception e) {
            close();
            throw new KineticException(e);
        }
    }

	public ClientProxy getClient() {
		return this.client;
	}

	public MessageHandler getMessageHandler() {
		return this.messageHandler;
	}

	/**
	 * Write message to the binded transport.
	 *
	 * @param message
	 *            out bound message.
	 *
	 * @throws IOException
	 *             if I/O failed.
	 */
	public synchronized void write(KineticMessage message) throws IOException {

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("writing message: " + message.getMessage());
		}

		this.transport.write(message);
	}

	/**
	 * Close the I/O handler.
	 */
	public void close() {

		try {
			this.messageHandler.close();
			this.transport.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public synchronized ClientTransportProvider getTransport()
			throws KineticException {

		// instantiate a new instance of transport if none exists yet.
		if (transport == null) {

			// load transport provider with the defined plug-in class name
			if (this.isLoadTransportPlugIn) {
				logger.info("using plug-in transport., class name ="
						+ TRANSPORT_PLUGIN_CLASS);
				String className = this.config
						.getProperty(TRANSPORT_PLUGIN_CLASS);
				this.transport = this.loadTransportProvider(className);
			} else if (config.getUseSsl()) {
				logger.info("using ssl transport ...");
				this.transport = this.loadTransportProvider(SSL_NIO_TRANSPORT);
			} else if (useUdt) {
				logger.info("using udt transport ....");
				transport = this.loadTransportProvider(UDT_TRANSPORT);
			} else if (useHttp) {
				logger.info("using http transport ....");
				transport = this.loadTransportProvider(HTTP_TRANSPORT);
			} else if (useHttps) {
				logger.info("using https transport ....");
				transport = this.loadTransportProvider(HTTP_TRANSPORT);
			} else if (config.getUseNio()) {
				logger.info("using Java NIO TCP transport ....");
				transport = this.loadTransportProvider(TCP_NIO_TRANSPORT);
			} else {
				logger.info("using TCP transport ....");
				transport = this.loadTransportProvider(TCP_TRANSPORT);
			}
		}

		return transport;
	}

	/**
	 * Load transport provider class based on the specified class name.
	 * <p>
	 *
	 * @param className
	 *            the class full name used to load the transport provider
	 *            instance.
	 *
	 * @return a new instance of transport provider.
	 */
	private ClientTransportProvider loadTransportProvider(String className)
			throws KineticException {

		ClientTransportProvider provider = null;

		try {
			provider = (ClientTransportProvider) Class.forName(className)
					.newInstance();

			logger.info("loaded transport provider class., name=" + className);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);

			throw new KineticException(e);
		}

		return provider;
	}
	
	public boolean shouldWaitForStatusMessage() {
	    
	    if (this.useHttp || this.useHttps || this.useUdt) {
	        return false;
	    }
	    
	    return true;
	}

}
