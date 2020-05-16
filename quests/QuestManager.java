package net.rinhaxor.rpg.quests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.rinhaxor.core.menus.MenuManager;
import net.rinhaxor.core.unlocks.Unlock;
import net.rinhaxor.core.utils.RMessages;
import net.rinhaxor.rpg.AbstractManagerRPG;
import net.rinhaxor.rpg.SabasRPG;
import net.rinhaxor.rpg.PlayerDataRPG;
import net.rinhaxor.rpg.items.ItemManager;
import net.rinhaxor.rpg.items.RPGItem;
import net.rinhaxor.rpg.npcs.NPCManager;
import net.rinhaxor.rpg.npcs.NPCType;

public class QuestManager extends AbstractManagerRPG {

    public static HashMap<String, Quest> quests = new HashMap<String, Quest>();
    public static ArrayList<Quest> activeQuestList = new ArrayList<Quest>();

    public static HashMap<String, MobTrackerInfo> trackerInfo = new HashMap<String, MobTrackerInfo>();

    public QuestManager(SabasRPG plugin) {
        super(plugin);
    }

    @Override
    public void initialize() {
        Quest.plugin = plugin;
        reload();
    }

    public static void reload() {
        for (Quest q : activeQuestList) {
            for (QuestVillager qv : q.npcs.values()) {
                NPCManager.unregister(qv);
            }
        }
        quests.clear();
        activeQuestList.clear();
        final File dir = new File(QuestManager.plugin.getDataFolder(), "quests");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] listFiles;
        for (int length = (listFiles = dir.listFiles()).length, i = 0; i < length; ++i) {
            final File f = listFiles[i];
            if (f.getName().endsWith(".txt")) {
                readQuest(f);
            }
        }
        for (final Quest q2 : QuestManager.quests.values()) {
            if (q2.active) {
                QuestManager.activeQuestList.add(q2);
            }
        }
        Collections.sort(QuestManager.activeQuestList);
    }
    
    public static void showQuestMenu(final PlayerDataRPG pd) {
        if (pd.getPlayer() != null) {
            final Player p = pd.getPlayer();
            final ArrayList<Object[]> display = new ArrayList<Object[]>();
            int row = 0;
            int col = 0;
            for (final Quest q : QuestManager.activeQuestList) {
                String current = "";
                final int currProg = pd.getQuestProgress(q);
                Material m = Material.PAPER;
                if (q.parts == null) {
                    System.out.println("WARNING: COULD NOT DISPLAY QUEST " + q);
                }
                else {
                    if (currProg <= 0) {
                        current = q.parts.get(0).desc;
                    }
                    else if (currProg < q.parts.size() - 1) {
                        current = q.parts.get(currProg).desc;
                        m = Material.BOOK_AND_QUILL;
                    }
                    else {
                        current = ChatColor.GREEN + "Nhi\u1ec7m v\u1ee5 \u0111\u00e3 ho\u00e0n th\u00e0nh!";
                        m = Material.BOOK;
                    }
                    if (q.reqQuestsProcessed == null) {
                        q.reqQuestsProcessed = new ArrayList<Quest>();
                        for (final String s : q.reqQuests) {
                            if (QuestManager.quests.containsKey(s)) {
                                q.reqQuestsProcessed.add(QuestManager.quests.get(s));
                            }
                        }
                    }
                    boolean questAvailable = pd.level >= q.reqLevel;
                    final ArrayList<String> reqQuests = new ArrayList<String>();
                    if (q.reqQuestsProcessed.size() > 0) {
                        for (final Quest quest : q.reqQuestsProcessed) {
                            if (pd.completedQuest(quest)) {
                                reqQuests.add(ChatColor.GRAY + "- " + ChatColor.GREEN + quest.name);
                            }
                            else {
                                questAvailable = false;
                                reqQuests.add(ChatColor.GRAY + "- " + ChatColor.RED + quest.name);
                            }
                        }
                    }
                    else {
                        reqQuests.add(ChatColor.GRAY + "- Kh\u00f4ng c\u00f3");
                    }
                    if (!questAvailable) {
                        m = Material.EMPTY_MAP;
                    }
                    final ArrayList<Object> list = new ArrayList<Object>();
                    list.add(ChatColor.DARK_AQUA);
                    list.add(q.desc);
                    list.add(ChatColor.YELLOW);
                    list.add("Y\u00eau c\u1ea7u c\u1ea5p \u0111\u1ed9: " + ((pd.level >= q.reqLevel) ? ChatColor.WHITE : ChatColor.RED) + q.reqLevel);
                    list.add(ChatColor.YELLOW);
                    list.add("Y\u00eau c\u1ea7u \u0111\u00e3 xong nhi\u1ec7m v\u1ee5: ");
                    for (final String s2 : reqQuests) {
                        list.add(ChatColor.YELLOW);
                        list.add(s2);
                    }
                    list.add(ChatColor.YELLOW);
                    list.add("Ph\u1ea7n th\u01b0\u1edfng: ");
                    for (final String s2 : q.totalRewards) {
                        list.add(ChatColor.YELLOW);
                        list.add(ChatColor.GRAY + "- " + ChatColor.DARK_GREEN + s2);
                    }
                    list.add(null);
                    list.add("");
                    list.add(ChatColor.AQUA);
                    list.add(current);
                    final Object[] o = { row, col, m, ChatColor.WHITE + q.name, list.toArray(), new Runnable() {
                            @Override
                            public void run() {
                            }
                        } };
                    if (++col >= 9) {
                        ++row;
                        col = 0;
                    }
                    display.add(o);
                }
            }
            final Inventory menu = MenuManager.createMenu(p, ChatColor.LIGHT_PURPLE + "Nh\u1eadt k\u00fd nhi\u1ec7m v\u1ee5 Athena", 6, (Object[][])display.toArray(new Object[display.size()][]));
            p.openInventory(menu);
        }
    }
    
    public static void readQuest(final File f) {
        Scanner scan = null;
        try {
            String next = "";
            scan = new Scanner(f, "utf-8");
            final String questName;
            next = (questName = scan.nextLine().trim());
            next = scan.nextLine().trim();
            final int displayOrder = Integer.parseInt(next.substring("Display Order:".length()).trim());
            next = scan.nextLine().trim();
            final int reqLevel = Integer.parseInt(next.substring("Required Level:".length()).trim());
            next = scan.nextLine().trim();
            String[] data = next.substring("Required Quests:".length()).trim().split(", ");
            final ArrayList<String> reqQuests = new ArrayList<String>();
            String[] array;
            for (int length = (array = data).length, l = 0; l < length; ++l) {
                String s = array[l];
                if (!(s = s.trim()).equalsIgnoreCase("none")) {
                    reqQuests.add(s.trim());
                }
            }
            next = scan.nextLine().trim();
            final ArrayList<String> totalRewards = new ArrayList<String>();
            String[] split;
            for (int length2 = (split = next.substring("Total Rewards:".length()).trim().split(", ")).length, n = 0; n < length2; ++n) {
                final String s2 = split[n];
                totalRewards.add(s2.trim());
            }
            next = scan.nextLine().trim();
            final String desc = next.substring("Description:".length()).trim();
            final Quest q = new Quest(questName, reqLevel, reqQuests, desc, totalRewards);
            q.displayOrder = displayOrder;
            final String identifier = f.getName().substring(0, f.getName().lastIndexOf(46));
            q.identifier = identifier;
            QuestManager.quests.put(identifier, q);
            do {
                next = scan.nextLine().trim();
                if (!next.startsWith("//")) {
                    if (next.length() == 0) {
                        continue;
                    }
                    if (next.equalsIgnoreCase("#start#")) {
                        break;
                    }
                    data = next.split(", ");
                    final int id = Integer.parseInt(data[0]);
                    final String name = data[1];
                    final boolean isBaby = Boolean.parseBoolean(data[2]);
                    final double x = Double.parseDouble(data[3]);
                    final double y = Double.parseDouble(data[4]);
                    final double z = Double.parseDouble(data[5]);
                    final String world = data[6];
                    final QuestVillager qv = new QuestVillager(q, id, name, isBaby ? NPCType.BABYVILLAGER : NPCType.VILLAGER, x, y, z, world);
                    qv.register();
                    q.npcs.put(qv.id, qv);
                }
            } while (!next.equalsIgnoreCase("#start#"));
            int currentPartNum = 0;
            QuestPart qp = null;
            boolean nextPart = true;
            while (scan.hasNextLine()) {
                next = scan.nextLine().trim();
                if (next.length() != 0 && !next.startsWith("//")) {
                    if (next.startsWith("#")) {
                        continue;
                    }
                    if (nextPart) {
                        QuestPart last = null;
                        if (qp != null) {
                            q.parts.put(currentPartNum, qp);
                            ++currentPartNum;
                            last = qp;
                        }
                        qp = new QuestPart(q, currentPartNum);
                        if (last != null) {
                            qp.registerTransfer(last);
                        }
                    }
                    nextPart = true;
                    data = next.split(" ");
                    if (next.startsWith("desc")) {
                        qp.desc = next.substring("desc ".length());
                        qp.desc = qp.desc.replaceAll("\\(", ChatColor.WHITE + ChatColor.BOLD.toString() + "(");
                        qp.desc = qp.desc.replaceAll("\\)", ")" + ChatColor.RESET);
                        qp.newDesc = true;
                        nextPart = false;
                    }
                    else if (next.startsWith("set_default")) {
                        final int id2 = Integer.parseInt(data[1]);
                        final StringBuilder sb = new StringBuilder();
                        for (int k = 2; k < data.length; ++k) {
                            sb.append(data[k]);
                            sb.append(' ');
                        }
                        qp.register(id2, ChatColor.translateAlternateColorCodes('&', sb.toString()));
                        nextPart = false;
                    }
                    else if (next.startsWith("say")) {
                        final int id2 = Integer.parseInt(data[1]);
                        final StringBuilder sb = new StringBuilder();
                        for (int k = 2; k < data.length; ++k) {
                            sb.append(data[k]);
                            sb.append(' ');
                        }
                        qp.partType = QuestPartType.SAY;
                        qp.sayId = id2;
                        qp.sayMsg = ChatColor.translateAlternateColorCodes('&', sb.toString().trim());
                    }
                    else if (next.startsWith("self")) {
                        final int id2 = Integer.parseInt(data[1]);
                        final StringBuilder sb = new StringBuilder();
                        for (int k = 2; k < data.length; ++k) {
                            sb.append(data[k]);
                            sb.append(' ');
                        }
                        qp.partType = QuestPartType.SELF;
                        qp.selfId = id2;
                        qp.selfMsg = ChatColor.translateAlternateColorCodes('&', sb.toString().trim());
                    }
                    else if (next.startsWith("give ")) {
                        nextPart = false;
                        final RPGItem rpgi = ItemManager.itemIdentifierToRPGItemMap.get(data[1]);
                        if (rpgi == null) {
                            RMessages.announce("ERROR: Could not find quest item " + data[1]);
                        }
                        else {
                            qp.giveItems.put(rpgi, Integer.parseInt(data[2]));
                        }
                    }
                    else if (next.startsWith("give_shards")) {
                        nextPart = false;
                        qp.giveShards = Integer.parseInt(data[1]);
                    }
                    else if (next.startsWith("give_exp")) {
                        nextPart = false;
                        qp.giveExp = Long.parseLong(data[1]);
                    }
                    else if (next.startsWith("command")) {
                        nextPart = false;
                        qp.command = data[1].replaceAll("#", " ");
                    }
                    else if (next.startsWith("take")) {
                        qp.partType = QuestPartType.TAKE;
                        final RPGItem rpgi = ItemManager.itemIdentifierToRPGItemMap.get(data[1]);
                        if (rpgi == null) {
                            RMessages.announce("ERROR: Could not find quest item " + data[1]);
                        }
                        else {
                            qp.takeItem = rpgi;
                            qp.takeItemGenerated = rpgi.generate();
                            qp.takeAmount = Integer.parseInt(data[2]);
                            qp.takeId = Integer.parseInt(data[3]);
                            final StringBuilder sb = new StringBuilder();
                            for (int k = 4; k < data.length; ++k) {
                                sb.append(data[k]);
                                sb.append(' ');
                            }
                            final String[] temp = sb.toString().trim().split("##");
                            qp.takeSuccess = ChatColor.translateAlternateColorCodes('&', temp[0].trim());
                            qp.takeFail = ChatColor.translateAlternateColorCodes('&', temp[1].trim());
                        }
                    }
                    else if (next.startsWith("warp")) {
                        final int id2 = Integer.parseInt(data[1]);
                        final double x2 = Double.parseDouble(data[2]);
                        final double y2 = Double.parseDouble(data[3]);
                        final double z2 = Double.parseDouble(data[4]);
                        final float yaw = Float.parseFloat(data[5]);
                        final float pitch = Float.parseFloat(data[6]);
                        final World w = QuestManager.plugin.getServer().getWorld(data[7]);
                        if (w == null) {
                            throw new Exception("Missing quest world " + data[7]);
                        }
                        final StringBuilder sb2 = new StringBuilder();
                        for (int i = 8; i < data.length; ++i) {
                            sb2.append(data[i]);
                            sb2.append(' ');
                        }
                        final Location loc = new Location(w, x2, y2, z2, yaw, pitch);
                        qp.partType = QuestPartType.WARP;
                        qp.warpId = id2;
                        qp.warpLoc = loc;
                        qp.warpMsg = ChatColor.translateAlternateColorCodes('&', sb2.toString().trim());
                    }
                    else if (next.startsWith("state")) {
                        qp.partType = QuestPartType.STATE;
                        qp.stateId = Integer.parseInt(data[1]);
                        qp.stateIdentifier = data[2];
                        final StringBuilder sb3 = new StringBuilder();
                        for (int j = 3; j < data.length; ++j) {
                            sb3.append(data[j]);
                            sb3.append(' ');
                        }
                        final String[] temp2 = sb3.toString().trim().split("##");
                        qp.stateSuccess = temp2[0];
                        qp.stateFail = temp2[1];
                    }
                    else if (next.startsWith("unlock")) {
                        nextPart = false;
                        qp.giveUnlock = Unlock.valueOf(data[1].toUpperCase());
                    }
                    else if (next.startsWith("givestate")) {
                        nextPart = false;
                        qp.giveState = data[1];
                    }
                    else if (next.startsWith("addtracker")) {
                        nextPart = false;
                        qp.hasTracker = true;
                        qp.addingTrackerIdentifier = String.valueOf(q.identifier) + "-" + data[1];
                        qp.mobsToTrack = data[2].split(",");
                        final StringBuilder sb3 = new StringBuilder();
                        for (int j = 4; j < data.length; ++j) {
                            sb3.append(data[j]);
                            sb3.append(' ');
                        }
                        final String[] temp2 = sb3.toString().trim().split("##");
                        final MobTrackerInfo mti = new MobTrackerInfo();
                        mti.mobsToTrack = qp.mobsToTrack;
                        mti.trackerMobName = temp2[0].trim();
                        mti.questName = q.name;
                        mti.trackerFinishedNotification = temp2[1].trim();
                        mti.requiredCount = Integer.parseInt(data[3]);
                        mti.identifier = qp.addingTrackerIdentifier;
                        QuestManager.trackerInfo.put(qp.addingTrackerIdentifier, mti);
                    }
                    else if (next.startsWith("tracker")) {
                        qp.partType = QuestPartType.TRACKER;
                        qp.trackerNPCID = Integer.parseInt(data[1]);
                        qp.trackerIdentifierToCheck = String.valueOf(q.identifier) + "-" + data[2];
                        final StringBuilder sb3 = new StringBuilder();
                        for (int j = 3; j < data.length; ++j) {
                            sb3.append(data[j]);
                            sb3.append(' ');
                        }
                        final String[] temp2 = sb3.toString().trim().split("##");
                        qp.trackerSuccess = temp2[0].trim();
                        qp.trackerFail = temp2[1].trim();
                    }
                    else {
                        if (!next.startsWith("custom")) {
                            throw new Exception("Unknown quest instruction " + next);
                        }
                        final int id2 = Integer.parseInt(data[1]);
                        final int num = Integer.parseInt(data[2]);
                        qp.partType = QuestPartType.CUSTOM;
                        qp.customId = id2;
                        qp.customNum = num;
                    }
                }
            }
            if (qp != null) {
                q.parts.put(currentPartNum, qp);
            }
            QuestManager.quests.put(identifier, q);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (Exception e2) {
            System.err.println("Error reading quest in " + f.getName() + ".");
            e2.printStackTrace();
        }
        finally {
            if (scan != null) {
                scan.close();
            }
        }
        if (scan != null) {
            scan.close();
        }
        System.out.println("Finished reading quest " + f.getName() + ".");
    }
}