package dev.lorenzzz.lobby.ui;

import com.cryptomorin.xseries.XMaterial;
import net.j4c0b3y.api.menu.button.Button;
import net.j4c0b3y.api.menu.button.ButtonClick;
import org.bukkit.inventory.ItemStack;

public class BorderButton extends Button {
    private static final ItemStack CACHED_ICON;

    static {
        ItemStack parsed = XMaterial.BLACK_STAINED_GLASS_PANE.parseItem();
        CACHED_ICON = parsed != null ? parsed : new ItemStack(org.bukkit.Material.AIR);
    }

    @Override
    public ItemStack getIcon(){
        return CACHED_ICON;
    }

    @Override
    public void onClick(ButtonClick click){
        // root
    }
}
