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

public class MonitorConfiguration {
	
	/**
	 * jetty server port.
	 */
	private int port = 8080;

	/**
	 * WebAppContext context path.
	 */
	private String contextPath = "/";

	/**
	 * WebAppContext resource base.
	 */
	private String resourceBase = ".";

	/**
	 * Welcome file path.
	 */
	private String[] welcomeFiles = new String[] { "page/index.html" };

	/**
	 * Monitor check node unavailable interval time(s)
	 */
	private long unavailableThreshold = 60;

	/**
	 * Get jetty server port.
	 * 
	 * @return jetty server port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set jetty server port.
	 * 
	 * @param port
	 *            jetty server port.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Get context path.
	 * 
	 * @return jetty server port.
	 */
	public String getContextPath() {
		return contextPath;
	}

	/**
	 * Set context path.
	 * 
	 * @param contextPath
	 *            context path.
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Get resource base path.
	 * 
	 * @return resource base path.
	 */
	public String getResourceBase() {
		return resourceBase;
	}

	/**
	 * Set resource base path.
	 * 
	 * @param resourceBase
	 *            resource base path.
	 */
	public void setResourceBase(String resourceBase) {
		this.resourceBase = resourceBase;
	}

	/**
	 * Get checking node unavailable time(s).
	 * 
	 * @return node unavailable checking time.
	 */
	public long getUnavailableThreshold() {
		return unavailableThreshold;
	}

	/**
	 * Set checking node unavailable time(s).
	 * 
	 * @param unavailableThreshold
	 *            checking node unavailable time(s).
	 */
	public void setUnavailableThreshold(long unavailableThreshold) {
		this.unavailableThreshold = unavailableThreshold;
	}

	/**
	 * Get welcome file path.
	 * 
	 * @return welcome file path.
	 */
	public String[] getWelcomeFiles() {
		return welcomeFiles;
	}

	/**
	 * Set welcome file path.
	 * 
	 * @param welcomeFiles
	 *            welcome file path.
	 */
	public void setWelcomeFiles(String[] welcomeFiles) {
		this.welcomeFiles = welcomeFiles;
	}
}
