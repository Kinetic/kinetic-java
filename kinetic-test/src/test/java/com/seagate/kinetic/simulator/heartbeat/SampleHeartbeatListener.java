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
package com.seagate.kinetic.simulator.heartbeat;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.seagate.kinetic.heartbeat.HeartbeatMessage;
import com.seagate.kinetic.monitor.HeartbeatListener;

public class SampleHeartbeatListener extends HeartbeatListener {

	private final static Logger logger = Logger
			.getLogger(SampleHeartbeatListener.class.getName());

	public SampleHeartbeatListener() throws IOException {
		super();
	}

	@Override
	public void onMessage(byte[] data) {

		try {

			String message = new String(data, "UTF8");

			JsonReader reader = new JsonReader(new StringReader(message));
			reader.setLenient(true);

			// pretty print use this
			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			// normal print, use this
			// Gson gson = new Gson();

			HeartbeatMessage hbm = gson
					.fromJson(reader, HeartbeatMessage.class);

			String jsonOutput = gson.toJson(hbm);

			logger.info("received heart beat: " + jsonOutput);

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

	}

	public static void main(String[] args) throws IOException {
		// heart beat listener
		@SuppressWarnings("unused")
		SampleHeartbeatListener listener = new SampleHeartbeatListener();
	}

}
