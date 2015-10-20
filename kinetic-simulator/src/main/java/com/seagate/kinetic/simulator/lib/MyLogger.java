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

package com.seagate.kinetic.simulator.lib;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MyLogger {

	private final static Logger LOG = Logger.getLogger("MyLogger");
	private final static Handler handler = new ConsoleHandler();

	static class myFormatter extends java.util.logging.SimpleFormatter {
		public String format(LogRecord record) {
			return String.format("%12s, %50s, %15s, %s \r\n",
					record.getLevel(), record.getSourceClassName(),
					record.getSourceMethodName(), record.getMessage());
		}
	}

	public static Logger get() {
		return LOG;
	}

	static {
		handler.setLevel(Level.INFO);
		java.util.logging.Formatter formatter = new myFormatter();
		handler.setFormatter(formatter);

		LOG.setLevel(Level.INFO);
		LOG.addHandler(handler);
		LOG.fine("MyLogger initalized");
		handler.flush();
	}
}
