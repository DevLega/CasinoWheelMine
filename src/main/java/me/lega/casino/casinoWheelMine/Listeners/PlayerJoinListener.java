package me.lega.casino.casinoWheelMine.Listeners;

import me.lega.casino.casinoWheelMine.Database.WheelDatabase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class PlayerJoinListener implements Listener {
    private WheelDatabase wheelDatabase;

    public PlayerJoinListener(WheelDatabase wheelDatabase) {
        this.wheelDatabase = wheelDatabase;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws SQLException {
        if(wheelDatabase.playerExists(event.getPlayer().getUniqueId().toString())) return;
        wheelDatabase.addPlayer(event.getPlayer().getUniqueId().toString(), 0);
    }
}
