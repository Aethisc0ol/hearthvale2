package com.hearthvale.data;

import com.google.gson.JsonObject;
import com.hearthvale.util.GameTime;
import com.hearthvale.util.Ids;
import com.hearthvale.util.Json;
import com.hearthvale.world.PlayerData;
import com.hearthvale.world.Relationship;
import net.minecraft.server.level.ServerLevel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Optional restrictions shared by greetings and events. All keys are optional and read flat from the
 * same JSON object:
 *   min_hearts, max_hearts, status ["friend","dating","engaged","married"],
 *   requires_event ["event_id" | "npc_ns:npc#event_id"], requires_flag [..], forbids_flag [..],
 *   time "morning|afternoon|evening|night|H:MM-H:MM", weather "rain|clear|storm",
 *   location "<building type>", season "spring|summer|fall|winter",
 *   weekday ["mon", "sat", ...], birthday true/false
 */
public final class Conditions {
    public static final Conditions NONE = new Conditions();

    public record Ctx(ServerLevel level, NpcDefinition def, PlayerData data, Relationship rel, String buildingType) {}

    private int minHearts = 0;
    private int maxHearts = Integer.MAX_VALUE;
    private Set<String> status = Set.of();
    private List<String> requiresEvent = List.of();
    private List<String> requiresFlag = List.of();
    private List<String> forbidsFlag = List.of();
    private int timeFrom = -1, timeTo = -1;
    private String weather, location, season;
    private Set<Integer> weekdays = Set.of();
    private Boolean birthday;

    public static Conditions parse(JsonObject o) {
        Conditions c = new Conditions();
        c.minHearts = Json.integer(o, "min_hearts", 0);
        c.maxHearts = Json.integer(o, "max_hearts", Integer.MAX_VALUE);
        Set<String> st = new HashSet<>();
        for (String s : Json.strings(o, "status")) st.add(s.toLowerCase());
        c.status = st;
        c.requiresEvent = Json.strings(o, "requires_event");
        c.requiresFlag = Json.strings(o, "requires_flag");
        c.forbidsFlag = Json.strings(o, "forbids_flag");
        String time = Json.str(o, "time", null);
        if (time != null) parseTime(c, time.toLowerCase().trim());
        c.weather = Json.str(o, "weather", null);
        String loc = Json.str(o, "location", null);
        c.location = loc == null ? null : Ids.norm(loc);
        c.season = Json.str(o, "season", null);
        Set<Integer> wd = new HashSet<>();
        for (String s : Json.strings(o, "weekday")) wd.add(GameTime.weekdayIndex(s));
        c.weekdays = wd;
        if (o.has("birthday")) c.birthday = Json.bool(o, "birthday", false);
        return c;
    }

    private static void parseTime(Conditions c, String t) {
        switch (t) {
            case "morning" -> { c.timeFrom = 0; c.timeTo = 6000; }
            case "afternoon" -> { c.timeFrom = 6000; c.timeTo = 12000; }
            case "evening" -> { c.timeFrom = 12000; c.timeTo = 16000; }
            case "night" -> { c.timeFrom = 16000; c.timeTo = 0; }
            case "day" -> { c.timeFrom = 0; c.timeTo = 12000; }
            default -> {
                String[] p = t.split("-");
                c.timeFrom = GameTime.parseClock(p[0]);
                c.timeTo = GameTime.parseClock(p[1]);
            }
        }
    }

    public boolean test(Ctx c) {
        int hearts = c.rel().hearts();
        if (hearts < minHearts || hearts > maxHearts) return false;
        if (!status.isEmpty() && !status.contains(c.rel().status.id())) return false;
        for (String token : requiresEvent) {
            String npcId = c.def().id;
            String ev = token;
            int h = token.indexOf('#');
            if (h >= 0) {
                npcId = Ids.norm(token.substring(0, h));
                ev = token.substring(h + 1);
            }
            Relationship r = c.data().relationships.get(npcId);
            if (r == null || !r.events.contains(ev)) return false;
        }
        for (String f : requiresFlag) if (!c.data().flags.contains(f)) return false;
        for (String f : forbidsFlag) if (c.data().flags.contains(f)) return false;
        ServerLevel level = c.level();
        if (timeFrom >= 0 && !GameTime.inRange(GameTime.tick(level), timeFrom, timeTo)) return false;
        if (weather != null) {
            boolean ok = switch (weather.toLowerCase()) {
                case "rain" -> level.isRaining();
                case "storm" -> level.isThundering();
                case "clear" -> !level.isRaining();
                default -> true;
            };
            if (!ok) return false;
        }
        if (location != null && !location.equals(c.buildingType())) return false;
        if (season != null && GameTime.season(level) != GameTime.seasonIndex(season)) return false;
        if (!weekdays.isEmpty() && !weekdays.contains(GameTime.weekday(level))) return false;
        if (birthday != null && c.def().isBirthday(level) != birthday) return false;
        return true;
    }

    /** More restrictive conditions win when several greetings match. */
    public int specificity() {
        int s = 0;
        if (minHearts > 0 || maxHearts != Integer.MAX_VALUE) s++;
        if (!status.isEmpty()) s += 2;
        if (!requiresEvent.isEmpty() || !requiresFlag.isEmpty() || !forbidsFlag.isEmpty()) s++;
        if (timeFrom >= 0) s++;
        if (weather != null) s += 2;
        if (location != null) s += 2;
        if (season != null) s++;
        if (!weekdays.isEmpty()) s++;
        if (birthday != null) s += 5;
        return s;
    }
}
