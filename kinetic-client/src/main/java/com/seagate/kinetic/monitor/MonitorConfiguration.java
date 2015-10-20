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
