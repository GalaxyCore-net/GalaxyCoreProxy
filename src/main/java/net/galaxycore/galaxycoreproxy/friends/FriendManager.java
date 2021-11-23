package net.galaxycore.galaxycoreproxy.friends;

import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.DatabaseConfiguration;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.SQLUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class FriendManager {
    private final DatabaseConfiguration dbConfig;

    public FriendManager() {
        dbConfig = ProxyProvider.getProxy().getDatabaseConfiguration();
        SQLUtils.runScript(dbConfig, "friend", "initialize");
    }

    public boolean addFriend(PlayerLoader first, PlayerLoader second) {
        if (getFriends(first).contains(second) || getFriends(second).contains(first))
            return false;

        try {
            PreparedStatement addFriendStatement = dbConfig.getConnection().prepareStatement("INSERT INTO core_friends VALUES (?, ?), (?, ?)");
            addFriendStatement.setInt(1, first.getId());
            addFriendStatement.setInt(2, second.getId());
            addFriendStatement.setInt(3, second.getId());
            addFriendStatement.setInt(4, first.getId());

            addFriendStatement.executeUpdate();
            addFriendStatement.close();
        } catch (SQLException exception) {
            ProxyProvider.getProxy().getLogger().error("Friend Add failed: ", exception);
            return false;
        }
        return true;
    }

    @SneakyThrows
    public boolean removeFriend(PlayerLoader first, PlayerLoader second) {
        try {
            PreparedStatement removeFriendStatement = dbConfig.getConnection().prepareStatement("DELETE FROM core_friends WHERE (player_id=? and other_player_id=?) or (player_id=? and other_player_id=?)");
            removeFriendStatement.setInt(1, first.getId());
            removeFriendStatement.setInt(2, second.getId());
            removeFriendStatement.setInt(3, second.getId());
            removeFriendStatement.setInt(4, first.getId());

            removeFriendStatement.executeUpdate();
            removeFriendStatement.close();
        } catch (SQLException exception) {
            ProxyProvider.getProxy().getLogger().error("Friend Remove failed: ", exception);
            return false;
        }
        return true;
    }

    public ImmutableList<PlayerLoader> getFriends(PlayerLoader loader) {
        ImmutableList.Builder<PlayerLoader> listBuilder = ImmutableList.builder();

        try {
            PreparedStatement getFriendsStatement = dbConfig.getConnection().prepareStatement("SELECT other_player_id FROM core_friends WHERE player_id=?");
            getFriendsStatement.setInt(1, loader.getId());

            ResultSet getFriendResult = getFriendsStatement.executeQuery();

            while (getFriendResult.next()) {
                listBuilder.add(Objects.requireNonNull(PlayerLoader.buildLoader(getFriendResult.getInt("other_player_id"))));
            }

            getFriendResult.close();
            getFriendsStatement.close();
        } catch (SQLException exception) {
            ProxyProvider.getProxy().getLogger().error("Friend Load failed: ", exception);
        }

        return listBuilder.build();
    }
}
