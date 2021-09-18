package net.galaxycore.galaxycoreproxy.utils;

import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused") // API Usage
public class PermissionUtils {

    public static boolean hasPermission(User user, String permission, GalaxyCoreProxy proxy) {
        return hasPermission(user, permission, true, proxy);
    }

    public static boolean hasPermission(User user, String permission, boolean value, GalaxyCoreProxy proxy) {
        return hasPermission(user.getUniqueId(), permission, value, proxy);
    }

    public static boolean hasPermission(UUID uuid, String permission, GalaxyCoreProxy proxy) {
        return hasPermission(uuid, permission, true, proxy);
    }

    public static boolean hasPermission(UUID uuid, String permission, boolean value, GalaxyCoreProxy proxy) {
        return Objects.requireNonNull(proxy.getLuckPermsAPI().getUserManager()
                .getUser(uuid)).getNodes().stream()
                .filter(node -> node.getValue() == value)
                .anyMatch(node -> node.getKey().equals(permission));
    }

    public static boolean hasPermission(Player player, String permission, GalaxyCoreProxy proxy) {
        return hasPermission(player, permission, true, proxy);
    }

    public static boolean hasPermission(Player player, String permission, boolean value, GalaxyCoreProxy proxy) {
        return hasPermission(player.getUniqueId(), permission, value, proxy);
    }

    public static void addPermission(User user, String permission, GalaxyCoreProxy proxy) {
        addPermission(user, permission, true, proxy);
    }

    public static void addPermission(User user, String permission, boolean value, GalaxyCoreProxy proxy) {
        addPermission(user.getUniqueId(), permission, value, proxy);
    }

    public static void addPermission(UUID uuid, String permission, GalaxyCoreProxy proxy) {
        addPermission(uuid, permission, true, proxy);
    }

    public static void addPermission(UUID uuid, String permission, boolean value, GalaxyCoreProxy proxy) {
        proxy.getLuckPermsAPI().getUserManager().modifyUser(uuid, user -> user.data().add(Node.builder(permission).value(value).build()));
    }

    public static void addPermission(Player player, String permission, GalaxyCoreProxy proxy) {
        addPermission(player, permission, true, proxy);
    }

    public static void addPermission(Player player, String permission, boolean value, GalaxyCoreProxy proxy) {
        addPermission(player.getUniqueId(), permission, value, proxy);
    }

    public static void setPermission(User user, String permission, boolean value, GalaxyCoreProxy proxy) {
        setPermission(user.getUniqueId(), permission, value, proxy);
    }

    public static void setPermission(UUID uuid, String permission, boolean value, GalaxyCoreProxy proxy) {
        Optional<Node> optionalNode = Objects.requireNonNull(proxy.getLuckPermsAPI().getUserManager().getUser(uuid))
                .getNodes().stream().filter(node1 -> node1.getKey().equals(permission)).findFirst();
        if(optionalNode.isEmpty()) {
            addPermission(uuid, permission, value, proxy);
            return;
        }
        Objects.requireNonNull(proxy.getLuckPermsAPI().getUserManager().getUser(uuid)).data().remove(optionalNode.get());
        Objects.requireNonNull(proxy.getLuckPermsAPI().getUserManager().getUser(uuid)).data().add(optionalNode.get().toBuilder().value(value).build());
        proxy.getLuckPermsAPI().getUserManager().saveUser(Objects.requireNonNull(proxy.getLuckPermsAPI().getUserManager().getUser(uuid)));
    }

    public static void setPermission(Player player, String permission, boolean value, GalaxyCoreProxy proxy) {
        setPermission(player.getUniqueId(), permission, value, proxy);
    }

    public static void removePermission(User user, String permission, GalaxyCoreProxy proxy) {
        removePermission(user.getUniqueId(), permission, proxy);
    }

    public static void removePermission(UUID uuid, String permission, GalaxyCoreProxy proxy) {
        Objects.requireNonNull(proxy.getLuckPermsAPI().getUserManager().getUser(uuid))
                .getNodes().stream().filter(node -> node.getKey().equals(permission))
                .forEach(node -> Objects.requireNonNull(proxy.getLuckPermsAPI().getUserManager().getUser(uuid)).data().remove(node));
        proxy.getLuckPermsAPI().getUserManager().saveUser(Objects.requireNonNull(proxy.getLuckPermsAPI().getUserManager().getUser(uuid)));
    }

    public static void removePermission(Player player, String permission, GalaxyCoreProxy proxy) {
        removePermission(player.getUniqueId(), permission, proxy);
    }

}
