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

import java.io.InputStream;

/**
 * A key store which provides all the required information to create an SSL
 * connection.
 * 
 * To generate a kinetic key store:
 * 
 * <pre>
 * keytool  -genkey -alias securechat -keysize 2048 -validity 36500
 *          -keyalg RSA -dname "CN=kinetic"
 *          -keypass secret -storepass secret
 *          -keystore kinetic.jks
 * </pre>
 */
public final class KineticKeyStore {

	public static InputStream asInputStream() {

		return KineticKeyStore.class
				.getResourceAsStream("kinetic.jks");
	}

	public static char[] getCertificatePassword() {
		return "secret".toCharArray();
	}

	public static char[] getKeyStorePassword() {
		return "secret".toCharArray();
	}

	private KineticKeyStore() {
		// Unused
	}
}

