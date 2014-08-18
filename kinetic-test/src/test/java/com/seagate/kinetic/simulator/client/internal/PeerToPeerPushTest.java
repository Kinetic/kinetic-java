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
package com.seagate.kinetic.simulator.client.internal;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.client.internal.p2p.DefaultKineticP2pClient;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.P2POperation;
import com.seagate.kinetic.proto.Kinetic.Command.P2POperation.Operation;
import com.seagate.kinetic.proto.Kinetic.Command.P2POperation.Peer;

/**
 * P2P raw/internal API test.
 *
 * @author Chiaming Yang
 *
 */
public class PeerToPeerPushTest {

	Logger logger = Logger
			.getLogger(PeerToPeerPushTest.class.getName());

	private final int max = 2;
	private final int portbase = 18123;
	private final int sslPortbase = 18443;

	private final SimulatorConfiguration[] sconfigs = new SimulatorConfiguration[max];
	private final ClientConfiguration[] cconfigs = new ClientConfiguration[max];

	private final KineticSimulator[] servers = new KineticSimulator[max];

	@Before
	public void setUp() throws Exception {
		// server configs

		for (int i = 0; i < max; i++) {

			int myport = portbase + i;

			// client configuratiomn
			cconfigs[i] = new ClientConfiguration();
			cconfigs[i].setPort(myport);

			// server configuration
			sconfigs[i] = new SimulatorConfiguration();
			sconfigs[i].setPort(myport);
			sconfigs[i].setSslPort(sslPortbase + i);

			sconfigs[i].put(SimulatorConfiguration.PERSIST_HOME, "instance_"
					+ myport);

			servers[i] = new KineticSimulator(sconfigs[i]);

			logger.info("server started, port=" + myport + ", ssl port="
					+ sslPortbase + i);
		}
	}

	@Test
	public void p2pTest() throws Exception {

		ByteString key = ByteString.copyFromUtf8("p2p_key");
		ByteString value = ByteString.copyFromUtf8("p2p_value");

		Entry entry = new Entry();
		entry.setKey(key.toByteArray());
		entry.setValue(value.toByteArray());

		// construct client from config 0
		DefaultKineticP2pClient p2pClient = new DefaultKineticP2pClient(
				cconfigs[0]);

		// put entry to config 0
		p2pClient.putForced(entry);

		// request message
		KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
        
		Message.Builder request = (Message.Builder) km.getMessage();
		Command.Builder commandBuilder = (Command.Builder) km.getCommand();

		// set message type
		commandBuilder.getHeaderBuilder()
		.setMessageType(MessageType.PEER2PEERPUSH);

		P2POperation.Builder p2pOp = commandBuilder
				.getBodyBuilder().getP2POperationBuilder();

		// set peer
		Peer.Builder peer = p2pOp.getPeerBuilder();
		peer.setHostname("localhost");
		peer.setPort(cconfigs[1].getPort());
		peer.setTls(false);

		Operation.Builder operation = Operation.newBuilder();
		operation.setForce(true);
		operation.setKey(key);

		p2pOp.addOperation(operation.build());

		// p2p push -- config 0
		
		p2pClient.PeerToPeerPush(km);

		logger.info("peer to peer pushed entry ...");

		boolean deleted = p2pClient.deleteForced(key.toByteArray());

		assertTrue(deleted);

		// close client -- config 0
		p2pClient.close();

		// construct client - config 1
		DefaultKineticP2pClient validationClient = new DefaultKineticP2pClient(cconfigs[1]);

		// get entry from config 1
		Entry pcEntry = validationClient.get(key.toByteArray());

		deleted = validationClient.deleteForced(key.toByteArray());

		assertTrue(deleted);

		validationClient.close();

		assertTrue(Arrays.equals(pcEntry.getValue(), value.toByteArray()));

		logger.info("peer to peer test passed");
	}

	@After
	public void tearDown() throws Exception {

		for (int i = 0; i < max; i++) {

			servers[i].close();

			logger.info("server close, myport="
					+ servers[i].getServerConfiguration().getPort());
		}
	}

}
