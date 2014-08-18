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

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.admin.impl.DefaultAdminClient;
import com.seagate.kinetic.client.internal.MessageFactory;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Message;

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
				fail("Async exception" + exception);
			}
		};
	}

	/**
	 * Reset pin, unless kinetic.test.disable-clean-pin system property is set
	 * to 'true'
	 *
	 * @param pin
	 *            new pin
	 * @param client
	 *            client to use
	 * @throws KineticException
	 *            if any kinetic internal error occurred.
	 */
	public static void cleanPin(String pin, DefaultAdminClient client)
			throws KineticException {
	    
		if (Boolean.getBoolean("kinetic.test.disable-clean-pin")) {
			return;
		}
		
		KineticMessage km = MessageFactory.createKineticMessageWithBuilder();
		
		Command.Builder commandBuilder = (Command.Builder) km.getCommand();
		
		Kinetic.Command.Setup.Builder setup = commandBuilder
				.getBodyBuilder().getSetupBuilder();
		
		/**
		 * XXX: protocol-3.0.0
		 */
		//setup.setPin(ByteString.copyFromUtf8(pin));

		KineticMessage respond = client.configureSetupPolicy(km);

		assertTrue(respond.getCommand().getStatus().getCode()
				.equals(Kinetic.Command.Status.StatusCode.SUCCESS));
	}

	/**
	 * Async success handler interface.
	 *
	 */
	public interface SuccessAsyncHandler<T> {
		void onSuccess(CallbackResult<T> result);
	}

	/**
	 * Wait for count down latch reduced to zero.
	 *
	 * @param latch
	 *        a count down latch number.
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
	 *        a count down latch number.
	 * @param secondsTimeout
	 *        time out time to be set.
	 */
	public static void waitForLatch(CountDownLatch latch, int secondsTimeout)
			throws InterruptedException {
		assertTrue(latch.await(secondsTimeout, TimeUnit.SECONDS));
	}

}
