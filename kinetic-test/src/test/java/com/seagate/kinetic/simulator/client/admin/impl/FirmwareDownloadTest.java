package com.seagate.kinetic.simulator.client.admin.impl;

import static org.junit.Assert.assertTrue;
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
        byte[] newPin = "123".getBytes();

        try {
            getAdminClient().setup(null, newPin, 0, false);
            getAdminClient().firmwareDownload(newPin, firmwareInfo);
            getAdminClient().setup(newPin, null, 0, false);
        } catch (KineticException e) {
            fail("firmware download failed");
        }
    }

    @Test
    @SimulatorOnly
    public void testFirmwareDownload_WithIncorrectPin() throws KineticException {
        byte[] firmwareInfo = "firmware download info after pin set".getBytes();
        byte[] newPin = "123".getBytes();

        getAdminClient().setup(null, newPin, 0, false);

        byte[] incorrectPin = "456".getBytes();

        try {
            getAdminClient().firmwareDownload(incorrectPin, firmwareInfo);
            fail("firmware download failed");
        } catch (KineticException e) {
            assertTrue(e.getMessage().contains("Pin not match"));
            getAdminClient().setup(newPin, null, 0, false);
        }
    }
}
