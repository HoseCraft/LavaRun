package com.infermc.lavarun;

import com.infermc.hosecraft.command.Command;
import com.infermc.hosecraft.events.EventHandler;
import com.infermc.hosecraft.events.Listener;
import com.infermc.hosecraft.events.chat.ChatEvent;
import com.infermc.hosecraft.events.player.PlayerJoinEvent;
import com.infermc.hosecraft.events.player.PlayerQuitEvent;
import com.infermc.hosecraft.plugins.JavaPlugin;
import com.infermc.hosecraft.server.Location;
import com.infermc.hosecraft.server.Player;
import com.infermc.hosecraft.util.Chat;
import com.infermc.hosecraft.wrappers.ConfigSection;
import com.infermc.hosecraft.wrappers.YamlConfiguration;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.tile.Block;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LavaRun extends JavaPlugin implements Listener {

    int lobbyDuration = 30;
    int prepDuration = 15;
    int gameDuration = 60;

    boolean gameRunning = false;
    boolean inLobby = true;
    ArrayList<String> deadPlayers = new ArrayList<String>();

    int timeLeft = lobbyDuration;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onLoad() {
        getServer().registerLevelGenerator(new LavaRunLevelGenerator(getLogger()));
    }

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        BlockHandler bk = new BlockHandler(this);

        if (getServer() != null) {
            getServer().getPluginManager().registerEvents(this, this);
            getServer().getPluginManager().registerEvents(bk, this);
            getLogger().info("I've been enabled. I'm running on HoseCraft v" + getServer().getVersion() + "-" + getServer().getFlavour());
        }

        //getServer().getCommandRegistry().registerCommand(this,new Command("trust",new trustedManager(this)).setDescription("Adds a player to the trusted players list."));
        resetGame();
        startTimer();
    }


    public void startTimer() {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                if (getServer().getPlayers().size() > 0) {

                    timeLeft--;

                    String countdown = timeLeft + " seconds left until";
                    if (inLobby) {
                        countdown = countdown + " the game starts!";
                    } else if (!inLobby && !gameRunning) {
                        countdown = countdown + " lava starts to fall!";
                    } else if (gameRunning) {
                        countdown = countdown + " the game finishes!";
                    }

                    if (timeLeft % 5 == 0) {
                        getLogger().info(countdown);
                    }

                    if (timeLeft <= 5 && timeLeft > 0) {
                        for (Player p : getServer().getPlayers()) {
                            p.sendMessage(countdown);
                        }
                    } else if (timeLeft <= 0) {
                        if (gameRunning && !inLobby) {
                            // Game finished!
                            getLogger().info("Game finished!");
                            resetGame();
                            for (Player p : getServer().getPlayers()) {
                                p.kick("You survived! +1 Point, Resetting, Please wait!");
                            }
                        } else if (inLobby) {
                            // Lobby time
                            inLobby = false;
                            timeLeft = prepDuration;
                            getLogger().info("Game Started!");
                            for (Player p : getServer().getPlayers()) {
                                p.sendMessage("Let the games begin! You have " + prepDuration + " seconds to build a shelter!");
                            }

                        } else {
                            // Prep time
                            gameRunning = true;
                            timeLeft = gameDuration;
                            getLogger().info("Prep time over, dropping lava!");
                            for (Player p : getServer().getPlayers()) {
                                p.sendMessage("Let the Lava FALL! You have to survive for " + gameDuration + " seconds");
                            }
                            placeLava();
                        }
                    }
                    for (Player p : getServer().getPlayers()) {
                        if (p.getLocation().getBlock().id == Block.LAVA.id || p.getLocation().getBlock().id == Block.STATIONARY_LAVA.id) {
                            deadPlayers.add(p.getName());
                            p.kick("You're dead! Please wait for the next game!");
                        }
                    }
                }
                startTimer();
            }
        }, 1L, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        if (isDead(ev.getPlayer().getName())) {
            ev.setMessage(null);
            ev.getPlayer().kick("The game is still in progress and you've died!");
        } else {
            ev.setMessage("&8" + ev.getPlayer().getName()+" has joined &6LavaRun");
            if (inLobby && !gameRunning) {
                ev.getPlayer().sendMessage("You're just in time, the game will start in "+timeLeft+" seconds!");
            } else if (!inLobby && !gameRunning) {
                ev.getPlayer().sendMessage("Quick! You need to shelter from the lava that will fall in "+timeLeft+" seconds!");
            } else {
                ev.getPlayer().sendMessage("QUICK! Lava is already falling shelter and survive for "+timeLeft+" seconds!");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        getLogger().info("There are "+getServer().getPlayers().size()+" players online");
        if (getServer().getPlayers().size() <= 1) {
            getLogger().info("All players left! Resetting");
            resetGame();
        }
    }

    public boolean isDead(String username) {
        for (String p : deadPlayers) {
            if (p.equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    public boolean isOnline(String username) {
        if (getOnlinePlayer(username) != null) return true;
        return false;
    }

    public Player getOnlinePlayer(String username) {
        for (Player p : getServer().getPlayers()) {
            if (p.getName().equalsIgnoreCase(username)) return p;
        }
        return null;
    }

    public List<String> wordWrap(String input, int maxLineLength) {
        String[] words = input.split(" ");
        List<String> output = new ArrayList<String>();
        String sc = "";
        for (String word : words) {
            if ((sc.length()+1 + word.length()) > maxLineLength) {
                output.add(sc.trim());
                sc = word;
            } else {
                sc = sc +" "+ word;
            }
        }
        if (sc != "") output.add(sc.trim());
        return output;
    }

    public void resetGame() {
        getLogger().info("Resetting Game");
        deadPlayers.clear();
        timeLeft = lobbyDuration;
        inLobby = true;
        gameRunning = false;

        getLogger().info("Generating the Level");
        Level lvl = getServer().getLevelGenerator("LavaRunLevelGenerator").generate("--",256,256,64);
        getServer().MC.c = lvl;
        getLogger().info("Finished. Game ready to start again!");

        startTimer();
    }
    public void placeLava() {
        Location lava = new Location(getServer().MC.c,0,32,0);
        lava.setBlock(Block.LAVA);

        for (Player p : getServer().getPlayers()) {
            getLogger().info(p.getLocation().getX()+" "+p.getLocation().getY()+" "+p.getLocation().getZ());
        }
    }
}