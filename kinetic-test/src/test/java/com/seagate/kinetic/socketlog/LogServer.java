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
package com.seagate.kinetic.socketlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

public class LogServer {
	private static int PORT = 60123;

	public static void main(String args[]) {
		if (args.length != 0 && args.length != 1) {
			System.out.println("Parameters error!!!");
			System.out.println("Usage:");
			System.out.println("logserver [port]");
			System.out.println("Welcome to try again.");
			return;

		}
		if (args.length == 1) {
			PORT = Integer.parseInt(args[0]);
			System.out.println("Log server port=" + PORT);

		}

		ServerSocketFactory serverSocketFactory = ServerSocketFactory
				.getDefault();
		ServerSocket serverSocket = null;
		try {
			serverSocket = serverSocketFactory.createServerSocket(PORT);
		} catch (IOException ignored) {
			System.err.println("Unable to create server");
			System.exit(-1);
		}
		System.out.printf("LogServer running on port: %s%n", PORT);
		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				InputStream is = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						is, "US-ASCII"));
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException exception) {
				// Just handle next request.
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException ignored) {
					}
				}
			}
		}
	}
}
