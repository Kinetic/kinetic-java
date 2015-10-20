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

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import kinetic.client.KineticException;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

@Test (groups = {"simulator"})
public class PinOpTest extends IntegrationTestCase {

    @Test
    public void testLockDevice_withUnNullOrEmptyLockPin_LockEnable() {
        // set a lock pin
        byte[] lockPinB = toByteArray("lockpin");
        try {
            getAdminClient().setLockPin(null, lockPinB);
        } catch (KineticException e1) {
            Assert.fail("set lock pin throw exception: " + e1.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            Assert.fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().getLog();
            Assert.fail("Should throw exception");
        } catch (KineticException e) {
            AssertJUnit.assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.DEVICE_LOCKED));
        }

        // clean up: unlock device and then instant erase
        try {
            getAdminClient().unLockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("unlock device throw exception" + e.getMessage());
        }

        try {
            getAdminClient().instantErase(null);
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }
    }

    @Test
    public void testLockDevice_withUnNullOrEmptyLockPin_LockEnable_unLockDevice() {
        // set a lock pin
        byte[] lockPinB = toByteArray("lockpin");
        try {
            getAdminClient().setLockPin(null, lockPinB);
        } catch (KineticException e1) {
            Assert.fail("set lock pin throw exception: " + e1.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            Assert.fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().unLockDevice(lockPinB);
        } catch (KineticException e1) {
            Assert.fail("unlock device throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().getLog();
        } catch (KineticException e) {
            Assert.fail("After unlock device, op throw exception: " + e.getMessage());
        }

        // clean up: erase lock pin
        try {
            getAdminClient().instantErase(null);
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception" + e.getMessage());
        }
    }

    @Test
    public void testLockDevice_withNullLockPin_LockDisable() {
        // set a lock pin
        try {
            getAdminClient().setLockPin(null, null);
        } catch (KineticException e1) {
            Assert.fail("set lock pin throw exception: " + e1.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            Assert.fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().getLog();
        } catch (KineticException e) {
            Assert.fail("Lock disable, op throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testLockDevice_withEmptyLockPin_LockDisable() {
        // set a lock pin
        byte[] lockPinB = toByteArray("");
        try {
            getAdminClient().setLockPin(null, lockPinB);
        } catch (KineticException e1) {
            Assert.fail("set lock pin throw exception: " + e1.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            Assert.fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().getLog();
        } catch (KineticException e) {
            Assert.fail("Lock disable, op throw exception: " + e.getMessage());
        }
    }

}
