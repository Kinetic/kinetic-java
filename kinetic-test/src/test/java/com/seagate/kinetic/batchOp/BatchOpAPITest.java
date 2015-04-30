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
package com.seagate.kinetic.batchOp;

import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kinetic.client.BatchOperation;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

/**
 * Kinetic Client batch operation API.
 * <p>
 * Batch operation API include:
 * <p>
 * putAsync(Entry entry, byte[] newVersion, CallbackHandler<Entry> handler)
 * <p>
 * putForcedAsync(Entry entry, CallbackHandler<Entry> handler)
 * <p>
 * deleteAsync(Entry entry, CallbackHandler<Boolean> handler)
 * <p>
 * deleteForcedAsync(byte[] key, CallbackHandler<Boolean> handler)
 * <p>
 * commit()
 * <p>
 * 
 * @see KineticClient
 * @see BatchOperation
 * 
 */

@Test(groups = { "simulator" })
public class BatchOpAPITest extends IntegrationTestCase {

    private final int valueSize = 1024 * 1024;

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PutsForcedAsyncSucceeds(String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            batch.putForced(foo);
            batch.putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception: " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch commit throw exception: " + e.getMessage());
        }

        // get foo, expect to find it
        Entry fooGet = null;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(foo.getValue(), fooGet.getValue()));
            assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                    fooGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        // get bar, expect to find it
        Entry barGet = null;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                    barGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PutsAsyncSucceeds(String clientName) {
        Entry bar = getBarEntry();
        bar.getEntryMetadata().setVersion(null);
        Entry foo = getFooEntry();
        foo.getEntryMetadata().setVersion(null);
        byte[] newVersion = toByteArray("5678");

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            batch.put(foo, newVersion);
            batch.put(bar, newVersion);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception: " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch commit throw exception: " + e.getMessage());
        }

        // get foo, expect to find it
        Entry fooGet = null;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(foo.getValue(), fooGet.getValue()));
            assertTrue(Arrays.equals(newVersion, fooGet.getEntryMetadata()
                    .getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        // get bar, expect to find it
        Entry barGet = null;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(newVersion, barGet.getEntryMetadata()
                    .getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PutsAsyncOneFailedOnePutAsyncSuccess_AllFailed(
            String clientName) {
        Entry bar = getBarEntry();
        bar.getEntryMetadata().setVersion(null);
        Entry foo = getFooEntry();
        foo.getEntryMetadata().setVersion(null);
        byte[] newVersion = toByteArray("5678");

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            batch.put(foo, newVersion);

            bar.getEntryMetadata().setVersion(newVersion);
            batch.put(bar, newVersion);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception: " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.INVALID_BATCH));
        }

        // get foo, expect to find null
        Entry fooGet = null;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        // get bar, expect to find null
        Entry barGet = null;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertNull(barGet);
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PutsAsyncAndPutForcedAsyncSucceeds(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();
        foo.getEntryMetadata().setVersion(null);
        byte[] newVersion = toByteArray("5678");

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            batch.putForced(bar);
            batch.put(foo, newVersion);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception: " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch commit throw exception: " + e.getMessage());
        }

        // get foo, expect to find it
        Entry fooGet = null;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(foo.getValue(), fooGet.getValue()));
            assertTrue(Arrays.equals(newVersion, fooGet.getEntryMetadata()
                    .getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        // get bar, expect to find it
        Entry barGet = null;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                    barGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PutsAsyncOneFailedOnePutForcedAsyncSuccess_AllFailed(
            String clientName) {
        Entry bar = getBarEntry();
        bar.getEntryMetadata().setVersion(null);
        Entry foo = getFooEntry();
        byte[] newVersion = toByteArray("5678");

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            batch.putForced(foo);

            bar.getEntryMetadata().setVersion(newVersion);
            batch.put(bar, newVersion);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception: " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.INVALID_BATCH));
        }

        // get foo, expect to find null
        Entry fooGet = null;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        // get bar, expect to find null
        Entry barGet = null;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertNull(barGet);
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_DeletesAsyncSucceeds(String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
            getClient(clientName).putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put entry throw exception. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            batch.delete(bar);
            batch.delete(foo);
        } catch (KineticException e) {
            Assert.fail("Delete async operation throw exception: "
                    + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch commit operation throw exception. "
                    + e.getMessage());
        }

        // get foo, expect to null
        try {
            Entry fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get foo entry throw exception. " + e.getMessage());
        }

        // get bar, expect to null
        try {
            Entry barGet = getClient(clientName).get(bar.getKey());
            assertNull(barGet);
        } catch (KineticException e) {
            Assert.fail("Get bar entry throw exception. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_DeletesAsyncOneFailed_AllFailed(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
            getClient(clientName).putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put entry throw exception. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            bar.getEntryMetadata().setVersion(toByteArray("NoMatchDbVersion"));
            batch.delete(bar);

            batch.delete(foo);
        } catch (KineticException e) {
            Assert.fail("Delete async operation throw exception: "
                    + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.INVALID_BATCH));
        }

        // get foo, expect to find it
        Entry fooGet = null;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(foo.getValue(), fooGet.getValue()));
            assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                    fooGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        // get bar, expect to find it
        Entry barGet = null;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(toByteArray("1234"), barGet
                    .getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry foo throw exception: " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_DeletesForcedAsyncSucceeds(String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
            getClient(clientName).putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put entry throw exception. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {

            batch.deleteForced(bar.getKey());
            batch.deleteForced(foo.getKey());
        } catch (KineticException e) {
            Assert.fail("Delete async operation throw exception: "
                    + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch commit operation throw exception. "
                    + e.getMessage());
        }

        // get foo, expect to null
        try {
            Entry fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get foo entry throw exception. " + e.getMessage());
        }

        // get bar, expect to null
        try {
            Entry barGet = getClient(clientName).get(bar.getKey());
            assertNull(barGet);
        } catch (KineticException e) {
            Assert.fail("Get bar entry throw exception. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_DeleteForcedAsyncAndDeleteAsyncSucceeds(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
            getClient(clientName).putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put entry throw exception. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            batch.delete(bar);
            batch.deleteForced(foo.getKey());
        } catch (KineticException e) {
            Assert.fail("Delete async operation throw exception: "
                    + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch commit operation throw exception. "
                    + e.getMessage());
        }

        // get foo, expect to null
        try {
            Entry fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get foo entry throw exception. " + e.getMessage());
        }

        // get bar, expect to null
        try {
            Entry barGet = getClient(clientName).get(bar.getKey());
            assertNull(barGet);
        } catch (KineticException e) {
            Assert.fail("Get bar entry throw exception. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_DeletesAsyncOneFailed_OneDeleteForcedAsyncSuccess_AllFailed(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
            getClient(clientName).putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put entry throw exception. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            batch.deleteForced(bar.getKey());

            foo.getEntryMetadata().setVersion(toByteArray("NoMatchDBVersion"));
            batch.delete(foo);
        } catch (KineticException e) {
            Assert.fail("Delete async operation throw exception: "
                    + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.INVALID_BATCH));
        }

        // get foo, expect to find it
        Entry fooGet = null;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(foo.getValue(), fooGet.getValue()));
            assertTrue(Arrays.equals(toByteArray("1234"), fooGet
                    .getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry throw exception: " + e.getMessage());
        }

        // get bar, expect to find it
        Entry barGet = null;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                    barGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry throw exception: " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testBatchOperation_PutAndDeleteSucceeds(String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();
        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        byte[] newVersion = toByteArray("5678");

        try {
            batch.putForced(foo);
            batch.put(foo, newVersion);
        } catch (KineticException e) {
            Assert.fail("Put entry throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch commit throw exception. " + e.getMessage());
        }

        // get foo, expect to find it
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(foo.getValue(), fooGet.getValue()));
            assertTrue(Arrays.equals(newVersion, fooGet.getEntryMetadata()
                    .getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get foo throw exception. " + e.getMessage());
        }

        // get bar, expect to null
        Entry barGet;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertNull(barGet);
        } catch (KineticException e) {
            Assert.fail("Get bar throw exception. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PutAndDelete_WithBigValue_Succeeds(
            String clientName) {
        Entry bar = getBarEntry();
        byte[] barBigValue = ByteBuffer.allocate(valueSize).array();
        bar.setValue(barBigValue);

        Entry foo = getFooEntry();
        foo.getEntryMetadata().setVersion(null);
        byte[] newVersion = toByteArray("5678");
        byte[] fooBigValue = ByteBuffer.allocate(valueSize).array();
        foo.setValue(fooBigValue);

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            batch.put(foo, newVersion);
        } catch (KineticException e) {
            Assert.fail("Put entry throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch commit throw exception. " + e.getMessage());
        }

        // get foo, expect to find it
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(fooBigValue, fooGet.getValue()));
            assertTrue(Arrays.equals(newVersion, fooGet.getEntryMetadata()
                    .getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get foo throw exception. " + e.getMessage());
        }

        // get bar, expect to null
        Entry barGet;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertNull(barGet);
        } catch (KineticException e) {
            Assert.fail("Get bar throw exception. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testBatchOperation_PutAndDeleteForcedSucceeds(String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();
        byte[] newVersion = toByteArray("5678");

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            batch.putForced(foo);
            batch.put(foo, newVersion);
        } catch (KineticException e) {
            Assert.fail("Put entry throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
            batch.deleteForced(foo.getKey());
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch commit throw exception. " + e.getMessage());
        }

        // get foo, expect to find it
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get foo throw exception. " + e.getMessage());
        }

        // get bar, expect to null
        Entry barGet;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertNull(barGet);
        } catch (KineticException e) {
            Assert.fail("Get bar throw exception. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PutAndDeleteForcedPartiallyFailed_AllFailed(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();
        byte[] newVersion = toByteArray("5678");

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            batch.putForced(foo);
            foo.getEntryMetadata().setVersion(toByteArray("NoMatchDbVersion"));
            batch.put(foo, newVersion);

        } catch (KineticException e) {
            Assert.fail("Put async throw exception. " + e.getMessage());
        }

        try {
            bar.getEntryMetadata().setVersion(toByteArray("NoMatchDbVersion"));
            batch.delete(bar);
            batch.deleteForced(foo.getKey());
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.INVALID_BATCH));
        }

        // get foo, expect to find null
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get foo throw exception. " + e.getMessage());
        }

        // get bar, expect to find it
        Entry barGet;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(toByteArray("1234"), barGet
                    .getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get bar throw exception. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PutAndDeleteForcedAllOperationFailed_AllFailed(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();
        byte[] newVersion = toByteArray("5678");

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            foo.getEntryMetadata().setVersion(toByteArray("NoMatchDbVersion"));
            batch.put(foo, newVersion);

        } catch (KineticException e) {
            Assert.fail("Put entry throw exception. " + e.getMessage());
        }

        try {
            bar.getEntryMetadata().setVersion(toByteArray("NoMatchDbVersion"));
            batch.delete(bar);

            foo.getEntryMetadata().setVersion(toByteArray("NoMatchDbVersion"));
            batch.delete(foo);
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.INVALID_BATCH));
        }

        // get foo, expect to find null
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get foo throw exception. " + e.getMessage());
        }

        // get bar, expect to find it
        Entry barGet;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(toByteArray("1234"), barGet
                    .getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get bar throw exception. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_AllOperationSuccess_AbortOperation_Succeeds(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put operation failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            batch.putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put operation throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete operation throw exception. " + e.getMessage());
        }

        try {
            batch.abort();
        } catch (KineticException e) {
            Assert.fail("Abort operation throw exception. " + e.getMessage());
        }

        // get foo, expect to null
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        // get bar, expect to null
        Entry barGet;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                    barGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PartiallyOperationFailed_AbortOperation_Succeeds(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put operation failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            batch.putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put operation throw exception. " + e.getMessage());
        }

        try {
            bar.getEntryMetadata().setVersion(toByteArray("NoMatchDbVersion"));
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete operation throw exception. " + e.getMessage());
        }

        try {
            batch.abort();
        } catch (KineticException e) {
            Assert.fail("Abort operation throw exception. " + e.getMessage());
        }

        // get foo, expect to null
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        // get bar, expect to find it
        Entry barGet;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(toByteArray("1234"), barGet
                    .getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_AllOperationFailed_AbortOperation_Succeeds(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put operation failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation failed. " + e.getMessage());
        }

        try {
            byte[] newVersion = toByteArray("5678");
            batch.put(foo, newVersion);
        } catch (KineticException e) {
            Assert.fail("Put operation throw exception. " + e.getMessage());
        }

        try {
            bar.getEntryMetadata().setVersion(toByteArray("NoMatchDbVersion"));
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete operation throw exception. " + e.getMessage());
        }

        try {
            batch.abort();
        } catch (KineticException e) {
            Assert.fail("Abort operation throw exception. " + e.getMessage());
        }

        // get foo, expect to null
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertNull(fooGet);
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        // get bar, expect to find it
        Entry barGet;
        try {
            barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(toByteArray("1234"), barGet
                    .getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_FollowedBothGetAndPutByOneClient_PutSuccess(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            batch.putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        ClientConfiguration cc = kineticClientConfigutations.get(clientName);
        KineticClient client1 = null;
        try {
            client1 = KineticClientFactory.createInstance(cc);
        } catch (KineticException e) {
            Assert.fail("Create a new client throw exception. "
                    + e.getMessage());
        }
        try {
            assertNull(client1.get(foo.getKey()));
        } catch (KineticException e) {
            Assert.fail("Another connection can not operate before batch operation end. "
                    + e.getMessage());
        }

        byte[] newFooVersion = null;
        byte[] newValue = null;
        try {
            newValue = toByteArray("newValue");
            foo.setValue(newValue);
            foo.getEntryMetadata().setVersion(null);
            newFooVersion = toByteArray("newVersion");

            client1.put(foo, newFooVersion);
            assertTrue(Arrays.equals(newValue, client1.get(foo.getKey())
                    .getValue()));
            assertTrue(Arrays.equals(newFooVersion, client1.get(foo.getKey())
                    .getEntryMetadata().getVersion()));

        } catch (KineticException e1) {
        }

        try {
            Entry barGet = client1.get(bar.getKey());
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                    barGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Another connection can not operate before batch operation end. "
                    + e.getMessage());
        } finally {
            try {
                client1.close();
            } catch (KineticException e) {
                Assert.fail("Another connection close throw exception. "
                        + e.getMessage());
            }
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch operation commit throw exception. "
                    + e.getMessage());
        }

        // get foo, expect to find it
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays
                    .equals(toByteArray("foovalue"), fooGet.getValue()));
            assertTrue(Arrays.equals(toByteArray("1234"), fooGet
                    .getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        // get bar, expect to null
        try {
            assertNull(getClient(clientName).get(bar.getKey()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_FollowedBothReadByOneClientAfterEndBatch_Success(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            batch.putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch operation commit throw exception. "
                    + e.getMessage());
        }

        ClientConfiguration cc = kineticClientConfigutations.get(clientName);
        KineticClient client1 = null;
        try {
            client1 = KineticClientFactory.createInstance(cc);
        } catch (KineticException e) {
            Assert.fail("Create a new client throw exception. "
                    + e.getMessage());
        }
        try {
            client1.get(foo.getKey());
        } catch (KineticException e) {
            Assert.fail("Another connection can not operate before batch operation end. "
                    + e.getMessage());
        }

        try {
            client1.get(bar.getKey());
        } catch (KineticException e) {
            Assert.fail("Another connection can not operate before batch operation end. "
                    + e.getMessage());
        } finally {
            try {
                client1.close();
            } catch (KineticException e) {
                Assert.fail("Another connection close throw exception. "
                        + e.getMessage());
            }
        }

        // get foo, expect to find it
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(foo.getValue(), fooGet.getValue()));
            assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                    fooGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        // get bar, expect to null
        try {
            assertNull(getClient(clientName).get(bar.getKey()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_FollowedBothGetAndPutByOneClient_PutFailed(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            batch.putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        ClientConfiguration cc = kineticClientConfigutations.get(clientName);
        KineticClient client1 = null;
        try {
            client1 = KineticClientFactory.createInstance(cc);
        } catch (KineticException e) {
            Assert.fail("Create a new client throw exception. "
                    + e.getMessage());
        }
        try {
            assertNull(client1.get(foo.getKey()));
        } catch (KineticException e) {
            Assert.fail("Another connection can not operate before batch operation end. "
                    + e.getMessage());
        }

        byte[] newFooVersion = null;
        byte[] newValue = null;
        try {
            newValue = toByteArray("newValue");
            foo.setValue(newValue);
            foo.getEntryMetadata().setVersion(toByteArray("1111"));
            newFooVersion = toByteArray("newVersion");

            client1.put(foo, newFooVersion);

        } catch (KineticException e1) {
            assertTrue(e1.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.VERSION_MISMATCH));
        }

        try {
            Entry barGet = client1.get(bar.getKey());
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                    barGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Another connection can not operate before batch operation end. "
                    + e.getMessage());
        } finally {
            try {
                client1.close();
            } catch (KineticException e) {
                Assert.fail("Another connection close throw exception. "
                        + e.getMessage());
            }
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch operation commit throw exception. "
                    + e.getMessage());
        }

        // get foo, expect to find it
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays
                    .equals(toByteArray("foovalue"), fooGet.getValue()));
            assertTrue(Arrays.equals(toByteArray("1234"), fooGet
                    .getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        // get bar, expect to null
        try {
            assertNull(getClient(clientName).get(bar.getKey()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_FollowedBothGetAndPutByOneClient_PutAsyncAndPutSuccess(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            foo.getEntryMetadata().setVersion(toByteArray("1111"));
            foo.setValue(toByteArray("newfoovalue"));
            byte[] newVersion = toByteArray("3333");
            batch.put(foo, newVersion);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        ClientConfiguration cc = kineticClientConfigutations.get(clientName);
        KineticClient client1 = null;
        try {
            client1 = KineticClientFactory.createInstance(cc);
        } catch (KineticException e) {
            Assert.fail("Create a new client throw exception. "
                    + e.getMessage());
        }
        try {
            assertNull(client1.get(foo.getKey()));
        } catch (KineticException e) {
            Assert.fail("Another connection can not operate before batch operation end. "
                    + e.getMessage());
        }

        byte[] newFooVersion = null;
        byte[] newValue = null;
        try {
            newValue = toByteArray("newValue");
            foo.setValue(newValue);
            foo.getEntryMetadata().setVersion(null);
            newFooVersion = toByteArray("1111");

            client1.put(foo, newFooVersion);

        } catch (KineticException e1) {
        }

        try {
            Entry barGet = client1.get(bar.getKey());
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                    barGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Another connection can not operate before batch operation end. "
                    + e.getMessage());
        } finally {
            try {
                client1.close();
            } catch (KineticException e) {
                Assert.fail("Another connection close throw exception. "
                        + e.getMessage());
            }
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch operation commit throw exception. "
                    + e.getMessage());
        }

        // get foo, expect to find it
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(toByteArray("newfoovalue"),
                    fooGet.getValue()));
            assertTrue(Arrays.equals(toByteArray("3333"), fooGet
                    .getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        // get bar, expect to null
        try {
            assertNull(getClient(clientName).get(bar.getKey()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_FollowedBothGetAndPutByOneClient_PutAsyncFailed(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            batch.put(foo, toByteArray("2222"));
        } catch (KineticException e) {
            Assert.fail("Put async throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        ClientConfiguration cc = kineticClientConfigutations.get(clientName);
        KineticClient client1 = null;
        try {
            client1 = KineticClientFactory.createInstance(cc);
        } catch (KineticException e) {
            Assert.fail("Create a new client throw exception. "
                    + e.getMessage());
        }
        try {
            assertNull(client1.get(foo.getKey()));
        } catch (KineticException e) {
            Assert.fail("Another connection can not operate before batch operation end. "
                    + e.getMessage());
        }

        try {
            Entry barGet = client1.get(bar.getKey());
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                    barGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Another connection can not operate before batch operation end. "
                    + e.getMessage());
        }

        byte[] newFooVersion = null;
        byte[] newValue = null;
        try {
            newValue = toByteArray("newValue");
            foo.setValue(newValue);
            foo.getEntryMetadata().setVersion(null);
            newFooVersion = toByteArray("1111");

            client1.put(foo, newFooVersion);
            assertTrue(Arrays.equals(newValue, client1.get(foo.getKey())
                    .getValue()));
            assertTrue(Arrays.equals(newFooVersion, client1.get(foo.getKey())
                    .getEntryMetadata().getVersion()));

        } catch (KineticException e1) {
        } finally {
            try {
                client1.close();
            } catch (KineticException e) {
                Assert.fail("Another connection close throw exception. "
                        + e.getMessage());
            }
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            assertTrue(e.getResponseMessage().getCommand().getStatus()
                    .getCode().equals(StatusCode.INVALID_BATCH));
        }

        // get foo, expect to find it
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(newValue, fooGet.getValue()));
            assertTrue(Arrays.equals(newFooVersion, fooGet.getEntryMetadata()
                    .getVersion()));

        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        // get bar, expect to null
        try {
            Entry barGet = getClient(clientName).get(bar.getKey());
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                    barGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_FollowedBothReadByTwoClientAfterBatchCommit_Success(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            batch.putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch operation commit throw exception. "
                    + e.getMessage());
        }

        ClientConfiguration cc = kineticClientConfigutations.get(clientName);
        KineticClient client1 = null;
        KineticClient client2 = null;
        try {
            client1 = KineticClientFactory.createInstance(cc);
            client2 = KineticClientFactory.createInstance(cc);
        } catch (KineticException e) {
            Assert.fail("Create two clients failed. " + e.getMessage());
        }

        try {
            assertTrue(Arrays.equals(foo.getKey(), client1.get(foo.getKey())
                    .getKey()));
            assertTrue(Arrays.equals(foo.getValue(), client1.get(foo.getKey())
                    .getValue()));
            assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                    client1.get(foo.getKey()).getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Should not thrown exception. " + e.getMessage());
        } finally {
            try {
                client1.close();
            } catch (KineticException e) {
                Assert.fail("Close connetction failed. " + e.getMessage());
            }
        }

        try {
            assertNull(client2.get(bar.getKey()));
        } catch (KineticException e) {
            Assert.fail("Should not thrown exception. " + e.getMessage());
        } finally {
            try {
                client2.close();
            } catch (KineticException e) {
                Assert.fail("Close connetction failed. " + e.getMessage());
            }
        }

        // get foo, expect to find it
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(foo.getValue(), fooGet.getValue()));
            assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                    fooGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        // get bar, expect to null
        try {
            assertNull(getClient(clientName).get(bar.getKey()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_Succeeds_FollowedSingleReadByTwoClient_BeforeBatchCommit_Success(
            String clientName) {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            getClient(clientName).putForced(bar);
        } catch (KineticException e) {
            Assert.fail("Put entry failed. " + e.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = getClient(clientName).createBatchOperation();
        } catch (KineticException e) {
            Assert.fail("Create batch operation throw exception. "
                    + e.getMessage());
        }

        try {
            batch.putForced(foo);
        } catch (KineticException e) {
            Assert.fail("Put async throw exception. " + e.getMessage());
        }

        try {
            batch.delete(bar);
        } catch (KineticException e) {
            Assert.fail("Delete async throw exception. " + e.getMessage());
        }

        ClientConfiguration cc = kineticClientConfigutations.get(clientName);
        KineticClient client1 = null;
        KineticClient client2 = null;
        try {
            client1 = KineticClientFactory.createInstance(cc);
            client2 = KineticClientFactory.createInstance(cc);
        } catch (KineticException e) {
            Assert.fail("Create two clients failed. " + e.getMessage());
        }

        try {
            Entry barGet = client1.get(bar.getKey());
            assertTrue(Arrays.equals(bar.getKey(), barGet.getKey()));
            assertTrue(Arrays.equals(bar.getValue(), barGet.getValue()));
            assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                    barGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Should not thrown exception. " + e.getMessage());
        } finally {
            try {
                client1.close();
            } catch (KineticException e) {
                Assert.fail("Close connetction failed. " + e.getMessage());
            }
        }

        try {
            assertNull(client2.get(foo.getKey()));
        } catch (KineticException e) {
            Assert.fail("Should not thrown exception. " + e.getMessage());
        } finally {
            try {
                client2.close();
            } catch (KineticException e) {
                Assert.fail("Close connetction failed. " + e.getMessage());
            }
        }

        try {
            batch.commit();
        } catch (KineticException e) {
            Assert.fail("Batch operation commit throw exception. "
                    + e.getMessage());
        }

        // get foo, expect to find it
        Entry fooGet;
        try {
            fooGet = getClient(clientName).get(foo.getKey());
            assertTrue(Arrays.equals(foo.getKey(), fooGet.getKey()));
            assertTrue(Arrays.equals(foo.getValue(), fooGet.getValue()));
            assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                    fooGet.getEntryMetadata().getVersion()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        // get bar, expect to null
        try {
            assertNull(getClient(clientName).get(bar.getKey()));
        } catch (KineticException e) {
            Assert.fail("Get entry failed. " + e.getMessage());
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperationInLoop_OneClient_CommitBatch_DifferentKey(
            String clientName) {
        Entry foo = getFooEntry();
        Entry bar = getBarEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        int loopCount = 4;
        KineticClient client = getClient(clientName);
        for (int i = 0; i < loopCount; i++) {
            byte[] fooKey = toByteArray("foo" + i);
            try {
                client.deleteForced(fooKey);
            } catch (KineticException e) {
                Assert.fail("clean data failed. " + e.getMessage());
            }
        }

        for (int i = 0; i < loopCount; i++) {
            byte[] fooKey = toByteArray("foo" + i);
            byte[] fooValue = ByteBuffer.allocate(valueSize).array();
            byte[] fooVersion = toByteArray("v" + i);
            foo.setKey(fooKey);
            foo.setValue(fooValue);
            foo.getEntryMetadata().setVersion(fooVersion);

            byte[] barKey = toByteArray("bar" + i);
            bar.setKey(barKey);

            try {
                client.putForced(bar);
            } catch (KineticException e2) {
                Assert.fail("Put bar throw exception: " + e2.getMessage());
            }

            BatchOperation batch = null;
            try {
                batch = client.createBatchOperation();
            } catch (KineticException e) {
                Assert.fail("Create batch operation failed. " + e.getMessage());
            }

            try {
                batch.putForced(foo);
            } catch (KineticException e1) {
                Assert.fail("Put async batch op throw exception. "
                        + e1.getMessage());
            }

            try {
                batch.delete(bar);
            } catch (KineticException e1) {
                Assert.fail("Delete async batch op throw exception. "
                        + e1.getMessage());
            }

            try {
                batch.commit();
            } catch (KineticException e1) {
                Assert.fail("Batch commit throw exception. " + e1.getMessage());
            }

            try {
                Entry fooGet = getClient(clientName).get(fooKey);
                assertTrue(Arrays.equals(fooGet.getKey(), fooKey));
                assertTrue(Arrays.equals(fooGet.getValue(), fooValue));
                assertTrue(Arrays.equals(
                        fooGet.getEntryMetadata().getVersion(), fooVersion));
            } catch (KineticException e1) {
                Assert.fail("Get foo throw exception. " + e1.getMessage());
            }

            try {
                Entry barGet = getClient(clientName).get(barKey);
                assertNull(barGet);
            } catch (KineticException e1) {
                Assert.fail("Get bar throw exception. " + e1.getMessage());
            }

            for (int j = 0; j < loopCount; j++) {
                byte[] fooKeyD = toByteArray("foo" + j);
                try {
                    client.deleteForced(fooKeyD);
                } catch (KineticException e) {
                    Assert.fail("clean data failed. " + e.getMessage());
                }
            }

            try {
                cleanEntry(bar, getClient(clientName));
                cleanEntry(foo, getClient(clientName));
            } catch (KineticException e) {
                Assert.fail("Clean entry failed. " + e.getMessage());
            }
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperationInLoop_OneClient_CommitBatch_SameKey(
            String clientName) {
        Entry foo = getFooEntry();
        Entry bar = getBarEntry();

        int loopCount = 4;
        KineticClient client = getClient(clientName);

        try {
            cleanEntry(bar, client);
            cleanEntry(foo, client);
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        for (int i = 0; i < loopCount; i++) {
            byte[] fooValue = ByteBuffer.allocate(valueSize).array();
            byte[] fooVersion = toByteArray("v" + i);
            foo.setValue(fooValue);
            foo.getEntryMetadata().setVersion(fooVersion);

            try {
                client.putForced(bar);
            } catch (KineticException e2) {
                Assert.fail("Put bar throw exception: " + e2.getMessage());
            }

            BatchOperation batch = null;
            try {
                batch = client.createBatchOperation();
            } catch (KineticException e) {
                Assert.fail("Create batch operation failed. " + e.getMessage());
            }

            try {
                batch.putForced(foo);
            } catch (KineticException e1) {
                Assert.fail("Put async batch op throw exception. "
                        + e1.getMessage());
            }

            try {
                batch.delete(bar);
            } catch (KineticException e1) {
                Assert.fail("Delete async batch op throw exception. "
                        + e1.getMessage());
            }

            try {
                batch.commit();
            } catch (KineticException e1) {
                Assert.fail("Batch commit throw exception. " + e1.getMessage());
            }

            try {
                Entry fooGet = getClient(clientName).get(foo.getKey());
                assertTrue(Arrays.equals(fooGet.getKey(), foo.getKey()));
                assertTrue(Arrays.equals(fooGet.getValue(), fooValue));
                assertTrue(Arrays.equals(
                        fooGet.getEntryMetadata().getVersion(), fooVersion));
            } catch (KineticException e1) {
                Assert.fail("Get foo throw exception. " + e1.getMessage());
            }

            try {
                Entry barGet = getClient(clientName).get(bar.getKey());
                assertNull(barGet);
            } catch (KineticException e1) {
                Assert.fail("Get bar throw exception. " + e1.getMessage());
            }

            try {
                cleanEntry(bar, client);
                cleanEntry(foo, client);
            } catch (KineticException e) {
                Assert.fail("Clean entry failed. " + e.getMessage());
            }
        }
    }

    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testBatchOperationInLoop_OneClient_AbortBatch_DifferentKey(
            String clientName) {
        Entry foo = getFooEntry();
        Entry bar = getBarEntry();

        int loopCount = 4;
        KineticClient client = getClient(clientName);

        for (int j = 0; j < loopCount; j++) {
            byte[] fooKeyD = toByteArray("foo" + j);
            try {
                client.deleteForced(fooKeyD);
            } catch (KineticException e) {
                Assert.fail("clean data failed. " + e.getMessage());
            }
        }

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        for (int i = 0; i < loopCount; i++) {
            byte[] fooKey = toByteArray("foo" + i);
            byte[] fooValue = ByteBuffer.allocate(valueSize).array();
            byte[] fooVersion = toByteArray("v" + i);
            foo.setKey(fooKey);
            foo.setValue(fooValue);
            foo.getEntryMetadata().setVersion(fooVersion);

            byte[] barKey = toByteArray("bar" + i);
            bar.setKey(barKey);

            try {
                client.putForced(bar);
            } catch (KineticException e2) {
                Assert.fail("Put bar throw exception: " + e2.getMessage());
            }

            BatchOperation batch = null;
            try {
                batch = client.createBatchOperation();
            } catch (KineticException e) {
                Assert.fail("Create batch operation failed. " + e.getMessage());
            }

            try {
                batch.putForced(foo);
            } catch (KineticException e1) {
                Assert.fail("Put async batch op throw exception. "
                        + e1.getMessage());
            }

            try {
                batch.delete(bar);
            } catch (KineticException e1) {
                Assert.fail("Delete async batch op throw exception. "
                        + e1.getMessage());
            }

            try {
                batch.abort();
            } catch (KineticException e1) {
                Assert.fail("Batch abort throw exception. " + e1.getMessage());
            }

            try {
                Entry fooGet = getClient(clientName).get(fooKey);
                assertNull(fooGet);
            } catch (KineticException e1) {
                Assert.fail("Get foo throw exception. " + e1.getMessage());
            }

            try {
                Entry barGet = getClient(clientName).get(barKey);
                assertTrue(Arrays.equals(barGet.getKey(), bar.getKey()));
                assertTrue(Arrays.equals(barGet.getValue(), bar.getValue()));
                assertTrue(Arrays.equals(
                        barGet.getEntryMetadata().getVersion(), bar
                                .getEntryMetadata().getVersion()));
            } catch (KineticException e1) {
                Assert.fail("Get bar throw exception. " + e1.getMessage());
            }

            for (int j = 0; j < loopCount; j++) {
                byte[] fooKeyD = toByteArray("foo" + j);
                try {
                    client.deleteForced(fooKeyD);
                } catch (KineticException e) {
                    Assert.fail("clean data failed. " + e.getMessage());
                }
            }

            try {
                cleanEntry(bar, getClient(clientName));
                cleanEntry(foo, getClient(clientName));
            } catch (KineticException e) {
                Assert.fail("Clean entry failed. " + e.getMessage());
            }
        }
    }

    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testBatchOperationInLoop_OneClient_AbortBatch_SameKey(
            String clientName) {
        Entry foo = getFooEntry();
        Entry bar = getBarEntry();

        try {
            cleanEntry(bar, getClient(clientName));
            cleanEntry(foo, getClient(clientName));
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        int loopCount = 4;
        KineticClient client = getClient(clientName);
        for (int i = 0; i < loopCount; i++) {
            byte[] fooValue = ByteBuffer.allocate(valueSize).array();
            byte[] fooVersion = toByteArray("v" + i);
            foo.setValue(fooValue);
            foo.getEntryMetadata().setVersion(fooVersion);

            try {
                client.putForced(bar);
            } catch (KineticException e2) {
                Assert.fail("Put bar throw exception: " + e2.getMessage());
            }

            BatchOperation batch = null;
            try {
                batch = client.createBatchOperation();
            } catch (KineticException e) {
                Assert.fail("Create batch operation failed. " + e.getMessage());
            }

            try {
                batch.putForced(foo);
            } catch (KineticException e1) {
                Assert.fail("Put async batch op throw exception. "
                        + e1.getMessage());
            }

            try {
                batch.delete(bar);
            } catch (KineticException e1) {
                Assert.fail("Delete async batch op throw exception. "
                        + e1.getMessage());
            }

            try {
                batch.abort();
            } catch (KineticException e1) {
                Assert.fail("Batch abort throw exception. " + e1.getMessage());
            }

            try {
                Entry fooGet = getClient(clientName).get(foo.getKey());
                assertNull(fooGet);
            } catch (KineticException e1) {
                Assert.fail("Get foo throw exception. " + e1.getMessage());
            }

            try {
                Entry barGet = getClient(clientName).get(bar.getKey());
                assertTrue(Arrays.equals(barGet.getKey(), bar.getKey()));
                assertTrue(Arrays.equals(barGet.getValue(), bar.getValue()));
                assertTrue(Arrays.equals(
                        barGet.getEntryMetadata().getVersion(), bar
                                .getEntryMetadata().getVersion()));

            } catch (KineticException e1) {
                Assert.fail("Get bar throw exception. " + e1.getMessage());
            }

            try {
                cleanEntry(bar, getClient(clientName));
                cleanEntry(foo, getClient(clientName));
            } catch (KineticException e) {
                Assert.fail("Clean entry failed. " + e.getMessage());
            }
        }
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_Concurrent_MultiClients_SameKey_AllSuccess(
            String clientName) {
        int writeThreads = 5;
        CountDownLatch latch = new CountDownLatch(writeThreads);
        ExecutorService pool = Executors.newCachedThreadPool();

        KineticClient kineticClient = null;
        for (int i = 0; i < writeThreads; i++) {
            try {
                kineticClient = KineticClientFactory
                        .createInstance(kineticClientConfigutations
                                .get(clientName));
            } catch (KineticException e) {
                Assert.fail("Create client throw exception. " + e.getMessage());
            }
            pool.execute(new BatchThread(kineticClient, latch));
        }

        // wait all threads finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail("latch await throw exception. " + e.getMessage());
        }
        pool.shutdown();

    }

    private Entry getFooEntry() {
        Entry foo = new Entry();
        byte[] fooKey = toByteArray("foo");
        foo.setKey(fooKey);
        byte[] fooValue = toByteArray("foovalue");
        foo.setValue(fooValue);
        byte[] fooVersion = toByteArray("1234");
        foo.getEntryMetadata().setVersion(fooVersion);

        return foo;
    }

    private Entry getBarEntry() {
        Entry bar = new Entry();
        byte[] barKey = toByteArray("bar");
        bar.setKey(barKey);
        byte[] barValue = toByteArray("barvalue");
        bar.setValue(barValue);
        byte[] barVersion = toByteArray("1234");
        bar.getEntryMetadata().setVersion(barVersion);

        return bar;
    }

    private void cleanEntry(Entry entry, KineticClient client)
            throws KineticException {
        client.deleteForced(entry.getKey());
    }
}

class BatchThread implements Runnable {
    private final CountDownLatch latch;
    private final KineticClient kineticClient;

    public BatchThread(KineticClient kineticClient, CountDownLatch latch) {
        this.kineticClient = kineticClient;
        this.latch = latch;
    }

    @Override
    public void run() {
        Entry bar = new Entry();
        byte[] barKey = toByteArray("bar");
        bar.setKey(barKey);
        byte[] barValue = toByteArray("barvalue");
        bar.setValue(barValue);
        byte[] barVersion = toByteArray("1234");
        bar.getEntryMetadata().setVersion(barVersion);

        Entry foo = new Entry();
        byte[] fooKey = toByteArray("foo");
        foo.setKey(fooKey);
        byte[] fooValue = toByteArray("foovalue");
        foo.setValue(fooValue);
        byte[] fooVersion = toByteArray("1234");
        foo.getEntryMetadata().setVersion(fooVersion);

        try {
            kineticClient.deleteForced(fooKey);
            kineticClient.deleteForced(barKey);
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            kineticClient.putForced(bar);
        } catch (KineticException e1) {
            Assert.fail("Put entry failed. " + e1.getMessage());
        }

        BatchOperation batch = null;
        try {
            batch = kineticClient.createBatchOperation();
        } catch (KineticException e1) {
            Assert.fail("Create batch throw exception. " + e1.getMessage());
        }

        try {
            batch.putForced(foo);
        } catch (KineticException e1) {
            Assert.fail("Put entry failed. " + e1.getMessage());
        }

        try {
            batch.deleteForced(bar.getKey());
        } catch (KineticException e1) {
            Assert.fail("Delete entry failed. " + e1.getMessage());
        }

        try {
            batch.commit();
        } catch (KineticException e1) {
            Assert.fail("Batch commit throw exception. " + e1.getMessage());
        }

        try {
            kineticClient.deleteForced(fooKey);
            kineticClient.deleteForced(barKey);
        } catch (KineticException e) {
            Assert.fail("Clean entry failed. " + e.getMessage());
        }

        try {
            kineticClient.close();
        } catch (KineticException e) {
            Assert.fail("close kineticClient failed, " + e.getMessage());
        } catch (Exception e) {
            Assert.fail("close kineticClient failed, " + e.getMessage());
        }

        // latch count down
        latch.countDown();
    }
}
