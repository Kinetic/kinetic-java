/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
