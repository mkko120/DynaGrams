package studio.mkko120.dynagrams.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import studio.mkko120.dynagrams.DynaGrams;
import studio.mkko120.dynagrams.db.HologramDBO;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Command class for holograms
 * @since 1.0.0
 * @author mkko120
 */
@CommandAlias("hologram|dynagram|dg")
@CommandPermission("dynagrams.use")
public class HologramsCommand extends BaseCommand {

    private final DynaGrams instance;
    private final Logger logger;

    public HologramsCommand(DynaGrams instance, Logger logger) {
        this.instance = instance;
        this.logger = logger;
    }

    /**
     * Default command, shows the issuer command help
     * @param sender the command issuer
     * @param help the command help
     */
    @Default
    @HelpCommand
    public void onDefault(CommandIssuer sender, @NotNull CommandHelp help) {
        help.showHelp(sender);
    }

    /**
     * Create a hologram
     * @param sender the command issuer
     * @param name the name of the hologram
     */
    @Subcommand("create")
    @CommandPermission("dynagrams.create")
    public void createHologram(CommandSender sender, @Single String name) {
        if (name == null) {
            sender.sendMessage(
                Component.text()
                    .content("Please provide a name for the hologram.")
                    .color(NamedTextColor.DARK_RED)
            );
            return;
        }
        if (name.length() > 255) {
            sender.sendMessage(
                Component.text()
                    .content("The name of the hologram is too long.")
                    .color(NamedTextColor.DARK_RED)
            );
        }
        if (name.length() < 3) {
            sender.sendMessage(
                Component.text()
                    .content("The name of the hologram is too short.")
                    .color(NamedTextColor.DARK_RED)
            );
        }

        int id = instance.getHologramManager().createHologram(name);

        if (id > 0) {
            sender.sendMessage(
                Component.text("Successfully created hologram: ", NamedTextColor.GREEN)
                    .append(
                            Component.text(name, Style.style(TextDecoration.BOLD, NamedTextColor.AQUA))
                    )
                    .append(Component.text(" with ID: ", NamedTextColor.GREEN))
                    .append(Component.text(id, NamedTextColor.AQUA))
            );
        } else if (id == 0) {
            sender.sendMessage(
                Component.text("Failed to create hologram: ", NamedTextColor.DARK_RED)
                    .append(Component.text(name, Style.style(TextDecoration.BOLD, NamedTextColor.RED)))
                    .append(Component.text(" (already exists?)", NamedTextColor.DARK_RED))
            );
        } else {
            sender.sendMessage(
                Component.text("Failed to create hologram: ", NamedTextColor.DARK_RED)
                    .append(Component.text(name, Style.style(TextDecoration.BOLD, NamedTextColor.RED)))
            );
        }

    }

    /**
     * Delete a hologram
     * @param sender the command issuer
     * @param name the name of the hologram
     */
    @Subcommand("delete")
    @CommandCompletion("@holograms")
    @CommandPermission("dynagrams.delete")
    public void deleteHologram(CommandSender sender, @Values("@holograms") @Single String name) {
        if (name == null) {
            sender.sendMessage(
                    Component.text()
                            .content("Please provide a name for the hologram.")
                            .color(NamedTextColor.DARK_RED)
            );
            return;
        }
        if (instance.getHologramManager().deleteHologram(name)) {
            sender.sendMessage(
                    Component.text("Successfully deleted hologram: ", NamedTextColor.GREEN)
                            .append(
                                    Component.text(name, Style.style(TextDecoration.BOLD, NamedTextColor.AQUA))
                            )
            );
        } else {
            sender.sendMessage(
                    Component.text("Failed to delete hologram: ", NamedTextColor.DARK_RED)
                            .append(Component.text(name, Style.style(TextDecoration.BOLD, NamedTextColor.RED)))
            );
        }
    }

    /**
     * Set a hologram's line
     * @param sender the command issuer
     * @param name the name of the hologram
     * @param line the line number to modify
     * @param text the new text for the line
     */
    @Subcommand("setline")
    @CommandCompletion("@holograms")
    @CommandPermission("dynagrams.modify")
    public void setLine(CommandSender sender, @Values("@holograms") String name, int line, String text) {


    }

    /**
     * Add a line to a hologram
     * @param sender the command issuer
     * @param name the name of the hologram
     * @param text the text to add
     */
    @Subcommand("addline")
    @CommandCompletion("@holograms")
    @CommandPermission("dynagrams.modify")
    public void addLine(CommandSender sender, @Values("@holograms") String name, String text) {
        logger.info("Adding line to hologram with name: " + name);
    }

    /**
     * List all holograms that are placed on the server or all holograms in the database
     * @param sender the command issuer
     */
    @Subcommand("list")
    @CommandPermission("dynagrams.list")
    public void listHolograms(
            CommandSender sender,
            @Default("false") boolean all
    ) {
        if (all) {
            sender.sendMessage(Component.text("Listing all holograms in the database", NamedTextColor.GREEN));
            HashMap<Integer, HologramDBO> holograms = instance.getHologramManager().getAllHolograms();
            if (holograms == null) {
                sender.sendMessage(Component.text("Failed to list holograms", NamedTextColor.DARK_RED));
                return;
            }
            holograms.forEach((id, dbo) -> sender.sendMessage(
                    Component.text("ID: ", NamedTextColor.AQUA)
                            .append(Component.text(id, NamedTextColor.GREEN))
                            .append(Component.text(", Name: ", NamedTextColor.AQUA))
                            .append(Component.text(dbo.getName(), NamedTextColor.GREEN))
            ));
            sender.sendMessage(
                    Component.text("Total: ", NamedTextColor.AQUA)
                            .append(Component.text(holograms.size(), NamedTextColor.GREEN))
            );
        } else {
            logger.info("Listing only holograms that are placed on the server");
        }
    }

    /**
     * Places a hologram at player's location
     * @param player the command issuer
     * @param id the ID of the hologram to place
     */
    @Subcommand("place")
    @CommandCompletion("@holograms")
    @CommandPermission("dynagrams.place")
    public void placeHologram(Player player, String name) {
        if (name == null) {
            player.sendMessage(
                    Component.text()
                            .content("Please provide a name of the hologram.")
                            .color(NamedTextColor.DARK_RED)
            );
            return;
        }
        if (instance.getHologramManager().placeHologram(player, name)) {
            player.sendMessage(
                    Component.text("Successfully placed hologram: ", NamedTextColor.GREEN)
                            .append(
                                    Component.text(name, Style.style(TextDecoration.BOLD, NamedTextColor.AQUA))
                            )
            );
        } else {
            player.sendMessage(
                    Component.text("Failed to place hologram: ", NamedTextColor.DARK_RED)
                            .append(Component.text(name, Style.style(TextDecoration.BOLD, NamedTextColor.RED)))
            );

        }

    }

    @Subcommand("unplace")
    @CommandCompletion("@holograms")
    @CommandPermission("dynagrams.unplace")
    public void unplaceHologram(Player player, @Values("@holograms") @Single String name) {
        if (name == null) {
            player.sendMessage(
                    Component.text()
                            .content("Please provide a name of the hologram.")
                            .color(NamedTextColor.DARK_RED)
            );
            return;
        }
        if (instance.getHologramManager().unplaceHologram(name)) {
            player.sendMessage(
                    Component.text("Successfully unplaced hologram: ", NamedTextColor.GREEN)
                            .append(
                                    Component.text(name, Style.style(TextDecoration.BOLD, NamedTextColor.AQUA))
                            )
            );
        } else {
            player.sendMessage(
                    Component.text("Failed to unplace hologram: ", NamedTextColor.DARK_RED)
                            .append(Component.text(name, Style.style(TextDecoration.BOLD, NamedTextColor.RED)))
            );
        }
    }

}
