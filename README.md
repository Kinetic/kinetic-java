# Kinetic-Java implements Java API and Simulator for the Kinetic Open Storage Platform.

* Kinetic Protocol Definition: [https://github.com/Kinetic/kinetic-protocol] (https://github.com/Kinetic/kinetic-protocol)

## Overview 

Project components:

* `kinetic-client`      (Java client API and implementation of Kinetic Protocol)
* `kinetic-simulator`   (Kinetic device simulator)
* `kinetic-common`      (Common library for kinetic-client and kinetic-simulator)
* `kinetic-test`        (Test Suite for simulator and kinetic devices)

## Suggested development environment

* Latest version of Git: [http://git-scm.com/downloads](http://git-scm.com/downloads)

* JDK 1.7 or above: [http://www.oracle.com/technetwork/java/javase/downloads/index.html](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

* Maven 3.0.3 or later: [http://maven.apache.org/download.cgi](http://maven.apache.org/download.cgi)

## Quick start

### Download and build

```bash 
  cd ~/workspace
  git clone https://github.com/Kinetic/kinetic-java.git
  mvn clean package
```

**By default, simulator uses "USER-HOME/kinetic" as its data store folder. The "workspace" must be created in a separate folder as the data store folder.**

### Start simulator

**Start with simulator jar**:    

```bash
  cd ~/workspace
  java -jar kinetic-simulator/target/kinetic-simulator-"Version"-SNAPSHOT-jar-with-dependencies.jar    
```
where "Version" above is the build version number (such as 3.0.7).

**Start with script** (If configuring tcp_port, tls_port and Kinetic_home, type `script -help` for usage help):

```bash
  cd ~/workspace
  ./bin/startSimulator.sh
```

### Ping simulator

```bash
cd ~/workspace

# ping <device-ip>, 
# e.g.:
./bin/ping.sh 127.0.0.1 
```

### Test against remote instance

1. Run the integration tests against the simulator with specified max memory: `mvn test -DargLine="-Xmx500M"`
1. Run the integration tests against the remote instance at a particular host: `mvn test -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=1.2.3.4`

## Admin client command line usage

1. Download, build, and start simulator as described in Quick Start section above
1. Run admin CLI

```bash
   cd ~/wprkspace
   ./bin/kineticadmin.sh -help
```

### Erasing all data in the Simulator

* The simulator should be running, default port for TCP is 8123, SSL/TLS port is 8443
 
```bash
  ./bin/kineticadmin.sh -instanterase
```

## Simulator and Java API usage examples
=================================

Examples are located at the following directory.

"workspace"/kinetic-test/src/test/java/com/seagate/kinetic/example

## Kinetic Java client API Javadoc location
=================================
To browse Javadoc: [http://kinetic.github.io/kinetic-java/](http://kinetic.github.io/kinetic-java/)

## kinetic-java runtime on maven central
=================================
* kinetic-releases: [https://github.com/Kinetic/kinetic-java/releases](https://github.com/Kinetic/kinetic-java/releases)

## Run smoke test against simulator or kinetic drive
==================================

1.  Download, build, and start simulator as described in Quick Start section above 
1. `cd "workspace"/bin`
1. `sh runSmokeTests.sh [-host host_ip] [-port port] [-tlsport tlsport] [-home kinetic_home]`
