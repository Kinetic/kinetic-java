/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
