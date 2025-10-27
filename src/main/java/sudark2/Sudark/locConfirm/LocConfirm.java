package sudark2.Sudark.locConfirm;

import io.papermc.paper.event.player.PlayerPickBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LocConfirm extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerCheckListener(), this);
    }

    static Location CL;
    static List<Location> struct = new ArrayList<>();

    private class PlayerCheckListener implements org.bukkit.event.Listener {
        @EventHandler
        public void onPlayerCheck(PlayerPickBlockEvent event) {
            Player pl = event.getPlayer();
            ItemStack item = pl.getItemInHand();
            Location tarLoc = pl.getTargetBlockExact(5).getLocation();
            if (item.getType().equals(Material.AIR)) {
                CL = tarLoc;
                pl.sendActionBar("§e" + CL.getBlockX() + " §f, §e" + CL.getBlockY() + " §f, §e" + CL.getBlockZ());
                return;
            }
            pl.sendActionBar("§e" + (CL.getBlockX() - tarLoc.getBlockX())
                    + " §f, §e" + (tarLoc.getBlockY() - CL.getBlockY())
                    + " §f, §e" + (CL.getBlockZ() - tarLoc.getBlockZ()));
        }
    }

    @EventHandler
    public void onPlayerDropItem(BlockBreakEvent event) {
        Player pl = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        ItemStack itemInHand = pl.getItemInHand();

        if (itemInHand != null && itemInHand.getType() == Material.AIR) {
            if (struct.size() > 1) {
                transform(struct.getFirst(), loc);
                return;
            }
            struct.add(loc);
        }
    }

    public void transform(Location locA, Location locB) {
        struct.clear();
        Location maxLoc, minLoc;
        World world = locA.getWorld();
        if (locA.getBlockX() > locB.getBlockX()) {
            maxLoc = locA;
            minLoc = locB;
        } else {
            maxLoc = locB;
            minLoc = locA;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Effect[] newCover = {");

        for (int x = minLoc.getBlockX(); x <= maxLoc.getBlockX(); x++)
            for (int y = minLoc.getBlockY(); y <= maxLoc.getBlockY(); y++)
                for (int z = minLoc.getBlockZ(); z <= maxLoc.getBlockZ(); z++) {
                    Block start = world.getBlockAt(x, y, z);
                    Material type = start.getType();
                    if (type == Material.AIR) continue;

                    if (start.getType() == Material.AIR) continue;

                    int dx = 1, dy = 1, dz = 1;

                    boolean canExpand = true;
                    while (canExpand) {
                        // 尝试优先沿 X 扩展
                        boolean expandX = true;
                        for (int yy = y; yy < y + dy && expandX; yy++)
                            for (int zz = z; zz < z + dz && expandX; zz++)
                                if (world.getBlockAt(x + dx, yy, zz).getType() != type)
                                    expandX = false;

                        if (expandX && x + dx <= maxLoc.getBlockX()) {
                            dx++;
                            continue;
                        }

                        // 尝试沿 Z 扩展
                        boolean expandZ = true;
                        for (int xx = x; xx < x + dx && expandZ; xx++)
                            for (int yy = y; yy < y + dy && expandZ; yy++)
                                if (world.getBlockAt(xx, yy, z + dz).getType() != type)
                                    expandZ = false;

                        if (expandZ && z + dz <= maxLoc.getBlockZ()) {
                            dz++;
                            continue;
                        }

                        // 尝试沿 Y 扩展
                        boolean expandY = true;
                        for (int xx = x; xx < x + dx && expandY; xx++)
                            for (int zz = z; zz < z + dz && expandY; zz++)
                                if (world.getBlockAt(xx, y + dy, zz).getType() != type)
                                    expandY = false;

                        if (expandY && y + dy <= maxLoc.getBlockY()) {
                            dy++;
                            continue;
                        }

                        canExpand = false;
                    }

                    sb.append("new Effect(Material." + type.name() + ",HalfP(LocHP( tx" + x + ",ty" + y + ",tz" + z + ")," + dx + ", " + dy + ", " + dz + ")), ");

                    // ====== 确认完整长方体并清空 ======
                    for (int xx = x; xx < x + dx; xx++)
                        for (int yy = y; yy < y + dy; yy++)
                            for (int zz = z; zz < z + dz; zz++)
                                world.getBlockAt(xx, yy, zz).setType(Material.AIR);
                }
        sb.append("};");
        System.out.println(sb);
    }
}
