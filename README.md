# Kotlin Mindstorms EV3 Bot

Or "stuffing modern tech into an old LEGO ev3 to see if it works"

##Setup LeJOS (Java 1.8) on EV3

Sources: 
* [leJOS / EV3 Wiki / Installing leJOS](https://sourceforge.net/p/lejos/wiki/Installing%20leJOS/)
* [jornfranke/lejos-ev3-example: Example for a Gradle project for LEGO Mindstorm EV3 and Lejos](https://github.com/jornfranke/lejos-ev3-example)
* [Oracle ejdk-8-fcs-b132-linux-arm-sflt](http://download.oracle.com/otn/java/ejdk/8-b132/ejdk-8-fcs-b132-linux-arm-sflt-03_mar_2014.tar.gz)
* [Tutorial on how to install and run Java on Lego Mindstorms EV3 using Eclipse on Mac OS X](http://www.bartneck.de/2017/06/04/tutorial-on-how-to-install-and-run-java-on-lego-mindstorms-ev3-using-eclipse-on-mac-os-x/)

1. Reformat disk as MS DOS (FAT) to avoid permission bug in balena etcher
2. flash sd500.img
3. unzip the lejosimage.zip file from the leJOS home directory to the root directory of the card
4. Need the right Oracle JRE, but webpage only has 1.7 and I want 1.8
5. From: Java 8 support - leJOS
   * `export JAVA_HOME=$(/usr/libexec/java_home -v1.8)`
   * `./jrecreate.sh -g --dest newjre --profile compact2 --vm client`
   * `tar czf ejre-8-b132-linux-arm-sflt.tar.gz ejre-8-b132-linux-arm-sflt`
   * Drop that file off in the root of the card along with everything else
   * Boot EV3.  Let it think for 10 minutes.

Login and verify:

```bash
sudo nano /etc/ssh/ssh_config # uncomment line with old ssh algos.
ssh root@192.168.86.42
# (no pw)
root@EV3:/# /home/root/lejos/ejre-8-b132-linux-arm-sflt/bin/java -version
# java version "1.8.0"
# Java(TM) SE Embedded Runtime Environment (build 1.8.0-b132, profile compact2, headless)
```
... all looks good!

## Setup Local Dev Env
Latest IntelliJ IDE, create a new app: Kotlin > Console App > JRE 1.8
(creates a pom.xml)

Or clone https://github.com/salamanders/kev3

```bash
mvn versions:display-dependency-updates # then fix outdated stuff
mvn dependency:copy-dependencies # which creates target/dependency folder of the jars you need!
```
    
    





## NOTES
    mvn deploy
    mvn antrun:run

To SSH into the robot:

    root@EV3:/home/lejos/programs# jrun -cp whiteboardbot-0.0.1-SNAPSHOT-jar-with-dependencies.jar info.benjaminhill.wbb.MainKt

Copying files

    scp -oKexAlgorithms=+diffie-hellman-group1-sha1 ./x.jar root@192.168.86.250:/home/root/lejos/lib/

To view console Run ev3console or Eclipse: ev3control
http://www.lejos.org/ev3/docs/

    
    
   
