package net.rinhaxor.rpg.quests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.rinhaxor.core.utils.RScheduler;
import net.rinhaxor.core.utils.RTicks;
import net.rinhaxor.core.utils.RScheduler.Halter;
import net.rinhaxor.rpg.AbstractManagerRPG;
import net.rinhaxor.rpg.SabasRPG;
import net.rinhaxor.rpg.PlayerDataRPG;

public class CustomQuestParts extends AbstractManagerRPG {

    public CustomQuestParts(SabasRPG pl) {
		super(pl);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}
    
	public static boolean check(int id, Player p, PlayerDataRPG pd, Quest quest, QuestVillager qv) {
        if (id == 1) {
            if (!pd.castedFirework) {
                qv.say(p, "Thi triển bắn pháo hóa, tôi sẽ đưa bạn đến phần tiếp theo! Nếu cần giúp đỡ, đừng ngại hỏi!");
                return false;
            } else {
                qv.say(p, "Giỏi lắm! Hãy nói chuyện với tôi để tiếp tục!");
                return true;
            }
        } else if (id == 2) {
            if (pd.soared) {
                qv.say(p, "Bạn đã trở lại!");
                return true;
            } else {
                pd.sendMessage(ChatColor.RED + "You'd better try Soaring before you talk to ChinnSu.");
                pd.sendMessage(ChatColor.RED + "Maybe Rina tricked you and it doesn't work at all!");
                pd.sendMessage(ChatColor.AQUA + "Để bay lượn, " + ChatColor.GREEN + "sneak (nút SHIFT) trên mặt đất trong một giây, sau đó nhảy lên và thả SHIFT.");
                return false;
            }
        } else if (id == 4) {
        	pd.customQuestPart1 = true;
        	RScheduler.schedule(plugin, () -> {
	        	p.teleport(new Location(Bukkit.getWorld("SabasRPG-Main"), -1523.5, 66, -602.5, 0, 0));
	        	p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 450, 2));
	        	Halter h = new Halter();
	        	RScheduler.scheduleRepeating(plugin, new Runnable() {
					@Override
					public void run() {
						p.playSound(p.getLocation(), Sound.ENTITY_BOAT_PADDLE_WATER, 0.75f, 0.65f);
						p.playSound(p.getLocation(), Sound.WEATHER_RAIN_ABOVE, 0.25f, 0.65f);
					}
	        	}, 15, h);
	        	RScheduler.schedule(plugin, () -> {
	        		p.sendMessage(ChatColor.YELLOW + "Chuyến đi đã bắt đầu");
		            RScheduler.schedule(plugin, () -> {
		            	p.sendMessage(ChatColor.YELLOW + "Những tiếng sóng kêu lên xung quanh con thuyền");
		                RScheduler.schedule(plugin, () -> {
		                	p.sendMessage(ChatColor.YELLOW + "Bạn chìm đi trong giấc ngủ");
		                    RScheduler.schedule(plugin, () -> {
		                    	p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.75f, 0.75f);
		                    	p.sendMessage(ChatColor.YELLOW + "Đã tới nơi!");
			                    RScheduler.schedule(plugin, () -> {
			                    	pd.customQuestPart1 = false;
			                    	p.teleport(new Location(Bukkit.getWorld("SabasRPG-Main"), -1229.5, 66, -421.5, 0, 0));
			                    }, RTicks.seconds(2));
			                    h.halt = true;
		                    }, RTicks.seconds(5));
		                }, RTicks.seconds(5));
		            }, RTicks.seconds(5));
	        	}, RTicks.seconds(5));
        	}, RTicks.seconds(3));
            return true;
        }
        
        return false;
    }
}
