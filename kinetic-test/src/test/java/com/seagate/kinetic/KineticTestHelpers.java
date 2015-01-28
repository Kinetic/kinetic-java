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
    
    public static void instantErase(String oldErasePin, String newErasePin, KineticAdminClient client)
            throws KineticException {

        byte[] oldErasePinB = toByteArray(oldErasePin);
        byte[] newErasePinB = toByteArray(newErasePin);
        client.setErasePin(oldErasePinB, newErasePinB);
        client.instantErase(newErasePinB);
    }
    
    public static void cleanData(int keyCount, KineticClient client)
            throws KineticException {
        for (int i = 0; i < keyCount; i++) {
            byte[] key = toByteArray("key" + i);
            client.deleteForced(key);
        }
    }
    
    public static void cleanKVGenData(int keyCount, KineticClient client)
            throws KineticException {
        KVGenerator kvGen = new KVGenerator(); 
        for (int i = 0; i < keyCount; i++) {
            byte[] key = toByteArray(kvGen.getNextKey());
            client.deleteForced(key);
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
