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
package com.seagate.kinetic.simulator.client.admin;

import org.testng.annotations.Test;
import com.google.protobuf.ByteString;
import com.seagate.kinetic.admin.impl.JsonUtil;
import com.seagate.kinetic.proto.Kinetic.Command;

import com.seagate.kinetic.proto.Kinetic.Command.GetLog.Type;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;

@Test (groups = {"simulator"})
public class GenerateJsonUtilForAdminScript {

    /*
     * Generate security setup temperature json files
     */
    @Test
    public void generateAllJsonFileTest() {
        com.seagate.kinetic.proto.Kinetic.Command.Security.Builder security = com.seagate.kinetic.proto.Kinetic.Command.Security
                .newBuilder();

        // client 1 has all roles
        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Builder acl1 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL
                .newBuilder();
        acl1.setIdentity(1);
        acl1.setKey(ByteString.copyFromUtf8("asdfasdf1"));

        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope.Builder domain = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope
                .newBuilder();

        for (Permission role : Permission.values()) {
            if (!role.equals(Permission.INVALID_PERMISSION)) {
                domain.addPermission(role);
            }
        }

        acl1.addScope(domain);

        security.addAcl(acl1);

        // client 2 only has read permission
        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Builder acl2 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL
                .newBuilder();
        acl2.setIdentity(2);
        acl2.setKey(ByteString.copyFromUtf8("asdfasdf2"));

        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope.Builder domain2 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope
                .newBuilder();
        domain2.addPermission(Permission.READ);
        acl2.addScope(domain2);

        security.addAcl(acl2);

        // client 3 only has write permission
        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Builder acl3 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL
                .newBuilder();
        acl3.setIdentity(3);
        acl3.setKey(ByteString.copyFromUtf8("asdfasdf3"));

        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope.Builder domain3 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope
                .newBuilder();
        domain3.addPermission(Permission.WRITE);
        acl3.addScope(domain3);

        security.addAcl(acl3);

        // client 4 only has delete permission
        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Builder acl4 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL
                .newBuilder();
        acl4.setIdentity(4);
        acl4.setKey(ByteString.copyFromUtf8("asdfasdf4"));

        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope.Builder domain4 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope
                .newBuilder();
        domain4.addPermission(Permission.DELETE);
        acl4.addScope(domain4);

        security.addAcl(acl4);

        // client 5 only has read and write permission
        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Builder acl5 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL
                .newBuilder();
        acl5.setIdentity(5);
        acl5.setKey(ByteString.copyFromUtf8("asdfasdf5"));

        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope.Builder domain5 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope
                .newBuilder();
        domain5.addPermission(Permission.READ);
        domain5.addPermission(Permission.WRITE);
        acl5.addScope(domain5);

        security.addAcl(acl5);

        // client 6 only has read and delete permission
        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Builder acl6 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL
                .newBuilder();
        acl6.setIdentity(6);
        acl6.setKey(ByteString.copyFromUtf8("asdfasdf6"));

        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope.Builder domain6 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope
                .newBuilder();
        domain6.addPermission(Permission.READ);
        domain6.addPermission(Permission.DELETE);
        acl6.addScope(domain6);

        security.addAcl(acl6);

        // client 7 only has write and delete permission
        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Builder acl7 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL
                .newBuilder();
        acl7.setIdentity(7);
        acl7.setKey(ByteString.copyFromUtf8("asdfasdf7"));

        com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope.Builder domain7 = com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Scope
                .newBuilder();
        domain7.addPermission(Permission.WRITE);
        domain7.addPermission(Permission.DELETE);
        acl7.addScope(domain7);

        security.addAcl(acl7);
        JsonUtil.toJson(security.build());
        //		System.out.println(JsonUtil.toJson(security.build()));

        Command.Builder setupBuilder = Command.newBuilder();
        com.seagate.kinetic.proto.Kinetic.Command.Setup.Builder setup = setupBuilder
                .getBodyBuilder().getSetupBuilder();
        setup.setNewClusterVersion(1);
        
        /**
         * XXX protocol-3.0.0
         */
        
        //setup.setInstantSecureErase(false);
        //setup.setSetPin(ByteString.copyFromUtf8("pin002"));
        //setup.setPin(ByteString.copyFromUtf8("pin001"));

        JsonUtil.toJson(setup.build());
        //		System.out.println(JsonUtil.toJson(setup.build()));

        Command.Builder getLogBuilder = Command.newBuilder();
        com.seagate.kinetic.proto.Kinetic.Command.GetLog.Builder getLog = getLogBuilder
                .getBodyBuilder().getGetLogBuilder();
        getLog.addTypes(Type.TEMPERATURES);
        getLog.addTypes(Type.CAPACITIES);
        getLog.addTypes(Type.UTILIZATIONS);
        JsonUtil.toJson(getLog.build());
        //		System.out.println(JsonUtil.toJson(getLog.build()));

    }

}
