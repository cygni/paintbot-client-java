# Paintbot client in Java

This is a Paintbot Client written in Java 11. 

For more information about what Paintbot is, see: https://paintbot.cygni.se/

For running your own server see [Paintbot Server Repository](https://github.com/cygni/paintbot)

## Requirements

* Java JDK >= 11
* Gradle
* Paintbot Server (local or remote, there's one running by Cygni so no worries ;) )

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

There is only one class in this project, have a look at SimplePaintbotPlayer.java. The method to start in looks like this:

```java
@Override
public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
    ansiPrinter.printMap(mapUpdateEvent);
    // Some code doing soem logic for the bot


    // Choose action here!
    registerMove(mapUpdateEvent.getGameTick(), CharacterAction.DOWN);
}
```

For every MapUpdateEvent received your are expected to reply with a CharacterAction (UP, DOWN, LEFT, RIGHT, STAY or EXPLODE). 

### Help
There's a utility class with nifty methods to help you out. Take a look at `MapUtil` and what it offers

### Pitfalls

Beware the common mishaps:

- If two bots try to move to the same empty space, they will collide and stun each other. Once the stun ends, they risk doing the same thing again. And again, and again. Don't be the bot who runs into another bot the whole game!
