package com.lokij07.riftguard;

/**
 * Seeded physical battlefield landmarks for all 300 stages. Props have real
 * health, collapse states, collision steering, and tactical attack responses.
 * They are part of the battle simulation, not just decorative backdrop art.
 */
public final class DestructibleTerrainV065 {
    public static final String[] NAMES={
        "WOODEN BARRICADE","ROOT NEST","POISON POD","CANYON BOULDER",
        "ICE PILLAR","IRON GEAR","CRYSTAL OBELISK","STORM CONDUIT",
        "SHADOW MONOLITH","RIFT SHARD"
    };
    public static final class Prop {
        public final int type,number;
        public final float x,y,maxHp;
        public float hp;
        Prop(int type,int number,float x,float y,float maxHp){
            this.type=type;this.number=number;this.x=x;this.y=y;
            this.maxHp=maxHp;this.hp=maxHp;
        }
        public boolean broken(){return hp<=0;}
        public float fraction(){return hp/maxHp;}
        /** Returns true only on the attack that causes this prop to collapse. */
        public boolean damage(float amount){
            if(broken()||!Float.isFinite(amount)||amount<=0)return false;
            hp=Math.max(0,hp-amount);
            return broken();
        }
    }
    private final Prop[] props;
    private final int stage;
    public DestructibleTerrainV065(int stage){
        Campaign.checkStage(stage);
        this.stage=stage;
        int biome=Campaign.world(stage);
        int count=3+Math.floorMod(stage,2);
        props=new Prop[count];
        for(int i=0;i<count;i++){
            float x=1190+i*195+(Math.floorMod(Campaign.seed(stage)>>i,61)-30);
            float lane=LaneRules.stagePathY(stage,x);
            float y=BattleRules.clamp(lane+((i&1)==0?95:-95),125f,900f);
            float hp=85+biome*24+(i%3)*17;
            props[i]=new Prop(biome,i,x,y,hp);
        }
    }
    public int stage(){return stage;}
    public int count(){return props.length;}
    public Prop at(int index){if(index<0||index>=props.length)throw new IllegalArgumentException("prop");return props[index];}
    public int intact(){int n=0;for(Prop p:props)if(!p.broken())n++;return n;}
    /** Blast radius applies to props only; caller owns effects and score logic. */
    public int splash(float x,float y,float radius,float amount){
        if(radius<=0||amount<=0)return 0;
        int broken=0;
        for(Prop p:props)if(!p.broken()&&
                Math.hypot(p.x-x,p.y-y)<=radius){
            if(p.damage(amount))broken++;
        }
        return broken;
    }
    /** Enemy lane changes while the obstacle blocks the route. */
    public float waypointOffset(float enemyX,float enemyY){
        for(Prop p:props)if(!p.broken()&&
                Math.abs(enemyX-p.x)<105&&Math.abs(enemyY-p.y)<150)
            return enemyY<p.y?-47f:47f;
        return 0f;
    }
    /** Rubble is traversable but slows movement after a collapse. */
    public float rubbleSlow(float enemyX,float enemyY){
        for(Prop p:props)if(p.broken()&&
                Math.hypot(enemyX-p.x,enemyY-p.y)<85)return .83f;
        return 1f;
    }
}
