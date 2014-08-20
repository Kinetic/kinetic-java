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

import java.util.ArrayList;
import java.util.List;

import kinetic.admin.ACL;
import kinetic.admin.AdminClientConfiguration;
import kinetic.admin.Capacity;
import kinetic.admin.Domain;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.admin.KineticLog;
import kinetic.admin.Role;
import kinetic.admin.Temperature;
import kinetic.admin.Utilization;
import kinetic.client.KineticException;

public class KineticAdminUsage {

    public static void main(String[] args) throws KineticException {
        AdminClientConfiguration config = new AdminClientConfiguration();
        config.setHost("localhost");
        KineticAdminClient adminClient = KineticAdminClientFactory
                .createInstance(config);

        // setup, set pin, clusterVersion and erase db
        adminClient.setup(null, "pin001".getBytes(), 1, true);
        
        adminClient.close();

        config.setClusterVersion(1);
        KineticAdminClient adminClient1 = KineticAdminClientFactory
                .createInstance(config);
        KineticLog kineticLog = adminClient1.getLog();
        @SuppressWarnings("unused")
        List<Utilization> utils = kineticLog.getUtilization();
        @SuppressWarnings("unused")
        List<Temperature> temps = kineticLog.getTemperature();
        @SuppressWarnings("unused")
        Capacity capacity = kineticLog.getCapacity();

        adminClient1.close();

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
        acl1.setUserId(0);
        acl1.setKey("asdfasdf");

        acls.add(acl1);

        config.setClusterVersion(1);
        KineticAdminClient adminClient2 = KineticAdminClientFactory
                .createInstance(config);
        
        byte[] pin = "1".getBytes();
        
        adminClient2.setSecurity(acls, null, pin, null, pin);

        adminClient2.close();

    }

}
