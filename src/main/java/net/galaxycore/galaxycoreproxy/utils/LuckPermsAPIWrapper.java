package net.galaxycore.galaxycoreproxy.utils;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.galaxycore.galaxycorecore.permissions.exceptions.MissingFieldException;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import javax.annotation.Nullable;
import java.util.OptionalInt;

@SuppressWarnings("unused") // API
@Getter
public class LuckPermsAPIWrapper {

    private final Player player;
    private final LuckPerms api;
    private final User user;
    private final Group primaryGroup;

    public LuckPermsAPIWrapper(Player player) {
        api = LuckPermsProvider.get();
        user = api.getUserManager().getUser(player.getUniqueId());

        assert user !=  null;
        primaryGroup = api.getGroupManager().getGroup(user.getPrimaryGroup());
        this.player = player;
    }

    public LuckPermsAPIWrapper() {
        player = null;
        api = null;
        user = null;
        primaryGroup = null;
    }

    public String getPermissionsPrefix() {
        @Nullable String prefix = primaryGroup.getCachedData().getMetaData().getPrefix();

        if(prefix != null)
            return prefix;
        else
            throw new MissingFieldException("Prefix");
    }

    public String getPermissionColor() {
        @Nullable String suffix = primaryGroup.getCachedData().getMetaData().getSuffix();

        if(suffix != null)
            return suffix;
        else
            throw new MissingFieldException("Suffix");
    }

    public String getPermissionDisplayName() {
        @Nullable String name = primaryGroup.getDisplayName();

        if(name != null)
            return name;
        else
            throw new MissingFieldException("DisplayName");
    }

    public int getPermissionsWeight() {
        OptionalInt weight = primaryGroup.getWeight();

        if(weight.isPresent())
            return weight.getAsInt();
        else
            throw new MissingFieldException("Weight");
    }

    public String getPermissionsGroupNameRaw() {
        return user.getPrimaryGroup();
    }

    public String getPlayerName() {
        return player.getUsername();
    }

    public boolean isChatImportant() {
        return player.hasPermission("core.chat.important");
    }

    public Player getPlayer() {
        return player;
    }

}
