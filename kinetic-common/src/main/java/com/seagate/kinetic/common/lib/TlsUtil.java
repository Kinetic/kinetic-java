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
package com.seagate.kinetic.common.lib;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

/**
 * 
 * TLS/SSL common utility for the simulator and Java API implementation.
 * 
 * @author chiaming
 * 
 */
public class TlsUtil {

	// logger
	private static final Logger logger = Logger.getLogger(TlsUtil.class
			.getName());

	/**
	 * supported TLS/SSL protocols for the kinetic drive. This is imposed
	 * because the drive only supports the soecified prtocols.
	 */
	public static final String SUPPORTED_TLS_PROTOCOLS[] = new String[] {
		"TLSv1", "TLSv1.1", "TLSv1.2" };

	private static String supportedTLSString = Arrays
			.toString(SUPPORTED_TLS_PROTOCOLS);

	/**
	 * 
	 * Configure the TLS/SSL engine to support the specified protocols.
	 * <P>
	 * The current supported protocols are "TLSv1", "TLSv1.1", "TLSv1.2".
	 * <p>
	 * 
	 * @param engine
	 *            the TLS engine to be configured.
	 * 
	 * @see SSLEngine
	 */
	public static void enableSupportedProtocols(SSLEngine engine) {
		try {
			// set enabled protocols
			engine.setEnabledProtocols(SUPPORTED_TLS_PROTOCOLS);

			logger.info("enabled TLS protocol: " + supportedTLSString);
		} catch (Exception e) {
			/**
			 * Log the exception and exception stack trace but continue program
			 * execution.
			 * <p>
			 * During evaluation phase, we intend to improve the user experience
			 * by demonstrating TLS/SSL usage is functioning out of the box for
			 * Java 1.6 and 1.7.
			 * <p>
			 * We intended to log this on the face exception to inform user to
			 * upgrade to Jave 1.7 when connecting (TLS) to the drive.
			 */
			logger.warning("Failed to enable TLS protocols. Possible fix is to use Java 1.7 or later.");
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Get the enabled TLS/SSL protocols for the drive/simulator.
	 * <P>
	 * The current supported protocols are "TLSv1", "TLSv1.1", "TLSv1.2".
	 * <p>
	 * 
	 * @return the supported/enabled TLS protocols for the drive/simulator.
	 * 
	 * @see SSLEngine#getSupportedProtocols()
	 */
	public static final String[] getEnabledTlsProtocols() {
		return SUPPORTED_TLS_PROTOCOLS;
	}

}
