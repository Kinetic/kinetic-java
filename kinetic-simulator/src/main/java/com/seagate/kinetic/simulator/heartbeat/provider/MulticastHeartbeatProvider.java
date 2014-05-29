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
package com.seagate.kinetic.simulator.heartbeat.provider;

import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.google.gson.Gson;
import com.seagate.kinetic.heartbeat.HeartbeatMessage;
import com.seagate.kinetic.heartbeat.KineticNetworkInterface;
import com.seagate.kinetic.simulator.heartbeat.HeartbeatProvider;

/**
 * 
 * Multicast heartbeat provider.
 * 
 * @author chiaming
 * 
 */
public class MulticastHeartbeatProvider implements HeartbeatProvider {

	private final static Logger logger = Logger
			.getLogger(MulticastHeartbeatProvider.class.getName());

	// simulator configuration
	private SimulatorConfiguration config = null;

	// multicast socket
	private MulticastSocket mcastSocket = null;

	// inetaddress
	private InetAddress mcastAddress = null;

	// multicast address
	private final String mcastDestination = "239.1.2.3";

	// destination port
	private int mcastPort = 8123;

	// host ip
	//private String thisHostIp = "127.0.0.1";

	// host port
	//private String thisHostPort = thisHostIp + ":8123";

	// gson instance
	private static Gson gson = new Gson();

	// current engine instance
	// private final SimulatorEngine engine = null;

	// heart beat message
	private final HeartbeatMessage heartbeatMessage = new HeartbeatMessage();

	// heartbeat message in string format
	private String heartbeatMessageStr = null;

	// heart beat packet
	private DatagramPacket packet = null;

	public MulticastHeartbeatProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(SimulatorConfiguration config) {
		// my config
		this.config = config;

		// initilize
		this.doInit();
	}

	@Override
	public void sendHeartbeat() {
		try {
			// send heart beat
			this.mcastSocket.send(packet);

			// logger.info ("sent heartbeat message: " +
			// this.heartbeatMessageStr);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	@Override
	public void close() {

		// close mcast socket
		this.mcastSocket.close();

		logger.info("multicast heartbeat stopped");
	}

	/**
	 * Initialize heart beat destination, message, etc.
	 */
	private void doInit() {

		try {
			// this host name
		    //this.thisHostIp = InetAddress.getByName(thisHostIp).getHostAddress();

			// this host address
			//this.thisHostPort = this.thisHostIp + ":" + config.getPort() + ":"
			//		+ config.getSslPort();

			// multicast socket
			mcastSocket = new MulticastSocket();

			// multicast group address
			mcastAddress = InetAddress.getByName(mcastDestination);

			// multicast listener port
			this.mcastPort = config.getHeartbeatPort();

			// init heart beat message
			this.initHeartbeatMessage();

//			logger.info("Heart beat initialized., my address="
//					+ this.thisHostPort + ", tick time=" + config.getTickTime()
//					+ " milli-secs, mcast Address=" + this.mcastDestination
//					+ ":" + this.mcastPort);

		} catch (Exception e) {
		    logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * initialize heart beat message
	 */
	public void initHeartbeatMessage() {

		try {

			// set port to heart beat message
			this.heartbeatMessage.setPort(config.getPort());

			// set tls port to heart beat message
			this.heartbeatMessage.setTlsPort(config.getSslPort());

			// get all network interfaces on this machine
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface
					.getNetworkInterfaces();

			while (netInterfaces.hasMoreElements()) {

				// get next interface
				NetworkInterface ni = netInterfaces.nextElement();

				// my network interface
				KineticNetworkInterface knetInterface = new KineticNetworkInterface();

				// set kinetic NI display name
				knetInterface.setName(ni.getDisplayName());

				// set mac addr
				byte[] mac = ni.getHardwareAddress();
				if (mac != null) {
					knetInterface.setMacAddress(bytesToStringMac(mac));
				}

				// get inet addresses on this interface
				Enumeration<InetAddress> addresses = ni.getInetAddresses();

				while (addresses.hasMoreElements()) {
					// get next inet addr
					InetAddress addr2 = addresses.nextElement();
					// get string address
					String addrString = addr2.getHostAddress();

					if (addr2 instanceof Inet6Address) {
						knetInterface.setIpV6Address(addrString);
					} else {
						knetInterface.setIpV4Address(addrString);
					}
				}

				// add my network interface to heart beat
				this.heartbeatMessage.getNetworkInterfaces().add(knetInterface);
			}

			// heart beat message string
			this.heartbeatMessageStr = gson.toJson(this.heartbeatMessage,
					HeartbeatMessage.class);

			// heart beat message byte[]
			byte[] data = heartbeatMessageStr.getBytes("UTF-8");

			// heart beat packet
			packet = new DatagramPacket(data, data.length, this.mcastAddress,
					this.mcastPort);

			logger.info("heart beat message initialized., msg="
					+ heartbeatMessageStr);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public static String bytesToStringMac(byte[] mac) {
		StringBuilder sb = new StringBuilder(18);
		for (byte b : mac) {
			if (sb.length() > 0) {
				sb.append(':');
			}
			sb.append(String.format("%02x", b));
		}

		return sb.toString();
	}

}
