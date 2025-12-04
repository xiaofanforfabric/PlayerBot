package com.xiaofan.version;

import com.xiaofan.api.*;

/**
 * 1.20.1 版本的 Minecraft 版本实现
 * 这是主接口，提供所有子接口的访问
 */
public class MinecraftVersionImpl implements IMinecraftVersion {
    private final ILogger logger = new LoggerImpl();
    private final IPlayerProvider playerProvider = new PlayerProviderImpl();
    private final IWorldTimeProvider worldTimeProvider = new WorldTimeProviderImpl();
    private final IItemRegistry itemRegistry = new ItemRegistryImpl();
    private final ICommandExecutor commandExecutor = new CommandExecutorImpl();
    private final IPlayerStatusChecker playerStatusChecker = new PlayerStatusCheckerImpl();
    private final IBlockInteractor blockInteractor = new BlockInteractorImpl();
    private final IGameDirectoryProvider gameDirectoryProvider = new GameDirectoryProviderImpl();
    private final ITickHandler tickHandler = new TickHandlerImpl();
    private final IBaritoneExecutor baritoneExecutor = new BaritoneExecutorImpl();
    private final IKeyInputHandler keyInputHandler = new KeyInputHandlerImpl();
    private final IGuiRenderHandler guiRenderHandler = new GuiRenderHandlerImpl();
    
    @Override
    public int getMajorVersion() {
        return 1;
    }
    
    @Override
    public int getMinorVersion() {
        return 20;
    }
    
    @Override
    public int getPatchVersion() {
        return 1; // 1.20.1
    }
    
    @Override
    public String getVersionString() {
        return "1.20.1";
    }
    
    @Override
    public IPlayerProvider getPlayerProvider() {
        return playerProvider;
    }
    
    @Override
    public IWorldTimeProvider getWorldTimeProvider() {
        return worldTimeProvider;
    }
    
    @Override
    public IItemRegistry getItemRegistry() {
        return itemRegistry;
    }
    
    @Override
    public ICommandExecutor getCommandExecutor() {
        return commandExecutor;
    }
    
    @Override
    public IPlayerStatusChecker getPlayerStatusChecker() {
        return playerStatusChecker;
    }
    
    @Override
    public IBlockInteractor getBlockInteractor() {
        return blockInteractor;
    }
    
    @Override
    public IGameDirectoryProvider getGameDirectoryProvider() {
        return gameDirectoryProvider;
    }
    
    @Override
    public ILogger getLogger() {
        return logger;
    }
    
    @Override
    public ITickHandler getTickHandler() {
        return tickHandler;
    }
    
    @Override
    public IBaritoneExecutor getBaritoneExecutor() {
        return baritoneExecutor;
    }
    
    @Override
    public IKeyInputHandler getKeyInputHandler() {
        return keyInputHandler;
    }
    
    @Override
    public IGuiRenderHandler getGuiRenderHandler() {
        return guiRenderHandler;
    }
}

