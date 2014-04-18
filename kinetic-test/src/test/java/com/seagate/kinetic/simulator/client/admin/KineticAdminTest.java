package com.seagate.kinetic.simulator.client.admin;

import static com.seagate.kinetic.KineticTestHelpers.cleanPin;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import kinetic.admin.ACL;
import kinetic.admin.Capacity;
import kinetic.admin.Configuration;
import kinetic.admin.Domain;
import kinetic.admin.Interface;
import kinetic.admin.KineticLog;
import kinetic.admin.KineticLogType;
import kinetic.admin.Role;
import kinetic.admin.Statistics;
import kinetic.admin.Temperature;
import kinetic.admin.Utilization;
import kinetic.client.KineticException;

import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;

/**
 * 
 * KineticAdminClient test case
 * <p>
 * 
 * @author Chenchong Li
 * 
 */
public class KineticAdminTest extends IntegrationTestCase {

	// @Test
	public void setupTest() throws KineticException {
		getAdminClient().setup(null, "pin001".getBytes(), 0, true);

        getAdminClient().setup("pin001".getBytes(), "pin002".getBytes(), 0, true);

        cleanPin("pin002", this.getAdminClient());
	}

	@Test
	public void getLogTest() throws KineticException {
		KineticLog log = getAdminClient().getLog();

		assertTrue(log.getTemperature().size() > 0);
		assertTrue(log.getUtilization().size() > 0);

	}

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
	}

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
		try {
            getAdminClient().setSecurity(acls);
			assertTrue(true);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fail("no exception was thrown");
		}

	}

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
		try {
            getAdminClient().setSecurity(acls);
			assertTrue(true);
		} catch (Exception e) {
			fail("no exception was thrown");
		}
	}

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
			assertTrue(true);
		}

	}

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
			assertTrue(true);
		}

	}

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
			assertTrue(true);
		}

	}

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
			// System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	@SuppressWarnings("unused")
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
		// System.out.println("Capacity: remaining: " +
		// capacity.getRemaining());
		// System.out.println("Capacity: total: " + capacity.getTotal());

		Configuration configuration = log.getConfiguration();
		// System.out.println("Configuration: compilationDate: "
		// + configuration.getCompilationDate());
		// System.out.println("Configuration: mode: " +
		// configuration.getModel());
		// System.out.println("Configuration: port: " +
		// configuration.getPort());
		// System.out.println("Configuration: sourceHash: "
		// + configuration.getSourceHash());
		// System.out.println("Configuration: tlsport: "
		// + configuration.getTlsPort());
		// System.out.println("Configuration: vendor: "
		// + configuration.getVendor());
		// System.out.println("Configuration: version: "
		// + configuration.getVersion());
		// System.out.println("Configuration: serialNumber: "
		// + configuration.getSerialNumber());

		List<Interface> interfaceOfList = configuration.getInterfaces();
		for (Interface interfaces : interfaceOfList) {
			// System.out.println("Interface: name: " + interfaces.getName());
			// System.out.println("Interface: IPV4: "
			// + new String(interfaces.getIpv4Address()));
			// System.out.println("Interface: IPV6: "
			// + new String(interfaces.getIpv6Address()));
			// System.out.println("Interface: MAC: "
			// + new String(interfaces.getMAC()));
		}

		byte[] messages = log.getMessages();
		// System.out.println("Message: " + new String(messages));

		List<Statistics> statisticsOfList = log.getStatistics();
		for (Statistics statistics : statisticsOfList) {
			// System.out.println("Statistics: bytes: " +
			// statistics.getBytes());
			// System.out.println("Statistics: counts: " +
			// statistics.getCount());
			// System.out.println("Statistics: messageType: "
			// + statistics.getMessageType());
		}

		List<Temperature> tempOfList = log.getTemperature();
		for (Temperature temperature : tempOfList) {
			// System.out.println("Temperature: name: " +
			// temperature.getName());
			// System.out.println("Temperature: current: "
			// + temperature.getCurrent());
			// System.out.println("Temperature: max: " + temperature.getMax());
			// System.out.println("Temperature: min: " + temperature.getMin());
			// System.out.println("Temperature: target: "
			// + temperature.getTarget());
		}

		List<Utilization> utilOfList = new ArrayList<Utilization>();
		for (Utilization util : utilOfList) {
			// System.out.println("Utilization: name" + util.getName());
			// System.out.println("Utilization: utility" + util.getUtility());
		}

		KineticLogType[] logTypes = log.getContainedLogTypes();
		for (int i = 0; i < logTypes.length; i++) {
			// System.out.println("KineticLogType: " + logTypes[i]);
		}
	}
}
