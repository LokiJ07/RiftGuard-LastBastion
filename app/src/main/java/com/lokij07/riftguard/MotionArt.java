package com.lokij07.riftguard;

import android.content.res.Resources;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
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

    /** Synchronize movement, attack and spell frames with the real simulation. */
    public boolean hero(Canvas c,float x,float y,float time,float facing,
                        boolean running,float attackProgress,float castProgress,
                        boolean hurt,int weaponId) {
        if(!heroReady())return false;
        int state=castProgress>=0?3:attackProgress>=0?2:running?1:0;
        int frame=state==2?eventFrame(attackProgress):state==3?
                eventFrame(castProgress):frame(time,state==1?10:3,4);
        boolean left=Math.cos(facing)<-.28f;
        drawHeroFrame(c,state,frame,x,y,left,hurt);
        if(weaponsReady()){
            c.save();c.translate(x,y);
            if(left)c.scale(-1,1);
            drawHeldWeapon(c,weaponId,time,attackProgress,castProgress);
            c.restore();
        }
        return true;
    }
    private void drawHeldWeapon(Canvas c,int weaponId,float time,
                                float attackProgress,float castProgress) {
        int id=Math.max(0,Math.min(5,weaponId));
        boolean melee=id==1||id==2;
        float angle=melee?15f:-17f;
        if(melee&&attackProgress>=0){
            float swing=clamp01(attackProgress);
            angle=-108+208*(swing*swing*(3-2*swing));
            if(swing>.07f&&swing<.92f){
                Paint arc=new Paint(Paint.ANTI_ALIAS_FLAG);
                arc.setStyle(Paint.Style.STROKE);
                arc.setStrokeCap(Paint.Cap.ROUND);
                int tint=id==2?0xffff9d55:0xffb2edff;
                arc.setStrokeWidth(10f*(1-swing*.35f));
                arc.setColor(tint);
                arc.setAlpha((int)(180*Math.sin(Math.PI*swing)));
                c.drawArc(-74,-113,77,35,-190+165*swing,95,false,arc);
                arc.setStrokeWidth(3);
                arc.setColor(Color.WHITE);
                arc.setAlpha((int)(180*Math.sin(Math.PI*swing)));
                c.drawArc(-77,-116,78,36,-190+165*swing,89,false,arc);
            }
        }else if(attackProgress>=0) {
            angle=-37+(float)Math.sin(clamp01(attackProgress)*Math.PI)*27;
        }
        if(castProgress>=0) angle=-30+50*(float)Math.sin(Math.PI*clamp01(castProgress));
        angle+=(float)Math.sin(time*2.1f)*2.5f;
        c.save();
        c.translate(25,-48);
        c.rotate(angle);
        Rect source=new Rect(id*WEAPON_W,0,(id+1)*WEAPON_W,WEAPON_H);
        // Held art shares a pivot near the gauntlet and rotates independently.
        c.drawBitmap(weapons,source,new RectF(-20,-74,20,12),p);
        c.restore();
        if(attackProgress>=0&&!melee) {
            Paint glow=new Paint(Paint.ANTI_ALIAS_FLAG);
            int color=id==5?0xffaafaff:id==3?0xffaca5ff:0xffffce8c;
            glow.setColor(color);
            glow.setAlpha((int)(170*(1-clamp01(attackProgress))));
            c.drawCircle(37,-103,13,glow);
            glow.setAlpha(240);
            c.drawCircle(37,-103,4,glow);
        }
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
        p.setColor(Color.WHITE);p.setAlpha(hurt?190:255);
        c.drawBitmap(monsters,src,new RectF(-width/2,-height,width/2,6),p);
        p.setAlpha(255);
        c.restore();
        return true;
    }
}
