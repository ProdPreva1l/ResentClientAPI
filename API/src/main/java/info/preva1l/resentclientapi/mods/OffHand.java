package info.preva1l.resentclientapi.mods;

import info.preva1l.resentclientapi.Actor;
import info.preva1l.resentclientapi.ResentMod;

import java.util.Arrays;

public abstract class OffHand extends ResentMod {

    protected OffHand() {
        super();
        dataRegistry.put("equipped", "{\"action\":\"equip_item\",\"item_id\":\"%item_id%\",\"enchanted\":%enchanted%}".getBytes());
        dataRegistry.put("unequipped", "{\"action\":\"un-equip_item\",\"item_id\":\"%item_id%\",\"enchanted\":%enchanted%}".getBytes());
    }

    public void sendOffhandEquipEvent(Actor actor, String itemId, boolean enchanted) {
        actor.sendPacket(this.getChannel(),
                Arrays.toString(this.getData("equipped"))
                        .replace("%item_id%", itemId)
                        .replace("%enchanted%", enchanted + "").getBytes());
    }

    public void sendOffhandUnEquipEvent(Actor actor, String itemId, boolean enchanted) {
        actor.sendPacket(this.getChannel(),
                Arrays.toString(this.getData("unequipped"))
                        .replace("%item_id%", itemId)
                        .replace("%enchanted%", enchanted + "").getBytes());
    }

    @Override
    public String getChannel() {
        return "resent:offhand";
    }
}
