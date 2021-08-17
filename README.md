# Kotlin Mindstorms EV3 Bot

Or "stuffing modern tech into an old LEGO ev3 to see if it works while cackling like a mad scientist."

## Bugs

* Needs the path.  `jrun -cp "/home/lejos/programs/kev3-1.0-SNAPSHOT.jar:/home/root/lejos/lib/*" info.benjaminhill.kev3.MainKt`
* Caliko is compiled with JDK11.  Recompiled locally with JDK8.
* Issue with HTTPS/SSL.  `date -s "2021-07-29 05:31:00"`
* Code deploy run loop is too many commands.
* java.lang.UnsupportedOperationException: Remote regulators are not supported
* Missing the JNA dependency will make you smack face on keyboard.  Tried removing extra jna.
* 

## Setup LeJOS (Java 1.8) on EV3

1. [Latest LeJOS](https://sourceforge.net/projects/ev3.lejos.p/files/0.9.1-beta/)
2. Reformat ~2gb microSD disk as MS DOS (FAT) to avoid permission bug in balena etcher
3. Flash sd500.img as a nice baseline.
4. unzip the lejosimage.zip file from the leJOS home directory to the root directory of the card
5. You want JRE 1.8. The webpage has 1.7. 😖 
6. Grab ejdk1.8.0.  From: Java 8 support - leJOS (don't forget that `-g`, it matters!)
    * `export JAVA_HOME=$(/usr/libexec/java_home -v1.8)`
    * `./jrecreate.sh -g --dest ejre-8-b132-linux-arm-sflt --profile compact2 --vm client`
    * `tar czf ejre-8-b132-linux-arm-sflt.tar.gz ejre-8-b132-linux-arm-sflt`
    * Move that tar.gz file to the root of the card along with everything else
    * Boot EV3. Let the potato rest for 5 minutes.

Login and verify:

```bash
ssh -c aes256-cbc  -oKexAlgorithms=+diffie-hellman-group1-sha1 root@192.168.86.45
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

### If you need inverse kinematics, but Caliko is not on maven central

```bash
# from the caliko ./target/ folder: 
mvn install:install-file \
  -DgroupId=au.edu.federation.caliko \
  -DartifactId=caliko \
  -Dversion=1.3.8 \
  -Dfile=caliko-1.3.8.jar \
  -DgeneratePom=true \
  -Dpackaging=jar
```

## Load the script from a web service

```bash
cd kev3bot-server
firebase init
# In the FireBase console, create your empty Firestore Database, fill in your own PROJECT_ID and API_KEY.
```

The ev3's wifi dongle is... not fast.  (maybe 20KB/sec).
You will want to 1-time copy over all the files from 
`mvn dependency:copy-dependencies` to the robot, so each time you
build/deploy/test you only have to move the very small app-code only jar file over. 
**Do this whenever you update your maven dependencies.**

    scp -c aes256-cbc -oKexAlgorithms=+diffie-hellman-group1-sha1 ./target/dependency/*.jar root@192.168.86.45:/home/root/lejos/lib/
    # no password

Every time you want to test some code:

    mvn deploy
    mvn antrun:run

Or you can do it from a ssh:

    jrun -cp "/home/lejos/programs/kev3-1.0-SNAPSHOT.jar:/home/root/lejos/lib/*" info.benjaminhill.kev3.MainKt

# Sources:

* [leJOS / EV3 Wiki / Installing leJOS](https://sourceforge.net/p/lejos/wiki/Installing%20leJOS/)
* [jornfranke/lejos-ev3-example: Example for a Gradle project for LEGO Mindstorm EV3 and Lejos](https://github.com/jornfranke/lejos-ev3-example)
* [Oracle ejdk-8-fcs-b132-linux-arm-sflt](http://download.oracle.com/otn/java/ejdk/8-b132/ejdk-8-fcs-b132-linux-arm-sflt-03_mar_2014.tar.gz)
* [Tutorial on how to install and run Java on Lego Mindstorms EV3 using Eclipse on Mac OS X](http://www.bartneck.de/2017/06/04/tutorial-on-how-to-install-and-run-java-on-lego-mindstorms-ev3-using-eclipse-on-mac-os-x/)
* [Kotlin + Lego](https://dev.to/viniciusccarvalho/kotlin--lego-59fc)
