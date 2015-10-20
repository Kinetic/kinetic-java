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

import kinetic.client.BatchOperation;
import kinetic.client.CallbackHandler;
import kinetic.client.Entry;
import kinetic.client.KineticException;

/**
 * Kinetic client batch operation implementation.
 * 
 * 
 * @author chiaming
 *
 */
public class DefaultBatchOperation implements BatchOperation {

    private static int batchIdSequence = 1;

    private int batchId = -1;

    // operation count
    private int count = 0;

    private DefaultKineticClient client = null;

    public DefaultBatchOperation(DefaultKineticClient client)
            throws KineticException {

        this.batchId = nextBatchId();

        this.client = client;

        this.client.startBatchOperation(batchId);
    }

    public void putAsync(Entry entry, byte[] newVersion,
            CallbackHandler<Entry> handler) throws KineticException {

        this.client.batchPutAsync(entry, newVersion, handler, batchId);
        this.count++;
    }

    public void putForcedAsync(Entry entry, CallbackHandler<Entry> handler)
            throws KineticException {

        this.client.batchPutForcedAsync(entry, handler, batchId);
        this.count++;
    }


    public void deleteAsync(Entry entry, CallbackHandler<Boolean> handler)
            throws KineticException {

        this.client.batchDeleteAsync(entry, handler, batchId);
        this.count++;
    }

    public void deleteForcedAsync(byte[] key, CallbackHandler<Boolean> handler)
            throws KineticException {

        this.client.batchDeleteForcedAsync(key, handler, batchId);
        this.count++;
    }

    @Override
    public void commit() throws KineticException {

        this.client.endBatchOperation(batchId, count);
    }

    private synchronized static int nextBatchId() {
        return batchIdSequence++;
    }

    @Override
    public void abort() throws KineticException {
        this.client.abortBatchOperation(batchId);
    }

    @Override
    public void put(Entry entry, byte[] newVersion) throws KineticException {
        // batch forced put
        this.client.batchPut(entry, newVersion, batchId);

        // increase count
        this.count++;
    }

    @Override
    public void putForced(Entry entry) throws KineticException {
        // batch forced put no ack
        this.client.batchPutForced(entry, batchId);

        // increase count
        this.count++;
    }

    @Override
    public void delete(Entry entry) throws KineticException {
        // batch delete no ack
        this.client.batchDelete(entry, batchId);

        // increase count
        this.count++;
    }

    @Override
    public void deleteForced(byte[] key) throws KineticException {

        // batch forced delete no ack
        this.client.batchDeleteForced(key, batchId);

        // increase count
        this.count++;
    }

}
