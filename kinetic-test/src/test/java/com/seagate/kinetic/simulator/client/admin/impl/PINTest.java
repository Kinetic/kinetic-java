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
import kinetic.client.KineticException;

import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;

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

        List<ACL> acls = new ArrayList<ACL>();
        acls = setDefaultAcls();
        try {
            getAdminClient().setSecurity(acls, null, null, null, newErasePin);
            getAdminClient().setSecurity(acls, null, null, newErasePin, null);
        } catch (KineticException e) {
            fail("set pin throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testModifyOldPin_WithRightOldPin() {
        byte[] oldErasePin = toByteArray("123");
        byte[] newErasePin = toByteArray("456");

        List<ACL> acls = new ArrayList<ACL>();
        acls = setDefaultAcls();
        try {
            getAdminClient().setSecurity(acls, null, null, null, oldErasePin);
        } catch (KineticException e) {
            fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().setSecurity(acls, null, null, oldErasePin,
                    newErasePin);
        } catch (KineticException e) {
            fail("modify pin throw exception: " + e.getMessage());
        }

        // clean pin
        try {
            getAdminClient().setSecurity(acls, null, null, newErasePin, null);
        } catch (KineticException e) {
            fail("clean pin throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testModifyOldPin_WithWrongOldPin() {
        byte[] oldErasePin = toByteArray("123");
        byte[] oldIncorrectErasePin = toByteArray("456");
        byte[] newErasePin = toByteArray("789");

        List<ACL> acls = new ArrayList<ACL>();
        acls = setDefaultAcls();
        try {
            getAdminClient().setSecurity(acls, null, null, null, oldErasePin);
        } catch (KineticException e) {
            fail("set pin throw exception: " + e.getMessage());
        }

        try {
            getAdminClient().setSecurity(acls, null, null,
                    oldIncorrectErasePin, newErasePin);
            fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getMessage().contains("NOT_AUTHORIZED"));
        }

        // clean pin
        try {
            getAdminClient().setSecurity(acls, null, null, oldErasePin, null);
        } catch (KineticException e) {
            fail("clean pin throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testModifyOldPinWithRightPin_AfterRestartServer() {
        byte[] oldErasePin = toByteArray("123");
        byte[] newErasePin = toByteArray("456");

        List<ACL> acls = new ArrayList<ACL>();
        acls = setDefaultAcls();
        try {
            getAdminClient().setSecurity(acls, null, null, null, oldErasePin);
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
            getAdminClient().setSecurity(acls, null, null, oldErasePin,
                    newErasePin);
        } catch (KineticException e) {
            fail("modify pin throw exception: " + e.getMessage());
        }

        // clean pin
        try {
            getAdminClient().setSecurity(acls, null, null, newErasePin, null);
        } catch (KineticException e) {
            fail("clean pin throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testModifyOldPin_WithWrongPin_AfterRestartServer() {
        byte[] oldErasePin = toByteArray("123");
        byte[] oldIncorrectErasePin = toByteArray("456");
        byte[] newErasePin = toByteArray("789");

        List<ACL> acls = new ArrayList<ACL>();
        acls = setDefaultAcls();
        try {
            getAdminClient().setSecurity(acls, null, null, null, oldErasePin);
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
            getAdminClient().setSecurity(acls, null, null,
                    oldIncorrectErasePin, newErasePin);
            fail("should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getMessage().contains("NOT_AUTHORIZED"));
        }

        // clean pin
        try {
            getAdminClient().setSecurity(acls, null, null, oldErasePin, null);
        } catch (KineticException e) {
            fail("clean pin throw exception: " + e.getMessage());
        }
    }

}
