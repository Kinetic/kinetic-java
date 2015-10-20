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
package kinetic.client.p2p;

/**
 * 
 * Peer specification for the P2P operation.
 * 
 * @author chiaming
 * 
 */
public class Peer {

	// peer host name
	private String host = "localhost";

	// peer port
	private int port = 8123;

	// use tls/ssl
	private boolean usTls = false;

	/**
	 * Get host name of the peer.
	 * 
	 * @return host name
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Get port number of the peer.
	 * 
	 * @return peer port
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Get if TLS is used by the peer.
	 * 
	 * @return true if TLS should be used. Otherwise, return false.
	 */
	public boolean getUseTls() {
		return this.usTls;
	}

	/**
	 * Set peer host name.
	 * 
	 * @param host
	 *            peer host name.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Set peer port number.
	 * 
	 * @param port
	 *            peer port number
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Set if TLS should be used for the peer to peer operation.
	 * 
	 * @param useTls
	 *            true if TLS should be used. Default is set to false.
	 */
	public void setUseTls(boolean useTls) {
		this.usTls = useTls;
	}
}
