package com.hearthvale.dialogue;

import com.hearthvale.Hearthvale;
import com.hearthvale.data.EffectContext;
import com.hearthvale.data.NpcDefinition;
import com.hearthvale.data.Script;
import com.hearthvale.data.ScriptEffect;
import com.hearthvale.entity.NpcEntity;
import com.hearthvale.network.DialoguePagePayload;
import com.hearthvale.world.Village;
import com.hearthvale.world.WorldState;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Server-side state machine for one conversation. The client only ever renders the page it was sent and
 * reports "advance" or "chose option N", so scripts and their effects can't be tampered with.
 */
public class DialogueSession {
    private static final class Frame {
        final List<Script.Line> lines;
        int idx = 0;
        Frame(List<Script.Line> lines) { this.lines = lines; }
    }

    final ServerPlayer player;
    final NpcEntity npc;
    private final NpcDefinition def;
    private final Deque<Frame> stack = new ArrayDeque<>();
    private final Runnable onFinish;
    private Script.Line current;
    private boolean ended;

    DialogueSession(ServerPlayer player, NpcEntity npc, Script script, Runnable onFinish) {
        this.player = player;
        this.npc = npc;
        this.def = npc.def();
        this.onFinish = onFinish;
        stack.push(new Frame(script.lines()));
    }

    /** Advance to the next line that has text, applying effects of any lines passed on the way. */
    void step() {
        current = null;
        while (!ended) {
            if (stack.isEmpty()) {
                finish(true);
                return;
            }
            Frame f = stack.peek();
            if (f.idx >= f.lines.size()) {
                stack.pop();
                continue;
            }
            Script.Line line = f.lines.get(f.idx++);
            apply(line.effects());
            if (line.text() == null || line.text().isEmpty()) continue;
            current = line;
            send(line);
            return;
        }
    }

    void respond(int choice) {
        if (ended) return;
        if (choice == -2) {
            cancel();
            return;
        }
        if (current == null) return;
        if (!current.choices().isEmpty()) {
            if (choice < 0 || choice >= current.choices().size()) return;
            Script.Choice c = current.choices().get(choice);
            apply(c.effects());
            if (!c.reply().lines().isEmpty()) stack.push(new Frame(c.reply().lines()));
            step();
        } else if (choice == -1) {
            step();
        }
    }

    void cancel() { finish(false); }

    private void finish(boolean completed) {
        if (ended) return;
        ended = true;
        DialogueManager.remove(this);
        npc.endTalk();
        PacketDistributor.sendToPlayer(player, new DialoguePagePayload(npc.getId(), "", "", "", List.of(), 0));
        if (completed && onFinish != null) {
            try {
                onFinish.run();
            } catch (Exception e) {
                Hearthvale.LOGGER.error("Dialogue completion callback failed", e);
            }
        }
    }

    private void apply(List<ScriptEffect> effects) {
        if (effects.isEmpty() || def == null) return;
        EffectContext ctx = new EffectContext(player, npc, def);
        for (ScriptEffect e : effects) {
            try {
                e.apply(ctx);
            } catch (Exception ex) {
                Hearthvale.LOGGER.error("Dialogue effect failed", ex);
            }
        }
    }

    private void send(Script.Line line) {
        List<String> choices = new ArrayList<>();
        for (Script.Choice c : line.choices()) choices.add(fmt(c.text()));
        String speaker = line.speaker() != null ? line.speaker() : (def != null ? def.name : "???");
        int points = def == null ? 0 : WorldState.get(player.serverLevel()).player(player.getUUID()).peek(def.id).points;
        PacketDistributor.sendToPlayer(player, new DialoguePagePayload(npc.getId(), npc.getNpcId(), speaker, fmt(line.text()), choices, points));
    }

    private String fmt(String s) {
        Village v = npc.village();
        return s.replace("{player}", player.getGameProfile().getName())
                .replace("{npc}", def == null ? "" : def.name)
                .replace("{village}", v == null ? "the village" : v.name);
    }
}
