package com.lokij07.riftguard;

import android.content.res.Resources;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Production sprite layers for Android Canvas.
 * Hero animation has four independently painted/warped poses per state:
 * idle, running, attack and cast. Monsters have 4 locomotion + 4 attack poses.
 * The original painted Warden armor is equipment-masked; independently
 * rendered weapons stay anchored to the hand. Smooth short transitions preserve
 * the painterly sprite atlas without introducing fake AI-generated frames.
 */
public final class MotionArt {
    private static final int HERO_W=164,HERO_H=188;
    private static final int ENEMY_W=112,ENEMY_H=126;
    private static final int WEAPON_W=128,WEAPON_H=144;
    private static final int TILE=256;
    private static final int WORLD_W=1900,WORLD_H=1050;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
    private final Bitmap heroes,monsters,weapons,tiles,castle;
    private static final int[] BASE={
        0xff3f704e,0xff2c5346,0xff415e4a,0xff8c5b3f,0xff91acbc,
        0xff6c736a,0xff4b7282,0xff50637a,0xff46415d,0xff784349
    };
    private Bitmap cachedTerrain;
    private int cachedStage=-1;
    // Pre-feathered torso crops preserve painted realism, avoid rectangular
    // clipping seams and NEVER include the original weapon-at-waist region.
    private final Bitmap[] cleanTorsoCache=new Bitmap[16];

    public MotionArt(Resources resources) {
        this(resources,null);
    }
    public MotionArt(Context context) {
        this(context.getResources(),context);
    }
    private MotionArt(Resources resources,Context context) {
        heroes=decode(resources,context,R.drawable.riftguard_hero_motion,
                "riftguard_hero_motion.webp",656,752);
        monsters=decode(resources,context,R.drawable.riftguard_enemy_motion,
                "riftguard_enemy_motion.webp",896,882);
        weapons=decode(resources,context,R.drawable.riftguard_weapons_motion,
                "riftguard_weapons_motion.webp",768,144);
        tiles=decode(resources,context,R.drawable.riftguard_terrain_tiles,
                "riftguard_terrain_tiles.webp",1024,2560);
        castle=decode(resources,context,R.drawable.riftguard_castle_game,
                "riftguard_castle_game.webp",420,338);
    }
    private static Bitmap decode(Resources resources,Context context,int id,
                                 String file,int width,int height) {
        if(context!=null)
            return ResourcePacks.decodeOrBundled(context,id,file,width,height);
        return decode(resources,id);
    }
    private static Bitmap decode(Resources resources,int id) {
        try {
            BitmapFactory.Options opts=new BitmapFactory.Options();
            opts.inScaled=false;
            return BitmapFactory.decodeResource(resources,id,opts);
        }catch(RuntimeException ex){return null;}
    }
    public boolean heroReady(){return valid(heroes,656,752);}
    public boolean enemiesReady(){return valid(monsters,896,882);}
    public boolean weaponsReady(){return valid(weapons,768,144);}
    public boolean terrainReady(){return valid(tiles,1024,2560);}
    public boolean fortressReady(){return valid(castle,420,338);}
    public boolean ready(){return heroReady()&&enemiesReady()&&weaponsReady()
            &&terrainReady()&&fortressReady();}
    private static boolean valid(Bitmap b,int w,int h) {
        return b!=null&&!b.isRecycled()&&b.getWidth()==w&&b.getHeight()==h;
    }
    public static int frame(float seconds,float fps,int frames) {
        if(!Float.isFinite(seconds)||!Float.isFinite(fps)||fps<=0||frames<=0)return 0;
        return Math.floorMod((int)Math.floor(seconds*fps),frames);
    }
    public static int eventFrame(float progress) {
        if(Float.isNaN(progress))return 0;
        return Math.max(0,Math.min(3,(int)(Math.max(0,progress)*4)));
    }
    private static float clamp01(float n){return Math.max(0,Math.min(1,n));}

    /** Weapon sprites can also be used in the HUD/Armory as real image assets. */
    public boolean weaponIcon(Canvas c,int id,float x,float y,float width,float height) {
        if(!weaponsReady()||id<0||id>=6)return false;
        Rect source=new Rect(id*WEAPON_W,0,(id+1)*WEAPON_W,WEAPON_H);
        p.setColor(Color.WHITE);p.setAlpha(255);
        c.drawBitmap(weapons,source,
                new RectF(x-width/2f,y-height/2f,x+width/2f,y+height/2f),p);
        return true;
    }
    public void prepareTerrain(int stage) {
        if(!terrainReady()||cachedStage==stage)return;
        if(cachedTerrain!=null){cachedTerrain.recycle();cachedTerrain=null;}
        cachedStage=stage;
        cachedTerrain=Bitmap.createBitmap(WORLD_W,WORLD_H,Bitmap.Config.RGB_565);
        Canvas c=new Canvas(cachedTerrain);
        int biome=Campaign.world(stage);
        c.drawColor(BASE[biome]);
        p.setColor(Color.WHITE);p.setAlpha(255);
        // Feathered 256px texture patches overlap on a 160px grid.
        // Their transparent edges blend naturally rather than exposing hard square seams.
        for(int y=-128;y<WORLD_H+TILE;y+=160){
            for(int x=-128;x<WORLD_W+TILE;x+=160){
                int xIndex=Math.floorDiv(x,160),yIndex=Math.floorDiv(y,160);
                int v=Math.floorMod((xIndex*73856093)^(yIndex*19349663)^
                        Campaign.seed(stage),4);
                Rect src=new Rect(v*TILE,biome*TILE,(v+1)*TILE,(biome+1)*TILE);
                Rect dst=new Rect(x,y,x+TILE,y+TILE);
                c.drawBitmap(tiles,src,dst,p);
            }
        }
        Paint shade=new Paint(Paint.ANTI_ALIAS_FLAG);
        shade.setShader(new LinearGradient(0,0,0,WORLD_H,
                0x14ffffff,0x320a182a,Shader.TileMode.CLAMP));
        c.drawRect(0,0,WORLD_W,WORLD_H,shade);
        shade.setShader(null);
        if(Campaign.map(stage)==2){
            shade.setColor(0x190f0920);
            c.drawRect(0,0,WORLD_W,WORLD_H,shade);
        }
    }
    public boolean terrain(Canvas c) {
        if(cachedTerrain==null||cachedTerrain.isRecycled())return false;
        p.setColor(Color.WHITE);
        p.setAlpha(255);
        c.drawBitmap(cachedTerrain,0,0,p);
        return true;
    }

    /** Separate painted castle structure with animated lights and rank-specific layers. */
    public boolean fortress(Canvas c,float x,float y,int rank,float time) {
        if(rank<3||!fortressReady())return false;
        p.setColor(0x9907161c);p.setAlpha(85);
        c.drawOval(new RectF(x-180,y+82,x+182,y+135),p);
        p.setColor(Color.WHITE);p.setAlpha(255);
        c.drawBitmap(castle,null,new RectF(x-219,y-301,x+219,y+128),p);
        FortressStructures.rankStructures(c,x,y,rank,time);
        Paint fx=new Paint(Paint.ANTI_ALIAS_FLAG);
        // Animated lanterns have physical positions separate from the castle texture.
        for(int i=0;i<4;i++) {
            float tx=x+(i%2==0?-145:145);
            float ty=y-45-(i/2)*115;
            float glow=6+(float)Math.sin(time*5+i*2.3f)*2.5f;
            fx.setColor(0x66ffc774);c.drawCircle(tx,ty,glow*2.7f,fx);
            fx.setColor(0xffffd39a);c.drawCircle(tx,ty,glow*.68f,fx);
        }
        if(rank>=4) {
            float pulse=(float)Math.sin(time*3f)*3f;
            fx.setStyle(Paint.Style.STROKE);fx.setStrokeWidth(4);
            fx.setColor(0xaaa2efff);
            c.drawOval(new RectF(x-154-pulse,y-82-pulse,
                    x+154+pulse,y+112+pulse),fx);
            fx.setStyle(Paint.Style.FILL);
        }
        if(rank>=5) {
            float pulse=(float)Math.sin(time*2.4f)*9;
            fx.setColor(0x448be5ff);
            c.drawCircle(x,y-234,37+pulse,fx);
            fx.setColor(0xffb4f3ff);
            c.drawCircle(x,y-234,12+pulse*.2f,fx);
        }
        return true;
    }

    /** Draw an actual frame by sprite-sheet index: 0 idle, 1 run, 2 attack, 3 cast. */
    public void drawHeroFrame(Canvas c,int state,int frame,float x,float y,
                              boolean flip,boolean hurt) {
        // Even single-frame previews must use the same equipment-masked hero as
        // gameplay; never reintroduce the old full-rectangle character atlas.
        drawHeroRigFrame(c,state,frame,x,y,flip,hurt,state==1,
                frame*1.8f,state==2?frame/3f:-1f);
    }


    /**
     * Separately articulated lower body: boots, shins and knees are independent
     * bones rather than vertical bob applied to the entire original character.
     * Upper-body armor remains a clipped section of the original detailed art.
     */
    /**
     * A hinged limb cut directly from each creature's REAL painted atlas frame.
     * Source armor, leather and boots retain their detailed textures, with the
     * footprint and rotation determined by ActorGait instead of a flat overlay.
     */
    private void paintedLeg(Canvas c,Bitmap atlas,int frameLeft,int frameTop,
                            int cropLeft,int cropTop,int cropRight,int cropBottom,
                            ActorGait.Leg bone,float width,boolean hurt){
        float dy=Math.max(15,bone.footY-bone.hipY);
        float dx=bone.footX-bone.hipX;
        float tilt=-(float)Math.toDegrees(Math.atan2(dx,dy))*.47f;
        c.save();
        c.translate(bone.hipX,bone.hipY);
        c.rotate(tilt);
        Rect source=new Rect(frameLeft+cropLeft,frameTop+cropTop,
                frameLeft+cropRight,frameTop+cropBottom);
        // A tapered boot/greave silhouette avoids the old billboard-like
        // rectangular armor crops and retains the original painted textures.
        Path legShape=new Path();
        legShape.moveTo(-width*.38f,-8);
        legShape.quadTo(0,-13,width*.38f,-8);
        legShape.lineTo(width*.48f,dy*.56f);
        legShape.lineTo(width*.39f,dy+5);
        legShape.quadTo(0,dy+9,-width*.45f,dy+5);
        legShape.lineTo(-width*.48f,dy*.58f);
        legShape.close();
        c.clipPath(legShape);
        p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);
        p.setAlpha(hurt?195:255);
        c.drawBitmap(atlas,source,
                new RectF(-width*.5f,-8,width*.5f,dy+6),p);
        p.setAlpha(255);
        c.restore();
    }
    /**
     * Build a single painterly upper-body layer from the original source frame.
     * Right-waist equipment (right of x=112 below y=76) is NEVER sampled.
     * Soft transparency along the crop edge prevents a pasted rectangle.
     * Cached per pose: no per-frame pixel processing in the game loop.
     */
    private Bitmap cleanUpperBody(int row,int col) {
        int index=row*4+col;
        if(cleanTorsoCache[index]!=null)return cleanTorsoCache[index];
        Bitmap result=Bitmap.createBitmap(141,122,Bitmap.Config.ARGB_8888);
        Canvas stage=new Canvas(result);
        Paint copied=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        copied.setColor(Color.WHITE);
        int left=col*HERO_W,top=row*HERO_H;
        stage.drawBitmap(heroes,new Rect(left,top,left+141,top+76),
                new Rect(0,0,141,76),copied);
        stage.drawBitmap(heroes,new Rect(left,top+76,left+112,top+122),
                new Rect(0,76,112,122),copied);
        stage.drawBitmap(heroes,new Rect(left+101,top+34,left+139,top+72),
                new Rect(101,34,139,72),copied);
        int[] pixels=new int[141*122];
        result.getPixels(pixels,0,141,0,0,141,122);
        for(int y=0;y<122;y++){
            int end=y<76?141:112;
            float bottom=y<=112?1f:Math.max(0,(122-y)/10f);
            for(int x=0;x<141;x++){
                int idx=y*141+x;
                int pixel=pixels[idx];
                if((pixel>>>24)==0)continue;
                if(x>=end){pixels[idx]=0;continue;}
                // Smooth only the artificial crop boundaries, not details.
                float right=Math.min(1f,Math.max(0f,(end-x)/9f));
                float leftFade=Math.min(1f,Math.max(0f,(x+2)/7f));
                float edge=Math.min(Math.min(right,leftFade),bottom);
                int alpha=Math.round((pixel>>>24)*edge);
                pixels[idx]=(pixel&0x00ffffff)|(alpha<<24);
            }
        }
        result.setPixels(pixels,0,141,0,0,141,122);
        cleanTorsoCache[index]=result;
        return result;
    }

    private void drawHeroRigFrame(Canvas c,int state,int frame,float x,float y,
                                  boolean flip,boolean hurt,boolean running,float phase,
                                  float attack) {
        drawHeroRigFrame(c,state,frame,x,y,flip,hurt,running,phase,attack,0);
    }
    private void drawHeroRigFrame(Canvas c,int state,int frame,float x,float y,
                                  boolean flip,boolean hurt,boolean running,float phase,
                                  float attack,int weaponId) {
        if(!heroReady())return;
        int row=Math.max(0,Math.min(3,state));
        int column=Math.max(0,Math.min(3,frame));
        c.save();c.translate(x,y);
        if(flip)c.scale(-1,1);
        p.setStyle(Paint.Style.FILL);p.setColor(0x9908131e);p.setAlpha(120);
        c.drawOval(new RectF(-36,-3,35,11),p);
        // The cape is a separately swaying crop of the SAME painted armor set.
        float wind=running?(float)Math.sin(phase)*11:((float)Math.sin(phase*.3f))*2;
        p.setColor(Color.WHITE);p.setAlpha(hurt?190:245);
        // Cloth uses the actual illustrated fabric, but its outer silhouette
        // now bends and tapers with the wind instead of exposing a rectangle.
        c.save();
        Path capeShape=new Path();
        capeShape.moveTo(-40+wind*.36f,-70);
        capeShape.quadTo(-65+wind*.52f,-43,-64+wind*.58f,-8);
        capeShape.lineTo(-55+wind*.66f,9);
        capeShape.lineTo(-35+wind*.33f,2);
        capeShape.lineTo(-14+wind*.22f,-20);
        capeShape.lineTo(-13+wind*.22f,-67);
        capeShape.close();
        c.clipPath(capeShape);
        c.drawBitmap(heroes,
                new Rect(column*HERO_W+17,row*HERO_H+82,
                         column*HERO_W+71,row*HERO_H+184),
                new RectF(-63+wind*.55f,-69,-13+wind*.22f,10),p);
        c.restore();
        p.setAlpha(255);
        // Individually anchored original boots and greaves rotate around their hip.
        paintedLeg(c,heroes,column*HERO_W,row*HERO_H,
                55,112,96,187,ActorGait.leg(phase,running,-1,140),31,hurt);
        paintedLeg(c,heroes,column*HERO_W,row*HERO_H,
                96,111,139,187,ActorGait.leg(phase,running,1,140),34,hurt);
        float lean=ActorGait.torsoLean(phase,running,attack);
        // Keep the ORIGINAL detailed painted armor sheet, but animate the
        // upper torso's breathing weight and anticipatory lean separately
        // from the planted boots. No rectangular pasted portrait layer.
        float breath=running?0f:(float)Math.sin(phase*.72f)*.017f;
        float anticipation=attack<0?0f:
                -(float)Math.sin(Math.PI*Math.min(1,attack))*
                (attack<.5f?2.8f:1.2f);
        anticipation+=WeaponAnimation.anticipation(weaponId,attack);
        c.save();
        c.scale(1f+breath*.4f,1f-breath,0,-53);
        c.rotate(lean+anticipation,0,-39);
        // One feathered painted torso, explicitly excluding the original
        // right-waist weapon. Prevents hard rectangular torso cut seams.
        p.setColor(Color.WHITE);p.setAlpha(hurt?195:255);
        c.drawBitmap(cleanUpperBody(row,column),null,
                new RectF(-57,-132,41,-27),p);
        // Painted central tabard overlaps the hip/greave seam, with its own
        // much smaller cloth motion than the legs. Avoid rectangular cut-lines.
        float cloth=(float)Math.sin(phase*.77f)*(running?4.4f:1.1f);
        if(attack>=0)cloth+=(float)Math.sin(Math.PI*Math.min(1,attack))*6f;
        Rect tabard=new Rect(column*HERO_W+73,row*HERO_H+97,
                column*HERO_W+113,row*HERO_H+176);
        p.setAlpha(hurt?185:255);
        c.save();
        Path tabardShape=new Path();
        tabardShape.moveTo(-17+cloth*.55f,-57);
        tabardShape.lineTo(21+cloth*.18f,-57);
        tabardShape.lineTo(17+cloth*.20f,-6);
        tabardShape.quadTo(6+cloth*.45f,7,-13+cloth*.55f,1);
        tabardShape.close();
        c.clipPath(tabardShape);
        c.drawBitmap(heroes,tabard,
                new RectF(-17+cloth*.55f,-57,22+cloth*.18f,4),p);
        c.restore();
        p.setAlpha(255);c.restore();
        c.restore();
    }

    /**
     * Interpolate adjacent illustrated body poses for the last 22% of a frame.
     * The outer weapon, muzzle and hitbox are NOT double-drawn: they stay in
     * the existing continuous IK combat rig, avoiding detached trails.
     */
    private void drawTweenedHeroBody(Canvas c,int state,boolean left,boolean hurt,
                                    boolean running,float phase,float attack,
                                    float time,float x,float y,int weaponId,
                                    float eventProgress){
        AnimationBlend.State pose=state==2||state==3?
                AnimationBlend.event(eventProgress,4):
                AnimationBlend.loop(time,running?10f:3f,4);
        drawHeroRigFrame(c,state,pose.current,x,y,left,hurt,running,
                phase,attack,weaponId);
        if(pose.shouldBlend()){
            int alpha=Math.max(1,Math.min(112,Math.round(255*pose.nextAlpha)));
            int layer=c.saveLayerAlpha(new RectF(x-100,y-165,x+102,y+22),alpha);
            drawHeroRigFrame(c,state,pose.next,x,y,left,hurt,running,
                    phase,attack,weaponId);
            c.restoreToCount(layer);
        }
    }

    /** Hero's armor and limbs without any externally equipped weapon. */
    public boolean heroBody(Canvas c,float x,float y,float time,float facing,
                            boolean running,float attackProgress,float castProgress,
                            boolean hurt) {
        if(!heroReady())return false;
        int state=castProgress>=0?3:attackProgress>=0?2:running?1:0;
        boolean left=Math.cos(facing)<-.28f;
        drawTweenedHeroBody(c,state,left,hurt,running,
                running?time*11.7f:time*2.2f,attackProgress,
                time,x,y,0,state==2?attackProgress:castProgress);
        return true;
    }

    /** Synchronize the same cleaned hero body with the equipped weapon rig. */
    public boolean hero(Canvas c,float x,float y,float time,float facing,
                        boolean running,float attackProgress,float castProgress,
                        boolean hurt,int weaponId) {
        return hero(c,x,y,time,facing,running,attackProgress,castProgress,
                hurt,weaponId,0);
    }
    /** The actual visible equipment evolves as mastery nodes are purchased. */
    public boolean hero(Canvas c,float x,float y,float time,float facing,
                        boolean running,float attackProgress,float castProgress,
                        boolean hurt,int weaponId,int masteryNodes) {
        if(!heroReady())return false;
        int state=castProgress>=0?3:attackProgress>=0?2:running?1:0;
        boolean left=Math.cos(facing)<-.28f;
        // Six different body anticipations, smooth painterly frame transition;
        // the separately driven hand/weapon is deliberately NOT frame-blended.
        drawTweenedHeroBody(c,state,left,hurt,running,
                running?time*11.7f:time*2.2f,attackProgress,
                time,x,y,weaponId,state==2?attackProgress:castProgress);
        // Keep the transparent combat model visible even if an icon atlas is missing.
        c.save();c.translate(x,y);
        if(left)c.scale(-1,1);
        drawHeldWeapon(c,weaponId,time,attackProgress,castProgress,running,
                facing,masteryNodes);
        c.restore();
        return true;
    }
    /**
     * Wrist-anchored layered rig: the shoulder and elbow move with each
     * weapon class and the drawn gauntlet covers the weapon's actual grip.
     * Uses the original art; full separated skeletal sprites remain future work.
     */
    /**
     * Paint a real illustrated armor fragment along a shoulder-to-elbow or
     * elbow-to-gauntlet bone. This replaces the plain stick/rod impression:
     * the source crop comes from the Warden's metal armor, intentionally
     * avoiding the original right-waist weapon and background.
     */
    private void paintedArmorBone(Canvas c,float ax,float ay,
                                  float bx,float by,boolean forearm){
        if(!heroReady())return;
        float dx=bx-ax,dy=by-ay;
        float len=(float)Math.hypot(dx,dy);
        if(len<3)return;
        float radius=forearm?9:12;
        c.save();
        c.translate(ax,ay);
        c.rotate((float)Math.toDegrees(Math.atan2(dy,dx))-90f);
        Path contour=new Path();
        contour.moveTo(-radius+2,-2);
        contour.quadTo(0,-5,radius-1,-2);
        contour.lineTo(radius,len*.35f);
        contour.lineTo(radius*.68f,len*.79f);
        contour.lineTo(radius*.55f,len+2);
        contour.quadTo(0,len+4,-radius*.62f,len+1);
        contour.lineTo(-radius*.82f,len*.59f);
        contour.close();
        c.clipPath(contour);
        p.setColor(Color.WHITE);p.setAlpha(255);p.setStyle(Paint.Style.FILL);
        Rect texture=forearm?new Rect(109,73,137,111):
                new Rect(102,49,137,100);
        c.drawBitmap(heroes,texture,
                new RectF(-radius-1,-3,radius+1,len+3),p);
        // Local plate edge follows animated bone orientation.
        Paint accent=new Paint(Paint.ANTI_ALIAS_FLAG);
        accent.setStrokeCap(Paint.Cap.ROUND);
        accent.setColor(0x9984a9bb);
        accent.setStrokeWidth(1.8f);
        c.drawLine(-radius+2,3,-radius*.55f,len*.80f,accent);
        accent.setColor(0x99edcf92);
        c.drawLine(radius*.47f,len*.12f,radius*.40f,len*.59f,accent);
        c.restore();
    }

    private void drawHeldWeapon(Canvas c,int weaponId,float time,
                                float attackProgress,float castProgress,
                                boolean running,float facing,int masteryNodes) {
        int id=Math.max(0,Math.min(5,weaponId));
        WeaponRig.Pose pose=WeaponRig.aimedPose(id,attackProgress,castProgress,
                time,running,facing);
        boolean blade=id==1||id==2;
        if(blade && attackProgress>=.07f && attackProgress<.92f){
            // The slash follows the blade's actual moving TIP, not a ring
            // rotating around the body or an unconnected on-screen effect.
            float t=clamp01(attackProgress);
            WeaponRig.Pose previous=WeaponRig.aimedPose(id,Math.max(0,t-.13f),
                    castProgress,time,running,facing);
            WeaponRig.Point a=WeaponRig.tip(id,previous),b=WeaponRig.tip(id,pose);
            Path slash=new Path();
            slash.moveTo(a.x,a.y);
            slash.quadTo((a.x+b.x)*.5f,(a.y+b.y)*.5f-7,b.x,b.y);
            Paint arc=new Paint(Paint.ANTI_ALIAS_FLAG);
            arc.setStyle(Paint.Style.STROKE);
            arc.setStrokeCap(Paint.Cap.ROUND);
            int tint=id==2?0xffff9d55:0xffb2edff;
            arc.setColor(tint);arc.setStrokeWidth(15f*(1-t*.4f));
            arc.setAlpha((int)(185*Math.sin(Math.PI*t)));
            c.drawPath(slash,arc);
            arc.setColor(Color.WHITE);arc.setStrokeWidth(3);
            arc.setAlpha((int)(210*Math.sin(Math.PI*t)));
            c.drawPath(slash,arc);
        }
        // Shoulder joint and segmented pauldron/forearm, with articulated elbow.
        Paint arm=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        arm.setStrokeCap(Paint.Cap.ROUND);
        arm.setStyle(Paint.Style.STROKE);
        arm.setColor(0xff21394b);arm.setStrokeWidth(15);
        c.drawLine(pose.shoulderX,pose.shoulderY,
                pose.elbowX,pose.elbowY,arm);
        c.drawLine(pose.elbowX,pose.elbowY,pose.handX,pose.handY,arm);
        arm.setColor(0xff7c9db1);arm.setStrokeWidth(9);
        c.drawLine(pose.shoulderX-1,pose.shoulderY-1,
                pose.elbowX-1,pose.elbowY-2,arm);
        c.drawLine(pose.elbowX-1,pose.elbowY-2,
                pose.handX-1,pose.handY-1,arm);
        // Animated painterly armor fragments hide generic rod-style arms
        // without painting a second built-in weapon on the character.
        paintedArmorBone(c,pose.shoulderX,pose.shoulderY,
                pose.elbowX,pose.elbowY,false);
        paintedArmorBone(c,pose.elbowX,pose.elbowY,
                pose.handX,pose.handY,true);
        arm.setStyle(Paint.Style.FILL);
        arm.setColor(0xffc2b078);
        c.drawCircle(pose.elbowX,pose.elbowY,5,arm);
        // Real support-hand grip on the large sword, channel staff and lance.
        // It moves WITH the animated main hand, not at a fixed screen coordinate.
        if(id==1||id==3||id==5){
            float supportX=pose.handX-(id==1?10:12);
            float supportY=pose.handY+(id==1?12:8);
            arm.setColor(0xff283a4c);arm.setStrokeWidth(12f);
            arm.setStyle(Paint.Style.STROKE);
            c.drawLine(-20,-86,supportX-5,supportY+2,arm);
            arm.setColor(0xff93b7c1);arm.setStrokeWidth(7);
            c.drawLine(-20,-86,supportX-5,supportY+2,arm);
            paintedArmorBone(c,-20,-86,supportX-5,supportY+2,false);
            arm.setStyle(Paint.Style.FILL);
            arm.setColor(0xffcfbb98);
            c.drawCircle(supportX,supportY,7,arm);
        }
        // Weapon image is positioned by the grip socket, not screen-space sticker.
        c.save();
        c.translate(pose.handX,pose.handY);
        c.rotate(pose.angle);
        // Scale BOTH combat geometry and its mastery markings together.
        // The hit tip from WeaponRig uses this exact same render scale.
        c.scale(WeaponRig.renderScale(id),WeaponRig.renderScale(id));
        // Unlike the inventory bitmap, these in-hand weapon models have
        // transparent backgrounds. Never paint a solid equipment-card tile.
        WorldArt.drawHeldWeapon(c,id,0,0,1f,time);
        // Mastery marks are IN the rotated weapon's local coordinates. They
        // stay glued to the weapon (not a floating HUD ring) across all poses.
        if(masteryNodes>=2) {
            Paint rune=new Paint(Paint.ANTI_ALIAS_FLAG);
            int tint=Progression.WEAPONS[id].color;
            float pulse=.68f+.24f*(float)Math.sin(time*6+id);
            rune.setColor(tint);rune.setAlpha((int)(220*pulse));
            rune.setStyle(Paint.Style.STROKE);rune.setStrokeWidth(2.2f);
            c.drawLine(-3,-11,-3,-29,rune);
            c.drawLine(3,-11,3,-29,rune);
            rune.setStyle(Paint.Style.FILL);
            c.drawCircle(0,-32,2.8f,rune);
            if(masteryNodes>=6) {
                Path etched=new Path();
                etched.moveTo(0,-42);etched.lineTo(7,-35);
                etched.lineTo(0,-27);etched.lineTo(-7,-35);
                etched.close();
                c.drawPath(etched,rune);
                rune.setAlpha(230);
                c.drawCircle(0,-35,3.3f,rune);
            }
            if(masteryNodes>=11) {
                // The final mastered model acquires a pointed colored cutting
                // extension; energy radiates along its actual local blade axis.
                Path crest=new Path();
                crest.moveTo(-8,-30);crest.lineTo(0,-40);
                crest.lineTo(8,-30);crest.lineTo(0,-35);crest.close();
                rune.setColor(tint);rune.setAlpha(200);
                c.drawPath(crest,rune);
                rune.setColor(Color.WHITE);rune.setStyle(Paint.Style.STROKE);
                rune.setStrokeWidth(1.7f);
                c.drawLine(0,-35,0,-39,rune);
            }
        }
        c.restore();
        // The hand is drawn over the handle, so visibly gripping the weapon.
        arm.setColor(0xff293d4e);
        c.drawCircle(pose.handX,pose.handY,9,arm);
        arm.setColor(0xffc3d4d8);
        c.drawCircle(pose.handX-1,pose.handY-2,6,arm);
        arm.setColor(0xffd8b47d);
        c.drawCircle(pose.handX+2,pose.handY+2,2.7f,arm);
        if(attackProgress>=0&&!blade) {
            Paint glow=new Paint(Paint.ANTI_ALIAS_FLAG);
            int color=id==5?0xffaafaff:id==3?0xffaca5ff:0xffffce8c;
            glow.setColor(color);
            glow.setAlpha((int)(170*(1-clamp01(attackProgress))));
            WeaponRig.Point muzzle=WeaponRig.tip(id,pose);
            c.drawCircle(muzzle.x,muzzle.y,11,glow);
            glow.setAlpha(240);
            c.drawCircle(muzzle.x,muzzle.y,3,glow);
            glow.setStyle(Paint.Style.STROKE);
            glow.setStrokeWidth(2);
            c.drawLine(muzzle.x-10,muzzle.y,muzzle.x+10,muzzle.y,glow);
            glow.setStyle(Paint.Style.FILL);
        }
    }

    /**
     * Only sprites with clean separated boot regions can use hinged cutouts.
     * Giant bosses have painted fire plinths, flyers have wings and boars have
     * four legs: these models need dedicated rigs instead of generic boot cuts.
     */
    public static boolean segmentedEnemyLegs(int kind) {
        return kind==0||kind==1||kind==2||kind==4;
    }

    /**
     * A flying imp uses the existing illustrated source actor as its body,
     * with separately animated painted membrane wings. This replaces the old
     * generic purple cylinder fallback with a recognisable living creature.
     */
    private void wing(Canvas c,int side,float time) {
        float flap=(float)Math.sin(time*10f)*23f;
        float tipX=side*(72+flap*.31f);
        float tipY=-92+flap;
        float rootX=side*19;
        Path membrane=new Path();
        membrane.moveTo(rootX,-58);
        membrane.quadTo(side*47,-88,tipX,tipY);
        membrane.lineTo(side*(63+flap*.18f),-42+flap*.43f);
        membrane.lineTo(side*47,-22);
        membrane.quadTo(side*23,-35,rootX,-34);
        membrane.close();
        Paint wing=new Paint(Paint.ANTI_ALIAS_FLAG);
        wing.setShader(new LinearGradient(rootX,-45,tipX,tipY,
                0xff292447,0xff7059a8,Shader.TileMode.CLAMP));
        c.drawPath(membrane,wing);
        wing.setShader(null);
        wing.setStrokeCap(Paint.Cap.ROUND);
        wing.setStrokeWidth(4);
        wing.setColor(0xffb39cd2);
        wing.setStyle(Paint.Style.STROKE);
        c.drawPath(membrane,wing);
        wing.setColor(0xff675585);wing.setStrokeWidth(3);
        c.drawLine(rootX,-56,tipX,tipY,wing);
        c.drawLine(rootX,-53,side*(63+flap*.18f),-42+flap*.43f,wing);
        wing.setStyle(Paint.Style.FILL);
    }

    public boolean enemy(Canvas c,int kind,float x,float y,float time,
                         boolean moving,float attackProgress,boolean hurt) {
        if(!enemiesReady()||kind<0||kind>6)return false;
        if(kind==3){
            AnimationBlend.State frames=attackProgress>=0?
                    AnimationBlend.event(attackProgress,4):
                    AnimationBlend.loop(time,moving?12f:4f,4);
            int frame=(attackProgress>=0?4:0)+frames.current;
            Rect src=new Rect(frame*ENEMY_W,3*ENEMY_H,
                    (frame+1)*ENEMY_W,4*ENEMY_H);
            c.save();c.translate(x,y);
            Paint shadow=new Paint(Paint.ANTI_ALIAS_FLAG);
            shadow.setColor(0x66091824);
            c.drawOval(-24,-2,25,10,shadow);
            c.translate(0,-9+(float)Math.sin(time*5)*5);
            wing(c,-1,time);wing(c,1,time+0.03f);
            p.setColor(Color.WHITE);p.setAlpha(hurt?185:255);
            // Reuse painted armor and face; do not substitute a geometry stick man.
            c.drawBitmap(monsters,src,new RectF(-36,-78,36,4),p);
            if(frames.shouldBlend()){
                Rect next=new Rect((frame-frames.current+frames.next)*ENEMY_W,
                        3*ENEMY_H,(frame-frames.current+frames.next+1)*ENEMY_W,
                        4*ENEMY_H);
                p.setAlpha(Math.round((hurt?185:255)*frames.nextAlpha));
                c.drawBitmap(monsters,next,new RectF(-36,-78,36,4),p);
            }
            p.setAlpha(255);
            if(attackProgress>=0){
                Paint magic=new Paint(Paint.ANTI_ALIAS_FLAG);
                magic.setColor(0x88c6a4ff);
                float size=10+7*(float)Math.sin(Math.PI*attackProgress);
                c.drawCircle(25,-42,size,magic);
                magic.setColor(0xffefe3ff);
                c.drawCircle(25,-42,4,magic);
            }
            c.restore();return true;
        }
        AnimationBlend.State frames=attackProgress>=0?
                AnimationBlend.event(attackProgress,4):
                AnimationBlend.loop(time,moving?11f:2.2f,4);
        int frame=(attackProgress>=0?4:0)+frames.current;
        Rect src=new Rect(frame*ENEMY_W,kind*ENEMY_H,
                (frame+1)*ENEMY_W,(kind+1)*ENEMY_H);
        Rect nextSrc=new Rect((frame-frames.current+frames.next)*ENEMY_W,
                kind*ENEMY_H,(frame-frames.current+frames.next+1)*ENEMY_W,
                (kind+1)*ENEMY_H);
        float width=kind==5?108:kind==2?79:kind==6?76:57;
        float height=kind==5?127:kind==2?91:kind==6?77:68;
        c.save();c.translate(x,y);
        p.setColor(0xbb000000);p.setAlpha(100);
        c.drawOval(new RectF(-width*.35f,-4,width*.35f,10),p);
        if(segmentedEnemyLegs(kind)) {
            // The original species armor/boots move as independent limb crops.
            float step=height*.93f;
            paintedLeg(c,monsters,src.left,src.top,21,82,60,126,
                    ActorGait.leg(time,moving,-1,step),width*.48f,hurt);
            paintedLeg(c,monsters,src.left,src.top,58,82,101,126,
                    ActorGait.leg(time,moving,1,step),width*.51f,hurt);
            Rect upper=new Rect(src.left,src.top,src.right,
                    src.top+(int)(ENEMY_H*.74f));
            c.save();
            float bodyLean=ActorGait.torsoLean(time,moving,attackProgress);
            c.rotate(bodyLean+AnimationBlend.enemyAttackLean(kind,attackProgress),
                    0,-height*.23f);
            c.translate(0,AnimationBlend.enemyBreath(kind,time,moving));
            p.setColor(Color.WHITE);p.setAlpha(hurt?188:255);
            RectF chest=new RectF(-width/2,-height,width/2,-height*.22f);
            c.drawBitmap(monsters,upper,chest,p);
            if(frames.shouldBlend()){
                Rect nextChest=new Rect(nextSrc.left,nextSrc.top,
                        nextSrc.right,nextSrc.top+(int)(ENEMY_H*.74f));
                p.setAlpha(Math.round((hurt?188:255)*frames.nextAlpha));
                c.drawBitmap(monsters,nextChest,chest,p);
            }
            c.restore();
        }else{
            // Flying and four-legged creatures keep their own authentic frames.
            p.setColor(Color.WHITE);p.setAlpha(hurt?190:255);
            float bob=AnimationBlend.enemyBreath(kind,time,moving);
            RectF target=new RectF(-width/2,-height+bob,width/2,6+bob);
            c.drawBitmap(monsters,src,target,p);
            if(frames.shouldBlend()){
                p.setAlpha(Math.round((hurt?190:255)*frames.nextAlpha));
                c.drawBitmap(monsters,nextSrc,target,p);
            }
        }
        p.setAlpha(255);
        c.restore();
        return true;
    }
}
