# Rooms / Backend

This is the backend server for the Rooms service, which finds bookable rooms which aren't already booked at Chalmers
campus Johanneberg.

## Building and running

The project is a Gradle project running Gradle 7.2 with the Application plugin. The program is compatible with Java 11
or later. Run the jobs directly from `gradlew` (or `gradlew.bat`), like `./gradlew run`. A wrapper jar is included so
gradle doesn't need to be installed separately. The job `installDist` makes an install to build/install, and
`distTar`/`distZip` will tar/zip it. It can also be run directly by Gradle using `gradle run`.
