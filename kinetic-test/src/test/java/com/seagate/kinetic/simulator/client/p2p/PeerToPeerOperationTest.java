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
package com.seagate.kinetic.simulator.client.p2p;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.client.Entry;
import kinetic.client.KineticException;
import kinetic.client.p2p.KineticP2pClient;
import kinetic.client.p2p.Operation;
import kinetic.client.p2p.Peer;
import kinetic.client.p2p.PeerToPeerOperation;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.seagate.kinetic.AbstractIntegrationTestTarget;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestTargetFactory;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

/**
 * P2P operation test.
 * <p/>
 * This test case also serves as an example for P2P API usage.
 *
 * @author Chiaming Yang
 */
@Test(groups = { "simulator" })
public class PeerToPeerOperationTest extends IntegrationTestCase {
    private AbstractIntegrationTestTarget secondaryTestTarget;
    private KineticP2pClient secondaryClient;

    @BeforeMethod
    public void setUp() throws Exception {
        secondaryTestTarget = IntegrationTestTargetFactory
                .createAlternateTestTarget();

        KineticAdminClient adminClient = KineticAdminClientFactory
                .createInstance(secondaryTestTarget.getAdminClientConfig());
        adminClient.instantErase(null);
        adminClient.close();

        secondaryClient = secondaryTestTarget.createKineticClient();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        secondaryClient.close();
        secondaryTestTarget.shutdown();
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testP2PPut_WorksOverPlainConnection(String clientName)
            throws Exception {
        byte[] key = "an awesome key!".getBytes(Charsets.UTF_8);
        byte[] value = "an awesome value!".getBytes(Charsets.UTF_8);

        Entry entry = new Entry(key, value);
        getClient(clientName).putForced(entry);

        PeerToPeerOperation p2p = new PeerToPeerOperation();
        p2p.setPeer(secondaryTestTarget.getPeer());
        Operation op = new Operation();
        op.setKey(key);
        op.setForced(true);
        p2p.addOperation(op);

        PeerToPeerOperation p2pResp = getClient(clientName).PeerToPeerPush(p2p);

        assertTrue(p2pResp.getStatus());

        Entry peerEntry = secondaryClient.get(key);

        assertArrayEquals(value, peerEntry.getValue());
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testP2PPut_WorksOverTlsConnection(String clientName)
            throws Exception {
        byte[] key = "an awesome key!".getBytes(Charsets.UTF_8);
        byte[] value = "an awesome value!".getBytes(Charsets.UTF_8);

        Entry entry = new Entry(key, value);
        getClient(clientName).putForced(entry);

        PeerToPeerOperation p2p = new PeerToPeerOperation();
        p2p.setPeer(secondaryTestTarget.getTlsPeer());
        Operation op = new Operation();
        op.setKey(key);
        op.setForced(true);
        p2p.addOperation(op);

        PeerToPeerOperation p2pResp = getClient(clientName).PeerToPeerPush(p2p);

        assertTrue(p2pResp.getStatus());

        Entry peerEntry = secondaryClient.get(key);

        assertArrayEquals(value, peerEntry.getValue());
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testP2PPut_Fails_ForVersionMismatch(String clientName)
            throws Exception {
        byte[] key = "an awesome key!".getBytes(Charsets.UTF_8);
        byte[] value = "an awesome value!".getBytes(Charsets.UTF_8);

        Entry entry = new Entry(key, value);
        getClient(clientName).putForced(entry);

        PeerToPeerOperation p2p = new PeerToPeerOperation();
        p2p.setPeer(secondaryTestTarget.getTlsPeer());
        Operation op = new Operation();
        op.setKey(key);
        op.setVersion(key);
        p2p.addOperation(op);

        PeerToPeerOperation p2pResp = getClient(clientName).PeerToPeerPush(p2p);

        // expect overall status to be false due to version mismatch
        assertFalse(p2pResp.getStatus());

        // expect response op status to set to false
        assertFalse(p2pResp.getOperationList().get(0).getStatus());

        // expect version mis-match due to the version specified does not exist
        assertTrue("expect version mis-match status code",
                StatusCode.VERSION_MISMATCH == p2pResp.getOperationList()
                        .get(0).getStatusCode());
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testP2PPut_Fails_ForInvalidPeer(String clientName)
            throws Exception {
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
            getClient(clientName).PeerToPeerPush(p2pop);
            AssertJUnit.fail("Should have thrown KineticException");
        } catch (KineticException e) {
            // expected exception should be caught here.
            ;
        }
    }
}
