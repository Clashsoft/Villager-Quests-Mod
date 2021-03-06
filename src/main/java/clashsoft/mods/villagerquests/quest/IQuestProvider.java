package clashsoft.mods.villagerquests.quest;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IQuestProvider extends IMerchant
{
	public void shuffleQuests(EntityPlayerMP player);
	
	public void refreshQuests(EntityPlayerMP player);
	
	public void rewardQuests(EntityPlayerMP player);
	
	public QuestList getQuests();
}
