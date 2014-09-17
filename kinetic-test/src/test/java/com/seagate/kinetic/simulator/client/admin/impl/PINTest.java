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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import kinetic.client.KineticException;

import org.junit.Test;

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
public class PINTest extends IntegrationTestCase {

    @Test
    public void testSetErasePin() {
        byte[] newErasePin = toByteArray("123");

        try {
            getAdminClient().setErasePin(null, newErasePin);
        } catch (KineticException e) {
            fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e1) {
            fail("instant erase throw exception: " + e1.getMessage());
        }
    }

    @Test
    public void testModifyOldPin_WithRightOldPin() {
        byte[] oldErasePin = toByteArray("123");
        byte[] newErasePin = toByteArray("456");

        try {
            getAdminClient().setErasePin(null, oldErasePin);
        } catch (KineticException e) {
            fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().setErasePin(oldErasePin, newErasePin);
        } catch (KineticException e) {
            fail("modify pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e1) {
            fail("instant erase throw exception: " + e1.getMessage());
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
            fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().setErasePin(oldIncorrectErasePin, newErasePin);
            fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.NOT_AUTHORIZED));
        }

        try {
            getAdminClient().instantErase(oldErasePin);
        } catch (KineticException e1) {
            fail("instant erase throw exception: " + e1.getMessage());
        }
    }

    @Test
    public void testModifyOldPinWithRightPin_AfterRestartServer() {
        byte[] oldErasePin = toByteArray("123");
        byte[] newErasePin = toByteArray("456");

        try {
            getAdminClient().setErasePin(null, oldErasePin);
        } catch (KineticException e) {
            fail("set pin throw exception: " + e.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().setErasePin(oldErasePin, newErasePin);
        } catch (KineticException e) {
            fail("modify pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().instantErase(newErasePin);
        } catch (KineticException e1) {
            fail("instant erase throw exception: " + e1.getMessage());
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
            fail("set pin throw exception: " + e.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().setErasePin(oldIncorrectErasePin, newErasePin);
            fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.NOT_AUTHORIZED));
        }

        try {
            getAdminClient().instantErase(oldErasePin);
        } catch (KineticException e1) {
            fail("instant erase throw exception: " + e1.getMessage());
        }
    }

}
