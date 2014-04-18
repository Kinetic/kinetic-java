/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
