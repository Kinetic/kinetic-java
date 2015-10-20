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
        adminClient.instantErase(null);
        adminClient.setErasePin(null, "pin001".getBytes());
        adminClient.setClusterVersion(1);
        
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
        
        adminClient2.setErasePin(null, pin);
        adminClient2.setLockPin(null, pin);

        adminClient2.close();

    }

}
