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
package com.seagate.kinetic.usage.p2p;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Logger;

import kinetic.client.AsyncKineticException;
import kinetic.client.CallbackHandler;
import kinetic.client.CallbackResult;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

public class KineticP2PUsageExample {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	KineticSimulator newSimulator(int port) throws KineticException {
		SimulatorConfiguration sConfig = new SimulatorConfiguration();
		sConfig.setPort(port);
		sConfig.setStartSsl(false);
		sConfig.put(SimulatorConfiguration.PERSIST_HOME, "instance_" + port);
		return new KineticSimulator(sConfig);
	}

	KineticClient newClient(KineticSimulator s) throws KineticException {
		SimulatorConfiguration sc = s.getServerConfiguration();
		ClientConfiguration cConfig = new ClientConfiguration();
		cConfig.setHost("localhost");
		cConfig.setPort(sc.getPort());
		return KineticClientFactory.createInstance(cConfig);
	}

	@SuppressWarnings("rawtypes")
	List<Event> running = Collections.synchronizedList(new LinkedList<Event>());

	// List<Event> events = new LinkedList<Event>();

	synchronized void allComplete() throws AsyncKineticException {
		try {
			logger.fine("waitingAll");
			while (!running.isEmpty()) {
				logger.fine("allComplete waiting");
				synchronized (running) {
					running.wait(5000);
				}
			}
			logger.fine("waitedAll");
		} catch (InterruptedException e) {
			throw new AsyncKineticException("Timed out [2]");
		}
	}

	// this can be either a named event that can be waited on or an
	// anonymous event that will automatically be a part of the "running"
	// event list and can be waited on with a call to "completeAll()".
	public class Event<T> implements CallbackHandler<T> {

		T result = null;
		private AsyncKineticException exception = null;
		private boolean success = false;
		private boolean complete = false;

		void done() {
			complete = true;
			this.notifyAll();

			synchronized (running) {
				int i = running.indexOf(this);
				if (i >= 0) {
					running.remove(i);
				}
				if (running.isEmpty()) {
					running.notifyAll();
				}
			}
		}

		@Override
		synchronized public void onSuccess(CallbackResult<T> result) {
			logger.fine("onSuccess");
			if (!complete) {
				this.result = result.getResult();
				success = true;
			}
			done();
		}

		@Override
		synchronized public void onError(AsyncKineticException exception) {
			logger.warning("onError");
			if (!complete) {
				this.exception = exception;
				logger.warning(exception.toString());
				success = false;
			}
			done();
		}

		void completed() throws AsyncKineticException {
			try {
				logger.fine("waitingOne");
				synchronized (this) {
					while (!complete) {
						this.wait(1000);
					}
				}
			} catch (InterruptedException e) {
				throw new AsyncKineticException("Timed out");
			}
			logger.fine("waitedOne");
			if (!success)
				throw this.exception;
		}

		Event() {
			running.add(this);
		}
	}

	Entry myEntry(String k) {
		return myEntry(k, null);
	}

	Entry myEntry(String k, String v) {
		Entry e = new Entry();
		e.setKey(k.getBytes());
		if (v != null)
			e.setValue(v.getBytes());
		return e;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	List shuffle(List l) {
		LinkedList l1 = new LinkedList();
		Random r = new Random();
		while (!l.isEmpty()) {
			int i = r.nextInt(l.size());
			l1.add(l.get(i));
			l.remove(i);
		}
		return l1;
	}

	static enum Command {
		PUSH
	};

	public class RemoteOp {
		Command command;
		boolean started = false;
		boolean success = false;
		KineticException exception;
		byte[] key = null;
		byte[] newKey = null;
		byte[] version = null;
		boolean force = false;

		RemoteOp(Command c, byte[] key) {
			this.command = c;
			this.key = key;
		}
	}

	// this is the interface to the library and a thing that is supposed to be
	// similar
	// to what is actually done in the drive. Imagine that this was really
	// local.P2POperation(....) and that the local parameter is not there.
	List<RemoteOp> P2POperation(KineticClient local, List<RemoteOp> l,
			String peer, int port, boolean ssl) throws KineticException {

		ClientConfiguration cConfig = new ClientConfiguration();
		cConfig.setHost(peer);
		cConfig.setPort(port);
		cConfig.setUseSsl(ssl);
		KineticClient remote = KineticClientFactory.createInstance(cConfig);

		LinkedList<RemoteOp> lout = new LinkedList<RemoteOp>();

		for (RemoteOp op : l) {
			try {
				op.started = true;
				switch (op.command) {
				case PUSH:

					// we get the data
					Entry e = local.get(op.key);

					if (op.newKey != null)
						e.setKey(op.newKey);

					// TODO: Chiaming: How does "force" put implemented in the
					// api? Is it? Maybe not?
					if (op.force)
						throw new KineticException(
								"P2POperation.force not implemented");

					// and if no options, just put it...
					if (op.version != null) {
						remote.putForced(e);
					} else /* (op.version != null) */{
						// in this case, we need to put the old version in the
						// new
						// data to the put with version works and then
						// mark the new version as what we read.
						byte[] newVersion = e.getEntryMetadata().getVersion();
						byte[] oldVersion = op.version;
						e.getEntryMetadata().setVersion(oldVersion);
						remote.put(e, newVersion);
					}
					break;
				default:
					throw new KineticException("unknown P2P command");
				}
				// if we get here without an exception, we were successful.
				op.success = true;
			} catch (KineticException e) {
				op.exception = e;
				op.success = false;
			}
			lout.add(op);
		}
		return lout;
	}

	@SuppressWarnings("unchecked")
	KineticP2PUsageExample() throws KineticException {

		logger.warning("new");

		KineticSimulator s1 = newSimulator(8123);
		KineticSimulator s2 = newSimulator(8224);

		KineticClient c1 = newClient(s1);
		KineticClient c2 = newClient(s2);

		logger.warning("started");

		try {

			List<Entry> keys = new LinkedList<Entry>();
			for (int i = 0; i < 256; i++) {
				byte[] bs = new byte[1];
				bs[0] = (byte) i;
				keys.add(new Entry(bs, bs));
			}

			keys = shuffle(keys);

			for (Entry e : keys) {
				c1.deleteForcedAsync(e.getKey(), new Event<Boolean>());
			}
			// allComplete();
			logger.warning("deleted");

			for (Entry e : keys) {
				c1.putForcedAsync(e, new Event<Entry>());
			}
			// allComplete();
			logger.warning("written");

			List<byte[]> l = c1.getKeyRange(new byte[] {}, true, new byte[] {
					-1, -1 }, false, 500);

			List<RemoteOp> ops = new LinkedList<RemoteOp>();
			for (Entry e : keys) {
				ops.add(new RemoteOp(Command.PUSH, e.getKey()));
			}
			logger.warning("total operations out: " + ops.size());
			ops = P2POperation(c1, ops, "localhost", 8224, false);
			logger.warning("total operations in:  " + ops.size());
			for (RemoteOp op : ops) {
				if (op.success)
					System.out.printf("Success, key %02x\n", op.key[0]);
				else
					System.out.printf("Failed, key %02x\n%s\n", op.key[0],
							op.exception.toString());
			}

			logger.warning("total running: " + running.size());

			for (byte[] ba : l) {
				System.out.printf("%04d %02x \n", ba[0], ba[0]);
			}

			logger.warning("complete");

		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}

		logger.warning("done");

		c1.close();
		c2.close();
		s1.close();
		s2.close();

		logger.warning("Finished!");
		Handler[] handlers = logger.getHandlers();
		for (Handler handler : handlers) {
			handler.close();
		}
	}

	public static byte[] toByteArray(String s)
			throws UnsupportedEncodingException {
		return s.getBytes("utf8");
	}

	public static void main(String[] args) throws Exception {

		@SuppressWarnings("unused")
		KineticP2PUsageExample xx = new KineticP2PUsageExample();
	}
}
