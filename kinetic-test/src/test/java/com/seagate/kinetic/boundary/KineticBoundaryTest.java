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
package com.seagate.kinetic.boundary;

import static com.seagate.kinetic.KineticAssertions.assertEntryEquals;
import static com.seagate.kinetic.KineticAssertions.assertKeyNotFound;
import static com.seagate.kinetic.KineticTestHelpers.cleanData;
import static com.seagate.kinetic.KineticTestHelpers.cleanNextData;
import static com.seagate.kinetic.KineticTestHelpers.cleanPreviousData;
import static com.seagate.kinetic.KineticTestHelpers.int32;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import kinetic.client.Entry;
import kinetic.client.EntryMetadata;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.client.VersionMismatchException;
import kinetic.client.advanced.AdvancedKineticClient;
import kinetic.simulator.SimulatorConfiguration;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;
import com.seagate.kinetic.client.internal.DefaultKineticClient;
import com.seagate.kinetic.proto.Kinetic;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Command.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

/**
 * Kinetic Client API Boundary Test.
 * <p>
 * Boundary test against basic API.
 * <p>
 *
 * @see KineticClient
 *
 */
@Test(groups = { "simulator", "drive" })
public class KineticBoundaryTest extends IntegrationTestCase {

    private static final Logger logger = IntegrationTestLoggerFactory
            .getLogger(KineticBoundaryTest.class.getName());

    /**
     * Put a different version with entry db version existed in simulator/drive.
     * Two versions have different length, can not match each other. The test
     * result should be thrown KineticException.
     *
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPut_Throws_ForVersionWithDifferentLength(String clientName) {
        byte[] key = toByteArray("key00000000000");
        byte[] newVersionInit = int32(0);
        byte[] valueInit = toByteArray("value00000000000");

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("clean data throw exception! " + e.getMessage());
        }

        EntryMetadata entryMetadata = new EntryMetadata();
        Entry versionedInit = new Entry(key, valueInit, entryMetadata);
        try {
            getClient(clientName).put(versionedInit, newVersionInit);
        } catch (KineticException e) {
            Assert.fail("put entry throw exception. " + e.getMessage());
        }

        try {
            byte[] newVersion = int32(1);
            byte[] value = toByteArray("value00000000001");
            byte[] dbVersion = toByteArray(new String(getClient(clientName)
                    .get(key).getEntryMetadata().getVersion()) + 10);

            EntryMetadata entryMetadata1 = new EntryMetadata();
            entryMetadata1.setVersion(dbVersion);
            Entry versioned = new Entry(key, value, entryMetadata1);

            getClient(clientName).put(versioned, newVersion);
            Assert.fail("Should have thrown");
        } catch (KineticException e1) {
            Entry vGet = null;
            try {
                vGet = getClient(clientName).get(key);
            } catch (KineticException e) {
                Assert.fail("get entry throw exception. " + e.getMessage());
            }
            assertEntryEquals(key, valueInit, newVersionInit, vGet);
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("clean data throw exception! " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Put a different version with entry db version existed in simulator/drive.
     * Two versions have different value, can not match each other. The test
     * result should be thrown KineticException.
     *
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPut_Throws_ForWrongVersion(String clientName) {
        byte[] key = toByteArray("key00000000000");
        byte[] newVersionInit = int32(0);
        byte[] valueInit = toByteArray("value00000000000");

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("delete key throw exceptions. " + e.getMessage());
        }

        EntryMetadata entryMetadata = new EntryMetadata();
        Entry versionedInit = new Entry(key, valueInit, entryMetadata);
        try {
            getClient(clientName).put(versionedInit, newVersionInit);
        } catch (KineticException e2) {
            Assert.fail("put date throw exception. " + e2.getMessage());
        }

        try {
            byte[] newVersion = int32(1);
            byte[] value = toByteArray("value00000000001");
            byte[] dbVersion = int32(1);

            EntryMetadata entryMetadata1 = new EntryMetadata();
            entryMetadata1.setVersion(dbVersion);
            Entry versioned = new Entry(key, value, entryMetadata1);

            getClient(clientName).put(versioned, newVersion);
            Assert.fail("Should have thrown");
        } catch (VersionMismatchException e1) {
            Entry vGet = null;
            try {
                vGet = getClient(clientName).get(key);
            } catch (KineticException e) {
                Assert.fail("get data throw exception. " + e.getMessage());
            }
            assertEntryEquals(key, valueInit, newVersionInit, vGet);
        } catch (KineticException e) {
            Assert.fail("put entry throw exception. " + e.getMessage());
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("delete key throw exceptions. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Put a new key in the store (insert, not update) while providing a
     * non-empty dbVersion. This should cause an exception and return a status
     * of VERSION_MISMATCH
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPut_Throws_WhenVersionIsSetWithNewKey(String clientName) {
        byte[] key = toByteArray("key00000000000");
        byte[] newVersion = toByteArray("ExistingVersion");

        byte[] value = toByteArray("value00000000000");

        // This is a new KeyValue pair, there shouldn't be this dbVersion
        byte[] invalidDbVersion = int32(10);

        EntryMetadata entryMetadata = new EntryMetadata();
        entryMetadata.setVersion(invalidDbVersion);

        Entry newEntryWithInvalidDbVersion = new Entry(key, value,
                entryMetadata);

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("delete key throw exceptions. " + e.getMessage());
        }

        try {
            getClient(clientName).put(newEntryWithInvalidDbVersion, newVersion);
            Assert.fail("Should have thrown");
        } catch (VersionMismatchException e1) {
            logger.info("caught expected VersionMismatchException exception.");
        } catch (KineticException ke) {
            Assert.fail("Should have caught VersionMismatchException.");
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("delete key throw exceptions. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Put a null entry to simulator/drive. The test result should be thrown
     * KineticException.
     * <p>
     *
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPut_Throws_WhenPuttingNull(String clientName) {
        try {
            getClient(clientName).put(null, null);
            Assert.fail("put null should fail");
        } catch (KineticException e) {
            assertNull(e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Put a entry with value is empty. The value get from simulator/drive
     * should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPutAllowsEmptyValues(String clientName) {
        byte[] key = { 0x3 };

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("Clean key throw exceptions." + e.getMessage());
        }

        Entry entry = new Entry(key, new byte[0]);
        try {
            getClient(clientName).put(entry, new byte[] { 2 });
            assertArrayEquals(new byte[0], getClient(clientName).get(key)
                    .getValue());
        } catch (KineticException e1) {
            Assert.fail("throw exception. " + e1.getMessage());
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("Clean key throw exceptions." + e.getMessage());
        }
        logger.info(this.testEndInfo());
    }

    /**
     * Put a entry with value is null. The value get from simulator/drive should
     * be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPutAllowsNullValues(String clientName) {
        byte[] key = { 0x3 };

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("Clean key throw exceptions." + e.getMessage());
        }

        Entry entry = new Entry(key, null);
        try {
            getClient(clientName).put(entry, new byte[] { 2 });
            assertArrayEquals(new byte[0], getClient(clientName).get(key)
                    .getValue());
        } catch (KineticException e1) {
            Assert.fail("throw exception. " + e1.getMessage());
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("Clean key throw exceptions." + e.getMessage());
        }
        logger.info(this.testEndInfo());
    }

    /**
     * Put a entry with value is space. The value get from simulator/drive
     * should be space.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPutAllowsSpaceValues(String clientName) {
        byte[] key = { 0x3 };

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("Clean key throw exceptions." + e.getMessage());
        }

        byte[] value = toByteArray(" ");
        Entry entry = new Entry(key, value);
        try {
            getClient(clientName).put(entry, new byte[] { 2 });
            assertArrayEquals(value, getClient(clientName).get(key).getValue());
        } catch (KineticException e1) {
            Assert.fail("throw exception. " + e1.getMessage());
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("Clean key throw exceptions." + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Put a entry with a value which is too long.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    // TODO open
    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testPut_Throws_ForValueTooLong(String clientName) {
        byte[] key = toByteArray("key00000000000");
        byte[] newVersion = int32(0);
        // The max size should be 1024*2
        byte[] longValue = new byte[1024 * 1024 + 1];
        for (int i = 0; i < longValue.length; ++i) {
            longValue[i] = 'a';
        }
        EntryMetadata entryMetadata = new EntryMetadata();
        Entry entry = new Entry(key, longValue, entryMetadata);

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("clean data throw exception! " + e.getMessage());
        }

        try {
            getClient(clientName).put(entry, newVersion);
            Assert.fail("Should have thrown");
        } catch (KineticException e) {
            // TODO: The simulator returns INTERNAL_ERROR, but the drive returns
            // an IO error
            // ... We would rather have this test than remove it, and it is
            // undesirable for it to fail when run
            // ... against one target. So, we tolerate both cases for now.
            StatusCode code = e.getResponseMessage().getCommand().getStatus()
                    .getCode();

            assertEquals(StatusCode.INVALID_REQUEST, code);

            try {
                getClient(clientName).deleteForced(key);
            } catch (KineticException e1) {
                Assert.fail("clean data throw exception! " + e1.getMessage());
            }
        }
    }

    /**
     * Put a entry when a user does not have permissions to do so.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testPut_Throws_ForUserWithoutWritePerms(String clientName) {
        int clientId = 2;
        String clientKeyString = "testclientwithoutputkey";

        // Give the client a "Read" permission but no Write
        try {
            createClientAclWithRoles(
                    clientName,
                    clientId,
                    clientKeyString,
                    Collections
                            .singletonList(Kinetic.Command.Security.ACL.Permission.READ));
        } catch (KineticException e1) {
            Assert.fail("create client acl with roles throw exception. "
                    + e1.getMessage());
        }

        KineticClient clientWithoutPutPermission = null;
        try {
            clientWithoutPutPermission = KineticClientFactory
                    .createInstance(getClientConfig(clientId, clientKeyString));
        } catch (KineticException e1) {
            Assert.fail("create client without put role throw exception. "
                    + e1.getMessage());
        }

        byte[] key = toByteArray("key00000000000");
        byte[] newVersion = int32(0);
        byte[] value = toByteArray("value00000000000");

        EntryMetadata entryMetadata = new EntryMetadata();
        Entry entry = new Entry(key, value, entryMetadata);

        try {
            clientWithoutPutPermission.put(entry, newVersion);
            Assert.fail("Should have thrown");
        } catch (KineticException e) {
            assertEquals(
                    "Kinetic Command Exception: NOT_AUTHORIZED: permission denied",
                    e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Get the key does not exist in simulator/drive, the result should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGet_ReturnsNull_ForNonExistingKey(String clientName) {
        byte[] key = toByteArray("key00000000000");

        try {
            assertKeyNotFound(getClient(clientName), key);
        } catch (KineticException e) {
            logger.info("key exists! " + e.getMessage());
            try {
                getClient(clientName).deleteForced(key);
            } catch (KineticException e1) {
                Assert.fail("Clean key throw exception. " + e1.getMessage());
            }
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("Delete non existing key throw exception, the expected result: return null. "
                    + e.getMessage());
        }

        try {
            assertKeyNotFound(getClient(clientName), key);
        } catch (KineticException e) {
            Assert.fail("key exists! " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Get the key's value is null from simulator/drive, the result should be
     * thrown KineticException.
     * <p>
     *
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGet_Throws_ForNullKey(String clientName) {
        try {
            getClient(clientName).get(null);
            Assert.fail("the key is null, get failed");
        } catch (KineticException e) {
            assertNull(e.getMessage());
        }
        logger.info(this.testEndInfo());
    }

    /**
     * Delete a entry does not exist in simulator/drive, the result should be
     * false. Verify get key result is null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDelete_ReturnsFalse_ForNonExistingKey(String clientName) {
        byte[] key = toByteArray("key00000000000");
        byte[] valueInit = toByteArray("value00000000000");
        Entry versionedInit;

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("Clean key throw exceptions." + e.getMessage());
        }

        EntryMetadata entryMetadata = new EntryMetadata();
        versionedInit = new Entry(key, valueInit, entryMetadata);
        try {
            assertFalse(getClient(clientName).delete(versionedInit));
        } catch (KineticException e) {
            Assert.fail("Delete non existing key throw exception, expected result is return false. "
                    + e.getMessage());
        }
        try {
            assertKeyNotFound(getClient(clientName), key);
        } catch (KineticException e) {
            Assert.fail("key wasn't deleted. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Delete a entry has same key but different length version with entry
     * existed in simulator/drive, the result should be thrown KineticException.
     * Verify get key result is the existed entry.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDelete_Throws_ForWrongLengthVersion(String clientName) {
        byte[] key = toByteArray("key00000000000");
        byte[] newVersionInit = int32(0);
        byte[] valueInit = toByteArray("value00000000000");
        Entry versionedInit;

        EntryMetadata entryMetadata = new EntryMetadata();
        versionedInit = new Entry(key, valueInit, entryMetadata);

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e1) {
            Assert.fail("Clean data throw exception. " + e1.getMessage());
        }

        try {
            getClient(clientName).put(versionedInit, newVersionInit);

            byte[] value = toByteArray("value00000000001");
            byte[] dbVersion = toByteArray(new String(getClient(clientName)
                    .get(key).getEntryMetadata().getVersion()) + 10);

            entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(dbVersion);
            Entry versioned = new Entry(key, value, entryMetadata);

            getClient(clientName).delete(versioned);
            Assert.fail("Should have thrown");
        } catch (KineticException e) {
            Entry vGet = null;
            try {
                vGet = getClient(clientName).get(key);
            } catch (KineticException e1) {
                Assert.fail("get key throw exception. " + e1.getMessage());
            }
            assertEntryEquals(key, valueInit, newVersionInit, vGet);
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e1) {
            Assert.fail("Clean data throw exception. " + e1.getMessage());
        }
        logger.info(this.testEndInfo());
    }

    /**
     * Delete a entry has same key but different version with entry existed in
     * simulator/drive, the result should be thrown KineticException. Verify get
     * key result is the existed entry.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDelete_Throws_ForWrongVersion(String clientName) {
        byte[] key = toByteArray("key00000000000");
        byte[] newVersionInit = int32(0);
        byte[] valueInit = toByteArray("value00000000000");
        Entry versionedInit;

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e1) {
            Assert.fail("Clean data throw exception. " + e1.getMessage());
        }

        EntryMetadata entryMetadata = new EntryMetadata();
        versionedInit = new Entry(key, valueInit, entryMetadata);
        try {
            getClient(clientName).put(versionedInit, newVersionInit);
        } catch (KineticException e2) {
            Assert.fail("put data throw exception. " + e2.getMessage());
        }
        try {
            byte[] value = toByteArray("value00000000001");
            byte[] dbVersion = int32(1);

            entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(dbVersion);
            Entry versioned = new Entry(key, value, entryMetadata);

            getClient(clientName).delete(versioned);
            Assert.fail("Should have thrown");
        } catch (KineticException e) {
            Entry vGet = null;
            try {
                vGet = getClient(clientName).get(key);
            } catch (KineticException e1) {
                Assert.fail("get data throw exception. " + e1.getMessage());
            }
            assertEntryEquals(key, valueInit, newVersionInit, vGet);
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e1) {
            Assert.fail("Clean data throw exception. " + e1.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Delete a entry when a user does not have permissions to do so.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDelete_Throws_ForUserWithoutDeletePerms(String clientName) {
        int clientId = 2;
        String clientKeyString = "testclientwithoutdeletekey";

        // Give the client a "Write" permission but no delete
        try {
            createClientAclWithRoles(
                    clientName,
                    clientId,
                    clientKeyString,
                    Collections
                            .singletonList(Kinetic.Command.Security.ACL.Permission.WRITE));
        } catch (KineticException e1) {
            Assert.fail("Create client acl with roles throw exception. "
                    + e1.getMessage());
        }

        KineticClient clientWithoutDelete = null;
        try {
            clientWithoutDelete = KineticClientFactory
                    .createInstance(getClientConfig(clientId, clientKeyString));
        } catch (KineticException e1) {
            Assert.fail("create client without delete right throw exception. "
                    + e1.getMessage());
        }

        byte[] key = toByteArray("key00000000000");
        byte[] newVersion = int32(0);
        byte[] value = toByteArray("value00000000000");
        Entry entry;

        EntryMetadata entryMetadata = new EntryMetadata();
        entry = new Entry(key, value, entryMetadata);

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e1) {
            Assert.fail("Clean data throw exception. " + e1.getMessage());
        }

        Entry putResultEntry = null;
        try {
            putResultEntry = clientWithoutDelete.put(entry, newVersion);
        } catch (KineticException e2) {
            Assert.fail("Put Entry throw exception. " + e2.getMessage());
        }

        try {
            clientWithoutDelete.delete(putResultEntry);
            Assert.fail("Should have thrown");
        } catch (KineticException e) {
            assertEquals(
                    "Kinetic Command Exception: NOT_AUTHORIZED: permission denied",
                    e.getMessage());
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e1) {
            Assert.fail("Clean data throw exception. " + e1.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Delete a null entry, the result should be thrown KineticException.
     * <p>
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testDelete_Throws_ForNullKey(String clientName) {
        try {
            getClient(clientName).delete(null);
            Assert.fail("Should have thrown");
        } catch (KineticException e) {
            assertTrue(e.getLocalizedMessage().contains("NullPointerException"));
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetNext entry which has not existed in simulator/drive, the result should
     * be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_ReturnsNull_ForNonExistKey(String clientName) {
        try {
            getClient(clientName).deleteForced(toByteArray("foobarbaz"));
        } catch (KineticException e) {
            Assert.fail("Clean data throw exception: " + e.getMessage());
        }

        try {
            Entry v = getClient(clientName).getNext(toByteArray("foobarbaz"));
            assertNull(v);
        } catch (KineticException e) {
            Assert.fail("Get next data throw exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetNext entry which is null, the result should be thrown
     * KineticException.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_Throws_ForNullKey(String clientName) {
        try {
            getClient(clientName).getNext(null);
            Assert.fail("getNext should throw for null key");
        } catch (KineticException e) {
            assertNull(e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetNext entry which is smaller than the first entry in simulator/drive,
     * the result should be the first entry in simulator/drive.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_ForKeySmallerThanTheFirstKeyInDB(String clientName) {
        Entry entry0 = null;
        byte[] key = toByteArray("key00");

        try {
            cleanData(key, toByteArray("key007"), getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("clean data throw exception. " + e2.getMessage());
        }

        try {
            entry0 = buildAndPutEntry("key005", "value005",
                    getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            buildAndPutEntry("key007", "value007", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put entry throw exception. "
                    + e.getMessage());
        }

        Entry entryNext = null;
        try {
            entryNext = getClient(clientName).getNext(key);
        } catch (KineticException e1) {
            Assert.fail("get next throw exception. " + e1.getMessage());
        }

        assertEntryEquals(entry0, entryNext);

        try {
            cleanData(key, toByteArray("key007"), getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean key throw exception. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetNext entry which is the first entry in simulator/drive, the result
     * should be the second entry in simulator/drive.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_ForKeyIsTheFirstKeyInDB(String clientName) {

        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean key throw exception. " + e.getMessage());
        }

        Entry entry0 = null;
        Entry entry1 = null;
        try {
            entry0 = buildAndPutEntry("key005", "value005",
                    getClient(clientName));
            entry1 = buildAndPutEntry("key006", "value006",
                    getClient(clientName));
            buildAndPutEntry("key007", "value007", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put entry throw exception. "
                    + e.getMessage());
        }

        byte[] key = entry0.getKey();

        Entry entryNext;
        try {
            entryNext = getClient(clientName).getNext(key);
            assertEntryEquals(entry1, entryNext);
        } catch (KineticException e) {
            Assert.fail("get next throw exception. " + e.getMessage());
        }

        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean key throw exception. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetNext entry which is the second entry in simulator/drive, the result
     * should be the third entry in simulator/drive.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_ForKeyIsTheSecondKeyInDB(String clientName) {
        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean key throw exception. " + e.getMessage());
        }

        Entry entry1 = null;
        Entry entry2 = null;
        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            entry1 = buildAndPutEntry("key006", "value006",
                    getClient(clientName));
            entry2 = buildAndPutEntry("key007", "value007",
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put entry throw exception! "
                    + e.getMessage());
        }

        byte[] key = entry1.getKey();

        Entry entryNext;
        try {
            entryNext = getClient(clientName).getNext(key);
            assertEntryEquals(entry2, entryNext);
        } catch (KineticException e1) {
            Assert.fail("get next throw exception. " + e1.getMessage());
        }

        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean key throw exception. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetNext entry which is the last entry in simulator/drive, the result
     * should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_ReturnNull_ForKeyIsTheLastKeyInDB(String clientName) {
        try {
            cleanNextData(toByteArray("key007"), getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean key throw exception. " + e.getMessage());
        }

        Entry entry2 = null;
        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            entry2 = buildAndPutEntry("key007", "value007",
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put entry throw exception! "
                    + e.getMessage());
        }

        byte[] key = entry2.getKey();

        try {
            Entry entryNext = getClient(clientName).getNext(key);
            assertNull(entryNext);
        } catch (KineticException e1) {
            Assert.fail("get next throw exception. " + e1.getMessage());
        }

        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }
        logger.info(this.testEndInfo());
    }

    /**
     * GetNext entry which is bigger than the last entry in simulator/drive, the
     * result should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_ReturnNull_ForKeyBiggerThanTheLastKeyInDB(
            String clientName) {
        byte[] key = toByteArray("key09");

        try {
            cleanNextData(key, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }

        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            buildAndPutEntry("key007", "value007", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put entry throw exception! "
                    + e.getMessage());
        }

        try {
            Entry entryNext = getClient(clientName).getNext(key);
            assertNull(entryNext);
        } catch (KineticException e1) {
            Assert.fail("get next throw exception. " + e1.getMessage());
        }

        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }
        logger.info(this.testEndInfo());
    }

    /**
     * Test that not found is returned when a user tries to call getNext when
     * the would-be-returned key is not visible to that user.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_ReturnUnauthorizedStatus_ForNextKeyNotVisible(
            String clientName) throws UnsupportedEncodingException,
            KineticException {
        try {
            cleanData(toByteArray("a"), toByteArray("c"), getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }

        List<Entry> visibleEntries = Lists.newArrayList();
        visibleEntries.add(new Entry(toByteArray("a"), toByteArray("valuea")));
        visibleEntries.add(new Entry(toByteArray("c"), toByteArray("valuec")));

        List<Entry> notVisibleEntries = Lists.newArrayList();
        notVisibleEntries.add(new Entry(toByteArray("b"), toByteArray("b")));

        for (Entry entry : visibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        for (Entry entry : notVisibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        KineticClient clientWithLimitedRead = createClientWithLimitedVisibilityAndAddEntriesToStore(
                clientName, visibleEntries, notVisibleEntries);

        try {
            // This client should not be able to read 'b', but should be able to
            // read 'a' and 'c'
            clientWithLimitedRead.getNext(visibleEntries.get(0).getKey());
            Assert.fail("Error Expected: This test fails against the simulator but passes against the drive due to a simulator bug");
        } catch (KineticException e) {
            assertEquals(e.getMessage(),
                    "Kinetic Command Exception: NOT_AUTHORIZED: permission denied");
        }

        for (Entry entry : visibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        for (Entry entry : notVisibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test that getNext succeeds when the user doesn't have visibility for the
     * key passed but does have visibility for the next key.
     *
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_Succeeds_ForGivenKeyNotVisible(String clientName)
            throws UnsupportedEncodingException, KineticException {
        try {
            cleanData(toByteArray("a"), toByteArray("c"), getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }

        List<Entry> visibleEntries = Lists.newArrayList();
        visibleEntries.add(new Entry(toByteArray("a"), toByteArray("valuea")));
        visibleEntries.add(new Entry(toByteArray("c"), toByteArray("valuec")));

        List<Entry> notVisibleEntries = Lists.newArrayList();
        notVisibleEntries.add(new Entry(toByteArray("b"), toByteArray("b")));

        for (Entry entry : visibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        for (Entry entry : notVisibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        KineticClient clientWithLimitRead = createClientWithLimitedVisibilityAndAddEntriesToStore(
                clientName, visibleEntries, notVisibleEntries);
        Entry getNextResult = clientWithLimitRead.getNext(notVisibleEntries
                .get(0).getKey());
        Entry expectedEntry = visibleEntries.get(1);
        assertArrayEquals(expectedEntry.getKey(), getNextResult.getKey());
        assertArrayEquals(expectedEntry.getValue(), getNextResult.getValue());

        for (Entry entry : visibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        for (Entry entry : notVisibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetNext entry with empty in simulator/drive, the result should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetNext_ReturnNull_ForNoDataInDB(String clientName) {
        byte[] key = toByteArray("key09");
        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("clean key throw exception! " + e.getMessage());
        }

        try {
            Entry entryNext = getClient(clientName).getNext(key);
            assertNull(entryNext);
        } catch (KineticException e) {
            Assert.fail("get next throw exception. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetPrevious entry which has not existed in simulator/drive, the result
     * should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ReturnsNull_ForNonExistingKey(String clientName) {
        try {
            getClient(clientName).deleteForced(toByteArray("foobaraasdf"));
        } catch (KineticException e) {
            Assert.fail("clean key throw exception! " + e.getMessage());
        }

        try {
            Entry v = getClient(clientName).getPrevious(
                    toByteArray("foobaraasdf"));
            assertNull(v);
        } catch (KineticException e) {
            Assert.fail("get next throw exception. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetPrevious entry which is the first entry in simulator/drive, the result
     * should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ReturnsNull_ForFirstKey(String clientName) {
        byte[] key0 = toByteArray("key00000000000");
        byte[] newVersion0 = int32(0);
        byte[] value0 = toByteArray("value00000000000");
        EntryMetadata entryMetadata = new EntryMetadata();
        Entry versioned0 = new Entry(key0, value0, entryMetadata);

        try {
            cleanPreviousData(key0, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean key throw exception! " + e.getMessage());
        }

        try {
            getClient(clientName).put(versioned0, newVersion0);
            assertNull(getClient(clientName).getPrevious(key0));
        } catch (KineticException e1) {
            Assert.fail("throw exception! " + e1.getMessage());
        }

        try {
            getClient(clientName).deleteForced(key0);
        } catch (KineticException e) {
            Assert.fail("clean key throw exception! " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetPrevious entry is null in simulator/drive, the result should be thrown
     * KineticException.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_Throws_ForNullKey(String clientName) {
        try {
            getClient(clientName).getPrevious(null);
            Assert.fail("Should have thrown");
        } catch (KineticException e) {
            assertNull(e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetPrevious entry which is smaller than the first entry in
     * simulator/drive, the result should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ReturnNull_ForKeySmallerThanTheFirstKeyInDB(
            String clientName) {
        byte[] key = toByteArray("key00");

        try {
            cleanPreviousData(key, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("clean previous data throw exception! "
                    + e2.getMessage());
        }

        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            buildAndPutEntry("key007", "value007", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put entry throw exception! "
                    + e.getMessage());
        }

        try {
            Entry entryPrevious = getClient(clientName).getPrevious(key);
            assertNull(entryPrevious);
        } catch (KineticException e1) {
            Assert.fail("get previous throw exception! " + e1.getMessage());
        }

        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetPrevious entry which is the first entry in simulator/drive, the result
     * should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ForKeyIsTheFirstKeyInDB(String clientName) {
        try {
            cleanPreviousData(toByteArray("key005"), getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("clean previous data throw exception! "
                    + e2.getMessage());
        }

        Entry entry0 = null;
        try {
            entry0 = buildAndPutEntry("key005", "value005",
                    getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            buildAndPutEntry("key007", "value007", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put entry throw exception! "
                    + e.getMessage());
        }
        byte[] key = entry0.getKey();

        Entry entryPrevious;
        try {
            entryPrevious = getClient(clientName).getPrevious(key);
            assertNull(entryPrevious);
        } catch (KineticException e1) {
            Assert.fail("get previous throw exception! " + e1.getMessage());
        }

        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }
        logger.info(this.testEndInfo());
    }

    /**
     * GetPrevious entry which is the second entry in simulator/drive, the
     * result should be the first entry.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ForKeyIsTheSecondKeyInDB(String clientName) {
        try {
            cleanPreviousData(toByteArray("key006"), getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("clean previous data throw exception! "
                    + e2.getMessage());
        }

        Entry entry0 = null;
        Entry entry1 = null;
        try {
            entry0 = buildAndPutEntry("key005", "value005",
                    getClient(clientName));
            entry1 = buildAndPutEntry("key006", "value006",
                    getClient(clientName));
            buildAndPutEntry("key007", "value007", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put entry throw exception! "
                    + e.getMessage());
        }

        byte[] key = entry1.getKey();

        Entry entryPrevious;
        try {
            entryPrevious = getClient(clientName).getPrevious(key);
            assertEntryEquals(entry0, entryPrevious);
        } catch (KineticException e1) {
            Assert.fail("get previous throw exception! " + e1.getMessage());
        }

        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetPrevious entry which is the third entry in simulator/drive, the result
     * should be the second entry.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ForKeyIsTheThirdKeyInDB(String clientName) {
        try {
            cleanPreviousData(toByteArray("key007"), getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("clean previous data throw exception! "
                    + e2.getMessage());
        }

        Entry entry1 = null;
        Entry entry2 = null;
        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            entry1 = buildAndPutEntry("key006", "value006",
                    getClient(clientName));
            entry2 = buildAndPutEntry("key007", "value007",
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put entry throw exception! "
                    + e.getMessage());
        }

        byte[] key = entry2.getKey();

        Entry entryPrevious;
        try {
            entryPrevious = getClient(clientName).getPrevious(key);
            assertEntryEquals(entry1, entryPrevious);
        } catch (KineticException e1) {
            Assert.fail("get previous throw exception! " + e1.getMessage());
        }

        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }
        logger.info(this.testEndInfo());
    }

    /**
     * GetPrevious entry which is bigger than the last entry in simulator/drive,
     * the result should be the last entry.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ForKeyBiggerThanTheLastKeyInDB(String clientName) {
        byte[] key = toByteArray("key09");

        try {
            cleanPreviousData(toByteArray("key09"), getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("clean previous data throw exception! "
                    + e2.getMessage());
        }

        Entry entry2 = null;
        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            entry2 = buildAndPutEntry("key007", "value007",
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put entry throw exception! "
                    + e.getMessage());
        }

        Entry entryPrevious;
        try {
            entryPrevious = getClient(clientName).getPrevious(key);
            assertEntryEquals(entry2, entryPrevious);
        } catch (KineticException e1) {
            Assert.fail("get previous throw exception! " + e1.getMessage());
        }

        try {
            cleanData(toByteArray("key005"), toByteArray("key007"),
                    getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetPrevious entry but no data existed in simulator/drive, the result
     * should be the null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ReturnNull_ForNoDataInDB(String clientName) {
        byte[] key = toByteArray("key09");

        try {
            cleanPreviousData(toByteArray("key09"), getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("clean previous data throw exception! "
                    + e2.getMessage());
        }

        try {
            Entry entryPrevious = getClient(clientName).getPrevious(key);
            assertNull(entryPrevious);
        } catch (KineticException e) {
            Assert.fail("get previous throw exception! " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test that not found is returned when a user tries to call getPrevious
     * when the would-be-returned key is not visible to that user.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_ReturnUnauthorizedStatus_ForNextKeyNotVisible(
            String clientName) throws KineticException {
        try {
            cleanPreviousData(toByteArray("a"), getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("clean previous data throw exception! "
                    + e2.getMessage());
        }

        List<Entry> visibleEntries = Lists.newArrayList();
        visibleEntries.add(new Entry(toByteArray("a"), toByteArray("valuea")));
        visibleEntries.add(new Entry(toByteArray("c"), toByteArray("valuec")));

        List<Entry> notVisibleEntries = Lists.newArrayList();
        notVisibleEntries.add(new Entry(toByteArray("b"), toByteArray("b")));

        for (Entry entry : visibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        for (Entry entry : notVisibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        KineticClient clientWithLimitedRead = createClientWithLimitedVisibilityAndAddEntriesToStore(
                clientName, visibleEntries, notVisibleEntries);

        try {
            // This client should not be able to read 'b', but should be able to
            // read 'a' and 'c'
            clientWithLimitedRead.getPrevious(visibleEntries.get(1).getKey());
            Assert.fail("Error Expected: This test fails against the simulator but passes against the drive due to a simulator bug");
        } catch (KineticException e) {
            assertEquals(
                    "Kinetic Command Exception: NOT_AUTHORIZED: permission denied",
                    e.getMessage());
        }

        for (Entry entry : visibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        for (Entry entry : notVisibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test that getPrevious succeeds when the user doesn't have visibility for
     * the key passed but does have visibility for the previous key.
     *
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetPrevious_Succeeds_ForGivenKeyNotVisible(String clientName)
            throws KineticException {
        try {
            cleanPreviousData(toByteArray("b"), getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("clean previous data throw exception! "
                    + e2.getMessage());
        }

        List<Entry> visibleEntries = Lists.newArrayList();
        visibleEntries.add(new Entry(toByteArray("a"), toByteArray("valuea")));
        visibleEntries.add(new Entry(toByteArray("c"), toByteArray("valuec")));

        List<Entry> notVisibleEntries = Lists.newArrayList();
        notVisibleEntries.add(new Entry(toByteArray("b"), toByteArray("b")));

        for (Entry entry : visibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        for (Entry entry : notVisibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        KineticClient clientWithLimitRead = createClientWithLimitedVisibilityAndAddEntriesToStore(
                clientName, visibleEntries, notVisibleEntries);

        // getPrevious in the middle and expect the first
        Entry getPreviousResult = clientWithLimitRead
                .getPrevious(notVisibleEntries.get(0).getKey());
        Entry expectedEntry = visibleEntries.get(0);
        assertArrayEquals(expectedEntry.getKey(), getPreviousResult.getKey());
        assertArrayEquals(expectedEntry.getValue(),
                getPreviousResult.getValue());

        for (Entry entry : visibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        for (Entry entry : notVisibleEntries) {
            getClient(clientName).deleteForced(entry.getKey());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is null, the result should be thrown
     * KineticException.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_Throws_ForStartKeyIsNull(String clientName) {
        byte[] key0 = toByteArray("key00000000000");
        byte[] newVersion0 = int32(0);
        byte[] value0 = toByteArray("value00000000000");
        EntryMetadata entryMetadata = new EntryMetadata();
        Entry versioned0 = new Entry(key0, value0, entryMetadata);

        byte[] key1 = toByteArray("key00000000001");
        byte[] newVersion1 = int32(1);
        byte[] value1 = toByteArray("value00000000001");
        EntryMetadata entryMetadata1 = new EntryMetadata();
        Entry versioned1 = new Entry(key1, value1, entryMetadata1);

        try {
            getClient(clientName).deleteForced(key0);
            getClient(clientName).deleteForced(key1);
        } catch (KineticException e1) {
            Assert.fail("clean keys throw exception! " + e1.getMessage());
        }

        try {
            getClient(clientName).put(versioned0, newVersion0);
            getClient(clientName).put(versioned1, newVersion1);
        } catch (KineticException e1) {
            Assert.fail("put keys throw exception! " + e1.getMessage());
        }

        try {
            getClient(clientName).getKeyRange(null, true, key1, true, 10);
            Assert.fail("start key is null, get range failed");
        } catch (KineticException e) {
            assertNull(e.getMessage());
        }

        try {
            getClient(clientName).deleteForced(key0);
            getClient(clientName).deleteForced(key1);
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception! " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, endKey is null, the result should be thrown
     * KineticException.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_Throws_ForEndKeyIsNull(String clientName) {
        byte[] key0 = toByteArray("key00000000000");
        byte[] newVersion0 = int32(0);
        byte[] value0 = toByteArray("value00000000000");
        EntryMetadata entryMetadata = new EntryMetadata();
        Entry versioned0 = new Entry(key0, value0, entryMetadata);

        byte[] key1 = toByteArray("key00000000001");
        byte[] newVersion1 = int32(1);
        byte[] value1 = toByteArray("value00000000001");
        EntryMetadata entryMetadata1 = new EntryMetadata();
        Entry versioned1 = new Entry(key1, value1, entryMetadata1);

        try {
            getClient(clientName).deleteForced(key0);
            getClient(clientName).deleteForced(key1);
        } catch (KineticException e1) {
            Assert.fail("clean keys throw exception! " + e1.getMessage());
        }

        try {
            getClient(clientName).put(versioned0, newVersion0);
            getClient(clientName).put(versioned1, newVersion1);
        } catch (KineticException e1) {
            Assert.fail("put keys throw exception! " + e1.getMessage());
        }

        try {
            getClient(clientName).getKeyRange(key0, true, null, true, 10);
            Assert.fail("end key is null, get range failed");
        } catch (KineticException e) {
            assertNull(e.getMessage());
        }

        try {
            getClient(clientName).deleteForced(key0);
            getClient(clientName).deleteForced(key1);
        } catch (KineticException e1) {
            Assert.fail("clean keys throw exception! " + e1.getMessage());
        }
        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are inclusive, but they do not exist in
     * simulator/drive, the result of key list should be any keys that are
     * sorted between them.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ReturnsKeysInRange_ForStartKeyInclusiveEndKeyInclusiveWithStartAndEndKeyNotExistInDB(
            String clientName) {
        // Not present, before first key
        byte[] startKey = toByteArray("key00");
        // Not present, in between two keys
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = null;
        Entry entry1 = null;
        try {
            entry0 = buildAndPutEntry("key005", "value005",
                    getClient(clientName));
            entry1 = buildAndPutEntry("key006", "value006",
                    getClient(clientName));
            buildAndPutEntry("key008", "value008", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put keys throw exception! " + e.getMessage());
        }

        List<byte[]> keys;
        try {
            keys = getClient(clientName).getKeyRange(startKey, true, endKey,
                    true, 10);
            assertEquals(2, keys.size());
            assertArrayEquals(entry0.getKey(), keys.get(0));
            assertArrayEquals(entry1.getKey(), keys.get(1));
        } catch (KineticException e) {
            Assert.fail("get key range throw exception! " + e.getMessage());
        }

        try {
            getClient(clientName).deleteForced(toByteArray("key005"));
            getClient(clientName).deleteForced(toByteArray("key006"));
            getClient(clientName).deleteForced(toByteArray("key008"));
        } catch (KineticException e1) {
            Assert.fail("clean keys throw exception! " + e1.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are exclusive, but they do not exist in
     * simulator/drive, the result of key list should be any keys that are
     * sorted between them.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetetKeyRange_ReturnsKeysInRange_ForStartKeyExclusiveEndKeyExclusiveWithStartAndEndKeyNotExistInDB(
            String clientName) {
        // Not present, before first key
        byte[] startKey = toByteArray("key00");
        // Not present, in between two keys
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        try {
            getClient(clientName).deleteForced(toByteArray("key005"));
            getClient(clientName).deleteForced(toByteArray("key006"));
            getClient(clientName).deleteForced(toByteArray("key008"));
        } catch (KineticException e) {
            Assert.fail("clean key throw exception. " + e.getMessage());
        }

        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            buildAndPutEntry("key008", "value008", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put Entry throw exception. "
                    + e.getMessage());
        }

        List<byte[]> keys = new ArrayList<byte[]>();
        try {
            keys = getClient(clientName).getKeyRange(startKey, false, endKey,
                    false, 10);
        } catch (KineticException e) {
            Assert.fail("get key range throw exception. " + e.getMessage());
        }
        assertEquals(2, keys.size());
        assertArrayEquals(toByteArray("key005"), keys.get(0));
        assertArrayEquals(toByteArray("key006"), keys.get(1));

        try {
            getClient(clientName).deleteForced(toByteArray("key005"));
            getClient(clientName).deleteForced(toByteArray("key006"));
            getClient(clientName).deleteForced(toByteArray("key008"));
        } catch (KineticException e) {
            Assert.fail("clean key throw exception. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is inclusive, endKey is exclusive, but they do not
     * exist in simulator/drive, the result of key list should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartAndEndKeyNotExistInDB(
            String clientName) {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key002");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        try {
            cleanKeys(getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception. " + e.getMessage());
        }

        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            buildAndPutEntry("key007", "value007", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put Entry throw exception. "
                    + e.getMessage());
        }

        List<byte[]> keys = new ArrayList<byte[]>();
        try {
            keys = getClient(clientName).getKeyRange(startKey, true, endKey,
                    false, 10);
        } catch (KineticException e) {
            Assert.fail("get key range throw exception. " + e.getMessage());
        }

        assertEquals(0, keys.size());

        try {
            cleanKeys(getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is exclusive, endKey is inclusive, but they do not
     * exist in simulator/drive, the result of key list should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartAndEndKeyNotExistInDB(
            String clientName) {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key002");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        try {
            cleanKeys(getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception. " + e.getMessage());
        }

        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            buildAndPutEntry("key007", "value007", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put Entry throw exception. "
                    + e.getMessage());
        }

        List<byte[]> keys = new ArrayList<byte[]>();
        try {
            keys = getClient(clientName).getKeyRange(startKey, false, endKey,
                    true, 10);
        } catch (KineticException e1) {
            Assert.fail("get key range throw exception. " + e1.getMessage());
        }
        assertEquals(0, keys.size());

        try {
            cleanKeys(getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are exclusive, only endKey exists in
     * simulator/drive, the result of key list should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyExistsInDB(
            String clientName) {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key005");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        try {
            cleanKeys(getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception. " + e.getMessage());
        }

        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            buildAndPutEntry("key007", "value007", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put Entry throw exception. "
                    + e.getMessage());
        }

        List<byte[]> keys = null;
        try {
            keys = getClient(clientName).getKeyRange(startKey, false, endKey,
                    false, 10);
        } catch (KineticException e1) {
            Assert.fail("get key range throw exception. " + e1.getMessage());
        }
        assertEquals(0, keys.size());

        try {
            cleanKeys(getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception. " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is inclusive and endKey is exclusive, only endKey
     * exists in simulator/drive, the result of key list should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyExistsInDB(
            String clientName) {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key005");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        try {
            cleanKeys(getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception. " + e.getMessage());
        }

        try {
            buildAndPutEntry("key005", "value005", getClient(clientName));
            buildAndPutEntry("key006", "value006", getClient(clientName));
            buildAndPutEntry("key007", "value007", getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("build and put Entry throw exception. "
                    + e.getMessage());
        }

        List<byte[]> keys = null;
        try {
            keys = getClient(clientName).getKeyRange(startKey, true, endKey,
                    false, 10);
        } catch (KineticException e1) {
            Assert.fail("get key range throw exception. " + e1.getMessage());
        }
        assertEquals(0, keys.size());

        try {
            cleanKeys(getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("clean keys throw exception. " + e.getMessage());
        }
        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is exclusive and endKey is inclusive, only endKey
     * exists in simulator/drive, the result of key list should include end key.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key005");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey is inclusive, only endKey exists in
     * simulator/drive, the result of key list should include end key.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key005");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey is inclusive, only endKey is the second
     * key exists in simulator/drive, the result of key list should include the
     * first key existed in simulator/drive.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyIsTheSecondKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key006");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is inclusive and endKey is exclusive, only endKey
     * is the second key exists in simulator/drive, the result of key list
     * should include the first key existed in simulator/drive.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyIsTheSecondKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key006");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is exclusive and endKey is inclusive, only endKey
     * is the second key exists in simulator/drive, the result of key list
     * should include the first key existed in simulator/drive and endKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyIsTheSecondKeyExistsInDB(
            String clientName) throws UnsupportedEncodingException,
            KineticException {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key006");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        try {
            List<byte[]> keys = getClient(clientName).getKeyRange(startKey,
                    false, endKey, true, 10);
            assertEquals(2, keys.size());
            assertArrayEquals(entry0.getKey(), keys.get(0));
            assertArrayEquals(entry1.getKey(), keys.get(1));
        } catch (KineticException e) {
            Assert.fail("get range failed" + e.getMessage());
        }

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is inclusive and endKey is inclusive, only endKey
     * is the second key exists in simulator/drive, the result of key list
     * should include the first key existed in simulator/drive and endKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyIsTheSecondKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key006");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey is exclusive, only endKey is the last
     * key exists in simulator/drive, the result of key list should include the
     * key existed in simulator/drive without the endKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyIsTheLastKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, false, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is inclusive and endKey is exclusive, only endKey
     * is the last key exists in simulator/drive, the result of key list should
     * include the key existed in simulator/drive without the endKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyIsTheLastKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, false, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is exclusive and endKey is inclusive, only endKey
     * is the last key exists in simulator/drive, the result of key list should
     * include the key existed in simulator/drive.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyIsTheLastKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        Entry entry2 = buildAndPutEntry("key007", "value007",
                getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, true, 10);
        assertEquals(3, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));
        assertArrayEquals(entry2.getKey(), keys.get(2));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey is inclusive, only endKey is the last
     * key exists in simulator/drive, the result of key list should include the
     * key existed in simulator/drive.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyIsTheLastKeyExistsInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        Entry entry2 = buildAndPutEntry("key007", "value007",
                getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, true, 10);
        assertEquals(3, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));
        assertArrayEquals(entry2.getKey(), keys.get(2));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey is exclusive, startKey is the first key
     * and endKey is the second key exist in simulator/drive, the result of key
     * list should empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key005");
        byte[] endKey = toByteArray("key006");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, false, 10);
        assertEquals(0, keys.size());

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is inclusive and endKey is exclusive, startKey is
     * the first key and endKey is the second key exist in simulator/drive, the
     * result of key list should include the first key.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key005");
        byte[] endKey = toByteArray("key006");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is exclusive and endKey is inclusive, startKey is
     * the first key and endKey is the second key exist in simulator/drive, the
     * result of key list should include endKey existed in simulator/drive.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key005");
        byte[] endKey = toByteArray("key006");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry1.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are inclusive, startKey is the first key
     * and endKey is the second key exist in simulator/drive, the result of key
     * list should include startKey and endKey existed in simulator/drive.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key005");
        byte[] endKey = toByteArray("key006");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are exclusive, startKey is the first key
     * and endKey is the last key exist in simulator/drive, the result of key
     * list should include keys existed in simulator/drive without startKey and
     * endKey .
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key005");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry1.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is inclusive and endKey is exclusive, startKey is
     * the first key and endKey is the last key exist in simulator/drive, the
     * result of key list should include keys existed in simulator/drive without
     * endKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void tesGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key005");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, false, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is exclusive and endKey is inclusive, startKey is
     * the first key and endKey is the last key exist in simulator/drive, the
     * result of key list should include keys existed in simulator/drive without
     * startKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key005");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        Entry entry2 = buildAndPutEntry("key007", "value007",
                getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry1.getKey(), keys.get(0));
        assertArrayEquals(entry2.getKey(), keys.get(1));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are inclusive, startKey is the first key
     * and endKey is the last key exist in simulator/drive, the result of key
     * list should include all keys existed in simulator/drive.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key005");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        Entry entry0 = buildAndPutEntry("key005", "value005",
                getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        Entry entry2 = buildAndPutEntry("key007", "value007",
                getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, true, 10);
        assertEquals(3, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));
        assertArrayEquals(entry2.getKey(), keys.get(2));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are exclusive, startKey is the second
     * key and endKey is the last key exist in simulator/drive, the result of
     * key list should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key006");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, false, 10);
        assertEquals(0, keys.size());

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is inclusive and endKey is exclusive, startKey is
     * the second key and endKey is the last key exist in simulator/drive, the
     * result of key list should include startKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key006");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry1.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is exclusive and endKey is inclusive, startKey is
     * the second key and endKey is the last key exist in simulator/drive, the
     * result of key list should include endKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key006");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        Entry entry2 = buildAndPutEntry("key007", "value007",
                getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry2.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are inclusive, startKey is the second
     * key and endKey is the last key exist in simulator/drive, the result of
     * key list should include startKey and endKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key006");
        byte[] endKey = toByteArray("key007");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        Entry entry1 = buildAndPutEntry("key006", "value006",
                getClient(clientName));
        Entry entry2 = buildAndPutEntry("key007", "value007",
                getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry1.getKey(), keys.get(0));
        assertArrayEquals(entry2.getKey(), keys.get(1));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are exclusive, startKey is the last key
     * in simulator/drive, the result of key list should include be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key007");
        byte[] endKey = toByteArray("key09");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, false, 10);
        assertEquals(0, keys.size());

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is inclusive and endKey is exclusive, startKey is
     * the last key in simulator/drive, the result of key list should include
     * the last key.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key007");
        byte[] endKey = toByteArray("key09");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        Entry entry2 = buildAndPutEntry("key007", "value007",
                getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry2.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is exclusive and endKey is inclusive, startKey is
     * the last key in simulator/drive, the result of key list should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key007");
        byte[] endKey = toByteArray("key09");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, true, 10);
        assertEquals(0, keys.size());

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are inclusive, startKey is the last key
     * in simulator/drive, the result of key list should include the last key.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key007");
        byte[] endKey = toByteArray("key09");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        Entry entry2 = buildAndPutEntry("key007", "value007",
                getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry2.getKey(), keys.get(0));

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are exclusive, startKey is the last key
     * in simulator/drive, the result of key list should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyBiggerThanTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, false, 10);
        assertEquals(0, keys.size());

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is inclusive and endKey is exclusive, startKey is
     * bigger than the last key in simulator/drive, the result of key list
     * should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyBiggerThanTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, false, 10);
        assertEquals(0, keys.size());

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is exclusive and endKey is inclusive, startKey is
     * bigger than the last key in simulator/drive, the result of key list
     * should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyBiggerThanTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, false,
                endKey, true, 10);
        assertEquals(0, keys.size());

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are inclusive, startKey is bigger than
     * the last key in simulator/drive, the result of key list should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyBiggerThanTheLastKeyInDB(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, true, 10);
        assertEquals(0, keys.size());

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, no data is stored in simulator/drive, the result of key list
     * should be empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForNoDataInDB(String clientName)
            throws KineticException {
        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, true, 10);
        assertEquals(0, keys.size());
        assertTrue(keys.isEmpty());

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey is after endKey, the result of key list should be
     * empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyAfterEndKey(
            String clientName) throws KineticException {
        byte[] startKey = toByteArray("key006");
        byte[] endKey = toByteArray("key005");

        try {
            cleanData(startKey, endKey, getClient(clientName));
        } catch (KineticException e2) {
            Assert.fail("Clean data throw exception! " + e2.getMessage());
        }

        buildAndPutEntry("key005", "value005", getClient(clientName));
        buildAndPutEntry("key006", "value006", getClient(clientName));
        buildAndPutEntry("key007", "value007", getClient(clientName));

        List<byte[]> keys = getClient(clientName).getKeyRange(startKey, true,
                endKey, true, 10);
        assertEquals(0, keys.size());

        cleanKeys(getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRange API: startKey equals endKey, startKey inclusive and
     * endKey inclusive, should return startKey.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_StartKeyEqualsEndKey_StartKeyInclusiveEndKeyInclusive(
            String clientName) throws KineticException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        for (byte[] key : keys) {
            getClient(clientName).putForced(new Entry(key, key));
        }

        List<byte[]> returnedKeys = Lists.newLinkedList(getClient(clientName)
                .getKeyRange(keys.get(0), true, keys.get(0), true,
                        keys.size() - 1));

        assertEquals(1, returnedKeys.size());
        assertArrayEquals(keys.get(0), returnedKeys.get(0));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRange API: startKey equals endKey, startKey exclusive and
     * endKey inclusive, should return empty list.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_StartKeyEqualsEndKey_StartKeyExclusiveEndKeyInclusive(
            String clientName) throws KineticException {

        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        for (byte[] key : keys) {
            getClient(clientName).putForced(new Entry(key, key));
        }

        List<byte[]> returnedKeys = Lists.newLinkedList(getClient(clientName)
                .getKeyRange(keys.get(0), false, keys.get(0), true,
                        keys.size() - 1));

        assertEquals(0, returnedKeys.size());

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRange API: startKey equals endKey, startKey inclusive and
     * endKey exclusive, should return empty list.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_StartKeyEqualsEndKey_StartKeyinclusiveEndKeyexclusive(
            String clientName) throws KineticException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        for (byte[] key : keys) {
            getClient(clientName).putForced(new Entry(key, key));
        }

        List<byte[]> returnedKeys = Lists.newLinkedList(getClient(clientName)
                .getKeyRange(keys.get(0), true, keys.get(0), false,
                        keys.size() - 1));

        assertEquals(0, returnedKeys.size());

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * Test getKeyRange API: startKey equals endKey, startKey exclusive and
     * endKey exclusive, should return empty.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testGetKeyRange_StartKeyEqualsEndKey_StartKeyexclusiveEndKeyexclusive(
            String clientName) throws KineticException {
        List<byte[]> keys = Arrays.asList(toByteArray("00"), toByteArray("01"),
                toByteArray("02"), toByteArray("03"), toByteArray("04"),
                toByteArray("05"), toByteArray("06"), toByteArray("07"),
                toByteArray("08"), toByteArray("09"), toByteArray("10"),
                toByteArray("11"), toByteArray("12"), toByteArray("13"),
                toByteArray("14"));

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        for (byte[] key : keys) {
            getClient(clientName).putForced(new Entry(key, key));
        }

        List<byte[]> returnedKeys = Lists.newLinkedList(getClient(clientName)
                .getKeyRange(keys.get(0), false, keys.get(0), false,
                        keys.size() - 1));

        assertEquals(0, returnedKeys.size());

        cleanData(toByteArray("00"), toByteArray("14"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, returns the first contiguous block of keys for which the
     * user has RANGE role. Does not return subsequent keys, even if there is a
     * second block of keys in the requested range with RANGE role.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testGetKeyRange_ReturnsFirstNKeysWithRangeRole(String clientName)
            throws KineticException {
        List<Entry> listEntry = new ArrayList<Entry>();
        // Client will not have RANGE on this
        Entry entry01 = new Entry(toByteArray("k01"), toByteArray("v01"));

        // Client will have RANGE on these
        Entry entry02 = new Entry(toByteArray("k02"), toByteArray("v02"));
        Entry entry03 = new Entry(toByteArray("k03"), toByteArray("v03"));

        // Client will not have RANGE on these
        Entry entry04 = new Entry(toByteArray("k04"), toByteArray("v04"));
        Entry entry05 = new Entry(toByteArray("k05"), toByteArray("v05"));

        // Client will have RANGE on these
        Entry entry06 = new Entry(toByteArray("k06"), toByteArray("v06"));
        Entry entry07 = new Entry(toByteArray("k07"), toByteArray("v07"));
        Entry entry08 = new Entry(toByteArray("k08"), toByteArray("v08"));

        // Client will not have RANGE on this
        Entry entry09 = new Entry(toByteArray("k09"), toByteArray("v09"));

        listEntry.add(entry01);
        listEntry.add(entry02);
        listEntry.add(entry03);
        listEntry.add(entry04);
        listEntry.add(entry05);
        listEntry.add(entry06);
        listEntry.add(entry07);
        listEntry.add(entry08);
        listEntry.add(entry09);

        Map<List<Entry>, List<Kinetic.Command.Security.ACL.Permission>> entryToRoleMap = Maps
                .newHashMap();
        // Put the first set with Range
        entryToRoleMap.put(Arrays.asList(entry02, entry03), Collections
                .singletonList(Kinetic.Command.Security.ACL.Permission.RANGE));
        // Put the second set, without range, which is a breaking gap
        entryToRoleMap.put(Arrays.asList(entry04, entry05), Collections
                .singletonList(Kinetic.Command.Security.ACL.Permission.READ));
        // Put the third set, also with range, which will not be returned
        // because of the gap
        entryToRoleMap.put(Arrays.asList(entry06, entry08), Collections
                .singletonList(Kinetic.Command.Security.ACL.Permission.RANGE));

        cleanData(toByteArray("k01"), toByteArray("k09"), getClient(clientName));

        KineticClient clientWithVisibilityGap = createClientWithSpecifiedRolesForEntries(
                clientName, entryToRoleMap);

        // XXX chiaming 01/27/2015: RANGE op throws Exception if no permission
        // for all keys.
        // for all domains.

        List<byte[]> keyRange = clientWithVisibilityGap.getKeyRange(
                entry02.getKey(), true, entry03.getKey(), true, 10);
        assertEquals(2, keyRange.size());
        assertArrayEquals(entry02.getKey(), keyRange.get(0));
        assertArrayEquals(entry03.getKey(), keyRange.get(1));

        cleanData(toByteArray("k01"), toByteArray("k09"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, returns the first contiguous block of keys for which the
     * user has RANGE role. Does not return subsequent keys, even if there is a
     * second block of keys in the requested range with RANGE role.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testGetKeyRangeReversed_ReturnsLastNKeysWithRangeRole(
            String clientName) throws KineticException {
        List<Entry> listEntry = new ArrayList<Entry>();
        // Client will not have RANGE on this
        Entry entry01 = new Entry(toByteArray("k01"), toByteArray("v01"));

        // Client will have RANGE on these
        Entry entry02 = new Entry(toByteArray("k02"), toByteArray("v02"));
        Entry entry03 = new Entry(toByteArray("k03"), toByteArray("v03"));

        // Client will not have RANGE on these
        Entry entry04 = new Entry(toByteArray("k04"), toByteArray("v04"));
        Entry entry05 = new Entry(toByteArray("k05"), toByteArray("v05"));

        // Client will have RANGE on these
        Entry entry06 = new Entry(toByteArray("k06"), toByteArray("v06"));
        Entry entry07 = new Entry(toByteArray("k07"), toByteArray("v07"));
        Entry entry08 = new Entry(toByteArray("k08"), toByteArray("v08"));

        // Client will not have RANGE on this
        Entry entry09 = new Entry(toByteArray("k09"), toByteArray("v09"));

        listEntry.add(entry01);
        listEntry.add(entry02);
        listEntry.add(entry03);
        listEntry.add(entry04);
        listEntry.add(entry05);
        listEntry.add(entry06);
        listEntry.add(entry07);
        listEntry.add(entry08);
        listEntry.add(entry09);

        Map<List<Entry>, List<Kinetic.Command.Security.ACL.Permission>> entryToRoleMap = Maps
                .newHashMap();
        // Put the first set with Range, which will not be returned because of
        // the gap
        entryToRoleMap.put(Arrays.asList(entry02, entry03), Collections
                .singletonList(Kinetic.Command.Security.ACL.Permission.RANGE));
        // Put the second set, without range, which is a breaking gap
        entryToRoleMap.put(Arrays.asList(entry04, entry05), Collections
                .singletonList(Kinetic.Command.Security.ACL.Permission.READ));
        // Put the third set, also with range, which we expect to be returned
        // (reversed range)
        entryToRoleMap
                .put(Arrays.asList(entry06, entry07, entry08),
                        Collections
                                .singletonList(Kinetic.Command.Security.ACL.Permission.RANGE));

        cleanData(toByteArray("k01"), toByteArray("k09"), getClient(clientName));

        AdvancedKineticClient clientWithVisibilityGap = createClientWithSpecifiedRolesForEntries(
                clientName, entryToRoleMap);

        // XXX chiaming 01/27/2015: Range Op throws exception if no permission
        // for all keys.
        List<byte[]> keyRange = clientWithVisibilityGap.getKeyRangeReversed(
                entry06.getKey(), true, entry08.getKey(), true, 10);
        assertEquals(3, keyRange.size());
        assertArrayEquals(entry08.getKey(), keyRange.get(0));
        assertArrayEquals(entry07.getKey(), keyRange.get(1));
        assertArrayEquals(entry06.getKey(), keyRange.get(2));

        cleanData(toByteArray("k01"), toByteArray("k09"), getClient(clientName));

        logger.info(this.testEndInfo());
    }

    /**
     * A utility which is useful for testing permissions on various operations.
     * <p/>
     * Creates a new client who is only able to see visbileEntries and adds all
     * entries to store.
     *
     * @param visibleEntries
     *            The entries the new client should be able to see
     * @param notVisibleEntries
     *            The entries the new client should not be able to see
     * @return KineticClient a client with limited visibility
     * @throws KineticException
     */
    private KineticClient createClientWithLimitedVisibilityAndAddEntriesToStore(
            String clientName, List<Entry> visibleEntries,
            List<Entry> notVisibleEntries) throws KineticException {
        Map<List<Entry>, List<ACL.Permission>> map = Maps.newHashMap();
        map.put(visibleEntries, Collections
                .singletonList(Kinetic.Command.Security.ACL.Permission.READ));
        map.put(notVisibleEntries, Collections
                .<Kinetic.Command.Security.ACL.Permission> emptyList());

        return createClientWithSpecifiedRolesForEntries(clientName, map);
    }

    /**
     * A utility which is useful for testing permissions on various operations.
     *
     * Puts all entries in the given map into the store, for each list of
     * entries there is a list of roles which will be set.
     *
     * This allows us to, say, put one list of objects with READ/WRITE ability
     * and another set with RANGE ability
     *
     * @param entriesToRoleMap
     *            A map of List of entries (to put) to the List of roles to set
     *            for the client on those entries
     *
     * @return DefaultKineticClient
     * @throws KineticException
     */
    private DefaultKineticClient createClientWithSpecifiedRolesForEntries(
            String clientName,
            Map<List<Entry>, List<Kinetic.Command.Security.ACL.Permission>> entriesToRoleMap)
            throws KineticException {
        // Set up a new client with 2 domains which allow the client to read
        // keys that start with "a" or "c"
        int clientId = 2;
        String clientKeyString = "ClientWhoCannotReadEverything";

        List<Kinetic.Command.Security.ACL.Scope> domains = Lists.newArrayList();

        for (Map.Entry<List<Entry>, List<Kinetic.Command.Security.ACL.Permission>> listListEntry : entriesToRoleMap
                .entrySet()) {
            domains.addAll(putEntriesAndGetDomains(clientName,
                    listListEntry.getKey(), listListEntry.getValue()));
        }

        createClientAclWithDomains(clientName, clientId, clientKeyString,
                domains);

        // Instantiate directly instead of using the factory, since we want this
        // typed as a DefaultKineticClient to
        // expose certain methods that don't exist on the top-level
        // KineticClient interface.
        DefaultKineticClient clientWithLimitedReadPermission = new DefaultKineticClient(
                getClientConfig(clientId, clientKeyString));

        return clientWithLimitedReadPermission;
    }

    /**
     * Put a list of entries and builds a list of domains to add to client ACL
     * based on the rolesToAdd
     *
     * @param entriesToPut
     *            The entries to add to the store
     * @param rolesToAdd
     *            The roles to set on those entries for the given client
     * @return The domains to add to the client ACL
     * @throws KineticException
     */
    private List<Kinetic.Command.Security.ACL.Scope> putEntriesAndGetDomains(
            String clientName, List<Entry> entriesToPut,
            List<Permission> rolesToAdd) throws KineticException {
        List<Kinetic.Command.Security.ACL.Scope> domains = Lists.newArrayList();
        for (Entry entry : entriesToPut) {
            getClient(clientName).deleteForced(entry.getKey());
            getClient(clientName).put(entry, null);

            if (!rolesToAdd.isEmpty()) {
                // Create a domain that allows the given role for this entry's
                // key
                Kinetic.Command.Security.ACL.Scope.Builder domain = Kinetic.Command.Security.ACL.Scope
                        .newBuilder();
                for (Kinetic.Command.Security.ACL.Permission role : rolesToAdd) {
                    domain.addPermission(role);
                }
                domain.setOffset(0);
                domain.setValue(ByteString.copyFrom(entry.getKey()));
                domains.add(domain.build());
            }

        }

        return domains;
    }

    /**
     * Test max key range request size cannot exceed max supported size (1024).
     */
    // TODO open
    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testGetRangeExceedMaxSize(String clientName) {

        byte[] key0 = toByteArray("key00000000000");
        byte[] key1 = toByteArray("key00000000001");

        try {
            int max = SimulatorConfiguration.getMaxSupportedKeyRangeSize();
            getClient(clientName)
                    .getKeyRange(key0, true, key1, true, (max + 1));
            Assert.fail("did not receive expected exception: request key range exceeds max allowed size "
                    + max);
        } catch (KineticException e) {
            logger.info("caught expected exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test max key request size cannot exceed max supported size (4096).
     */
    // TODO open
    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testMaxKeyLength(String clientName) {
        int size = SimulatorConfiguration.getMaxSupportedKeySize();
        byte[] key0 = new byte[size];
        try {
            getClient(clientName).get(key0);
        } catch (KineticException e) {
            Assert.fail("received unexpected exception: " + e);
        }

        byte[] key1 = new byte[size + 1];

        try {
            getClient(clientName).get(key1);
            Assert.fail("did not receive expected exception: request key exceeds max allowed size "
                    + size);
        } catch (KineticException e) {
            logger.info("caught expected exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test put max version length cannot exceed max supported size (2048).
     * 
     * @throws KineticException
     */
    // TODO
    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testPutExceedMaxVersionLength(String clientName) {
        byte[] key = toByteArray("key00000000000");
        byte[] value = toByteArray("value00000000000");

        int vlen = SimulatorConfiguration.getMaxSupportedVersionSize();

        byte[] version = new byte[vlen + 1];
        Entry entry = new Entry();
        entry.setKey(key);
        entry.setValue(value);
        entry.getEntryMetadata().setVersion(version);

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("Clean key throw exceptions." + e.getMessage());
        }

        // expect to fail: exceed max version size to put
        try {
            getClient(clientName).putForced(entry);
            Assert.fail("did not receive expected exception: request put version exceeds max allowed size "
                    + vlen);
        } catch (KineticException e) {
            logger.info("caught expected exception: " + e.getMessage());
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e) {
            Assert.fail("Clean key throw exceptions." + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test delete max version length cannot exceed max supported size (2048).
     */
    // TODO
    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testDeleteExceedMaxVersionLength(String clientName) {

        byte[] key = toByteArray("key00000000000");
        byte[] value = toByteArray("value00000000000");

        int vlen = SimulatorConfiguration.getMaxSupportedVersionSize();

        byte[] version = new byte[vlen + 1];
        Entry entry = new Entry();
        entry.setKey(key);
        entry.setValue(value);
        entry.getEntryMetadata().setVersion(version);

        // expect fail to delete: exceed max version size
        try {
            getClient(clientName).delete(entry);
            Assert.fail("did not receive expected exception: request delete version exceeds max allowed size "
                    + vlen);
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.INVALID_REQUEST));
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Test max version length cannot exceed max supported size (2048).
     */
    @Test(dataProvider = "transportProtocolOptions")
    public void testValidMaxVersionLength(String clientName) {

        byte[] key = toByteArray("key00000000000");
        byte[] value = toByteArray("value00000000000");

        int vlen = SimulatorConfiguration.getMaxSupportedVersionSize();

        byte[] version = new byte[vlen];
        Entry entry = new Entry();
        entry.setKey(key);
        entry.setValue(value);
        entry.getEntryMetadata().setVersion(version);

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e1) {
            Assert.fail("received unexpected exception: " + e1);
        }

        // expect to succeed - allowed version size
        try {
            getClient(clientName).putForced(entry);
        } catch (KineticException e) {
            Assert.fail("received unexpected exception: " + e);
        }

        // expect succeed to delete.
        try {

            entry.getEntryMetadata().setVersion(version);
            // expect succeed
            boolean deleted = getClient(clientName).delete(entry);

            assertTrue(deleted);
        } catch (KineticException e) {
            Assert.fail("received unexpected exception: " + e);
        }

        try {
            getClient(clientName).deleteForced(key);
        } catch (KineticException e1) {
            Assert.fail("received unexpected exception: " + e1);
        }

        logger.info(this.testEndInfo());
    }

    private void cleanKeys(KineticClient client) throws KineticException {
        byte[] key0 = toByteArray("key005");
        client.deleteForced(key0);

        byte[] key1 = toByteArray("key006");
        client.deleteForced(key1);

        byte[] key2 = toByteArray("key007");
        client.deleteForced(key2);
    }
}
