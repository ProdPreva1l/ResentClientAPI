package prelude.adapter.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import prelude.adapter.BukkitPlayerAdapter;
import prelude.adapter.VersionAdapter;
import prelude.api.Prelude;
import prelude.api.mods.AnchorRenderer;
import prelude.api.mods.OffHand;
import prelude.api.mods.TotemTweaks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Adapter_1_16_5 implements VersionAdapter {
    private final JavaPlugin plugin;
    private final Map<Player, ItemStack> offhandItemMap = new HashMap<>();

    public Adapter_1_16_5(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerAnchorListener(AnchorRenderer anchorMod) {
        plugin.getServer().getPluginManager().registerEvents(new Adapter_1_16_5.AnchorListeners(), plugin);
    }

    @Override
    public void registerTotemListener(TotemTweaks totemMod) {
        plugin.getServer().getPluginManager().registerEvents(new Adapter_1_16_5.TotemListeners(), plugin);
    }

    @Override
    public Runnable getOffHandRunnable(OffHand offhandMod) {
        // This is my solution for monitoring offhand items
        // Cires was to track the ItemStacks client side, while this makes more sense,
        // I think we should take as much stress off the client as possible
        // if you come up with a better implementation please make a PR
        return () -> {
            if (!offhandMod.isAllowed() || !offhandMod.isOfficiallyHooked()) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack currentOffhand = player.getInventory().getItemInOffHand();
                if (!offhandItemMap.containsKey(player)) {
                    offhandItemMap.put(player, currentOffhand);
                    continue;
                }
                ItemStack cachedOffhand = offhandItemMap.get(player);
                if (cachedOffhand == currentOffhand) {
                    continue;
                }
                offhandItemMap.replace(player, currentOffhand);
                if (currentOffhand.getType().isAir()) {
                    offhandMod.sendOffhandUnEquipEvent(BukkitPlayerAdapter.getPreludePlayer(plugin, player),
                            Material.AIR.name(), false);
                } else {
                    offhandMod.sendOffhandEquipEvent(BukkitPlayerAdapter.getPreludePlayer(plugin, player),
                            currentOffhand.getType().name(), !currentOffhand.getEnchantments().isEmpty());
                }
            }
        };
    }

    public class TotemListeners implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onResurrectEvent(EntityResurrectEvent event) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                Optional<TotemTweaks> mod = Prelude.getInstance().getMod(TotemTweaks.class);
                if (!mod.isPresent() || !mod.get().isAllowed() || !mod.get().isOfficiallyHooked()) {
                    return;
                }
                mod.get().sendTotemPoppedEvent(BukkitPlayerAdapter.getPreludePlayer(plugin, player));
            }
        }
    }

    public class AnchorListeners implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onAnchorPlace(BlockPlaceEvent event) {
            if (event.getBlockPlaced().getType() != Material.RESPAWN_ANCHOR) {
                return;
            }

            Optional<AnchorRenderer> mod = Prelude.getInstance().getMod(AnchorRenderer.class);

            if (!mod.isPresent() || !mod.get().isAllowed() || !mod.get().isOfficiallyHooked()) {
                return;
            }

            int x = event.getBlockPlaced().getX();
            int y = event.getBlockPlaced().getY();
            int z = event.getBlockPlaced().getZ();

            mod.get().sendPlacedAnchorPacket(BukkitPlayerAdapter.getPreludePlayer(plugin, event.getPlayer()), x, y, z);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onAnchorInteract(PlayerInteractEvent event) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            if (event.getClickedBlock() == null
                    || event.getClickedBlock().getType() != Material.RESPAWN_ANCHOR) {
                return;
            }

            if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.GLOWSTONE) {
                return;
            }

            Optional<AnchorRenderer> mod = Prelude.getInstance().getMod(AnchorRenderer.class);

            if (!mod.isPresent() || !mod.get().isAllowed() || !mod.get().isOfficiallyHooked()) {
                return;
            }

            int x = event.getClickedBlock().getX();
            int y = event.getClickedBlock().getY();
            int z = event.getClickedBlock().getZ();

            int charges = ((RespawnAnchor) event.getClickedBlock().getBlockData()).getCharges();

            if (charges == 3) {
                mod.get().sendBlownUpAnchorPacket(BukkitPlayerAdapter.getPreludePlayer(plugin, event.getPlayer()), x, y, z);
                return;
            }

            mod.get().sendInteractedAnchorPacket(BukkitPlayerAdapter.getPreludePlayer(plugin, event.getPlayer()), x, y, z, charges + 1);
        }
    }

    @Override
    public boolean hasAnchorSupport() {
        return true;
    }

    @Override
    public boolean hasTotemSupport() {
        return true;
    }

    @Override
    public boolean hasOffHandSupport() {
        return true;
    }
}
