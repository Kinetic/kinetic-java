/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.internal;

import java.util.ArrayList;
import java.util.List;

import kinetic.client.CallbackResult;
import kinetic.client.Entry;
import kinetic.client.EntryMetadata;

import com.google.protobuf.ByteString;
import com.seagate.kinetic.common.lib.KineticMessage;
import com.seagate.kinetic.proto.Kinetic.Message.Status.StatusCode;

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
		entry.setKey(request.getMessage().getCommand().getBody().getKeyValue()
				.getKey()
				.toByteArray());

		// set value if any
		if (request.getValue() != null) {
			entry.setValue(request.getValue());
		}

		// set metadata
		EntryMetadata metadata = entry.getEntryMetadata();

		// set version
		if (request.getMessage().getCommand().getBody().getKeyValue()
				.hasNewVersion()) {
			metadata.setVersion(request.getMessage().getCommand().getBody()
					.getKeyValue()
					.getNewVersion().toByteArray());
		}

		// set body
		if (request.getMessage().getCommand().getBody().getKeyValue().hasTag()) {
			metadata.setTag(request.getMessage().getCommand().getBody()
					.getKeyValue()
					.getTag().toByteArray());
		}

		// set algorithm
		if (request.getMessage().getCommand().getBody().getKeyValue()
				.hasAlgorithm()) {
			metadata.setAlgorithm(request.getMessage().getCommand().getBody()
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

		List<ByteString> bsList = response.getMessage().getCommand().getBody()
				.getRange()
				.getKeyList();
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

		boolean deleted = (response.getMessage().getCommand().getStatus()
				.getCode() == StatusCode.SUCCESS);

		// callback result
		AsyncCallbackResult<Boolean> result = new AsyncCallbackResult<Boolean>(
				request, response, Boolean.valueOf(deleted));

		return result;
	}
}
