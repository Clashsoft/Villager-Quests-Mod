package clashsoft.mods.avi.client.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import clashsoft.mods.avi.api.IQuestProvider;
import clashsoft.mods.avi.inventory.ContainerAdvancedVillager;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonMerchant;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

public class GuiAdvancedVillager extends GuiContainer
{
	public boolean					questMode		= false;
	
	public static ResourceLocation	questBackground	= new ResourceLocation("avi", "textures/gui/container/villager_quests.png");
	public static ResourceLocation	tradeBackground	= new ResourceLocation("avi", "textures/gui/container/villager_trading.png");
	
	/** Instance of IMerchant interface. */
	public IQuestProvider			theVillager;
	public int						currentRecipeIndex;
	public String					name;
	
	public GuiButtonMerchant		nextRecipeButtonIndex;
	public GuiButtonMerchant		previousRecipeButtonIndex;
	public GuiButton				shuffleQuestsButton;
	public GuiButton				questReward;
	
	public GuiAdvancedVillager(InventoryPlayer inventory, IQuestProvider merchant, World world, String name)
	{
		super(new ContainerAdvancedVillager(inventory, merchant, world));
		this.theVillager = merchant;
		this.name = name != null && name.length() >= 1 ? name : I18n.getString("entity.Villager.name");
	}
	
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui()
	{
		super.initGui();
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		
		if (!this.questMode)
		{
			this.buttonList.add(this.nextRecipeButtonIndex = new GuiButtonMerchant(1, i + 102, j + 4, true));
			this.buttonList.add(this.previousRecipeButtonIndex = new GuiButtonMerchant(2, i + 6, j + 4, false));
			this.nextRecipeButtonIndex.enabled = false;
			this.previousRecipeButtonIndex.enabled = false;
		}
		else
		{
			this.buttonList.add(this.shuffleQuestsButton = new GuiButton(1, i + 120, j + 20, 40, 20, "Shuffle"));
			this.buttonList.add(this.questReward = new GuiButton(2, i + 120, j + 45, 40, 20, "Reward"));
		}
	}
	
	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.fontRenderer.drawString(this.name, 60 - this.fontRenderer.getStringWidth(this.name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.getString("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}
	
	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen()
	{
		super.updateScreen();
		MerchantRecipeList merchantrecipelist = this.theVillager.getRecipes(this.mc.thePlayer);
		
		if (merchantrecipelist != null)
		{
			this.nextRecipeButtonIndex.enabled = this.currentRecipeIndex < merchantrecipelist.size() - 1;
			this.previousRecipeButtonIndex.enabled = this.currentRecipeIndex > 0;
		}
	}
	
	/**
	 * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
	 */
	protected void actionPerformed(GuiButton par1GuiButton)
	{
		boolean flag = false;
		
		if (par1GuiButton == this.nextRecipeButtonIndex)
		{
			++this.currentRecipeIndex;
			flag = true;
		}
		else if (par1GuiButton == this.previousRecipeButtonIndex)
		{
			--this.currentRecipeIndex;
			flag = true;
		}
		
		if (flag)
		{
			((ContainerMerchant) this.inventorySlots).setCurrentRecipeIndex(this.currentRecipeIndex);
			ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
			DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
			
			try
			{
				dataoutputstream.writeInt(this.currentRecipeIndex);
				this.mc.getNetHandler().addToSendQueue(new Packet250CustomPayload("MC|TrSel", bytearrayoutputstream.toByteArray()));
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}
	
	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(tradeBackground);
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
		MerchantRecipeList merchantrecipelist = this.theVillager.getRecipes(this.mc.thePlayer);
		
		if (merchantrecipelist != null && !merchantrecipelist.isEmpty())
		{
			int i1 = this.currentRecipeIndex;
			MerchantRecipe merchantrecipe = (MerchantRecipe) merchantrecipelist.get(i1);
			
			if (merchantrecipe.func_82784_g())
			{
				this.mc.getTextureManager().bindTexture(tradeBackground);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glDisable(GL11.GL_LIGHTING);
				this.drawTexturedModalRect(this.guiLeft + 55, this.guiTop + 21, 212, 0, 28, 21);
				this.drawTexturedModalRect(this.guiLeft + 55, this.guiTop + 51, 212, 0, 28, 21);
			}
		}
	}
	
	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3)
	{
		super.drawScreen(par1, par2, par3);
		MerchantRecipeList merchantrecipelist = this.theVillager.getRecipes(this.mc.thePlayer);
		
		if (merchantrecipelist != null && !merchantrecipelist.isEmpty())
		{
			int k = (this.width - this.xSize) / 2;
			int l = (this.height - this.ySize) / 2;
			int i1 = this.currentRecipeIndex;
			
			MerchantRecipe merchantrecipe = (MerchantRecipe) merchantrecipelist.get(i1);
			GL11.glPushMatrix();
			ItemStack itemstack = merchantrecipe.getItemToBuy();
			ItemStack itemstack1 = merchantrecipe.getSecondItemToBuy();
			ItemStack itemstack2 = merchantrecipe.getItemToSell();
			RenderHelper.enableGUIStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRenderer.zLevel = 100.0F;
			itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.getTextureManager(), itemstack, k + 8, l + 24);
			itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.getTextureManager(), itemstack, k + 8, l + 24);
			
			if (itemstack1 != null)
			{
				itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.getTextureManager(), itemstack1, k + 34, l + 24);
				itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.getTextureManager(), itemstack1, k + 34, l + 24);
			}
			
			itemRenderer.renderItemAndEffectIntoGUI(this.fontRenderer, this.mc.getTextureManager(), itemstack2, k + 92, l + 24);
			itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.getTextureManager(), itemstack2, k + 92, l + 24);
			itemRenderer.zLevel = 0.0F;
			GL11.glDisable(GL11.GL_LIGHTING);
			
			if (this.isPointInRegion(8, 24, 16, 16, par1, par2))
			{
				this.drawItemStackTooltip(itemstack, par1, par2);
			}
			else if (itemstack1 != null && this.isPointInRegion(34, 24, 16, 16, par1, par2))
			{
				this.drawItemStackTooltip(itemstack1, par1, par2);
			}
			else if (this.isPointInRegion(92, 24, 16, 16, par1, par2))
			{
				this.drawItemStackTooltip(itemstack2, par1, par2);
			}
			
			GL11.glPopMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
		}
	}
	
	/**
	 * Gets the Instance of IMerchant interface.
	 */
	public IMerchant getIMerchant()
	{
		return this.theVillager;
	}
}
