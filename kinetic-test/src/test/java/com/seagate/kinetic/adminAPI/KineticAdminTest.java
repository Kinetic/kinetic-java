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
package com.seagate.kinetic.adminAPI;

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.junit.Assert.assertArrayEquals;
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
import kinetic.admin.Limits;
import kinetic.admin.Role;
import kinetic.admin.Statistics;
import kinetic.admin.Temperature;
import kinetic.admin.Utilization;
import kinetic.client.ClusterVersionFailureException;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;
import com.seagate.kinetic.admin.impl.DefaultAdminClient;
import com.seagate.kinetic.client.internal.ClientProxy.LCException;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.GetLog;
import com.seagate.kinetic.proto.Kinetic.Message.GetLog.Type;
import com.seagate.kinetic.proto.Kinetic.Message.Security;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.HMACAlgorithm;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Scope;
import com.seagate.kinetic.proto.Kinetic.Message.Setup;
import com.seagate.kinetic.proto.Kinetic.Message.Status;
import com.seagate.kinetic.proto.Kinetic.MessageOrBuilder;

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

    private final byte[] INIT_KEY = "0".getBytes();
    private final byte[] INIT_VALUE = "0".getBytes();
    private final byte[] INIT_VERSION = "0".getBytes();

    @Before
    public void setUp() throws Exception {
        EntryMetadata entryMetadata = new EntryMetadata();
        KineticClient kineticClient = KineticClientFactory
                .createInstance(getClientConfig());
        kineticClient.put(new Entry(INIT_KEY, INIT_VALUE, entryMetadata),
                INIT_VERSION);
        kineticClient.close();
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
        } catch (ClusterVersionFailureException cve) {
            long requestClusterVersion = cve.getRequestMessage().getMessage()
                    .getCommand().getHeader().getClusterVersion();
            
            long responseClusterVersion = cve.getResponseMessage().getMessage()
                    .getCommand().getHeader().getClusterVersion();

            logger.info("caught expected exception, this is ok. request cluster version="
                    + requestClusterVersion + ", respose cluster version=" + responseClusterVersion);
        } catch (Exception e) {
           fail ("should have caught ClusterVersionException");
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

        client.instantErase(null);
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
                .createInstance(getAdminClientConfig(1));

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

        assertTrue(log.getLimits().getMaxKeySize() == 4096);
        assertTrue(log.getLimits().getMaxValueSize() == 1024 * 1024);
        assertTrue(log.getLimits().getMaxVersionSize() == 2048);
        assertTrue(log.getLimits().getMaxKeyRangeCount() == 1024);
        // // TODO: To be validated
        // assertTrue(log.getLimits().getMaxTagSize() >= 0);
        // assertTrue(log.getLimits().getMaxOutstandingReadRequests() >= 0);
        // assertTrue(log.getLimits().getMaxOutstandingWriteRequests() >= 0);
        // assertTrue(log.getLimits().getMaxConnections() >= 0);
        // assertTrue(log.getLimits().getMaxMessageSize() >= 0);
        // assertTrue(log.getLimits().getMaxKeyRangeCount() >= 0);

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

    @Test
    public void testMultiUsersACLs_VerifyRoles() throws LCException,
            KineticException {
        Message.Builder request = Message.newBuilder();
        Security.Builder security = request.getCommandBuilder()
                .getBodyBuilder().getSecurityBuilder();

        // client 1 has all roles
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl1 = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        acl1.setIdentity(1);
        acl1.setKey(ByteString.copyFromUtf8("asdfasdf"));
        acl1.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain = Scope.newBuilder();
        for (Permission role : Permission.values()) {
            if (!role.equals(Permission.INVALID_PERMISSION)) {
                domain.addPermission(role);
            }
        }
        acl1.addScope(domain);
        security.addAcl(acl1);

        // client 2 only has read permission
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl2 = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        acl2.setIdentity(2);
        acl2.setKey(ByteString.copyFromUtf8("asdfasdf2"));
        acl2.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain2 = Scope.newBuilder();
        domain2.addPermission(Permission.READ);
        acl2.addScope(domain2);
        security.addAcl(acl2);

        // client 3 only has write permission
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl3 = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        acl3.setIdentity(3);
        acl3.setKey(ByteString.copyFromUtf8("asdfasdf3"));
        acl3.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain3 = Scope.newBuilder();
        domain3.addPermission(Permission.WRITE);
        acl3.addScope(domain3);
        security.addAcl(acl3);

        // client 4 only has delete permission
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl4 = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        acl4.setIdentity(4);
        acl4.setKey(ByteString.copyFromUtf8("asdfasdf4"));
        acl4.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain4 = Scope.newBuilder();
        domain4.addPermission(Permission.DELETE);
        acl4.addScope(domain4);
        security.addAcl(acl4);

        // client 5 only has read and write permission
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl5 = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        acl5.setIdentity(5);
        acl5.setKey(ByteString.copyFromUtf8("asdfasdf5"));
        acl5.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain5 = Scope.newBuilder();
        domain5.addPermission(Permission.READ);
        domain5.addPermission(Permission.WRITE);
        acl5.addScope(domain5);
        security.addAcl(acl5);

        // client 6 only has read and delete permission
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl6 = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        acl6.setIdentity(6);
        acl6.setKey(ByteString.copyFromUtf8("asdfasdf6"));
        acl6.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain6 = Scope.newBuilder();
        domain6.addPermission(Permission.READ);
        domain6.addPermission(Permission.DELETE);
        acl6.addScope(domain6);
        security.addAcl(acl6);

        // client 7 only has write and delete permission
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl7 = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        acl7.setIdentity(7);
        acl7.setKey(ByteString.copyFromUtf8("asdfasdf7"));
        acl7.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain7 = Scope.newBuilder();
        domain7.addPermission(Permission.WRITE);
        domain7.addPermission(Permission.DELETE);
        acl7.addScope(domain7);
        security.addAcl(acl7);

        Message response = getAdminClient().configureSecurityPolicy(request);
        assertTrue(response.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));

        // client1 can do read write and delete
        KineticClient kineticClient1 = KineticClientFactory
                .createInstance(getClientConfig(1, "asdfasdf"));
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient1.put(new Entry("123".getBytes(), "456".getBytes(),
                    entryMetadata), "789".getBytes());
        } catch (Exception e) {
            fail("put operation throw exception" + e.getMessage());
        }

        Entry vGet = null;
        try {
            vGet = kineticClient1.get("123".getBytes());
        } catch (Exception e) {
            fail("get operation throw exception" + e.getMessage());
        }

        try {
            kineticClient1.delete(vGet);
        } catch (Exception e) {
            fail("delete operation throw exception" + e.getMessage());
        }
        kineticClient1.close();

        // client2 can do read, can not do write and delete
        KineticClient kineticClient2 = KineticClientFactory
                .createInstance(getClientConfig(2, "asdfasdf2"));
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient2.put(new Entry("123".getBytes(), "456".getBytes(),
                    entryMetadata), "789".getBytes());
            fail("client2 does not have write rights, but can write, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            vGet = kineticClient2.get(INIT_KEY);
        } catch (Exception e) {
            fail("get operation throw exception" + e.getMessage());
        }

        try {
            kineticClient2.delete(vGet);
            fail("client2 does not have delete rights, but can delete, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }
        kineticClient2.close();

        // client3 can do write, can not do read and delete
        KineticClient kineticClient3 = KineticClientFactory
                .createInstance(getClientConfig(3, "asdfasdf3"));
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient3.put(v, INIT_VERSION);
        } catch (Exception e) {
            fail("client3 put operation throw exception" + e.getMessage());
        }

        try {
            vGet = kineticClient3.get(INIT_KEY);
            fail("client3 does not have read rights, but can get, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            kineticClient3.delete(vGet);
            fail("client3 does not have delete rights, but can delete, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }
        kineticClient3.close();

        // client4 can do delete, can not do read and write
        KineticClient kineticClient4 = KineticClientFactory
                .createInstance(getClientConfig(4, "asdfasdf4"));
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient4.put(v, INIT_VERSION);
            fail("client4 does not have write rights, but can put, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            vGet = kineticClient4.get(INIT_KEY);
            fail("client4 does not have read rights, but can get, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient4.delete(v);
        } catch (Exception e) {
            fail("client4 delete operation throw exception" + e.getMessage());
        }
        kineticClient4.close();

        // client5 can do read and write, can not do delete
        KineticClient kineticClient5 = KineticClientFactory
                .createInstance(getClientConfig(5, "asdfasdf5"));
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient5.put(v, INIT_VERSION);
        } catch (Exception e) {
            fail("client5 put operation throw exception" + e.getMessage());
        }

        try {
            vGet = kineticClient5.get(INIT_KEY);
        } catch (Exception e) {
            fail("client5 get operation throw exception" + e.getMessage());
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient5.delete(v);
            fail("client5 does not have delete rights, but can delete, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }
        kineticClient5.close();

        // client6 can do read and delete, can not do write
        KineticClient kineticClient6 = KineticClientFactory
                .createInstance(getClientConfig(6, "asdfasdf6"));
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient6.put(v, INIT_VERSION);
            fail("client6 does not have write rights, but can put, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            vGet = kineticClient6.get(INIT_KEY);
        } catch (Exception e) {
            fail("client5 get operation throw exception" + e.getMessage());
        }

        try {
            kineticClient6.delete(vGet);
        } catch (Exception e) {
            fail("client6 delete operation throw exception" + e.getMessage());
        }
        kineticClient6.close();

        // client7 can do write and delete, can not do read
        KineticClient kineticClient7 = KineticClientFactory
                .createInstance(getClientConfig(7, "asdfasdf7"));
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient7.put(v, INIT_VERSION);
        } catch (Exception e) {
            fail("client7 put operation throw exception" + e.getMessage());
        }

        try {
            vGet = kineticClient7.get(INIT_KEY);
            fail("client7 does not have read rights, but can get, so failed");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(INIT_VERSION);
            Entry v = new Entry(INIT_KEY, INIT_VALUE, entryMetadata);
            kineticClient7.delete(v);
        } catch (Exception e) {
            fail("client7 delete operation throw exception" + e.getMessage());
        }
        kineticClient7.close();
    }

    @Test
    public void testSingleUserACL_WithMultiDomain_VerifyRolesInDifferentDomain()
            throws LCException, KineticException {
        Message.Builder request = null;
        byte[] value = "456".getBytes();
        byte[] version = "0".getBytes();

        Security.Builder security = null;
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl = null;
        Message response = null;

        request = Message.newBuilder();
        security = request.getCommandBuilder().getBodyBuilder()
                .getSecurityBuilder();

        // add admin acl info
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder aclAdmin = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        aclAdmin.setIdentity(1);
        aclAdmin.setKey(ByteString.copyFromUtf8("asdfasdf"));
        aclAdmin.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domainAdmin = Scope.newBuilder();
        for (Permission role : Permission.values()) {
            if (!role.equals(Permission.INVALID_PERMISSION)) {
                domainAdmin.addPermission(role);
            }
        }
        aclAdmin.addScope(domainAdmin);
        security.addAcl(aclAdmin);

        acl = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        acl.setIdentity(2);
        acl.setKey(ByteString.copyFromUtf8("asdfasdf2"));
        acl.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);

        Scope.Builder domain0 = Scope.newBuilder();
        domain0.addPermission(Permission.READ);
        domain0.setOffset(0);
        domain0.setValue(ByteString.copyFromUtf8("domain0"));
        acl.addScope(domain0);

        Scope.Builder domain1 = Scope.newBuilder();
        domain1.addPermission(Permission.READ);
        domain1.addPermission(Permission.WRITE);
        domain1.setOffset(1);
        domain1.setValue(ByteString.copyFromUtf8("domain1"));
        acl.addScope(domain1);

        Scope.Builder domain2 = Scope.newBuilder();
        domain2.addPermission(Permission.READ);
        domain2.addPermission(Permission.DELETE);
        domain2.setOffset(2);
        domain2.setValue(ByteString.copyFromUtf8("domain2"));
        acl.addScope(domain2);

        Scope.Builder domain3 = Scope.newBuilder();
        domain3.addPermission(Permission.WRITE);
        domain3.setOffset(3);
        domain3.setValue(ByteString.copyFromUtf8("domain3"));
        acl.addScope(domain3);

        Scope.Builder domain4 = Scope.newBuilder();
        domain4.addPermission(Permission.WRITE);
        domain4.addPermission(Permission.DELETE);
        domain4.setOffset(4);
        domain4.setValue(ByteString.copyFromUtf8("domain4"));
        acl.addScope(domain4);

        Scope.Builder domain5 = Scope.newBuilder();
        domain5.addPermission(Permission.DELETE);
        domain5.setOffset(5);
        domain5.setValue(ByteString.copyFromUtf8("domain5"));
        acl.addScope(domain5);

        Scope.Builder domain6 = Scope.newBuilder();
        domain6.addPermission(Permission.READ);
        domain6.addPermission(Permission.WRITE);
        domain6.addPermission(Permission.DELETE);
        domain6.setOffset(6);
        domain6.setValue(ByteString.copyFromUtf8("domain6"));
        acl.addScope(domain6);

        security.addAcl(acl);
        response = getAdminClient().configureSecurityPolicy(request);
        assertTrue(response.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));

        KineticClient kineticClient = KineticClientFactory
                .createInstance(getClientConfig(2, "asdfasdf2"));

        // operation in scope 0
        try {
            byte[] key1 = toByteArray("domain0key000");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key1, value, entryMetadata), version);
            fail("The user does not have write rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            byte[] key1 = toByteArray("domain0key000");
            assertTrue(null == kineticClient.get(key1));

        } catch (Exception e) {
            fail("get exception" + e.getMessage());
        }

        try {
            byte[] key1 = toByteArray("domain0key000");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.delete(new Entry(key1, value, entryMetadata));
            fail("The user do not have delete rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        // operation in scope 1
        try {
            byte[] key1 = toByteArray("adomain1key001");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key1, value, entryMetadata), version);
        } catch (Exception e) {
            fail("put in domain1 exception" + e.getMessage());
        }
        Entry vGet = null;
        try {
            byte[] key1 = toByteArray("adomain1key001");
            vGet = kineticClient.get(key1);
            assertArrayEquals(key1, vGet.getKey());
            assertArrayEquals(value, vGet.getValue());
        } catch (Exception e) {
            fail("get exception" + e.getMessage());
        }

        try {
            kineticClient.delete(vGet);
            fail("The user do not have delete rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        // operation in scope 2
        try {
            byte[] key2 = toByteArray("abdomain2key002");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key2, value, entryMetadata), version);
            fail("The user does not have put rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }
        try {
            byte[] key2 = toByteArray("abdomain2key002");
            vGet = kineticClient.get(key2);
            assertEquals(null, vGet);
        } catch (Exception e) {
            fail("get exception" + e.getMessage());
        }

        try {
            byte[] key2 = toByteArray("abdomain2key002");
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(version);
            Entry vDel = new Entry(key2, value, entryMetadata);
            kineticClient.delete(vDel);
        } catch (Exception e) {
            fail("get exception" + e.getMessage());
        }

        // operation in scope 3
        try {
            byte[] key3 = toByteArray("abcdomain3key003");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key3, value, entryMetadata), version);
        } catch (Exception e) {
            fail("put in domain3 exception" + e.getMessage());
        }
        try {
            byte[] key3 = toByteArray("abcdomain3key003");
            vGet = kineticClient.get(key3);
            fail("The user do not have get rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            byte[] key3 = toByteArray("abcdomain3key003");
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(version);
            Entry v = new Entry(key3, value, entryMetadata);
            kineticClient.delete(v);
            fail("The user do not have delete rights");
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
            fail("put in domain4 exception" + e.getMessage());
        }
        try {
            key4 = toByteArray("abcddomain4key004");
            vGet = kineticClient.get(key4);
            fail("The user do not have get rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(version);
            Entry v = new Entry(key4, value, entryMetadata);
            kineticClient.delete(v);
        } catch (Exception e) {
            fail("delete in domain4 exception" + e.getMessage());
        }

        // operation in scope 5
        byte[] key5 = null;
        try {
            key5 = toByteArray("abcdedomain5key005");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key5, value, entryMetadata), version);
            fail("The user do not have put rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }
        try {
            key5 = toByteArray("abcdedomain5key005");
            vGet = kineticClient.get(key5);
            fail("The user do not have get rights");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(version);
            Entry v = new Entry(key5, value, entryMetadata);
            kineticClient.delete(v);
        } catch (Exception e) {
            fail("delete in domain5 exception" + e.getMessage());
        }

        // operation in scope 6
        byte[] key6 = null;
        try {
            key6 = toByteArray("abcdefdomain6key006");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key6, value, entryMetadata), version);
        } catch (Exception e) {
            fail("put in domain6 exception" + e.getMessage());
        }
        try {
            vGet = kineticClient.get(key6);
            assertArrayEquals(key6, vGet.getKey());
            assertArrayEquals(value, vGet.getValue());
        } catch (Exception e) {
            fail("get in domain6 exception" + e.getMessage());
        }

        try {
            kineticClient.delete(vGet);
        } catch (Exception e) {
            fail("delete in domain6 exception" + e.getMessage());
        }

        // wrong scope operation
        byte[] key7 = null;
        try {
            key7 = toByteArray("domain7key007");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key7, value, entryMetadata), version);
            fail("The user do not have right domain");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        // key size smaller than scope
        byte[] key8 = null;
        try {
            key8 = toByteArray("key0");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key8, value, entryMetadata), version);
            fail("The key size is smaller than domain");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        kineticClient.close();

    }

    @Test
    public void testNegativeDomainOffset() throws KineticException {
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        acl.setIdentity(2);
        acl.setKey(ByteString.copyFromUtf8("asdfasdf2"));
        acl.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);

        Scope.Builder domain = Scope.newBuilder();
        domain.addPermission(Permission.READ);
        domain.setOffset(-1);
        domain.setValue(ByteString.copyFromUtf8("domain"));
        acl.addScope(domain);

        Message.Builder request = Message.newBuilder();
        request.getCommandBuilder().getBodyBuilder().getSecurityBuilder()
                .addAcl(acl);
        
        try {
            @SuppressWarnings("unused")
            Message response = getAdminClient().configureSecurityPolicy(request);
        } catch (KineticException ke) {
            assertEquals(Status.StatusCode.INTERNAL_ERROR, ke.getResponseMessage().getMessage().getCommand()
                .getStatus().getCode());
        }
    }

    @Test
    public void testAlgorithm_DefaultAlgorithmHmacSha1()
            throws KineticException {
        Message.Builder request = Message.newBuilder();
        Security.Builder security = request.getCommandBuilder()
                .getBodyBuilder().getSecurityBuilder();

        // client 1 has all roles
        com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Builder acl1 = com.seagate.kinetic.proto.Kinetic.Message.Security.ACL
                .newBuilder();
        acl1.setIdentity(1);
        acl1.setKey(ByteString.copyFromUtf8("asdfasdf"));
        acl1.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain = Scope.newBuilder();
        for (Permission role : Permission.values()) {
            if (!role.equals(Permission.INVALID_PERMISSION)) {
                domain.addPermission(role);
            }
        }
        acl1.addScope(domain);
        security.addAcl(acl1);

        Message response = getAdminClient().configureSecurityPolicy(request);
        assertTrue(response.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));

        // client1 can do read write and delete
        KineticClient kineticClient1 = KineticClientFactory
                .createInstance(getClientConfig(1, "asdfasdf"));
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient1.put(new Entry("123".getBytes(), "456".getBytes(),
                    entryMetadata), "789".getBytes());
        } catch (Exception e) {
            fail("put operation throw exception" + e.getMessage());
        }

        Entry vGet = null;
        try {
            vGet = kineticClient1.get("123".getBytes());
        } catch (Exception e) {
            fail("get operation throw exception" + e.getMessage());
        }

        try {
            kineticClient1.delete(vGet);
        } catch (Exception e) {
            fail("delete operation throw exception" + e.getMessage());
        }
        kineticClient1.close();
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
        listOfLogType.add(KineticLogType.LIMITS);

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
            assertTrue(interfaces.getName().length() > 0);
        }

        byte[] messages = log.getMessages();
        assertTrue(messages.length > 0);

        List<Statistics> statisticsOfList = log.getStatistics();
        for (Statistics statistics : statisticsOfList) {
            assertTrue(statistics.getBytes() >= 0);
            assertTrue(statistics.getCount() >= 0);
        }

        List<Temperature> tempOfList = log.getTemperature();
        for (Temperature temperature : tempOfList) {
            assertTrue(temperature.getName().equals("HDA")
                    || temperature.getName().equals("CPU"));
            assertTrue(temperature.getCurrent() >= 0);
            assertTrue(temperature.getMax() >= 0);
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

        Limits limits = log.getLimits();
        assertTrue(limits.getMaxKeySize() == 4096);
        assertTrue(limits.getMaxValueSize() == 1024 * 1024);
        assertTrue(limits.getMaxVersionSize() == 2048);
        assertTrue(limits.getMaxKeyRangeCount() == 1024);
        // // TODO: To be validated
        // assertTrue(limits.getMaxTagSize() >= 0);
        // assertTrue(limits.getMaxOutstandingReadRequests() >= 0);
        // assertTrue(limits.getMaxOutstandingWriteRequests() >= 0);
        // assertTrue(limits.getMaxConnections() >= 0);
        // assertTrue(limits.getMaxMessageSize() >= 0);

        logger.info(this.testEndInfo());
    }

    @Test
    public void testGetUtilization() throws KineticException {

        Message.Builder request = Message.newBuilder();
        GetLog.Builder getLog = request.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();
        getLog.addType(Type.UTILIZATIONS);

        KineticMessage km = new KineticMessage();
        km.setMessage(request);

        Message respond = (Message) getAdminClient().getLog(km).getMessage();

        assertTrue(respond.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));
        assertEquals("HDA", respond.getCommand().getBody().getGetLog()
                .getUtilizationList().get(0).getName());
        assertEquals("EN0", respond.getCommand().getBody().getGetLog()
                .getUtilizationList().get(1).getName());
    }

    @Test
    public void testGetCapacity() throws KineticException {

        Message.Builder request1 = Message.newBuilder();
        GetLog.Builder getLog1 = request1.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();
        getLog1.addType(Type.CAPACITIES);

        KineticMessage km1 = new KineticMessage();
        km1.setMessage(request1);

        Message respond1 = (Message) getAdminClient().getLog(km1).getMessage();

        assertTrue(respond1.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));
        assertTrue(0 <= respond1.getCommand().getBody().getGetLog()
                .getCapacity().getNominalCapacityInBytes());
    }

    @Test
    public void testGetTemperature() throws KineticException {

        Message.Builder request2 = Message.newBuilder();
        GetLog.Builder getLog2 = request2.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();
        getLog2.addType(Type.TEMPERATURES);

        KineticMessage km = new KineticMessage();
        km.setMessage(request2);

        Message respond2 = (Message) getAdminClient().getLog(km).getMessage();

        assertTrue(respond2.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));
        assertTrue(0 < respond2.getCommand().getBody().getGetLog()
                .getTemperatureList().get(0).getMaximum());
    }

    @Test
    public void testGetTemperatureAndCapacityAndUtilization()
            throws KineticException {

        Message.Builder request3 = Message.newBuilder();
        GetLog.Builder getLog3 = request3.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();
        getLog3.addType(Type.TEMPERATURES);
        getLog3.addType(Type.CAPACITIES);
        getLog3.addType(Type.UTILIZATIONS);

        KineticMessage km = new KineticMessage();
        km.setMessage(request3);

        Message respond3 = (Message) getAdminClient().getLog(km).getMessage();

        assertTrue(respond3.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));
        assertEquals("HDA", respond3.getCommand().getBody().getGetLog()
                .getUtilizationList().get(0).getName());
        assertEquals("EN0", respond3.getCommand().getBody().getGetLog()
                .getUtilizationList().get(1).getName());
        assertTrue(0 <= respond3.getCommand().getBody().getGetLog()
                .getCapacity().getNominalCapacityInBytes());
        assertTrue(0 < respond3.getCommand().getBody().getGetLog()
                .getTemperatureList().get(0).getMaximum());
    }

    @Test
    public void testGetLimits() throws KineticException {

        Message.Builder request1 = Message.newBuilder();
        GetLog.Builder getLog1 = request1.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();
        getLog1.addType(Type.LIMITS);

        KineticMessage km1 = new KineticMessage();
        km1.setMessage(request1);

        Message respond1 = (Message) getAdminClient().getLog(km1).getMessage();

        assertTrue(respond1.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));
        assertTrue(4096 == respond1.getCommand().getBody().getGetLog()
                .getLimits().getMaxKeySize());
        assertTrue(1024 * 1024 == respond1.getCommand().getBody().getGetLog()
                .getLimits().getMaxValueSize());
        assertTrue(2048 == respond1.getCommand().getBody().getGetLog()
                .getLimits().getMaxVersionSize());
        assertTrue(1024 == respond1.getCommand().getBody().getGetLog()
                .getLimits().getMaxKeyRangeCount());
        // // TODO: To be validated
        // assertTrue(log.getLimits().getMaxTagSize() >= 0);
        // assertTrue(log.getLimits().getMaxOutstandingReadRequests() >= 0);
        // assertTrue(log.getLimits().getMaxOutstandingWriteRequests() >= 0);
        // assertTrue(log.getLimits().getMaxConnections() >= 0);
        // assertTrue(log.getLimits().getMaxMessageSize() >= 0);
        // assertTrue(log.getLimits().getMaxKeyRangeCount() >= 0);
    }

    @Test
    public void testGetTemperatureAndCapacity() throws KineticException {

        Message.Builder request4 = Message.newBuilder();
        GetLog.Builder getLog4 = request4.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();
        getLog4.addType(Type.TEMPERATURES);
        getLog4.addType(Type.CAPACITIES);

        KineticMessage km = new KineticMessage();
        km.setMessage(request4);

        Message respond4 = (Message) getAdminClient().getLog(km).getMessage();

        assertTrue(respond4.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));
        assertTrue(0 <= respond4.getCommand().getBody().getGetLog()
                .getCapacity().getNominalCapacityInBytes());
        assertTrue(0 <= respond4.getCommand().getBody().getGetLog()
                .getTemperatureList().get(0).getMaximum());
        assertTrue(0 <= respond4.getCommand().getBody().getGetLog()
                .getTemperatureList().get(0).getMinimum());
        assertTrue(0 <= respond4.getCommand().getBody().getGetLog()
                .getTemperatureList().get(0).getTarget());
    }

    @Test
    public void testGetTemperatureAndUtilization() throws KineticException {

        Message.Builder request5 = Message.newBuilder();
        GetLog.Builder getLog5 = request5.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();
        getLog5.addType(Type.TEMPERATURES);
        getLog5.addType(Type.UTILIZATIONS);

        KineticMessage km = new KineticMessage();
        km.setMessage(request5);

        Message respond5 = (Message) getAdminClient().getLog(km).getMessage();

        assertTrue(respond5.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));
        assertEquals("HDA", respond5.getCommand().getBody().getGetLog()
                .getUtilizationList().get(0).getName());
        assertEquals("EN0", respond5.getCommand().getBody().getGetLog()
                .getUtilizationList().get(1).getName());
        assertTrue(0 <= respond5.getCommand().getBody().getGetLog()
                .getTemperatureList().get(0).getMaximum());
        assertTrue(0 <= respond5.getCommand().getBody().getGetLog()
                .getTemperatureList().get(0).getMinimum());
        assertTrue(0 <= respond5.getCommand().getBody().getGetLog()
                .getTemperatureList().get(0).getTarget());
    }

    @Test
    public void testGetCapacityAndUtilization() throws KineticException {

        Message.Builder request6 = Message.newBuilder();
        GetLog.Builder getLog6 = request6.getCommandBuilder().getBodyBuilder()
                .getGetLogBuilder();
        getLog6.addType(Type.CAPACITIES);
        getLog6.addType(Type.UTILIZATIONS);

        KineticMessage km = new KineticMessage();
        km.setMessage(request6);

        MessageOrBuilder respond6 = getAdminClient().getLog(km).getMessage();

        assertTrue(respond6.getCommand().getStatus().getCode()
                .equals(Status.StatusCode.SUCCESS));
        assertEquals("HDA", respond6.getCommand().getBody().getGetLog()
                .getUtilizationList().get(0).getName());
        assertEquals("EN0", respond6.getCommand().getBody().getGetLog()
                .getUtilizationList().get(1).getName());
        assertTrue(0 <= respond6.getCommand().getBody().getGetLog()
                .getCapacity().getNominalCapacityInBytes());
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
                .createInstance(getAdminClientConfig(currentClusterVersion));
        client.setup(null, null, 0, false);
        client.close();
    }

}
