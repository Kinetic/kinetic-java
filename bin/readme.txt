Kinetic Simulator/Admin Command Line Interface (CLI).

0. Assume you have at least Java SE 1.6 or 1.7 or 1.8 installed on your machine.
 
1. Define JAVA_HOME environment variable (example: google 'how to set JAVA_HOME on Mac).

2. Run "mvn clean package" in <Kinetic-Folder>, verify 
   <Kinetic-Folder>/kinetic-simulator/target/kinetic-simulator-0.6.0.2-SNAPSHOT-jar-with-dependencies.jar 
   <Kinetic-Folder>/kinetic-client/target/kinetic-client-0.6.0.2-SNAPSHOT-jar-with-dependencies.jar
   exist.

3. To start Kinetic simulator:

   Windows: 
            cd <Kinetic-Folder>\bin
            startSimulator.bat
            
   Linux & Mac:
            cd <Kinetic-Folder>/bin
            sh startSimulator.sh
            
4. To run Kinetic admin CLI:

   Windows: 
            cd <Kinetic-Folder>\bin
            kineticAdmin.bat -help
            
   Linux & Mac:
            cd <Kinetic-Folder>/bin
            sh kineticAdmin.sh -help

5. Security file template: 
            security.template

6. To start Kinetic simulator (configuration port, tls_port and kinetic_home)

   Windows: 
            cd <Kinetic-Folder>\bin
            startSimulator.bat -help
            
   Linux & Mac:
            cd <Kinetic-Folder>/bin
            sh startSimulator.sh -help
            
Run smoke test against simulator or kinetic drive
==================================
Make sure one instance of simulator or kinetic drive is running.

1. Run "mvn clean package" in <Kinetic-Folder> or <Kinetic-Folder>/kinetic-test, verify 
   <Kinetic-Folder>/kinetic-test/target/kinetic-test-0.6.0.2-SNAPSHOT-jar-with-dependencies.jar 
   <Kinetic-Folder>/kinetic-test/target/smoke-tests.jar
   exist.

2. cd <Kinetic-Folder>/bin

3. sh runSmokeTests.sh [-host host_ip] [-port port] [-tlsport tlsport] [-home kinetic_home]
   or
   python runSmokeTests.py [-host host_ip] [-port port] [-tlsport tlsport] [-home kinetic_home]
   
Usage of Kinetic Admin API script
==================================
1. kineticAdmin.sh, you can use "-help" to see usage.
   
   Usage: kineticAdmin <-setup|-security|-getlog|-firmware>
          kineticAdmin -h|-help
          kineticAdmin -setup [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-pin <pin>] [-newclversion <newclusterversion>] [-setpin <setpin>] [-erase <true|false>]
          kineticAdmin -security <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
          kineticAdmin -getlog [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-type <utilization|temperature|capacity|configuration|message|statistic|all>]
          kineticAdmin -firmware <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-pin <pin>]

2. setup:
   sh kineticAdmin.sh -setup [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-pin <pin>] [-newclversion <newclusterversion>] [-setpin <setpin>] [-erase <true|false>]
   
   Parameters are optional, the default values are set as below: 
   host=127.0.0.1    simulator/drive's ipaddress
   tlsport=8443      admin client connect to simulator/drive via this port
   clversion=0       admin client connect to simulator/drive within this cluster version
   
   For example,
   2.1 set new cluster version.
       Simulator/drive locally:
       First time: sh kineticAdmin.sh -setup -newclversion 1
       Second time: sh kineticAdmin.sh -setup -clversion 1 -newclversion 2
       Third time: after set pin (sh kineticAdmin.sh -setup -setpin 123):
                    sh kineticAdmin.sh -setup -pin 123 -clversion 2 -newclversion 3
       
       
       Simulator/drive remotely(IP:10.24.70.123):
       First time: sh kineticAdmin.sh -setup -host 10.24.70.123 -newclversion 1
       Second time: sh kineticAdmin.sh -setup -host 10.24.70.123 -clversion 1 -newclversion 2
       Third time: after set pin (sh kineticAdmin.sh -setup -setpin 123):
                    sh kineticAdmin.sh -setup -pin 123 -clversion 2 -newclversion 3
       
   2.2 set pin.
       Simulator/drive locally:
       First time: sh kineticAdmin.sh -setup -setpin 123
       Second time: sh kineticAdmin.sh -setup -pin 123 -setpin 456
       Third time: after set new cluster version (sh kineticAdmin.sh -setup -newclversion 1)
                    sh kineticAdmin.sh -setup -clversion 1 -pin 456 -setpin 789
       
       
       Simulator/drive remotely(IP:10.24.70.123):
       First time: sh kineticAdmin.sh -setup -host 10.24.70.123 -setpin 123
       Second time: sh kineticAdmin.sh -setup -host 10.24.70.123 -pin 123 -setpin 456
       Third time: after set new cluster version (sh kineticAdmin.sh -setup -newclversion 1)
                    sh kineticAdmin.sh -setup -host 10.24.70.123 -clversion 1 -pin 456 -setpin 789
       
   2.3 erase data
       Simulator/drive locally:
       First time: sh kineticAdmin.sh -setup -erase true
       Second time, 
         after set new cluster version (sh kineticAdmin.sh -setup -newclversion 1) for the simulator/drive
                   sh kineticAdmin.sh -setup -clversion 1 -erase true
         after set pin (sh kineticAdmin.sh -setup -setpin 123) for the simulator/drive, 
                   sh kineticAdmin.sh -setup -pin 123 -erase true
         after set new cluster version and pin (sh kineticAdmin.sh -setup -newclversion 1 -setpin 123) for the simulator/drive
                   sh kineticAdmin.sh -setup -clversion 1 -pin 123 -erase true


       Simulator/drive remotely(IP:10.24.70.123):
       First time: sh kineticAdmin.sh -setup -host 10.24.70.123 -erase true
       Second time, 
         after set new cluster version (sh kineticAdmin.sh -setup -host 10.24.70.123 -newclversion 1) for the simulator/drive
                   sh kineticAdmin.sh -setup -host 10.24.70.123 -clversion 1 -erase true
         after set pin (sh kineticAdmin.sh -setup -host 10.24.70.123 -setpin 123) for the simulator/drive, 
                   sh kineticAdmin.sh -setup -host 10.24.70.123 -pin 123 -erase true
         after set new cluster version and pin (sh kineticAdmin.sh -setup -host 10.24.70.123 -newclversion 1 -setpin 123) for the simulator/drive
                   sh kineticAdmin.sh -setup -host 10.24.70.123 -clversion 1 -pin 123 -erase true
                   
3. set security                   
   sh kineticAdmin.sh -security <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]               
   
   Parameters are optional, the default values are set as below: 
   host=127.0.0.1
   tlsport=8443
   clversion=0               
                   
   For example,
   Simulator/drive locally:
   Before setup new cluster version：sh kineticAdmin.sh -security security.template
   After setup new cluster version (sh kineticAdmin.sh -setup -newclversion 1) for the simulator/drive
         sh kineticAdmin.sh -clversion 1 -security security.template
      
         
   Simulator/drive remotely(IP:10.24.70.123):
   Before setup new cluster version：sh kineticAdmin.sh -security security.template -host 10.24.70.123
   After setup new cluster version (sh kineticAdmin.sh -setup -host 10.24.70.123 -newclversion 1) for the simulator/drive
         sh kineticAdmin.sh -security security.template -host 10.24.70.123 -clversion 1
         
4. get log
   kineticAdmin -getlog [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-type <utilization|temperature|capacity|configuration|message|statistic|all>]
   
   Parameters are optional, the default values are set as below: 
   host=127.0.0.1
   tlsport=8443
   clversion=0
   
   For example,
   Simulator/drive locally:
   Before setup new cluster version：
   Get all type log information: sh kineticAdmin.sh -getlog
                                 or
                                 sh kineticAdmin.sh -getlog -type all
   Get utilization   log info: sh kineticAdmin.sh -getlog -type utilization
   Get temperature   log info: sh kineticAdmin.sh -getlog -type temperature
   Get capacity      log info: sh kineticAdmin.sh -getlog -type capacity
   Get configuration log info: sh kineticAdmin.sh -getlog -type configuration
   Get message       log info: sh kineticAdmin.sh -getlog -type message
   Get statistic     log info: sh kineticAdmin.sh -getlog -type statistic 
   
   After setup new cluster version (sh kineticAdmin.sh -setup -newclversion 1) for the simulator/drive
   Get all type log information: sh kineticAdmin.sh -getlog -clversion 1
                                 or
                                 sh kineticAdmin.sh -getlog -clversion 1 -type all
   Get utilization   log info: sh kineticAdmin.sh -getlog -clversion 1 -type utilization
   Get temperature   log info: sh kineticAdmin.sh -getlog -clversion 1 -type temperature
   Get capacity      log info: sh kineticAdmin.sh -getlog -clversion 1 -type capacity
   Get configuration log info: sh kineticAdmin.sh -getlog -clversion 1 -type configuration
   Get message       log info: sh kineticAdmin.sh -getlog -clversion 1 -type message
   Get statistic     log info: sh kineticAdmin.sh -getlog -clversion 1 -type statistic
   
   
   Simulator/drive remotely(IP:10.24.70.123):
   Before setup new cluster version：
   Get all type log information: sh kineticAdmin.sh -getlog -host 10.24.70.123
                                 or
                                 sh kineticAdmin.sh -getlog -host 10.24.70.123 -type all
   Get utilization   log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -type utilization
   Get temperature   log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -type temperature
   Get capacity      log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -type capacity
   Get configuration log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -type configuration
   Get message       log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -type message
   Get statistic     log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -type statistic 
   
   After setup new cluster version (sh kineticAdmin.sh -setup -newclversion 1) for the simulator/drive
   Get all type log information: sh kineticAdmin.sh -getlog -clversion 1
                                 or
                                 sh kineticAdmin.sh -getlog -clversion 1 -type all
   Get utilization   log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type utilization
   Get temperature   log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type temperature
   Get capacity      log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type capacity
   Get configuration log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type configuration
   Get message       log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type message
   Get statistic     log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type statistic
   
5. firmware download
   kineticAdmin -firmware <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-pin <pin>]
   
   Parameters are optional, the default values are set as below: 
   host=127.0.0.1
   tlsport=8443
   clversion=0            
   
   For example,
   Simulator/drive locally:
   Before setup new cluster version：sh kineticAdmin.sh -firmware /Users/Emma/123.run
   After setup new cluster version (sh kineticAdmin.sh -setup -newclversion 1) for the simulator/drive
         sh kineticAdmin.sh -firmware /Users/Emma/123.run -clversion 1
         
         
   Simulator/drive remotely(IP:10.24.70.123):
   Before setup new cluster version：sh kineticAdmin.sh -firmware /Users/Emma/123.run -host 10.24.70.123
   After setup new cluster version (sh kineticAdmin.sh -setup -newclversion 1) for the simulator/drive
         sh kineticAdmin.sh -firmware /Users/Emma/123.run -host 10.24.70.123 -clversion 1  
         
Usage of proto scripts
===========================
1. Sync protocol file from Kinetic-Protocol github repo [https://github.com/Seagate/Kinetic-Protocol.git] to local.
   $sh syncProtoFromRepo.sh  to get the latest version.
   $sh syncProtoFromRepo.sh $commitHash to get the commit hash version. 
   For example,
   $sh syncProtoFromRepo.sh c4c95530b099c4882f3229560038e427e85fe219
   
2. Build protocol file locally, including compile kinetic.proto, kineticDb.proto, kineticIo.proto.
   $sh buildProto.sh
