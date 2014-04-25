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
