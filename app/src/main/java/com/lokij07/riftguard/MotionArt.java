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
 * Weapon is a separate PNG/WebP attachment, never baked into the hero's sprite.
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
        if(!heroReady())return;
        int row=Math.max(0,Math.min(3,state));
        int column=Math.max(0,Math.min(3,frame));
        Rect src=new Rect(column*HERO_W,row*HERO_H,(column+1)*HERO_W,(row+1)*HERO_H);
        c.save();
        c.translate(x,y);
        if(flip)c.scale(-1,1);
        p.setColor(0x8807131b);p.setAlpha(110);
        c.drawOval(new RectF(-32,-4,34,12),p);
        p.setColor(Color.WHITE);p.setAlpha(hurt?175:255);
        c.drawBitmap(heroes,src,new RectF(-57,-132,57,8),p);
        p.setAlpha(255);
        c.restore();
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
        float tilt=-(float)Math.toDegrees(Math.atan2(dx,dy));
        c.save();
        c.translate(bone.hipX,bone.hipY);
        c.rotate(tilt);
        Rect source=new Rect(frameLeft+cropLeft,frameTop+cropTop,
                frameLeft+cropRight,frameTop+cropBottom);
        p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);
        p.setAlpha(hurt?195:255);
        c.drawBitmap(atlas,source,
                new RectF(-width*.5f,-8,width*.5f,dy+6),p);
        p.setAlpha(255);
        c.restore();
    }
    private void drawHeroRigFrame(Canvas c,int state,int frame,float x,float y,
                                  boolean flip,boolean hurt,boolean running,float phase,
                                  float attack) {
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
        c.drawBitmap(heroes,
                new Rect(column*HERO_W+17,row*HERO_H+82,
                         column*HERO_W+71,row*HERO_H+184),
                new RectF(-63+wind*.55f,-69,-13+wind*.22f,10),p);
        p.setAlpha(255);
        // Individually anchored original boots and greaves rotate around their hip.
        paintedLeg(c,heroes,column*HERO_W,row*HERO_H,
                55,112,96,187,ActorGait.leg(phase,running,-1,140),31,hurt);
        paintedLeg(c,heroes,column*HERO_W,row*HERO_H,
                96,111,139,187,ActorGait.leg(phase,running,1,140),34,hurt);
        float lean=ActorGait.torsoLean(phase,running,attack);
        c.save();c.rotate(lean,0,-39);
        Rect src=new Rect(column*HERO_W,row*HERO_H,
                (column+1)*HERO_W,row*HERO_H+122);
        p.setColor(Color.WHITE);p.setAlpha(hurt?195:255);
        c.drawBitmap(heroes,src,new RectF(-57,-132,57,-27),p);
        p.setAlpha(255);c.restore();
        c.restore();
    }

    /** Synchronize movement, attack and spell frames with the real simulation. */
    public boolean hero(Canvas c,float x,float y,float time,float facing,
                        boolean running,float attackProgress,float castProgress,
                        boolean hurt,int weaponId) {
        if(!heroReady())return false;
        int state=castProgress>=0?3:attackProgress>=0?2:running?1:0;
        int frame=state==2?eventFrame(attackProgress):state==3?
                eventFrame(castProgress):frame(time,state==1?10:3,4);
        boolean left=Math.cos(facing)<-.28f;
        drawHeroRigFrame(c,state,frame,x,y,left,hurt,running,time*11.7f,
                attackProgress);
        if(weaponsReady()){
            c.save();c.translate(x,y);
            if(left)c.scale(-1,1);
            drawHeldWeapon(c,weaponId,time,attackProgress,castProgress,running);
            c.restore();
        }
        return true;
    }
    /**
     * Wrist-anchored layered rig: the shoulder and elbow move with each
     * weapon class and the drawn gauntlet covers the weapon's actual grip.
     * Uses the original art; full separated skeletal sprites remain future work.
     */
    private void drawHeldWeapon(Canvas c,int weaponId,float time,
                                float attackProgress,float castProgress,
                                boolean running) {
        int id=Math.max(0,Math.min(5,weaponId));
        WeaponRig.Pose pose=WeaponRig.pose(id,attackProgress,castProgress,time,running);
        boolean blade=id==1||id==2;
        if(blade && attackProgress>=.07f && attackProgress<.92f){
            float t=clamp01(attackProgress);
            Paint arc=new Paint(Paint.ANTI_ALIAS_FLAG);
            arc.setStyle(Paint.Style.STROKE);
            arc.setStrokeCap(Paint.Cap.ROUND);
            int tint=id==2?0xffff9d55:0xffb2edff;
            arc.setStrokeWidth(10f*(1-t*.35f));
            arc.setColor(tint);
            arc.setAlpha((int)(180*Math.sin(Math.PI*t)));
            c.drawArc(-76,-143,89,25,-192+171*t,100,false,arc);
            arc.setStrokeWidth(3);
            arc.setColor(Color.WHITE);
            arc.setAlpha((int)(180*Math.sin(Math.PI*t)));
            c.drawArc(-80,-145,91,27,-192+171*t,94,false,arc);
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
        arm.setStyle(Paint.Style.FILL);
        arm.setColor(0xffc2b078);
        c.drawCircle(pose.elbowX,pose.elbowY,5,arm);
        // Weapon image is positioned by the grip socket, not screen-space sticker.
        c.save();
        c.translate(pose.handX,pose.handY);
        c.rotate(pose.angle);
        Rect source=new Rect(id*WEAPON_W,0,(id+1)*WEAPON_W,WEAPON_H);
        p.setColor(Color.WHITE);p.setAlpha(255);
        // Lower portion of the atlas sprite is the handle/grip.
        c.drawBitmap(weapons,source,new RectF(-19,-77,19,9),p);
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
            c.drawCircle(pose.handX+17,pose.handY-54,14,glow);
            glow.setAlpha(240);
            c.drawCircle(pose.handX+17,pose.handY-54,4,glow);
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

    /** Returns false for the winged monster, which still has its own flapping rig. */
    public boolean enemy(Canvas c,int kind,float x,float y,float time,
                         boolean moving,float attackProgress,boolean hurt) {
        if(!enemiesReady()||kind==3||kind<0||kind>6)return false;
        int frame;
        if(attackProgress>=0)frame=4+eventFrame(attackProgress);
        else frame=frame(time,moving?11:2.2f,4);
        Rect src=new Rect(frame*ENEMY_W,kind*ENEMY_H,
                (frame+1)*ENEMY_W,(kind+1)*ENEMY_H);
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
            c.rotate(bodyLean,0,-height*.23f);
            p.setColor(Color.WHITE);p.setAlpha(hurt?188:255);
            c.drawBitmap(monsters,upper,
                    new RectF(-width/2,-height,width/2,-height*.22f),p);
            c.restore();
        }else{
            // Flying and four-legged creatures keep their own authentic frames.
            p.setColor(Color.WHITE);p.setAlpha(hurt?190:255);
            c.drawBitmap(monsters,src,new RectF(-width/2,-height,width/2,6),p);
        }
        p.setAlpha(255);
        c.restore();
        return true;
    }
}
