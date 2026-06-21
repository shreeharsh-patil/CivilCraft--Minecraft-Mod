package com.civilcraftai.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import java.util.List;

public class CoinItem extends Item {
    public CoinItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("§7The main currency of CivilCraft AI."));
        tooltip.add(Text.literal("§eCan be deposited into a Town treasury."));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
