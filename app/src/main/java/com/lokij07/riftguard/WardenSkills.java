package com.lokij07.riftguard;

/** Permanent six-skill progression; combat rules remain offline and deterministic. */
public final class WardenSkills {
    private WardenSkills(){}
    public static final int PULSE=0, EMBER=1, FROST=2, BANNER=3, TRAP=4, METEOR=5;
    public static final class Skill {
        public final String name,detail;
        public final int requiredRank,color;
        public final float cooldown,range;
        public Skill(String name,String detail,int rank,int color,float cooldown,float range){
            this.name=name;this.detail=detail;this.requiredRank=rank;this.color=color;
            this.cooldown=cooldown;this.range=range;
        }
    }
    public static final Skill[] SKILLS={
        new Skill("Arc Pulse","Push back nearby invaders",1,0xff9aeaff,11f,235f),
        new Skill("Ember Cleave","Burn enemies in a frontal arc",2,0xffffa65f,9f,205f),
        new Skill("Frost Nova","Freeze and weaken attackers",3,0xff93e4ff,12f,245f),
        new Skill("Healing Banner","Restore hero and core health",3,0xffa8e5b3,14f,180f),
        new Skill("Spike Trap","Arm a persistent ground trap",4,0xffe0c393,13f,104f),
        new Skill("Meteor Strike","Telegraphed explosive siege magic",5,0xffffad78,18f,520f)
    };
    public static boolean unlocked(int rank,int skillId){
        return rank>=1&&rank<=Progression.MAX_RANK&&skillId>=0&&skillId<SKILLS.length
                &&SKILLS[skillId].requiredRank<=rank;
    }
    public static float effectiveCooldown(int id,int capacitorRank){
        if(id<0||id>=SKILLS.length)throw new IllegalArgumentException("skill");
        return Math.max(3f,SKILLS[id].cooldown-Math.max(0,Math.min(5,capacitorRank)));
    }
    public static float clampHealth(float current,float heal,float maximum){
        return Math.min(Math.max(0,maximum),Math.max(0,current)+Math.max(0,heal));
    }
    public static boolean inRadius(float cx,float cy,float radius,float x,float y){
        if(radius<0)return false;
        float dx=x-cx,dy=y-cy;return dx*dx+dy*dy<=radius*radius;
    }
}
