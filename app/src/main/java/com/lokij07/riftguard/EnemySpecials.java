package com.lokij07.riftguard;

/** New distinct enemy archetype mechanics; independent of rendering and Android. */
public final class EnemySpecials {
    private EnemySpecials(){}
    public static final float BOAR_WINDUP=.48f,BOAR_DASH=.64f;
    public static final float SHAMAN_BOLT_SPEED=390f;
    public static boolean canBoarCharge(int kind,float dist,float cooldown) {
        return kind==6&&dist>56f&&dist<310f&&cooldown<=0;
    }
    public static boolean canShamanCast(int kind,float dist,float cooldown) {
        return kind==4&&dist>55f&&dist<390f&&cooldown<=0;
    }
    public static float mobilityMultiplier(float windup,float dash){
        return dash>0?2.4f:windup>0?.34f:1f;
    }
    public static boolean boltConnects(float boltX,float boltY,
                                      float heroX,float heroY){
        return WardenSkills.inRadius(boltX,boltY,55,heroX,heroY);
    }
}
