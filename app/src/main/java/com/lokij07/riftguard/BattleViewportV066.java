package com.lokij07.riftguard;

/**
 * Pure viewport bounds shared by transient rendering only.
 * This never changes combat, collisions, drop lifetime, animation timing,
 * wave spawning, boss AI, or gameplay progression.
 *
 * Radius is a conservative visual reach in WORLD units, not a hitbox.
 * The same camera/zoom is used for regular battle and moving outros.
 */
public final class BattleViewportV066 {
    private float left,top,right,bottom;
    public BattleViewportV066(float cameraX,float cameraY,
                              float width,float height,float zoom){
        reset(cameraX,cameraY,width,height,zoom);
    }
    /**
     * Recalculate the moving camera without allocating another viewport
     * every displayed frame. Instance belongs to a single game view.
     */
    public void reset(float cameraX,float cameraY,
                      float width,float height,float zoom){
        if(!Float.isFinite(cameraX)||!Float.isFinite(cameraY)||
                !Float.isFinite(width)||!Float.isFinite(height)||
                !Float.isFinite(zoom)||width<=0f||height<=0f||zoom<=0f)
            throw new IllegalArgumentException("Invalid battle viewport");
        float rx=width*.5f/zoom,ry=height*.5f/zoom;
        left=cameraX-rx;right=cameraX+rx;
        top=cameraY-ry;bottom=cameraY+ry;
    }
    public boolean visible(float x,float y,float radius){
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(radius))
            return false;
        float r=Math.max(0f,radius);
        return x+r>=left&&x-r<=right&&y+r>=top&&y-r<=bottom;
    }
    public boolean rectVisible(float a,float b,float c,float d){
        return Math.max(a,c)>=left&&Math.min(a,c)<=right&&
                Math.max(b,d)>=top&&Math.min(b,d)<=bottom;
    }
    public float left(){return left;}
    public float right(){return right;}
    public float top(){return top;}
    public float bottom(){return bottom;}
}
