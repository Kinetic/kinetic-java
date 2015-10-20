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
package com.seagate.kinetic;

import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SocketHandler;

public abstract class IntegrationTestLoggerFactory {
	public static Logger getLogger(String className) {
		Logger logger = Logger.getAnonymousLogger();
		String defaultClassName = "com.seagate.kinetic.socketlog.DefaultLogFormatter";
		boolean runSocketLogger = Boolean.parseBoolean(System
				.getProperty("USE_SOCKET_LOGGER"));

		if (runSocketLogger) {
			logger = Logger.getAnonymousLogger();
			String logSocketHost = System.getProperty("LOG_SOCKET_HOST",
					"localhost");
			int logSocketPort = Integer.parseInt(System.getProperty(
					"LOG_SOCKET_PORT", "60123"));
			String formatterClassName = System.getProperty(
					"KINETIC_LOG_FORMATTER", defaultClassName);

			try {
				Handler handler = new SocketHandler(logSocketHost,
						logSocketPort);

				// Formatter myFormatter = new SimpleFormatter();
				Formatter myFormatter = (Formatter) Class.forName(
						formatterClassName).newInstance();
				handler.setFormatter(myFormatter);

				logger.addHandler(handler);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("System exit, can't connect to log server "
						+ logSocketHost + ":" + logSocketPort);
				System.exit(1);
			} catch (InstantiationException e) {
				e.printStackTrace();
				System.err.println("System exit, can't init formatter "
						+ formatterClassName);
				System.exit(1);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				System.err.println("System exit, can't access formatter "
						+ formatterClassName);
				System.exit(1);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.err.println("System exit, can't find formatter class "
						+ formatterClassName);
				System.exit(1);
			}

		} else {
			logger = Logger.getLogger(className);
		}

		return logger;
	}
}
