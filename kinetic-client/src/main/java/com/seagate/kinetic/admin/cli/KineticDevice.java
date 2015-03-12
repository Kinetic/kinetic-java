package com.seagate.kinetic.admin.cli;

import java.util.ArrayList;
import java.util.List;

public class KineticDevice {
    private List<String> inet4;
    private int port;
    private int tlsPort;
    private String wwn;
    private String model;
    private String serialNumber;

    public KineticDevice() {
        this.inet4 = new ArrayList<String>();
        this.port = 8123;
        this.tlsPort = 8443;
        this.wwn = "";
        this.model = "";
        this.serialNumber = "";
    }

    public KineticDevice(List<String> inet4, int port, int tlsPort, String wwn,
            String model, String serialNumber) {
        this.inet4 = inet4;
        this.port = port;
        this.tlsPort = tlsPort;
        this.wwn = wwn;
        this.model = model;
        this.serialNumber = serialNumber;
    }

    public List<String> getInet4() {
        return inet4;
    }

    public void setInet4(List<String> inet4) {
        this.inet4 = inet4;
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

    public String getWwn() {
        return wwn;
    }

    public void setWwn(String wwn) {
        this.wwn = wwn;
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

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(inet4.get(0));
        sb.append(":");
        sb.append(inet4.get(1));
        sb.append(":" + wwn + ":" + serialNumber + ":" + port + ":" + tlsPort);

        return sb.toString();
    }
}
