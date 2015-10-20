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
package kinetic.client.advanced;

import java.util.List;

import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;

/**
 * 
 * The AdvancedKineticClient defines extended interfaces for PUT/DELETE
 * operations.
 * <p>
 * Applications use these interfaces to control when a PUT/GET operation should
 * be synced/flushed to the persistent storage.
 * <p>
 * The default persist option is set to SYNC if not specified.
 * <p>
 * If FLUSH option is used and the operation is successfully returned (callback
 * handler is invoked with SUCCESS status), all operations performed with ASYNC
 * option are also 'synced' to the persistent store.
 * 
 * @see PersistOption
 * 
 * @author chiaming
 * 
 */
public interface AdvancedKineticClient extends KineticClient {

	/**
	 * Delete the entry that is associated with the key specified in the
	 * <code>entry</code>. Applications may also use other variations of this
	 * API to perform deletion of an entry. For example,
	 * {@link #deleteForced(byte[])} or
	 * {@link #deleteAsync(Entry, CallbackHandler)}, etc.
	 * <p>
	 * 
	 * @param entry
	 *            the key in the object is used to find the associated entry.
	 * 
	 * @param option
	 *            instruct the service to perform SYNC/ASYNC/FLUSH operation.
	 * 
	 * @return true if entry is found and deleted. Otherwise, return false.
	 * 
	 * @throws KineticException
	 *             if any internal error occurred.
	 * 
	 * @see #deleteForcedAsync(byte[], CallbackHandler)
	 * @see #deleteForcedAsync(byte[], PersistOption, CallbackHandler)
	 * @see #deleteForced(byte[])
	 * @see #deleteForced(byte[], PersistOption)
	 * @see #deleteAsync(Entry, CallbackHandler)
	 * @see #deleteAsync(Entry, PersistOption, CallbackHandler)
	 */
	public boolean delete(Entry entry, PersistOption option)
			throws KineticException;

	/**
	 * Delete the entry that is associated with the key specified in the
	 * <code>entry</code> asynchronously. The version specified in the entry
	 * metadata must match the one stored in the persistent storage. Otherwise,
	 * a KineticException is raised.
	 * 
	 * @param entry
	 *            the key in the entry is used to find the associated entry.
	 * 
	 * @param option
	 *            instruct the service to perform SYNC/ASYNC/FLUSH operation.
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
	 * 
	 * @see #delete(Entry, PersistOption)
	 * @see #deleteForcedAsync(byte[], PersistOption, CallbackHandler)
	 */
	public void deleteAsync(Entry entry, PersistOption option,
			CallbackHandler<Boolean> handler) throws KineticException;

	/**
	 * Force to delete the entry in the store that is associated with the
	 * specified key ignoring the version information of the entry.
	 * <p>
	 * 
	 * @param key
	 *            the key used to find the associated entry.
	 * 
	 * @param option
	 *            instruct the service to perform SYNC/ASYNC/FLUSH operation.
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
	public boolean deleteForced(byte[] key, PersistOption option)
			throws KineticException;

	/**
	 * Force delete the entry that is associated with the key specified in the
	 * parameter asynchronously, ignoring the entry version stored in the
	 * persistent store.
	 * 
	 * @param key
	 *            the key in the entry is used to find the associated entry.
	 * 
	 * @param option
	 *            instruct the service to perform SYNC/ASYNC/FLUSH operation.
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
	public void deleteForcedAsync(byte[] key, PersistOption option,
			CallbackHandler<Boolean> handler) throws KineticException;


	/**
	 * Put the specified <code>Entry</code> entry to the persistent store.
	 * Replace the version in the store with the new version. If the version in
	 * the specified entry does not match the version stored in the persistent
	 * store, a <code>KineticException</code> is thrown.
	 * <p>
	 * The specified entry is guaranteed to be persisted in the store if
	 * SYNC/FLUSH option is used and the call returns successfully.
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
	 * @param option
	 *            instruct the service to perform SYNC/ASYNC/FLUSH operation.
	 * 
	 * 
	 * @return a shallow copy of the specified entry but the value of version in
	 *         the entry metadata is set to the new version.
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
	public Entry put(Entry entry, byte[] newVersion, PersistOption option)
			throws KineticException;

	/**
	 * Put the versioned <code>Entry</code> asynchronously. If the version in
	 * the specified entry does not match the version stored in the persistent
	 * store, a <code>KineticException</code> is delivered to the callback
	 * instance.
	 * 
	 * @param entry
	 *            the <code>Entry</code> to be put to the persistent store.
	 * 
	 * @param newVersion
	 *            new version for the entry.
	 * 
	 * @param option
	 *            instruct the service to perform SYNC/ASYNC/FLUSH operation.
	 * 
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
	public void putAsync(Entry entry, byte[] newVersion, PersistOption option,
			CallbackHandler<Entry> handler) throws KineticException;

	/**
	 * Put the specified <code>Entry</code> to the persistent store. Force to
	 * overwrite the entry in the store if existed.
	 * <p>
	 * The specified entry is guaranteed to be persisted in the store if
	 * SYNC/FLUSH option is used and the call returns successfully.
	 * <p>
	 * The entry may or may not be persisted successfully if an Exception is
	 * raised. Applications may retry the put() operation or use the get()
	 * operation to determine the state of the previous put() status.
	 * 
	 * @param entry
	 *            the <code>entry</code> to be put to the persistent store.
	 * 
	 * @param option
	 *            instruct the service to perform SYNC/ASYNC/FLUSH operation.
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
	public Entry putForced(Entry entry, PersistOption option)
			throws KineticException;

	/**
	 * Force to put the specified <code>Entry</code> asynchronously. Overwrite
	 * the entry in the store if existed.
	 * 
	 * @param entry
	 *            the <code>Entry</code> to be put to the persistent store.
	 * 
	 * @param option
	 *            instruct the service to perform SYNC/ASYNC/FLUSH operation.
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
	public void putForcedAsync(Entry entry, PersistOption option,
			CallbackHandler<Entry> handler) throws KineticException;

	/**
	 * Get a list of keys in the sequence based on the specified key range. The
	 * returned keys are sorted in reversed order.
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
	 * @see #getKeyRangeReversedAsync(byte[], boolean, byte[], boolean, int,
	 *      CallbackHandler)
	 */
	public List<byte[]> getKeyRangeReversed(byte[] startKey,
			boolean startKeyInclusive, byte[] endKey, boolean endKeyInclusive,
			int maxKeys) throws KineticException;

	/**
	 * Get a <code>List</code> of keys in the sequence based on the specified
	 * key range in a reversed order asynchronously.
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
	public void getKeyRangeReversedAsync(byte[] startKey,
			boolean startKeyInclusive, byte[] endKey, boolean endKeyInclusive,
			int maxKeys, CallbackHandler<List<byte[]>> handler)
					throws KineticException;

}
