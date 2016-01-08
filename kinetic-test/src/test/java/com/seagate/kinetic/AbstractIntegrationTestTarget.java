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
package com.seagate.kinetic;

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import kinetic.admin.AdminClientConfiguration;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;
import kinetic.client.p2p.KineticP2PClientFactory;
import kinetic.client.p2p.KineticP2pClient;
import kinetic.client.p2p.Peer;

public abstract class AbstractIntegrationTestTarget {
	protected final String host;
	protected final int port;
	protected final int tlsPort;

	protected AbstractIntegrationTestTarget(String host, int port, int tlsPort) {
		this.host = host;
		this.port = port;
		this.tlsPort = tlsPort;
	}

	public abstract void shutdown() throws Exception;

	public KineticP2pClient createKineticClient() throws KineticException {
		return KineticP2PClientFactory.createP2pClient(getAdminClientConfig());
	}

	protected void waitForServerReady() throws InterruptedException {
		final int pollingIntervalMS = 50;
		final int maxWaitTimeSec = 10;

		long waitStartTime = System.currentTimeMillis();
		float elapsedTimeMS;
		while (true) {
			Thread.sleep(pollingIntervalMS);

			elapsedTimeMS = System.currentTimeMillis() - waitStartTime;
			if (elapsedTimeMS > maxWaitTimeSec * 1000) {
				throw new RuntimeException("Server never became available");
			}
			try {
				KineticClient kineticClient = createKineticClient();
				kineticClient.noop();
				kineticClient.close();
				break;
			} catch (KineticException e) {
				// Since all exceptions get turned into KineticException we have
				// to manually check the message
				if (e.getMessage().contains("Kinetic Command Exception: ")) {
					break;
				}
				// Ignore this exception because it probably means that the
				// server isn't ready yet so
				// we'll just take a brief nap and try again in a bit
			}
		}
	}

	public AdminClientConfiguration getAdminClientConfig() {
		AdminClientConfiguration clientConfiguration = new AdminClientConfiguration();
		clientConfiguration.setHost(host);
		clientConfiguration.setPort(tlsPort);
		clientConfiguration.setNioServiceThreads(1);
		clientConfiguration.setRequestTimeoutMillis(180000);
		return clientConfiguration;
	}

	// public ClientConfiguration getClientConfig() {
	// ClientConfiguration clientConfiguration = new ClientConfiguration();
	// clientConfiguration.setHost(host);
	// clientConfiguration.setPort(port);
	// clientConfiguration.setNioServiceThreads(1);
	// return clientConfiguration;
	// }

	public Peer getPeer() {
		Peer peer = new Peer();
		peer.setHost(host);
		peer.setPort(port);
		return peer;
	}

	public Peer getTlsPeer() {
		Peer peer = new Peer();
		peer.setHost(host);
		peer.setPort(tlsPort);
		peer.setUseTls(true);
		return peer;
	}

	protected void performISE() throws KineticException {
		KineticAdminClient kineticAdminClient = KineticAdminClientFactory
				.createInstance(getAdminClientConfig());
		String oldErasePin = System.getProperty("OLD_PIN", "");
		String newErasePin = System.getProperty("NEW_PIN", "123");
		
		kineticAdminClient.setErasePin(toByteArray(oldErasePin), toByteArray(newErasePin));
		kineticAdminClient.instantErase(toByteArray(newErasePin));
		kineticAdminClient.close();
	}
}
