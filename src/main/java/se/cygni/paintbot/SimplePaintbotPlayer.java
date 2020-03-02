package se.cygni.paintbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import se.cygni.paintbot.api.event.*;
import se.cygni.paintbot.api.exception.InvalidPlayerName;
import se.cygni.paintbot.api.model.CharacterAction;
import se.cygni.paintbot.api.model.GameMode;
import se.cygni.paintbot.api.model.GameSettings;
import se.cygni.paintbot.api.model.PlayerPoints;
import se.cygni.paintbot.api.response.PlayerRegistered;
import se.cygni.paintbot.api.util.GameSettingsUtils;
import se.cygni.paintbot.client.AnsiPrinter;
import se.cygni.paintbot.client.BasePaintbotClient;
import se.cygni.paintbot.client.MapUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class SimplePaintbotPlayer extends BasePaintbotClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePaintbotPlayer.class);

    // Set to false if you want to start the game from a GUI
    private static final boolean AUTO_START_GAME = true;

    // Personalise your game ...
    private static final String SERVER_NAME = "ecs-load-balancer-168404476.eu-west-1.elb.amazonaws.com"; //"paintbot.cygni.se";
    private static final int SERVER_PORT = 80;

    private static final GameMode GAME_MODE = GameMode.TRAINING;
    private static final String BOT_NAME = "The Simple Painter";

    // Set to false if you don't want the game world printed every game tick.
    private static final boolean ANSI_PRINTER_ACTIVE = false;
    private AnsiPrinter ansiPrinter = new AnsiPrinter(ANSI_PRINTER_ACTIVE, true);

    public static void main(String[] args) {
        SimplePaintbotPlayer simplePaintbotPlayer = new SimplePaintbotPlayer();

        try {
            ListenableFuture<WebSocketSession> connect = simplePaintbotPlayer.connect();
            connect.get();
        } catch (Exception e) {
            LOGGER.error("Failed to connect to server", e);
            System.exit(1);
        }

        startTheBot(simplePaintbotPlayer);
    }

    /**
     * The Snake client will continue to run ...
     * : in TRAINING mode, until the single game ends.
     * : in TOURNAMENT mode, until the server tells us its all over.
     */
    private static void startTheBot(final SimplePaintbotPlayer simplePaintbotPlayer) {
        Runnable task = () -> {
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (simplePaintbotPlayer.isPlaying());

            LOGGER.info("Shutting down");
        };

        Thread thread = new Thread(task);
        thread.start();
    }

    @Override
    public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        ansiPrinter.printMap(mapUpdateEvent);

        // MapUtil contains lot's of useful methods for querying the map!
        MapUtil mapUtil = new MapUtil(mapUpdateEvent.getMap(), getPlayerId());

        List<CharacterAction> actions = new ArrayList<>();

        // Let's see in which movement actions I can take
        for (CharacterAction action : CharacterAction.values()) {
            if (mapUtil.canIMoveInDirection(action)) {
                actions.add(action);
            }
        }

        // Check if we're carrying a power up
        if(mapUtil.getMyCharacterInfo().isCarryingPowerUp()) {
            actions.add(CharacterAction.EXPLODE);
        }

        Random r = new Random();
        CharacterAction chosenAction = CharacterAction.DOWN;

        // Choose a random direction
        if (!actions.isEmpty()) {
            chosenAction = actions.get(r.nextInt(actions.size()));
        }

        // Register action here!
        registerMove(mapUpdateEvent.getGameTick(), chosenAction);
    }

    @Override
    public void onPaintbotDead(CharacterStunnedEvent characterStunnedEvent) {
        // Wrong name, does not die. Might be stunned though by crashing or getting caught in explosion
    }


    @Override
    public void onInvalidPlayerName(InvalidPlayerName invalidPlayerName) {
        LOGGER.debug("InvalidPlayerNameEvent: " + invalidPlayerName);
    }

    @Override
    public void onGameResult(GameResultEvent gameResultEvent) {
        LOGGER.info("Game result:");
        gameResultEvent.getPlayerRanks().forEach(playerRank -> LOGGER.info(playerRank.toString()));
    }

    @Override
    public void onGameEnded(GameEndedEvent gameEndedEvent) {
        LOGGER.debug("GameEndedEvent: " + gameEndedEvent);
    }

    @Override
    public void onGameStarting(GameStartingEvent gameStartingEvent) {
        LOGGER.debug("GameStartingEvent: " + gameStartingEvent);
    }

    @Override
    public void onPlayerRegistered(PlayerRegistered playerRegistered) {
        LOGGER.info("PlayerRegistered: " + playerRegistered);

        if (AUTO_START_GAME) {
            startGame();
        }
    }

    @Override
    public void onTournamentEnded(TournamentEndedEvent tournamentEndedEvent) {
        LOGGER.info("Tournament has ended, winner playerId: {}", tournamentEndedEvent.getPlayerWinnerId());
        int c = 1;
        for (PlayerPoints pp : tournamentEndedEvent.getGameResult()) {
            LOGGER.info("{}. {} - {} points", c++, pp.getName(), pp.getPoints());
        }
    }

    @Override
    public void onGameLink(GameLinkEvent gameLinkEvent) {
        LOGGER.info("The game can be viewed at: {}", gameLinkEvent.getUrl());
    }

    @Override
    public void onSessionClosed() {
        LOGGER.info("Session closed");
    }

    @Override
    public void onConnected() {
        LOGGER.info("Connected, registering for training...");
        GameSettings gameSettings = GameSettingsUtils.trainingWorld();
        registerForGame(gameSettings);
    }

    @Override
    public String getName() {
        return BOT_NAME;
    }

    @Override
    public String getServerHost() {
        return SERVER_NAME;
    }

    @Override
    public int getServerPort() {
        return SERVER_PORT;
    }

    @Override
    public GameMode getGameMode() {
        return GAME_MODE;
    }
}
