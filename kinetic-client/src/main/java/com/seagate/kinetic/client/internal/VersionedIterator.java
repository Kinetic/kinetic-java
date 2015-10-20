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

import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.Entry;
import kinetic.client.KineticException;

/**
 * 
 * kinetic iterator implementation.
 * 
 * @author James Hughes.
 * @author Chiaming Yang
 * 
 */
public class VersionedIterator extends KineticIterator {

	private final static Logger logger = Logger
			.getLogger(VersionedIterator.class.getName());

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
	public VersionedIterator(DefaultKineticClient kinetic, byte[] startKey,
			boolean startKeyInclusive, byte[] endKey, boolean endKeyInclusive)
					throws KineticException {
		super(kinetic, startKey, startKeyInclusive, endKey, endKeyInclusive);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Entry next() {

		Entry versioned = null;

		while (versioned == null) {
			versioned = this.getNext();
		}

		return versioned;
	}

	private synchronized Entry getNext() {

		Entry versioned = null;

		if (this.hasNext() == false) {
			throw new NoSuchElementException();
		}

		try {
			versioned = this.doGet();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);

			throw new NoSuchElementException(e.getMessage());
		}

		return versioned;
	}

	/**
	 * Get Entry with the key obtained from the current position of the
	 * batched key range.
	 * 
	 * @return a Entry entry with the key obtained from the current position
	 *         of the batched key range.
	 * @throws KineticException
	 *             if any internal error occurred.
	 */
	private Entry doGet() throws KineticException {

		Entry versioned = this.kinetic.get(this.keyRange
				.get(currentPosition));

		this.currentPosition++;

		return versioned;
	}
}
