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

import java.util.List;

import kinetic.client.advanced.PersistOption;

/**
 * Kinetic Client Application Interface.
 * <p>
 * Applications use <code>KineticClientFactory</code> to construct new instances
 * of <code>KineticClient</code>. All application level operations use this
 * interface to communicate with the Kinetic service.
 * <p>
 * The Kinetic API provide synchronous and asynchronous operations.
 * <p>
 * For each synchronous operation, the operation is guaranteed to be
 * successfully performed on server if the call returns without an Exception
 * being raised.
 * <p>
 * For example, an entry is persisted on server if put() operation returned
 * successfully.
 * <p>
 * For each asynchronous operation, the operation is guaranteed to be
 * successfully performed on server if its callback handler's onSuccess() method
 * received an successful CallBackResult.
 * <p>
 * Each KineticClient instance supports concurrent operations. Each method in
 * the KineticClient class also supports concurrent operations.
 * <p>
 * 
 * @see KineticClientFactory
 * @see CallbackHandler
 * 
 * @author James Hughes.
 * @author Chiaming Yang
 */
public interface KineticClient extends GenericKineticClient {

    /**
     * Performs a no op request-response command to the Kinetic service. A
     * successful noop operation returns the round-trip time of the command
     * invocation in milliseconds.
     * <p>
     * Please note that multiple invocations may likely reflect the average
     * response time of the service.
     * 
     * @return the round-trip time for the no op command in millisecond.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     */
    public long noop() throws KineticException;

    /**
     * The flush operation flushes any outstanding PUTs or DELETEs on the
     * device/simulator.
     * <p>
     * If the call returns successfully, all PUT/DELETE operations with
     * SYNC/ASYNC PersistOption received by the service prior to this are
     * flushed to the store.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see PersistOption
     */
    public void flush() throws KineticException;

    /**
     * Put the specified <code>Entry</code> entry to the persistent store.
     * Replace the version in the store with the new version. If the version in
     * the specified entry does not match the version stored in the persistent
     * store, a <code>VersionMismatchException</code> is thrown.
     * <p>
     * The specified entry is guaranteed to be persisted in the store if the
     * call returns successfully.
     * <p>
     * The entry may or may not be persisted successfully if an Exception is
     * raised. Applications may retry the put() operation or use the get()
     * operation to determine the state of the previous unsuccessful put()
     * status.
     * <p>
     * Applications may force put the entry with {@link #putForced(Entry)} API.
     * An existed entry in the store is over-written if the key matches the
     * entry in the store.
     * <p>
     * Applications may use asynchronous put operation to put entry
     * asynchronously. The result of the asynchronous operation is delivered to
     * the application's CallbackHandler.onSuccess() method.
     * <p>
     * 
     * @param entry
     *            the <code>entry</code> to be put to the persistent store.
     * 
     * @param newVersion
     *            the new version for the specified Entry.
     * 
     * @return a shallow copy of the specified entry but the value of version in
     *         the entry metadata is set to the new version.
     * 
     * @throws VersionMismatchException
     *             If the version in the specified entry does not match the
     *             version stored.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #putForced(Entry)
     * @see #putForcedAsync(Entry, CallbackHandler)
     * @see #putAsync(Entry, byte[], CallbackHandler)
     * @see CallbackHandler
     * @see CallbackResult
     */
    public Entry put(Entry entry, byte[] newVersion) throws KineticException;

    /**
     * Put the specified <code>Entry</code> to the persistent store. Force to
     * overwrite the entry in the store if existed.
     * <p>
     * The specified entry is guaranteed to be persisted in the store if the
     * call returns successfully.
     * <p>
     * The entry may or may not be persisted successfully if an Exception is
     * raised. Applications may retry the put() operation or use the get()
     * operation to determine the state of the previous put() status.
     * 
     * @param entry
     *            the <code>entry</code> to be put to the persistent store.
     * 
     * @return the same entry reference as specified in the parameter.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #put(Entry, byte[])
     * @see #putForcedAsync(Entry, CallbackHandler)
     * @see #putAsync(Entry, byte[], CallbackHandler)
     */
    public Entry putForced(Entry entry) throws KineticException;

    /**
     * Put the versioned <code>Entry</code> asynchronously. If the version in
     * the specified entry does not match the version stored in the persistent
     * store, a <code>VersionMismatchException</code> is delivered to the
     * callback instance.
     * 
     * @param entry
     *            the <code>Entry</code> to be put to the persistent store.
     * 
     * @param newVersion
     *            new version for the entry.
     * 
     * @param handler
     *            callback handler for this operation.
     * @throws KineticException
     *             if any internal errors occurred.
     * 
     * @see #putForced(Entry)
     * @see #put(Entry, byte[])
     * @see #putForcedAsync(Entry, CallbackHandler)
     */
    public void putAsync(Entry entry, byte[] newVersion,
            CallbackHandler<Entry> handler) throws KineticException;

    /**
     * Force to put the specified <code>Entry</code> asynchronously. Overwrite
     * the entry in the store if existed.
     * 
     * @param entry
     *            the <code>Entry</code> to be put to the persistent store.
     * 
     * @param handler
     *            callback handler for this operation.
     * @throws KineticException
     *             if any internal errors occurred.
     * 
     * @see #putForced(Entry)
     * @see #put(Entry, byte[])
     * @see #putAsync(Entry, byte[], CallbackHandler)
     */
    public void putForcedAsync(Entry entry, CallbackHandler<Entry> handler)
            throws KineticException;

    /**
     * Get the <code>Entry</code> entry associated with the specified key.
     * <p>
     * Applications may use other variations of this API to perform get()
     * operation. For example, {@link #getAsync(byte[], CallbackHandler)}
     * 
     * @param key
     *            the key used to obtain the entry.
     * 
     * @return the <code>Entry</code> in the persistent store if there is a
     *         match. Otherwise, returns null.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see Entry
     * @see #getAsync(byte[], CallbackHandler)
     */
    public Entry get(byte[] key) throws KineticException;

    /**
     * Get the version of the entry associated with the specified key.
     * 
     * @param key
     *            the key used to obtain the version of the matched entry.
     * 
     * @return the version of the Entry in the persistent store if there is a
     *         match. Otherwise, returns null.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     */
    public byte[] getVersion(byte[] key) throws KineticException;

    /**
     * Get the <code>Entry</code> associated with the specified key
     * asynchronously.
     * <p>
     * If there is no entry found for an asynchronous get operation, the
     * callback handler's onSuccess method is invoked.
     * <p>
     * In this scenario (entry not found), calling CallbackResult.getResult()
     * will return null. This behavior is consistent with the synchronous
     * {@link #get(byte[])} operation.
     * 
     * @param key
     *            the key used to obtain the entry.
     * 
     * @param handler
     *            get operation asynchronous callback handler
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #get(byte[])
     * @see CallbackHandler
     */
    public void getAsync(byte[] key, CallbackHandler<Entry> handler)
            throws KineticException;

    /**
     * Delete the entry that is associated with the key specified in the
     * <code>entry</code>. Applications may also use other variations of this
     * API to perform deletion of an entry. For example,
     * {@link #deleteForced(byte[])} or
     * {@link #deleteAsync(Entry, CallbackHandler)}, etc.
     * 
     * @param entry
     *            the key in the object is used to find the associated entry.
     * 
     * @return true if entry is found and deleted. Otherwise, return false.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #deleteForcedAsync(byte[], CallbackHandler)
     * @see #deleteForced(byte[])
     * @see #deleteAsync(Entry, CallbackHandler)
     */
    public boolean delete(Entry entry) throws KineticException;

    /**
     * Force to delete the entry in the store that is associated with the
     * specified key ignoring the version information of the entry.
     * <p>
     * 
     * @param key
     *            the key used to find the associated entry.
     * 
     * @return true if operation is successfully returned from server. Server
     *         does not check if entry exists in store.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #delete(Entry)
     * @see #deleteAsync(Entry, CallbackHandler)
     * @see #deleteForcedAsync(byte[], CallbackHandler)
     */
    public boolean deleteForced(byte[] key) throws KineticException;

    /**
     * Delete the entry that is associated with the key specified in the
     * <code>entry</code> asynchronously. The version specified in the entry
     * metadata must match the one stored in the persistent storage. Otherwise,
     * a KineticException is raised.
     * 
     * @param entry
     *            the key in the entry is used to find the associated entry.
     * 
     * @param handler
     *            the callback handler for the asynchronous delete operation.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #delete(Entry)
     * @see #deleteForced(byte[])
     * @see #deleteForcedAsync(byte[], CallbackHandler)
     */
    public void deleteAsync(Entry entry, CallbackHandler<Boolean> handler)
            throws KineticException;

    /**
     * Force delete the entry that is associated with the key specified in the
     * parameter asynchronously, ignoring the entry version stored in the
     * persistent store.
     * 
     * @param key
     *            the key in the entry is used to find the associated entry.
     * 
     * @param handler
     *            the callback handler for the asynchronous delete operation.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #delete(Entry)
     * @see #deleteAsync(Entry, CallbackHandler)
     * @see #deleteForced(byte[])
     */
    public void deleteForcedAsync(byte[] key, CallbackHandler<Boolean> handler)
            throws KineticException;

    /**
     * Get the <code>Entry</code> entry associated with a key that is after the
     * specified key.
     * 
     * @param key
     *            the key used to get the <code>Entry</code> associated with a
     *            key that it.
     * 
     * @return the <code>Entry</code> associated with a key that is after the
     *         specified key. Returns null if no <code>Entry</code> entry found
     *         after the specified key.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see Entry
     * @see #getNextAsync(byte[], CallbackHandler)
     */
    public Entry getNext(byte[] key) throws KineticException;

    /**
     * Get the <code>Entry</code> associated with a key that is after the
     * specified key Asynchronously.
     * 
     * @param key
     *            the key used to get the <code>Entry</code> associated with a
     *            key after it.
     * 
     * @param handler
     *            the callback handler for the asynchronous getNext operation.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see Entry
     * 
     * @see #getNext(byte[])
     */
    public void getNextAsync(byte[] key, CallbackHandler<Entry> handler)
            throws KineticException;

    /**
     * Get the <code>Entry</code> entry associated with a key that is before the
     * specified key in the sequence.
     * 
     * @param key
     *            the key used to get the <code>Entry</code> associated with a
     *            key that is before it in the sequence.
     * 
     * @return the <code>Entry</code> associated with a key that is before the
     *         specified key. Returns null if reached the end of the sequence.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #getPreviousAsync(byte[], CallbackHandler)
     */
    public Entry getPrevious(byte[] key) throws KineticException;

    /**
     * Get the <code>Entry</code> associated with a key that is before the
     * specified key in the sequence asynchronously.
     * 
     * @param key
     *            the key used to get the <code>Entry</code> associated with a
     *            key that is before it in the sequence.
     * 
     * @param handler
     *            the callback handler for the asynchronous getPrevious
     *            operation.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #getPrevious(byte[])
     */
    public void getPreviousAsync(byte[] key, CallbackHandler<Entry> handler)
            throws KineticException;

    /**
     * Get a list of keys in the sequence based on the specified key range.
     * 
     * @param startKey
     *            the start key in the specified key range.
     * @param startKeyInclusive
     *            true if the start key is inclusive.
     * @param endKey
     *            the end key in the specified key range.
     * @param endKeyInclusive
     *            true if the start key is inclusive.
     * @param maxKeys
     *            max number of keys can be returned.
     * 
     * @return a list of keys in the sequence based on the specified key range.
     *         If <code>maxKeys</code> is larger than the number of keys in the
     *         range, only the number of keys in the range will be returned. If
     *         the number of keys in the range is larger than
     *         <code>maxKeys</code>, then only <code>maxKeys</code> keys will be
     *         returned.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #getKeyRangeAsync(byte[], boolean, byte[], boolean, int,
     *      CallbackHandler)
     */
    public List<byte[]> getKeyRange(byte[] startKey, boolean startKeyInclusive,
            byte[] endKey, boolean endKeyInclusive, int maxKeys)
            throws KineticException;

    /**
     * Get a <code>List</code> of keys in the sequence based on the specified
     * key range asynchronously.
     * 
     * @param startKey
     *            the start key in the specified key range.
     * @param startKeyInclusive
     *            true if the start key is inclusive.
     * @param endKey
     *            the end key in the specified key range.
     * @param endKeyInclusive
     *            true if the start key is inclusive.
     * 
     * @param maxKeys
     *            max number of keys to be returned for this operation.
     * 
     * @param handler
     *            the callback handler for the asynchronous getKeyRange
     *            operation.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #getKeyRange(byte[], boolean, byte[], boolean, int)
     */
    public void getKeyRangeAsync(byte[] startKey, boolean startKeyInclusive,
            byte[] endKey, boolean endKeyInclusive, int maxKeys,
            CallbackHandler<List<byte[]>> handler) throws KineticException;

    /**
     * Get entry metadata for the specified key.
     * 
     * @param key
     *            the entry key.
     * 
     * @return metadata associated with the entry.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     * 
     * @see #getMetadataAsync(byte[], CallbackHandler)
     */
    public EntryMetadata getMetadata(byte[] key) throws KineticException;

    /**
     * Get the entry metadata asynchronous for the specified key.
     * <p>
     * 
     * @param key
     *            the entry key.
     * 
     * @param handler
     *            asynchronous callback handler for this operation.
     * 
     * @see #getMetadata(byte[])
     * @see CallbackHandler
     * @see EntryMetadata
     * 
     * @throws KineticException
     *             if any internal errors occur.
     */
    public void getMetadataAsync(byte[] key,
            CallbackHandler<EntryMetadata> handler) throws KineticException;

    /**
     * Create a new instance of <code>BatchOperation</code> object.
     * <p>
     * Please note that this API is only supported by the simulator and Drive
     * that implement Kinetic protocol 3.0.6 and later.
     * 
     * @return a new instance of <code>BatchOperation</code> object.
     * 
     * @since protocol version 3.0.6
     * 
     * @throws KineticException if any internal error occurred.
     */
    public BatchOperation createBatchOperation() throws KineticException;

    /**
     * Close the connection and release all resources allocated by this
     * instance.
     * 
     * @throws KineticException
     *             if any internal error occurred.
     */
    public void close() throws KineticException;
}
