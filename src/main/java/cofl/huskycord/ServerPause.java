package cofl.huskycord;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.Category;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static cofl.huskycord.HuskycordMod.LOGGER;

public class ServerPause {
    private static final ScheduledExecutorService debounceThread =
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
    private static volatile ScheduledFuture<?> debounceTask;

    public static final GameRules.Key<GameRules.IntegerValue> SERVER_AUTOFREEZE_DELAY =
        GameRuleRegistry.register("serverAutofreezeDelay", Category.MISC,
            GameRuleFactory.createIntRule(10, 1, 300));

    public static void register(){
        ServerLifecycleEvents.SERVER_STARTING.register(ServerPause::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(ServerPause::onServerStarted);
        ServerPlayConnectionEvents.JOIN.register(ServerPause::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(ServerPause::onPlayerLeave);
    }

    private static void setFrozen(MinecraftServer server, boolean state, boolean broadcast){
        var isFrozen = server.tickRateManager().isFrozen();
        if(state && broadcast && isFrozen){
            LOGGER.info("Entering new frozen state, performing save.");
            server.saveEverything(true, false, false);
        }

        var tickRateManager = (ITickRateManager)server.tickRateManager();
        var autosave = (IAutosaveManager)server;
        tickRateManager.huskycord$setFrozen(state, broadcast);
        autosave.huskycord$setShouldSave(!state);
    }

    private static void onServerStarting(MinecraftServer server) {
        LOGGER.info("Freezing server at startup.");
        setFrozen(server, true, false);
    }

    private static void onServerStarted(MinecraftServer server) {
        LOGGER.info("Re-freezing server after startup.");
        setFrozen(server, true, true);
    }

    private static void onPlayerJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        var atomicTask = debounceTask;
        if(atomicTask != null && !atomicTask.isDone()){
            LOGGER.info("Cancelling pending freeze task.");
            atomicTask.cancel(false);
        }

        if(server.tickRateManager().isFrozen()){
            LOGGER.info("Player joined, unfreezing server.");
            setFrozen(server, false, true);
        }
    }

    private static void onPlayerLeave(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        if(server.getPlayerCount() <= 1){
            var delayTime = server.getGameRules().getInt(SERVER_AUTOFREEZE_DELAY);
            LOGGER.info("Potential last player left, scheduling server freeze in {} seconds.", delayTime);
            debounceTask = debounceThread.schedule(() -> {
                if (server.getPlayerCount() <= 0){
                    LOGGER.info("Last player left, freezing server.");
                    server.execute(() -> setFrozen(server, true, true));
                }
            }, delayTime, TimeUnit.SECONDS);
        }
    }
}
