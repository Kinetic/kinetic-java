package com.seagate.kinetic.adminAPI;

import static com.seagate.kinetic.KineticTestHelpers.cleanPin;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import kinetic.admin.ACL;
import kinetic.admin.Capacity;
import kinetic.admin.Configuration;
import kinetic.admin.Domain;
import kinetic.admin.Interface;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.admin.KineticLog;
import kinetic.admin.KineticLogType;
import kinetic.admin.Role;
import kinetic.admin.Statistics;
import kinetic.admin.Temperature;
import kinetic.admin.Utilization;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;
import com.seagate.kinetic.admin.impl.DefaultAdminClient;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Setup;
import com.seagate.kinetic.proto.Kinetic.Message.Status;

/**
 * Kinetic Administrator Client Basic API Test.
 * <p>
 * Kinetic admin API include:
 * <p>
 * setup(byte[] pin, byte[] setPin, long newClusterVersion, boolean secureErase)
 * <p>
 * firmwareDownload(byte[] pin, byte[] bytes)
 * <p>
 * getLog()
 * <p>
 * getLog(List<KineticLogType> listOfLogType)
 * <p>
 * setSecurity(List<ACL> acls)
 * <p>
 *
 * @see KineticAdminClient
 *
 */
public class KineticAdminTest extends IntegrationTestCase {
	private static final Logger logger = IntegrationTestLoggerFactory
			.getLogger(KineticAdminTest.class.getName());

	// @Test
	public void setupTest() throws KineticException {
		getAdminClient().setup(null, toByteArray("pin001"), 0, true);

		getAdminClient().setup(toByteArray("pin001"), toByteArray("pin001"), 0,
				true);

		cleanPin("pin002", this.getAdminClient());
	}

	/**
	 * Test setup API, erase data in simulator/drive. The result should be true.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	@Test
	public void testSetup_EraseDB() throws KineticException {
		EntryMetadata entryMetadata = new EntryMetadata();
		entryMetadata.setVersion(toByteArray("0"));

		KineticClient client = KineticClientFactory
				.createInstance(getClientConfig());
		client.delete(new Entry(toByteArray("key"), toByteArray("value"),
				entryMetadata));
		entryMetadata = new EntryMetadata();
		client.put(new Entry(toByteArray("key"), toByteArray("value"),
				entryMetadata), toByteArray("0"));

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

		logger.info(this.testEndInfo());
	}

	/**
	 * Test setup API, set cluster version for simulator/drive. The result
	 * should be true.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void testSetup_SetClusterVersion() throws KineticException {
		Message.Builder request = Message.newBuilder();
		Setup.Builder setup = request.getCommandBuilder().getBodyBuilder()
				.getSetupBuilder();
		setup.setNewClusterVersion(1);

		KineticMessage km = new KineticMessage();
		km.setMessage(request);

		Message respond = (Message) getAdminClient().configureSetupPolicy(km)
				.getMessage();

		assertTrue(respond.getCommand().getStatus().getCode()
				.equals(Status.StatusCode.SUCCESS));

		resetClusterVersion(1);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test setup API, modify cluster version for simulator/drive, restart
	 * simulator/drive, then modify again with wrong administrator client. The
	 * result should be thrown exception.
	 * <p>
	 */
	@Test
	public void testSetup_ModifyClusterVersion_UseWrongAdminClientModifyAgain()
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

		logger.info(this.testEndInfo());
	}

	/**
	 * Test setup API, set cluster version for simulator/drive first, then erase
	 * data with wrong cluster version. The result should be thrown exception.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void testSetup_ClusterVersionHonored() throws KineticException {
		getAdminClient().setup(null, null, 123, false);

		final Message.Builder testClusterVersionRequest = Message.newBuilder();
		testClusterVersionRequest.getCommandBuilder().getBodyBuilder()
		.getSetupBuilder().setInstantSecureErase(true);
		try {
			getAdminClient().getLog();
			fail("Should have thrown");
		} catch (KineticException e) {
			assertTrue(e.getMessage().contains("CLUSTER_VERSION_FAILURE"));
		}

		resetClusterVersion(123);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test setup API, set cluster version for simulator/drive first, then erase
	 * data with correct cluster version. The result should be success.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void testSetup_ClusterVersion_ErasedByISE() throws KineticException {
		// Set Cluster Version
		getAdminClient().setup(null, null, 123, false);

		// Perform ISE. New cluster version should be ignored.
		final DefaultAdminClient client = new DefaultAdminClient(
				getClientConfig(123));

		client.SecureErase(null);
		client.close();

		// The cluster version should have been erased, so making a call without
		// a cluster version should succeed
		KineticLog log = getAdminClient().getLog();
		assertTrue(log.getTemperature().size() > 0);
		assertTrue(log.getStatistics().size() > 0);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test setup API, modify cluster version for simulator/drive, restart
	 * simulator/drive, then modify again with correct administrator client. The
	 * result should be success.
	 * <p>
	 *
	 * @throws KineticException
	 *             if Kinetic exception error occurred.
	 * @throws IOException
	 *             if any IO error occurred.
	 * @throws InterruptedException
	 *             if any Interrupt error occurred.
	 *
	 */
	@Test
	public void testSetup_ModifyClusterVersion_UseRightAdminClientModifyAgain()
			throws Exception {
		getAdminClient().setup(null, null, 1, false);

		// restart server
		restartServer();

		final KineticAdminClient adminClient = KineticAdminClientFactory
				.createInstance(getClientConfig(1));

		adminClient.setup(null, null, 2, false);
		adminClient.close();

		resetClusterVersion(2);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test get log API. The result should be success.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void test_GetLogTest() throws KineticException {
		KineticLog log = getAdminClient().getLog();

		assertTrue(log.getTemperature().size() > 0);
		assertTrue(log.getUtilization().size() > 0);
		assertTrue(log.getStatistics().size() > 0);
		assertTrue(log.getMessages().length > 0);

		assertTrue(log.getCapacity().getRemaining() >= 0);
		assertTrue(log.getCapacity().getTotal() >= 0);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test set security API. The result should be success.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void setSecurity() throws KineticException {
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.DELETE);
		roles.add(Role.GETLOG);
		roles.add(Role.READ);
		roles.add(Role.RANGE);
		roles.add(Role.SECURITY);
		roles.add(Role.SETUP);
		roles.add(Role.WRITE);

		Domain domain = new Domain();
		domain.setRoles(roles);

		List<Domain> domains = new ArrayList<Domain>();
		domains.add(domain);

		List<ACL> acls = new ArrayList<ACL>();
		ACL acl1 = new ACL();
		acl1.setDomains(domains);
		acl1.setUserId(1);
		acl1.setKey("asdfasdf");

		acls.add(acl1);

		getAdminClient().setSecurity(acls);

		// The acl have been set, so making a call with getlog role should
		// succeed
		KineticLog log = getAdminClient().getLog();
		assertTrue(log.getTemperature().size() > 0);
		assertTrue(log.getStatistics().size() > 0);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test set security API. The algorithm value is valid, the result should be
	 * success.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void setSecurity_algorithmIsSupportTest() throws KineticException {
		List<ACL> acls = new ArrayList<ACL>();

		ACL acl1 = new ACL();
		acl1.setUserId(1);
		acl1.setKey("asdfasdf");
		acl1.setAlgorithm("HmacSHA1");
		Domain domain = new Domain();

		List<Domain> domains = new ArrayList<Domain>();

		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.DELETE);
		roles.add(Role.GETLOG);
		roles.add(Role.READ);
		roles.add(Role.RANGE);
		roles.add(Role.SECURITY);
		roles.add(Role.SETUP);
		roles.add(Role.WRITE);

		domain.setRoles(roles);
		domains.add(domain);

		acl1.setDomains(domains);

		acls.add(acl1);

		getAdminClient().setSecurity(acls);

		KineticLog log = getAdminClient().getLog();
		assertTrue(log.getTemperature().size() > 0);
		assertTrue(log.getStatistics().size() > 0);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test set security API. The algorithm value is null, the result should be
	 * success.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void setSecurity_algorithmIsSupport_IsNullTest()
			throws KineticException {
		List<ACL> acls = new ArrayList<ACL>();

		ACL acl1 = new ACL();
		acl1.setUserId(1);
		acl1.setKey("asdfasdf");
		Domain domain = new Domain();

		List<Domain> domains = new ArrayList<Domain>();

		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.DELETE);
		roles.add(Role.GETLOG);
		roles.add(Role.READ);
		roles.add(Role.RANGE);
		roles.add(Role.SECURITY);
		roles.add(Role.SETUP);
		roles.add(Role.WRITE);

		domain.setRoles(roles);
		domains.add(domain);

		acl1.setDomains(domains);

		acls.add(acl1);
		getAdminClient().setSecurity(acls);

		KineticLog log = getAdminClient().getLog();
		assertTrue(log.getTemperature().size() > 0);
		assertTrue(log.getStatistics().size() > 0);

		logger.info(this.testEndInfo());
	}

	/**
	 * Test set security API. The algorithm value is empty, the result should be
	 * thrown exception.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void setSecurity_algorithmIsSupport_IsEmptyTest()
			throws KineticException {
		List<ACL> acls = new ArrayList<ACL>();

		ACL acl1 = new ACL();
		acl1.setUserId(1);
		acl1.setKey("asdfasdf");
		acl1.setAlgorithm("");
		Domain domain = new Domain();

		List<Domain> domains = new ArrayList<Domain>();

		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.DELETE);
		roles.add(Role.GETLOG);
		roles.add(Role.READ);
		roles.add(Role.RANGE);
		roles.add(Role.SECURITY);
		roles.add(Role.SETUP);
		roles.add(Role.WRITE);

		domain.setRoles(roles);
		domains.add(domain);

		acl1.setDomains(domains);

		acls.add(acl1);
		try {
			getAdminClient().setSecurity(acls);
			fail("no exception was thrown");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("No enum constant"));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test set security API. The algorithm value is invalid, the result should
	 * be thrown exception.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void setSecurity_algorithmIsNotSupport_IgnoreCaseTest()
			throws KineticException {
		List<ACL> acls = new ArrayList<ACL>();

		ACL acl1 = new ACL();
		acl1.setUserId(1);
		acl1.setKey("asdfasdf");
		acl1.setAlgorithm("hmacSha1");
		Domain domain = new Domain();

		List<Domain> domains = new ArrayList<Domain>();

		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.DELETE);
		roles.add(Role.GETLOG);
		roles.add(Role.READ);
		roles.add(Role.RANGE);
		roles.add(Role.SECURITY);
		roles.add(Role.SETUP);
		roles.add(Role.WRITE);

		domain.setRoles(roles);
		domains.add(domain);

		acl1.setDomains(domains);

		acls.add(acl1);
		try {
			getAdminClient().setSecurity(acls);
			fail("no exception was thrown");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("No enum constant"));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test set security API. The algorithm value is not enum constant, the
	 * result should be thrown exception.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void setSecurity_algorithmIsNotSupportTest() throws KineticException {
		List<ACL> acls = new ArrayList<ACL>();

		ACL acl1 = new ACL();
		acl1.setUserId(1);
		acl1.setKey("asdfasdf");
		acl1.setAlgorithm("hmacSha2");
		Domain domain = new Domain();

		List<Domain> domains = new ArrayList<Domain>();

		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.DELETE);
		roles.add(Role.GETLOG);
		roles.add(Role.READ);
		roles.add(Role.RANGE);
		roles.add(Role.SECURITY);
		roles.add(Role.SETUP);
		roles.add(Role.WRITE);

		domain.setRoles(roles);
		domains.add(domain);

		acl1.setDomains(domains);

		acls.add(acl1);
		try {
			getAdminClient().setSecurity(acls);
			fail("no exception was thrown");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("No enum constant"));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test set security API. No role set in domain, the result should be thrown
	 * exception.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void setSecurity_NoRoleSetInDomainTest() throws KineticException {
		List<ACL> acls = new ArrayList<ACL>();

		ACL acl1 = new ACL();
		acl1.setUserId(1);
		acl1.setKey("asdfasdf");
		acl1.setAlgorithm("HmacSHA1");
		Domain domain = new Domain();

		List<Domain> domains = new ArrayList<Domain>();
		domains.add(domain);

		acl1.setDomains(domains);

		acls.add(acl1);
		try {
			getAdminClient().setSecurity(acls);
			fail("no exception was thrown");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains("Paramter Exception"));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Test get log API. Check every log field value whether valid.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	@Test
	public void getLogNewAPI() throws KineticException {
		List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
		listOfLogType.add(KineticLogType.CAPACITIES);
		listOfLogType.add(KineticLogType.CONFIGURATION);
		listOfLogType.add(KineticLogType.MESSAGES);
		listOfLogType.add(KineticLogType.STATISTICS);
		listOfLogType.add(KineticLogType.TEMPERATURES);
		listOfLogType.add(KineticLogType.UTILIZATIONS);

		KineticLog log = getAdminClient().getLog(listOfLogType);

		Capacity capacity = log.getCapacity();
		assertTrue(capacity.getRemaining() >= 0);
		assertTrue(capacity.getTotal() >= 0);

		Configuration configuration = log.getConfiguration();
		assertTrue(configuration.getCompilationDate().length() > 0);
		assertTrue(configuration.getModel().length() > 0);
		assertTrue(configuration.getPort() >= 0);
		assertTrue(configuration.getTlsPort() >= 0);
		assertTrue(configuration.getSerialNumber().length() > 0);
		assertTrue(configuration.getSourceHash().length() > 0);
		assertTrue(configuration.getVendor().length() > 0);
		assertTrue(configuration.getVersion().length() > 0);

		List<Interface> interfaceOfList = configuration.getInterfaces();
		for (Interface interfaces : interfaceOfList) {
			// System.out.println("Interface: name: " + interfaces.getName());
			// System.out.println("Interface: IPV4: "
					// + new String(interfaces.getIpv4Address()));
			// System.out.println("Interface: IPV6: "
			// + new String(interfaces.getIpv6Address()));
			// System.out.println("Interface: MAC: "
			// + new String(interfaces.getMAC()));
			assertTrue(interfaces.getName().length() > 0);
			// assertTrue(interfaces.getIpv4Address().length() > 0);
		}

		byte[] messages = log.getMessages();
		assertTrue(messages.length > 0);

		List<Statistics> statisticsOfList = log.getStatistics();
		for (Statistics statistics : statisticsOfList) {
			assertTrue(statistics.getBytes() >= 0);
			assertTrue(statistics.getCount() >= 0);
			// assertTrue(statistics.getMessageType().);
		}

		List<Temperature> tempOfList = log.getTemperature();
		for (Temperature temperature : tempOfList) {
			assertTrue(temperature.getName().equals("HDA")
					|| temperature.getName().equals("CPU"));
			assertTrue(temperature.getCurrent() >= 0);
			assertTrue(temperature.getMax() >= 0);
			assertTrue(temperature.getMin() >= 0);
			assertTrue(temperature.getTarget() >= 0);
		}

		List<Utilization> utilOfList = log.getUtilization();
		for (Utilization util : utilOfList) {
			assertTrue(util.getName().equals("HDA")
					|| util.getName().equals("EN0")
					|| util.getName().equals("EN1")
					|| util.getName().equals("CPU"));

			assertTrue(util.getUtility() >= 0);
		}

		KineticLogType[] logTypes = log.getContainedLogTypes();
		assertEquals(listOfLogType.size(), logTypes.length);

		for (int i = 0; i < logTypes.length; i++) {
			assertTrue(listOfLogType.contains(logTypes[i]));
		}

		logger.info(this.testEndInfo());
	}

	/**
	 * Reset cluster version method.
	 * <p>
	 *
	 * @throws KineticException
	 *             if any internal error occurred.
	 *
	 */
	private void resetClusterVersion(int currentClusterVersion)
			throws KineticException {
		final KineticAdminClient client = KineticAdminClientFactory
				.createInstance(getClientConfig(currentClusterVersion));
		client.setup(null, null, 0, false);
		client.close();
	}

}
