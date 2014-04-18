package kinetic.admin;

import java.util.List;

public class Configuration {

	// name of the vendor. Should be "Seagate"
	private String vendor;

	// name of the device. Have no clue what this should be...
	private String model;

	// Device Serial number (WWN) from the underlying drive
	private String serialNumber;

	// This is the version of the software on the drive in dot notation
	// if this is not set or ends with "x" this is test code.
	private String version;

	// This is the date/time string of when the source was compiled
	private String compilationDate;

	// This is the git hash of the source tree so that the exact code can be determined.
	private String sourceHash;

	// the interfaces for this device. one per interface.
	private List<Interface> interfaces;

	// these are the port numbers for the software
	private int port;
	
	private int tlsPort;

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCompilationDate() {
		return compilationDate;
	}

	public void setCompilationDate(String compilationDate) {
		this.compilationDate = compilationDate;
	}

	public String getSourceHash() {
		return sourceHash;
	}

	public void setSourceHash(String sourceHash) {
		this.sourceHash = sourceHash;
	}

	public List<Interface> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<Interface> interfaces) {
		this.interfaces = interfaces;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTlsPort() {
		return tlsPort;
	}

	public void setTlsPort(int tlsPort) {
		this.tlsPort = tlsPort;
	}

}
