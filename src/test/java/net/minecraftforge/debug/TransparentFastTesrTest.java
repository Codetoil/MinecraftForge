package net.minecraftforge.debug;

import com.google.common.base.Optional;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = TransparentFastTesrTest.MODID, name = "TransparentFastTESRTest")
public class TransparentFastTesrTest
{
    static final String MODID = "transparent_fast_tesr_test";

    private static class TransparentFastTESR extends FastTESR<TransparentFastTE>
    {

        private static TextureAtlasSprite getFluidTexture(Fluid fluid)
        {
            final ResourceLocation textureLocation = fluid.getStill();
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(textureLocation.toString());
        }

        private static void addVertexWithUV(VertexBuffer wr, double x, double y, double z, double u, double v, int skyLight, int blockLight)
        {
            wr.pos(x, y, z).color(0xFF, 0xFF, 0xFF, 0xFF).tex(u, v).lightmap(skyLight, blockLight).endVertex();
        }

        @Override
        public void renderTileEntityFast(TransparentFastTE te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer wr)
        {
            if (te.fluid == null)
                return;
            final int skyLight = 0x00f0;
            final int blockLight = 0x00f0;

            final TextureAtlasSprite texture = getFluidTexture(te.fluid);
            final double uMin = texture.getMinU();
            final double uMax = texture.getMaxU();
            final double vMin = texture.getMinV();
            final double vMax = texture.getMaxV();

            wr.setTranslation(x, y, z);
            addVertexWithUV(wr, 1, 0, 0, uMax, vMin, skyLight, blockLight);
            addVertexWithUV(wr, 1, 1, 0, uMax, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 0, 1, 0, uMin, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 0, 0, 0, uMin, vMin, skyLight, blockLight);

            addVertexWithUV(wr, 1, 0, 1, uMin, vMin, skyLight, blockLight);
            addVertexWithUV(wr, 1, 1, 1, uMin, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 0, 1, 1, uMax, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 0, 0, 1, uMax, vMin, skyLight, blockLight);

            addVertexWithUV(wr, 1, 0, 0, uMin, vMin, skyLight, blockLight);
            addVertexWithUV(wr, 1, 1, 0, uMin, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 1, 1, 1, uMax, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 1, 0, 1, uMax, vMin, skyLight, blockLight);

            addVertexWithUV(wr, 0, 0, 1, uMin, vMin, skyLight, blockLight);
            addVertexWithUV(wr, 0, 1, 1, uMin, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 0, 1, 0, uMax, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 0, 0, 0, uMax, vMin, skyLight, blockLight);

            addVertexWithUV(wr, 1, 0, 0, uMax, vMin, skyLight, blockLight);
            addVertexWithUV(wr, 1, 0, 1, uMin, vMin, skyLight, blockLight);
            addVertexWithUV(wr, 0, 0, 1, uMin, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 0, 0, 0, uMax, vMax, skyLight, blockLight);

            addVertexWithUV(wr, 1, 1, 0, uMax, vMin, skyLight, blockLight);
            addVertexWithUV(wr, 0, 1, 0, uMax, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 0, 1, 1, uMin, vMax, skyLight, blockLight);
            addVertexWithUV(wr, 1, 1, 1, uMin, vMin, skyLight, blockLight);
            wr.setTranslation(0, 0, 0);
        }
    }

    public static class TransparentFastTE extends TileEntity
    {

        private final Fluid fluid;

        public TransparentFastTE()
        {
            this(null);
        }

        public TransparentFastTE(Fluid fluid)
        {
            this.fluid = fluid;
        }

        @Override
        public boolean hasFastRenderer()
        {
            return true;
        }

        @Override
        public boolean shouldRenderInPass(int pass)
        {
            return pass == 1;
        }

    }

    public static final PropertyInteger FLUID = PropertyInteger.create("fluid", 0, 15);

    private static Optional<Fluid> getNthFluid(int meta)
    {
        for (Fluid f : FluidRegistry.getRegisteredFluids().values())
        {
            if (meta-- == 0)
                return Optional.of(f);
        }

        return Optional.absent();
    }

    private static final Block testBlock = new BlockContainer(Material.CORAL)
    {

        @Override
        public TileEntity createNewTileEntity(World worldIn, int meta)
        {
            Optional<Fluid> maybeFluid = getNthFluid(meta);
            if (maybeFluid.isPresent())
            {
                return new TransparentFastTE(maybeFluid.get());
            }

            return null;
        }

        @Override
        public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
        {
            final ItemStack held = placer.getHeldItem(hand);
            return getDefaultState().withProperty(FLUID, Math.min(held.getMetadata(), 15));
        }

        @Override
        protected BlockStateContainer createBlockState()
        {
            return new BlockStateContainer(this, FLUID);
        }

        @Override
        public int getMetaFromState(IBlockState state)
        {
            return state.getValue(FLUID);
        }

        @Override
        public IBlockState getStateFromMeta(int meta)
        {
            return getDefaultState().withProperty(FLUID, meta);
        }

        @Override
        public boolean isOpaqueCube(IBlockState state)
        {
            return false;
        }

        @Override
        public void getSubBlocks(Item item, CreativeTabs itemIn, NonNullList<ItemStack> items)
        {
            final int fluidCount = Math.min(FluidRegistry.getRegisteredFluids().size(), 15);
            for (int i = 0; i < fluidCount; i++)
                items.add(new ItemStack(this, 1, i));
        }
    };

    @EventBusSubscriber
    public static class BlockHolder
    {

        @SubscribeEvent
        public static void onBlockRegister(RegistryEvent.Register<Block> evt)
        {
            evt.getRegistry().register(testBlock
                    .setCreativeTab(CreativeTabs.DECORATIONS)
                    .setRegistryName("fluid-tesr-block"));
        }

        @SubscribeEvent
        public static void onItemRegister(RegistryEvent.Register<Item> evt)
        {
            evt.getRegistry().register(new ItemBlock(testBlock)
            {
                @Override
                public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
                {
                    final Optional<Fluid> nthFluid = getNthFluid(stack.getMetadata());
                    if (nthFluid.isPresent())
                    {
                        tooltip.add("Fluid: " + nthFluid.get().getName());
                    }
                }
            }
                    .setHasSubtypes(true)
                    .setRegistryName("tesr-test"));
        }

    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        GameRegistry.registerTileEntity(TransparentFastTE.class, "fast-tesr-te");
    }

    @Mod.EventBusSubscriber(value = Side.CLIENT, modid = MODID)
    public static class ClientLoader
    {
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event)
        {
            ClientRegistry.bindTileEntitySpecialRenderer(TransparentFastTE.class, new TransparentFastTESR());
        }
    }
}
