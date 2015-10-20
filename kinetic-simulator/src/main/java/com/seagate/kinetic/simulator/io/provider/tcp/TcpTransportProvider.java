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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.simulator.io.provider.spi.MessageService;
import com.seagate.kinetic.simulator.io.provider.spi.TransportProvider;

/**
 * 
 * TCP transport i/o service.
 * <p>
 * This instantiates Thread Pooling and network server socket service.
 * 
 * @author James Hughes
 * @author chiaming Yang
 * 
 */
public class TcpTransportProvider implements TransportProvider, Runnable {

	// my logger
	private final static Logger logger = Logger.getLogger(TcpTransportProvider.class
			.getName());

	// wait termination time in seconds
	private static final int WAIT_TERMINATION_TIME = 7;

	// my master server
	private MessageService service = null;

	// server socket listening to client request
	private ServerSocket serverSocket = null;

	// thread pool service
	private ExecutorService pool = null;

	// I/O service thread (server socket)
	private Thread myThread = null;

	// my name
	private String myName = null;

	/**
	 * constructor.
	 * 
	 * @param simulatorEngine
	 *            master server
	 * 
	 * @throws IOException
	 *             if unable to bind to a service port.
	 */
	public TcpTransportProvider() {
	}

	/**
	 * init thread pool and server socket.
	 * 
	 * @throws IOException
	 *             if unable to create a server socket.
	 */
	private void doInit() throws IOException {

		// my thread pool
		pool = Executors.newCachedThreadPool(new ServiceThreadFactory(service
				.getServiceConfiguration()));
		try {

			// create a new server socket
			serverSocket = new ServerSocket(service.getServiceConfiguration()
					.getPort());

			// my own thread
			myThread = new Thread(this);

			// create my name
			myName = "IoService" + "-"
					+ this.service.getServiceConfiguration().getPort() + "-main";

			// set my thread name so that I can see it.
			myThread.setName(myName);

			// start the i/o service
			myThread.start();

		} catch (IOException e) {
			close();
			throw e;
		}
	}

	/**
	 * server socket service
	 */
	@Override
	public void run() {

		logger.info("starting io service, port="
				+ this.service.getServiceConfiguration().getPort());

		try {
			for (;;) {

				// accept socket request
				Socket socket = serverSocket.accept();

				if (socket != null) {
					// handle socket request
					pool.execute(new IoHandler(socket, this));
				}
			}
		} catch (java.net.SocketException e) {
			logger.log(Level.FINE, e.getMessage(), e);
			close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			close();
		} finally {
			;
		}
	}

	/**
	 * Get thread pool service.
	 * 
	 * @return my thread pool service
	 */
	public ExecutorService getPool() {
		return this.pool;
	}

	/**
	 * Get master server service.
	 * 
	 * @return master server service.
	 */
	public MessageService getMessageService() {
		return this.service;
	}

	/**
	 * shutdown thread pool and server socket.
	 */
	@Override
	public void close() {
		;
	}

	private void shutdownAndAwaitTermination() {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(WAIT_TERMINATION_TIME, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(WAIT_TERMINATION_TIME,
						TimeUnit.SECONDS)) {
					logger.warning("Pool did not terminate");
				} else {
					logger.info("pool shutdown, my name=" + this.myName);
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Get my name.
	 * 
	 * @return my service name.
	 */
	public String getName() {
		return this.myName;
	}

	@Override
	public void init(MessageService messageService) {
		this.service = messageService;
	}

	@Override
	public void start() throws IOException {
		this.doInit();
	}

	@Override
	public void stop() {
		try {

			logger.info("shutting down server ...");

			shutdownAndAwaitTermination();
			serverSocket.close();

			logger.info("server shutdown properly, my name=" + this.myName);
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

}
