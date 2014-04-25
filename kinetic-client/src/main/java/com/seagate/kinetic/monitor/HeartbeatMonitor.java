/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.seagate.kinetic.monitor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * 
 * Heartbeat monitor boot-strap class.
 * <p>
 * Applications may use this class to monitor instance(s) of the simulator.
 * <p>
 * There is a main method provided in this class as a reference to start a new
 * instance of heartbeat monitor.
 * <p>
 * Applications may also define their own MonitorConfigration instances and
 * start the heartbeat monitor with customized configurations.
 * 
 * @see MonitorConfiguration
 * 
 */
public class HeartbeatMonitor {
	private final static Logger logger = Logger
			.getLogger(HeartbeatMonitor.class.getName());

	private MonitorConfiguration monitorConfiguration;

	/**
	 * Constructor for the heartbeat monitor.
	 * 
	 * 
	 * @param monitorConfiguration
	 *            configurations for the monitor.
	 */
	public HeartbeatMonitor(MonitorConfiguration monitorConfiguration) {
		this.monitorConfiguration = monitorConfiguration;
	}

	/**
	 * The method how to start heartbeat monitor.
	 * 
	 */
	public void start() {
		Server server = new Server(monitorConfiguration.getPort());
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(monitorConfiguration.getWelcomeFiles());
		resourceHandler.setResourceBase(monitorConfiguration.getResourceBase());

		ServletContextHandler contextHandler = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		contextHandler.setContextPath(monitorConfiguration.getContextPath());
		contextHandler.setResourceBase(monitorConfiguration.getResourceBase());
		server.setHandler(contextHandler);
		ServletHolder holder = contextHandler
				.addServlet(
						com.seagate.kinetic.monitor.internal.servlet.KineticAdminServlet.class,
						"/servlet/*");
		holder.setInitOrder(0);
		holder.setInitParameter("resourceBase", "/servlet");
		holder.setInitParameter("pathInfoOnly", "true");
		holder.setInitParameter("unavailableThreshold", ""
				+ monitorConfiguration.getUnavailableThreshold());
		holder.setDisplayName("kineticadminservlet");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceHandler, contextHandler,
				new DefaultHandler() });
		server.setHandler(handlers);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	/**
	 * A default heartbeat monitor instance boot-strap method.
	 * 
	 */
	public static void main(String[] args) throws IOException {
		MonitorConfiguration config = new MonitorConfiguration();
		HeartbeatMonitor monitor = new HeartbeatMonitor(config);
		monitor.start();
	}
}
