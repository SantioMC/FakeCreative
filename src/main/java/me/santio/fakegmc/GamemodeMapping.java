package me.santio.fakegmc;

import org.bukkit.GameMode;

@SuppressWarnings("MissingJavadoc")
public enum GamemodeMapping {
    SURVIVAL(0),
    CREATIVE(1),
    ADVENTURE(2),
    SPECTATOR(3);
    
    private final float id;
    
    GamemodeMapping(float id) {
        this.id = id;
    }
    
    public float id() {
        return id;
    }
    
    public static GamemodeMapping fromBukkit(GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> SURVIVAL;
            case CREATIVE -> CREATIVE;
            case ADVENTURE -> ADVENTURE;
            case SPECTATOR -> SPECTATOR;
        };
    }
}
