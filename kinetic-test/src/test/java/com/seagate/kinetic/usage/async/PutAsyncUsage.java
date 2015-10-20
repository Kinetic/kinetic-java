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
package com.seagate.kinetic.usage.async;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import kinetic.client.CallbackHandler;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticException;

/**
 * 
 * put async example, no blocking.
 * <p>
 * 
 * @author Chenchong(Emma) Li
 * 
 */
class PutAsyncUsage {
	private final Logger logger = Logger.getLogger(PutAsyncUsage.class
			.getName());

	private CallbackHandler<Entry> callback = null;
	private Map<String, Entry> entryMap = null;
	private int limit;

	public PutAsyncUsage(int limit) {
		this.limit = limit;
		entryMap = new HashMap<String, Entry>();
	}

	public synchronized void Put(Entry entry, byte[] newVersion,
			KineticClient client) throws InterruptedException, KineticException {

		while (entryMap.size() >= limit) {
			wait();
			logger.info("entryMap arrives limit, waiting notify...");
		}

		entryMap.put(new String(entry.getKey()), entry);

		callback = new PutAsyncCallbackHandler(this);
		client.putAsync(entry, newVersion, callback);

	}

	public synchronized void received(Entry entry) {
		byte[] key = entry.getKey();
		entryMap.remove(new String(key));

		this.notifyAll();

	}
}
