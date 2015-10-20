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
package com.seagate.kinetic.client.internal;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.KineticException;

/**
 * 
 * kinetic iterator implementation.
 * 
 * @author James Hughes.
 * @author Chiaming Yang
 * 
 */
@SuppressWarnings("rawtypes")
public abstract class KineticIterator implements Iterator {

	private final static Logger logger = Logger
			.getLogger(KineticIterator.class.getName());

	// my client handle
	protected DefaultKineticClient kinetic = null;

	// endkey
	protected byte[] endKey = null;

	// endkey inclusive flag
	protected boolean endKeyInclusive = false;

	// list of batched key range
	protected List<byte[]> keyRange = null;

	// default max returned size for the batched key range
	protected final int MAX_RETURNED = 10;

	// current index for the batched key range cached in client runtime
	protected int currentPosition = -1;

	// flag to indicate if more batched key range to read into client cache.
	protected boolean hasMoreBatch = false;

	/**
	 * Constructs a new instance of kinetic iterator.
	 * 
	 * @param kinetic
	 *            my client handle
	 * @param startKey
	 *            the start key in the specified key range.
	 * @param startKeyInclusive
	 *            true if the start key is inclusive.
	 * @param endKey
	 *            the end key in the specified key range.
	 * @param endKeyInclusive
	 *            true if the start key is inclusive.
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	public KineticIterator(DefaultKineticClient kinetic, byte[] startKey,
			boolean startKeyInclusive, byte[] endKey, boolean endKeyInclusive)
					throws KineticException {

		this.kinetic = kinetic;

		this.endKey = endKey;

		this.endKeyInclusive = endKeyInclusive;

		// read first batched key range into cache
		this.getNextBatch(startKey, startKeyInclusive);
	}

	/**
	 * Get next batched key range.
	 * 
	 * @param startKey
	 *            the start key in the specified key range.
	 * @param startKeyInclusive
	 *            true if the start key is inclusive.
	 * @param endKey
	 *            the end key in the specified key range.
	 * @param endKeyInclusive
	 *            true if the start key is inclusive.
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	protected void getNextBatch(byte[] startKey, boolean startKeyInclusive)
			throws KineticException {

		// get key range
		this.keyRange = this.kinetic.getKeyRange(startKey,
				startKeyInclusive, endKey, endKeyInclusive, MAX_RETURNED);

		if (keyRange.size() > 0) {
			// set current read position of the batch
			this.currentPosition = 0;
		} else {
			// list is empty
			this.currentPosition = -1;
		}

		if (this.keyRange.size() == this.MAX_RETURNED) {
			// has more batch
			this.hasMoreBatch = true;
		} else {
			// no more batch
			this.hasMoreBatch = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean hasNext() {

		boolean hasNext = false;

		try {
			// check if cursor is in current batch
			if (this.currentPosition >= 0
					&& this.currentPosition < this.keyRange.size()) {

				// in current batched key range
				hasNext = true;
			} else if (this.hasMoreBatch) {

				// next batched key range
				byte[] startKey = this.keyRange.get(MAX_RETURNED - 1);

				// get next batched key range
				this.getNextBatch(startKey, false);

				// check if there are matched keys in the next batch
				hasNext = (this.keyRange.size() > 0);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		return hasNext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
