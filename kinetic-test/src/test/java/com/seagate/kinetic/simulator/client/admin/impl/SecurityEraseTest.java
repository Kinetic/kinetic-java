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
package com.seagate.kinetic.simulator.client.admin.impl;

import static com.seagate.kinetic.KineticTestHelpers.setDefaultAcls;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import kinetic.admin.ACL;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.client.KineticException;

import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;

//import com.seagate.kinetic.proto.Kinetic.Message.Setup;

public class SecurityEraseTest extends IntegrationTestCase {

    String pin = "pin001";

    @Test
    public void testSecureErase_NoPinSetForDrive() {
        byte[] pin = null;
        try {
            getAdminClient().instantErase(pin);
        } catch (KineticException e) {
            fail("secure erase throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testSecureErase_WithWrongPinOFDrive() {
        byte[] newErasePin = toByteArray("123");
        byte[] wrongPin = toByteArray("456");

        List<ACL> acls = new ArrayList<ACL>();
        acls = setDefaultAcls();
        try {
            getAdminClient().setSecurity(acls, null, null, null, newErasePin);
        } catch (KineticException e) {
            fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(wrongPin);
            fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getMessage().contains("NOT_AUTHORIZED"));
        }

        // clean pin
        try {
            getAdminClient().setSecurity(acls, null, null, newErasePin, null);
        } catch (KineticException e) {
            fail("clean pin throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testSecureErase_WithRightPinOFDrive() {
        byte[] newErasePin = toByteArray("123");

        List<ACL> acls = new ArrayList<ACL>();
        acls = setDefaultAcls();
        try {
            getAdminClient().setSecurity(acls, null, null, null, newErasePin);
        } catch (KineticException e) {
            fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e) {
            fail("instant erase throw exception: " + e.getMessage());
        }

        // clean pin
        try {
            getAdminClient().setSecurity(acls, null, null, newErasePin, null);
        } catch (KineticException e) {
            fail("clean pin throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testSecureErase_WithRightACL() {

        try {
            getAdminClient().instantErase(toByteArray(pin));
        } catch (KineticException e) {
            fail("instant erase with right ACL throw exception: "
                    + e.getMessage());
        }
    }

    @Test
    public void testSecureErase_WithWrongClientID_ShouldPass() {
        KineticAdminClient adminClient = null;
        try {
            adminClient = KineticAdminClientFactory
                    .createInstance(getAdminClientConfig(2, "asdfasdf"));
        } catch (KineticException e) {
            fail("create kinetic admin client throw exception: "
                    + e.getMessage());
        }

        try {
            adminClient.instantErase(null);
        } catch (KineticException e) {
            fail("instant erase throw exception: " + e.getMessage());
        } finally {
            try {
                adminClient.close();
            } catch (KineticException e) {
                fail("close admin client throw exception: " + e.getMessage());
            }
        }
    }

    @Test
    public void testSecureErase_WithWrongClientKey_ShouldPass() {
        KineticAdminClient adminClient = null;
        try {
            adminClient = KineticAdminClientFactory
                    .createInstance(getAdminClientConfig(1, "asdfasdf1"));
        } catch (KineticException e) {
            fail("create kinetic admin client throw exception: "
                    + e.getMessage());
        }

        try {
            adminClient.instantErase(null);
        } catch (KineticException e) {
            fail("instant erase throw exception: " + e.getMessage());
        } finally {
            try {
                adminClient.close();
            } catch (KineticException e) {
                fail("close admin client throw exception: " + e.getMessage());
            }
        }
    }

}
