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
package com.seagate.kinetic.simulator.client.async;

import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticException;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.client.internal.DefaultKineticClient;

public class KineticAsyncExample {

	private final Logger logger = Logger.getLogger(AsyncRequestTest.class
			.getName());

	private static final String UTF8 = "UTF8";

	private ClientConfiguration clientConfig = null;

	private DefaultKineticClient kineticClient = null;

	public KineticAsyncExample() throws Exception {
		this.setUp();
		this.asyncApiExample();
	}

	public void setUp() throws KineticException {

		this.clientConfig = new ClientConfiguration(System.getProperties());

		kineticClient = new DefaultKineticClient(this.clientConfig);
	}

	public void asyncApiExample() throws Exception {

		long index = 0;

		while (true) {

			String ks = "Hello-" + index;
			String vs = "World-" + index;
			byte[] key = ks.getBytes(UTF8);
			byte[] value = vs.getBytes(UTF8);

			byte[] version = "v1".getBytes(UTF8);

			Entry entry = new Entry(key, value);

			this.kineticClient.putAsync(entry, version,
					new CallbackHandler<Entry>() {

						@Override
						public void onSuccess(CallbackResult<Entry> result) {

							logger.info("putAsync message received, key = "
									+ ByteString.copyFrom(
											result.getResult().getKey())
											.toStringUtf8());
						}

						@Override
						public void onError(AsyncKineticException exception) {
							logger.log(Level.WARNING, exception.getMessage(),
									exception);
						}
					});

			// get async
			this.kineticClient.getAsync(key, new CallbackHandler<Entry>() {

				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					logger.info("getAsync message received, key = "
							+ ByteString.copyFrom(result.getResult().getKey())
									.toStringUtf8());

				}

				@Override
				public void onError(AsyncKineticException exception) {
					logger.log(Level.WARNING, exception.getMessage(), exception);
				}

			});

			// get next async
			this.kineticClient.getNextAsync(key, new CallbackHandler<Entry>() {

				@Override
				public void onSuccess(CallbackResult<Entry> result) {
					logger.info("getNextAsync message received, entry = "
							+ result.getResult());
				}

				@Override
				public void onError(AsyncKineticException exception) {
					logger.log(Level.WARNING, exception.getMessage(), exception);
				}

			});

			// get previous async
			this.kineticClient.getPreviousAsync(key,
					new CallbackHandler<Entry>() {

						@Override
						public void onSuccess(CallbackResult<Entry> result) {
							logger.info("getPreviousAsync message received, entry = "
									+ result.getResult());
						}

						@Override
						public void onError(AsyncKineticException exception) {
							logger.log(Level.WARNING, exception.getMessage(),
									exception);
						}

					});

			// delete async
			entry.getEntryMetadata().setVersion(version);

			this.kineticClient.deleteAsync(entry,
					new CallbackHandler<Boolean>() {

						@Override
						public void onSuccess(CallbackResult<Boolean> result) {
							logger.info("deleteAsync onSuccess , deleted = "
									+ result.getResult());

						}

						@Override
						public void onError(AsyncKineticException exception) {
							logger.log(Level.WARNING, exception.getMessage(),
									exception);
						}

					});

			logger.info("entry put to store asynchronously., key=" + key);

			index++;

			Thread.sleep(10);
		}
	}

	public void close() throws KineticException {
		this.kineticClient.close();
	}

	public static void main(String[] args) throws Exception {
		@SuppressWarnings("unused")
		KineticAsyncExample putAsync = new KineticAsyncExample();
	}

}
