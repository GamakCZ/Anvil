package cz.vixikhd.anvil.inventory;

import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerUIInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Position;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class AnvilInventory extends cn.nukkit.inventory.AnvilInventory {

    public AnvilInventory(PlayerUIInventory playerUI, Position position) {
        super(playerUI, position);
    }

    // copied from nukkit
    public boolean onRename(Player player, @NotNull Item resultItem, boolean sendToUI) {
        Item local = getItem(TARGET);
        Item second = getItem(SACRIFICE);

        if (!resultItem.equals(local, true, false) || resultItem.getCount() != local.getCount()) {
            //Item does not match target item. Everything must match except the tags.
            return false;
        }

        if (local.equals(resultItem)) {
            //just item transaction
            return true;
        }

        if (local.getId() != 0 && second.getId() == 0) { //only rename
            local.setCustomName(resultItem.getCustomName());
            setItem(RESULT, local);
            player.getInventory().addItem(local);
            clearAll();
            player.getInventory().sendContents(player);
            sendContents(player);

            player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_RANDOM_ANVIL_USE);
            return true;
        } else if (local.getId() != 0 && second.getId() != 0) { //enchants combining
            if (!local.equals(second, true, false)) {
                return false;
            }

            if (local.getId() != 0 && second.getId() != 0) {
                Item result = local.clone();
                int enchants = 0;

                ArrayList<Enchantment> enchantments = new ArrayList<>(Arrays.asList(second.getEnchantments()));

                ArrayList<Enchantment> baseEnchants = new ArrayList<>();

                for (Enchantment ench : local.getEnchantments()) {
                    if (ench.isMajor()) {
                        baseEnchants.add(ench);
                    }
                }

                for (Enchantment enchantment : enchantments) {
                    if (enchantment.getLevel() < 0 || enchantment.getId() < 0) {
                        continue;
                    }

                    if (enchantment.isMajor()) {
                        boolean same = false;
                        boolean another = false;

                        for (Enchantment baseEnchant : baseEnchants) {
                            if (baseEnchant.getId() == enchantment.getId())
                                same = true;
                            else {
                                another = true;
                            }
                        }

                        if (!same && another) {
                            continue;
                        }
                    }

                    Enchantment localEnchantment = local.getEnchantment(enchantment.getId());

                    if (localEnchantment != null) {
                        int level = Math.max(localEnchantment.getLevel(), enchantment.getLevel());

                        if (localEnchantment.getLevel() == enchantment.getLevel())
                            level++;

                        enchantment.setLevel(level);
                        result.addEnchantment(enchantment);
                        continue;
                    }

                    result.addEnchantment(enchantment);
                    enchants++;
                }

                if(!resultItem.getCustomName().equals("")) {
                    result.setCustomName(resultItem.getCustomName());
                }

                player.getInventory().addItem(result);
                player.getInventory().sendContents(player);

                clearAll();
                sendContents(player);

                player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_RANDOM_ANVIL_USE);
                return true;
            }
        }

        return false;
    }
}
