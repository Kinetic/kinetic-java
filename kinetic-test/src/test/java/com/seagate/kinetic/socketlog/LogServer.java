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
