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

package com.seagate.kinetic.common.lib;

import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.crypto.Mac;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.seagate.kinetic.proto.Kinetic.Command;
import com.seagate.kinetic.proto.Kinetic.Message;

/**
 * Hmac common library.
 *
 * @author Jim Hugues.
 * @author chiaming
 *
 */
public class Hmac {

	static final Hmac h = new Hmac();

	private final static Logger LOG = Logger.getLogger(Hmac.class.getName());

	public static String toString(byte[] b) {
		final int MAX_LENGTH = 50; // only include up to MAX_LENGTH bytes
		StringWriter sw = new StringWriter();
		int length = b.length;
		if (length > MAX_LENGTH) {
			length = MAX_LENGTH;
		}

		for (int i = 0; i < length; i++) {
			sw.append(String.format("%02x ", b[i]));
		}
		return sw.toString();
	}

	public static String toString(ByteString s) {
		byte[] b = s.toByteArray();
		return toString(b);
	}

	private Hmac() {
	}

	static private void oops(String s) throws HmacException {
		// oops(Message.Header.Status.INTERNAL_ERROR, s);
		oops(Command.Status.StatusCode.INTERNAL_ERROR, s);
	}

	public class HmacException extends Exception {
		private static final long serialVersionUID = 5201751340412081922L;

		// Message.Header.Status status;
		Command.Status.StatusCode status;

		public HmacException(Command.Status.StatusCode status, String s) {
			super(s);
			this.status = status;
		}
	}

	static private void oops(Command.Status.StatusCode status, String s)
			throws HmacException {
		throw h.new HmacException(status, s);
	}

	static private byte[] int32(int x) {
		return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(x)
				.array();
	}

	// private byte[] int64(long x) {
	// return ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(x)
	// .array();
	// }

	static void lv(String name, Mac mac, GeneratedMessage m) {

		byte[] bytes = m.toByteArray();

		lv(name, mac, bytes);

		// if (x.length > 0) {
		// LOG.fine(name + toString(x));
		// mac.update(int32(x.length));
		// mac.update(x);
		// }
	}

	static void lv(String name, Mac mac, byte[] bytes) {
		if (bytes.length > 0) {
			// LOG.fine(name + toString(bytes));
			mac.update(int32(bytes.length));
			mac.update(bytes);
		}
	}

//	public static ByteString calc(KineticMessage im, Key key)
//			throws HmacException {
//
//		try {
//
//			//Mac mac = getMacInstance (users, user);
//
//			Mac mac = getMacInstance (key);
//
//			lv("command", mac, im.getMessage().getCommand().toByteArray());
//
//			ByteString result = ByteString.copyFrom(mac.doFinal());
//
//			LOG.fine("Message Hmac :" + toString(result));
//			return result;
//
//		} catch (GeneralSecurityException e) {
//			oops(e.getMessage());
//		}
//		return null; // should never get here...
//	}
	
	/**
	 * Calculate HMAC based on the specified bytes and key.
	 * 
	 * @param bytes bytes for HMAC calculation
	 * @param key security key used to calculate HMAC
	 * @return byte string of hmac value
	 * @throws HmacException 
	 */
	public static ByteString calc(byte[] bytes, Key key)
            throws HmacException {

        try {

            Mac mac = getMacInstance (key);

            lv("command", mac, bytes);

            ByteString result = ByteString.copyFrom(mac.doFinal());

            LOG.fine("Message Hmac :" + toString(result));
            return result;

        } catch (GeneralSecurityException e) {
            oops(e.getMessage());
        }
        return null; // should never get here...
    }

	public static ByteString calcTag(KineticMessage im, Key key) {

		ByteString result = null;

		try {

			// Message.Builder message = (Builder) im.getMessage();

			Mac mac = getMacInstance(key);

			byte[] value = im.getValue();
			if (value == null) {
				value = new byte[0];
			}
			lv("tag", mac, value);

			result = ByteString.copyFrom(mac.doFinal());

			LOG.fine("Message Tag Hmac :" + toString(result));

		} catch (Exception e) {
			LOG.warning(e.getMessage());
		}

		return result;
	}

	public static boolean check(KineticMessage km, Key key)
			throws HmacException {

	    // get commnad bytes
	    byte[] bytes = km.getMessage().getCommandBytes().toByteArray();
	    
	    // get expected hmac value
	    ByteString expected = km.getMessage().getHmacAuth().getHmac();
	    
	    // calculate hmac and compare to expected value
	    if (check (bytes, key, expected)) {
	        return true;
	    }

		LOG.warning("HMAC did not compare");
		return false;
	}
	
	/**
	 * Check if the specified byte[] is equal to the expected hmac with the specified key. 
	 * @param bytes
	 * @param key
	 * @param expectedHmac
	 * @return true or false
	 * @throws HmacException
	 */
	public static boolean check(byte[] bytes, Key key, ByteString expectedHmac)
            throws HmacException {

        if (calc(bytes, key).equals(expectedHmac)) {
            return true;
        }

        LOG.warning("HMAC did not compare");
        return false;
    }

	public static Mac getMacInstance (Key key) throws HmacException, NoSuchAlgorithmException, InvalidKeyException {

		Mac mac = null;

		//Key key = map.get(user);

		if (key == null) {
			oops("User not found");
		}

		mac = Mac.getInstance(key.getAlgorithm());

		mac.init(key);

		return mac;
	}

}
