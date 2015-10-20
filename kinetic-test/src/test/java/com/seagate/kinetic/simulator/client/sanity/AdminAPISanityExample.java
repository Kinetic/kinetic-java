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
package com.seagate.kinetic.simulator.client.sanity;

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

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.admin.ACL;
import kinetic.admin.AdminClientConfiguration;
import kinetic.admin.Capacity;
import kinetic.admin.Configuration;
import kinetic.admin.Device;
import kinetic.admin.Domain;
import kinetic.admin.Interface;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.KineticAdminClientFactory;
import kinetic.admin.KineticLog;
import kinetic.admin.KineticLogType;
import kinetic.admin.Limits;
import kinetic.admin.Role;
import kinetic.admin.Statistics;
import kinetic.admin.Temperature;
import kinetic.admin.Utilization;
import kinetic.client.EntryNotFoundException;
import kinetic.client.KineticException;

import org.testng.Assert;

import com.seagate.kinetic.IntegrationTestLoggerFactory;

/**
 * Kinetic Administrator Client Basic API Test.
 * <p>
 * Kinetic admin API include:
 * <p>
 * setup(byte[] pin, byte[] setPin, long newClusterVersion, boolean secureErase)
 * <p>
 * firmwareDownload(byte[] pin, byte[] bytes)
 * <p>
 * getLog()
 * <p>
 * getLog(List<KineticLogType> listOfLogType)
 * <p>
 * setSecurity(List<ACL> acls)
 * <p>
 * 
 * @see KineticAdminClient
 * 
 */
public class AdminAPISanityExample {
    private static final Logger logger = IntegrationTestLoggerFactory
            .getLogger(AdminAPISanityExample.class.getName());

    private AdminClientConfiguration acconfig;
    private KineticAdminClient kac;
    private String host = System.getProperty("KINETIC_HOST", "127.0.0.1");
    private int port = Integer.parseInt(System.getProperty("KINEITC_PORT",
            "8123"));
    private int sslPort = Integer.parseInt(System.getProperty(
            "KINEITC_SSL_PORT", "8443"));
    private boolean useSsl = Boolean.parseBoolean(System.getProperty(
            "RUN_SSL_TEST", "true"));

    public AdminAPISanityExample() throws KineticException {
        acconfig = new AdminClientConfiguration();
        acconfig.setHost(host);
        acconfig.setPort(sslPort);
        if (!useSsl) {
            acconfig.setUseSsl(false);
            acconfig.setPort(port);
        }

        kac = KineticAdminClientFactory.createInstance(acconfig);
    }

    /**
     * Test setClusterVersion API, set cluster version for simulator/drive. The
     * result should be true.
     * <p>
     */
    public void test_setClusterVersion() {
        long newClusterVersion = 0;

        // modify cluster version.
        try {
            kac.setClusterVersion(newClusterVersion);

        } catch (KineticException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test instantErase API, erase data in simulator/drive. The result should
     * be true.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     */
    public void test_instanErase() {
        try {
            kac.instantErase("NULL".getBytes(Charset.forName("UTF-8")));
        } catch (KineticException e) {
            Assert.fail("instant erase throw exception: " + e.getMessage());
        }
    }

    /**
     * Test secureErase API, erase data in simulator/drive. The result should be
     * true.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     */
    public void test_secureErase() {
        try {
            kac.secureErase("NULL".getBytes(Charset.forName("UTF-8")));
        } catch (KineticException e) {
            Assert.fail("secure erase throw exception: " + e.getMessage());
        }
    }

    /**
     * Test get log API. The result should be success.
     * <p>
     */
    public void test_getLog() {
        KineticLog log;
        try {
            log = kac.getLog();

            assertTrue(log.getTemperature().size() > 0);
            assertTrue(log.getUtilization().size() > 0);
            assertTrue(log.getStatistics().size() > 0);
            assertTrue(log.getMessages().length > 0);

            assertTrue(log.getCapacity().getPortionFull() >= 0);
            assertTrue(log.getCapacity().getNominalCapacityInBytes() >= 0);

        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }
    }

    /**
     * Test getVendorSpecificDeviceLog API. The device name is drive's name.
     * <p>
     */
    public void test_getVendorSpecificDeviceLog_ForDrive() {
        byte[] name = null;
        // name supported by the simulator only
        String sname = "com.Seagate.Kinetic.HDD.Gen1";

        // name not supported by anyone
        String sname2 = "com.seagate.Kinetic.HDD.badName";

        byte[] name2 = null;
        name = toByteArray(sname);
        name2 = toByteArray(sname2);

        try {
            Device device = kac.getVendorSpecificDeviceLog(name);

            logger.info("got vendor specific log., name = " + sname
                    + ", log size=" + device.getValue().length);
        } catch (EntryNotFoundException enfe) {
            // could happen if this the service is not simulator
            logger.info("device log name not found for name: " + sname);
        } catch (KineticException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        try {
            kac.getVendorSpecificDeviceLog(name2);

            Assert.fail("should have caught EntryNotFoundException");
        } catch (EntryNotFoundException enfe) {
            // could happen if this the service is not simulator
            logger.info("device log name not found for name: " + sname2);
        } catch (KineticException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            Assert.fail("should have caught EntryNotFoundException");
        }

    }

    /**
     * Test set security API. The result should be success. If failed, throw
     * KineticException.
     * <p>
     */
    public void test_setAcl() {
        List<ACL> acls = new ArrayList<ACL>();

        List<Role> roles = new ArrayList<Role>();
        roles.add(Role.DELETE);
        roles.add(Role.GETLOG);
        roles.add(Role.READ);
        roles.add(Role.RANGE);
        roles.add(Role.SECURITY);
        roles.add(Role.SETUP);
        roles.add(Role.WRITE);
        roles.add(Role.P2POP);

        Domain domain = new Domain();
        domain.setRoles(roles);

        List<Domain> domains = new ArrayList<Domain>();
        domains.add(domain);

        ACL acl1 = new ACL();
        acl1.setDomains(domains);
        acl1.setUserId(1);
        acl1.setKey("asdfasdf");

        acls.add(acl1);

        // all pins set the same
        try {
            kac.setAcl(acls);
        } catch (KineticException e) {
            Assert.fail("Set Security throw exception" + e.getMessage());
        }
    }

    /**
     * Test set security, set erase pin.
     * <p>
     */
    public void test_setErasePin() {
        String erasePin = "erasePin";
        byte[] erasePinB = toByteArray(erasePin);

        try {
            kac.setErasePin("".getBytes(Charset.forName("UTF-8")), erasePinB);
        } catch (KineticException e) {
            Assert.fail("Set erase pin throw exception" + e.getMessage());
        }

        // clean pin
        try {
            kac.secureErase(erasePinB);
        } catch (KineticException e) {
            Assert.fail("secure erase throw exception" + e.getMessage());
        }
    }

    /**
     * Test set security, set lock pin.
     * <p>
     */
    public void test_setLockPin() {
        String lockPin = "lockPin";
        byte[] lockPinB = toByteArray(lockPin);

        try {
            kac.setLockPin("".getBytes(Charset.forName("UTF-8")), lockPinB);
        } catch (KineticException e) {
            Assert.fail("Set erase pin throw exception" + e.getMessage());
        }

        // clean pin
        try {
            kac.secureErase("123".getBytes(Charset.forName("UTF-8")));
        } catch (KineticException e) {
            Assert.fail("secure erase throw exception" + e.getMessage());
        }
    }

    /**
     * Test lock device with correct lock pin, should lock the device.
     * <p>
     */
    public void test_lockDevice() {
        // set a lock pin
        byte[] lockPinB = toByteArray("lockpin");
        try {
            kac.setLockPin("".getBytes(Charset.forName("UTF-8")), lockPinB);
        } catch (KineticException e1) {
            Assert.fail("set lock pin throw exception: " + e1.getMessage());
        }

        try {
            kac.lockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("Lock device with correct pin failed: "
                    + e.getMessage());
        }

        // unlock device
        try {
            kac.unLockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("unLock device with correct pin failed: "
                    + e.getMessage());
        }

        // clean pin
        try {
            kac.secureErase("NULL".getBytes(Charset.forName("UTF-8")));
        } catch (KineticException e) {
            Assert.fail("secure erase throw exception" + e.getMessage());
        }
    }

    /**
     * Test unlock device with correct unlock pin, should unlock the device.
     * <p>
     */
    public void test_unLockDevice() {
        // set a lock pin
        byte[] lockPinB = toByteArray("lockpin");
        try {
            kac.setLockPin("".getBytes(Charset.forName("UTF-8")), lockPinB);
        } catch (KineticException e1) {
            Assert.fail("set lock pin throw exception: " + e1.getMessage());
        }

        try {
            kac.lockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("Lock device with correct pin failed: "
                    + e.getMessage());
        }

        try {
            kac.unLockDevice(lockPinB);
        } catch (KineticException e) {
            Assert.fail("unLock device with correct pin failed: "
                    + e.getMessage());
        }

        // clean pin
        try {
            kac.secureErase("NULL".getBytes(Charset.forName("UTF-8")));
        } catch (KineticException e) {
            Assert.fail("secure erase throw exception" + e.getMessage());
        }
    }

    /**
     * Test get log API. Check every log field value whether valid.
     * <p>
     */
    public void test_getLog_withLogType() {
        List<KineticLogType> listOfLogType = new ArrayList<KineticLogType>();
        listOfLogType.add(KineticLogType.CAPACITIES);
        listOfLogType.add(KineticLogType.CONFIGURATION);
        listOfLogType.add(KineticLogType.MESSAGES);
        listOfLogType.add(KineticLogType.STATISTICS);
        listOfLogType.add(KineticLogType.TEMPERATURES);
        listOfLogType.add(KineticLogType.UTILIZATIONS);
        listOfLogType.add(KineticLogType.LIMITS);

        KineticLog log = null;
        try {
            log = kac.getLog(listOfLogType);
        } catch (KineticException e) {
            Assert.fail("getLog throw exception: " + e.getMessage());
        }

        Capacity capacity;
        try {
            capacity = log.getCapacity();
            assertTrue(capacity.getPortionFull() >= 0);
            assertTrue(capacity.getNominalCapacityInBytes() >= 0);
        } catch (KineticException e) {
            Assert.fail("get capacity throw exception: " + e.getMessage());
        }

        Configuration configuration;
        try {
            configuration = log.getConfiguration();
            assertTrue(configuration.getCompilationDate().length() > 0);
            assertTrue(configuration.getModel().length() > 0);
            assertTrue(configuration.getPort() >= 0);
            assertTrue(configuration.getTlsPort() >= 0);
            assertTrue(configuration.getSerialNumber().length() > 0);
            assertTrue(configuration.getWorldWideName().length() > 0);
            assertTrue(configuration.getSourceHash().length() > 0);
            assertTrue(configuration.getVendor().length() > 0);
            assertTrue(configuration.getVersion().length() > 0);

            List<Interface> interfaceOfList = configuration.getInterfaces();
            for (Interface interfaces : interfaceOfList) {
                assertTrue(interfaces.getName().length() > 0);
            }
        } catch (KineticException e) {
            Assert.fail("get configuration throw exception: " + e.getMessage());
        }

        byte[] messages;
        try {
            messages = log.getMessages();
            assertTrue(messages.length > 0);
        } catch (KineticException e) {
            Assert.fail("get message throw exception: " + e.getMessage());
        }

        List<Statistics> statisticsOfList;
        try {
            statisticsOfList = log.getStatistics();
            for (Statistics statistics : statisticsOfList) {
                assertTrue(statistics.getBytes() >= 0);
                assertTrue(statistics.getCount() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get statistics throw exception: " + e.getMessage());
        }

        List<Temperature> tempOfList;
        try {
            tempOfList = log.getTemperature();
            for (Temperature temperature : tempOfList) {
                assertTrue(temperature.getCurrent() >= 0);
                assertTrue(temperature.getMax() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get temperature throw exception: " + e.getMessage());
        }

        List<Utilization> utilOfList;
        try {
            utilOfList = log.getUtilization();
            for (Utilization util : utilOfList) {
                assertTrue(util.getUtility() >= 0);
            }
        } catch (KineticException e) {
            Assert.fail("get utilization throw exception: " + e.getMessage());
        }

        KineticLogType[] logTypes;
        try {
            logTypes = log.getContainedLogTypes();
            assertEquals(listOfLogType.size(), logTypes.length);

            for (int i = 0; i < logTypes.length; i++) {
                assertTrue(listOfLogType.contains(logTypes[i]));
            }

        } catch (KineticException e) {
            Assert.fail("get containedLogTypes throw exception: "
                    + e.getMessage());
        }

        Limits limits;
        try {
            limits = log.getLimits();
            assertTrue(limits.getMaxKeySize() >= 0);
            assertTrue(limits.getMaxValueSize() >= 0);
            assertTrue(limits.getMaxVersionSize() >= 0);
            assertTrue(limits.getMaxKeyRangeCount() >= 0);
        } catch (KineticException e) {
            Assert.fail("get limits throw exception: " + e.getMessage());
        }
    }

    public void close() throws KineticException {
        kac.close();
    }

    public static void main(String[] args) {
        AdminAPISanityExample example = null;
        try {
            example = new AdminAPISanityExample();
            example.test_setClusterVersion();
            example.test_getLog();
            example.test_getLog_withLogType();
            example.test_getVendorSpecificDeviceLog_ForDrive();
            example.test_setAcl();
        } catch (KineticException e) {
            logger.severe(e.getMessage());
        } finally {
            try {
                example.close();
            } catch (KineticException e) {
                logger.severe(e.getMessage());
            }
        }
    }
}
