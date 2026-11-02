package com.hearthvale.client;

import com.hearthvale.network.DialoguePagePayload;
import net.minecraft.client.Minecraft;

/** Only ever touched on the client (referenced lazily from the payload handler). */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handlePage(DialoguePagePayload p) {
        Minecraft mc = Minecraft.getInstance();
        if (p.text().isEmpty()) {
            if (mc.screen instanceof DialogueScreen) mc.setScreen(null);
            return;
        }
        if (mc.screen instanceof DialogueScreen ds && ds.entityId == p.entityId()) ds.setPage(p);
        else mc.setScreen(new DialogueScreen(p));
    }
}
