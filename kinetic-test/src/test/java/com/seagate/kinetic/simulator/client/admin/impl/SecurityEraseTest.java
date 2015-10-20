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
package com.seagate.kinetic.simulator.client.admin.impl;

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.testng.Assert;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.client.KineticException;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

@Test (groups = {"simulator"})
public class SecurityEraseTest extends IntegrationTestCase {

    String pin = "pin001";

    @Test
    public void testSecureErase_NoPinSetForDrive() {
        byte[] pin = null;
        try {
            getAdminClient().instantErase(pin);
        } catch (KineticException e) {
            Assert.fail("secure erase throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testSecureErase_WithWrongPinOFDrive() {
        byte[] newErasePin = toByteArray("123");
        byte[] wrongPin = toByteArray("456");

        try {
            getAdminClient().setErasePin(null, newErasePin);
        } catch (KineticException e) {
            Assert.fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(wrongPin);
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.NOT_AUTHORIZED));
        }

        try {
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e1) {
            Assert.fail("instant erase throw exception: " + e1.getMessage());
        }
    }

    @Test
    public void testSecureErase_WithRightPinOFDrive() {
        byte[] newErasePin = toByteArray("123");

        try {
            getAdminClient().setErasePin(null, newErasePin);
        } catch (KineticException e) {
            Assert.fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e1) {
            Assert.fail("instant erase throw exception: " + e1.getMessage());
        }
    }

    @Test
    public void testSecureErase_WithRightACL() {

        try {
            getAdminClient().instantErase(null);
        } catch (KineticException e) {
            Assert.fail("instant erase with right ACL throw exception: "
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
            Assert.fail("create kinetic admin client throw exception: "
                    + e.getMessage());
        }

        try {
            adminClient.instantErase(null);
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception: " + e.getMessage());
        } finally {
            try {
                adminClient.close();
            } catch (KineticException e) {
                Assert.fail("close admin client throw exception: " + e.getMessage());
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
            Assert.fail("create kinetic admin client throw exception: "
                    + e.getMessage());
        }

        try {
            adminClient.instantErase(null);
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception: " + e.getMessage());
        } finally {
            try {
                adminClient.close();
            } catch (KineticException e) {
                Assert.fail("close admin client throw exception: " + e.getMessage());
            }
        }
    }

}
