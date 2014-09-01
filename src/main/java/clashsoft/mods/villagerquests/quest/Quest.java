package clashsoft.mods.villagerquests.quest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import clashsoft.cslib.minecraft.lang.I18n;
import clashsoft.cslib.util.CSString;
import clashsoft.mods.villagerquests.quest.type.QuestType;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class Quest
{
	private IQuestProvider	provider;
	private EntityPlayer	player;
	private QuestType		type;
	
	public float			amount;
	public float			maxAmount;
	private float			completion	= -1F;
	
	private boolean			rewarded;
	private List<ItemStack>	rewards;
	
	public Quest()
	{
	}
	
	public Quest(IQuestProvider provider, EntityPlayer player, QuestType type, float maxAmount)
	{
		this.provider = provider;
		this.player = player;
		this.type = type;
		this.maxAmount = maxAmount;
	}
	
	public IQuestProvider getProvider()
	{
		return this.provider;
	}
	
	public QuestType getType()
	{
		return this.type;
	}
	
	public String getName()
	{
		return this.type.getName();
	}
	
	public int getReward()
	{
		float f;
		if (this.hasAmount())
		{
			f = this.type.getReward(this.maxAmount);
		}
		else
		{
			f = this.type.getReward();
		}
		return (int) (f * this.provider.getRewardMultiplier());
	}
	
	public void setProvider(IQuestProvider provider)
	{
		this.provider = provider;
	}
	
	public void setPlayer(EntityPlayer player)
	{
		if (this.player != player)
		{
			QuestList questList = QuestList.getPlayerQuests(player);
			questList.add(this);
			this.player = player;
		}
	}
	
	public static Quest random(IQuestProvider provider, Random seed)
	{
		QuestType type = QuestType.random(seed);
		float amount = type.getRandomAmount(seed);
		return new Quest(provider, null, type, amount);
	}
	
	public boolean isCompleted()
	{
		return this.completion >= 1F;
	}
	
	public float getCompletion()
	{
		return this.completion;
	}
	
	public boolean isRewarded()
	{
		return this.rewarded;
	}
	
	public boolean hasAmount()
	{
		return this.type.hasAmount();
	}
	
	public boolean checkCompleted(EntityPlayer player)
	{
		this.getCompletion(player);
		return this.isCompleted();
	}
	
	public float getCompletion(EntityPlayer player)
	{
		if (player == null)
		{
			return 0F;
		}
		
		this.completion = this.amount / this.maxAmount;
		if (this.completion < 1F)
		{
			this.rewarded = false;
		}
		return this.completion;
	}
	
	public List<ItemStack> getRewards()
	{
		if (this.rewards == null)
		{
			int i = this.getReward();
			
			if (i == 0)
			{
				this.rewards = Collections.EMPTY_LIST;
				return this.rewards;
			}
			
			List<ItemStack> rewards = new ArrayList();
			while (i > 0)
			{
				if (i < 9)
				{
					rewards.add(new ItemStack(Items.gold_nugget, i));
					break;
				}
				else if (i < 27)
				{
					int j = i / 9;
					rewards.add(new ItemStack(Items.gold_ingot, j));
					i -= j * 9;
				}
				else if (i < 81)
				{
					int j = i / 27;
					rewards.add(new ItemStack(Items.emerald, j));
					i -= j * 27;
				}
				else if (i < 162)
				{
					int j = i / 81;
					rewards.add(new ItemStack(Items.diamond, j));
					i -= j * 81;
				}
				else
				{
					int j = i / 162;
					rewards.add(new ItemStack(Items.experience_bottle, j));
					i -= j * 162;
				}
			}
			this.rewards = rewards;
		}
		return this.rewards;
	}
	
	public void reward(EntityPlayer player)
	{
		if (this.isCompleted() && !this.rewarded)
		{
			for (ItemStack stack : this.getRewards())
			{
				player.inventory.addItemStackToInventory(stack);
			}
			this.rewarded = true;
		}
	}
	
	public void addDescription(EntityPlayer player, List<String> lines)
	{
		String name = I18n.getString(this.type.getName());
		String desc = I18n.getString(this.type.getName() + ".desc", this.maxAmount);
		
		lines.add(name);
		for (String s : CSString.cutString(desc, name.length() + 10))
		{
			lines.add("\u00a77" + s);
		}
		
		if (this.isCompleted())
		{
			lines.add("\u00a7a\u00a7o" + I18n.getString("quest.completed"));
			
			if (this.rewarded)
			{
				lines.add("\u00a7e\u00a7o" + I18n.getString("quest.rewarded"));
			}
			else
			{
				lines.add("\u00a78" + I18n.getString("quest.not_rewarded"));
			}
		}
		else
		{
			lines.add("\u00a78\u00a7o" + I18n.getString("quest.not_completed"));
		}
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("Type", this.type.getName());
		nbt.setFloat("Amount", this.amount);
		nbt.setFloat("MaxAmount", this.maxAmount);
		nbt.setFloat("Completion", this.completion);
		nbt.setBoolean("Rewarded", this.rewarded);
	}
	
	public void writeToBuffer(PacketBuffer buffer)
	{
		try
		{
			buffer.writeStringToBuffer(this.type.getName());
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		buffer.writeFloat(this.amount);
		buffer.writeFloat(this.maxAmount);
		buffer.writeFloat(this.completion);
		buffer.writeBoolean(this.rewarded);
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		this.type = QuestType.get(nbt.getString("Type"));
		this.amount = nbt.getFloat("Amount");
		this.maxAmount = nbt.getFloat("MaxAmount");
		this.completion = nbt.getFloat("Completion");
		this.rewarded = nbt.getBoolean("Rewarded");
	}
	
	public void readFromBuffer(PacketBuffer buffer)
	{
		try
		{
			this.type = QuestType.get(buffer.readStringFromBuffer(0xFFFF));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		this.amount = buffer.readFloat();
		this.maxAmount = buffer.readFloat();
		this.completion = buffer.readFloat();
		this.rewarded = buffer.readBoolean();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(this.amount);
		result = prime * result + Float.floatToIntBits(this.maxAmount);
		result = prime * result + Float.floatToIntBits(this.completion);
		result = prime * result + (this.rewarded ? 1231 : 1237);
		result = prime * result + (this.type == null ? 0 : this.type.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (this.getClass() != obj.getClass())
		{
			return false;
		}
		Quest other = (Quest) obj;
		if (this.amount != other.amount)
		{
			return false;
		}
		if (this.maxAmount != other.maxAmount)
		{
			return false;
		}
		if (this.completion != other.completion)
		{
			return false;
		}
		if (this.rewarded != other.rewarded)
		{
			return false;
		}
		if (this.type == null)
		{
			if (other.type != null)
			{
				return false;
			}
		}
		else if (!this.type.equals(other.type))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Quest [type=").append(this.type);
		builder.append(", amount=").append(this.amount);
		builder.append(", maxAmount=").append(this.maxAmount);
		builder.append(", completion=").append(this.completion);
		builder.append(", rewarded=").append(this.rewarded);
		builder.append("]");
		return builder.toString();
	}
}
