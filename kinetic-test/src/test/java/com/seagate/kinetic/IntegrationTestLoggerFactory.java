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
