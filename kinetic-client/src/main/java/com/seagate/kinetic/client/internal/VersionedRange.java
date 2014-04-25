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

import java.util.Iterator;

import kinetic.client.Entry;
import kinetic.client.KineticException;

/**
 *
 * Entry range implementation.
 *
 * @author James Hughes.
 * @author Chiaming Yang
 *
 */

public class VersionedRange implements Iterable<Entry> {

    private KineticIterator lcIterator = null;

    /**
     * Constructs a new instance of kinetic entry range.
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
    public VersionedRange(DefaultKineticClient kinetic, byte[] startKey,
            boolean startKeyInclusive, byte[] endKey, boolean endKeyInclusive)
                    throws KineticException {

        this.lcIterator = new VersionedIterator(kinetic, startKey,
                startKeyInclusive, endKey, endKeyInclusive);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Entry> iterator() {
        return this.lcIterator;
    }

}
