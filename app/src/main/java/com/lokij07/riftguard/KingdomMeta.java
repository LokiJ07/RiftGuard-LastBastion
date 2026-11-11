package com.lokij07.riftguard;

import android.content.SharedPreferences;

/**
 * Offline-only permanent progression: daily login, quests, weekly events,
 * achievements and useful shop upgrades. No advertisements, accounts or network.
 * No background work. All rewards are claimed explicitly and saved atomically.
 */
public final class KingdomMeta {
    private final SharedPreferences prefs;
    public static final String[] SHOP_NAMES={
            "WARDEN VITALITY","CORE REINFORCEMENT","TOWER ARSENAL","PULSE CAPACITOR"};
    public static final String[] SHOP_DETAILS={
            "+15 MAX HERO HP / RANK","+160 CORE HP / RANK",
            "+9% ALL TURRET DMG / RANK","-1s PULSE COOLDOWN / RANK"};
    public static final String[] QUEST_NAMES={
            "DEFEAT 20 MONSTERS","WIN 2 BATTLES","CAST 3 PULSE SKILLS"};
    public static final int[] QUEST_TARGETS={20,2,3};
    public static final int[] QUEST_REWARDS={80,120,90};
    public static final String[] ACHIEVEMENTS={
            "FIRST DEFENSE","MONSTER HUNTER","KINGDOM SAVIOR",
            "HARDENED WARDEN","BOSS SLAYER","MONSTER VANQUISHER"};
    public static final int[] ACHIEVEMENT_REWARDS={80,140,250,230,325,450};
    public static final String[] EVENT_NAMES={"GOBLIN INVASION","HERO'S CAMPAIGN","TITAN HUNT"};
    private static final int[] EVENT_TARGETS={70,6,1};
    private static final int[] EVENT_REWARDS={300,360,500};
    private static final int MAX_SHOP_LEVEL=5;

    public KingdomMeta(SharedPreferences prefs){this.prefs=prefs;}
    /** UTC midnight; monotonic day counters prevent rewards via backwards clocks. */
    public static int utcDay(long epochMillis) {
        return (int)Math.floorDiv(Math.max(0,epochMillis),86400000L);
    }
    public static int weekOf(long epochMillis){return utcDay(epochMillis)/7;}
    private static int clamp(int value){return Math.max(0,value);}
    public int supplies(){return clamp(prefs.getInt("supplies",0));}
    public int totalKills(){return clamp(prefs.getInt("meta.kills",0));}
    public int totalWins(){return clamp(prefs.getInt("meta.wins",0));}
    public int totalHardWins(){return clamp(prefs.getInt("meta.hardWins",0));}
    public int totalBossWins(){return clamp(prefs.getInt("meta.bossWins",0));}
    public int shopRank(int index){
        checkShop(index);
        return Math.min(MAX_SHOP_LEVEL,clamp(prefs.getInt("meta.shop."+index,0)));
    }
    public static int shopCost(int index,int rank){
        checkShop(index);
        if(rank>=MAX_SHOP_LEVEL)return 0;
        return (index+1)*55+110+rank*(100+index*30);
    }
    public int shopCost(int index){return shopCost(index,shopRank(index));}
    public int heroMaxHp(){return 100+15*shopRank(0);}
    public int coreBonusHp(){return 160*shopRank(1);}
    public float towerDamageMultiplier(){return 1f+.09f*shopRank(2);}
    public float pulseCooldown(){return 11f-shopRank(3);}
    public boolean buy(int index) {
        checkShop(index);
        int rank=shopRank(index),cost=shopCost(index,rank),money=supplies();
        if(cost<=0||money<cost)return false;
        return prefs.edit().putInt("supplies",money-cost)
                .putInt("meta.shop."+index,rank+1).commit();
    }
    private static void checkShop(int index){
        if(index<0||index>=SHOP_NAMES.length)throw new IllegalArgumentException("shop item");
    }
    /** Calendar is checked on access, so the game never requires background network. */
    private void refreshDay(long now){
        int day=utcDay(now);
        int last=prefs.getInt("meta.questDay",-1);
        if(day>last){
            prefs.edit().putInt("meta.questDay",day)
                    .putInt("meta.dayKills",0).putInt("meta.dayWins",0)
                    .putInt("meta.dayPulses",0).putInt("meta.questClaims",0).commit();
        }
    }
    private void refreshEvent(long now){
        int week=weekOf(now),previous=prefs.getInt("meta.eventWeek",-1);
        if(week>previous) {
            prefs.edit().putInt("meta.eventWeek",week)
                    .putInt("meta.eventKills",0).putInt("meta.eventWins",0)
                    .putInt("meta.eventBossWins",0)
                    .putBoolean("meta.eventClaimed",false).commit();
        }
    }
    public int dailyStreak(){
        return Math.min(7,clamp(prefs.getInt("meta.dailyStreak",0)));
    }
    public boolean canClaimDaily(long now){
        return utcDay(now)>prefs.getInt("meta.dailyClaim",-1);
    }
    public int dailyReward(long now) {
        int newStreak=utcDay(now)==prefs.getInt("meta.dailyClaim",-1)+1
                ?Math.min(7,Math.max(1,dailyStreak()+1)):1;
        return 65+newStreak*25;
    }
    public int claimDaily(long now){
        if(!canClaimDaily(now))return 0;
        int day=utcDay(now),streak=day==prefs.getInt("meta.dailyClaim",-1)+1
                ?Math.min(7,Math.max(1,dailyStreak()+1)):1;
        int amount=65+streak*25;
        if(!prefs.edit().putInt("meta.dailyClaim",day)
                .putInt("meta.dailyStreak",streak)
                .putInt("supplies",supplies()+amount).commit())return 0;
        return amount;
    }
    public int questProgress(int id,long now){
        if(id<0||id>=3)throw new IllegalArgumentException("quest");
        refreshDay(now);
        return clamp(prefs.getInt(new String[]{"meta.dayKills","meta.dayWins",
                "meta.dayPulses"}[id],0));
    }
    public boolean questClaimed(int id,long now){
        refreshDay(now);
        return (prefs.getInt("meta.questClaims",0)&(1<<id))!=0;
    }
    public int claimQuest(int id,long now){
        if(id<0||id>=3)throw new IllegalArgumentException("quest");
        if(questClaimed(id,now)||questProgress(id,now)<QUEST_TARGETS[id])return 0;
        int reward=QUEST_REWARDS[id];
        if(!prefs.edit().putInt("meta.questClaims",
                prefs.getInt("meta.questClaims",0)|(1<<id))
                .putInt("supplies",supplies()+reward).commit())return 0;
        return reward;
    }
    public int achievementProgress(int id){
        switch(id) {
            case 0:return totalWins();
            case 1:return totalKills();
            case 2:return totalWins();
            case 3:return totalHardWins();
            case 4:return totalBossWins();
            case 5:return totalKills();
            default:throw new IllegalArgumentException("achievement");
        }
    }
    public int achievementTarget(int id){
        return new int[]{1,50,10,5,1,300}[id];
    }
    public boolean achievementClaimed(int id){
        if(id<0||id>=ACHIEVEMENTS.length)throw new IllegalArgumentException("achievement");
        return (prefs.getInt("meta.achievements",0)&(1<<id))!=0;
    }
    public int claimAchievement(int id){
        if(id<0||id>=ACHIEVEMENTS.length)throw new IllegalArgumentException("achievement");
        if(achievementClaimed(id)||achievementProgress(id)<achievementTarget(id))return 0;
        int reward=ACHIEVEMENT_REWARDS[id];
        if(!prefs.edit().putInt("meta.achievements",
                prefs.getInt("meta.achievements",0)|(1<<id))
                .putInt("supplies",supplies()+reward).commit())return 0;
        return reward;
    }
    public int eventIndex(long now){refreshEvent(now);return weekOf(now)%3;}
    public int eventTarget(long now){return EVENT_TARGETS[eventIndex(now)];}
    public int eventReward(long now){return EVENT_REWARDS[eventIndex(now)];}
    public int eventProgress(long now){
        switch(eventIndex(now)){
            case 0:return clamp(prefs.getInt("meta.eventKills",0));
            case 1:return clamp(prefs.getInt("meta.eventWins",0));
            default:return clamp(prefs.getInt("meta.eventBossWins",0));
        }
    }
    public boolean eventClaimed(long now){
        refreshEvent(now);
        return prefs.getBoolean("meta.eventClaimed",false);
    }
    public int claimEvent(long now){
        if(eventClaimed(now)||eventProgress(now)<eventTarget(now))return 0;
        int reward=eventReward(now);
        if(!prefs.edit().putBoolean("meta.eventClaimed",true)
                .putInt("supplies",supplies()+reward).commit())return 0;
        return reward;
    }
    /** Called once per finished battle, including losses (kills and casts count). */
    public void recordBattle(int stage,int difficulty,int kills,int pulses,
                             boolean victory,long now) {
        Campaign.checkStage(stage);
        refreshDay(now);refreshEvent(now);
        int cleanedKills=Math.max(0,kills),cleanedPulses=Math.max(0,pulses);
        boolean boss=victory&&DifficultyRules.worldBoss(stage);
        prefs.edit()
                .putInt("meta.kills",totalKills()+cleanedKills)
                .putInt("meta.wins",totalWins()+(victory?1:0))
                .putInt("meta.hardWins",totalHardWins()+
                        (victory&&difficulty>=DifficultyRules.HARD?1:0))
                .putInt("meta.bossWins",totalBossWins()+(boss?1:0))
                .putInt("meta.dayKills",clamp(prefs.getInt("meta.dayKills",0))+cleanedKills)
                .putInt("meta.dayWins",clamp(prefs.getInt("meta.dayWins",0))+(victory?1:0))
                .putInt("meta.dayPulses",clamp(prefs.getInt("meta.dayPulses",0))+cleanedPulses)
                .putInt("meta.eventKills",clamp(prefs.getInt("meta.eventKills",0))+cleanedKills)
                .putInt("meta.eventWins",clamp(prefs.getInt("meta.eventWins",0))+(victory?1:0))
                .putInt("meta.eventBossWins",clamp(prefs.getInt("meta.eventBossWins",0))+(boss?1:0))
                .commit();
    }
}
