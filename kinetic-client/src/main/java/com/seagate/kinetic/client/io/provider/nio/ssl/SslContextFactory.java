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

package com.seagate.kinetic.client.io.provider.nio.ssl;

import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 *
 * @author chiaming
 *
 */
public final class SslContextFactory {

	private static final String PROTOCOL = "TLS";
	private static final SSLContext SERVER_CONTEXT;
	private static final SSLContext CLIENT_CONTEXT;

	static {
		String algorithm = Security
				.getProperty("ssl.KeyManagerFactory.algorithm");
		if (algorithm == null) {
			algorithm = "SunX509";
		}

		SSLContext serverContext;
		SSLContext clientContext;
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(KineticKeyStore.asInputStream(),
					KineticKeyStore.getKeyStorePassword());

			// Set up key manager factory to use our key store
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			kmf.init(ks, KineticKeyStore.getCertificatePassword());

			// Initialize the SSLContext to work with our key managers.
			serverContext = SSLContext.getInstance(PROTOCOL);
			serverContext.init(kmf.getKeyManagers(), null, null);
		} catch (Exception e) {
			throw new Error("Failed to initialize the server-side SSLContext",
					e);
		}

		try {
			clientContext = SSLContext.getInstance(PROTOCOL);
			clientContext.init(null, SslTrustManagerFactory.getTrustManagers(),
					null);
		} catch (Exception e) {
			throw new Error("Failed to initialize the client-side SSLContext",
					e);
		}

		SERVER_CONTEXT = serverContext;
		CLIENT_CONTEXT = clientContext;
	}

	public static SSLContext getServerContext() {
		return SERVER_CONTEXT;
	}

	public static SSLContext getClientContext() {
		return CLIENT_CONTEXT;
	}

	private SslContextFactory() {
		// Unused
	}
}
