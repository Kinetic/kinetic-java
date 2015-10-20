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
package com.seagate.kinetic.heartbeat.message;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

/**
 * 
 * Heart beat prototype message.
 * 
 */
public class HeartbeatMessageToBeDeleted {

	// message source host name/ip
	private String host = "localhost";

	// message source port
	private int port = 8123;

	// message source tls port
	private int tlsPort = 8443;

	//
	private final List<Temperature> temperatures = new ArrayList<Temperature>();

	private final List<Utilization> utilizations = new ArrayList<Utilization>();

	private Capacity capacity = null;

	// command operation counter
	private OperationCounter operationCounters = null;

	// byte counters
	private ByteCounter byteCounters = null;

	// private double timestamp = 0;

	// gson to
	private static Gson gson = new Gson();

	public HeartbeatMessageToBeDeleted() {
		// timestamp = System.currentTimeMillis();
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return this.host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

	public void setTlsPort(int port) {
		this.tlsPort = port;
	}

	public int getTlsPort() {
		return this.tlsPort;
	}

	public void setCapacity(Capacity capacity) {
		this.capacity = capacity;
	}

	public Capacity getCapacity() {
		return this.capacity;
	}

	public void addUtilization(Utilization util) {
		this.utilizations.add(util);
	}

	public List<Utilization> getUtilizations() {
		return this.utilizations;
	}

	public void addTemperature(Temperature temp) {
		this.temperatures.add(temp);
	}

	public List<Temperature> getTemperatures() {
		return this.temperatures;
	}

	public void setOperationCounter(OperationCounter opCounter) {
		this.operationCounters = opCounter;
	}

	public OperationCounter getOperationCounter() {
		return this.operationCounters;
	}

	public void setByteCounter(ByteCounter byteCounter) {
		this.byteCounters = byteCounter;
	}

	public ByteCounter getByteCounter() {
		return this.byteCounters;
	}

	public static String toJson(Object obj) {
		return gson.toJson(obj, HeartbeatMessageToBeDeleted.class);
	}

	public static HeartbeatMessageToBeDeleted fromJson(String str) {
		return gson.fromJson(str, HeartbeatMessageToBeDeleted.class);
	}

	public static void main(String[] args) throws UnsupportedEncodingException {

		HeartbeatMessageToBeDeleted hbm = new HeartbeatMessageToBeDeleted();

		// capacity
		Capacity cap = new Capacity();
		cap.setRemaining( (float)10000.1);

		cap.setTotal((float) 1024 * 10 * 10 * 10);

		hbm.setCapacity(cap);

		// temp
		Temperature temp = new Temperature();
		temp.setName("myname");
		temp.setMaximum(100);
		temp.setMinimum(0);
		temp.setCurrent((float) 0.0);

		temp.setTarget(0);

		hbm.addTemperature(temp);

		// utilization
		Utilization util = new Utilization();
		util.setName("HDA");
		util.setValue((float) 0.1);
		hbm.addUtilization(util);

		Utilization util2 = new Utilization();
		util2.setName("EN0");
		util2.setValue((float) 0.1);
		hbm.addUtilization(util2);

		// op counter
		OperationCounter counter = new OperationCounter();
		hbm.setOperationCounter(counter);

		String msg = HeartbeatMessageToBeDeleted.toJson(hbm);

		System.out.println("msg=" + msg + ", size="
				+ msg.getBytes("utf8").length);

		byte[] data = msg.getBytes("UTF8");

		String msgX = new String(data, "UTF8");

		HeartbeatMessageToBeDeleted newMsg = HeartbeatMessageToBeDeleted.fromJson(msgX);

		String msg2 = HeartbeatMessageToBeDeleted.toJson(newMsg);

		System.out.println("msg=" + msg2 + ", size="
				+ msg2.getBytes("utf8").length);
	}

}
