// Do NOT modify or remove this copyright and confidentiality notice!
//
// Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
//
// The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
// Portions are also trade secret. Any use, duplication, derivation, distribution
// or disclosure of this code, for any reason, not expressly authorized is
// prohibited. All other rights are expressly reserved by Seagate Technology, LLC.

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
