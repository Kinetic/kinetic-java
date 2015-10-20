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
