/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
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
