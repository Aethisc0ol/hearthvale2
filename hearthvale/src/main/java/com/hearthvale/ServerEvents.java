package com.hearthvale;

import com.hearthvale.command.HearthvaleCommands;
import com.hearthvale.data.BuildingRegistry;
import com.hearthvale.data.NpcRegistry;
import com.hearthvale.dialogue.DialogueManager;
import com.hearthvale.world.VillageManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Game-bus events. */
@EventBusSubscriber(modid = Hearthvale.MODID)
public final class ServerEvents {
    private ServerEvents() {}

    @SubscribeEvent
    public static void reload(AddReloadListenerEvent e) {
        e.addListener(new BuildingRegistry());
        e.addListener(new NpcRegistry());
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post e) {
        VillageManager.tick(e.getServer());
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        HearthvaleCommands.register(e.getDispatcher());
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        DialogueManager.end(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent e) {
        DialogueManager.clear();
    }
}
