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
package com.seagate.kinetic.common.lib;

import com.seagate.kinetic.proto.Kinetic.MessageOrBuilder;

/**
 * A data container that holds a Kinetic protocol buffer message and an optional
 * byte[] value.
 * <p>
 * Please note that instances of this class does not provide synchronization and
 * thus API user must provide synchronization facility for the concurrent
 * operations.
 * <p>
 *
 * @author chiaming
 *
 */
public class KineticMessage {

	// protocol buffer message
	private MessageOrBuilder message = null;

	// optional value
	private byte[] value = null;

	/**
	 * Set protocol buffer message.
	 *
	 * @param message
	 *            message to be set in this instance.
	 */
	public void setMessage(MessageOrBuilder message) {
		this.message = message;
	}

	/**
	 * Get protocol buffer message.
	 *
	 * @return protocol buffer message in this instance.
	 */
	public MessageOrBuilder getMessage() {
		return this.message;
	}

	/**
	 * Set value to this message instance.
	 *
	 * @param value
	 *            value to bes et into this message instance.
	 */
	public void setValue(byte[] value) {
		this.value = value;
	}

	/**
	 * Get value from this message instance.
	 *
	 * @return value from this message instance.
	 */
	public byte[] getValue() {
		return this.value;
	}

}
