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

import org.testng.annotations.Test;
import org.testng.Assert;

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import kinetic.client.KineticException;

import com.seagate.kinetic.IntegrationTestCase;

@Test (groups = {"simulator"})
@SuppressWarnings("deprecation")
public class FirmwareDownloadTest extends IntegrationTestCase {
    @Test
    public void testFirmwareDownload_NoPin() {
        byte[] firmwareInfo = "firmware download info".getBytes();
        try {
            getAdminClient().firmwareDownload(null, firmwareInfo);
        } catch (KineticException e) {
            Assert.fail("firmware download failed");
        }
    }

    @Test
    public void testFirmwareDownload_WithCorrectPin() {
        byte[] firmwareInfo = "firmware download info after pin set".getBytes();
        byte[] newErasePin = toByteArray("123");

        try {
            getAdminClient().setErasePin(null, newErasePin);
        } catch (KineticException e1) {
            Assert.fail("set erase pin failed");
        }

        try {
            getAdminClient().firmwareDownload(newErasePin, firmwareInfo);
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e) {
            try {
                getAdminClient().instantErase(newErasePin);
            } catch (KineticException e1) {
                Assert.fail("instant erase failed: " + e1.getMessage());
            }
            Assert.fail("firmware download failed");
        }
    }

    @Test
    public void testFirmwareDownload_WithIncorrectPin() {
        byte[] firmwareInfo = "firmware download info after pin set".getBytes();
        byte[] newErasePin = toByteArray("123");

        try {
            getAdminClient().setErasePin(null, newErasePin);
        } catch (KineticException e) {
            Assert.fail("set erase pin throw exception: " + e.getMessage());
        }

        byte[] incorrectPin = toByteArray("456");

        try {
            getAdminClient().firmwareDownload(incorrectPin, firmwareInfo);
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e) {
            try {
                getAdminClient().instantErase(newErasePin);
            } catch (KineticException e1) {
                Assert.fail("instant erase throw exception: " + e1.getMessage());
            }
        }
    }
}
