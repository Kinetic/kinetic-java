package com.seagate.kinetic.simulator.client.admin.impl;

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.junit.Assert.*;
import kinetic.client.KineticException;

import com.seagate.kinetic.SimulatorOnly;

import org.junit.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

public class PinOpTest extends IntegrationTestCase {

    @Test
    @SimulatorOnly
    public void testLockDevice_withUnNullOrEmptyLockPin_LockEnable() {
        // set a lock pin
        byte[] lockPinB = toByteArray("lockpin");
        try {
            getAdminClient().setLockPin(null, lockPinB);
        } catch (KineticException e1) {
            fail("set lock pin throw exception: " + e1.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().getLog();
            fail("Should throw exception");
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.DEVICE_LOCKED));
        }

        // clean up: unlock device and then instant erase
        try {
            getAdminClient().unLockDevice(lockPinB);
        } catch (KineticException e) {
            fail("unlock device throw exception" + e.getMessage());
        }

        try {
            getAdminClient().instantErase(null);
        } catch (KineticException e) {
            fail("instant erase throw exception" + e.getMessage());
        }
    }

    @Test
    @SimulatorOnly
    public void testLockDevice_withUnNullOrEmptyLockPin_LockEnable_unLockDevice() {
        // set a lock pin
        byte[] lockPinB = toByteArray("lockpin");
        try {
            getAdminClient().setLockPin(null, lockPinB);
        } catch (KineticException e1) {
            fail("set lock pin throw exception: " + e1.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().unLockDevice(lockPinB);
        } catch (KineticException e1) {
            fail("unlock device throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().getLog();
        } catch (KineticException e) {
            fail("After unlock device, op throw exception: " + e.getMessage());
        }

        // clean up: erase lock pin
        try {
            getAdminClient().instantErase(null);
        } catch (KineticException e) {
            fail("instant erase throw exception" + e.getMessage());
        }
    }

    @Test
    @SimulatorOnly
    public void testLockDevice_withNullLockPin_LockDisable() {
        // set a lock pin
        try {
            getAdminClient().setLockPin(null, null);
        } catch (KineticException e1) {
            fail("set lock pin throw exception: " + e1.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().getLog();
        } catch (KineticException e) {
            fail("Lock disable, op throw exception: " + e.getMessage());
        }
    }

    @Test
    @SimulatorOnly
    public void testLockDevice_withEmptyLockPin_LockDisable() {
        // set a lock pin
        byte[] lockPinB = toByteArray("");
        try {
            getAdminClient().setLockPin(null, lockPinB);
        } catch (KineticException e1) {
            fail("set lock pin throw exception: " + e1.getMessage());
        }

        // restart server
        try {
            restartServer();
        } catch (Exception e1) {
            fail("restart server throw exception: " + e1.getMessage());
        }

        try {
            getAdminClient().getLog();
        } catch (KineticException e) {
            fail("Lock disable, op throw exception: " + e.getMessage());
        }
    }

}
