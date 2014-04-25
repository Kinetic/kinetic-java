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
package com.seagate.kinetic.example.heartbeat;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.monitor.HeartbeatListener;

/**
 * 
 * A simple Kinetic Heartbeat listener example.
 * <p>
 * A Kinetic heart beat message is a JSON formated string message (UTF-8)
 * published by a Kinetic drive or simulator.
 * <p>
 * The <code>HeartbeatListener</code> is an utility that listens on the
 * specified heart beat address. The {@link HeartbeatListener#onMessage(byte[])}
 * is invoked when a heartbeat message is received.
 * <p>
 * Applications may extend the <code>HeartbeatListener</code> and override its
 * onMessage() method to receive the heart beat messages.
 * <p>
 * 
 * @author chiaming
 */
public class SampleHeartbeatListener extends HeartbeatListener {

	private final static Logger logger = Logger
			.getLogger(SampleHeartbeatListener.class.getName());

	/**
	 * 
	 * Instantiate a new heartbeat listener that listens on kinetic multicast
	 * address 239.1.2.3:8123.
	 * 
	 * @throws IOException
	 *             if any i/o error occurred when starting the heartbeat
	 *             listener.
	 */
	public SampleHeartbeatListener() throws IOException {
		super();
	}

	/**
	 * Instantiate a new heartbeat listener that listens messages on the
	 * specified multicast address and port.
	 * 
	 * @param address
	 *            multicast address.
	 * @param port
	 *            multicast port.
	 * 
	 * @throws IOException
	 *             if any network IO exception ocurred.
	 */
	public SampleHeartbeatListener(String address, int port) throws IOException {
		super(address, port);
	}

	/**
	 * This method is invoked when a heart beat message is received.
	 */
	@Override
	public void onMessage(byte[] data) {

		try {

			String message = new String(data, "UTF8");

			logger.info("received heart beat: " + message);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

	}

	public static void main(String[] args) throws IOException {
		// instantiate a new heart beat listener
		new SampleHeartbeatListener();
	}

}
