/**
 * Copyright (c) 2013 Seagate Technology LLC
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:

 * 1) Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.

 * 2) Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.

 * 3) Neither the name of Seagate Technology nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission
 * from Seagate Technology.

 * 4) No patent or trade secret license whatsoever, either express or implied, is granted by Seagate
 * Technology or its contributors by this copyright license.

 * 5) All modifications must be reposted in source code form in a manner that allows user to
 * readily access the source code.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, NONINFRINGEMENT AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS DISCLAIM ALL LIABILITY FOR
 * INTELLECTUAL PROPERTY INFRINGEMENT RELATED TO THIS SOFTWARE.
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
