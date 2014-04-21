Suggested Development Environments
==================================
* Latest version of Git for your OS: [http://git-scm.com/downloads](http://git-scm.com/downloads)

* JDK 1.7: [http://www.oracle.com/technetwork/java/javase/downloads/index.html](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  (Please note that JDK 1.8 is not officially supported yet due to Javadoc incompatibility issues.)

* Eclipse IDE For Java Developers. Version 4.2 (Juno) or later: [http://www.eclipse.org/downloads/](http://www.eclipse.org/downloads/)

* Maven 3.0.3 or later: [http://maven.apache.org/download.cgi](http://maven.apache.org/download.cgi)

* For Windows 7 system, install the system patch package for jni:
  32 bit: [http://www.microsoft.com/en-us/download/details.aspx?id=5555](http://www.microsoft.com/en-us/download/details.aspx?id=5555)
  64 bit: [http://www.microsoft.com/en-us/download/details.aspx?id=14632](http://www.microsoft.com/en-us/download/details.aspx?id=14632 )

Project components
===================================
The kinetic-java project contains:

- kinetic-client      (kinetic java client API and implementation)

- kinetic-simulator   (kinetic simulator API and implementation)

- kinetic-common      (common library for kinetic-client and kinetic-simulator)

- kinetic-test        (test suite for the kinetic-java and kinetic drive implementation) 
 

Getting Started With Kinetic drives
===================================
* The 4-bay developer kit: 
[https://developers.seagate.com/display/KV/Kinetic+Developer+Kit+Connection+Guide](https://developers.seagate.com/display/KV/Kinetic+Developer+Kit+Connection+Guide)
[https://developers.seagate.com/display/KV/Development+Chassis](https://developers.seagate.com/display/KV/Development+Chassis)

Getting Started With Development
================================
1. Clone the code: `git clone https://github.com/Seagate/kinetic-java`
1. Run the integration tests against the Java Simulator: `mvn test -DargLine="-Xmx500M"`
1. Run the integration tests but exclude specific files: `mvn test -Dmaven.test.excludes="**/File.java,**/OtherFile.java"
1. Run the integration tests against the remote instance at a particular path: `mvn test -DRUN_AGAINST_EXTERNAL=true -DKINETIC_PATH=/path/to/kinetic_home`
1. Run the integration tests against the remote instance at a particular host: `mvn test -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=1.2.3.4`
1. Run the integration tests against the remote instance at a particular host using SSH to reset state before runs: `mvn test -DRUN_AGAINST_EXTERNAL=true -DKINETIC_HOST=1.2.3.4 -DFAST_CLEAN_UP=true`

Getting Started With Simulator
================================
1. Run "mvn clean package" in "Kinetic-Folder"
2. Start with java CLI: 
       java -jar in "Kinetic-Folder"/kinetic-simulator/target/kinetic-simulator-0.6.0.1-SNAPSHOT-jar-with-dependencies.jar
   
   or Start with script (If configuration tcp_port, tls_port and Kinetic_home, use script -help):
       Windows: 
            cd "Kinetic-Folder"\bin
            startSimulator.bat
            
       Linux & Mac:
            cd "Kinetic-Folder"/bin
            sh startSimulator.sh

Admin command line Usage
==============================
1, make sure "Kinetic-Folder"/kinetic-simulator/target/kinetic-simulator-0.6.0.1-SNAPSHOT-jar-with-dependencies.jar 
             "Kinetic-Folder"/kinetic-client/target/kinetic-client-0.6.0.1-SNAPSHOT-jar-with-dependencies.jar
   exist

2, start simulator
   Windows:  cd "Kinetic-Folder"/bin
             kineticsimulator.bat
             
   Linux:    cd "Kinetic-Folder"/bin
             ./kineticsimulator.sh
   
3, run admin cli
   Windows:  cd "Kinetic-Folder"/bin
             kineticadmin -help
   
   Linux:    cd "Kinetic-Folder"/bin
             ./kineticadmin.sh -help

Erasing all data in the Simulator
=================================

* The simulator should be running, default port for TCP is 8123, SSL/TLS port is 8443
* You'll need to have recently built .jar (i.e. run `mvn package`)

1. ./bin/kineticadmin.sh -setup -erase true

Simulator and Java API usage examples
=================================

Examples are located at the following directory.

"Kinetic-Folder"/kinetic-test/src/test/java/com/seagate/kinetic/example


Kinetic Javadoc location
=================================
1. Run "mvn clean package" in "Kinetic-Folder".

1. Run "mvn clean package" in "Kinetic-Folder"/kinetic-client, "Kinetic-Folder"/kinetic-simulator and "Kinetic-Folder"/kinetic-test.

2. Javadoc will be generated for modules in directory of "Kinetic-Folder"/kinetic-client/doc, "Kinetic-Folder"/kinetic-simulator/doc and "Kinetic-Folder"/kinetic-test/doc.


Run smoke test against simulator or kinetic drive
==================================
Make sure one instance of simulator or kinetic drive is running.

1.  Run "mvn clean package" in "Kinetic-Folder" or "Kinetic-Folder"/kinetic-test, verify that
   "Kinetic-Folder"/kinetic-test/target/kinetic-test-0.6.0.1-SNAPSHOT-jar-with-dependencies.jar 
   "Kinetic-Folder"/kinetic-test/target/smoke-tests.jar
   exist.

2. cd "Kinetic-Folder"/bin

3. sh runSmokeTests.sh [-host host_ip] [-port port] [-home kinetic_home]
   or
   python runSmokeTests.py [-host host_ip] [-port port] [-home kinetic_home]
            
            
