# Chess game application.

The application has two modes:

- Standalone
  * allows 2 players to play chess on single computer
- LAN
  * allows 2 players to play over socket connection

**Note**: in order to succeed you must have jfxrt.jar in your classpath (if you have Java 8 it is likely you do).

For build use `mvn package`. To launch the app go to /target folder and run `java -jar Chess.jar`.