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
package com.seagate.kinetic.asyncAPI;

import static com.seagate.kinetic.KineticTestHelpers.buildSuccessOnlyCallbackHandler;
import static com.seagate.kinetic.KineticTestHelpers.toByteArray;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import kinetic.client.BatchOperation;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;

import org.testng.annotations.Test;

import com.seagate.kinetic.IntegrationTestCase;
import com.seagate.kinetic.KineticTestHelpers;

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

@Test(groups = { "simulator", "drive" })
public class BatchOpAPITest extends IntegrationTestCase {

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PutAndDeleteSucceeds(String clientName)
            throws KineticException, UnsupportedEncodingException {
        Entry bar = getBarEntry();

        getClient(clientName).putForced(bar);

        BatchOperation batch = getClient(clientName).createBatchOperation();

        Entry foo = getFooEntry();

        CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
            @Override
            public void onSuccess(CallbackResult<Entry> result) {
            }
        });

        batch.putForcedAsync(foo, handler);

        CallbackHandler<Boolean> dhandler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
            @Override
            public void onSuccess(CallbackResult<Boolean> result) {
            }
        });

        batch.deleteAsync(bar, dhandler);

        batch.commit();

        // get foo, expect to find it
        assertTrue(Arrays.equals(foo.getKey(),
                getClient(clientName).get(foo.getKey()).getKey()));
        assertTrue(Arrays.equals(foo.getValue(),
                getClient(clientName).get(foo.getKey()).getValue()));
        assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                getClient(clientName).get(foo.getKey()).getEntryMetadata()
                        .getVersion()));

        // get bar, expect to null
        assertNull(getClient(clientName).get(bar.getKey()));
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_PutsSucceeds(String clientName)
            throws KineticException, UnsupportedEncodingException {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();

        BatchOperation batch = getClient(clientName).createBatchOperation();

        CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
            @Override
            public void onSuccess(CallbackResult<Entry> result) {
            }
        });

        batch.putForcedAsync(foo, handler);
        batch.putForcedAsync(bar, handler);

        batch.commit();

        // get foo, expect to find it
        assertTrue(Arrays.equals(foo.getKey(),
                getClient(clientName).get(foo.getKey()).getKey()));
        assertTrue(Arrays.equals(foo.getValue(),
                getClient(clientName).get(foo.getKey()).getValue()));
        assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                getClient(clientName).get(foo.getKey()).getEntryMetadata()
                        .getVersion()));

        // get bar, expect to find it
        assertTrue(Arrays.equals(bar.getKey(),
                getClient(clientName).get(bar.getKey()).getKey()));
        assertTrue(Arrays.equals(bar.getValue(),
                getClient(clientName).get(bar.getKey()).getValue()));
        assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                getClient(clientName).get(bar.getKey()).getEntryMetadata()
                        .getVersion()));
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_DeletesSucceeds(String clientName)
            throws KineticException, UnsupportedEncodingException {
        Entry bar = getBarEntry();
        Entry foo = getFooEntry();
        getClient(clientName).putForced(bar);
        getClient(clientName).putForced(foo);

        BatchOperation batch = getClient(clientName).createBatchOperation();

        CallbackHandler<Boolean> dhandler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
            @Override
            public void onSuccess(CallbackResult<Boolean> result) {
            }
        });

        batch.deleteAsync(bar, dhandler);

        batch.deleteAsync(foo, dhandler);

        batch.commit();

        // get foo, expect to null
        assertNull(getClient(clientName).get(foo.getKey()));

        // get bar, expect to null
        assertNull(getClient(clientName).get(bar.getKey()));
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_FollowedBothReadByOneClient_Failed(
            String clientName) throws KineticException,
            UnsupportedEncodingException {
        Entry bar = getBarEntry();
        getClient(clientName).putForced(bar);

        BatchOperation batch = getClient(clientName).createBatchOperation();

        Entry foo = getFooEntry();

        CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
            @Override
            public void onSuccess(CallbackResult<Entry> result) {
            }
        });

        batch.putForcedAsync(foo, handler);

        CallbackHandler<Boolean> dhandler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
            @Override
            public void onSuccess(CallbackResult<Boolean> result) {
            }
        });

        batch.deleteAsync(bar, dhandler);

        ClientConfiguration cc = new ClientConfiguration();
        KineticClient client1 = KineticClientFactory.createInstance(cc);
        try {
            client1.get(foo.getKey());
        } catch (KineticException e) {
            // assertTrue(e.getResponseMessage().getCommand().getStatus()
            // .getCode().equals(StatusCode.INVALID_BATCH));
        }

        try {
            client1.get(bar.getKey());
        } catch (KineticException e) {
            // assertTrue(e.getResponseMessage().getCommand().getStatus()
            // .getCode().equals(StatusCode.INVALID_BATCH));
        } finally {
            client1.close();
        }

        batch.commit();

        // get foo, expect to find it
        assertTrue(Arrays.equals(foo.getKey(),
                getClient(clientName).get(foo.getKey()).getKey()));
        assertTrue(Arrays.equals(foo.getValue(),
                getClient(clientName).get(foo.getKey()).getValue()));
        assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                getClient(clientName).get(foo.getKey()).getEntryMetadata()
                        .getVersion()));
        // get bar, expect to null
        assertNull(getClient(clientName).get(bar.getKey()));
    }

    @Test(dataProvider = "transportProtocolOptions")
    public void testBatchOperation_Succeeds_FollowedSingleReadByTwoClient(
            String clientName) throws KineticException,
            UnsupportedEncodingException {
        Entry bar = getBarEntry();

        getClient(clientName).putForced(bar);

        BatchOperation batch = getClient(clientName).createBatchOperation();

        Entry foo = getFooEntry();

        CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
            @Override
            public void onSuccess(CallbackResult<Entry> result) {
            }
        });

        batch.putForcedAsync(foo, handler);

        CallbackHandler<Boolean> dhandler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
            @Override
            public void onSuccess(CallbackResult<Boolean> result) {
            }
        });

        batch.deleteAsync(bar, dhandler);

        ClientConfiguration cc = new ClientConfiguration();
        KineticClient client1 = KineticClientFactory.createInstance(cc);
        KineticClient client2 = KineticClientFactory.createInstance(cc);

        try {
            client1.get(foo.getKey());
        } catch (KineticException e) {
            // assertTrue(e.getResponseMessage().getCommand().getStatus()
            // .getCode().equals(StatusCode.NOT_ATTEMPTED));
        } finally {
            client1.close();
        }

        try {
            client2.get(bar.getKey());
        } catch (KineticException e) {
            // assertTrue(e.getResponseMessage().getCommand().getStatus()
            // .getCode().equals(StatusCode.NOT_ATTEMPTED));
        } finally {
            client2.close();
        }

        batch.commit();

        // get foo, expect to find it
        assertTrue(Arrays.equals(foo.getKey(),
                getClient(clientName).get(foo.getKey()).getKey()));
        assertTrue(Arrays.equals(foo.getValue(),
                getClient(clientName).get(foo.getKey()).getValue()));
        assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                getClient(clientName).get(foo.getKey()).getEntryMetadata()
                        .getVersion()));

        assertNull(getClient(clientName).get(bar.getKey()));
    }

    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testBatchOperation_IfPutFailed_AllFailed(String clientName)
            throws KineticException, UnsupportedEncodingException {
        Entry foo = getFooEntry();
        Entry bar = getBarEntry();

        getClient(clientName).putForced(foo);
        getClient(clientName).putForced(bar);

        BatchOperation batch = getClient(clientName).createBatchOperation();

        byte[] foobatchValue = toByteArray("foobatch");
        foo.setValue(foobatchValue);
        byte[] foobatchVersion = toByteArray("5678");
        foo.getEntryMetadata().setVersion(foobatchVersion);

        CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
            @Override
            public void onSuccess(CallbackResult<Entry> result) {
            }
        });

        batch.putAsync(foo, foobatchVersion, handler);

        CallbackHandler<Boolean> dhandler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
            @Override
            public void onSuccess(CallbackResult<Boolean> result) {
            }
        });

        batch.deleteAsync(bar, dhandler);

        batch.commit();

        // get foo, expect to find the old one
        assertTrue(Arrays.equals(foo.getKey(),
                getClient(clientName).get(foo.getKey()).getKey()));
        assertTrue(Arrays.equals(foo.getValue(),
                getClient(clientName).get(foo.getKey()).getValue()));
        assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                getClient(clientName).get(foo.getKey()).getEntryMetadata()
                        .getVersion()));

        // get bar, expect to find it.
        assertTrue(Arrays.equals(bar.getKey(),
                getClient(clientName).get(bar.getKey()).getKey()));
        assertTrue(Arrays.equals(bar.getValue(),
                getClient(clientName).get(bar.getKey()).getValue()));
        assertTrue(Arrays.equals(bar.getEntryMetadata().getVersion(),
                getClient(clientName).get(bar.getKey()).getEntryMetadata()
                        .getVersion()));
    }

    @Test(dataProvider = "transportProtocolOptions", enabled = false)
    public void testBatchOperation_IfDeleteFailed_AllFailed(String clientName)
            throws KineticException, UnsupportedEncodingException {
        Entry bar = getBarEntry();
        getClient(clientName).putForced(bar);

        BatchOperation batch = getClient(clientName).createBatchOperation();

        Entry foo = getFooEntry();

        CallbackHandler<Entry> handler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Entry>() {
            @Override
            public void onSuccess(CallbackResult<Entry> result) {
            }
        });

        batch.putForcedAsync(foo, handler);

        byte[] barBatchVersion = toByteArray("5678");
        bar.getEntryMetadata().setVersion(barBatchVersion);

        CallbackHandler<Boolean> dhandler = buildSuccessOnlyCallbackHandler(new KineticTestHelpers.SuccessAsyncHandler<Boolean>() {
            @Override
            public void onSuccess(CallbackResult<Boolean> result) {
            }
        });

        batch.deleteAsync(bar, dhandler);

        batch.commit();

        // get foo, expect to find the old one
        assertTrue(Arrays.equals(foo.getKey(),
                getClient(clientName).get(foo.getKey()).getKey()));
        assertTrue(Arrays.equals(foo.getValue(),
                getClient(clientName).get(foo.getKey()).getValue()));
        assertTrue(Arrays.equals(foo.getEntryMetadata().getVersion(),
                getClient(clientName).get(foo.getKey()).getEntryMetadata()
                        .getVersion()));

        // get bar, expect to find it.
        assertTrue(Arrays.equals(bar.getKey(),
                getClient(clientName).get(bar.getKey()).getKey()));
        assertTrue(Arrays.equals(bar.getValue(),
                getClient(clientName).get(bar.getKey()).getValue()));
        assertTrue(Arrays.equals(toByteArray("1234"), getClient(clientName)
                .get(bar.getKey()).getEntryMetadata().getVersion()));
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
}
