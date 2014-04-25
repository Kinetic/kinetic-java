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
package kinetic.client;

/**
 * 
 * KineticClient Entry entry container.
 * <p>
 * A Kinetic entry contains key, value, and entry metadata.
 * <p>
 * Kinetic applications use KineticClient API to perform Entry operations. Such
 * as {@link KineticClient#putForced(Entry)} to store an Entry in the Kinetic store.
 * 
 * @author James Hughes.
 * @author Chiaming Yang
 * 
 * @see KineticClient
 * @see EntryMetadata
 */
public class Entry {

	// key field
	private byte[] key = null;

	// value value field
	private byte[] value = null;

	// metadata
	private EntryMetadata metadata = null;

	/**
	 * default constructor.
	 */
	public Entry() {
		this.metadata = new EntryMetadata();
	}

	/**
	 * 
	 * Construct an entry with the specified key and value.
	 * 
	 * @param key
	 *            key of the entry
	 * @param value
	 *            value of the entry
	 */
	public Entry(byte[] key, byte[] value) {
		this.key = key;
		this.value = value;
		this.metadata = new EntryMetadata();
	}

	/**
	 * Construct an entry with the specified key, value, and metadata.
	 * 
	 * @param key
	 *            key of the entry
	 * @param value
	 *            value of the entry
	 * @param metadata
	 *            metadata of the entry
	 */
	public Entry(byte[] key, byte[] value, EntryMetadata metadata) {
		this.key = key;
		this.value = value;

		if (metadata == null) {
			this.metadata = new EntryMetadata();
		} else {
			this.metadata = metadata;
		}
	}

	/**
	 * Get the key field content of the entry.
	 * 
	 * @return the key field in byte[] representation.
	 */
	public byte[] getKey() {
		return this.key;
	}

	/**
	 * Set the key field content of the entry.
	 * 
	 * @param key
	 *            the content to be set to the key field.
	 */
	public void setKey(byte[] key) {
		this.key = key;
	}

	/**
	 * Get the value field content of the entry.
	 * 
	 * @return the content of the value field.
	 */
	public byte[] getValue() {
		return this.value;
	}

	/**
	 * Set the content of the value field of the entry.
	 * 
	 * @param value
	 *            the content of the value field to be set to the entry.
	 */
	public void setValue(byte[] value) {
		this.value = value;
	}

	/**
	 * Get the meta data field content of the entry.
	 * 
	 * @return the content of the meta data field.
	 */
	public EntryMetadata getEntryMetadata() {
		return this.metadata;
	}

	/**
	 * Set entry meta data for this entry.
	 * 
	 * @param metadata
	 *            metadata associated with this entry.
	 */
	public void setEntryMetadata(EntryMetadata metadata) {
		this.metadata = metadata;
	}
}
