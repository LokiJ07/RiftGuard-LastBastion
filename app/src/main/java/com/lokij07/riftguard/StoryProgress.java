package com.lokij07.riftguard;

import android.content.SharedPreferences;

/**
 * v0.65 story save data is additive to riftguard_campaign_v1. Never rewrites
 * unlocked, weapons, supplies, fortress ranks, or mastery state.
 */
public final class StoryProgress {
    private static final String NAME="story.v065.wardenName";
    private static final String MEMORY="story.v065.memory.world.";
    private final SharedPreferences prefs;
    public StoryProgress(SharedPreferences prefs){this.prefs=prefs;}

    public String wardenName(){
        String n=prefs.getString(NAME,"Warden");
        return sanitizeName(n);
    }
    public static String sanitizeName(String requested){
        if(requested==null)return "Warden";
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<requested.length()&&sb.length()<16;i++){
            char ch=requested.charAt(i);
            if(Character.isLetterOrDigit(ch)||ch==' '||ch=='-'||ch=='\'')sb.append(ch);
        }
        String name=sb.toString().trim().replaceAll(" +"," ");
        return name.isEmpty()?"Warden":name;
    }
    public void setWardenName(String requested){
        prefs.edit().putString(NAME,sanitizeName(requested)).commit();
    }
    private static void checkWorld(int world){
        if(world<0||world>=10)throw new IllegalArgumentException("world");
    }
    private int bits(int world){checkWorld(world);return prefs.getInt(MEMORY+world,0)&1023;}
    public boolean hasMemory(int stage){
        Campaign.checkStage(stage);
        if(Campaign.map(stage)!=2)return false;
        int world=Campaign.world(stage), key=(Campaign.level(stage)-1)%10;
        return (bits(world)&(1<<key))!=0;
    }
    /** Claim only from the real completed Memory Trial, never from opening a journal. */
    public boolean claimMemory(int stage,boolean trialPassed,boolean victory){
        Campaign.checkStage(stage);
        if(Campaign.map(stage)!=2||!trialPassed||!victory||hasMemory(stage))return false;
        int world=Campaign.world(stage), key=(Campaign.level(stage)-1)%10;
        int value=bits(world)|(1<<key);
        return prefs.edit().putInt(MEMORY+world,value).commit();
    }
    public int count(int world){return Integer.bitCount(bits(world));}
    public int total(){int n=0;for(int w=0;w<10;w++)n+=count(w);return n;}
    public boolean hasRestoredSeal(int world){return hasOwnedSeal(world)&&count(world)==10;}
    /**
     * Legacy migration: 300-stage campaign is linear and existing 'unlocked'
     * identifies every earlier world boss victory, including pre-v0.65 saves.
     */
    public boolean hasOwnedSeal(int world){
        checkWorld(world);
        int cleared=prefs.getInt("unlocked",0);
        return cleared>=Campaign.stage((world+1)*10,2)+1;
    }
    public int ownedSeals(){int n=0;for(int w=0;w<10;w++)if(hasOwnedSeal(w))n++;return n;}
    public int restoredSeals(){int n=0;for(int w=0;w<10;w++)if(hasRestoredSeal(w))n++;return n;}
    public boolean caveUnlocked(){
        return prefs.getInt("unlocked",0)>=Campaign.STAGES&&ownedSeals()==10;
    }
    public boolean trueEndingUnlocked(){return caveUnlocked()&&total()==100;}
}
