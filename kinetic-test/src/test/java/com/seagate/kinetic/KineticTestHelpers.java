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
package com.seagate.kinetic;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kinetic.admin.ACL;
import kinetic.admin.Domain;
import kinetic.admin.KineticAdminClient;
import kinetic.admin.Role;
import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;

import org.testng.AssertJUnit;

import com.google.protobuf.ByteString;

/**
 * Kinetic test utility.
 * <p>
 * Test utility used by case.
 * <p>
 *
 */
public class KineticTestHelpers {
    private KineticTestHelpers() {
    }

    /**
     * Convert string to byte array, the chaset is UTF-8.
     *
     */
    public static byte[] toByteArray(String s) {
        return s.getBytes(Charset.forName("UTF-8"));
    }

    /**
     * Convert integer to byte array.
     *
     */
    public static byte[] int32(int x) {
        return ByteString.copyFrom(
                ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(x)
                        .array()).toByteArray();
    }

    /**
     * Generally tests performing async operations want to immeidately fail if
     * an async call fails. To simplify this common case use this method. It
     * will return a CallbackHandler that calls the given handler on success and
     * fail() on failure. This eliminates the need to have identical onError
     * implementations everywhere
     *
     * @param handler
     *            Method to call on success
     * @param <T>
     *            Callback type
     * @return A CallbackHandler implementation
     */
    public static <T> CallbackHandler<T> buildSuccessOnlyCallbackHandler(
            final SuccessAsyncHandler<T> handler) {
        return new CallbackHandler<T>() {
            @Override
            public void onSuccess(CallbackResult<T> result) {
                handler.onSuccess(result);
            }

            @Override
            public void onError(AsyncKineticException exception) {
                AssertJUnit.fail("Async exception" + exception);
            }
        };
    }

    public static <T> CallbackHandler<T> buildAsyncCallbackHandler(
            final AsyncHandler<T> handler) {
        return new CallbackHandler<T>() {
            @Override
            public void onSuccess(CallbackResult<T> result) {
                handler.onSuccess(result);
            }

            @Override
            public void onError(AsyncKineticException exception) {
            }
        };
    }

    public static void instantErase(String oldErasePin, String newErasePin,
            KineticAdminClient client) throws KineticException {

        byte[] oldErasePinB = toByteArray(oldErasePin);
        byte[] newErasePinB = toByteArray(newErasePin);
        client.setErasePin(oldErasePinB, newErasePinB);
        client.instantErase(newErasePinB);
    }

    public static void cleanData(int keyCount, KineticClient client)
            throws KineticException {
        int keySize = keyCount - 1;
        boolean flag = true;
        byte[] keyB = toByteArray("key0");
        while (flag) {
            client.deleteForced(keyB);

            Entry enN = client.getNext(keyB);

            if (enN == null) {
                flag = false;
            } else if (new String(enN.getKey(), Charset.forName("UTF-8"))
                    .equals("key" + keySize)) {
                flag = false;
                client.deleteForced(enN.getKey());
            } else {
                keyB = enN.getKey();
            }
        }
    }

    public static void cleanData(byte[] startKey, byte[] endKey,
            KineticClient client) throws KineticException {
        boolean flag = true;
        while (flag) {
            client.deleteForced(startKey);

            Entry enN = client.getNext(startKey);

            if (enN == null) {
                flag = false;
            } else if (new String(enN.getKey(), Charset.forName("UTF-8"))
                    .equals(new String(endKey, Charset.forName("UTF-8")))) {
                flag = false;
                client.deleteForced(enN.getKey());
            } else {
                startKey = enN.getKey();
            }
        }
    }

    public static void cleanNextData(byte[] key, KineticClient client)
            throws KineticException {
        boolean flag = true;
        while (flag) {
            Entry enN = client.getNext(key);
            if (enN == null) {
                flag = false;
                client.deleteForced(key);
            } else {
                client.deleteForced(key);
                key = enN.getKey();
            }
        }
    }

    public static void cleanPreviousData(byte[] key, KineticClient client)
            throws KineticException {
        boolean flag = true;
        while (flag) {
            Entry enN = client.getPrevious(key);

            if (enN == null) {
                flag = false;
                client.deleteForced(key);
            } else {
                client.deleteForced(key);
                key = enN.getKey();
            }
        }
    }

    public static void cleanKVGenData(int keyCount, KineticClient client)
            throws KineticException {
        KVGenerator kvGen = new KVGenerator();
        String lastKey = "";
        for (int i = 0; i < keyCount; i++) {
            lastKey = kvGen.getNextKey();
        }

        boolean flag = true;
        KVGenerator kvGen1 = new KVGenerator();
        byte[] keyB = toByteArray(kvGen1.getNextKey());
        while (flag) {
            client.deleteForced(keyB);

            Entry enN = client.getNext(keyB);

            if (enN == null) {
                flag = false;
            } else if (new String(enN.getKey(), Charset.forName("UTF-8"))
                    .equals(lastKey)) {
                flag = false;
                client.deleteForced(enN.getKey());
            } else {
                keyB = enN.getKey();
            }
        }
    }

    /**
     * Async success handler interface.
     *
     */
    public interface SuccessAsyncHandler<T> {
        void onSuccess(CallbackResult<T> result);
    }

    /**
     * Async success handler interface.
     *
     */
    public interface AsyncHandler<T> {
        void onSuccess(CallbackResult<T> result);

        void onError(AsyncKineticException exception);
    }

    /**
     * Wait for count down latch reduced to zero.
     *
     * @param latch
     *            a count down latch number.
     *
     */
    public static void waitForLatch(CountDownLatch latch)
            throws InterruptedException {
        waitForLatch(latch, 5);
    }

    /**
     * Wait for count down latch reduced to zero.
     *
     * @param latch
     *            a count down latch number.
     * @param secondsTimeout
     *            time out time to be set.
     */
    public static void waitForLatch(CountDownLatch latch, int secondsTimeout)
            throws InterruptedException {
        AssertJUnit.assertTrue(latch.await(secondsTimeout, TimeUnit.SECONDS));
    }

    /**
     * Set Default Acl.
     *
     * @return List<ACL> return default ACL.
     */
    public static List<ACL> setDefaultAcls() {
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

        List<ACL> acls = new ArrayList<ACL>();
        ACL acl1 = new ACL();
        acl1.setDomains(domains);
        acl1.setUserId(1);
        acl1.setKey("asdfasdf");

        acls.add(acl1);

        return acls;
    }
}
