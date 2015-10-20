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
 * Kinetic Entry Metadata container.
 * <p>
 * A Kinetic entry contains key, value, and entry metadata.
 * <p>
 * Kinetic applications use KineticClient API to perform Entry operations. Such
 * as {@link KineticClient#putForced(Entry)} to store an Entry in the Kinetic store.
 * <p>
 * Applications may also use {@link KineticClient#getMetadata(byte[])} or
 * {@link KineticClient#getMetadataAsync(byte[], CallbackHandler)} to obtain
 * entry metadata information.
 * 
 * @author James Hughes.
 * @author Chiaming Yang
 * 
 * @see Entry
 * @see KineticClient
 */
public class EntryMetadata {

	// version field
	private byte[] version = null;

	// tag field
	private byte[] tag = null;

	// algorithm field
	private String algorithm = null;

	/**
	 * default constructor.
	 */
	public EntryMetadata() {
		;
	}

	/**
	 * Construct meta data with the specified version, tag, and algorithm.
	 * 
	 * @param version
	 *            entry version.
	 * @param tag
	 *            entry tag
	 * @param algorithm
	 *            algorithm.
	 */
	public EntryMetadata(byte[] version, byte[] tag,
			String algorithm) {
		this.version = version;
		this.tag = tag;
		this.algorithm = algorithm;
	}

	/**
	 * Get the version field content of the entry metadata.
	 * 
	 * @return the version field in byte[] representation.
	 */
	public byte[] getVersion() {
		return version;
	}

	/**
	 * Set the version field content of the entry metadata.
	 * 
	 * @param version
	 *            the content to be set to the version field.
	 */
	public void setVersion(byte[] version) {
		this.version = version;
	}

	/**
	 * Get the tag field content of the entry metadata.
	 * 
	 * @return the tag field in byte[] representation.
	 */
	public byte[] getTag() {
		return tag;
	}

	/**
	 * Set the tag field content of the entry metadata.
	 * 
	 * @param tag
	 *            the content to be set to the tag field.
	 */
	public void setTag(byte[] tag) {
		this.tag = tag;
	}

	/**
	 * Get the algorithm field content of the entry metadata.
	 * 
	 * @return the algorithm field in String representation.
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * Set the algorithm field content of the entry.
	 * 
	 * @param algorithm
	 *            the content to be set to the algorithm field.
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
}
