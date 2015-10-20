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
