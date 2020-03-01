# paintbot-client-java

This a Paintbot Client written in Java 11.

## Requirements

* Java JDK >= 11
* Gradle
* Paintbot Server (local or remote)

## Installation

A. Clone the repository: `git clone git@github.com:cygni/paintbot-client-java.git`.

B. Open: `<repo>/`

C. Execute: `./gradlew build`

## Usage

To clean and build:
```
> ./gradlew clean build
```

To run your client:
```
> ./gradlew run
```

## Implementation

There is only one class in this project, have a look at SimplePaintbotPlayer.java. The main method to start in looks like this:

```java
@Override
public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
    ansiPrinter.printMap(mapUpdateEvent);
    // Some code doing soem logic for the bot


    // Choose action here!
    registerMove(mapUpdateEvent.getGameTick(), CharacterAction.DOWN);
}
```

For every MapUpdateEvent received your are expected to reply with a CharacterAction (UP, DOWN, LEFT, RIGHT or EXPLODE). 
