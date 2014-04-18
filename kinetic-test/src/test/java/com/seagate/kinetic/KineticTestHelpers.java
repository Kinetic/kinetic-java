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
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic;
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
		Kinetic.Message.Builder request = Kinetic.Message.newBuilder();
		Kinetic.Message.Setup.Builder setup = request.getCommandBuilder()
				.getBodyBuilder().getSetupBuilder();
		setup.setPin(ByteString.copyFromUtf8(pin));

		KineticMessage km = new KineticMessage();
		km.setMessage(request);

		Kinetic.Message respond = (Message) client.configureSetupPolicy(km)
				.getMessage();

		assertTrue(respond.getCommand().getStatus().getCode()
				.equals(Kinetic.Message.Status.StatusCode.SUCCESS));
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
