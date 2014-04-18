/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.heartbeat;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

/**
 * 
 * Heart beat prototype message.
 * 
 */
public class HeartbeatMessage {

	private final List<KineticNetworkInterface> network_interfaces = new ArrayList<KineticNetworkInterface>();

	// message source port
	private int port = 8123;

	// message source tls port
	private int tlsPort = 8443;

	// gson to
	private static Gson gson = new Gson();

	public HeartbeatMessage() {
		;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

	public void setTlsPort(int port) {
		this.tlsPort = port;
	}

	public int getTlsPort() {
		return this.tlsPort;
	}

	public void addNetworkInterface(KineticNetworkInterface networkInterface) {
		this.network_interfaces.add(networkInterface);
	}

	public List<KineticNetworkInterface> getNetworkInterfaces() {
		return this.network_interfaces;
	}

	public static String toJson(Object obj) {
		return gson.toJson(obj, HeartbeatMessage.class);
	}

	public static HeartbeatMessage fromJson(String str) {
		return gson.fromJson(str, HeartbeatMessage.class);
	}

	public static void main(String[] args) throws UnsupportedEncodingException {

		HeartbeatMessage hbm = new HeartbeatMessage();

		String msg = HeartbeatMessage.toJson(hbm);

		System.out.println("msg=" + msg + ", size="
				+ msg.getBytes("utf8").length);

		byte[] data = msg.getBytes("UTF8");

		String msgX = new String(data, "UTF8");

		HeartbeatMessage newMsg = HeartbeatMessage.fromJson(msgX);

		String msg2 = HeartbeatMessage.toJson(newMsg);

		System.out.println("msg=" + msg2 + ", size="
				+ msg2.getBytes("utf8").length);
	}

}
