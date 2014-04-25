/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.seagate.kinetic.simulator.persist;

import java.util.List;
import java.util.SortedMap;

//import PersistOption;
import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.simulator.internal.KVStoreException;

/**
 *
 * DB Application (Raw) Interface.
 *
 * @author James Hughes.
 * @author Chenchong Li
 */
public interface Store<K, O, V> {

    /**
     * Initialize the store with server configuration instance. The simulator
     * calls this method immediately after the implementation class is
     * instantiated.
     *
     * @param config
     *            the configuration to be used for this db instance.
     */
    public void init(SimulatorConfiguration config);

    /**
     * Put the specified <code>K, O, V</code> entry to the persistent store.
     *
     * @param key
     *            the <code>key</code> to be put to the persistent store.
     *
     * @param oldVersion
     *            the <code>oldVersion</code> to be compare with the Version get
     *            from the persistent store.
     *
     * @param value
     *            the <code>value</code> to be put to the persistent store.
     *
     * @return null
     *
     * @throws KVStoreException
     *             if any internal error occurred.
     */
    void put(K key, O oldVersion, V value, PersistOption option)
            throws KVStoreException;

    /**
     * Force to put the specified <code>K, V</code> entry to the persistent
     * store.
     *
     * @param key
     *            the <code>key</code> to be forced to put to the persistent
     *            store.
     *
     * @param value
     *            the <code>value</code> to be forced put to the persistent
     *            store.
     *
     * @return null
     *
     * @throws KVStoreException
     *             if any internal error occurred.
     */
    void putForced(K key, V value, PersistOption option)
            throws KVStoreException;

    /**
     * Delete the entry that is associated with the key specified in the
     * persistent store
     *
     * @param key
     *            the key in the object is used to find the associated entry.
     *
     * @param oldVersion
     *            the <code>oldVersion</code> to be compare with the Version get
     *            from the persistent store.
     *
     * @return null
     *
     * @throws KVStoreException
     *             if any internal error occurred.
     */
    void delete(K key, O oldVersion, PersistOption option)
            throws KVStoreException;

    /**
     * Force to delete the entry that is associated with the key specified in
     * the persistent store
     *
     * @param key
     *            the key in the object is used to find the associated entry.
     *
     * @return null
     *
     * @throws KVStoreException
     *             if any internal error occurred.
     */
    void deleteForced(K key, PersistOption option) throws KVStoreException;

    /**
     * Get the <code>key</code> entry associated with the specified key.
     *
     * @param key
     *            the key used to obtain the entry.
     *
     * @return the <code>V</code> in the persistent store if there is a match.
     *         Otherwise, returns different operation status message.
     *
     * @throws KVStoreException
     *             if any internal error occurred.
     */
    V get(K key) throws KVStoreException;

    /**
     * Get the <code>V</code> entry associated with a key that is before the
     * specified key in the sequence.
     *
     * @param key
     *            the key used to get the <code>V</code> associated with a key
     *            that is before it in the sequence.
     *
     * @return the <code>V</code> associated with a key that is before the
     *         specified key. Returns different operation status message if
     *         reached the end of the sequence.
     *
     * @throws KVStoreException
     *             if any internal error occurred.
     */
    V getPrevious(K key) throws KVStoreException;

    /**
     * Get the <code>V</code> entry associated with a key that is after the
     * specified key.
     *
     * @param key
     *            the key used to get the <code>V</code> associated with a key
     *            that it.
     *
     * @return the <code>V</code> associated with a key that is after the
     *         specified key. Returns different operation status message if no
     *         <code>V</code> entry found after the specified key.
     *
     * @throws KVStoreException
     *             if any internal error occurred.
     */
    V getNext(K key) throws KVStoreException;

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
     * @param maxReturned
     *            the maximum entry to be returned in the list.
     *
     * @return a list of keys in the sequence based on the specified key range.
     *         If <code>maxReturned</code> is larger than the number of keys in
     *         the range, only the number of keys in the range will be returned.
     *         If the number of keys in the range is larger than
     *         <code>maxReturned</code>, then only <code>maxReturned</code> keys
     *         will be returned.
     *
     * @throws KVStoreException
     *             if any internal error occurred.
     */
    SortedMap<?, ?> getRange(K startKey, boolean startKeyInclusive, K endKey,
            boolean endKeyInclusive, int n) throws KVStoreException;

    /**
     * Get a list of reversed keys in the sequence based on the specified key
     * range.
     *
     * @param startKey
     *            the start key in the specified key range.
     * @param startKeyInclusive
     *            true if the start key is inclusive.
     * @param endKey
     *            the end key in the specified key range.
     * @param endKeyInclusive
     *            true if the start key is inclusive.
     * @param maxReturned
     *            the maximum entry to be returned in the list.
     * @return a list of reversed keys in the sequence based on the specified
     *         key range. If <code>maxReturned</code> is larger than the number
     *         of keys in the range, only the number of keys in the range will
     *         be returned. If the number of keys in the range is larger than
     *         <code>maxReturned</code>, then only <code>maxReturned</code> keys
     *         will be returned.
     *
     * @throws KVStoreException
     *             if any internal error occurred.
     */
    List<?> getRangeReversed(K startKey, boolean startKeyInclusive, K endKey,
            boolean endKeyInclusive, int n) throws KVStoreException;

    /**
     * Close the connection and release all resources allocated by this
     * instance.
     */
    void close();

    /*
     * Erase the store and recreate it
     */
    void reset() throws KVStoreException;
}
