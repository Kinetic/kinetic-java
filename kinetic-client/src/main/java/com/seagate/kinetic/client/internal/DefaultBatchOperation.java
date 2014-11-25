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

    private DefaultKineticClient client = null;

    public DefaultBatchOperation(DefaultKineticClient client)
            throws KineticException {

        this.client = client;

        this.client.startBatchOperation();
    }

    @Override
    public void putAsync(Entry entry, byte[] newVersion,
            CallbackHandler<Entry> handler) throws KineticException {
        this.client.putAsync(entry, newVersion, handler);
    }

    @Override
    public void putForcedAsync(Entry entry, CallbackHandler<Entry> handler)
            throws KineticException {

        this.client.putForcedAsync(entry, handler);
    }

    @Override
    public void deleteAsync(Entry entry, CallbackHandler<Boolean> handler)
            throws KineticException {

        this.client.deleteAsync(entry, handler);
    }

    @Override
    public void deleteForcedAsync(byte[] key, CallbackHandler<Boolean> handler)
            throws KineticException {

        this.client.deleteForcedAsync(key, handler);
    }

    @Override
    public void commit() throws KineticException {

        this.client.endBatchOperation();
    }

}
