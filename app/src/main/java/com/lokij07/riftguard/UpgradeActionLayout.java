package com.lokij07.riftguard;

/**
 * The SAME fixed action row on all permanent upgrade screens.
 * Interactive coordinates intentionally come from these bounds, not from
 * ad-hoc text positions; tapping an invisible area never buys an upgrade.
 */
public final class UpgradeActionLayout {
    private UpgradeActionLayout(){}
    public static final int SLOTS=4;
    public static final float TOP=627f,BOTTOM=686f;
    public static final float[] LEFT={73f,350f,627f,904f};
    public static final float[] RIGHT={337f,614f,891f,1207f};
    public static int hit(float x,float y){
        if(y<TOP||y>BOTTOM)return -1;
        for(int i=0;i<SLOTS;i++)
            if(x>=LEFT[i]&&x<=RIGHT[i])return i;
        return -1;
    }
    public static float centerX(int slot){
        if(slot<0||slot>=SLOTS)throw new IllegalArgumentException("slot");
        return (LEFT[slot]+RIGHT[slot])*.5f;
    }
}
