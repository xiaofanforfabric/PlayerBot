package com.xiaofan.fabric;

import com.xiaofan.fabriclike.ExampleModFabricLike;
import net.fabricmc.api.ModInitializer;

public class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExampleModFabricLike.init();
    }
}
