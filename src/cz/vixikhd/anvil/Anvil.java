package cz.vixikhd.anvil;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.PluginBase;
import cz.vixikhd.anvil.inventory.AnvilInventory;

import java.util.HashMap;

public class Anvil extends PluginBase implements Listener {
    
    public HashMap<String, AnvilInventory> anvils = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getBlock().getId() == Block.ANVIL)
            anvils.put(event.getPlayer().getName(), new AnvilInventory(event.getPlayer().getUIInventory(), event.getBlock()));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        anvils.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onReceive(DataPacketReceiveEvent event) {
        if(!(event.getPacket() instanceof InventoryTransactionPacket)) {
            return;
        }

        Player player = event.getPlayer();

        if(!anvils.containsKey(player.getName())) {
            return;
        }

        InventoryTransactionPacket packet = (InventoryTransactionPacket) event.getPacket();
        AnvilInventory inventory = anvils.get(event.getPlayer().getName());

        boolean isFromInv = false;

        for(NetworkInventoryAction action : packet.actions) {
            if(action.windowId == 0 && !isFromInv) {
                isFromInv = true;
            }

            if(isFromInv && action.windowId == 124 && action.inventorySlot > 0 && action.inventorySlot < 3) {
                inventory.setItem(action.inventorySlot - 1, action.newItem);
                player.getInventory().removeItem(action.newItem);
            }

            if(action.windowId == -12 && action.inventorySlot == 2 && action.newItem.getId() == 0) {
                if(inventory.onRename(event.getPlayer(), action.oldItem, false)) {
                    for(Item item : inventory.getContents().values()) {
                        player.getUIInventory().removeItem(item);
                    }
                }
            }
        }

        System.out.println("konec akce");
    }
}
