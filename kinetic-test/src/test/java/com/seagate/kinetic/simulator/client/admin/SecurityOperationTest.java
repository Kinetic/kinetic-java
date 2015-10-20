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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import kinetic.admin.ACL;
import kinetic.admin.Domain;
import kinetic.admin.Role;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.admin.impl.DefaultAdminClient;
import com.seagate.kinetic.client.internal.ClientProxy.LCException;

@Test (groups = {"simulator"})
public class SecurityOperationTest extends IntegrationTestCase {

    Logger logger = Logger.getLogger(SecurityOperationTest.class.getName());

    private DefaultAdminClient kineticClient = null;

    @BeforeMethod
    public void setUp() throws Exception {
        kineticClient = new DefaultAdminClient(getClientConfig());
    }

    @AfterMethod
    public void tearDown() throws Exception {
        kineticClient.close();
    }

    @Test
    public void addAclTest() throws LCException, KineticException {
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

        try {
            getAdminClient().setAcl(acls);
        } catch (KineticException e1) {
            Assert.fail("set security throw exception: " + e1.getMessage());
        }

        // right user, wrong key
        KineticClient kineticClient2 = KineticClientFactory
                .createInstance(getClientConfig(1, "asdfasdf2"));
        try {

            kineticClient2.get(ByteString.copyFromUtf8("1234").toByteArray());
            Assert.fail("no exception was thrown");
        } catch (Exception e) {
            logger.info("caught expected exception");
        }

        kineticClient2.close();

        // right user/key
        KineticClient kineticClient3 = KineticClientFactory
                .createInstance(getClientConfig(1, "asdfasdf"));
        try {

            kineticClient3.get(ByteString.copyFromUtf8("1234").toByteArray());

        } catch (Exception e) {
            logger.info("caught unexpected exception");
            Assert.fail("caught unexpected exception");
        }

        kineticClient3.close();

    }
}
