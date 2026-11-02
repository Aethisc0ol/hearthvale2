package com.hearthvale;

import com.hearthvale.client.ClientPayloadHandler;
import com.hearthvale.dialogue.DialogueManager;
import com.hearthvale.entity.NpcEntity;
import com.hearthvale.network.DialoguePagePayload;
import com.hearthvale.network.DialogueResponsePayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Mod-bus events: attributes, creative tabs, network registration. */
@EventBusSubscriber(modid = Hearthvale.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEvents {
    private ModEvents() {}

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(ModRegistry.NPC.get(), NpcEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void tabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            e.accept(ModRegistry.HEARTH_ITEM.get());
        }
        if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            e.accept(ModRegistry.DEED.get());
            e.accept(ModRegistry.JOURNAL.get());
            e.accept(ModRegistry.BOUQUET.get());
            e.accept(ModRegistry.PENDANT.get());
        }
    }

    @SubscribeEvent
    public static void payloads(RegisterPayloadHandlersEvent e) {
        PayloadRegistrar r = e.registrar("1");
        r.playToClient(DialoguePagePayload.TYPE, DialoguePagePayload.CODEC, (payload, ctx) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) ClientPayloadHandler.handlePage(payload);
        });
        r.playToServer(DialogueResponsePayload.TYPE, DialogueResponsePayload.CODEC, (payload, ctx) -> {
            if (ctx.player() instanceof ServerPlayer sp) DialogueManager.respond(sp, payload.choice());
        });
    }
}
