package com.seagate.kinetic.simulator.client.p2p;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.client.Entry;
import kinetic.client.KineticException;
import kinetic.client.p2p.KineticP2pClient;
import kinetic.client.p2p.Operation;
import kinetic.client.p2p.Peer;
import kinetic.client.p2p.PeerToPeerOperation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Charsets;
import com.seagate.kinetic.AbstractIntegrationTestTarget;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestTargetFactory;
import com.seagate.kinetic.KineticTestRunner;
import com.seagate.kinetic.SimulatorOnly;

/**
 * P2P operation test.
 * <p/>
 * This test case also serves as an example for P2P API usage.
 *
 * @author Chiaming Yang
 */
@RunWith(KineticTestRunner.class)
public class PeerToPeerOperationTest extends IntegrationTestCase {
	private AbstractIntegrationTestTarget secondaryTestTarget;
	private KineticP2pClient secondaryClient;

	@Before
	public void setUp() throws Exception {
		secondaryTestTarget = IntegrationTestTargetFactory.createAlternateTestTarget();

		KineticAdminClient adminClient = KineticAdminClientFactory.createInstance(secondaryTestTarget.getClientConfig());
		adminClient.setup(null, null, 0, true);
		adminClient.close();

		secondaryClient = secondaryTestTarget.createKineticClient();
	}

	@After
	public void tearDown() throws Exception {
		secondaryClient.close();
		secondaryTestTarget.shutdown();
	}

	@Test
	public void testP2PPut_WorksOverPlainConnection() throws Exception {
		byte[] key = "an awesome key!".getBytes(Charsets.UTF_8);
		byte[] value = "an awesome value!".getBytes(Charsets.UTF_8);

		Entry entry = new Entry(key, value);
		getClient().putForced(entry);

		PeerToPeerOperation p2p = new PeerToPeerOperation();
		p2p.setPeer(secondaryTestTarget.getPeer());
		Operation op = new Operation();
		op.setKey(key);
		op.setForced(true);
		p2p.addOperation(op);

		PeerToPeerOperation p2pResp = getClient().PeerToPeerPush(p2p);

		assertTrue(p2pResp.getStatus());

		Entry peerEntry = secondaryClient.get(key);

		assertArrayEquals(value, peerEntry.getValue());
	}

	@Test
	@SimulatorOnly
	public void testP2PPut_WorksOverTlsConnection() throws Exception {
		byte[] key = "an awesome key!".getBytes(Charsets.UTF_8);
		byte[] value = "an awesome value!".getBytes(Charsets.UTF_8);

		Entry entry = new Entry(key, value);
		getClient().putForced(entry);

		PeerToPeerOperation p2p = new PeerToPeerOperation();
		p2p.setPeer(secondaryTestTarget.getTlsPeer());
		Operation op = new Operation();
		op.setKey(key);
		op.setForced(true);
		p2p.addOperation(op);

		PeerToPeerOperation p2pResp = getClient().PeerToPeerPush(p2p);

		assertTrue(p2pResp.getStatus());

		Entry peerEntry = secondaryClient.get(key);

		assertArrayEquals(value, peerEntry.getValue());
	}

	@Test
	public void testP2PPut_Fails_ForVersionMismatch() throws Exception {
		byte[] key = "an awesome key!".getBytes(Charsets.UTF_8);
		byte[] value = "an awesome value!".getBytes(Charsets.UTF_8);

		Entry entry = new Entry(key, value);
		getClient().putForced(entry);

		PeerToPeerOperation p2p = new PeerToPeerOperation();
		p2p.setPeer(secondaryTestTarget.getTlsPeer());
		Operation op = new Operation();
		op.setKey(key);
		op.setVersion(key);
		p2p.addOperation(op);

		PeerToPeerOperation p2pResp = getClient().PeerToPeerPush(p2p);

		assertFalse(p2pResp.getOperationList().get(0).getStatus());
		assertTrue(p2pResp.getStatus());
	}


	@Test
	public void testP2PPut_Fails_ForInvalidPeer() throws Exception {
		PeerToPeerOperation p2pop = new PeerToPeerOperation();
		Peer peer = new Peer();
		peer.setHost("localhost");
		peer.setPort(1);
		p2pop.setPeer(peer);
		Operation op = new Operation();
		op.setKey("foo".getBytes(Charsets.UTF_8));
		p2pop.addOperation(op);

		// the client should surface connection errors in a machine-readable way
		try {
			getClient().PeerToPeerPush(p2pop);
			fail("Should have thrown KineticException");
		} catch (KineticException e) {
			// expected exception should be caught here.
			;
		}
	}
}
