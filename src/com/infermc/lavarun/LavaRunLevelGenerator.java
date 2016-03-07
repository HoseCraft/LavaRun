package com.infermc.lavarun;

import java.util.logging.Logger;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.generator.LevelGenerator;
import com.mojang.minecraft.level.tile.Block;

public final class LavaRunLevelGenerator extends LevelGenerator {
    private Logger log;
    private int width;
    private int depth;
    private int height;
    private byte[] blocks;
    private int waterLevel;

    public LavaRunLevelGenerator(Logger var1) {
        log = var1;
    }

    /**
     * Generates a level
     *
     * @param creator
     * @param width
     * @param depth
     * @param height Seems to be unused.
     * @return
     */
    public final Level generate(String creator, int width, int depth, int height) {
        this.width = width;
        this.depth = depth;
        this.height = 64;
        waterLevel = 32;
        blocks = new byte[width * depth << 6];

        int z; //var 26
        int x; // var24
        int y; // var11
        int block_hash;
        int blok = 0;

        // Width
        for (x = 0; x < this.width; ++x) {
            if (x % 4 == 0) {
                this.setProgress(x * 100 / (this.width - 1));
            }
            // Depth
            for (y = 0; y < this.depth; ++y) {
                // Height
                for (z = 0; z < this.height; ++z) {
                    block_hash = (z * this.depth + y) * this.width + x;

                    if (z <= this.waterLevel-1) {
                        blok = Block.DIRT.id;
                    }
                    this.blocks[block_hash] = (byte) blok;
                    blok = 0; // Reset it.
                }
            }
        }

        Level level = new Level();
        level.waterLevel = waterLevel;
        level.setData(width, 64, depth, blocks);
        level.createTime = System.currentTimeMillis();
        level.creator = creator;
        level.name = "LavaRun";

        return level;
    }
    private void setProgress(int percentage) {
        if (percentage % 10 == 0) log.info(percentage+"%");
    }
}