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
package kinetic.client;

/**
 * 
 * Kinetic client batch operation interface.
 * <p>
 * 
 * @author James Hughes
 * @author chiaming
 *
 */
public interface BatchOperation {

    /**
     * Put the versioned <code>Entry</code> within the batch operation. The
     * command is not committed until the {@link #commit()} method is invoked
     * and returned successfully.
     * <p>
     * If the version in the specified entry does not match the version stored
     * in the persistent store, a <code>KineticException</code> is raised
     * 
     * @param entry
     *            the <code>Entry</code> to be put to the persistent store.
     * 
     * @param newVersion
     *            new version for the entry.
     * 
     * @throws KineticException
     *             if any internal errors occurred.
     * 
     * @see #putForced(Entry)
     */
    public void put(Entry entry, byte[] newVersion) throws KineticException;

    /**
     * Force to put the specified <code>Entry</code> within the batch operation.
     * Overwrite the entry in the store if existed. The command is not committed
     * until the {@link #commit()} method is invoked and returned successfully.
     * 
     * @param entry
     *            the <code>Entry</code> to be put to the persistent store.
     * 
     * @throws KineticException
     *             if any internal errors occurred.
     * 
     * @see #put(Entry, byte[])
     */
    public void putForced(Entry entry) throws KineticException;


    /**
     * Delete the entry that is associated with the key specified in the
     * <code>entry</code> within the batch operation. The command is not
     * committed until the {@link #commit()} method is invoked and returned
     * successfully.
     * <p>
     * The version specified in the entry metadata must match the one stored in
     * the persistent storage. Otherwise, a KineticException is raised.
     * 
     * @param entry
     *            the key in the entry is used to find the associated entry.
     * 
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #deleteForced(byte[])
     */
    public void delete(Entry entry) throws KineticException;

    /**
     * Force delete the entry that is associated with the key specified in the
     * parameter within the batch operation. The command is not committed until
     * the {@link #commit()} method is invoked and returned successfully.
     * <p>
     * The entry version stored in the persistent store is ignored.
     * 
     * @param key
     *            the key in the entry is used to find the associated entry.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #delete(Entry)
     */
    public void deleteForced(byte[] key) throws KineticException;

    /**
     * Commit the current batch operation.
     * <p>
     * When this call returned successfully, all the commands performed in the
     * current batch are executed and committed to store successfully.
     * 
     * @throws KineticException
     *             if any internal error occurred. The batch may or may not be
     *             committed. If committed, all commands are committed.
     *             Otherwise, no messages are committed.
     * @throws BatchAbortedException
     *             the commit failed. No messages within the batch were
     *             committed to the store.
     */
    public void commit() throws KineticException;

    /**
     * Abort the current batch operation.
     * <p>
     * When this call returned successfully, all the commands queued in the
     * current batch are aborted. Resources related to the current batch are
     * cleaned up and released.
     * <p>
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     */
    public void abort() throws KineticException;
}
