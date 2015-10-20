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
package com.seagate.kinetic.simulator.io.provider.nio.ssl;

import java.io.InputStream;

/**
 * A key store which provides all the required information to create an SSL
 * connection.
 * 
 * To generate a kinetic key store:
 * 
 * <pre>
 * keytool  -genkey -alias kinetic -keysize 2048 -validity 36500
 *          -keyalg RSA -dname "CN=kinetic"
 *          -keypass secret -storepass secret
 *          -keystore kinetic.jks
 * </pre>
 */
public final class KineticKeyStore {

	public static InputStream asInputStream() {

		return com.seagate.kinetic.simulator.io.provider.nio.ssl.KineticKeyStore.class
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

