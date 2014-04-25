/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
