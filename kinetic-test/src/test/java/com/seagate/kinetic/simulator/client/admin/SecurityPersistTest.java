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
import java.util.ArrayList;
import java.util.List;

import kinetic.admin.ACL;
import kinetic.admin.Domain;
import kinetic.admin.Role;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;

/**
 *
 * Security persist test
 * <p>
 *
 */
public class SecurityPersistTest extends IntegrationTestCase {
    private final byte[] INIT_KEY = "0".getBytes();
    private final byte[] INIT_VALUE = "0".getBytes();
    private final byte[] INIT_VERSION = "0".getBytes();

    @Test
    public void persistTest() throws Exception {
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

        // // case 1: server start, no .acl, the first security request
        try {
            getAdminClient().setAcl(acls);
        } catch (KineticException e1) {
            fail("set security throw exception: " + e1.getMessage());
        }

        // // case 2: restart server, then load the .acl and rewrite the aclmap
        restartServer();

        KineticClient client1 = KineticClientFactory
                .createInstance(getClientConfig(1, "asdfasdf"));
        Entry v = null;
        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            client1.put(new Entry(INIT_KEY, INIT_VALUE, entryMetadata),
                    INIT_VERSION);
            v = client1.get(INIT_KEY);
            client1.delete(v);
        } catch (KineticException e) {
            fail("put failed, the exception is: " + e.getMessage());
        }
        client1.close();

        KineticClient client2 = KineticClientFactory
                .createInstance(getClientConfig(2, "asdfasdf2"));

        try {
            v = client2.get(INIT_KEY);
        } catch (KineticException e) {
            fail("put failed, the exception is: " + e.getMessage());

        }

        try {
            EntryMetadata entryMetadata = new EntryMetadata();
            client2.put(new Entry(INIT_KEY, INIT_VALUE, entryMetadata),
                    INIT_VERSION);
            fail("user 2 does not have write role");
        } catch (KineticException e) {
            assertTrue(true);

        }
        client2.close();
    }

    @Test
    public void setCorrectHmacAlgorithmTest() throws KineticException,
            InterruptedException, IOException {
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
        acl1.setAlgorithm("HmacSHA1");

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

        // case 1: server start, no .acl, the first security request
        try {
            getAdminClient().setAcl(acls);
        } catch (KineticException e1) {
            fail("set security throw exception: " + e1.getMessage());
        }
    }

    @Test
    public void setNoRoleInDomainTest() {

        Domain domain1 = new Domain();
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
            fail("should throw exception.");
        } catch (KineticException e1) {
            assertTrue(e1.getMessage().contains("Paramter Exception"));
        }
    }

}
