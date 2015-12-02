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
package com.seagate.kinetic.adminAPI;

import static com.seagate.kinetic.KineticTestHelpers.instantErase;
import static com.seagate.kinetic.KineticTestHelpers.setDefaultAcls;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.admin.ACL;
import kinetic.admin.Capacity;
import kinetic.admin.Configuration;
import kinetic.admin.Device;
import kinetic.admin.Domain;
import kinetic.admin.Interface;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.admin.KineticLog;
import kinetic.admin.KineticLogType;
import kinetic.admin.Limits;
import kinetic.admin.Role;
import kinetic.admin.Statistics;
import kinetic.admin.Temperature;
import kinetic.admin.Utilization;
import kinetic.client.ClusterVersionFailureException;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.EntryNotFoundException;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Priority;
import com.seagate.kinetic.proto.Kinetic.Command.Range;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

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
@Test(groups = { "simulator", "drive" })
public class KineticAdminTest extends IntegrationTestCase {
    private static final Logger logger = IntegrationTestLoggerFactory
            .getLogger(KineticAdminTest.class.getName());

    private final byte[] INIT_KEY = toByteArray("0");
    private final byte[] INIT_VALUE = toByteArray("0");
    private final byte[] INIT_VERSION = toByteArray("0");
    private final long DEFAULT_CLUSTER_VERSION = 0;
    private String oldPin = System.getProperty("OLD_PIN", "");
    private String newPin = System.getProperty("NEW_PIN", "123");

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
        entryMetadata.setVersion(INIT_VERSION);

        KineticClient client = KineticClientFactory
                .createInstance(getClientConfig());
        client.delete(new Entry(toByteArray("key"), toByteArray("value"),
                entryMetadata));
        entryMetadata = new EntryMetadata();
        client.put(new Entry(toByteArray("key"), toByteArray("value"),
                entryMetadata), toByteArray("0"));

        instantErase(oldPin, newPin, getAdminClient());

        assertNull(client.get("key".getBytes()));

        client.close();

        logger.info(this.testEndInfo());
    }

    /**
     * Test setClusterVersion API, set cluster version for simulator/drive. The
     * result should be true.
     * <p>
     */
    @Test
    public void testSetClusterVersion() {
        long newClusterVersion = 1;

        // modify cluster version.
        try {
            getAdminClient().setClusterVersion(newClusterVersion);

            // set to default cluster version
            resetClusterVersionToDefault(newClusterVersion);

        } catch (KineticException e) {
            Assert.fail(e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test setup API, modify cluster version for simulator/drive, restart
     * simulator/drive, then modify again with wrong administrator client. The
     * result should be thrown exception.
     * <p>
     */
    @Test
    public void testSetup_ModifyClusterVersion_UseWrongAdminClientModifyAgain() {
        long newClusterVersion = 1;
        long modifyClusterVersion = 2;

        try {
            getAdminClient().setClusterVersion(newClusterVersion);
        } catch (KineticException e1) {
            Assert.fail("set cluster version throw exception: "
                    + e1.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            Assert.fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().setClusterVersion(modifyClusterVersion);
            Assert.fail("Should have thrown");
        } catch (KineticException e) {
            long requestClusterVersion = e.getRequestMessage().getCommand()
                    .getHeader().getClusterVersion();

            long responseClusterVersion = e.getResponseMessage().getCommand()
                    .getHeader().getClusterVersion();

            logger.info("caught expected exception, this is ok. request cluster version="
                    + requestClusterVersion
                    + ", respose cluster version="
                    + responseClusterVersion);
        } catch (Exception e) {
            Assert.fail("should have caught ClusterVersionException");
        }

        try {
            resetClusterVersionToDefault(newClusterVersion);
        } catch (KineticException e) {
            Assert.fail("reset cluster version to default throw exception: "
                    + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test setup API, set cluster version for simulator/drive first, then reset
     * it with correct cluster version. The result should be fine.
     * <p>
     * 
     */
    @Test
    public void testSetup_ClusterVersion() {
        long newClusterVersion = 1;

        try {
            getAdminClient().setClusterVersion(newClusterVersion);
        } catch (KineticException e1) {
            Assert.fail("set cluster version throw exception: "
                    + e1.getMessage());
        }

        try {
            resetClusterVersionToDefault(newClusterVersion);
        } catch (KineticException e) {
            Assert.fail("Should have thrown");
        }

    }

    /**
     * Test setup API, set cluster version for simulator/drive first, then erase
     * data with wrong cluster version. The result should be fine.
     * <p>
     * 
     */
    @Test
    public void testSetup_ClusterVersionHonored() {
        long newClusterVersion = 1;

        try {
            getAdminClient().setClusterVersion(newClusterVersion);
        } catch (KineticException e1) {
            Assert.fail("set cluster version throw exception: "
                    + e1.getMessage());
        }

        try {
            resetClusterVersionToDefault(DEFAULT_CLUSTER_VERSION);
        } catch (ClusterVersionFailureException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.VERSION_FAILURE));

            long requestClusterVersion = e.getRequestMessage().getCommand()
                    .getHeader().getClusterVersion();

            long responseClusterVersion = e.getResponseMessage().getCommand()
                    .getHeader().getClusterVersion();

            logger.info("caught expected exception, this is ok. request cluster version="
                    + requestClusterVersion
                    + ", respose cluster version="
                    + responseClusterVersion);
        } catch (Exception e1) {
            Assert.fail("Throw wrong exception. " + e1.getMessage());
        }
        
        // reset cluster version to default one.
        try {
            resetClusterVersionToDefault(newClusterVersion);
        } catch (KineticException e){
            Assert.fail("reset cluster version to default one throw exception! " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test setup API, modify cluster version for simulator/drive, restart
     * simulator/drive, then modify again with correct administrator client. The
     * result should be success.
     * <p>
     * 
     */
    @Test
    public void testSetup_ModifyClusterVersion_UseRightAdminClientModifyAgain() {
        long newClusterVersion = 1;

        try {
            getAdminClient().setClusterVersion(newClusterVersion);
        } catch (KineticException e1) {
            Assert.fail("set cluster version throw exception: "
                    + e1.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            Assert.fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            // Perform ISE. New cluster version should be ignored.
            resetClusterVersionToDefault(newClusterVersion);
        } catch (KineticException e) {
            Assert.fail("Should have thrown");
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test get log API. The result should be success.
     * <p>
     */
    @Test
    public void test_GetLogTest() {
        KineticLog log;
        try {
            log = getAdminClient().getLog();

            assertTrue(log.getTemperature().size() > 0);
            assertTrue(log.getUtilization().size() > 0);
            assertTrue(log.getStatistics().size() > 0);
            assertTrue(log.getMessages().length > 0);

            assertTrue(log.getCapacity().getPortionFull() >= 0);
            assertTrue(log.getCapacity().getNominalCapacityInBytes() >= 0);

            assertTrue(log.getLimits().getMaxKeySize() == 4096);
            assertTrue(log.getLimits().getMaxValueSize() == 1024 * 1024);
            assertTrue(log.getLimits().getMaxVersionSize() == 2048);
            assertTrue(log.getLimits().getMaxKeyRangeCount() == 200);
            // // TODO: To be validated
            // assertTrue(log.getLimits().getMaxTagSize() >= 0);
            // assertTrue(log.getLimits().getMaxOutstandingReadRequests() >= 0);
            // assertTrue(log.getLimits().getMaxOutstandingWriteRequests() >=
            // 0);
            // assertTrue(log.getLimits().getMaxConnections() >= 0);
            // assertTrue(log.getLimits().getMaxMessageSize() >= 0);
            // assertTrue(log.getLimits().getMaxKeyRangeCount() >= 0);

            logger.info("get max identity count: "
                    + log.getLimits().getMaxIdentityCount());
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test getVendorSpecificDeviceLog API. The device name is simulator's
     * drive.
     * <p>
     */
    @Test
    public void testGetVendorSpecificDeviceLog_ForSimulator() {
        KineticAdminClient aclient = getAdminClient();

        byte[] name = null;
        // name supported by the simulator only
        String sname = "com.seagate.simulator:dummy";

        // name not supported by anyone
        String sname2 = "com.seagate.simulator:badName";

        byte[] name2 = null;
        name = toByteArray(sname);
        name2 = toByteArray(sname2);

        try {
            Device device = aclient.getVendorSpecificDeviceLog(name);

            logger.info("got vendor specific log., name = " + sname
                    + ", log size=" + device.getValue().length);
        } catch (EntryNotFoundException enfe) {
            // could happen if this the service is not simulator
            logger.info("device log name not found for name: " + sname);
        } catch (KineticException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        try {
            aclient.getVendorSpecificDeviceLog(name2);

            Assert.fail("should have caught EntryNotFoundException");
        } catch (EntryNotFoundException enfe) {
            // could happen if this the service is not simulator
            logger.info("device log name not found for name: " + sname2);
        } catch (KineticException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            Assert.fail("should have caught EntryNotFoundException");
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test getVendorSpecificDeviceLog API. The device name is drive's name.
     * <p>
     */
    @Test
    public void testGetVendorSpecificDeviceLog_ForDrive() {
        KineticAdminClient aclient = getAdminClient();

        byte[] name = null;
        // name supported by the simulator only
        String sname = "com.Seagate.Kinetic.HDD.Gen1";

        // name not supported by anyone
        String sname2 = "com.seagate.Kinetic.HDD.badName";

        byte[] name2 = null;
        name = toByteArray(sname);
        name2 = toByteArray(sname2);

        try {
            Device device = aclient.getVendorSpecificDeviceLog(name);

            logger.info("got vendor specific log., name = " + sname
                    + ", log size=" + device.getValue().length);
        } catch (EntryNotFoundException enfe) {
            // could happen if this the service is not simulator
            logger.info("device log name not found for name: " + sname);
        } catch (KineticException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        try {
            aclient.getVendorSpecificDeviceLog(name2);

            Assert.fail("should have caught EntryNotFoundException");
        } catch (EntryNotFoundException enfe) {
            // could happen if this the service is not simulator
            logger.info("device log name not found for name: " + sname2);
        } catch (KineticException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            Assert.fail("should have caught EntryNotFoundException");
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security API. The result should be success. If failed, throw
     * KineticException.
     * <p>
     */
    @Test
    public void setSecurity() {
        List<ACL> acls = new ArrayList<ACL>();
        acls = setDefaultAcls();

        // all pins set the same
        try {
            getAdminClient().setAcl(acls);
        } catch (KineticException e) {
            Assert.fail("Set Security throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security API. The algorithm value is valid, the result should be
     * success.
     * <p>
     */
    @Test
    public void setSecurity_algorithmIsSupportTest() {
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
            getAdminClient().setAcl(acls);
        } catch (KineticException e) {
            Assert.fail("Set Security throw exception");
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security API. The algorithm value is null, the result should be
     * success.
     * <p>
     */
    @Test
    public void setSecurity_algorithmIsSupport_IsNullTest() {
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
            getAdminClient().setAcl(acls);
        } catch (KineticException e) {
            Assert.fail("Set Security throw exception");
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security API. The algorithm value is empty, the result should be
     * thrown exception.
     * <p>
     */
    @Test
    public void setSecurity_algorithmIsSupport_IsEmptyTest() {
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
            getAdminClient().setAcl(acls);
            Assert.fail("no exception was thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("No enum constant"));
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security API. The algorithm value is invalid, the result should
     * be thrown exception.
     * <p>
     */
    @Test
    public void setSecurity_algorithmIsNotSupport_IgnoreCaseTest() {
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
            getAdminClient().setAcl(acls);
            Assert.fail("no exception was thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("No enum constant"));
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security API. The algorithm value is not enum constant, the
     * result should be thrown exception.
     * <p>
     */
    @Test
    public void setSecurity_algorithmIsNotSupportTest() {
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
            getAdminClient().setAcl(acls);
            Assert.fail("no exception was thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("No enum constant"));
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security API. No role set in domain, the result should be thrown
     * exception.
     * <p>
     */
    @Test
    public void setSecurity_NoRoleSetInDomainTest() {
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
            getAdminClient().setAcl(acls);
            Assert.fail("no exception was thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Paramter Exception"));
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security API. Muti users with different roles set in domain.
     * <p>
     */
    @Test
    public void testMultiUsersACLs_VerifyRoles() {
        // client 1 has all roles
        List<Role> roles1 = new ArrayList<Role>();
        roles1.add(Role.DELETE);
        roles1.add(Role.GETLOG);
        roles1.add(Role.READ);
        roles1.add(Role.RANGE);
        roles1.add(Role.SECURITY);
        roles1.add(Role.SETUP);
        roles1.add(Role.WRITE);
        roles1.add(Role.P2POP);

        Domain domain1 = new Domain();
        domain1.setRoles(roles1);

        List<Domain> domains1 = new ArrayList<Domain>();
        domains1.add(domain1);

        List<ACL> acls = new ArrayList<ACL>();
        ACL acl1 = new ACL();
        acl1.setDomains(domains1);
        acl1.setUserId(1);
        acl1.setKey("asdfasdf");

        acls.add(acl1);

        // client 2 only has read permission
        List<Role> roles2 = new ArrayList<Role>();
        roles2.add(Role.READ);

        Domain domain2 = new Domain();
        domain2.setRoles(roles2);

        List<Domain> domains2 = new ArrayList<Domain>();
        domains2.add(domain2);

        ACL acl2 = new ACL();
        acl2.setDomains(domains2);
        acl2.setUserId(2);
        acl2.setKey("asdfasdf2");

        acls.add(acl2);

        // client 3 only has write permission
        List<Role> roles3 = new ArrayList<Role>();
        roles3.add(Role.WRITE);

        Domain domain3 = new Domain();
        domain3.setRoles(roles3);

        List<Domain> domains3 = new ArrayList<Domain>();
        domains3.add(domain3);

        ACL acl3 = new ACL();
        acl3.setDomains(domains3);
        acl3.setUserId(3);
        acl3.setKey("asdfasdf3");

        acls.add(acl3);

        // client 4 only has delete permission
        List<Role> roles4 = new ArrayList<Role>();
        roles4.add(Role.DELETE);

        Domain domain4 = new Domain();
        domain4.setRoles(roles4);

        List<Domain> domains4 = new ArrayList<Domain>();
        domains4.add(domain4);

        ACL acl4 = new ACL();
        acl4.setDomains(domains4);
        acl4.setUserId(4);
        acl4.setKey("asdfasdf4");

        acls.add(acl4);

        // client 5 only has read and write permission
        List<Role> roles5 = new ArrayList<Role>();
        roles5.add(Role.READ);
        roles5.add(Role.WRITE);

        Domain domain5 = new Domain();
        domain5.setRoles(roles5);

        List<Domain> domains5 = new ArrayList<Domain>();
        domains5.add(domain5);

        ACL acl5 = new ACL();
        acl5.setDomains(domains5);
        acl5.setUserId(5);
        acl5.setKey("asdfasdf5");

        acls.add(acl5);

        // client 6 only has read and delete permission
        List<Role> roles6 = new ArrayList<Role>();
        roles6.add(Role.READ);
        roles6.add(Role.DELETE);

        Domain domain6 = new Domain();
        domain6.setRoles(roles6);

        List<Domain> domains6 = new ArrayList<Domain>();
        domains6.add(domain6);

        ACL acl6 = new ACL();
        acl6.setDomains(domains6);
        acl6.setUserId(6);
        acl6.setKey("asdfasdf6");

        acls.add(acl6);

        // client 7 only has write and delete permission
        List<Role> roles7 = new ArrayList<Role>();
        roles7.add(Role.WRITE);
        roles7.add(Role.DELETE);

        Domain domain7 = new Domain();
        domain7.setRoles(roles7);

        List<Domain> domains7 = new ArrayList<Domain>();
        domains7.add(domain7);

        ACL acl7 = new ACL();
        acl7.setDomains(domains7);
        acl7.setUserId(7);
        acl7.setKey("asdfasdf7");

        acls.add(acl7);

        EntryMetadata entryMetadata1 = new EntryMetadata();
        KineticClient kineticClient;
        try {
            kineticClient = KineticClientFactory
                    .createInstance(getClientConfig());
            kineticClient.deleteForced(toByteArray("0"));
            kineticClient.put(new Entry(toByteArray("0"), toByteArray("0"),
                    entryMetadata1), toByteArray("0"));
            kineticClient.close();
        } catch (KineticException e2) {
            Assert.fail("Prepare for test data put failed. " + e2.getMessage());
        }

        try {
            getAdminClient().setAcl(acls);
        } catch (KineticException e1) {
            Assert.fail("set security throw exception: " + e1.getMessage());
        }

        // client1 can do read write and delete
        KineticClient kineticClient1 = null;
        try {
            kineticClient1 = KineticClientFactory
                    .createInstance(getClientConfig(1, "asdfasdf"));
        } catch (KineticException e1) {
            Assert.fail("create kinetic client throw exception: "
                    + e1.getMessage());
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient1.put(new Entry("123".getBytes(), "456".getBytes(),
                    entryMetadata), "789".getBytes());
        } catch (Exception e) {
            Assert.fail("put operation throw exception" + e.getMessage());
        }

        Entry vGet = null;
        try {
            vGet = kineticClient1.get("123".getBytes());
        } catch (Exception e) {
            Assert.fail("get operation throw exception" + e.getMessage());
        }

        try {
            kineticClient1.delete(vGet);
        } catch (Exception e) {
            Assert.fail("delete operation throw exception" + e.getMessage());
        }

        try {
            kineticClient1.close();
        } catch (KineticException e1) {
            Assert.fail("close kinetic client throw exception: "
                    + e1.getMessage());
        }

        // client2 can do read, can not do write and delete
        KineticClient kineticClient2 = null;
        try {
            kineticClient2 = KineticClientFactory
                    .createInstance(getClientConfig(2, "asdfasdf2"));
        } catch (KineticException e1) {
            Assert.fail("create kinetic client throw exception: "
                    + e1.getMessage());
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient2.put(new Entry("123".getBytes(), "456".getBytes(),
                    entryMetadata), "789".getBytes());
            Assert.fail("client2 does not have write rights, but can write, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            vGet = kineticClient2.get(INIT_KEY);
        } catch (Exception e) {
            Assert.fail("get operation throw exception" + e.getMessage());
        }

        try {
            kineticClient2.delete(vGet);
            Assert.fail("client2 does not have delete rights, but can delete, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            kineticClient2.close();
        } catch (KineticException e1) {
            Assert.fail("close kinetic client throw exception: "
                    + e1.getMessage());
        }

        // client3 can do write, can not do read and delete
        KineticClient kineticClient3 = null;
        try {
            kineticClient3 = KineticClientFactory
                    .createInstance(getClientConfig(3, "asdfasdf3"));
        } catch (KineticException e1) {
            Assert.fail("create kinetic client throw exception: "
                    + e1.getMessage());
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient3.put(v, INIT_VERSION);
        } catch (Exception e) {
            Assert.fail("client3 put operation throw exception"
                    + e.getMessage());
        }

        try {
            vGet = kineticClient3.get(INIT_KEY);
            Assert.fail("client3 does not have read rights, but can get, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            kineticClient3.delete(vGet);
            Assert.fail("client3 does not have delete rights, but can delete, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            kineticClient3.close();
        } catch (KineticException e1) {
            Assert.fail("close kinetic client throw exception: "
                    + e1.getMessage());
        }

        // client4 can do delete, can not do read and write
        KineticClient kineticClient4 = null;
        try {
            kineticClient4 = KineticClientFactory
                    .createInstance(getClientConfig(4, "asdfasdf4"));
        } catch (KineticException e1) {
            Assert.fail("create kinetic client throw exception: "
                    + e1.getMessage());
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient4.put(v, INIT_VERSION);
            Assert.fail("client4 does not have write rights, but can put, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            vGet = kineticClient4.get(INIT_KEY);
            Assert.fail("client4 does not have read rights, but can get, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient4.delete(v);
        } catch (Exception e) {
            Assert.fail("client4 delete operation throw exception"
                    + e.getMessage());
        }

        try {
            kineticClient4.close();
        } catch (KineticException e1) {
            Assert.fail("close kinetic client throw exception: "
                    + e1.getMessage());
        }

        // client5 can do read and write, can not do delete
        KineticClient kineticClient5 = null;
        try {
            kineticClient5 = KineticClientFactory
                    .createInstance(getClientConfig(5, "asdfasdf5"));
        } catch (KineticException e1) {
            Assert.fail("create kinetic client throw exception: "
                    + e1.getMessage());
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient5.put(v, INIT_VERSION);
        } catch (Exception e) {
            Assert.fail("client5 put operation throw exception"
                    + e.getMessage());
        }

        try {
            vGet = kineticClient5.get(INIT_KEY);
        } catch (Exception e) {
            Assert.fail("client5 get operation throw exception"
                    + e.getMessage());
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient5.delete(v);
            Assert.fail("client5 does not have delete rights, but can delete, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            kineticClient5.close();
        } catch (KineticException e1) {
            Assert.fail("close kinetic client throw exception: "
                    + e1.getMessage());
        }

        // client6 can do read and delete, can not do write
        KineticClient kineticClient6 = null;
        try {
            kineticClient6 = KineticClientFactory
                    .createInstance(getClientConfig(6, "asdfasdf6"));
        } catch (KineticException e1) {
            Assert.fail("create kinetic client throw exception: "
                    + e1.getMessage());
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient6.put(v, INIT_VERSION);
            Assert.fail("client6 does not have write rights, but can put, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            vGet = kineticClient6.get(INIT_KEY);
        } catch (Exception e) {
            Assert.fail("client5 get operation throw exception"
                    + e.getMessage());
        }

        try {
            kineticClient6.delete(vGet);
        } catch (Exception e) {
            Assert.fail("client6 delete operation throw exception"
                    + e.getMessage());
        }

        try {
            kineticClient6.close();
        } catch (KineticException e1) {
            Assert.fail("close kinetic client throw exception: "
                    + e1.getMessage());
        }

        // client7 can do write and delete, can not do read
        KineticClient kineticClient7 = null;
        try {
            kineticClient7 = KineticClientFactory
                    .createInstance(getClientConfig(7, "asdfasdf7"));
        } catch (KineticException e1) {
            Assert.fail("create kinetic client throw exception: "
                    + e1.getMessage());
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient7.put(v, INIT_VERSION);
        } catch (Exception e) {
            Assert.fail("client7 put operation throw exception"
                    + e.getMessage());
        }

        try {
            vGet = kineticClient7.get(INIT_KEY);
            Assert.fail("client7 does not have read rights, but can get, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient7.delete(v);
        } catch (Exception e) {
            Assert.fail("client7 delete operation throw exception"
                    + e.getMessage());
        }

        try {
            kineticClient7.close();
        } catch (KineticException e1) {
            Assert.fail("close kinetic client throw exception: "
                    + e1.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, get key range without range operation permit.
     * <p>
     */
    @Test
    public void testACLs_VerifyRange() {
        // client has all roles without range
        List<Role> roles1 = new ArrayList<Role>();
        roles1.add(Role.DELETE);
        roles1.add(Role.GETLOG);
        roles1.add(Role.READ);
        roles1.add(Role.SECURITY);
        roles1.add(Role.SETUP);
        roles1.add(Role.WRITE);
        roles1.add(Role.P2POP);

        Domain domain1 = new Domain();
        domain1.setRoles(roles1);

        List<Domain> domains1 = new ArrayList<Domain>();
        domains1.add(domain1);

        List<ACL> acls = new ArrayList<ACL>();
        ACL acl1 = new ACL();
        acl1.setDomains(domains1);
        acl1.setUserId(1);
        acl1.setKey("asdfasdf");

        acls.add(acl1);

        try {
            getAdminClient().setAcl(acls);
        } catch (KineticException e1) {
            Assert.fail("set security throw exception: " + e1.getMessage());
        }

        // client1 can not do range
        KineticClient kineticClient1 = null;
        try {
            kineticClient1 = KineticClientFactory
                    .createInstance(getClientConfig(1, "asdfasdf"));
        } catch (KineticException e1) {
            Assert.fail("create kinetic client throw exception: "
                    + e1.getMessage());
        }

        try {
            kineticClient1.getKeyRange("123".getBytes(), true,
                    "456".getBytes(), true, 10);
            Assert.fail("should thrown exception!");
        } catch (KineticException e) {
            assertEquals(e.getResponseMessage().getCommand().getStatus()
                    .getCode(), StatusCode.NOT_AUTHORIZED);
        }
    }

    /**
     * Test set security API. Single user with multi domain.
     * <p>
     */
    @Test
    public void testSingleUserACL_WithMultiDomain_VerifyRolesInDifferentDomain() {
        byte[] value = toByteArray("456");
        byte[] version = toByteArray("0");

        List<ACL> acls = new ArrayList<ACL>();

        ACL aclAdmin = new ACL();
        aclAdmin.setUserId(1);
        aclAdmin.setKey("asdfasdf");
        aclAdmin.setAlgorithm("HmacSHA1");
        Domain domain = new Domain();

        List<Domain> domainsAdmin = new ArrayList<Domain>();

        List<Role> roles = new ArrayList<Role>();
        roles.add(Role.DELETE);
        roles.add(Role.GETLOG);
        roles.add(Role.READ);
        roles.add(Role.RANGE);
        roles.add(Role.SECURITY);
        roles.add(Role.SETUP);
        roles.add(Role.WRITE);

        domain.setRoles(roles);
        domainsAdmin.add(domain);

        aclAdmin.setDomains(domainsAdmin);

        acls.add(aclAdmin);

        ACL acl = new ACL();
        acl.setUserId(2);
        acl.setKey("asdfasdf2");
        acl.setAlgorithm("HmacSHA1");

        List<Domain> domains = new ArrayList<Domain>();
        Domain domain0 = new Domain();
        List<Role> roles0 = new ArrayList<Role>();
        roles0.add(Role.READ);
        domain0.setRoles(roles0);
        domain0.setOffset(0);
        domain0.setValue(("domain0"));
        domains.add(domain0);

        Domain domain1 = new Domain();
        List<Role> roles1 = new ArrayList<Role>();
        roles1.add(Role.READ);
        roles1.add(Role.WRITE);
        domain1.setRoles(roles1);
        domain1.setOffset(1);
        domain1.setValue(("domain1"));
        domains.add(domain1);

        Domain domain2 = new Domain();
        List<Role> roles2 = new ArrayList<Role>();
        roles2.add(Role.READ);
        roles2.add(Role.DELETE);
        domain2.setRoles(roles2);
        domain2.setOffset(2);
        domain2.setValue(("domain2"));
        domains.add(domain2);

        Domain domain3 = new Domain();
        List<Role> roles3 = new ArrayList<Role>();
        roles3.add(Role.WRITE);
        domain3.setRoles(roles3);
        domain3.setOffset(3);
        domain3.setValue(("domain3"));
        domains.add(domain3);

        Domain domain4 = new Domain();
        List<Role> roles4 = new ArrayList<Role>();
        roles4.add(Role.WRITE);
        roles4.add(Role.DELETE);
        domain4.setRoles(roles4);
        domain4.setOffset(4);
        domain4.setValue(("domain4"));
        domains.add(domain4);

        Domain domain5 = new Domain();
        List<Role> roles5 = new ArrayList<Role>();
        roles5.add(Role.DELETE);
        domain5.setRoles(roles5);
        domain5.setOffset(5);
        domain5.setValue(("domain5"));
        domains.add(domain5);

        Domain domain6 = new Domain();
        List<Role> roles6 = new ArrayList<Role>();
        roles6.add(Role.DELETE);
        roles6.add(Role.READ);
        roles6.add(Role.WRITE);
        domain6.setRoles(roles6);
        domain6.setOffset(6);
        domain6.setValue(("domain6"));
        domains.add(domain6);

        acl.setDomains(domains);

        acls.add(acl);

        try {
            getAdminClient().setAcl(acls);
        } catch (KineticException e1) {
            Assert.fail("set security throw exception: " + e1.getMessage());
        }

        KineticClient kineticClient = null;
        try {
            kineticClient = KineticClientFactory
                    .createInstance(getClientConfig(2, "asdfasdf2"));
        } catch (KineticException e1) {
            Assert.fail("create kineticClient throw exception: "
                    + e1.getMessage());
        }

        // operation in scope 0
        try {
            byte[] key1 = toByteArray("domain0key000");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key1, value, entryMetadata), version);
            Assert.fail("The user does not have write rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            byte[] key1 = toByteArray("domain0key000");
            assertTrue(null == kineticClient.get(key1));

        } catch (Exception e) {
            Assert.fail("get exception" + e.getMessage());
        }

        try {
            byte[] key1 = toByteArray("domain0key000");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.delete(new Entry(key1, value, entryMetadata));
            Assert.fail("The user do not have delete rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        // operation in scope 1
        byte[] key1 = null;
        try {
            key1 = toByteArray("adomain1key001");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key1, value, entryMetadata), version);
        } catch (Exception e) {
            Assert.fail("put in domain1 exception" + e.getMessage());
        }
        Entry vGet = null;
        try {
            vGet = kineticClient.get(key1);
            assertArrayEquals(key1, vGet.getKey());
            assertArrayEquals(value, vGet.getValue());
        } catch (Exception e) {
            Assert.fail("get exception" + e.getMessage());
        }

        try {
            kineticClient.delete(vGet);
            Assert.fail("The user do not have delete rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        // operation in scope 2
        byte[] key2 = null;
        try {
            key2 = toByteArray("abdomain2key002");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key2, value, entryMetadata), version);
            Assert.fail("The user does not have put rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }
        try {
            vGet = kineticClient.get(key2);
            assertEquals(null, vGet);
        } catch (Exception e) {
            Assert.fail("get exception" + e.getMessage());
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(version);
            Entry vDel = new Entry(key2, value, entryMetadata);
            kineticClient.delete(vDel);
        } catch (Exception e) {
            Assert.fail("get exception" + e.getMessage());
        }

        // operation in scope 3
        byte[] key3 = null;
        try {
            key3 = toByteArray("abcdomain3key003");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key3, value, entryMetadata), version);
        } catch (Exception e) {
            Assert.fail("put in domain3 exception" + e.getMessage());
        }
        try {
            vGet = kineticClient.get(key3);
            Assert.fail("The user do not have get rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }
        
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(version);
            Entry v = new Entry(key3, value, entryMetadata);
            kineticClient.delete(v);
            Assert.fail("The user do not have delete rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        // operation in scope 4
        byte[] key4 = null;
        try {
            key4 = toByteArray("abcddomain4key004");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key4, value, entryMetadata), version);
        } catch (Exception e) {
            Assert.fail("put in domain4 exception" + e.getMessage());
        }
        try {
            key4 = toByteArray("abcddomain4key004");
            vGet = kineticClient.get(key4);
            Assert.fail("The user do not have get rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(version);
            Entry v = new Entry(key4, value, entryMetadata);
            kineticClient.delete(v);
        } catch (Exception e) {
            Assert.fail("delete in domain4 exception" + e.getMessage());
        }

        // operation in scope 5
        byte[] key5 = null;
        try {
            key5 = toByteArray("abcdedomain5key005");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key5, value, entryMetadata), version);
            Assert.fail("The user do not have put rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }
        try {
            key5 = toByteArray("abcdedomain5key005");
            vGet = kineticClient.get(key5);
            Assert.fail("The user do not have get rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(version);
            Entry v = new Entry(key5, value, entryMetadata);
            kineticClient.delete(v);
        } catch (Exception e) {
            Assert.fail("delete in domain5 exception" + e.getMessage());
        }

        // operation in scope 6
        byte[] key6 = null;
        try {
            key6 = toByteArray("abcdefdomain6key006");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key6, value, entryMetadata), version);
        } catch (Exception e) {
            Assert.fail("put in domain6 exception" + e.getMessage());
        }
        try {
            vGet = kineticClient.get(key6);
            assertArrayEquals(key6, vGet.getKey());
            assertArrayEquals(value, vGet.getValue());
        } catch (Exception e) {
            Assert.fail("get in domain6 exception" + e.getMessage());
        }

        try {
            kineticClient.delete(vGet);
        } catch (Exception e) {
            Assert.fail("delete in domain6 exception" + e.getMessage());
        }

        // wrong scope operation
        byte[] key7 = null;
        try {
            key7 = toByteArray("domain7key007");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key7, value, entryMetadata), version);
            Assert.fail("The user do not have right domain");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        // key size smaller than scope
        byte[] key8 = null;
        try {
            key8 = toByteArray("key0");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key8, value, entryMetadata), version);
            Assert.fail("The key size is smaller than domain");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            kineticClient.close();
        } catch (KineticException e) {
            Assert.fail("close kienticClient throw exception: "
                    + e.getMessage());
        }
        
        // clean up data(key1 and key3)
        KineticClient kineticClientAd = null;
        try {
            kineticClientAd = KineticClientFactory
                    .createInstance(getClientConfig());
            kineticClientAd.deleteForced(key1);
            kineticClientAd.deleteForced(key3);
        } catch (KineticException e1) {
            Assert.fail("create kineticClient throw exception: "
                    + e1.getMessage());
        }

        logger.info(this.testEndInfo());

    }

    /**
     * Test set security API. domain with negative offset, the result should be
     * thrown exception.
     * <p>
     */
    @Test
    public void testNegativeDomainOffset() {
        List<ACL> acls = new ArrayList<ACL>();

        List<Role> roles = new ArrayList<Role>();
        roles.add(Role.WRITE);
        roles.add(Role.DELETE);

        Domain domain = new Domain();
        domain.setRoles(roles);
        domain.setOffset(-1);

        List<Domain> domains = new ArrayList<Domain>();
        domains.add(domain);

        ACL acl = new ACL();
        acl.setDomains(domains);
        acl.setUserId(2);
        acl.setKey("asdfasdf2");
        acl.setAlgorithm("HmacSHA1");

        acls.add(acl);

        try {
            getAdminClient().setAcl(acls);
            Assert.fail("should throw exception.");
        } catch (KineticException e1) {
            assertTrue(e1.getMessage() != null);
        }

        logger.info(this.testEndInfo());

    }

    /**
     * Test set security API. The algorithm value is default one, the result
     * should be succeed.
     * <p>
     */
    @Test
    public void testAlgorithm_DefaultAlgorithmHmacSha1() {

        // client 1 has all roles
        List<ACL> acls = new ArrayList<ACL>();

        List<Role> roles = new ArrayList<Role>();
        roles.add(Role.WRITE);
        roles.add(Role.DELETE);
        roles.add(Role.GETLOG);
        roles.add(Role.P2POP);
        roles.add(Role.RANGE);
        roles.add(Role.READ);
        roles.add(Role.SECURITY);
        roles.add(Role.SETUP);

        Domain domain = new Domain();
        domain.setRoles(roles);

        List<Domain> domains = new ArrayList<Domain>();
        domains.add(domain);

        ACL acl = new ACL();
        acl.setDomains(domains);
        acl.setUserId(1);
        acl.setKey("asdfasdf");
        acl.setAlgorithm("HmacSHA1");

        acls.add(acl);

        try {
            getAdminClient().setAcl(acls);
        } catch (KineticException e1) {
            Assert.fail("set security throw exception: " + e1.getMessage());
        }

        // client1 can do read write and delete
        KineticClient kineticClient1 = null;
        try {
            kineticClient1 = KineticClientFactory
                    .createInstance(getClientConfig(1, "asdfasdf"));
        } catch (KineticException e1) {
            Assert.fail("create kineticClient throw exception: "
                    + e1.getMessage());
        }
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient1.put(new Entry("123".getBytes(), "456".getBytes(),
                    entryMetadata), "789".getBytes());
        } catch (Exception e) {
            Assert.fail("put operation throw exception" + e.getMessage());
        }

        Entry vGet = null;
        try {
            vGet = kineticClient1.get("123".getBytes());
        } catch (Exception e) {
            Assert.fail("get operation throw exception" + e.getMessage());
        }

        try {
            kineticClient1.delete(vGet);
        } catch (Exception e) {
            Assert.fail("delete operation throw exception" + e.getMessage());
        }

        try {
            kineticClient1.close();
        } catch (KineticException e) {
            Assert.fail("close kineticClient throw exception: "
                    + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, set erase pin.
     * <p>
     */
    @Test
    public void testSetSecurity_setErasePin() {
        String erasePin = newPin;
        byte[] erasePinB = toByteArray(erasePin);

        try {
            getAdminClient().setErasePin(toByteArray(oldPin), erasePinB);
        } catch (KineticException e) {
            Assert.fail("Set erase pin throw exception" + e.getMessage());
        }

        // reset pin
        try {
            getAdminClient().setErasePin(erasePinB, toByteArray(oldPin));
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, modify erase pin.
     * <p>
     */
    @Test
    public void testSetSecurity_modifyErasePin() {
        String oldErasePin = "oldErasePin";
        byte[] oldErasePinB = toByteArray(oldErasePin);
        try {
            getAdminClient().setErasePin(toByteArray(oldPin), oldErasePinB);
        } catch (KineticException e) {
            Assert.fail("Change erase pin throw exception" + e.getMessage());
        }

        String newErasePin = "newErasePin";
        byte[] newErasePinB = toByteArray(newErasePin);
        try {
            getAdminClient().setErasePin(oldErasePinB, newErasePinB);
        } catch (KineticException e) {
            Assert.fail("Change erase pin throw exception" + e.getMessage());
        }

        // erase pin
        try {
            getAdminClient().setErasePin(newErasePinB, toByteArray(oldPin));
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, set erase pin with null, throw exception.
     * <p>
     */
    @Test(enabled = false)
    public void testSetSecurity_setErasePin_withNull() {
        byte[] erasePinB = null;

        try {
            getAdminClient().setErasePin(null, erasePinB);

        } catch (KineticException e) {
            Assert.fail("set new erase pin with null throw exception: "
                    + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, set erase pin with empty, should success.
     * <p>
     */
    @Test(enabled = false)
    public void testSetSecurity_setErasePin_withEmpty() {
        byte[] erasePinB = toByteArray("");

        try {
            getAdminClient().setErasePin(toByteArray(""), erasePinB);
        } catch (KineticException e) {
            Assert.fail("set erase pin is empty throw exception: "
                    + e.getMessage());
        }

        // erase pin
        try {
            getAdminClient().instantErase(erasePinB);
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, modify erase pin with null.
     * <p>
     */
    @Test(enabled = false)
    public void testSetSecurity_modifyErasePin_withNull() {
        String oldErasePin = "oldErasePin";
        byte[] oldErasePinB = toByteArray(oldErasePin);
        try {
            getAdminClient().setErasePin(toByteArray(""), oldErasePinB);
        } catch (KineticException e) {
            Assert.fail("Change erase pin throw exception" + e.getMessage());
        }

        byte[] newErasePinB = null;
        try {
            getAdminClient().setErasePin(oldErasePinB, newErasePinB);
        } catch (KineticException e) {
            Assert.fail("modify erase pin to null throw exception: "
                    + e.getMessage());
        }

        // erase pin
        try {
            getAdminClient().instantErase(oldErasePinB);
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, modify erase pin with empty.
     * <p>
     */
    @Test(enabled = false)
    public void testSetSecurity_modifyErasePin_withEmpty() {
        String oldErasePin = "oldErasePin";
        byte[] oldErasePinB = toByteArray(oldErasePin);
        try {
            getAdminClient().setErasePin(toByteArray(""), oldErasePinB);
        } catch (KineticException e) {
            Assert.fail("Change erase pin throw exception" + e.getMessage());
        }

        byte[] newErasePinB = toByteArray("");
        try {
            getAdminClient().setErasePin(oldErasePinB, newErasePinB);
        } catch (KineticException e) {
            Assert.fail("set erase pin with empty throw exception: "
                    + e.getMessage());
        }

        // erase pin
        try {
            getAdminClient().instantErase(newErasePinB);
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, set lock pin.
     * <p>
     */
    @Test
    public void testSetSecurity_setLockPin() {
        String lockPin = newPin;
        byte[] lockPinB = toByteArray(lockPin);

        try {
            getAdminClient().setLockPin(toByteArray(oldPin), lockPinB);
        } catch (KineticException e) {
            Assert.fail("Set erase pin throw exception" + e.getMessage());
        }

        // erase pin
        try {
            getAdminClient().setLockPin(lockPinB, toByteArray(oldPin));
        } catch (KineticException e) {
            Assert.fail("erase pin throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, modify lock pin.
     * <p>
     */
    @Test
    public void testSetSecurity_modifyLockPin() {
        String oldLockPin = "123";
        byte[] oldLockPinB = toByteArray(oldLockPin);
        try {
            getAdminClient().setLockPin(toByteArray(oldPin), oldLockPinB);
        } catch (KineticException e) {
            Assert.fail("Change lock pin throw exception" + e.getMessage());
        }

        String newLockPin = "456";
        byte[] newLockPinB = toByteArray(newLockPin);
        try {
            getAdminClient().setLockPin(oldLockPinB, newLockPinB);
        } catch (KineticException e) {
            Assert.fail("Change lock pin throw exception" + e.getMessage());
        }

        // erase
        try {
            getAdminClient().setLockPin(newLockPinB, toByteArray(oldPin));
        } catch (KineticException e) {
            Assert.fail("reset lock pin throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, set lock pin with null, throw exception.
     * <p>
     */
    @Test(enabled = false)
    public void testSetSecurity_setLockPin_withNull() {
        byte[] lockPinB = null;

        try {
            getAdminClient().setLockPin(null, lockPinB);
        } catch (KineticException e) {
            Assert.fail("set lock pin to null throw exception: "
                    + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, set lock pin with empty, should success.
     * <p>
     */
    @Test(enabled = false)
    public void testSetSecurity_setLockPin_withEmpty() {
        byte[] lockPinB = toByteArray("");

        try {
            getAdminClient().setLockPin(toByteArray(""), lockPinB);
        } catch (KineticException e) {
            Assert.fail("set lock pin is empty throw exception: "
                    + e.getMessage());
        }

        // erase pin
        try {
            instantErase("", "123", getAdminClient());
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, modify lock pin with null.
     * <p>
     */
    @Test(enabled = false)
    public void testSetSecurity_modifyLockPin_withNull() {
        String oldLockPin = "oldLockPin";
        byte[] oldLockPinB = toByteArray(oldLockPin);
        try {
            getAdminClient().setLockPin(null, oldLockPinB);
        } catch (KineticException e) {
            Assert.fail("set lock pin throw exception" + e.getMessage());
        }

        byte[] newLockPinB = null;
        try {
            getAdminClient().setLockPin(oldLockPinB, newLockPinB);
        } catch (KineticException e) {
            Assert.fail("modify lock pin to null throw exception: "
                    + e.getMessage());
        }

        // erase pin
        try {
            instantErase("", "123", getAdminClient());
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test set security, modify lock pin with empty.
     * <p>
     */
    @Test(enabled = false)
    public void testSetSecurity_modifyLockPin_withEmpty() {
        String oldLockPin = "oldLockPin";
        byte[] oldLockPinB = toByteArray(oldLockPin);
        try {
            getAdminClient().setLockPin(toByteArray(""), oldLockPinB);
        } catch (KineticException e) {
            Assert.fail("Change erase pin throw exception" + e.getMessage());
        }

        byte[] newLockPinB = toByteArray("");
        try {
            getAdminClient().setLockPin(oldLockPinB, newLockPinB);
        } catch (KineticException e) {
            Assert.fail("set erase pin with empty throw exception: "
                    + e.getMessage());
        }

        // erase pin
        try {
            instantErase("", "123", getAdminClient());
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test lock device with correct lock pin, should lock the device.
     * <p>
     */
    @Test(enabled = false)
    public void testLockDevice_withCorrectLockpin() {
        // set a lock pin
        byte[] lockPinB = toByteArray("123");
        try {
            getAdminClient().setLockPin(toByteArray(""), lockPinB);
        } catch (KineticException e1) {
            Assert.fail("set lock pin throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().lockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("Lock device with correct pin failed: "
                    + e.getMessage());
        }

        try {
            getAdminClient().getLog();
            Assert.fail("Should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.DEVICE_LOCKED));
        }

        // clean up: unlock device
        try {
            getAdminClient().unLockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }

        // reset lock pin
        try {
            getAdminClient().setLockPin(lockPinB, toByteArray(""));
        } catch (KineticException e) {
            Assert.fail("reset lock pin throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test lock device with incorrect lock pin, should not lock the device.
     * <p>
     */
    @Test
    public void testLockDevice_withIncorrectLockpin() {
        // set a lock pin
        byte[] lockPinB = toByteArray("123");
        try {
            getAdminClient().setLockPin(toByteArray(oldPin), lockPinB);
        } catch (KineticException e1) {
            Assert.fail("set lock pin throw exception: " + e1.getMessage());
        }

        byte[] incorrectLockPinB = toByteArray("incorrectlockpin");
        try {
            getAdminClient().lockDevice(incorrectLockPinB);
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.NOT_AUTHORIZED));
        }

        try {
            getAdminClient().getLog();
        } catch (KineticException e) {
            Assert.fail("get log with unlocked device throw exception: "
                    + e.getMessage());
        }

        // reset lock pin
        try {
            getAdminClient().setLockPin(lockPinB, toByteArray(oldPin));
        } catch (KineticException e) {
            Assert.fail("reset lock pin throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test lock device with null lock pin, should throw exception.
     * <p>
     */
    @Test(enabled = false)
    public void testLockDevice_withNullLockpin() {
        try {
            getAdminClient().lockDevice(null);
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            logger.info("Lock device with null pin throw exception: "
                    + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test lock device with empty lock pin, should throw exception.
     * <p>
     */
    @Test(enabled = false)
    public void testLockDevice_withEmptyLockpin() {
        try {
            getAdminClient().lockDevice(toByteArray(""));
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            logger.info("Lock device with empty pin throw exception: "
                    + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test unlock device with correct unlock pin, should unlock the device.
     * <p>
     */
    @Test
    public void testunLockDevice_withCorrectLockpin() {
        // set a lock pin
        byte[] lockPinB = toByteArray("123");
        try {
            getAdminClient().setLockPin(toByteArray(oldPin), lockPinB);
        } catch (KineticException e1) {
            Assert.fail("set lock pin throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().lockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("Lock device with correct pin failed: "
                    + e.getMessage());
        }

        try {
            getAdminClient().unLockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("unLock device with correct pin failed: "
                    + e.getMessage());
        }

        try {
            getAdminClient().getLog();
        } catch (KineticException e) {
            Assert.fail("get log with unlock device throw exception: "
                    + e.getMessage());
        }

        // reset lock pin
        try {
            getAdminClient().setLockPin(lockPinB, toByteArray(oldPin));
        } catch (KineticException e) {
            Assert.fail("reset lock pin throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test unlock device with incorrect unlock pin, should not unlock the
     * device.
     * <p>
     */
    @Test(enabled = false)
    public void testunLockDevice_withIncorrectLockpin() {
        // set a lock pin
        byte[] lockPinB = toByteArray("123");
        try {
            getAdminClient().setLockPin(toByteArray(""), lockPinB);
        } catch (KineticException e1) {
            Assert.fail("set lock pin throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().lockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("Lock device with correct pin failed: "
                    + e.getMessage());
        }

        byte[] incorrectunLockPinB = toByteArray("incorrectunlockpin");
        try {
            getAdminClient().unLockDevice(incorrectunLockPinB);
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.NOT_AUTHORIZED));
        }

        try {
            getAdminClient().getLog();
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.DEVICE_LOCKED));
        }

        // clean up: unlock device
        try {
            getAdminClient().unLockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("unlock throw exception" + e.getMessage());
        }

        // erase
        try {
            getAdminClient().setLockPin(lockPinB, toByteArray(""));
        } catch (KineticException e) {
            Assert.fail("secure erase throw exception" + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test unlock device with null unlock pin, should throw exception.
     * <p>
     */
    @Test(enabled = false)
    public void testUnLockDevice_withNullLockpin() {
        try {
            getAdminClient().unLockDevice(null);
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            logger.info("unLock device with null pin throw exception: "
                    + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test unlock device with empty unlock pin, should throw exception.
     * <p>
     */
    @Test(enabled = false)
    public void testLockDevice_withEmptyUnLockpin() {
        try {
            getAdminClient().unLockDevice(toByteArray(""));
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            logger.info("unLock device with empty pin throw exception: "
                    + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test get log API. Check every log field value whether valid.
     * <p>
     */
    @Test
    public void getLogNewAPI() {
        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.CAPACITIES);
        listOfLogType.add(KineticLogType.CONFIGURATION);
        listOfLogType.add(KineticLogType.MESSAGES);
        listOfLogType.add(KineticLogType.STATISTICS);
        listOfLogType.add(KineticLogType.TEMPERATURES);
        listOfLogType.add(KineticLogType.UTILIZATIONS);
        listOfLogType.add(KineticLogType.LIMITS);

        KineticLog log = null;
        try {
            log = getAdminClient().getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        Capacity capacity;
        try {
            capacity = log.getCapacity();
            assertTrue(capacity.getPortionFull() >= 0);
            assertTrue(capacity.getNominalCapacityInBytes() >= 0);
        } catch (KineticException e) {
            Assert.fail("get capacity throw exception: " + e.getMessage());
        }

        Configuration configuration;
        try {
            configuration = log.getConfiguration();
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
                assertTrue(interfaces.getName().length() > 0);
            }
        } catch (KineticException e) {
            Assert.fail("get configuration throw exception: " + e.getMessage());
        }

        byte[] messages;
        try {
            messages = log.getMessages();
            assertTrue(messages.length > 0);
        } catch (KineticException e) {
            Assert.fail("get message throw exception: " + e.getMessage());
        }

        List<Statistics> statisticsOfList;
        try {
            statisticsOfList = log.getStatistics();
            for (Statistics statistics : statisticsOfList) {
                assertTrue(statistics.getBytes() >= 0);
                assertTrue(statistics.getCount() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get statistics throw exception: " + e.getMessage());
        }

        List<Temperature> tempOfList;
        try {
            tempOfList = log.getTemperature();
            for (Temperature temperature : tempOfList) {
                assertTrue(temperature.getName().equals("HDA")
                        || temperature.getName().equals("CPU"));
                assertTrue(temperature.getCurrent() >= 0);
                assertTrue(temperature.getMax() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get temperature throw exception: " + e.getMessage());
        }

        List<Utilization> utilOfList;
        try {
            utilOfList = log.getUtilization();
            for (Utilization util : utilOfList) {
                assertTrue(util.getUtility() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get utilization throw exception: " + e.getMessage());
        }

        KineticLogType[] logTypes;
        try {
            logTypes = log.getContainedLogTypes();
            assertEquals(listOfLogType.size(), logTypes.length);

            for (int i = 0; i < logTypes.length; i++) {
                assertTrue(listOfLogType.contains(logTypes[i]));
            }

        } catch (KineticException e) {
            Assert.fail("get containedLogTypes throw exception: "
                    + e.getMessage());
        }

        Limits limits;
        try {
            limits = log.getLimits();
            assertTrue(limits.getMaxKeySize() == 4096);
            assertTrue(limits.getMaxValueSize() == 1024 * 1024);
            assertTrue(limits.getMaxVersionSize() == 2048);
            assertTrue(limits.getMaxKeyRangeCount() == 200);
            // // TODO: To be validated
            // assertTrue(limits.getMaxTagSize() >= 0);
            // assertTrue(limits.getMaxOutstandingReadRequests() >= 0);
            // assertTrue(limits.getMaxOutstandingWriteRequests() >= 0);
            // assertTrue(limits.getMaxConnections() >= 0);
            // assertTrue(limits.getMaxMessageSize() >= 0);
        } catch (KineticException e) {
            Assert.fail("get limits throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test utilization of get log API. Check field value whether valid.
     * <p>
     */
    @Test
    public void testGetUtilization() {
        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.UTILIZATIONS);

        KineticLog log = null;
        try {
            log = getAdminClient().getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        List<Utilization> utilOfList;
        try {
            utilOfList = log.getUtilization();
            for (Utilization util : utilOfList) {
                assertTrue(util.getUtility() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get utilization throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test capacity of get log API. Check field value whether valid.
     * <p>
     */
    @Test
    public void testGetCapacity() {

        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.CAPACITIES);

        KineticLog log = null;
        try {
            log = getAdminClient().getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        Capacity capacity;
        try {
            capacity = log.getCapacity();
            assertTrue(capacity.getPortionFull() >= 0);
            assertTrue(capacity.getNominalCapacityInBytes() >= 0);
        } catch (KineticException e) {
            Assert.fail("get capacity throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test configuration of get log API. Check field value whether valid.
     * <p>
     */
    @Test
    public void testGetConfiguration() {

        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.CONFIGURATION);

        KineticLog log = null;
        try {
            log = getAdminClient().getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        Configuration configuration;
        try {
            configuration = log.getConfiguration();
            assertTrue(configuration.getCompilationDate().length() > 0);
            assertTrue(configuration.getModel().length() > 0);
            assertTrue(configuration.getPort() >= 0);
            assertTrue(configuration.getTlsPort() >= 0);
            assertTrue(configuration.getSerialNumber().length() > 0);
            assertTrue(configuration.getWorldWideName().length() > 0);
            assertTrue(configuration.getSourceHash().length() > 0);
            assertTrue(configuration.getVendor().length() > 0);
            assertTrue(configuration.getVersion().length() > 0);

            List<Interface> interfaceOfList = configuration.getInterfaces();
            for (Interface interfaces : interfaceOfList) {
                assertTrue(interfaces.getName().length() > 0);
            }
        } catch (KineticException e) {
            Assert.fail("get configuration throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test temperature of get log API. Check field value whether valid.
     * <p>
     */
    @Test
    public void testGetTemperature() {

        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.TEMPERATURES);

        KineticLog log = null;
        try {
            log = getAdminClient().getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        List<Temperature> tempOfList;
        try {
            tempOfList = log.getTemperature();
            for (Temperature temperature : tempOfList) {
                assertTrue(temperature.getCurrent() >= 0);
                assertTrue(temperature.getMax() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get temperature throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test temperature and capacity and utilization of get log API. Check field
     * value whether valid.
     * <p>
     */
    @Test
    public void testGetTemperatureAndCapacityAndUtilization() {

        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.TEMPERATURES);
        listOfLogType.add(KineticLogType.CAPACITIES);
        listOfLogType.add(KineticLogType.UTILIZATIONS);

        KineticLog log = null;
        try {
            log = getAdminClient().getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        List<Temperature> tempOfList;
        try {
            tempOfList = log.getTemperature();
            for (Temperature temperature : tempOfList) {
                assertTrue(temperature.getCurrent() >= 0);
                assertTrue(temperature.getMax() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get temperature throw exception: " + e.getMessage());
        }

        Capacity capacity;
        try {
            capacity = log.getCapacity();
            assertTrue(capacity.getPortionFull() >= 0);
            assertTrue(capacity.getNominalCapacityInBytes() >= 0);
        } catch (KineticException e) {
            Assert.fail("get capacity throw exception: " + e.getMessage());
        }

        List<Utilization> utilOfList;
        try {
            utilOfList = log.getUtilization();
            for (Utilization util : utilOfList) {
                assertTrue(util.getUtility() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get utilization throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test get limits of get log API. Check field value whether valid.
     * <p>
     */
    @Test
    public void testGetLimits() {

        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.LIMITS);

        KineticLog log = null;
        try {
            log = getAdminClient().getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        Limits limits;
        try {
            limits = log.getLimits();
            assertTrue(limits.getMaxKeySize() == 4096);
            assertTrue(limits.getMaxValueSize() == 1024 * 1024);
            assertTrue(limits.getMaxVersionSize() == 2048);
            assertTrue(limits.getMaxKeyRangeCount() == 200);
            // // TODO: To be validated
            // assertTrue(limits.getMaxTagSize() >= 0);
            // assertTrue(limits.getMaxOutstandingReadRequests() >= 0);
            // assertTrue(limits.getMaxOutstandingWriteRequests() >= 0);
            // assertTrue(limits.getMaxConnections() >= 0);
            // assertTrue(limits.getMaxMessageSize() >= 0);
        } catch (KineticException e) {
            Assert.fail("get limits throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test temperature and capacity of get log API. Check field value whether
     * valid.
     * <p>
     */
    @Test
    public void testGetTemperatureAndCapacity() throws KineticException {

        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.TEMPERATURES);
        listOfLogType.add(KineticLogType.CAPACITIES);

        KineticLog log = null;
        try {
            log = getAdminClient().getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        List<Temperature> tempOfList;
        try {
            tempOfList = log.getTemperature();
            for (Temperature temperature : tempOfList) {
                assertTrue(temperature.getCurrent() >= 0);
                assertTrue(temperature.getMax() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get temperature throw exception: " + e.getMessage());
        }

        Capacity capacity;
        try {
            capacity = log.getCapacity();
            assertTrue(capacity.getPortionFull() >= 0);
            assertTrue(capacity.getNominalCapacityInBytes() >= 0);
        } catch (KineticException e) {
            Assert.fail("get capacity throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test temperature and utilization of get log API. Check field value
     * whether valid.
     * <p>
     */
    @Test
    public void testGetTemperatureAndUtilization() throws KineticException {

        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.TEMPERATURES);
        listOfLogType.add(KineticLogType.UTILIZATIONS);

        KineticLog log = null;
        try {
            log = getAdminClient().getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        List<Temperature> tempOfList;
        try {
            tempOfList = log.getTemperature();
            for (Temperature temperature : tempOfList) {
                assertTrue(temperature.getCurrent() >= 0);
                assertTrue(temperature.getMax() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get temperature throw exception: " + e.getMessage());
        }

        List<Utilization> utilOfList;
        try {
            utilOfList = log.getUtilization();
            for (Utilization util : utilOfList) {
                assertTrue(util.getUtility() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get utilization throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test capacity and utilization of get log API. Check field value whether
     * valid.
     * <p>
     */
    @Test
    public void testGetCapacityAndUtilization() throws KineticException {

        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.CAPACITIES);
        listOfLogType.add(KineticLogType.UTILIZATIONS);

        KineticLog log = null;
        try {
            log = getAdminClient().getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        Capacity capacity;
        try {
            capacity = log.getCapacity();
            assertTrue(capacity.getPortionFull() >= 0);
            assertTrue(capacity.getNominalCapacityInBytes() >= 0);
        } catch (KineticException e) {
            Assert.fail("get capacity throw exception: " + e.getMessage());
        }

        List<Utilization> utilOfList;
        try {
            utilOfList = log.getUtilization();
            for (Utilization util : utilOfList) {
                assertTrue(util.getUtility() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get utilization throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test back ground operation API: mediaScan. Check the response message
     * field.
     * <p>
     */
    @Test(enabled = false)
    public void testMediaScan_withPriorityIsHighest_successOperation() {
        // create request message
        KineticMessage kmreq = MessageFactory.createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) kmreq.getCommand();

        Range.Builder rangeBuilder = commandBuilder.getBodyBuilder()
                .getRangeBuilder();

        ByteString startKey = ByteString.copyFromUtf8("start_key");
        ByteString endKey = ByteString.copyFromUtf8("end_key");

        rangeBuilder.setStartKey(startKey);
        rangeBuilder.setEndKey(endKey);
        rangeBuilder.setStartKeyInclusive(false);
        rangeBuilder.setEndKeyInclusive(true);

        Range range = rangeBuilder.build();

        try {
            KineticMessage kmrsp = getAdminClient().mediaScan(range,
                    Priority.HIGHEST);
            assertEquals(MessageType.MEDIASCAN_RESPONSE, kmrsp.getCommand()
                    .getHeader().getMessageType());
            assertEquals(StatusCode.SUCCESS, kmrsp.getCommand().getStatus()
                    .getCode());
        } catch (KineticException e) {
            Assert.fail("media scan operation throw exception: "
                    + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test back ground operation API: mediaScan. The priority is highest, no
     * range permission, should throw exception.
     * <p>
     */
    @Test(enabled = false)
    public void testMediaScan_withPriorityIsHighest_withNoRangePermission_throwException() {
        List<Role> roles = new ArrayList<Role>();
        roles.add(Role.DELETE);
        roles.add(Role.GETLOG);
        roles.add(Role.READ);
        roles.add(Role.SECURITY);
        roles.add(Role.SETUP);
        roles.add(Role.WRITE);
        roles.add(Role.P2POP);

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

        try {
            getAdminClient().setAcl(acls);
        } catch (KineticException e1) {
            Assert.fail("set acls without range permission throw exception:"
                    + e1.getMessage());
        }

        // create request message
        KineticMessage kmreq = MessageFactory.createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) kmreq.getCommand();

        Range.Builder rangeBuilder = commandBuilder.getBodyBuilder()
                .getRangeBuilder();

        ByteString startKey = ByteString.copyFromUtf8("start_key");
        ByteString endKey = ByteString.copyFromUtf8("end_key");

        rangeBuilder.setStartKey(startKey);
        rangeBuilder.setEndKey(endKey);
        rangeBuilder.setStartKeyInclusive(false);
        rangeBuilder.setEndKeyInclusive(true);

        Range range = rangeBuilder.build();

        try {
            getAdminClient().mediaScan(range, Priority.HIGHEST);
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.NOT_AUTHORIZED));
        }

        // clean up acls
        try {
            instantErase("", "123", getAdminClient());
        } catch (KineticException e) {
            Assert.fail("clean up acls throw excaption: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test back ground operation API: mediaOptimize. Check the response message
     * field.
     * <p>
     */
    @Test(enabled = false)
    public void testMediaOptimize_withPriorityIsHighest_successOperation() {
        // create request message
        KineticMessage kmreq = MessageFactory.createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) kmreq.getCommand();

        Range.Builder rangeBuilder = commandBuilder.getBodyBuilder()
                .getRangeBuilder();

        ByteString startKey = ByteString.copyFromUtf8("start_key");
        ByteString endKey = ByteString.copyFromUtf8("end_key");

        rangeBuilder.setStartKey(startKey);
        rangeBuilder.setEndKey(endKey);
        rangeBuilder.setStartKeyInclusive(false);
        rangeBuilder.setEndKeyInclusive(true);

        Range range = rangeBuilder.build();

        try {
            KineticMessage kmrsp = getAdminClient().mediaOptimize(range,
                    Priority.HIGHEST);
            assertEquals(MessageType.MEDIAOPTIMIZE_RESPONSE, kmrsp.getCommand()
                    .getHeader().getMessageType());
            assertEquals(StatusCode.SUCCESS, kmrsp.getCommand().getStatus()
                    .getCode());
        } catch (KineticException e) {
            Assert.fail("media optimize operation throw exception: "
                    + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test back ground operation API: mediaOptimize. The priority is highest,
     * no range permission, should throw exception.
     * <p>
     */
    @Test(enabled = false)
    public void testMediaOptimize_withPriorityIsHighest_withNoRangePermission_throwException() {
        List<Role> roles = new ArrayList<Role>();
        roles.add(Role.DELETE);
        roles.add(Role.GETLOG);
        roles.add(Role.READ);
        roles.add(Role.SECURITY);
        roles.add(Role.SETUP);
        roles.add(Role.WRITE);
        roles.add(Role.P2POP);

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

        try {
            getAdminClient().setAcl(acls);
        } catch (KineticException e1) {
            Assert.fail("set acls without range permission throw exception:"
                    + e1.getMessage());
        }

        // create request message
        KineticMessage kmreq = MessageFactory.createKineticMessageWithBuilder();

        Command.Builder commandBuilder = (Command.Builder) kmreq.getCommand();

        Range.Builder rangeBuilder = commandBuilder.getBodyBuilder()
                .getRangeBuilder();

        ByteString startKey = ByteString.copyFromUtf8("start_key");
        ByteString endKey = ByteString.copyFromUtf8("end_key");

        rangeBuilder.setStartKey(startKey);
        rangeBuilder.setEndKey(endKey);
        rangeBuilder.setStartKeyInclusive(false);
        rangeBuilder.setEndKeyInclusive(true);

        Range range = rangeBuilder.build();

        try {
            getAdminClient().mediaOptimize(range, Priority.HIGHEST);
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.NOT_AUTHORIZED));
        }

        // clean up acls
        try {
            instantErase("", "123", getAdminClient());
        } catch (KineticException e) {
            Assert.fail("clean up acls throw excaption: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Reset cluster version method.
     * <p>
     * 
     * @throws KineticException
     * 
     *             if any internal error occurred.
     * 
     */
    private void resetClusterVersionToDefault(long currentClusterVersion)
            throws KineticException {
        final KineticAdminClient client = KineticAdminClientFactory
                .createInstance(getAdminClientConfig(currentClusterVersion));
        client.setClusterVersion(DEFAULT_CLUSTER_VERSION);
        client.close();
    }
}
