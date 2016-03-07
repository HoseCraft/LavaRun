package com.infermc.lavarun;

import com.infermc.hosecraft.events.EventHandler;
import com.infermc.hosecraft.events.Listener;
import com.infermc.hosecraft.events.block.BlockBreakEvent;
import com.infermc.hosecraft.events.block.BlockPlaceEvent;
import com.infermc.hosecraft.events.player.PlayerMoveEvent;
import com.mojang.minecraft.level.tile.Block;

public class BlockHandler implements Listener {
    LavaRun parent;

    public BlockHandler(LavaRun p) {
        this.parent = p;
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent ev) {
        int block = ev.getBlock().id;
        System.out.println(ev.getLocation().getX()+" "+ev.getLocation().getY()+" "+ev.getLocation().getZ());
        if (block == 8 || block == 9 || block == 10 || block == 11) {
            ev.getPlayer().sendMessage("&cLiquid placement is disabled!");
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent ev) {
        System.out.println(ev.getTo().getY());
        ev.getTo().setBlock(Block.GOLD_BLOCK);
    }
}
