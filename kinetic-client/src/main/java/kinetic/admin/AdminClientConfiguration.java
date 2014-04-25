package kinetic.admin;

import kinetic.client.ClientConfiguration;

/**
 * Kinetic admin Client configuration.
 * <p>
 * Kinetic admin applications construct a new instance of this instance and set
 * appropriate configurations. Application then calls
 * {@link KineticAdminClientFactory#createInstance(AdminClientConfiguration)} to
 * create a new instance of {@link KineticAdminClient}
 * 
 * @author James Hughes.
 * @author Chiaming Yang
 * 
 * @see KineticAdminClientFactory#createInstance(AdminClientConfiguration)
 * @see KineticAdminClient
 */
public class AdminClientConfiguration extends ClientConfiguration {

    private static final long serialVersionUID = 1194417884359507016L;

    /**
     * Admin Client configuration constructor.
     * 
     */
    public AdminClientConfiguration() {
        super.setUseSsl(true);
        setPort(getSSLDefaultPort());
    }

    /**
     * Set if using ssl, admin client must use ssl to connect with dirve.
     * 
     * @param flag
     *            Support set true, if set false, throw
     *            UnsupportedOperationException.
     */
    @Override
    public void setUseSsl(boolean flag) {
        if (!flag)
            throw new java.lang.UnsupportedOperationException(
                    "cannot disable SSL usage for admin client");
    }
}
