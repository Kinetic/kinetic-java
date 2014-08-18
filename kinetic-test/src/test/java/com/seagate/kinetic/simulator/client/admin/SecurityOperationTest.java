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

import java.util.logging.Logger;

import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.admin.impl.DefaultAdminClient;
import com.seagate.kinetic.client.internal.ClientProxy.LCException;
import com.seagate.kinetic.proto.Kinetic.Message;
import com.seagate.kinetic.proto.Kinetic.Command.Security;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.HMACAlgorithm;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope;
import com.seagate.kinetic.proto.Kinetic.Command.Status;

public class SecurityOperationTest extends IntegrationTestCase {

    Logger logger = Logger.getLogger(SecurityOperationTest.class.getName());

    private DefaultAdminClient kineticClient = null;

    @Before
    public void setUp() throws Exception {
        kineticClient = new DefaultAdminClient(getClientConfig());
    }

    @After
    public void tearDown() throws Exception {
        kineticClient.close();
    }

    @Test
    public void addAclTest() throws LCException, KineticException {
//        Message.Builder request = Message.newBuilder();
//
//        Security.Builder sb = request.getCommandBuilder().getBodyBuilder()
//                .getSecurityBuilder();
//
//        ACL.Builder acl = ACL.newBuilder();
//        acl.setIdentity(1);
//        acl.setKey(ByteString.copyFromUtf8("asdfasdf"));
//        acl.setHmacAlgorithm(HMACAlgorithm.HmacSHA1);
//
//        Scope.Builder domain = Scope.newBuilder();
//
//        for (Permission role : Permission.values()) {
//            if (!role.equals(Permission.INVALID_PERMISSION)) {
//                domain.addPermission(role);
//            }
//        }
//
//        acl.addScope(domain);
//
//        sb.addAcl(acl.build());
//
//        Message respond = this.kineticClient.configureSecurityPolicy(request);
//
//        assertTrue(respond.getCommand().getStatus().getCode()
//                .equals(Status.StatusCode.SUCCESS));
//        this.kineticClient.close();
//
//        // right user, wrong key
//        KineticClient kineticClient2 = KineticClientFactory
//                .createInstance(getClientConfig(1, "asdfasdf2"));
//        try {
//
//            kineticClient2.get(ByteString.copyFromUtf8("1234").toByteArray());
//            fail("no exception was thrown");
//        } catch (Exception e) {
//            logger.info("caught expected exception");
//        }
//
//        kineticClient2.close();
//
//        // right user/key
//        KineticClient kineticClient3 = KineticClientFactory
//                .createInstance(getClientConfig(1, "asdfasdf"));
//        try {
//
//            kineticClient3.get(ByteString.copyFromUtf8("1234").toByteArray());
//
//        } catch (Exception e) {
//            logger.info("caught unexpected exception");
//            fail("caught unexpected exception");
//        }
//
//        kineticClient3.close();
//
//        // wrong user, right key
//        KineticClient kineticClient4 = KineticClientFactory
//                .createInstance(getClientConfig(2, "asdfasdf"));
//        try {
//
//            kineticClient4.get(ByteString.copyFromUtf8("1234").toByteArray());
//            fail("no exception was thrown");
//        } catch (Exception e) {
//            logger.info("caught expected exception");
//        }
//
//        kineticClient4.close();
    }
}
