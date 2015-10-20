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
package com.seagate.kinetic.client.internal.util.bigobject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticException;
import kinetic.client.advanced.AdvancedKineticClient;
import kinetic.client.advanced.AdvancedKineticClientFactory;
import kinetic.client.advanced.PersistOption;

/**
 * 
 * An example to use Kinetic API to put/get/delete arbitrary large objects on
 * one Kinetic storage.
 * 
 * @author chiaming
 * 
 */
public class BigObject {

	private final Logger logger = Logger.getLogger(BigObject.class
			.getName());

	// chunk value size
	private static final int CHUNK_SIZE = 1024 * 1024;

	// integer size
	private static int ISIZE = 4;

	// kinetic client instance
	private AdvancedKineticClient client = null;

	// put callback
	private final PutxCallbackHandler putCallback = new PutxCallbackHandler();

	/**
	 * constructor for a new instance
	 * 
	 * @param config
	 * @throws KineticException
	 */
	public BigObject(ClientConfiguration config)
			throws KineticException {

		client = AdvancedKineticClientFactory
				.createAdvancedClientInstance(config);
	}

	/**
	 * Put an arbitrary size of object to the kinetic storage based on the
	 * specified key space and input stream.
	 * <p>
	 * The specified parameter <code>key</code> is the key space for the object.
	 * Application that uses this API must ensure that the key space is an
	 * unique key space on the specified configuration storage (drive).
	 * <p>
	 * A big object is divided into 1M Key/Value chunks. Each key for a chunk is
	 * in sequence based on the specified base key.
	 * <p>
	 * 
	 * 
	 * @param key
	 *            the based key to store the object.
	 * 
	 * @param is
	 *            the input stream that used to read the object.
	 * 
	 * @return the total size of the value stored in the Kinetic storage.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public long putx(byte[] key, InputStream is) throws KineticException {

		DataInputStream dis = new DataInputStream(is);

		int kseq = 0;

		long total = 0;

		boolean done = false;

		try {

			// write master entry
			this.initEntry(key);

			// perform put in chunks
			while (done == false) {

				// value holder
				byte[] value = new byte[CHUNK_SIZE];

				// read value
				int vlen = dis.read(value);

				// more data
				if (vlen > 0) {

					// total bytes written
					total += vlen;

					// write entry in sequence
					this.writeEntryInSequence(client, kseq, key,
							value,
							vlen);

					// increase key sequence for next key
					kseq++;
				} else {
					// reached end of stream
					done = true;
				}
			}

			// wait for all ops to confirm
			putCallback.waitForFinish();

			// finalize entry
			this.finalizeEntry(key, kseq);

			logger.info("finished streaming, entries = " + kseq
					+ ", total bytes=" + total);

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new KineticException(e);
		} finally {
			try {
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return total;
	}

	/**
	 * initialize the master entry.
	 * 
	 * @param key
	 *            base key for the master entry.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	private void initEntry(byte[] key) throws KineticException {

		// init master entry
		Entry entry = new Entry(key, new byte[0]);

		/**
		 * simple versioning for the master entry
		 */
		Random random = new Random();
		byte[] version = new byte[20];
		random.nextBytes(version);

		client.put(entry, version, PersistOption.ASYNC);
	}

	/**
	 * Write entry in sequence.
	 * 
	 * @param client
	 *            the client instance used to write the entry.
	 * @param kseq
	 *            sequence for the chunk
	 * @param key
	 *            the base key
	 * @param value
	 *            value in chunk
	 * @param vlen
	 *            value length
	 * @throws KineticException
	 *             if any internal error occurred
	 */
	private void writeEntryInSequence(AdvancedKineticClient client, int kseq,
			byte[] key, byte[] value, int vlen) throws KineticException {

		// generate key and do put with 1M
		ByteBuffer kByteBuffer = ByteBuffer.allocate(key.length + ISIZE);

		// key + index
		byte[] kbytes = kByteBuffer.put(key).putInt(kseq).array();

		// make entry in sequence
		Entry entry = new Entry();
		entry.setKey(kbytes);

		// set entry value
		if (vlen == CHUNK_SIZE) {
			// full chunk entry
			entry.setValue(value);
		} else {
			// not full chunk entry in sequence
			ByteBuffer vByteBuffer = ByteBuffer.allocate(vlen);
			vByteBuffer.put(value, 0, vlen);
			entry.setValue(vByteBuffer.array());
		}

		// set tag in sequence
		entry.getEntryMetadata().setTag(new byte[1]);

		// add callback counter
		this.putCallback.increaseCounter();

		// do put chunk
		client.putForcedAsync(entry, PersistOption.ASYNC, this.putCallback);
	}

	/**
	 * Finalized the big object put operation.
	 * 
	 * @param key
	 *            base key
	 * @param index
	 *            last index for the key sequence.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred
	 */
	private void finalizeEntry(byte[] key, int index) throws KineticException {

		// finalize master entry
		Entry entry = new Entry();

		// set key
		entry.setKey(key);

		// set index
		ByteBuffer vbb = ByteBuffer.allocate(ISIZE);
		vbb.putInt(index);
		entry.setValue(vbb.array());

		// do put operation
		client.putForced(entry, PersistOption.FLUSH);
	}

	/**
	 * Get the big object from Kinetic storage based on the specified key space.
	 * The obtained object is written the specified output stream.
	 * 
	 * @param key
	 *            the key space that the big object is stored.
	 * 
	 * @param os
	 *            The obtained object is written the specified output stream.
	 * @return the total length (in bytes) written to the output stream
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public long getx(byte[] key, OutputStream os) throws KineticException {

		DataOutputStream dos = new DataOutputStream(os);
		long total = 0;
		int totalEntries = 0;

		try {
			// get master entry
			Entry entry = this.client.get(key);
			if (entry == null) {
				// if no entry found, return 0
				return 0;
			}

			// read #entries
			ByteBuffer vbb = ByteBuffer.wrap(entry.getValue());
			totalEntries = vbb.getInt();

			GetxCallbackHandler callback = new GetxCallbackHandler(
					dos);

			// perform get in chunks
			for (int kseq=0; kseq < totalEntries; kseq++) {

				// generate key and do put with 1M
				ByteBuffer kByteBuffer = ByteBuffer
						.allocate(key.length + ISIZE);

				// key + index
				byte[] keyInSeq = kByteBuffer.put(key).putInt(kseq).array();

				// async get entry
				this.client.getAsync(keyInSeq, callback);

				// increase read counter
				callback.increaseCounter();
			}

			// wait for async read and write (to output stream) to finish
			callback.waitForFinish();

			// get total read length
			total = callback.getTotalRead();

			logger.info("finished streaming, tatal=" + total);

		} catch (Exception e) {
			// do clean up
			e.printStackTrace();
		} finally {
			try {
				dos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return total;
	}

	/**
	 * Delete the object from Kinetic storage based on the specified key space.
	 * 
	 * @param key
	 *            the key space to delete for the object stored in Kinetic.
	 * @return the (approximate) total length of object bytes stored/deleted.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public long deletex(byte[] key) throws KineticException {

		DeletexCallbackHandler deleteCallback = new DeletexCallbackHandler();

		long total = 0;

		int totalEntries = 0;

		try {
			// get master entry
			Entry entry = this.client.get(key);
			if (entry == null) {
				// if no entry found, return 0
				return 0;
			}

			// read #entries
			ByteBuffer vbb = ByteBuffer.wrap(entry.getValue());
			totalEntries = vbb.getInt();

			// perform get in chunks
			for (int kseq = 0; kseq < totalEntries; kseq++) {

				// generate key and do put with 1M
				ByteBuffer kByteBuffer = ByteBuffer
						.allocate(key.length + ISIZE);

				// key + index
				byte[] keyInSeq = kByteBuffer.put(key).putInt(kseq).array();

				// async get entry
				this.client.deleteForcedAsync(keyInSeq, deleteCallback);

				// increase read counter
				deleteCallback.increaseCount();
			}

			// wait for async read and write (to output stream) to finish
			total = deleteCallback.waitForFinish();

			this.client.deleteForced(key);

			logger.info("finished streaming, tatal=" + total);

		} catch (Exception e) {
			// do clean up
			e.printStackTrace();
		}

		return total;
	}

	/**
	 * close the instance and release all resources.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public void close() throws KineticException {
		this.client.close();
	}

}
