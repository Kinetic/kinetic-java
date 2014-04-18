package com.seagate.kinetic.simulator.protocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import kinetic.client.ClientConfiguration;
import kinetic.client.Entry;
import kinetic.client.KineticClient;
import kinetic.client.KineticClientFactory;
import kinetic.client.KineticException;
import kinetic.simulator.KineticSimulator;
import kinetic.simulator.SimulatorConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.seagate.kinetic.KineticTestHelpers;

public class V2ProtocolTest {

	private static byte[] key = KineticTestHelpers.toByteArray("key");
	private static byte[] value = KineticTestHelpers.toByteArray("value");
	private static Entry entry = new Entry(key, value);
	private static int port = Integer.parseInt(System.getProperty(
			"KINETIC_PORT", "8123"));
	private static int sslPort = Integer.parseInt(System.getProperty(
			"KINETIC_SSL_PORT", "8443"));

	private SimulatorConfiguration simulatorConfiguration;
	private ClientConfiguration clientConfiguration;
	private KineticSimulator simulator;
	private KineticClient client;

	@Before
	public void init() {
		simulatorConfiguration = new SimulatorConfiguration();
		simulatorConfiguration.setPort(port);
		simulatorConfiguration.setSslPort(sslPort);

		clientConfiguration = new ClientConfiguration();
		clientConfiguration.setPort(port);
	}

	@After
	public void close()
	{
		if (null != client) {
			try {
				client.close();
			} catch (KineticException e) {
				e.printStackTrace();
			}
		}

		if (null != simulator) {
			simulator.close();
		}
	}

	@Test
	public void v2TcpClient_V2NioSimulator_test() throws KineticException {
		simulator = startV2NioSimulator();
		client = startV2TcpClient();

		client.put(entry, null);

		Entry entryGet = client.get(key);

		if (null == entryGet) {
			fail("v2TcpClient vs v2NioSimulator: get entry failed");
		}

		assertArrayEquals(value, entryGet.getValue());
		assertTrue(client.delete(entry));
	}

	@Test
	public void v2NioClient_V2NioSimulator_test() throws KineticException {
		simulator = startV2NioSimulator();
		client = startV2NioClient();

		client.put(entry, null);

		Entry entryGet = client.get(key);

		if (null == entryGet) {
			fail("v2NioClient vs v2NioSimulator: get entry failed");
		}

		assertArrayEquals(value, entryGet.getValue());
		assertTrue(client.delete(entry));
	}

	@Test
	public void v2TcpClient_V2TcpSimulator_test() throws KineticException {
		simulator = startV2TcpSimulator();
		client = startV2TcpClient();

		client.put(entry, null);

		Entry entryGet = client.get(key);

		if (null == entryGet) {
			fail("v2TcpClient vs v2TcpSimulator: get entry failed");
		}

		assertArrayEquals(value, entryGet.getValue());
		assertTrue(client.delete(entry));
	}

	@Test
	public void v2NioClient_V2TcpSimulator_test() throws KineticException {
		simulator = startV2TcpSimulator();
		client = startV2NioClient();

		client.put(entry, null);

		Entry entryGet = client.get(key);

		if (null == entryGet) {
			fail("v2NioClient vs v2TcpSimulator: get entry failed");
		}

		assertArrayEquals(value, entryGet.getValue());
		assertTrue(client.delete(entry));
	}

	@Test
	public void v2SslClient_V2SslSimulator_test() throws KineticException {
		simulator = startV2SslSimulator();
		client = startV2SslClient();

		client.put(entry, null);

		Entry entryGet = client.get(key);

		if (null == entryGet) {
			fail("v2SslClient vs v2SslSimulator: get entry failed");
		}

		assertArrayEquals(value, entryGet.getValue());
		assertTrue(client.delete(entry));
	}

	//@Test
	@Ignore
	public void v1TcpClient_V2NioSimulator_test() throws KineticException{
		simulator = startV2NioSimulator();
		client = startV1TcpClient();
		try {
			client.put(entry, null);
		} catch (Exception e) {
			assertTrue(e instanceof KineticException);
			assertTrue(e.getMessage().contains("Timeout - unable to receive response message"));
		}
	}

	//@Test
	@Ignore
	public void v1NioClient_V2NioSimulator_test() throws KineticException{
		simulator = startV2NioSimulator();
		client = startV1NioClient();

		try {
			client.put(entry, null);
			fail("v1NioClient vs v2NioSimulator: protocol not match");
		} catch (Exception e) {
			assertTrue(e instanceof KineticException);
			assertTrue(e.getMessage().contains("Timeout - unable to receive response message"));
		}
	}

	//@Test
	@Ignore
	public void v1TcpClient_V2TcpSimulator_test() throws KineticException {
		simulator = startV2TcpSimulator();
		client = startV1TcpClient();

		try {
			client.put(entry, null);
			fail("v1TcpClient vs v2TcpSimulator: protocol not match");

		} catch (Exception e) {
			assertTrue(e instanceof KineticException);
			assertTrue(e.getMessage().contains("Timeout - unable to receive response message"));
		}

	}

	//@Test
	@Ignore
	public void v1NioClient_V2TcpSimulator_test() throws KineticException {
		simulator = startV2TcpSimulator();
		client = startV1NioClient();

		try {
			client.put(entry, null);
			fail("v1NioClient vs v2TcpSimulator: protocol not match");

		} catch (Exception e) {
			assertTrue(e instanceof KineticException);
			assertTrue(e.getMessage().contains("Timeout - unable to receive response message"));
		}
	}

	//@Test
	@Ignore
	public void v1SslClient_V2SslSimulator_test() throws KineticException {
		simulator = startV2SslSimulator();
		client = startV1SslClient();

		try {
			client.put(entry, null);
			fail("v1SslClient vs v2SslSimulator: protocol not match");
		} catch (Exception e) {
			assertTrue(e instanceof KineticException);
			assertTrue(e.getMessage().contains("Timeout - unable to receive response message"));
		}
	}

	//@Test
	@Ignore
	public void v2TcpClient_V1NioSimulator_test() throws KineticException {
		simulator = startV1NioSimulator();
		client = startV2TcpClient();

		try {
			client.put(entry, null);
			fail("v2TcpClient vs v1NioSimulator: protocol not match");
		} catch (Exception e) {
			assertTrue(e instanceof KineticException);
			assertTrue(e.getMessage().contains("Timeout - unable to receive response message"));
		}
	}

	//@Test
	@Ignore
	public void v2NioClient_V1NioSimulator_test() throws KineticException {
		simulator = startV1NioSimulator();
		client = startV2NioClient();

		try {
			client.put(entry, null);
			fail("v2NioClient vs v1NioSimulator: protocol not match");
		} catch (Exception e) {
			assertTrue(e instanceof KineticException);
			assertTrue(e.getMessage().contains("Timeout - unable to receive response message"));
		}
	}

	//@Test
	public void v2TcpClient_V1TcpSimulator_test() throws KineticException {
		simulator = startV1TcpSimulator();
		client = startV2TcpClient();

		try {
			client.put(entry, null);
			fail("v2TcpClient vs v1TcpSimulator: protocol not match");
		} catch (Exception e) {
			assertTrue(e instanceof KineticException);
			assertTrue(e.getMessage().contains("Timeout - unable to receive response message"));
		}
	}

	//@Test
	@Ignore
	public void v2NioClient_V1TcpSimulator_test() throws KineticException {
		simulator = startV1TcpSimulator();
		client = startV2NioClient();

		try {
			client.put(entry, null);
			fail("v2NioClient vs v1TcpSimulator: protocol not match");

		} catch (Exception e) {
			assertTrue(e instanceof KineticException);
			assertTrue(e.getMessage().contains("Timeout - unable to receive response message"));        }
	}

	//@Test
	@Ignore
	public void v2SslClient_V1SslSimulator_test() throws KineticException {
		simulator = startV1SslSimulator();
		client = startV2SslClient();

		try {
			client.put(entry, null);
			fail("v2SslClient vs v1SslSimulator: protocol not match");

		} catch (Exception e) {
			assertTrue(e instanceof KineticException);
			assertTrue(e.getMessage().contains("Timeout - unable to receive response message"));        }
	}

	@Test
	@Ignore
	public void v1TcpClient_V1NioSimulator_test() throws KineticException {
		simulator = startV1NioSimulator();
		client = startV1TcpClient();

		client.put(entry, null);

		Entry entryGet = client.get(key);

		if (null == entryGet) {
			fail("v1TcpClient vs v1NioSimulator: get entry failed");
		}

		assertArrayEquals(value, entryGet.getValue());

		assertTrue(client.delete(entry));
	}

	@Test
	@Ignore
	public void v1NioClient_V1NioSimulator_test() throws KineticException {
		simulator = startV1NioSimulator();
		client = startV1NioClient();

		client.put(entry, null);

		Entry entryGet = client.get(key);

		if (null == entryGet) {
			fail("v1NioClient vs v1NioSimulator: get entry failed");
		}

		assertArrayEquals(value, entryGet.getValue());

		assertTrue(client.delete(entry));
	}

	@Test
	@Ignore
	public void v1TcpClient_V1TcpSimulator_test() throws KineticException {
		simulator = startV1TcpSimulator();
		client = startV1TcpClient();

		client.put(entry, null);

		Entry entryGet = client.get(key);

		if (null == entryGet) {
			fail("v1TcpClient vs v1TcpSimulator: get entry failed");
		}

		assertArrayEquals(value, entryGet.getValue());

		assertTrue(client.delete(entry));
	}

	@Test
	@Ignore
	public void v1NioClient_V1TcpSimulator_test() throws KineticException {
		simulator = startV1TcpSimulator();
		client = startV1NioClient();

		client.put(entry, null);

		Entry entryGet = client.get(key);

		if (null == entryGet) {
			fail("v1NioClient vs v1TcpSimulator: get entry failed");
		}

		assertArrayEquals(value, entryGet.getValue());

		assertTrue(client.delete(entry));
	}

	@Test
	@Ignore
	public void v1SslClient_V1SslSimulator_test() throws KineticException {
		simulator = startV1SslSimulator();
		client = startV1SslClient();

		client.put(entry, null);

		Entry entryGet = client.get(key);

		if (null == entryGet) {
			fail("v2SslClient vs v2SslSimulator: get entry failed");
		}

		assertArrayEquals(value, entryGet.getValue());

		assertTrue(client.delete(entry));
	}

	private KineticClient startV2TcpClient() throws KineticException {
		clientConfiguration.setUseV2Protocol(true);
		clientConfiguration.setUseNio(false);

		return KineticClientFactory.createInstance(clientConfiguration);
	}

	private KineticClient startV2NioClient() throws KineticException {
		clientConfiguration.setUseV2Protocol(true);
		clientConfiguration.setUseNio(true);

		return KineticClientFactory.createInstance(clientConfiguration);
	}

	private KineticClient startV2SslClient() throws KineticException {
		clientConfiguration.setUseV2Protocol(true);
		clientConfiguration.setUseSsl(true);
		clientConfiguration.setPort(sslPort);

		return KineticClientFactory.createInstance(clientConfiguration);
	}

	private KineticClient startV1TcpClient() throws KineticException {
		clientConfiguration.setUseNio(false);
		clientConfiguration.setUseV2Protocol(false);

		return KineticClientFactory.createInstance(clientConfiguration);
	}

	private KineticClient startV1NioClient() throws KineticException {
		clientConfiguration.setUseV2Protocol(false);

		return KineticClientFactory.createInstance(clientConfiguration);
	}

	private KineticClient startV1SslClient() throws KineticException {
		clientConfiguration.setUseV2Protocol(false);
		clientConfiguration.setUseSsl(true);
		clientConfiguration.setPort(sslPort);

		return KineticClientFactory.createInstance(clientConfiguration);
	}

	private KineticSimulator startV2NioSimulator() {
		simulatorConfiguration.setUseV2Protocol(true);
		simulatorConfiguration.setStartSsl(false);

		return new KineticSimulator(simulatorConfiguration);
	}

	private KineticSimulator startV2TcpSimulator() {
		simulatorConfiguration.setUseV2Protocol(true);
		simulatorConfiguration.setUseNio(false);
		simulatorConfiguration.setStartSsl(false);

		return new KineticSimulator(simulatorConfiguration);
	}

	private KineticSimulator startV2SslSimulator() {
		simulatorConfiguration.setUseV2Protocol(true);
		simulatorConfiguration.setUseNio(true);
		simulatorConfiguration.setStartSsl(true);

		return new KineticSimulator(simulatorConfiguration);
	}

	private KineticSimulator startV1NioSimulator() {
		simulatorConfiguration.setStartSsl(false);
		simulatorConfiguration.setUseV2Protocol(false);

		return new KineticSimulator(simulatorConfiguration);
	}

	private KineticSimulator startV1TcpSimulator() {
		simulatorConfiguration.setUseNio(false);
		simulatorConfiguration.setStartSsl(false);
		simulatorConfiguration.setUseV2Protocol(false);

		return new KineticSimulator(simulatorConfiguration);
	}

	private KineticSimulator startV1SslSimulator() {
		simulatorConfiguration.setUseNio(true);
		simulatorConfiguration.setStartSsl(true);
		simulatorConfiguration.setUseV2Protocol(false);

		return new KineticSimulator(simulatorConfiguration);
	}
}
