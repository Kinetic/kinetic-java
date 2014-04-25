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
