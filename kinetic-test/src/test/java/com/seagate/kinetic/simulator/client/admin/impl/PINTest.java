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
import kinetic.client.KineticException;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

/**
 *
 * Setup test
 * <p>
 *
 * @author Chenchong Li
 *
 */
@Test (groups = {"simulator"})
public class PINTest extends IntegrationTestCase {

    @Test
    public void testSetErasePin() {
        byte[] newErasePin = toByteArray("123");

        try {
            getAdminClient().setErasePin(null, newErasePin);
        } catch (KineticException e) {
            Assert.fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e1) {
            Assert.fail("instant erase throw exception: " + e1.getMessage());
        }
    }

    @Test
    public void testModifyOldPin_WithRightOldPin() {
        byte[] oldErasePin = toByteArray("123");
        byte[] newErasePin = toByteArray("456");

        try {
            getAdminClient().setErasePin(null, oldErasePin);
        } catch (KineticException e) {
            Assert.fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().setErasePin(oldErasePin, newErasePin);
        } catch (KineticException e) {
            Assert.fail("modify pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e1) {
            Assert.fail("instant erase throw exception: " + e1.getMessage());
        }
    }

    @Test
    public void testModifyOldPin_WithWrongOldPin() {
        byte[] oldErasePin = toByteArray("123");
        byte[] oldIncorrectErasePin = toByteArray("456");
        byte[] newErasePin = toByteArray("789");

        try {
            getAdminClient().setErasePin(null, oldErasePin);
        } catch (KineticException e) {
            Assert.fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().setErasePin(oldIncorrectErasePin, newErasePin);
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.NOT_AUTHORIZED));
        }

        try {
            getAdminClient().instantErase(oldErasePin);
        } catch (KineticException e1) {
            Assert.fail("instant erase throw exception: " + e1.getMessage());
        }
    }

    @Test
    public void testModifyOldPinWithRightPin_AfterRestartServer() {
        byte[] oldErasePin = toByteArray("123");
        byte[] newErasePin = toByteArray("456");

        try {
            getAdminClient().setErasePin(null, oldErasePin);
        } catch (KineticException e) {
            Assert.fail("set pin throw exception: " + e.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            Assert.fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().setErasePin(oldErasePin, newErasePin);
        } catch (KineticException e) {
            Assert.fail("modify pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e1) {
            Assert.fail("instant erase throw exception: " + e1.getMessage());
        }
    }

    @Test
    public void testModifyOldPin_WithWrongPin_AfterRestartServer() {
        byte[] oldErasePin = toByteArray("123");
        byte[] oldIncorrectErasePin = toByteArray("456");
        byte[] newErasePin = toByteArray("789");

        try {
            getAdminClient().setErasePin(null, oldErasePin);
        } catch (KineticException e) {
            Assert.fail("set pin throw exception: " + e.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            Assert.fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().setErasePin(oldIncorrectErasePin, newErasePin);
            Assert.fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.NOT_AUTHORIZED));
        }

        try {
            getAdminClient().instantErase(oldErasePin);
        } catch (KineticException e1) {
            Assert.fail("instant erase throw exception: " + e1.getMessage());
        }
    }

}
