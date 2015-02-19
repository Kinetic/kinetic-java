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
package com.seagate.kinetic;

import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;

import org.testng.Assert;
import org.testng.AssertJUnit;

import com.google.common.collect.Lists;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic;

/**
 * Kinetic test assertions utility.
 * <p>
 * Assertions utility used by test case.
 * <p>
 * 
 */
public class KineticAssertions {
	private KineticAssertions() {

	}

	/**
	 * Assert response message status is success.
	 * <p>
	 */
	public static void assertSuccess(KineticMessage response) {
		assertStatus(Kinetic.Command.Status.StatusCode.SUCCESS, response);
	}

	/**
	 * Assert response message status is equals as expected.
	 * <p>
	 */
	public static void assertStatus(Kinetic.Command.Status.StatusCode expected,
			KineticMessage response) {
		AssertJUnit.assertEquals(String.format("Message %s status is not %s", response,
				expected), expected, response.getCommand().getStatus()
				.getCode());
	}

	/**
	 * Assert the result of get key is not found.
	 * <p>
	 */
	public static void assertKeyNotFound(KineticClient client, byte[] key)
			throws KineticException {
		AssertJUnit.assertNull(String.format("Expected key <%s> to not exist", new String(
				key, Charset.forName("UTF-8"))), client.get(key));
	}

	/**
	 * Assert the entry is equals as expected, not including metadata comparing.
	 * <p>
	 */
	public static void assertEntryEquals(byte[] expectedKey,
			byte[] expectedValue, byte[] expectedVersion, Entry actual) {
		assertArrayEquals("Entry key mismatch", expectedKey, actual.getKey());
		assertArrayEquals("Entry value mismatch", expectedValue,
				actual.getValue());
		assertArrayEquals("Entry version mismatch", expectedVersion, actual
				.getEntryMetadata().getVersion());
	}

	/**
	 * Assert the entry is equals as expected, not including metadata comparing.
	 * <p>
	 */
	public static void assertEntryEquals(Entry expected, Entry actual) {
		assertEntryEquals(expected.getKey(), expected.getValue(), expected
				.getEntryMetadata().getVersion(), actual);
	}

	/**
	 * Assert two array list content are equals.
	 * <p>
	 */
	public static void assertListOfArraysEqual(Iterable<byte[]> expected,
			Iterable<byte[]> actual) {

        if (expected == null && actual == null) {
            return;
        }

        if (expected == null) {
            Assert.fail("Expected null but was " + actual);
        }

        if (actual == null) {
            Assert.fail("Expected " + expected + " but was null");
        }

        ArrayList<byte[]> expectedList = Lists.newArrayList(expected);
        ArrayList<byte[]> actualList = Lists.newArrayList(actual);

        AssertJUnit.assertEquals("Expected " + expectedList + " and actual "
                + actual + " are different sizes", expectedList.size(),
                actualList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            byte[] expectedEntry = expectedList.get(i);
            byte[] actualEntry = actualList.get(i);
            boolean areEqual = Arrays.equals(expectedEntry, actualEntry);
            AssertJUnit.assertTrue(String.format(
                    "Difference at index %d: %s != %s", i, expectedEntry,
                    actualEntry), areEqual);
        }

	}

	/**
	 * Assert two entry list content are equals.
	 * <p>
	 */
	public static void assertListOfEntriesEqual(Iterable<Entry> expected,
			Iterable<Entry> actual) {

        if (expected == null && actual == null) {
            return;
        }

        if (expected == null) {
            Assert.fail("Expected null but was " + actual);
        }

        if (actual == null) {
            Assert.fail("Expected " + expected + " but was null");
        }

        ArrayList<Entry> expectedList = Lists.newArrayList(expected);
        ArrayList<Entry> actualList = Lists.newArrayList(actual);

        AssertJUnit.assertEquals("Expected " + expectedList + " and actual "
                + actual + " are different sizes", expectedList.size(),
                actualList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            Entry expectedEntry = expectedList.get(i);
            Entry actualEntry = actualList.get(i);

            boolean areEqual = Arrays.equals(expectedEntry.getKey(),
                    actualEntry.getKey())
                    && Arrays.equals(expectedEntry.getValue(),
                            actualEntry.getValue())
                    && Arrays.equals(expectedEntry.getEntryMetadata()
                            .getVersion(), actualEntry.getEntryMetadata()
                            .getVersion());

            AssertJUnit.assertTrue(String.format(
                    "Difference at index %d: %s != %s", i, expectedEntry,
                    actualEntry), areEqual);
        }
	}

	/**
	 * Assert two object list content are equals.
	 * <p>
	 */
	public static <T> void assertListOfObjectsEqual(Iterable<T> expected,
			Iterable<T> actual, Comparator<T> comparator) {
		if (expected == null && actual == null) {
			return;
		}

		if (expected == null) {
			Assert.fail("Expected null but was " + actual);
		}

		if (actual == null) {
			Assert.fail("Expected " + expected + " but was null");
		}

		ArrayList<T> expectedList = Lists.newArrayList(expected);
		ArrayList<T> actualList = Lists.newArrayList(actual);

		AssertJUnit.assertEquals("Expected " + expectedList + " and actual " + actual
				+ " are different sizes", expectedList.size(),
				actualList.size());

		for (int i = 0; i < expectedList.size(); i++) {
			T expectedEntry = expectedList.get(i);
			T actualEntry = actualList.get(i);
			boolean areEqual = comparator.compare(expectedEntry, actualEntry) == 0;
			AssertJUnit.assertTrue(String.format("Difference at index %d: %s != %s", i,
					expectedEntry, actualEntry), areEqual);
		}
	}
}
