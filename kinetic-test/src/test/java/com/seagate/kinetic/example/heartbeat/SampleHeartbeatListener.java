/**
 * Copyright (c) 2013 Seagate Technology LLC
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:

 * 1) Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.

 * 2) Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.

 * 3) Neither the name of Seagate Technology nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission
 * from Seagate Technology.

 * 4) No patent or trade secret license whatsoever, either express or implied, is granted by Seagate
 * Technology or its contributors by this copyright license.

 * 5) All modifications must be reposted in source code form in a manner that allows user to
 * readily access the source code.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, NONINFRINGEMENT AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS DISCLAIM ALL LIABILITY FOR
 * INTELLECTUAL PROPERTY INFRINGEMENT RELATED TO THIS SOFTWARE.
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
