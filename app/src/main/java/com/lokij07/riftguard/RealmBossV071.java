package com.lokij07.riftguard;

/**
 * Ten distinct real world-boss hit geometries, compared with just three
 * legacy elite ability families. Coordinates are world units. The same
 * collision helpers are used by the live simulation and the telegraph art.
 * No new enemy spawn or change to the permanent campaign save schema.
 */
public final class RealmBossV071 {
    private RealmBossV071(){}
    private static final String[] ATTACKS={
        "BRAMBLE CRUSH","HOLLOW ROOTS","VENOM DIVE","EMBER MORTAR",
        "WHITEOUT SPEAR","IRON BARRAGE","PRISM SHATTER","STORM MARK",
        "SHADOW LASH","ECLIPSE NOVA"
    };
    private static final String[] COUNTERS={
        "Leave the bramble circle. Breaking WAR HORN weakens later slams.",
        "Step away from the locked roots. Breaking ROOT CROWN weakens the strike.",
        "Leave the poison mark; break DEEP CORE to stop the Leviathan healing.",
        "Dodge the marked mortar strike and repair damaged towers.",
        "Sidestep the narrow frost beam; a dodge avoids its slowing impact.",
        "Protect the targeted gun mount; break SHOULDER CANNON to cut damage.",
        "Stay inside or outside the crystal ring; break REFLECTED HEART to stop healing.",
        "Leave the locked lightning mark before the discharge.",
        "Move off the shadow beam to avoid damage and skill suppression.",
        "Escape the wide nova; shatter CORRUPTED SWORD to weaken the blast."
    };
    // Individual patterns have different range, affected defense resource
    // and necessary dodge position. Changes apply ONLY to actual world bosses.
    private static final float[] HERO_DAMAGE={26,18,16,18,22,17,24,29,23,34};
    private static final float[] CORE_DAMAGE={19,12,11,27,14,29,17,15,14,34};
    private static final int[] COLORS={
        0xffcfdf8a,0xffa8d89b,0xff91d17d,0xffff9c66,0xff94dcf9,
        0xffebbb82,0xffb2a1ff,0xff8fcfff,0xffc29ce7,0xffffb37d
    };
    public static boolean enabled(int stage,boolean isWorldBoss,boolean caveMode){
        Campaign.checkStage(stage);
        return !caveMode&&isWorldBoss&&DifficultyRules.worldBoss(stage);
    }
    public static int realm(int stage){return Campaign.world(stage);}
    public static String name(int stage){return ATTACKS[realm(stage)];}
    public static String counter(int stage){return COUNTERS[realm(stage)];}
    public static int color(int stage){return COLORS[realm(stage)];}
    public static float chargeSeconds(int stage,int phase){
        return Math.max(1.14f,1.55f-(Math.max(1,Math.min(3,phase))-1)*.12f
                +(realm(stage)%3)*.08f);
    }
    public static float intervalSeconds(int stage,int phase){
        return Math.max(7.2f,9.9f-(Math.max(1,Math.min(3,phase))-1)*.66f
                -(realm(stage)%4)*.09f);
    }
    public static boolean targetsTower(int stage){
        return realm(stage)==3||realm(stage)==5;
    }
    public static float radius(int stage,int phase){
        int p=Math.max(1,Math.min(3,phase))-1;
        switch(realm(stage)){
            case 0:return 216f+p*12f;
            case 1:return 112f+p*6f;
            case 2:return 129f+p*5f;
            case 3:return 143f+p*6f;
            case 4:return 66f+p*5f; // Beam half-width
            case 5:return 151f+p*5f;
            case 6:return 255f+p*10f; // Outer crystal annulus edge
            case 7:return 116f+p*6f;
            case 8:return 73f+p*4f; // Beam half-width
            default:return 290f+p*12f;
        }
    }
    public static float innerRadius(int stage,int phase){
        return realm(stage)==6?126f+7f*(Math.max(1,Math.min(3,phase))-1):0f;
    }
    private static float squared(float x,float y,float ax,float ay){
        float dx=x-ax,dy=y-ay;return dx*dx+dy*dy;
    }
    /** Endpoint-inclusive tapered-free capsule; renderer draws same width. */
    private static float segmentDistanceSquared(float x,float y,
                                                float ax,float ay,float bx,float by){
        float vx=bx-ax,vy=by-ay;
        float len=vx*vx+vy*vy;
        if(len<.0001f)return squared(x,y,ax,ay);
        float t=Math.max(0,Math.min(1,((x-ax)*vx+(y-ay)*vy)/len));
        return squared(x,y,ax+vx*t,ay+vy*t);
    }
    public static boolean hitsHero(int stage,int phase,float bossX,float bossY,
                                   float targetX,float targetY,float heroX,float heroY){
        if(!Float.isFinite(bossX)||!Float.isFinite(bossY)||
                !Float.isFinite(targetX)||!Float.isFinite(targetY)||
                !Float.isFinite(heroX)||!Float.isFinite(heroY))return false;
        int world=realm(stage);
        float radius=radius(stage,phase);
        float d2;
        if(world==4||world==8){
            d2=segmentDistanceSquared(heroX,heroY,bossX,bossY,targetX,targetY);
            return d2<=radius*radius;
        }
        if(world==0||world==6||world==9)
            d2=squared(heroX,heroY,bossX,bossY);
        else d2=squared(heroX,heroY,targetX,targetY);
        if(world==6){
            float inner=innerRadius(stage,phase);
            return d2>=inner*inner&&d2<=radius*radius;
        }
        return d2<=radius*radius;
    }
    public static float heroDamage(int stage){return HERO_DAMAGE[realm(stage)];}
    public static float coreDamage(int stage){return CORE_DAMAGE[realm(stage)];}
    public static float towerDamage(int stage){
        int r=realm(stage);
        return r==3?52f:r==5?76f:0f;
    }
    public static float slowSeconds(int stage){
        switch(realm(stage)){
            case 1:return 1.6f;
            case 2:return 1.9f;
            case 4:return 2.3f;
            case 9:return 1.2f;
            default:return 0f;
        }
    }
    public static float skillSuppression(int stage){
        return realm(stage)==8?1.2f:0f;
    }
    /** Core weak spot can be destroyed to prevent sustain and summons. */
    public static float selfHealFraction(int stage,int phase,boolean coreIntact){
        if(!coreIntact)return 0f;
        int r=realm(stage);
        if(r!=2&&r!=6)return 0f;
        return (r==2?.013f:.019f)+(Math.max(1,Math.min(3,phase))-1)*.002f;
    }
}
