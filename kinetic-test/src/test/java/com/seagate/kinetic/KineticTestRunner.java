package com.seagate.kinetic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import kinetic.client.ClientConfiguration;

import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.google.common.base.Throwables;

/**
 * 
 * Run simulator/drive at locally or remotely.
 * <p>
 * 
 */
public class KineticTestRunner extends Suite {

	private final Class<?> klass;

	public KineticTestRunner(Class<?> klass) throws Throwable {
		super(klass, Collections.<Runner> emptyList());
		this.klass = klass;
	}

	/**
	 * 
	 * Get system properties.
	 * <p>
	 * 
	 */
	@Override
	protected List<Runner> getChildren() {
		try {
			InnerRunner nonssl_nonnio = new InnerRunner(klass,
					new TestClientConfigConfigurator("Non-SSL/Non-NIO", false,
							false));

			InnerRunner nonssl_nio = new InnerRunner(
					klass,
					new TestClientConfigConfigurator("Non-SSL/NIO", false, true));

			InnerRunner ssl_nio = new InnerRunner(klass,
					new TestClientConfigConfigurator("SSL/NIO", true, true));

			ArrayList<Runner> runners = new ArrayList<Runner>();

			if (Boolean.parseBoolean(System.getProperty("RUN_TCP_TEST"))) {
				runners.add(nonssl_nonnio);
			}

            if (Boolean.parseBoolean(System.getProperty("RUN_NIO_TEST"))) {
				runners.add(nonssl_nio);
			}

            if (Boolean.parseBoolean(System.getProperty("RUN_SSL_TEST"))) {
				runners.add(ssl_nio);
			}

            if (runners.isEmpty()) {
                return Collections.singletonList((Runner)new BlockJUnit4ClassRunner(klass));
			}

			return runners;

		} catch (InitializationError initializationError) {
			throw Throwables.propagate(initializationError);
		}
    }

	/**
	 * 
	 * Test client configuration to define using ssl or nio.
	 * <p>
	 * 
	 */
	public class TestClientConfigConfigurator {
		private final String name;
		private final boolean ssl;
		private final boolean nio;

		public TestClientConfigConfigurator(String name, boolean ssl,
				boolean nio) {
			this.name = name;
			this.ssl = ssl;
			this.nio = nio;
		}

		void modifyClientConfig(ClientConfiguration defaultConfig) {
			if (ssl) {
				defaultConfig.setPort(defaultConfig.getSSLDefaultPort());
			}
			defaultConfig.setUseSsl(ssl);
			defaultConfig.setUseNio(nio);
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * 
	 * Inner runner class for test.
	 * <p>
	 * 
	 */
	private class InnerRunner extends BlockJUnit4ClassRunner {
		private final TestClientConfigConfigurator testClientFactory;

		public InnerRunner(Class<?> klass,
				TestClientConfigConfigurator testClientFactory)
				throws InitializationError {
			super(klass);
			this.testClientFactory = testClientFactory;
		}

		@Override
		protected String getName() {
			return String.format("[%s]", testClientFactory.getName());
		}

		@Override
		protected String testName(FrameworkMethod method) {
			return method.getName() + getName();
		}

		@Override
		protected Object createTest() throws Exception {
			try {
				Object instance = klass.newInstance();
				Field testClientFactoryField = klass
						.getField("testClientConfigurator");
				testClientFactoryField.set(instance, testClientFactory);
				return instance;
			} catch (NoSuchFieldException e) {
				throw new IllegalStateException(
						"Test cases run with KineticTestRunner need a field of type TestClientConfigurator called testClientConfigurator",
						e);
			}
		}

        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            // Copying the parent's list of methods is needed rather than modifying in place
            // because the parent's list is used in multiple places.
            List<FrameworkMethod> frameworkMethods = new ArrayList<FrameworkMethod>(super.computeTestMethods());

            // Right now the only custom annotation filtering we do is whether the tests run against the simulator
            // so if running against simulator we know all test methods can run
            if (IntegrationTestTargetFactory.isRunningAgainstSimulator()) {
                return frameworkMethods;
            }

            Iterator<FrameworkMethod> it = frameworkMethods.iterator();

            while (it.hasNext()) {
                FrameworkMethod frameworkMethod = it.next();

                boolean isSimulatorOnly = frameworkMethod.getAnnotation(SimulatorOnly.class) != null;

                if (isSimulatorOnly) {
                    it.remove();
                }
            }

            return frameworkMethods;
        }
    }
}
