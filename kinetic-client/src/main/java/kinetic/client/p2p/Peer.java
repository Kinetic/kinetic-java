/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
