/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
