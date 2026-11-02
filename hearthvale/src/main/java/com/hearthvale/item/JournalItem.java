package com.hearthvale.item;

import com.hearthvale.dialogue.NpcInteractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Prints the social tab: hearts, status and birthdays for everyone you've met. */
public class JournalItem extends Item {
    public JournalItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) NpcInteractions.journal(sp);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
