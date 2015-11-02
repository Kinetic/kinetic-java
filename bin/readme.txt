Kinetic Simulator/Admin Command Line Interface (CLI).

0. Assume you have at least Java SE 1.6 or 1.7 or 1.8 installed on your machine.
 
1. Define JAVA_HOME environment variable (example: google 'how to set JAVA_HOME on Mac).

2. Run "mvn clean package" in <Kinetic-Folder>, verify 
   <Kinetic-Folder>/kinetic-simulator/target/kinetic-simulator-0.8.0.1-SNAPSHOT-jar-with-dependencies.jar 
   <Kinetic-Folder>/kinetic-client/target/kinetic-client-0.8.0.1-SNAPSHOT-jar-with-dependencies.jar
   exist.

3. To start Kinetic simulator:

   Windows: 
            cd <Kinetic-Folder>\bin
            startSimulator.bat
            
   Linux & Mac:
            cd <Kinetic-Folder>/bin
            sh startSimulator.sh

3.1 To start multiple simulators:

   Linux & Mac:
            cd <Kinetic-Folder>/bin
	    sh startMultiSimulators.sh [#instances start_tcp_port start_ssl_port]
            
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
            
7. To ping the drive or simulator (Linux & Mac):
	    cd <Kinetic-Folder>/bin
	    sh ping.sh [host] [host port]


Run smoke test against simulator or kinetic drive
==================================
Make sure one instance of simulator or kinetic drive is running.

1. Run "mvn clean package" in <Kinetic-Folder> or <Kinetic-Folder>/kinetic-test, verify 
   <Kinetic-Folder>/kinetic-test/target/kinetic-test-0.8.0.2-SNAPSHOT-jar-with-dependencies.jar 
   <Kinetic-Folder>/kinetic-test/target/smoke-tests.jar
   exist.

2. cd <Kinetic-Folder>/bin

3. sh runSmokeTests.sh [-host host_ip] [-port port] [-tlsport tlsport] [-home kinetic_home]
   or
   python runSmokeTests.py [-host host_ip] [-port port] [-tlsport tlsport] [-home kinetic_home]
   

Run chassis test against simulator or kinetic drive
==================================
Make sure one instance of simulator or kinetic drive is running.  
  
1. Run "mvn clean package" in <Kinetic-Folder> or <Kinetic-Folder>/kinetic-test, verify 
   <Kinetic-Folder>/kinetic-test/target/kinetic-test-0.8.0.2-SNAPSHOT-jar-with-dependencies.jar 
   <Kinetic-Folder>/kinetic-test/target/smoke-tests.jar
   exist.

2. cd <Kinetic-Folder>/bin

3. sh chassisTest.sh -iplist ip1,ip2,..ipn

   If the chassis ip has the same subnet, no need to write all the ip, follow below:
   sh chassisTest.sh -iplist ip1~ipn
   
   for example, 192.168.2.10~192.168.2.25
   sh chassisTest.sh -iplist 192.168.2.10~192.168.2.25
   
   The test result will be in <Kinetic-Folder>/bin/kinetic_log/"drive's ip".log
   

Run sanity test against simulator or kinetic drive
==================================
Make sure one instance of simulator or kinetic drive is running.  
  
1. Run "mvn clean package" in <Kinetic-Folder> or <Kinetic-Folder>/kinetic-test, verify 
   <Kinetic-Folder>/kinetic-test/target/kinetic-test-0.8.0.2-SNAPSHOT-jar-with-dependencies.jar 
   <Kinetic-Folder>/kinetic-test/target/smoke-tests.jar
   exist.

2. cd <Kinetic-Folder>/bin

3. sh runSanityTests.sh [-host host_ip] [-port port] [-debug true|false] [-type admin|basic|all]
   
   host      simulator or drive's ip address, default is 127.0.0.1
   port      connection port, default is 8123
   debug     turn kinetic message dump info or not, default is false
   type      run admin sanity tests or basic sanity tests or all, default is basic
   
   for example, 
   sh runSanityTests.sh -host 10.24.70.123 -port 8123 -debug true -type all
   
   The admin sanity test result will be in <Kinetic-Folder>/bin/admin.result
   The basic sanity test result will be in <Kinetic-Folder>/bin/basic.result
   
   
Usage of Kinetic Admin API script
==================================
1. kineticAdmin.sh, you can use "-help" to see usage.
   
   Usage: kineticAdmin <-setclusterversion|-seterasepin|-setlockpin|-instanterase|-secureerase|-security|-getlog|-getvendorspecificdevicelog|-firmware|-lockdevice|-unlockdevice>
          
          kineticAdmin -h|-help
          kineticAdmin -setclusterversion <-newclversion <newclusterversion>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
          kineticAdmin -seterasepin <-olderasepin <olderasepin>> <-newerasepin <newerasepin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
          kineticAdmin -setlockpin <-oldlockpin <oldlockpin>> <-newlockpin <newlockpin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
          kineticAdmin -instanterase <-pin <erasepin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
          kineticAdmin -secureerase <-pin <erasepin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
          kineticAdmin -security <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
          kineticAdmin -getlog [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-type <utilization|temperature|capacity|configuration|message|statistic|limits|all>]
          kineticAdmin -getvendorspecificdevicelog <-name <vendorspecificname>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
          kineticAdmin -firmware <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-pin <pin>]
          kineticAdmin -lockdevice <-pin <lockpin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
          kineticAdmin -unlockdevice <-pin <lockpin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
2. setclusterversion:
   sh kineticAdmin.sh -setclusterversion [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-newclversion <newclusterversion>]
   
   Parameters are optional, the default values are set as below: 
   host=127.0.0.1    simulator/drive's ipaddress
   tlsport=8443      admin client connect to simulator/drive via this port
   clversion=0       admin client connect to simulator/drive within this cluster version
   
   For example,
   set new cluster version.
       Simulator/drive locally:
       First time: sh kineticAdmin.sh -setclusterversion -newclversion 1
       Second time: sh kineticAdmin.sh -setclusterversion -clversion 1 -newclversion 2
       
       Simulator/drive remotely(IP:10.24.70.123):
       First time: sh kineticAdmin.sh -setclusterversion -host 10.24.70.123 -newclversion 1
       Second time: sh kineticAdmin.sh -setclusterversion -host 10.24.70.123 -clversion 1 -newclversion 2

3. seterasepin
   sh kineticAdmin.sh -seterasepin <-olderasepin <olderasepin>> <-newerasepin <newerasepin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]

   Parameters are optional, the default values are set as below: 
   host=127.0.0.1    simulator/drive's ipaddress
   tlsport=8443      admin client connect to simulator/drive via this port
   clversion=0       admin client connect to simulator/drive within this cluster version

   For example,
   set erase pin.
       Simulator/drive locally:
       First time: sh kineticAdmin.sh -seterasepin -olderasepin anything -newerasepin 123
       Second time: sh kineticAdmin.sh -seterasepin -olderasepin 123 -newerasepin 456
       Third time: after set new cluster version (sh kineticAdmin.sh -setclusterversion -newclversion 1)
                    sh kineticAdmin.sh -seterasepin -clversion 1 -olderasepin 456 -newerasepin 789
       
       
       Simulator/drive remotely(IP:10.24.70.123):
       First time: sh kineticAdmin.sh -seterasepin -host 10.24.70.123 -olderasepin anything -newerasepin 123
       Second time: sh kineticAdmin.sh -seterasepin -host 10.24.70.123 -olderasepin 123 -newerasepin 456
       Third time: after set new cluster version (sh kineticAdmin.sh -setclusterversion -host 10.24.70.123 -newclversion 1)
                    sh kineticAdmin.sh -seterasepin -host 10.24.70.123 -clversion 1 -olderasepin 456 -newerasepin 789
                    
4. setlockpin
   Similar as seterasepin. Please see #3.
   
5. instanterase
   sh kineticAdmin.sh -instanterase <-pin <erasepin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
    
   Parameters are optional, the default values are set as below: 
   host=127.0.0.1    simulator/drive's ipaddress
   tlsport=8443      admin client connect to simulator/drive via this port
   clversion=0       admin client connect to simulator/drive within this cluster version
      
       Simulator/drive locally:
       First time: sh kineticAdmin.sh -instanterase -pin anything
       Second time, 
         after set new cluster version (sh kineticAdmin.sh -setclusterversion -newclversion 1) for the simulator/drive
                   sh kineticAdmin.sh -instanterase -clversion 1 -pin anything 


       Simulator/drive remotely(IP:10.24.70.123):
       First time: sh kineticAdmin.sh -instanterase -host 10.24.70.123 -pin anything
       Second time, 
         after set new cluster version (sh kineticAdmin.sh -setclusterversion -host 10.24.70.123 -newclversion 1) for the simulator/drive
                   sh kineticAdmin.sh -instanterase -host 10.24.70.123 -clversion 1 -pin anything
                   
6. secureerase
   Similar as instanterase. Please see #5.             
                   
7. set security                   
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
         
8. get log
   sh kineticAdmin.sh -getlog [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-type <utilization|temperature|capacity|configuration|message|statistic|limits|all>]
   
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
   Get limits        log info: sh kineticAdmin.sh -getlog -type limits
   
   After setup new cluster version (sh kineticAdmin.sh -setclusterversion -newclversion 1) for the simulator/drive
   Get all type log information: sh kineticAdmin.sh -getlog -clversion 1
                                 or
                                 sh kineticAdmin.sh -getlog -clversion 1 -type all
   Get utilization   log info: sh kineticAdmin.sh -getlog -clversion 1 -type utilization
   Get temperature   log info: sh kineticAdmin.sh -getlog -clversion 1 -type temperature
   Get capacity      log info: sh kineticAdmin.sh -getlog -clversion 1 -type capacity
   Get configuration log info: sh kineticAdmin.sh -getlog -clversion 1 -type configuration
   Get message       log info: sh kineticAdmin.sh -getlog -clversion 1 -type message
   Get statistic     log info: sh kineticAdmin.sh -getlog -clversion 1 -type statistic
   Get limits        log info: sh kineticAdmin.sh -getlog -clversion 1 -type limits
   
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
   Get limits        log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -type limits
   
   After setup new cluster version (sh kineticAdmin.sh -setclusterversion -newclversion 1) for the simulator/drive
   Get all type log information: sh kineticAdmin.sh -getlog -clversion 1
                                 or
                                 sh kineticAdmin.sh -getlog -clversion 1 -type all
   Get utilization   log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type utilization
   Get temperature   log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type temperature
   Get capacity      log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type capacity
   Get configuration log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type configuration
   Get message       log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type message
   Get statistic     log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type statistic
   Get limits        log info: sh kineticAdmin.sh -getlog -host 10.24.70.123 -clversion 1 -type limits

9. getvendorspecificdevicelog
   sh kineticAdmin.sh -getvendorspecificdevicelog <-name <vendorspecificname>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]

   Parameters are optional, the default values are set as below: 
   host=127.0.0.1
   tlsport=8443
   clversion=0
   
   For example,
   Simulator/drive locally:
   Before setup new cluster version：
   Get vendor specific device log information: sh kineticAdmin.sh -getvendorspecificdevicelog -name devicename
   
   Simulator/drive remotely(IP:10.24.70.123):
   Get vendor specific device log information: sh kineticAdmin.sh -getvendorspecificdevicelog -host 10.24.70.123 -name devicename

10. firmware download
   sh kineticAdmin.sh -firmware <file> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>] [-pin <pin>]
   
   Parameters are optional, the default values are set as below: 
   host=127.0.0.1
   tlsport=8443
   clversion=0            
   
   For example,
   Simulator/drive locally:
   Before setup new cluster version：sh kineticAdmin.sh -firmware /Users/Emma/123.run
   After setup new cluster version (sh kineticAdmin.sh -setclusterversion -newclversion 1) for the simulator/drive
         sh kineticAdmin.sh -firmware /Users/Emma/123.run -clversion 1
         
         
   Simulator/drive remotely(IP:10.24.70.123):
   Before setup new cluster version：sh kineticAdmin.sh -firmware /Users/Emma/123.run -host 10.24.70.123
   After setup new cluster version (sh kineticAdmin.sh -setclusterversion -newclversion 1) for the simulator/drive
         sh kineticAdmin.sh -firmware /Users/Emma/123.run -host 10.24.70.123 -clversion 1  
         
11. lockdevice
    sh kineticAdmin.sh -lockdevice <-pin <lockpin>> [-host <ip|hostname>] [-tlsport <tlsport>] [-clversion <clusterversion>]
       
    Parameters are optional, the default values are set as below: 
    host=127.0.0.1
    tlsport=8443
    clversion=0 
    
    For example,
    Simulator/drive locally:
    Before setup new cluster version：sh kineticAdmin.sh -lockdevice -pin lockpin
    After setup new cluster version (sh kineticAdmin.sh -setclusterversion -newclversion 1) for the simulator/drive
    sh kineticAdmin.sh -lockdevice -clversion 1 -pin lockpin
    
    Simulator/drive remotely(IP:10.24.70.123):
    Before setup new cluster version：sh kineticAdmin.sh -lockdevice -pin lockpin -host 10.24.70.123
    After setup new cluster version (sh kineticAdmin.sh -setclusterversion -newclversion 1) for the simulator/drive
    sh kineticAdmin.sh -lockdevice -pin lockpin -host 10.24.70.123 -clversion 1
    
12. unlock device
    Similar as lockdevice. Please see #11.
         

Usage of proto scripts
===========================
1. Sync protocol file from Kinetic-Protocol github repo [https://github.com/Kinetic/Kinetic-Protocol.git] to local.
   $sh syncProtoFromRepo.sh  to get the latest version.
   $sh syncProtoFromRepo.sh $commitHash to get the commit hash version. 
   For example,
   $sh syncProtoFromRepo.sh c4c95530b099c4882f3229560038e427e85fe219
   
2. Build protocol file locally, including compile kinetic.proto, kineticDb.proto, kineticIo.proto.
   $sh buildProto.sh


