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
package com.seagate.kinetic.client.internal;

import java.util.ArrayList;
import java.util.List;

import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Command.Status.StatusCode;

/**
 * Callback result message factory.
 *
 * @author chiaming
 *
 * @see CallbackResult
 */
public class CallbackResultMessageFactory {

	/**
	 * Create a new instance of put operation callback result based on the
	 * specified callback context.
	 *
	 * @param context
	 *            callback context associated with this callback result.
	 *
	 * @return A new instance of the callback result.
	 */
	public static CallbackResult<Entry> createPutCallbackResultMessage(
			CallbackContext<Entry> context) {

		// request message
		KineticMessage request = context.getRequestMessage();

		// response message
		KineticMessage response = context.getResponseMessage();

		// new entry.
		Entry entry = new Entry();

		// set entry key
		entry.setKey(request.getCommand().getBody().getKeyValue()
				.getKey()
				.toByteArray());

		// set value if any
		if (request.getValue() != null) {
			entry.setValue(request.getValue());
		}

		// set metadata
		EntryMetadata metadata = entry.getEntryMetadata();

		// set version
		if (request.getCommand().getBody().getKeyValue()
				.hasNewVersion()) {
			metadata.setVersion(request.getCommand().getBody()
					.getKeyValue()
					.getNewVersion().toByteArray());
		}

		// set body
		if (request.getCommand().getBody().getKeyValue().hasTag()) {
			metadata.setTag(request.getCommand().getBody()
					.getKeyValue()
					.getTag().toByteArray());
		}

		// set algorithm
		if (request.getCommand().getBody().getKeyValue()
				.hasAlgorithm()) {
			metadata.setAlgorithm(request.getCommand().getBody()
					.getKeyValue()
					.getAlgorithm().toString());
		}

		// callback result
		AsyncCallbackResult<Entry> result = new AsyncCallbackResult<Entry>(
				request, response, entry);

		// result
		return result;
	}

	/**
	 * Create a new instance of get operation callback result based on the
	 * specified callback context.
	 *
	 * @param context
	 *            callback context associated with this callback result
	 *
	 * @return callback result.
	 */
	public static CallbackResult<Entry> createGetCallbackResultMessage(
			CallbackContext<Entry> context) {

		// request message
		KineticMessage request = context.getRequestMessage();

		// response message
		KineticMessage response = context.getResponseMessage();

		// transform response to Entry
		Entry entry = MessageFactory.responsetoEntry(response);

		// callback result
		AsyncCallbackResult<Entry> result = new AsyncCallbackResult<Entry>(
				request, response, entry);

		// result
		return result;
	}

	/**
	 * Create a new instance of get meta data operation callback result based on
	 * the specified callback context.
	 *
	 * @param context
	 *            callback context associated with this callback result
	 * @return callback result
	 */
	public static CallbackResult<EntryMetadata> createGetMetadataCallbackResultMessage(
			CallbackContext<EntryMetadata> context) {

		KineticMessage request = context.getRequestMessage();
		KineticMessage response = context.getResponseMessage();

		Entry entry = MessageFactory.responsetoEntry(response);

		// callback result
		AsyncCallbackResult<EntryMetadata> result;
		if (null != entry) {
			result = new AsyncCallbackResult<EntryMetadata>(request, response,
					entry.getEntryMetadata());
		} else {
			result = new AsyncCallbackResult<EntryMetadata>(request, response,
					null);
		}

		return result;
	}

	/**
	 * Create a new instance of get key range callback result based on the
	 * specified callback context.
	 *
	 * @param context
	 *            callback context associated with this callback result
	 * @return callback result associated with the specified context.
	 */
	public static CallbackResult<List<byte[]>> createGetKeyRangeCallbackResultMessage(
			CallbackContext<List<byte[]>> context) {

		KineticMessage request = context.getRequestMessage();
		KineticMessage response = context.getResponseMessage();

		List<byte[]> listOfByteArray = new ArrayList<byte[]>();

		List<ByteString> bsList = response.getCommand().getBody()
				.getRange().getKeysList();
		
		for (ByteString bs : bsList) {
			listOfByteArray.add(bs.toByteArray());
		}

		// callback result
		AsyncCallbackResult<List<byte[]>> result = new AsyncCallbackResult<List<byte[]>>(
				request, response, listOfByteArray);

		return result;
	}

	/**
	 * Create a new instance of delete callback result based on the specified
	 * callback context.
	 *
	 * @param context
	 *            callback context associated with this callback result
	 * @return callback result associated with the specified context.
	 */
	public static CallbackResult<Boolean> createDeleteCallbackResultMessage(
			CallbackContext<Boolean> context) {

		KineticMessage request = context.getRequestMessage();
		KineticMessage response = context.getResponseMessage();

		boolean deleted = (response.getCommand().getStatus()
				.getCode() == StatusCode.SUCCESS);

		// callback result
		AsyncCallbackResult<Boolean> result = new AsyncCallbackResult<Boolean>(
				request, response, Boolean.valueOf(deleted));

		return result;
	}
}
