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
package com.seagate.kinetic.boundary;

import static com.seagate.kinetic.KineticAssertions.assertEntryEquals;
import static com.seagate.kinetic.KineticAssertions.assertKeyNotFound;
import static com.seagate.kinetic.KineticTestHelpers.int32;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
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

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.IntegrationTestLoggerFactory;
import com.seagate.kinetic.KineticTestRunner;
import com.seagate.kinetic.client.internal.DefaultKineticClient;
import com.seagate.kinetic.proto.Kinetic;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL;
import com.seagate.kinetic.proto.Kinetic.Message.Security.ACL.Permission;
import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;

/**
 * Kinetic Client API Boundary Test.
 * <p>
 * Boundary test against basic API.
 * <p>
 *
 * @see KineticClient
 *
 */
@RunWith(KineticTestRunner.class)
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
    @Test
    public void testPut_Throws_ForVersionWithDifferentLength()
            throws KineticException {
        byte[] key = toByteArray("key00000000000");
        byte[] newVersionInit = int32(0);
        byte[] valueInit = toByteArray("value00000000000");

        EntryMetadata entryMetadata = new EntryMetadata();
        Entry versionedInit = new Entry(key, valueInit, entryMetadata);
        getClient().put(versionedInit, newVersionInit);

        try {
            byte[] newVersion = int32(1);
            byte[] value = toByteArray("value00000000001");
            byte[] dbVersion = toByteArray(new String(getClient().get(key)
                    .getEntryMetadata().getVersion()) + 10);

            EntryMetadata entryMetadata1 = new EntryMetadata();
            entryMetadata1.setVersion(dbVersion);
            Entry versioned = new Entry(key, value, entryMetadata1);

            getClient().put(versioned, newVersion);
            fail("Should have thrown");
        } catch (KineticException e1) {
            Entry vGet = getClient().get(key);
            assertEntryEquals(key, valueInit, newVersionInit, vGet);
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
    @Test
    public void testPut_Throws_ForWrongVersion() throws KineticException {
        byte[] key = toByteArray("key00000000000");
        byte[] newVersionInit = int32(0);
        byte[] valueInit = toByteArray("value00000000000");

        EntryMetadata entryMetadata = new EntryMetadata();
        Entry versionedInit = new Entry(key, valueInit, entryMetadata);
        getClient().put(versionedInit, newVersionInit);

        try {
            byte[] newVersion = int32(1);
            byte[] value = toByteArray("value00000000001");
            byte[] dbVersion = int32(1);

            EntryMetadata entryMetadata1 = new EntryMetadata();
            entryMetadata1.setVersion(dbVersion);
            Entry versioned = new Entry(key, value, entryMetadata1);

            getClient().put(versioned, newVersion);
            fail("Should have thrown");
        } catch (VersionMismatchException e1) {
            Entry vGet = getClient().get(key);
            assertEntryEquals(key, valueInit, newVersionInit, vGet);
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
    @Test
    public void testPut_Throws_WhenVersionIsSetWithNewKey()
            throws KineticException {
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
            getClient().put(newEntryWithInvalidDbVersion, newVersion);
            fail("Should have thrown");
        } catch (VersionMismatchException e1) {
            logger.info("caught expected VersionMismatchException exception.");
        } catch (KineticException ke) {
            fail ("Should have caught VersionMismatchException.");
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Put a null entry to simulator/drive. The test result should be thrown
     * KineticException.
     * <p>
     *
     */
    @Test
    public void testPut_Throws_WhenPuttingNull() {
        try {
            getClient().put(null, null);
            fail("put null should fail");
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
    @Test
    public void testPutAllowsEmptyValues() throws KineticException {
        byte[] key = { 0x3 };
        Entry entry = new Entry(key, new byte[0]);
        getClient().put(entry, new byte[] { 2 });

        assertArrayEquals(new byte[0], getClient().get(key).getValue());

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
    @Test
    public void testPutAllowsNullValues() throws KineticException {
        byte[] key = { 0x3 };
        Entry entry = new Entry(key, null);
        getClient().put(entry, new byte[] { 2 });

        assertArrayEquals(new byte[0], getClient().get(key).getValue());

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
    @Test
    public void testPutAllowsSpaceValues() throws KineticException {
        byte[] key = { 0x3 };
        byte[] value = toByteArray(" ");
        Entry entry = new Entry(key, value);
        getClient().put(entry, new byte[] { 2 });

        assertArrayEquals(value, getClient().get(key).getValue());

        logger.info(this.testEndInfo());
    }

    /**
     * Put a entry with a value which is too long.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test
    public void testPut_Throws_ForValueTooLong() throws KineticException {
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
            getClient().put(entry, newVersion);
            fail("Should have thrown");
        } catch (KineticException e) {
            // TODO: The simulator returns INTERNAL_ERROR, but the drive returns
            // an IO error
            // ... We would rather have this test than remove it, and it is
            // undesirable for it to fail when run
            // ... against one target. So, we tolerate both cases for now.
            StatusCode code = e.getResponseMessage().getMessage().getCommand().getStatus().getCode();
            
            assertEquals (StatusCode.INVALID_REQUEST, code);
        }
    }

    /**
     * Put a entry when a user does not have permissions to do so.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test
    public void testPut_Throws_ForUserWithoutWritePerms()
            throws KineticException {
        int clientId = 2;
        String clientKeyString = "testclientwithoutputkey";

        // Give the client a "Read" permission but no Write
        createClientAclWithRoles(clientId, clientKeyString,
                Collections
                .singletonList(Kinetic.Message.Security.ACL.Permission.READ));

        KineticClient clientWithoutPutPermission = KineticClientFactory
                .createInstance(getClientConfig(clientId, clientKeyString));

        byte[] key = toByteArray("key00000000000");
        byte[] newVersion = int32(0);
        byte[] value = toByteArray("value00000000000");

        EntryMetadata entryMetadata = new EntryMetadata();
        Entry entry = new Entry(key, value, entryMetadata);

        try {
            clientWithoutPutPermission.put(entry, newVersion);
            fail("Should have thrown");
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
    @Test
    public void testGet_ReturnsNull_ForNonExistingKey() throws KineticException {
        byte[] key = toByteArray("key00000000000");
        assertKeyNotFound(getClient(), key);

        logger.info(this.testEndInfo());
    }

    /**
     * Get the key's value is null from simulator/drive, the result should be
     * thrown KineticException.
     * <p>
     *
     */
    @Test
    public void testGet_Throws_ForNullKey() {
        try {
            getClient().get(null);
            fail("the key is null, get failed");
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
    @Test
    public void testDelete_ReturnsFalse_ForNonExistingKey()
            throws UnsupportedEncodingException, KineticException {
        byte[] key = toByteArray("key00000000000");
        byte[] valueInit = toByteArray("value00000000000");
        Entry versionedInit;

        EntryMetadata entryMetadata = new EntryMetadata();
        versionedInit = new Entry(key, valueInit, entryMetadata);
        assertFalse(getClient().delete(versionedInit));
        assertKeyNotFound(getClient(), key);

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
    @Test
    public void testDelete_Throws_ForWrongLengthVersion()
            throws KineticException {
        byte[] key = toByteArray("key00000000000");
        byte[] newVersionInit = int32(0);
        byte[] valueInit = toByteArray("value00000000000");
        Entry versionedInit;

        EntryMetadata entryMetadata = new EntryMetadata();
        versionedInit = new Entry(key, valueInit, entryMetadata);

        try {
            getClient().put(versionedInit, newVersionInit);

            byte[] value = toByteArray("value00000000001");
            byte[] dbVersion = toByteArray(new String(getClient().get(key)
                    .getEntryMetadata().getVersion()) + 10);

            entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(dbVersion);
            Entry versioned = new Entry(key, value, entryMetadata);

            getClient().delete(versioned);
            fail("Should have thrown");
        } catch (KineticException e) {
            Entry vGet = getClient().get(key);
            assertEntryEquals(key, valueInit, newVersionInit, vGet);
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
    @Test
    public void testDelete_Throws_ForWrongVersion() throws KineticException {
        byte[] key = toByteArray("key00000000000");
        byte[] newVersionInit = int32(0);
        byte[] valueInit = toByteArray("value00000000000");
        Entry versionedInit;

        EntryMetadata entryMetadata = new EntryMetadata();
        versionedInit = new Entry(key, valueInit, entryMetadata);
        getClient().put(versionedInit, newVersionInit);
        try {
            byte[] value = toByteArray("value00000000001");
            byte[] dbVersion = int32(1);

            entryMetadata = new EntryMetadata();
            entryMetadata.setVersion(dbVersion);
            Entry versioned = new Entry(key, value, entryMetadata);

            getClient().delete(versioned);
            fail("Should have thrown");
        } catch (KineticException e) {
            Entry vGet = getClient().get(key);
            assertEntryEquals(key, valueInit, newVersionInit, vGet);
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
    @Test
    public void testDelete_Throws_ForUserWithoutDeletePerms()
            throws KineticException {
        int clientId = 2;
        String clientKeyString = "testclientwithoutdeletekey";

        // Give the client a "Write" permission but no delete
        createClientAclWithRoles(clientId, clientKeyString,
                Collections
                .singletonList(Kinetic.Message.Security.ACL.Permission.WRITE));

        KineticClient clientWithoutDelete = KineticClientFactory
                .createInstance(getClientConfig(clientId, clientKeyString));

        byte[] key = toByteArray("key00000000000");
        byte[] newVersion = int32(0);
        byte[] value = toByteArray("value00000000000");
        Entry entry;

        EntryMetadata entryMetadata = new EntryMetadata();
        entry = new Entry(key, value, entryMetadata);

        Entry putResultEntry = clientWithoutDelete.put(entry, newVersion);

        try {
            clientWithoutDelete.delete(putResultEntry);
            fail("Should have thrown");
        } catch (KineticException e) {
            assertEquals(
                    "Kinetic Command Exception: NOT_AUTHORIZED: permission denied",
                    e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * Delete a null entry, the result should be thrown KineticException.
     * <p>
     */
    @Test
    public void testDelete_Throws_ForNullKey() {
        try {
            getClient().delete(null);
            fail("Should have thrown");
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
    @Test
    public void testGetNext_ReturnsNull_ForNonExistKey()
            throws KineticException {
        Entry v = getClient().getNext(toByteArray("foobarbaz"));
        assertNull(v);

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
    @Test
    public void testGetNext_Throws_ForNullKey() throws KineticException {
        try {
            getClient().getNext(null);
            fail("getNext should throw for null key");
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
    @Test
    public void testGetNext_ForKeySmallerThanTheFirstKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] key = toByteArray("key00");

        Entry entryNext = getClient().getNext(key);

        assertEntryEquals(entry0, entryNext);

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
    @Test
    public void testGetNext_ForKeyIsTheFirstKeyInDB() throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] key = entry0.getKey();

        Entry entryNext = getClient().getNext(key);

        assertEntryEquals(entry1, entryNext);

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
    @Test
    public void testGetNext_ForKeyIsTheSecondKeyInDB() throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] key = entry1.getKey();

        Entry entryNext = getClient().getNext(key);

        assertEntryEquals(entry2, entryNext);

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
    @Test
    public void testGetNext_ReturnNull_ForKeyIsTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] key = entry2.getKey();

        Entry entryNext = getClient().getNext(key);

        assertNull(entryNext);

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
    @Test
    public void testGetNext_ReturnNull_ForKeyBiggerThanTheLastKeyInDB()
            throws UnsupportedEncodingException, KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] key = toByteArray("key09");

        Entry entryNext = getClient().getNext(key);

        assertNull(entryNext);

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
    @Test
    public void testGetNext_ReturnUnauthorizedStatus_ForNextKeyNotVisible()
            throws UnsupportedEncodingException, KineticException {
        List<Entry> visibleEntries = Lists.newArrayList();
        visibleEntries.add(new Entry(toByteArray("a"), toByteArray("valuea")));
        visibleEntries.add(new Entry(toByteArray("c"), toByteArray("valuec")));

        List<Entry> notVisibleEntries = Lists.newArrayList();
        notVisibleEntries.add(new Entry(toByteArray("b"), toByteArray("b")));

        KineticClient clientWithLimitedRead = createClientWithLimitedVisibilityAndAddEntriesToStore(
                visibleEntries, notVisibleEntries);

        try {
            // This client should not be able to read 'b', but should be able to
            // read 'a' and 'c'
            clientWithLimitedRead.getNext(visibleEntries.get(0).getKey());
            fail("Error Expected: This test fails against the simulator but passes against the drive due to a simulator bug");
        } catch (KineticException e) {
            assertEquals(e.getMessage(),
                    "Kinetic Command Exception: NOT_AUTHORIZED: permission denied");
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
    @Test
    public void testGetNext_Succeeds_ForGivenKeyNotVisible()
            throws UnsupportedEncodingException, KineticException {
        List<Entry> visibleEntries = Lists.newArrayList();
        visibleEntries.add(new Entry(toByteArray("a"), toByteArray("valuea")));
        visibleEntries.add(new Entry(toByteArray("c"), toByteArray("valuec")));

        List<Entry> notVisibleEntries = Lists.newArrayList();
        notVisibleEntries.add(new Entry(toByteArray("b"), toByteArray("b")));

        KineticClient clientWithLimitRead = createClientWithLimitedVisibilityAndAddEntriesToStore(
                visibleEntries, notVisibleEntries);
        Entry getNextResult = clientWithLimitRead.getNext(notVisibleEntries
                .get(0).getKey());
        Entry expectedEntry = visibleEntries.get(1);
        assertArrayEquals(expectedEntry.getKey(), getNextResult.getKey());
        assertArrayEquals(expectedEntry.getValue(), getNextResult.getValue());

        logger.info(this.testEndInfo());
    }

    /**
     * GetNext entry with empty in simulator/drive, the result should be null.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test
    public void testGetNext_ReturnNull_ForNoDataInDB() throws KineticException {
        byte[] key = toByteArray("key09");
        Entry entryNext = getClient().getNext(key);
        assertNull(entryNext);

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
    @Test
    public void testGetPrevious_ReturnsNull_ForNonExistingKey()
            throws KineticException {
        Entry v = getClient().getPrevious(toByteArray("foobaraasdf"));
        assertNull(v);

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
    @Test
    public void testGetPrevious_ReturnsNull_ForFirstKey()
            throws KineticException {
        byte[] key0 = toByteArray("key00000000000");
        byte[] newVersion0 = int32(0);
        byte[] value0 = toByteArray("value00000000000");
        EntryMetadata entryMetadata = new EntryMetadata();
        Entry versioned0 = new Entry(key0, value0, entryMetadata);

        getClient().put(versioned0, newVersion0);

        assertNull(getClient().getPrevious(key0));

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
    @Test
    public void testGetPrevious_Throws_ForNullKey() throws KineticException {
        try {
            getClient().getPrevious(null);
            fail("Should have thrown");
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
    @Test
    public void testGetPrevious_ReturnNull_ForKeySmallerThanTheFirstKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] key = toByteArray("key00");

        Entry entryPrevious = getClient().getPrevious(key);

        assertNull(entryPrevious);

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
    @Test
    public void testGetPrevious_ForKeyIsTheFirstKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] key = entry0.getKey();

        Entry entryPrevious = getClient().getPrevious(key);

        assertNull(entryPrevious);

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
    @Test
    public void testGetPrevious_ForKeyIsTheSecondKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] key = entry1.getKey();

        Entry entryPrevious = getClient().getPrevious(key);

        assertEntryEquals(entry0, entryPrevious);

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
    @Test
    public void testGetPrevious_ForKeyIsTheThirdKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] key = entry2.getKey();

        Entry entryPrevious = getClient().getPrevious(key);

        assertEntryEquals(entry1, entryPrevious);

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
    @Test
    public void testGetPrevious_ForKeyBiggerThanTheLastKeyInDB()
            throws KineticException {
        byte[] key0 = toByteArray("key005");
        byte[] value0 = toByteArray("value005");
        Entry entry0 = new Entry(key0, value0);

        byte[] key1 = toByteArray("key006");
        byte[] value1 = toByteArray("value006");
        Entry entry1 = new Entry(key1, value1);

        byte[] key2 = toByteArray("key007");
        byte[] value2 = toByteArray("value007");
        Entry entry2 = new Entry(key2, value2);

        getClient().put(entry0, null);
        getClient().put(entry1, null);
        getClient().put(entry2, null);

        byte[] key = toByteArray("key09");

        Entry entryPrevious = getClient().getPrevious(key);

        assertEntryEquals(entry2, entryPrevious);

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
    @Test
    public void testGetPrevious_ReturnNull_ForNoDataInDB()
            throws KineticException {
        byte[] key = toByteArray("key09");

        Entry entryPrevious = getClient().getPrevious(key);

        assertNull(entryPrevious);

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
    @Test
    public void testGetPrevious_ReturnUnauthorizedStatus_ForNextKeyNotVisible()
            throws UnsupportedEncodingException, KineticException {

        List<Entry> visibleEntries = Lists.newArrayList();
        visibleEntries.add(new Entry(toByteArray("a"), toByteArray("valuea")));
        visibleEntries.add(new Entry(toByteArray("c"), toByteArray("valuec")));

        List<Entry> notVisibleEntries = Lists.newArrayList();
        notVisibleEntries.add(new Entry(toByteArray("b"), toByteArray("b")));

        KineticClient clientWithLimitedRead = createClientWithLimitedVisibilityAndAddEntriesToStore(
                visibleEntries, notVisibleEntries);

        try {
            // This client should not be able to read 'b', but should be able to
            // read 'a' and 'c'
            clientWithLimitedRead.getPrevious(visibleEntries.get(1).getKey());
            fail("Error Expected: This test fails against the simulator but passes against the drive due to a simulator bug");
        } catch (KineticException e) {
            assertEquals(
                    "Kinetic Command Exception: NOT_AUTHORIZED: permission denied",
                    e.getMessage());
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
    @Test
    public void testGetPrevious_Succeeds_ForGivenKeyNotVisible()
            throws UnsupportedEncodingException, KineticException {
        List<Entry> visibleEntries = Lists.newArrayList();
        visibleEntries.add(new Entry(toByteArray("a"), toByteArray("valuea")));
        visibleEntries.add(new Entry(toByteArray("c"), toByteArray("valuec")));

        List<Entry> notVisibleEntries = Lists.newArrayList();
        notVisibleEntries.add(new Entry(toByteArray("b"), toByteArray("b")));

        KineticClient clientWithLimitRead = createClientWithLimitedVisibilityAndAddEntriesToStore(
                visibleEntries, notVisibleEntries);

        // getPrevious in the middle and expect the first
        Entry getPreviousResult = clientWithLimitRead
                .getPrevious(notVisibleEntries.get(0).getKey());
        Entry expectedEntry = visibleEntries.get(0);
        assertArrayEquals(expectedEntry.getKey(), getPreviousResult.getKey());
        assertArrayEquals(expectedEntry.getValue(),
                getPreviousResult.getValue());

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
    @Test
    public void testGetKeyRange_Throws_ForStartKeyIsNull()
            throws KineticException {
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

        getClient().put(versioned0, newVersion0);
        getClient().put(versioned1, newVersion1);

        try {
            getClient().getKeyRange(null, true, key1, true, 10);
            fail("start key is null, get range failed");
        } catch (KineticException e) {
            assertNull(e.getMessage());
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
    @Test
    public void testGetKeyRange_Throws_ForEndKeyIsNull()
            throws KineticException {
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

        getClient().put(versioned0, newVersion0);
        getClient().put(versioned1, newVersion1);

        try {
            getClient().getKeyRange(key0, true, null, true, 10);
            fail("end key is null, get range failed");
        } catch (KineticException e) {
            assertNull(e.getMessage());
        }

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are inclusive, but they do not exist in
     * simulator/drive, the result of key list should be any keys that are sorted
     * between them.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test
    public void testGetKeyRange_ReturnsKeysInRange_ForStartKeyInclusiveEndKeyInclusiveWithStartAndEndKeyNotExistInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key008", "value008", getClient());

        // Not present, before first key
        byte[] startKey = toByteArray("key00");
        // Not present, in between two keys
        byte[] endKey = toByteArray("key007");

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

        logger.info(this.testEndInfo());
    }

    /**
     * GetKeyRange, startKey and endKey are exclusive, but they do not exist in
     * simulator/drive, the result of key list should be any keys that are sorted
     * between them.
     * <p>
     *
     * @throws KineticException
     *             if any internal error occurred.
     */
    @Test
    public void testGetetKeyRange_ReturnsKeysInRange_ForStartKeyExclusiveEndKeyExclusiveWithStartAndEndKeyNotExistInDB()
            throws KineticException {

        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key008", "value008", getClient());

        // Not present, before first key
        byte[] startKey = toByteArray("key00");
        // Not present, in between two keys
        byte[] endKey = toByteArray("key007");

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                false, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartAndEndKeyNotExistInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key002");

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                false, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartAndEndKeyNotExistInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = toByteArray("key002");

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                true, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry0.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                false, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry0.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                false, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry0.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry0.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyIsTheSecondKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry1.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyIsTheSecondKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry1.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyIsTheSecondKeyExistsInDB()
            throws UnsupportedEncodingException, KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry1.getKey();

        try {
            List<byte[]> keys = getClient().getKeyRange(startKey, false,
                    endKey, true, 10);
            assertEquals(2, keys.size());
            assertArrayEquals(entry0.getKey(), keys.get(0));
            assertArrayEquals(entry1.getKey(), keys.get(1));
        } catch (KineticException e) {
            fail("get range failed" + e.getMessage());
        }

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyIsTheSecondKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry1.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithEndKeyIsTheLastKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                false, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithEndKeyIsTheLastKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                false, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithEndKeyIsTheLastKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                true, 10);
        assertEquals(3, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));
        assertArrayEquals(entry2.getKey(), keys.get(2));

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithEndKeyIsTheLastKeyExistsInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key00");
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
        assertEquals(3, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));
        assertArrayEquals(entry2.getKey(), keys.get(2));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry0.getKey();
        byte[] endKey = entry1.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                false, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry0.getKey();
        byte[] endKey = entry1.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry0.getKey();
        byte[] endKey = entry1.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry1.getKey(), keys.get(0));

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheSecondKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry0.getKey();
        byte[] endKey = entry1.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry0.getKey();
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry1.getKey(), keys.get(0));

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
    @Test
    public void tesGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry0.getKey();
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                false, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry0.getKey();
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry1.getKey(), keys.get(0));
        assertArrayEquals(entry2.getKey(), keys.get(1));

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheFirstKeyAndEndKeyIsTheLastKeyInDB()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry0.getKey();
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
        assertEquals(3, keys.size());
        assertArrayEquals(entry0.getKey(), keys.get(0));
        assertArrayEquals(entry1.getKey(), keys.get(1));
        assertArrayEquals(entry2.getKey(), keys.get(2));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry1.getKey();
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                false, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry1.getKey();
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry1.getKey(), keys.get(0));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry1.getKey();
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry2.getKey(), keys.get(0));

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheSecondKeyAndEndKeyIsTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        Entry entry1 = buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry1.getKey();
        byte[] endKey = entry2.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
        assertEquals(2, keys.size());
        assertArrayEquals(entry1.getKey(), keys.get(0));
        assertArrayEquals(entry2.getKey(), keys.get(1));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyIsTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry2.getKey();
        byte[] endKey = toByteArray("key09");

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                false, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyIsTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry2.getKey();
        byte[] endKey = toByteArray("key09");

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                false, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry2.getKey(), keys.get(0));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyIsTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry2.getKey();
        byte[] endKey = toByteArray("key09");

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                true, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyIsTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry2.getKey();
        byte[] endKey = toByteArray("key09");

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
        assertEquals(1, keys.size());
        assertArrayEquals(entry2.getKey(), keys.get(0));

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyExclusive_WithStartKeyBiggerThanTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                false, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyExclusive_WithStartKeyBiggerThanTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                false, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyExclusiveEndKeyInclusive_WithStartKeyBiggerThanTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        List<byte[]> keys = getClient().getKeyRange(startKey, false, endKey,
                true, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyBiggerThanTheLastKeyInDB()
            throws KineticException {
        buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ForNoDataInDB() throws KineticException {
        byte[] startKey = toByteArray("key09");
        byte[] endKey = toByteArray("key11");

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
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
    @Test
    public void testGetKeyRange_ForStartKeyInclusiveEndKeyInclusive_WithStartKeyAfterEndKey()
            throws KineticException {
        Entry entry0 = buildAndPutEntry("key005", "value005", getClient());
        buildAndPutEntry("key006", "value006", getClient());
        Entry entry2 = buildAndPutEntry("key007", "value007", getClient());

        byte[] startKey = entry2.getKey();
        byte[] endKey = entry0.getKey();

        List<byte[]> keys = getClient().getKeyRange(startKey, true, endKey,
                true, 10);
        assertEquals(0, keys.size());

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
    @Test
    public void testGetKeyRange_ReturnsFirstNKeysWithRangeRole()
            throws KineticException {
        // Client will not have RANGE on this
        new Entry(toByteArray("k01"), toByteArray("v01"));

        // Client will have RANGE on these
        Entry entry02 = new Entry(toByteArray("k02"), toByteArray("v02"));
        Entry entry03 = new Entry(toByteArray("k03"), toByteArray("v03"));

        // Client will not have RANGE on these
        Entry entry04 = new Entry(toByteArray("k04"), toByteArray("v04"));
        Entry entry05 = new Entry(toByteArray("k05"), toByteArray("v05"));

        // Client will have RANGE on these
        Entry entry06 = new Entry(toByteArray("k06"), toByteArray("v06"));
        new Entry(toByteArray("k07"), toByteArray("v07"));
        Entry entry08 = new Entry(toByteArray("k08"), toByteArray("v08"));

        // Client will not have RANGE on this
        Entry entry09 = new Entry(toByteArray("k09"), toByteArray("v09"));

        Map<List<Entry>, List<Kinetic.Message.Security.ACL.Permission>> entryToRoleMap = Maps
                .newHashMap();
        // Put the first set with Range
        entryToRoleMap.put(Arrays.asList(entry02, entry03), Collections
                .singletonList(Kinetic.Message.Security.ACL.Permission.RANGE));
        // Put the second set, without range, which is a breaking gap
        entryToRoleMap.put(Arrays.asList(entry04, entry05), Collections
                .singletonList(Kinetic.Message.Security.ACL.Permission.READ));
        // Put the third set, also with range, which will not be returned
        // because of the gap
        entryToRoleMap.put(Arrays.asList(entry06, entry08), Collections
                .singletonList(Kinetic.Message.Security.ACL.Permission.RANGE));

        KineticClient clientWithVisibilityGap = createClientWithSpecifiedRolesForEntries(entryToRoleMap);

        List<byte[]> keyRange = clientWithVisibilityGap.getKeyRange(
                entry02.getKey(), true, entry09.getKey(), true, 10);
        assertEquals(2, keyRange.size());
        assertArrayEquals(entry02.getKey(), keyRange.get(0));
        assertArrayEquals(entry03.getKey(), keyRange.get(1));

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
    @Test
    public void testGetKeyRangeReversed_ReturnsLastNKeysWithRangeRole()
            throws KineticException {
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

        Map<List<Entry>, List<Kinetic.Message.Security.ACL.Permission>> entryToRoleMap = Maps
                .newHashMap();
        // Put the first set with Range, which will not be returned because of
        // the gap
        entryToRoleMap.put(Arrays.asList(entry02, entry03), Collections
                .singletonList(Kinetic.Message.Security.ACL.Permission.RANGE));
        // Put the second set, without range, which is a breaking gap
        entryToRoleMap.put(Arrays.asList(entry04, entry05), Collections
                .singletonList(Kinetic.Message.Security.ACL.Permission.READ));
        // Put the third set, also with range, which we expect to be returned
        // (reversed range)
        entryToRoleMap
        .put(Arrays.asList(entry06, entry07, entry08), Collections
                .singletonList(Kinetic.Message.Security.ACL.Permission.RANGE));

        AdvancedKineticClient clientWithVisibilityGap = createClientWithSpecifiedRolesForEntries(entryToRoleMap);

        List<byte[]> keyRange = clientWithVisibilityGap.getKeyRangeReversed(
                entry01.getKey(), true, entry09.getKey(), true, 10);
        assertEquals(3, keyRange.size());
        assertArrayEquals(entry08.getKey(), keyRange.get(0));
        assertArrayEquals(entry07.getKey(), keyRange.get(1));
        assertArrayEquals(entry06.getKey(), keyRange.get(2));

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
            List<Entry> visibleEntries, List<Entry> notVisibleEntries)
                    throws KineticException {
        Map<List<Entry>, List<ACL.Permission>> map = Maps
                .newHashMap();
        map.put(visibleEntries, Collections
                .singletonList(Kinetic.Message.Security.ACL.Permission.READ));
        map.put(notVisibleEntries,
                Collections
                .<Kinetic.Message.Security.ACL.Permission> emptyList());

        return createClientWithSpecifiedRolesForEntries(map);
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
            Map<List<Entry>, List<Kinetic.Message.Security.ACL.Permission>> entriesToRoleMap)
                    throws KineticException {
        // Set up a new client with 2 domains which allow the client to read
        // keys that start with "a" or "c"
        int clientId = 2;
        String clientKeyString = "ClientWhoCannotReadEverything";

        List<Kinetic.Message.Security.ACL.Scope> domains = Lists
                .newArrayList();

        for (Map.Entry<List<Entry>, List<Kinetic.Message.Security.ACL.Permission>> listListEntry : entriesToRoleMap
                .entrySet()) {
            domains.addAll(putEntriesAndGetDomains(listListEntry.getKey(),
                    listListEntry.getValue()));
        }

        createClientAclWithDomains(clientId, clientKeyString, domains);

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
    private List<Kinetic.Message.Security.ACL.Scope> putEntriesAndGetDomains(
            List<Entry> entriesToPut, List<Permission> rolesToAdd)
                    throws KineticException {
        List<Kinetic.Message.Security.ACL.Scope> domains = Lists
                .newArrayList();
        for (Entry entry : entriesToPut) {
            getClient().put(entry, null);

            if (!rolesToAdd.isEmpty()) {
                // Create a domain that allows the given role for this entry's
                // key
                Kinetic.Message.Security.ACL.Scope.Builder domain = Kinetic.Message.Security.ACL.Scope
                        .newBuilder();
                for (Kinetic.Message.Security.ACL.Permission role : rolesToAdd) {
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
    @Test
    public void testGetRangeExceedMaxSize() {
        
        byte[] key0 = toByteArray("key00000000000");
        byte[] key1 = toByteArray("key00000000001");
        
        try {
            int max = SimulatorConfiguration.getMaxSupportedKeyRangeSize();
            getClient().getKeyRange(key0, true, key1, true,  (max +1));
            fail("did not receive expected exception: request key range exceeds max allowed size " + max);
        } catch (KineticException e) {
            logger.info("caught expected exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }
    
    /**
     * Test max key request size cannot exceed max supported size (4096).
     */
    @Test
    public void testMaxKeyLength() {
        
        int size = SimulatorConfiguration.getMaxSupportedKeySize();
        byte[] key0 = new byte[size];
        try {
            getClient().get(key0);    
        } catch (KineticException e) {
            fail("received unexpected exception: " + e);
        }
        
        byte[] key1 = new byte[size+1];
        
        try {
            getClient().get(key1);
            fail("did not receive expected exception: request key exceeds max allowed size " + size);
        } catch (KineticException e) {
            logger.info("caught expected exception: " + e.getMessage());
        }

        logger.info(this.testEndInfo());
    }
    
    /**
     * Test max version length cannot exceed max supported size (2048).
     */
    @Test
    public void testMaxVersionLength() {
        
        byte[] key = toByteArray("key00000000000");
        byte[] value = toByteArray("value00000000000");
        
        int vlen = SimulatorConfiguration.getMaxSupportedVersionSize();
        
        byte[] version = new byte[vlen];
        Entry entry = new Entry();
        entry.setKey(key);
        entry.setValue(value);
        entry.getEntryMetadata().setVersion(version);
        
        // expect to succeed - allowed version size
        try {
            getClient().putForced(entry);
        } catch (KineticException e) {
            fail("received unexpected exception: " + e);
        }
        
        //expect to fail: exceed max version size to put
        try {
            byte[] version2 = new byte[vlen + 1];
            entry.getEntryMetadata().setVersion(version2);
            
            getClient().putForced(entry);
            fail("did not receive expected exception: request key exceeds max allowed size " + vlen);
        } catch (KineticException e) {
            logger.info("caught expected exception: " + e.getMessage());
        }
        
        //expect fail to delete: exceed max version size
        try {
            byte[] version2 = new byte[vlen + 1];
            entry.getEntryMetadata().setVersion(version2);
           
            getClient().delete(entry);
            fail("did not receive expected exception: request key exceeds max allowed size " + vlen);
        } catch (KineticException e) {
            logger.info("caught expected exception: " + e.getMessage());
        }
        
        //expect succeed to delete.
        try {
            
            entry.getEntryMetadata().setVersion(version);
            //expect succeed
            boolean deleted = getClient().delete(entry);
            
            assertTrue (deleted);
        } catch (KineticException e) {
            fail("received unexpected exception: " + e);
        }
        
        logger.info(this.testEndInfo());
    }
}
