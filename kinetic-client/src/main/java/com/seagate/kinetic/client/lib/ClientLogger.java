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

package com.seagate.kinetic.client.lib;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ClientLogger {

	private final static Logger LOG = Logger.getLogger("ClientLogger");
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
		LOG.fine("ClientLogger initalized");
		handler.flush();
	}
}
