package com.seagate.kinetic.simulator.client.admin.impl;

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.client.internal.ClientProxy.LCException;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Message.Security;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.HMACAlgorithm;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Scope;
import com.seagate.kinetic.proto.Kinetic.Message.Status;

/**
 *
 * Security test
 * <p>
 *
 * @author Chenchong Li
 *
 */

public class SecurityTest extends IntegrationTestCase {
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

    @Test
    public void testMultiUsersACLs_VerifyRoles() throws LCException,
    KineticException {
        Message.Builder request = Message.newBuilder();
        Security.Builder security = request.getCommandBuilder()
                .getBodyBuilder().getSecurityBuilder();

        // client 1 has all roles
        ACL.Builder acl1 = ACL.newBuilder();
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
        ACL.Builder acl2 = ACL.newBuilder();
        acl2.setIdentity(2);
        acl2.setKey(ByteString.copyFromUtf8("asdfasdf2"));
        acl2.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain2 = Scope.newBuilder();
        domain2.addPermission(Permission.READ);
        acl2.addScope(domain2);
        security.addAcl(acl2);

        // client 3 only has write permission
        ACL.Builder acl3 = ACL.newBuilder();
        acl3.setIdentity(3);
        acl3.setKey(ByteString.copyFromUtf8("asdfasdf3"));
        acl3.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain3 = Scope.newBuilder();
        domain3.addPermission(Permission.WRITE);
        acl3.addScope(domain3);
        security.addAcl(acl3);

        // client 4 only has delete permission
        ACL.Builder acl4 = ACL.newBuilder();
        acl4.setIdentity(4);
        acl4.setKey(ByteString.copyFromUtf8("asdfasdf4"));
        acl4.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain4 = Scope.newBuilder();
        domain4.addPermission(Permission.DELETE);
        acl4.addScope(domain4);
        security.addAcl(acl4);

        // client 5 only has read and write permission
        ACL.Builder acl5 = ACL.newBuilder();
        acl5.setIdentity(5);
        acl5.setKey(ByteString.copyFromUtf8("asdfasdf5"));
        acl5.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain5 = Scope.newBuilder();
        domain5.addPermission(Permission.READ);
        domain5.addPermission(Permission.WRITE);
        acl5.addScope(domain5);
        security.addAcl(acl5);

        // client 6 only has read and delete permission
        ACL.Builder acl6 = ACL.newBuilder();
        acl6.setIdentity(6);
        acl6.setKey(ByteString.copyFromUtf8("asdfasdf6"));
        acl6.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
        Scope.Builder domain6 = Scope.newBuilder();
        domain6.addPermission(Permission.READ);
        domain6.addPermission(Permission.DELETE);
        acl6.addScope(domain6);
        security.addAcl(acl6);

        // client 7 only has write and delete permission
        ACL.Builder acl7 = ACL.newBuilder();
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
        ACL.Builder acl = null;
        Message response = null;

        request = Message.newBuilder();
        security = request.getCommandBuilder().getBodyBuilder()
                .getSecurityBuilder();

        // add admin acl info
        ACL.Builder aclAdmin = ACL.newBuilder();
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

        acl = ACL.newBuilder();
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

        // operation in domain 0
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

        // operation in domain 1
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

        // operation in domain 2
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

        // operation in domain 3
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

        // operation in domain 4
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

        // operation in domain 5
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

        // operation in domain 6
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

        // wrong domain operation
        byte[] key7 = null;
        try {
            key7 = toByteArray("domain7key007");
            EntryMetadata entryMetadata = new EntryMetadata();
            kineticClient.put(new Entry(key7, value, entryMetadata), version);
            fail("The user do not have right domain");
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("permission denied") != -1);
        }

        // key size smaller than domain
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
        ACL.Builder acl = ACL.newBuilder();
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
        Message response = getAdminClient().configureSecurityPolicy(request);
        assertEquals(Status.StatusCode.INTERNAL_ERROR, response.getCommand()
                .getStatus().getCode());
    }

    @Test
    public void testAlgorithm_DefaultAlgorithmHmacSha1()
            throws KineticException {
        Message.Builder request = Message.newBuilder();
        Security.Builder security = request.getCommandBuilder()
                .getBodyBuilder().getSecurityBuilder();

        // client 1 has all roles
        ACL.Builder acl1 = ACL.newBuilder();
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

    // @Test
    public void testAlgorithm_NotDefaultAlgorithmHmacSha1()
            throws KineticException {
        ACL.Builder acl1 = ACL.newBuilder();
        acl1.setIdentity(1);
        acl1.setKey(ByteString.copyFromUtf8("asdfasdf"));
        try {
            acl1.setHmacAlgorithm(HMACAlgorithm.valueOf("SHA2"));
        } catch (Exception e) {
            assertTrue(true);
        }
    }
}
