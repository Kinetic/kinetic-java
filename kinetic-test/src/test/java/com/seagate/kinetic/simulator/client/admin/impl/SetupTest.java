package com.seagate.kinetic.simulator.client.admin.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.admin.impl.DefaultAdminClient;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Setup;
import com.seagate.kinetic.proto.Kinetic.Message.Status;

/**
 *
 * Setup test
 * <p>
 *
 * @author Chenchong Li
 *
 */
public class SetupTest extends IntegrationTestCase {
	@Test
	public void testEraseDB() throws KineticException {
		EntryMetadata entryMetadata = new EntryMetadata();
		entryMetadata.setVersion("0".getBytes());
		KineticClient client = KineticClientFactory
				.createInstance(getClientConfig());
		client.delete(new Entry("key".getBytes(), "value".getBytes(),
				entryMetadata));
		entryMetadata = new EntryMetadata();
		client.put(new Entry("key".getBytes(), "value".getBytes(),
				entryMetadata), "0".getBytes());

		Message.Builder request = Message.newBuilder();
		Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		setup.setInstantSecureErase(true);

		KineticMessage km = new KineticMessage();
		km.setMessage(request);

		Message respond = (Message) this.getAdminClient()
				.configureSetupPolicy(km).getMessage();
		assertTrue(respond.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));
		assertNull(client.get("key".getBytes()));

		this.getAdminClient().close();
	}

	@Test
	public void testSetClusterVersion() throws KineticException, IOException,
	InterruptedException {
		Message.Builder request = Message.newBuilder();
		Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		setup.setNewClusterVersion(1);

		KineticMessage km = new KineticMessage();
		km.setMessage(request);

		getAdminClient().configureSetupPolicy(km);

		resetClusterVersion(1);
	}

	@Test
	public void testModifyClusterVersion_UseWrongAdminClientModifyAgain()
			throws Exception {
		getAdminClient().setup(null, null, 1, false);

		// restart server
		restartServer();

		Message.Builder request1 = Message.newBuilder();
		Setup.Builder setup1 = request1.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		setup1.setNewClusterVersion(2);

		try {
			getAdminClient().setup(null, null, 2, false);
			fail("Should have thrown");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains(
					"Unknown Error: VERSION_FAILURE: CLUSTER_VERSION_FAILURE"));
		}

		resetClusterVersion(1);
	}

	@Test
	public void testClusterVersionHonored() throws KineticException {
		getAdminClient().setup(null, null, 123, false);

		final Message.Builder testClusterVersionRequest = Message.newBuilder();
		testClusterVersionRequest.getCommandBuilder().getBodyBuilder()
		.getSetupBuilder().setInstantSecureErase(true);
		try {
			getAdminClient().getLog();
			fail("Should have thrown");
		} catch (KineticException e) {
		}

		resetClusterVersion(123);
	}

	@Test
	public void testClusterVersion_ErasedByISE() throws KineticException {
		// Set Cluster Version
		getAdminClient().setup(null, null, 123, false);

		// Perform ISE. New cluster version should be ignored.
		final DefaultAdminClient client = new DefaultAdminClient(
				getClientConfig(123));

		client.SecureErase(null);
		client.close();

		// The cluster version should have been erased, so making a call without
		// a
		// cluster version should succeed
		getAdminClient().getLog();
	}

	@Test
	public void testModifyClusterVersion_UseRightAdminClientModifyAgain()
			throws Exception {
		getAdminClient().setup(null, null, 1, false);

		// restart server
		restartServer();

		final KineticAdminClient adminClient = KineticAdminClientFactory
				.createInstance(getClientConfig(1));
		adminClient.setup(null, null, 2, false);
		adminClient.close();

		resetClusterVersion(2);
	}

	private void resetClusterVersion(int currentClusterVersion)
			throws KineticException {
		final KineticAdminClient client = KineticAdminClientFactory
				.createInstance(getClientConfig(currentClusterVersion));
		client.setup(null, null, 0, false);
		client.close();
	}
}
