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

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.junit.Assert.fail;
import kinetic.client.KineticException;

import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.SimulatorOnly;

public class FirmwareDownloadTest extends IntegrationTestCase {
    @Test
    @SimulatorOnly
    public void testFirmwareDownload_NoPin() {
        byte[] firmwareInfo = "firmware download info".getBytes();
        try {
            getAdminClient().firmwareDownload(null, firmwareInfo);
        } catch (KineticException e) {
            fail("firmware download failed");
        }
    }

    @Test
    @SimulatorOnly
    public void testFirmwareDownload_WithCorrectPin() {
        byte[] firmwareInfo = "firmware download info after pin set".getBytes();
        byte[] newErasePin = toByteArray("123");

        try {
            getAdminClient().setErasePin(null, newErasePin);
        } catch (KineticException e1) {
            fail("set erase pin failed");
        }

        try {
            getAdminClient().firmwareDownload(newErasePin, firmwareInfo);
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e) {
            try {
                getAdminClient().instantErase(newErasePin);
            } catch (KineticException e1) {
                fail("instant erase failed: " + e1.getMessage());
            }
            fail("firmware download failed");
        }
    }

    @Test
    @SimulatorOnly
    public void testFirmwareDownload_WithIncorrectPin() {
        byte[] firmwareInfo = "firmware download info after pin set".getBytes();
        byte[] newErasePin = toByteArray("123");

        try {
            getAdminClient().setErasePin(null, newErasePin);
        } catch (KineticException e) {
            fail("set erase pin throw exception: " + e.getMessage());
        }

        byte[] incorrectPin = toByteArray("456");

        try {
            getAdminClient().firmwareDownload(incorrectPin, firmwareInfo);
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e) {
            try {
                getAdminClient().instantErase(newErasePin);
            } catch (KineticException e1) {
                fail("instant erase throw exception: " + e1.getMessage());
            }
        }
    }
}
