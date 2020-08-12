package griglog.thaumtweaks.mixins.events;

import griglog.thaumtweaks.SF;
import griglog.thaumtweaks.events.EventHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLootBonus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.lib.events.ToolEvents;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.lib.utils.Utils;

import java.util.HashMap;
import java.util.List;

@Mixin(ToolEvents.class)
public class ToolEventsMixin {

    @Inject(method = "harvestBlockEvent", at=@At("HEAD"), cancellable = true, remap = false)
    private static void harvestBlockEvent(final BlockEvent.HarvestDropsEvent event, CallbackInfo ci) {
        dropEarths(event);

        if (!event.getWorld().isRemote && event.getHarvester() != null) {
            ItemStack heldItem = event.getHarvester().getHeldItem(event.getHarvester().getActiveHand());
            if (!heldItem.isEmpty()) {
                List<EnumInfusionEnchantment> list = EnumInfusionEnchantment.getInfusionEnchantments(heldItem);
                if (event.isSilkTouching() || ForgeHooks.isToolEffective(event.getWorld(), event.getPos(), heldItem) || heldItem.getItem() instanceof ItemTool && ((ItemTool)heldItem.getItem()).getDestroySpeed(heldItem, event.getState()) > 1.0F) {
                    int xx;
                    if (list.contains(EnumInfusionEnchantment.REFINING))
                        doRefining(event, heldItem);

                    if (list.contains(EnumInfusionEnchantment.DESTRUCTIVE) && !blockDestructiveRecursion && !event.getHarvester().isSneaking())
                        doDestructive(event, heldItem);

                    if (list.contains(EnumInfusionEnchantment.COLLECTOR) && !event.getHarvester().isSneaking()) {
                        InventoryUtils.dropHarvestsAtPos(event.getWorld(), event.getPos(), event.getDrops(), true, 10, event.getHarvester());
                        event.getDrops().clear();
                    }

                    if (list.contains(EnumInfusionEnchantment.LAMPLIGHT) && !event.getHarvester().isSneaking() && event.getHarvester() instanceof EntityPlayerMP) {
                        IThreadListener mainThread = ((EntityPlayerMP)event.getHarvester()).getServerWorld();
                        Runnable r = new EventHelper.GlimmRunnable(event);
                        mainThread.addScheduledTask(r);
                    }
                }
            }
        }
    ci.cancel();
    }

    private static void dropEarths(BlockEvent.HarvestDropsEvent event) {
        Block block = event.getState().getBlock();
        double r = event.getWorld().rand.nextDouble();
        double mult = 1;
        if (event.getHarvester() != null) {
            ItemStack heldItem = event.getHarvester().getHeldItem(event.getHarvester().getActiveHand());
            int fort = EnchantmentHelper.getEnchantmentLevel(Enchantment.getEnchantmentByLocation("fortune"), heldItem);
            mult = 1D / (fort + 2) + (fort + 1) / 2D;
        }
        if (!event.getWorld().isRemote && !event.isSilkTouching() &&
                (block == Blocks.DIAMOND_ORE && r < 0.05D * mult ||
                block == Blocks.EMERALD_ORE && r < 0.075D * mult ||
                block == Blocks.LAPIS_ORE && r < 0.01D * mult ||
                block == Blocks.COAL_ORE && r < 0.001D * mult ||
                block == Blocks.LIT_REDSTONE_ORE && r < 0.01D * mult ||
                block == Blocks.REDSTONE_ORE && r < 0.01D * mult ||
                block == Blocks.QUARTZ_ORE && r < 0.01D * mult ||
                block == BlocksTC.oreAmber && r < 0.05D * mult ||
                block == BlocksTC.oreQuartz && r < 0.05D * mult)) {
            event.getDrops().add(new ItemStack(ItemsTC.nuggets, 1, 10));
        }
    }

    private static void doDestructive(BlockEvent.HarvestDropsEvent event, ItemStack heldItem) {
        int xx;
        blockDestructiveRecursion = true;
        EnumFacing face = (EnumFacing)lastFaceClicked.get(event.getHarvester().getEntityId());
        if (face == null) {
            face = EnumFacing.getDirectionFromEntityLiving(event.getPos(), event.getHarvester());
        }

        int aa = -1;

        while(true) {
            if (aa > 1) {
                blockDestructiveRecursion = false;
                break;
            }

            for(int bb = -1; bb <= 1; ++bb) {
                if (aa != 0 || bb != 0) {
                    xx = 0;
                    int yy = 0;
                    int zz = 0;
                    if (face.ordinal() <= 1) {
                        xx = aa;
                        zz = bb;
                    } else if (face.ordinal() <= 3) {
                        xx = aa;
                        yy = bb;
                    } else {
                        zz = aa;
                        yy = bb;
                    }

                    IBlockState bl = event.getWorld().getBlockState(event.getPos().add(xx, yy, zz));
                    if (bl.getBlockHardness(event.getWorld(), event.getPos().add(xx, yy, zz)) >= 0.0F && (ForgeHooks.isToolEffective(event.getWorld(), event.getPos().add(xx, yy, zz), heldItem) || heldItem.getItem() instanceof ItemTool && ((ItemTool)heldItem.getItem()).getDestroySpeed(heldItem, bl) > 1.0F)) {
                        if (event.getHarvester().getName().equals("FakeThaumcraftBore")) {
                            ++event.getHarvester().xpCooldown;
                        } else {
                            heldItem.damageItem(1, event.getHarvester());
                        }

                        BlockUtils.harvestBlock(event.getWorld(), event.getHarvester(), event.getPos().add(xx, yy, zz));
                    }
                }
            }

            ++aa;
        }
    }

    private static void doRefining(BlockEvent.HarvestDropsEvent event, ItemStack heldItem) {
        int xx;
        int fortune = 1 + EnumInfusionEnchantment.getInfusionEnchantmentLevel(heldItem, EnumInfusionEnchantment.REFINING);
        float chance = (float)fortune * 0.125F;
        boolean b = false;

        for(xx = 0; xx < event.getDrops().size(); ++xx) {
            ItemStack is = (ItemStack)event.getDrops().get(xx);
            ItemStack smr = Utils.findSpecialMiningResult(is, chance, event.getWorld().rand);
            if (!is.isItemEqual(smr)) {
                event.getDrops().set(xx, smr);
                b = true;
            }
        }

        if (b) {
            event.getWorld().playSound((EntityPlayer)null, event.getPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.7F + event.getWorld().rand.nextFloat() * 0.2F);
        }
    }

    @Shadow
    static boolean blockDestructiveRecursion;
    @Shadow
    static HashMap<Integer, EnumFacing> lastFaceClicked;


}
