/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.io.provider.nio.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

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

	private final Logger logger = Logger.getLogger(SslTrustManagerFactory.class
			.getName());

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
