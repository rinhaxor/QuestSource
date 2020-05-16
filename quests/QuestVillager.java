package net.rinhaxor.rpg.quests;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.rinhaxor.rpg.PlayerDataRPG;
import net.rinhaxor.rpg.npcs.NPCEntity;
import net.rinhaxor.rpg.npcs.NPCType;

public class QuestVillager extends NPCEntity {

    public Quest quest;

    public QuestVillager(Quest quest, int id, String name, NPCType type, double x, double y, double z, String world) {
        super(id, name, type, x, y, z, world);
        this.quest = quest;
    }
    @Override
    public void interact(Player p, PlayerDataRPG pd) {
        if (pd.level < quest.reqLevel) {
            p.sendMessage("");
            p.sendMessage(ChatColor.GRAY + ">> " + ChatColor.RED + "NPC này là một phần của nhiệm vụ, nhưng bạn chưa đạt cấp độ yêu cầu!");
            p.sendMessage(ChatColor.GRAY + ">> " + ChatColor.RED + "Quay trở lại khi bạn đạt " + ChatColor.YELLOW + "Cấp " + quest.reqLevel + ChatColor.RED + ".");
            p.sendMessage(ChatColor.GRAY + ">> " + ChatColor.GRAY + "Cần trợ giúp về cấp độ? Hãy chắc chắn rằng bạn đã kiểm tra " + ChatColor.YELLOW + "/quests" + ChatColor.GRAY + "!");
            say(p, quest.parts.get(0).getMessage(this.id));
            return;
        }
        if (quest.reqQuests != null && !quest.reqQuests.isEmpty()) {
            if (quest.reqQuestsProcessed == null) {
                quest.reqQuestsProcessed = new ArrayList<Quest>();
                for (String s : quest.reqQuests) {
                    if (QuestManager.quests.containsKey(s)) {
                        quest.reqQuestsProcessed.add(QuestManager.quests.get(s));
                    }
                }
            }
            ArrayList<Quest> uncompleted = new ArrayList<Quest>();
            for (Quest q : quest.reqQuestsProcessed) {
                if (pd.completedQuest(q))
                    continue;
                uncompleted.add(q);
            }
            if (uncompleted.size() > 0) {
                p.sendMessage("");
                p.sendMessage(ChatColor.GRAY + ">> " + ChatColor.RED + "Nhiệm vụ này yêu cầu bạn hoàn thành các nhiệm vụ sau!");
                for (Quest q : uncompleted)
                    p.sendMessage(ChatColor.GRAY + ">> " + ChatColor.YELLOW + q.name);
                say(p, quest.parts.get(0).getMessage(this.id));
                return;
            }
        }
        int prog = -1;
        if ((prog = pd.getQuestProgress(quest)) >= -1) {
            if (quest.parts.containsKey(prog + 1)) {
                QuestPart qp = quest.parts.get(prog + 1);
                if (qp.isNPC(this)) {
                    if (qp.interact(this, p, pd))
                        pd.advanceQuest(quest);
                } else {
                    if (quest.parts.containsKey(prog))
                        say(p, quest.parts.get(prog).getMessage(this.id));
                    else
                        say(p, qp.getMessage(this.id));
                }
            } else if (pd.completedQuest(quest)) {
                say(p, quest.parts.get(quest.parts.size() - 1).getMessage(this.id));
            }
        }
    }

    @Override
    public String toString() {
        return "QuestNPC: " + name;
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.AQUA;
    }
	public Entity getEntity() {
		return le;
	}

}
