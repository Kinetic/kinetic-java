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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

/**
 * Provides transport layer encryption only. Certificates are not validated.
 *
 * @author chiaming
 *
 */
public class SslTrustManagerFactory extends TrustManagerFactorySpi {

	private static final TrustManager TRUST_MANAGER = new X509TrustManager() {
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
			// add logic as needed
			;
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) {
			// add logic as needed
		}
	};

	public static TrustManager[] getTrustManagers() {
		return new TrustManager[] { TRUST_MANAGER };
	}

	@Override
	protected TrustManager[] engineGetTrustManagers() {
		return getTrustManagers();
	}

	@Override
	protected void engineInit(KeyStore keystore) throws KeyStoreException {
		// Unused
	}

	@Override
	protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
			throws InvalidAlgorithmParameterException {
		// Unused
	}
}
