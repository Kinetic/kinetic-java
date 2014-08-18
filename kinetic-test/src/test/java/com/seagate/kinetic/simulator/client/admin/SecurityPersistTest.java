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
package com.seagate.kinetic.simulator.client.admin;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.junit.Test;

import com.google.protobuf.ByteString;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Command.Header;
import com.seagate.kinetic.proto.Kinetic.Command.MessageType;
import com.seagate.kinetic.proto.Kinetic.Command.Security;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.HMACAlgorithm;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope;
import com.seagate.kinetic.proto.Kinetic.Command.Status;

/**
 *
 * Security persist test
 * <p>
 *
 * @author Chenchong(Emma) Li
 *
 */
public class SecurityPersistTest extends IntegrationTestCase {
    private final byte[] INIT_KEY = "0".getBytes();
    private final byte[] INIT_VALUE = "0".getBytes();
    private final byte[] INIT_VERSION = "0".getBytes();

    @Test
    public void persistTest() throws Exception {
//        Message.Builder request = Message.newBuilder();
//        Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
//        header.setMessageType(MessageType.SECURITY);
//
//        Security.Builder security = request.getCommandBuilder()
//                .getBodyBuilder().getSecurityBuilder();
//
//        // client 1 has all roles
//        ACL.Builder acl1 = ACL.newBuilder();
//        acl1.setIdentity(1);
//        acl1.setKey(ByteString.copyFromUtf8("asdfasdf"));
//        acl1.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
//        Scope.Builder domain = Scope.newBuilder();
//        for (Permission role : Permission.values()) {
//            if (!role.equals(Permission.INVALID_PERMISSION)) {
//                domain.addPermission(role);
//            }
//
//        }
//        acl1.addScope(domain);
//        security.addAcl(acl1);
//
//        // client 2 only has read permission
//        ACL.Builder acl2 = ACL.newBuilder();
//        acl2.setIdentity(2);
//        acl2.setKey(ByteString.copyFromUtf8("asdfasdf2"));
//        acl2.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
//        Scope.Builder domain2 = Scope.newBuilder();
//        domain2.addPermission(Permission.READ);
//        acl2.addScope(domain2);
//        security.addAcl(acl2);
//
//        // case 1: server start, no .acl, the first security request
//        KineticMessage km = new KineticMessage();
//        km.setMessage(request);
//
//        Message response = (Message) this.getClient().request(km).getMessage();
//
//        assertTrue(response.getCommand().getStatus().getCode()
//                .equals(Status.StatusCode.SUCCESS));
//        this.getClient().close();
//
//        // case 2: restart server, then load the .acl and rewrite the aclmap
//        restartServer();
//
//        KineticClient client1 = KineticClientFactory
//                .createInstance(getClientConfig(1, "asdfasdf"));
//        Entry v = null;
//        try {
//            EntryMetadata entryMetadata = new EntryMetadata();
//            client1.put(new Entry(INIT_KEY, INIT_VALUE, entryMetadata),
//                    INIT_VERSION);
//            v = client1.get(INIT_KEY);
//            client1.delete(v);
//        } catch (KineticException e) {
//            fail("put failed, the exception is: " + e.getMessage());
//        }
//        client1.close();
//
//        KineticClient client2 = KineticClientFactory
//                .createInstance(getClientConfig(2, "asdfasdf2"));
//
//        try {
//            v = client2.get(INIT_KEY);
//        } catch (KineticException e) {
//            fail("put failed, the exception is: " + e.getMessage());
//
//        }
//
//        try {
//            EntryMetadata entryMetadata = new EntryMetadata();
//            client2.put(new Entry(INIT_KEY, INIT_VALUE, entryMetadata),
//                    INIT_VERSION);
//            fail("user 2 does not have write role");
//        } catch (KineticException e) {
//            assertTrue(true);
//
//        }
//        client2.close();
//
//        KineticClient client3 = KineticClientFactory
//                .createInstance(getClientConfig(3, "asdfasdf3"));
//        try {
//            client3.get(INIT_KEY);
//            fail("not include user 3");
//        } catch (KineticException e) {
//            assertTrue(true);
//        }
//        client3.close();
//
//        // case 3: restart server the second time, try to update .acl
//        restartServer();
//
//        // request has security permit
//        Message.Builder request1 = Message.newBuilder();
//        Header.Builder header1 = request1.getCommandBuilder()
//                .getHeaderBuilder();
//        header1.setMessageType(MessageType.SECURITY);
//
//        Security.Builder security1 = request1.getCommandBuilder()
//                .getBodyBuilder().getSecurityBuilder();
//
//        // case 31: has all roles(include security)
//        ACL.Builder acl3 = ACL.newBuilder();
//        acl3.setIdentity(1);
//        acl3.setKey(ByteString.copyFromUtf8("asdfasdf"));
//        acl3.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
//        Scope.Builder domain1 = Scope.newBuilder();
//        for (Permission role : Permission.values()) {
//            if (!role.equals(Permission.INVALID_PERMISSION)) {
//                domain1.addPermission(role);
//            }
//        }
//        acl3.addScope(domain1);
//        security1.addAcl(acl3);
//
//        KineticClient client4 = KineticClientFactory
//                .createInstance(getClientConfig(1, "asdfasdf"));
//
//        KineticMessage km1 = new KineticMessage();
//        km1.setMessage(request1);
//
//        Message response1 = (Message) client4.request(km1).getMessage();
//        assertTrue(response1.getCommand().getStatus().getCode()
//                .equals(Status.StatusCode.SUCCESS));
//
//        // request has security permit, could update
//        Message.Builder request2 = Message.newBuilder();
//        Header.Builder header2 = request2.getCommandBuilder()
//                .getHeaderBuilder();
//        header2.setMessageType(MessageType.SECURITY);
//
//        Security.Builder security2 = request2.getCommandBuilder()
//                .getBodyBuilder().getSecurityBuilder();
//
//        // case 32: client 2 has all roles(not include security)
//        ACL.Builder aclAdmin = ACL.newBuilder();
//        aclAdmin.setIdentity(1);
//        aclAdmin.setKey(ByteString.copyFromUtf8("asdfasdf"));
//        aclAdmin.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
//        Scope.Builder domainAdmin = Scope.newBuilder();
//
//        for (Permission role : Permission.values()) {
//            if (!role.equals(Permission.INVALID_PERMISSION)) {
//                domainAdmin.addPermission(role);
//            }
//        }
//
//        aclAdmin.addScope(domainAdmin);
//        security2.addAcl(aclAdmin);
//
//        ACL.Builder acl4 = ACL.newBuilder();
//        acl4.setIdentity(2);
//        acl4.setKey(ByteString.copyFromUtf8("asdfasdf2"));
//        acl4.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
//        Scope.Builder domain3 = Scope.newBuilder();
//        domain3.addPermission(Permission.WRITE);
//        acl4.addScope(domain3);
//        security2.addAcl(acl4);
//
//        KineticMessage km2 = new KineticMessage();
//        km2.setMessage(request2);
//        Message response2 = (Message) client4.request(km2).getMessage();
//        assertTrue(response2.getCommand().getStatus().getCode()
//                .equals(Status.StatusCode.SUCCESS));
//
//        client4.close();
//
//        // request has not security permit, could not update
//        Message.Builder request3 = Message.newBuilder();
//        Header.Builder header3 = request3.getCommandBuilder()
//                .getHeaderBuilder();
//        header3.setMessageType(MessageType.SECURITY);
//
//        Security.Builder security3 = request3.getCommandBuilder()
//                .getBodyBuilder().getSecurityBuilder();
//
//        // case 33: client 1 has write roles(not include security)
//        ACL.Builder acl5 = ACL.newBuilder();
//        acl5.setIdentity(2);
//        acl5.setKey(ByteString.copyFromUtf8("asdfasdf2"));
//        acl5.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
//        Scope.Builder domain4 = Scope.newBuilder();
//        domain4.addPermission(Permission.WRITE);
//        acl5.addScope(domain4);
//        security3.addAcl(acl5);
//
//        KineticClient client5 = KineticClientFactory
//                .createInstance(getClientConfig(2, "asdfasdf2"));
//
//        KineticMessage km3 = new KineticMessage();
//        km3.setMessage(request3);
//        
//        try {
//            @SuppressWarnings("unused")
//            Message response3 = (Message) client5.request(km3).getMessage();
//        } catch (KineticException ke) {
//            assertTrue(ke.getResponseMessage().getMessage().getCommand()
//                    .getStatus().getCode()
//                    .equals(Status.StatusCode.NOT_AUTHORIZED));
//        }
//
//        client5.close();

    }

    @Test
    public void setCorrectHmacAlgorithmTest() throws KineticException,
    InterruptedException, IOException {
//        Message.Builder request = Message.newBuilder();
//        Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
//        header.setMessageType(MessageType.SECURITY);
//
//        Security.Builder security = request.getCommandBuilder()
//                .getBodyBuilder().getSecurityBuilder();
//
//        // client 1 has all roles
//        ACL.Builder acl1 = ACL.newBuilder();
//        acl1.setIdentity(1);
//        acl1.setKey(ByteString.copyFromUtf8("asdfasdf"));
//        acl1.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
//        Scope.Builder domain = Scope.newBuilder();
//        for (Permission role : Permission.values()) {
//            if (!role.equals(Permission.INVALID_PERMISSION)) {
//                domain.addPermission(role);
//            }
//        }
//        acl1.addScope(domain);
//        security.addAcl(acl1);
//
//        // set security
//        KineticMessage km = new KineticMessage();
//        km.setMessage(request);
//        Message response = (Message) this.getClient().request(km).getMessage();
//        assertTrue(response.getCommand().getStatus().getCode()
//                .equals(Status.StatusCode.SUCCESS));
//        this.getClient().close();
    }

    @Test
    public void setInCorrectHmacAlgorithmTest() throws KineticException,
    InterruptedException, IOException {
//        Message.Builder request = Message.newBuilder();
//        Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
//        header.setMessageType(MessageType.SECURITY);
//
//        Security.Builder security = request.getCommandBuilder()
//                .getBodyBuilder().getSecurityBuilder();
//
//        // client 1 has all roles
//        ACL.Builder acl1 = ACL.newBuilder();
//        acl1.setIdentity(1);
//        acl1.setKey(ByteString.copyFromUtf8("asdfasdf"));
//        acl1.setHmacAlgorithm(HMACAlgorithm.INVALID_HMAC_ALGORITHM);
//        Scope.Builder domain = Scope.newBuilder();
//        for (Permission role : Permission.values()) {
//            if (!role.equals(Permission.INVALID_PERMISSION)) {
//                domain.addPermission(role);
//            }
//        }
//        acl1.addScope(domain);
//        security.addAcl(acl1);
//
//        // set security
//        KineticMessage km1 = new KineticMessage();
//        km1.setMessage(request);
//        
//        try {
//            @SuppressWarnings("unused")
//            Message response = (Message) this.getClient().request(km1)
//                    .getMessage();
//        } catch (KineticException ke) {
//            assertTrue(ke.getResponseMessage().getMessage().getCommand()
//                    .getStatus().getCode()
//                    .equals(Status.StatusCode.NO_SUCH_HMAC_ALGORITHM));
//        } finally {
//            this.getClient().close();
//        }
    }

    @Test
    public void setNoHmacAlgorithmTest() throws KineticException,
    InterruptedException, IOException {
//        Message.Builder request = Message.newBuilder();
//        Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
//        header.setMessageType(MessageType.SECURITY);
//
//        Security.Builder security = request.getCommandBuilder()
//                .getBodyBuilder().getSecurityBuilder();
//
//        // client 1 has all roles
//        ACL.Builder acl1 = ACL.newBuilder();
//        acl1.setIdentity(1);
//        acl1.setKey(ByteString.copyFromUtf8("asdfasdf"));
//        Scope.Builder domain = Scope.newBuilder();
//        for (Permission role : Permission.values()) {
//            if (!role.equals(Permission.INVALID_PERMISSION)) {
//                domain.addPermission(role);
//            }
//        }
//        acl1.addScope(domain);
//        security.addAcl(acl1);
//
//        // set security
//        KineticMessage km = new KineticMessage();
//        km.setMessage(request);
//        
//        try {
//            @SuppressWarnings("unused")
//            Message response = (Message) this.getClient().request(km)
//                    .getMessage();
//        } catch (KineticException ke) {
//            assertTrue(ke.getResponseMessage().getMessage().getCommand()
//                    .getStatus().getCode()
//                    .equals(Status.StatusCode.NO_SUCH_HMAC_ALGORITHM));
//        } finally {
//            this.getClient().close();
//        }
    }

    @Test
    public void setNoRoleInDomainTest() throws KineticException,
    InterruptedException, IOException {
//        Message.Builder request = Message.newBuilder();
//        Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
//        header.setMessageType(MessageType.SECURITY);
//
//        Security.Builder security = request.getCommandBuilder()
//                .getBodyBuilder().getSecurityBuilder();
//
//        // client 1 has all roles
//        ACL.Builder acl1 = ACL.newBuilder();
//        acl1.setIdentity(1);
//        acl1.setKey(ByteString.copyFromUtf8("asdfasdf"));
//        acl1.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
//        Scope.Builder domain = Scope.newBuilder();
//        acl1.addScope(domain);
//        security.addAcl(acl1);
//
//        // set security
//        KineticMessage km = new KineticMessage();
//        km.setMessage(request);
//        
//        try {
//        @SuppressWarnings("unused")
//        Message response = (Message) this.getClient().request(km).getMessage();
//        } catch (KineticException ke) {
//            assertTrue(ke.getResponseMessage().getMessage().getCommand().getStatus().getCode()
//                    .equals(Status.StatusCode.INVALID_REQUEST));
//            //assertTrue(response.getCommand().getStatus().getStatusMessage()
//            //        .equals("No role set in acl"));
//        } finally {
//        this.getClient().close();
//        }
    }

    @Test
    public void setInvalidRoleInDomainTest() throws KineticException,
    InterruptedException, IOException {
//        Message.Builder request = Message.newBuilder();
//        Header.Builder header = request.getCommandBuilder().getHeaderBuilder();
//        header.setMessageType(MessageType.SECURITY);
//
//        Security.Builder security = request.getCommandBuilder()
//                .getBodyBuilder().getSecurityBuilder();
//
//        // client 1 has all roles
//        ACL.Builder acl1 = ACL.newBuilder();
//        acl1.setIdentity(1);
//        acl1.setKey(ByteString.copyFromUtf8("asdfasdf"));
//        acl1.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
//        Scope.Builder domain = Scope.newBuilder();
//        for (Permission role : Permission.values()) {
//            domain.addPermission(role);
//        }
//
//        acl1.addScope(domain);
//        security.addAcl(acl1);
//
//        // set security
//        KineticMessage km = new KineticMessage();
//        km.setMessage(request);
//        
//        try {
//            this.getClient().request(km);
//        } catch (KineticException ke) {      
//            assertTrue(ke.getResponseMessage().getMessage().getCommand()
//                    .getStatus().getCode()
//                    .equals(Status.StatusCode.INVALID_REQUEST));
//            // assertTrue(response.getCommand().getStatus().getStatusMessage()
//            // .startsWith("Role is invalid in acl"));
//        } finally {
//            this.getClient().close();
//        }
    }
}
