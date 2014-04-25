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
