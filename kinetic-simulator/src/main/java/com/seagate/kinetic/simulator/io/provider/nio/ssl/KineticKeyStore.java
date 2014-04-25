/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
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

