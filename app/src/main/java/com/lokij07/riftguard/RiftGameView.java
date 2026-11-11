package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Original mobile canvas game: hero combat, turret construction, siege AI,
 * 300 deterministic campaign battlefields, touch controls and persistent saves.
 * Designed as a performant playable foundation; full illustrated assets follow later.
 */
public final class RiftGameView extends View {
    private static final float W=1280f, H=720f;
    private static final float WORLD_W=1900f, WORLD_H=1050f;
    private static final float BASE_X=950f, BASE_Y=510f;
    private static final Typeface FONT=Typeface.create("sans-serif-condensed",Typeface.BOLD);
    private static final int INK=Color.rgb(233,243,233);
    private static final int GOLD=Color.rgb(255,203,104);
    private static final int[] GROUNDS={
        0xff244b43,0xff263e39,0xff3e5340,0xff6f4536,0xff3d5363,
        0xff484c54,0xff334e59,0xff334765,0xff423847,0xff453242
    };
    private static final int[] GLOWS={
        0xff95d38d,0xffb1dc92,0xff9fc58a,0xffffae69,0xffb7d6ed,
        0xffffbb87,0xffa7eaff,0xffc1a5ff,0xffee97b9,0xffff8d77
    };
    private enum Scene { HOME, MAP, ARMORY, FORTRESS, PLAY, WIN, LOSE }
    private Scene scene=Scene.HOME;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SharedPreferences prefs;
    private final Random random=new Random(13);
    private final List<Enemy> enemies=new ArrayList<>();
    private final List<Shot> shots=new ArrayList<>();
    private final List<Spark> sparks=new ArrayList<>();
    private final List<Decor> decor=new ArrayList<>();
    private final int[] towers=new int[4];
    private final float[] towerTimers=new float[4];
    private final float[] towerX={810,1092,811,1093};
    private final float[] towerY={370,370,656,656};
    private int unlocked;
    private int supplies, fortressRank, equippedWeapon;
    private int selectedLevel=1, selectedMap=0, stage=0;
    private int wave, remaining, gold, kills;
    private float baseHp, heroHp, heroX, heroY, heroDir=-1.57f, spawnTimer, intermission, waveNotice;
    private float shotTimer, pulseCooldown, pulseFlash, hurtCooldown, fireFlash, noticeTimer, gameTime;
    private String notice="";
    private boolean waiting, paused, active=true, firing;
    private int joyPointer=-1, attackPointer=-1;
    private float axisX=0, axisY=0;
    private long lastNs=0;

    private static final class Enemy {
        float x,y,hp,maxHp,phase,attackTimer;
        int kind;
        Enemy(float x,float y,int kind,float hp,float phase) {
            this.x=x; this.y=y; this.kind=kind; this.hp=hp; this.maxHp=hp; this.phase=phase;
        }
    }
    private static final class Shot {
        float x,y,speed,damage,life;
        boolean tower;
        int weaponId=-1;
        Enemy target;
        Shot(float x,float y,float damage,float speed,Enemy target,boolean tower) {
            this.x=x; this.y=y; this.damage=damage; this.speed=speed; this.target=target;
            this.tower=tower; this.life=2.8f;
        }
    }
    private static final class Spark {
        float x,y,vx,vy,life,maxLife;
        int color;
        Spark(float x,float y,float vx,float vy,float life,int color) {
            this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.life=life;this.maxLife=life;this.color=color;
        }
    }
    private static final class Decor {
        float x,y,size; int kind;
        Decor(float x,float y,float size,int kind) { this.x=x;this.y=y;this.size=size;this.kind=kind; }
    }

    public RiftGameView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE,null);
        prefs=context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        unlocked=Math.max(0,Math.min(Campaign.STAGES,prefs.getInt("unlocked",0)));
        supplies=Math.max(0,prefs.getInt("supplies",0));
        fortressRank=Math.max(1,Math.min(Progression.MAX_RANK,prefs.getInt("fortress_rank",1)));
        equippedWeapon=prefs.getInt("weapon",0);
        if(!Progression.canEquip(fortressRank,equippedWeapon))equippedWeapon=0;
        selectedLevel=Campaign.level(Math.min(unlocked,299));
        setFocusable(true);
    }
    private void clearTouchControls() {
        joyPointer=-1;
        attackPointer=-1;
        axisX=axisY=0;
        firing=false;
    }
    public void setActive(boolean value) {
        active=value;
        lastNs=0;
        if (!value) clearTouchControls();
        if (active) postInvalidateOnAnimation();
    }
    public boolean handleBack() {
        if (scene==Scene.PLAY) {
            paused=!paused;
            clearTouchControls();
            return true;
        }
        if (scene!=Scene.HOME) { scene=Scene.HOME; return true; }
        return false;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now=System.nanoTime();
        float dt=lastNs==0 ? 0 : Math.min(.045f,(now-lastNs)/1000000000f);
        lastNs=now;
        if (active) {
            gameTime+=dt;
            if (scene==Scene.PLAY && !paused) update(dt);
        }
        canvas.drawColor(0xff101e22);
        float scale=Math.min(getWidth()/W,getHeight()/H);
        if (scale<=0) return;
        canvas.save();
        canvas.translate((getWidth()-W*scale)*.5f,(getHeight()-H*scale)*.5f);
        canvas.scale(scale,scale);
        if (scene==Scene.HOME) drawHome(canvas);
        else if (scene==Scene.MAP) drawMap(canvas);
        else if (scene==Scene.ARMORY) drawArmory(canvas);
        else if (scene==Scene.FORTRESS) drawFortress(canvas);
        else {
            drawBattle(canvas);
            if (scene==Scene.WIN || scene==Scene.LOSE) drawResult(canvas);
            else if (paused) drawPause(canvas);
        }
        canvas.restore();
        if (active) postInvalidateOnAnimation();
    }

    private void start(int s) {
        Campaign.checkStage(s);
        stage=s; scene=Scene.PLAY; paused=false;
        heroX=BASE_X; heroY=BASE_Y+180; heroHp=100;
        baseHp=Progression.maxCoreHp(fortressRank);
        wave=1; remaining=Campaign.enemies(stage,wave);
        spawnTimer=3.2f; intermission=0; waiting=false;
        shotTimer=0; pulseCooldown=0; pulseFlash=0; hurtCooldown=0; fireFlash=0;
        waveNotice=2.6f; noticeTimer=0; gold=150+(fortressRank-1)*20; kills=0;
        enemies.clear();shots.clear();sparks.clear();decor.clear();
        for (int i=0;i<towers.length;i++){towers[i]=0;towerTimers[i]=0;}
        towers[0]=1;
        if(fortressRank>=4)towers[1]=1;
        if(fortressRank>=5)towers[2]=1;
        Random r=new Random(Campaign.seed(stage));
        for (int i=0;i<125;i++) {
            float x=50+r.nextFloat()*(WORLD_W-100);
            float y=50+r.nextFloat()*(WORLD_H-100);
            if (distance(x,y,BASE_X,BASE_Y)<250) continue;
            if (distance(x,y,heroX,heroY)<95) continue;
            decor.add(new Decor(x,y, .65f+r.nextFloat()*.8f,r.nextInt(6)));
        }
        random.setSeed(Campaign.seed(stage)^0xDDEEFF);
        clearTouchControls();
    }

    private void update(float dt) {
        shotTimer-=dt; pulseCooldown=Math.max(0,pulseCooldown-dt);
        pulseFlash=Math.max(0,pulseFlash-dt);
        hurtCooldown=Math.max(0,hurtCooldown-dt);
        fireFlash=Math.max(0,fireFlash-dt);
        waveNotice=Math.max(0,waveNotice-dt);
        noticeTimer=Math.max(0,noticeTimer-dt);
        float speed=245f;
        // Independent X/Y collision resolution allows sliding along walls.
        float nextX=MovementRules.heroNextX(heroX,heroY,axisX*speed*dt);
        if (!hitsBuiltTower(nextX,heroY)) heroX=nextX;
        float nextY=MovementRules.heroNextY(heroX,heroY,axisY*speed*dt);
        if (!hitsBuiltTower(heroX,nextY)) heroY=nextY;
        if (Math.abs(axisX)+Math.abs(axisY)>.1f) heroDir=(float)Math.atan2(axisY,axisX);
        if (firing) fire();
        if (!waiting) {
            spawnTimer-=dt;
            if (remaining>0 && spawnTimer<=0) {
                spawnEnemy();
                remaining--;
                spawnTimer=Math.max(.23f,1.1f-stage*.0016f);
            }
        }
        for (int i=0;i<towers.length;i++) {
            towerTimers[i]-=dt;
            if (towers[i]>0 && towerTimers[i]<=0) {
                Enemy target=closest(towerX[i],towerY[i],280+towers[i]*35);
                if (target!=null) {
                    shots.add(new Shot(towerX[i],towerY[i]-25,BattleRules.towerDamage(towers[i]),
                            650,target,true));
                    towerTimers[i]=Math.max(.26f,.95f-towers[i]*.12f);
                    puff(towerX[i],towerY[i],3,GOLD);
                }
            }
        }
        Iterator<Enemy> it=enemies.iterator();
        while(it.hasNext()) {
            Enemy e=it.next();
            if(e.hp<=0) {
                gold+=BattleRules.killCoins(e.kind);
                kills++;
                puff(e.x,e.y,9,GLOWS[Campaign.world(stage)]);
                it.remove();continue;
            }
            float dHero=distance(e.x,e.y,heroX,heroY);
            boolean hunt=MovementRules.huntsHero(dHero,e.kind);
            float tx=hunt?heroX:BASE_X,ty=hunt?heroY:BASE_Y;
            float d=distance(e.x,e.y,tx,ty);
            float stop=MovementRules.enemyStopDistance(hunt,e.kind);
            if(d>stop) {
                float speedFactor=(e.kind==3?1.15f:1f);
                float step=Math.min(d-stop,BattleRules.enemySpeed(e.kind)*speedFactor*dt);
                e.x+=(tx-e.x)/Math.max(1,d)*step;
                e.y+=(ty-e.y)/Math.max(1,d)*step;
                e.phase+=dt*7;
            } else {
                e.phase+=dt*1.3f;
            }
            // Ground monsters are blocked by the fortifications.
            // Flying enemies can legitimately bypass the perimeter.
            float wallDx=e.x-BASE_X, wallDy=e.y-BASE_Y;
            if(e.kind!=3 && Math.abs(wallDx)<155 && Math.abs(wallDy)<134) {
                if(155-Math.abs(wallDx)<134-Math.abs(wallDy))
                    e.x=BASE_X+(wallDx<0?-155:155);
                else
                    e.y=BASE_Y+(wallDy<0?-134:134);
            }
            if (distance(e.x,e.y,heroX,heroY)<44 && hurtCooldown<=0) {
                heroHp-=e.kind==2?20:11;
                hurtCooldown=.8f; puff(heroX,heroY,6,0xffff8086);
                if(heroHp<=0){heroHp=0;scene=Scene.LOSE;return;}
            }
            if(!hunt && (distance(e.x,e.y,BASE_X,BASE_Y)<190 || (e.kind==5 &&
                    distance(e.x,e.y,BASE_X,BASE_Y)<275))) {
                e.attackTimer-=dt;
                if(e.attackTimer<=0) {
                    baseHp-=e.kind==2?30:(e.kind==5?18:12);
                    e.attackTimer=e.kind==2?1.7f:2.2f;
                    puff(BASE_X,BASE_Y,6,0xffff9f74);
                    if(baseHp<=0){baseHp=0;scene=Scene.LOSE;return;}
                }
            }
        }
        Iterator<Shot> sit=shots.iterator();
        while(sit.hasNext()) {
            Shot shot=sit.next();shot.life-=dt;
            if(shot.life<=0 || shot.target.hp<=0){sit.remove();continue;}
            float d=distance(shot.x,shot.y,shot.target.x,shot.target.y-10);
            if(d<24) {
                shot.target.hp-=shot.damage;
                int impact=shot.tower?GOLD:Progression.WEAPONS[shot.weaponId].color;
                if(shot.weaponId==3) {
                    // Arc Staff chains lightning to a second nearby monster.
                    for(Enemy nearby:enemies) {
                        if(nearby!=shot.target && nearby.hp>0 &&
                                distance(nearby.x,nearby.y,shot.target.x,shot.target.y)<110) {
                            nearby.hp-=shot.damage*.52f;
                            puff(nearby.x,nearby.y,6,impact);
                            break;
                        }
                    }
                } else if(shot.weaponId==5) {
                    // Frost shards also splash nearby attackers.
                    for(Enemy nearby:enemies) if(nearby!=shot.target && nearby.hp>0 &&
                            distance(nearby.x,nearby.y,shot.target.x,shot.target.y)<65)
                        nearby.hp-=shot.damage*.25f;
                }
                puff(shot.target.x,shot.target.y,7,impact);
                sit.remove();
            } else {
                shot.x+=(shot.target.x-shot.x)/Math.max(1,d)*shot.speed*dt;
                shot.y+=(shot.target.y-10-shot.y)/Math.max(1,d)*shot.speed*dt;
            }
        }
        Iterator<Spark> pit=sparks.iterator();
        while(pit.hasNext()) {
            Spark s=pit.next();s.life-=dt;
            if(s.life<=0){pit.remove();continue;}
            s.x+=s.vx*dt;s.y+=s.vy*dt;s.vy+=42*dt;
        }
        if(remaining==0 && enemies.isEmpty()) {
            if(!waiting){waiting=true;intermission=2.5f;gold+=25;waveNotice=2.5f;}
            else {
                intermission-=dt;
                if(intermission<=0) {
                    if(wave>=Campaign.waves(stage)){complete();return;}
                    wave++;remaining=Campaign.enemies(stage,wave);
                    spawnTimer=1.6f;waveNotice=2;waiting=false;
                }
            }
        }
    }
    private void spawnEnemy() {
        float angle=(float)(random.nextFloat()*Math.PI*2);
        float radius=700+random.nextFloat()*180;
        float x=BattleRules.clamp(BASE_X+(float)Math.cos(angle)*radius,35,WORLD_W-35);
        float y=BattleRules.clamp(BASE_Y+(float)Math.sin(angle)*radius,35,WORLD_H-35);
        int pool=Math.min(6,2+Campaign.world(stage)/2+(wave>=3?1:0));
        int kind=random.nextInt(Math.max(1,pool));
        if (wave==Campaign.waves(stage) && remaining==1 && Campaign.map(stage)==2) kind=2;
        float hp=BattleRules.enemyHealth(stage,kind);
        if (wave==Campaign.waves(stage) && remaining==1 && Campaign.map(stage)==2) hp*=4;
        enemies.add(new Enemy(x,y,kind,hp,random.nextFloat()*6.28f));
    }
    private Enemy closest(float x,float y,float range) {
        Enemy pick=null;float min=range*range;
        for(Enemy e:enemies) {
            if(e.hp<=0)continue;
            float dx=e.x-x,dy=e.y-y,d=dx*dx+dy*dy;
            if(d<min){min=d;pick=e;}
        }
        return pick;
    }
    private void fire() {
        if (shotTimer>0 || scene!=Scene.PLAY) return;
        Progression.Weapon weapon=Progression.WEAPONS[equippedWeapon];
        Enemy target=closest(heroX,heroY,weapon.range);
        if(target==null) { shotTimer=Math.min(.2f,weapon.cooldown); return; }
        heroDir=(float)Math.atan2(target.y-heroY,target.x-heroX);
        shotTimer=weapon.cooldown;
        fireFlash=.23f;
        if("MELEE".equals(weapon.type)) {
            // Sweeping blade hits every creature within its physical reach.
            for(Enemy e:enemies) if(e.hp>0
                    && distance(e.x,e.y,heroX,heroY)<weapon.range) {
                e.hp-=weapon.damage;
                puff(e.x,e.y,7,weapon.color);
            }
        } else {
            Shot shot=new Shot(heroX,heroY-18,weapon.damage,
                    equippedWeapon==4?900:730,target,false);
            shot.weaponId=equippedWeapon;
            shots.add(shot);
        }
        puff(heroX,heroY,5,weapon.color);
    }
    private void pulse() {
        if(pulseCooldown>0)return;
        pulseCooldown=11; pulseFlash=.55f;
        int hits=0;
        for(Enemy e:enemies) if(distance(e.x,e.y,heroX,heroY)<235) {
            e.hp-=70+Campaign.world(stage)*15;
            float dx=e.x-heroX,dy=e.y-heroY,d=Math.max(1,(float)Math.hypot(dx,dy));
            e.x=BattleRules.clamp(e.x+dx/d*75,20,WORLD_W-20);
            e.y=BattleRules.clamp(e.y+dy/d*75,20,WORLD_H-20);
            hits++;
        }
        puff(heroX,heroY,22,0xff97f9ff);
        announce(hits==0?"ARC PULSE":"ARC PULSE  •  "+hits+" HIT");
    }
    private boolean hitsBuiltTower(float x,float y) {
        for (int i=0;i<towers.length;i++)
            if (towers[i]>0 && distance(x,y,towerX[i],towerY[i])<29) return true;
        return false;
    }
    private void build() {
        int choice=-1;
        for(int i=0;i<towers.length;i++) {
            if(choice==-1 || towers[i]<towers[choice]) choice=i;
        }
        if(choice<0 || towers[choice]>=BattleRules.MAX_TOWER_TIER) {
            announce("ALL TOWERS MAXED");return;
        }
        int cost=BattleRules.towerCost(towers[choice]);
        if(gold<cost){announce("NEED "+(cost-gold)+" MORE GOLD");return;}
        gold-=cost;towers[choice]++;
        puff(towerX[choice],towerY[choice],18,GOLD);
        announce(towers[choice]==1?"TURRET BUILT":"TURRET LEVEL "+towers[choice]);
    }
    private void complete() {
        scene=Scene.WIN;paused=false;
        boolean firstClear=stage>=unlocked;
        supplies+=Progression.stageReward(stage,firstClear);
        unlocked=Campaign.unlockedAfter(unlocked,stage);
        prefs.edit().putInt("unlocked",unlocked).putInt("supplies",supplies).apply();
        selectedLevel=Campaign.level(Math.min(stage+1,299));
        selectedMap=Campaign.map(Math.min(stage+1,299));
    }
    private void announce(String text) {notice=text;noticeTimer=1.7f;}
    private void puff(float x,float y,int count,int color) {
        for(int i=0;i<count && sparks.size()<380;i++) {
            float a=random.nextFloat()*6.28318f,v=45+random.nextFloat()*180;
            sparks.add(new Spark(x,y,(float)Math.cos(a)*v,(float)Math.sin(a)*v,
                    .25f+random.nextFloat()*.42f,color));
        }
    }
    private static float distance(float x,float y,float a,float b) {
        return (float)Math.hypot(x-a,y-b);
    }

    private void drawBattle(Canvas c) {
        int biome=Campaign.world(stage);
        float camX=BattleRules.clamp(heroX,W*.5f,WORLD_W-W*.5f);
        float camY=BattleRules.clamp(heroY,H*.5f,WORLD_H-H*.5f);
        c.save();c.translate(W*.5f-camX,H*.5f-camY);
        drawTerrain(c,biome);
        for(Decor d:decor) drawDecor(c,d,biome);
        drawBase(c,biome);
        for(int i=0;i<towers.length;i++)drawTower(c,towerX[i],towerY[i],towers[i],biome);
        for(Enemy e:enemies)drawEnemy(c,e);
        drawHero(c);
        for(Shot s:shots){
            int boltColor=s.tower?GOLD:Progression.WEAPONS[s.weaponId].color;
            circle(c,s.x,s.y,s.weaponId==5?16:11,boltColor&0x77ffffff);
            circle(c,s.x,s.y,s.weaponId==5?8:5,boltColor);
            if(s.weaponId==4 || s.weaponId==0)
                line(c,s.x-10,s.y+10,s.x+7,s.y-9,0xfff6ead9,3);
        }
        for(Spark s:sparks){
            p.setColor(s.color);p.setAlpha((int)(255*Math.max(0,s.life/s.maxLife)));
            c.drawCircle(s.x,s.y,2.5f+3*s.life/s.maxLife,p);p.setAlpha(255);
        }
        if(pulseFlash>0) {
            p.setColor(0xffb3f7ff);p.setAlpha((int)(150*pulseFlash/.55f));
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(12);
            c.drawCircle(heroX,heroY,235*(1-pulseFlash/.55f),p);
            p.setStyle(Paint.Style.FILL);p.setAlpha(255);
        }
        c.restore();
        if(scene==Scene.PLAY)drawHud(c,biome);
        else drawHudTop(c,biome);
    }

    private void drawTerrain(Canvas c,int biome) {
        p.setShader(new LinearGradient(0,0,WORLD_W,WORLD_H,
                lighten(GROUNDS[biome],.13f),darken(GROUNDS[biome],.34f),Shader.TileMode.CLAMP));
        c.drawRect(0,0,WORLD_W,WORLD_H,p);p.setShader(null);
        // Ground tiles and subtle road markings.
        for(int x=0;x<=WORLD_W;x+=95) line(c,x,0,x,WORLD_H,0x163c4d4c,1);
        for(int y=0;y<=WORLD_H;y+=95) line(c,0,y,WORLD_W,y,0x163c4d4c,1);
        p.setColor(0x665e6457);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(96);
        Path road=new Path();
        if(Campaign.map(stage)==0) {
            road.moveTo(0,400);road.cubicTo(400,330,510,620,BASE_X,BASE_Y);
        }else if(Campaign.map(stage)==1){
            road.moveTo(0,900);road.cubicTo(450,850,580,250,BASE_X,BASE_Y);
        }else{
            road.moveTo(1750,0);road.cubicTo(1250,150,700,170,BASE_X,BASE_Y);
        }
        c.drawPath(road,p);
        p.setColor(0xff9a9173);p.setStrokeWidth(5);c.drawPath(road,p);
        p.setStyle(Paint.Style.FILL);
        for(int i=0;i<10;i++){
            float px=120+i*187,py=75+(i*143)%860;
            circle(c,px,py,80,0x0cffffff);
        }
        // Lightweight biome-specific animated atmosphere, fully offline.
        for(int i=0;i<34;i++){
            float drift=(biome==7?75f:biome==4?28f:9f)*gameTime;
            float px=(i*227.7f+drift)%WORLD_W;
            float py=(i*149.3f+(biome==4?gameTime*40:gameTime*6))%WORLD_H;
            int tint=biome==4?0x99f2faff:biome==7?0x66b7bbff:0x55c9e8c4;
            circle(c,px,py,biome==4?3.5f:2.2f,tint);
        }
    }
    private void drawDecor(Canvas c,Decor d,int biome) {
        c.save();c.translate(d.x,d.y);c.scale(d.size,d.size);
        if(biome==4 || biome==5 || d.kind==3){
            oval(c,-23,-7,25,11,0x44000000);
            polygon(c,new float[]{-20,0,-9,-24,14,-31,28,-3,12,8},0xff7f8c88);
            polygon(c,new float[]{-9,-24,14,-31,28,-3,3,-8},0xffb0b9b2);
            line(c,-8,-5,9,-12,0xff596e76,2);
        } else if(biome==3 || biome==9){
            oval(c,-18,-5,22,8,0x44000000);
            polygon(c,new float[]{-16,2,-7,-18,5,-6,18,-37,22,5},0xff3f353c);
            polygon(c,new float[]{-7,-18,5,-6,18,-37,12,-4},0xffe28055);
        } else if(d.kind%3==0 || biome==6) {
            oval(c,-24,-4,27,11,0x44000000);
            polygon(c,new float[]{-11,0,-1,-44,13,-25,24,0},biome==6?0xff85d9dd:0xff68896f);
            polygon(c,new float[]{-1,-44,13,-25,24,0,5,-8},biome==6?0xffbbf6fc:0xffa4c299);
        } else {
            oval(c,-24,-4,25,11,0x44000000);
            rect(c,-4,-35,7,5,0xff67503a);
            polygon(c,new float[]{-24,-25,-12,-46,10,-52,28,-22,9,-7,-17,-11},
                    biome==8?0xff725879:0xff3d7c63);
            polygon(c,new float[]{-11,-46,10,-52,28,-22,9,-27},
                    biome==8?0xffa88ab4:0xff65a982);
            circle(c,5,-32,3,0xffb9e5a8);
        }
        c.restore();
    }
    private void drawBase(Canvas c,int biome) {
        float x=BASE_X,y=BASE_Y;
        oval(c,x-158,y+96,x+158,y+128,0x550d161b);
        round(c,x-140,y-112,x+140,y+114,28,0xff36454d);
        round(c,x-121,y-93,x+121,y+94,19,0xff8a7b67);
        for(int i=0;i<7;i++){
            float bx=x-116+i*37;
            rect(c,bx,y-119,bx+25,y-89,0xffbec0ac);
            rect(c,bx,y+88,bx+25,y+118,0xff9e9f90);
        }
        for(int i=0;i<5;i++){
            float by=y-85+i*42;
            rect(c,x-147,by,x-119,by+25,0xffa8ad9e);
            rect(c,x+119,by,x+147,by+25,0xffa8ad9e);
        }
        // Gate, citadel walls, luminous core.
        round(c,x-35,y+91,x+35,y+123,6,0xff493d3b);
        for(int i=0;i<5;i++)line(c,x-27+i*13,y+95,x-27+i*13,y+121,0xffa88768,2);
        oval(c,x-76,y+44,x+78,y+70,0x55000000);
        polygon(c,new float[]{x-75,y+27,x,y-36,x+76,y+27,x+64,y+60,x-64,y+60},0xff667580);
        polygon(c,new float[]{x-80,y+25,x,y-50,x+80,y+25},0xff325567);
        polygon(c,new float[]{x-80,y+25,x,y-50,x,y+25},0xff547f84);
        round(c,x-18,y-10,x+18,y+35,5,0xffd7b98c);
        circle(c,x,y+1,12,GLOWS[biome]);
        circle(c,x,y+1,5,0xffffffff);
        line(c,x,y-49,x,y-91,0xffd3c6a2,5);
        polygon(c,new float[]{x+3,y-87,x+41,y-79,x+3,y-65},0xffe8a860);
        // Permanent rank changes add real defensive silhouettes, not only HP.
        if(fortressRank>=2){
            for(int side=-1;side<=1;side+=2){
                polygon(c,new float[]{x+side*110,y-113,x+side*145,y-152,
                     x+side*169,y-114},0xff507782);
                rect(c,x+side*145-13,y-114,x+side*145+13,y-72,0xffaab8aa);
            }
        }
        if(fortressRank>=3){
            rect(c,x-78,y+45,x+79,y+79,0xff67767a);
            for(int slot=-2;slot<=2;slot++)
                rect(c,x+slot*30-11,y+38,x+slot*30+11,y+49,0xffbdc5b6);
            polygon(c,new float[]{x-45,y+47,x,y+14,x+45,y+47},0xff8daba5);
        }
        if(fortressRank>=4){
            for(int side=-1;side<=1;side+=2){
                rect(c,x+side*165-15,y-80,x+side*165+15,y+28,0xff738b91);
                polygon(c,new float[]{x+side*165-27,y-85,x+side*165,y-121,
                     x+side*165+27,y-85},0xff38596b);
                circle(c,x+side*165,y-62,8,0xffffb672);
            }
        }
        if(fortressRank>=5){
            circle(c,x,y-45,29,0x449bdffe);
            circle(c,x,y-45,14,0xff8ae8ee);
            circle(c,x,y-45,6,0xffffffff);
            for(int i=0;i<9;i++){
                float a=i*.698f+gameTime*.3f;
                float ex=x+(float)Math.cos(a)*96,ey=y-45+(float)Math.sin(a)*52;
                circle(c,ex,ey,3.5f,0xffa2eaf9);
            }
        }
        if(baseHp<700) for(int i=0;i<3;i++)line(c,x-125+i*65,y-91,x-115+i*60,y-52,0xff57453d,4);
        if(baseHp<320) for(int i=0;i<4;i++){
            circle(c,x-140+i*85,y-90,22,0x55ff9d57);
            circle(c,x-140+i*85,y-111,9,0x99ffae69);
        }
    }
    private void drawTower(Canvas c,float x,float y,int tier,int biome) {
        oval(c,x-34,y+25,x+38,y+42,0x66000000);
        if(tier==0) {
            circle(c,x,y,26,0xff47514c);
            circle(c,x,y,18,0xff617265);
            text(c,"+",x,y+8,24,0xff91a99b,true);
            return;
        }
        float h=28+tier*11;
        polygon(c,new float[]{x-27,y+20,x-27,y-h,x+27,y-h,x+27,y+20},0xff6d7c81);
        polygon(c,new float[]{x-27,y-h,x,y-h-25,x+27,y-h},0xff304f60);
        polygon(c,new float[]{x-27,y-h,x,y-h-25,x,y-h},0xff597f8c);
        rect(c,x-9,y-h+10,x+9,y-h+31,0xff24393f);
        circle(c,x,y-h+18,8,GLOWS[biome]);
        line(c,x-7,y-h-1,x+25,y-h-23,0xffd4c08b,7);
        circle(c,x+25,y-h-23,7,GOLD);
        for(int j=0;j<tier;j++)circle(c,x-25+j*12,y+10,4,GOLD);
    }
    private void drawEnemy(Canvas c,Enemy e) {
        float bob=(float)Math.sin(e.phase)*4,scale=e.kind==2?1.6f:e.kind==1?.76f:1f;
        c.save();c.translate(e.x,e.y+bob);c.scale(scale,scale);
        oval(c,-24,15,24,27,0x66000000);
        int[] colors={0xffba6f61,0xffd9a45c,0xff788b9d,0xffad84cc,0xff70947a,0xffaa6781};
        int body=colors[e.kind];
        // Animated legs, shoulders, helmet, glowing eyes and species silhouettes.
        line(c,-12,5,-17+(float)Math.sin(e.phase)*7,22,0xff28333a,8);
        line(c,12,5,17-(float)Math.sin(e.phase)*7,22,0xff28333a,8);
        oval(c,-21,-24,21,14,body);
        oval(c,-15,-36,15,-7,lighten(body,.2f));
        if(e.kind==3){
            polygon(c,new float[]{-18,-20,-46,-34,-35,5,-17,0},0xffb7a3e4);
            polygon(c,new float[]{18,-20,46,-34,35,5,17,0},0xffb7a3e4);
        }else if(e.kind==2){
            polygon(c,new float[]{-12,-32,-27,-61,0,-40},0xffd7d1b4);
            polygon(c,new float[]{12,-32,27,-61,0,-40},0xffd7d1b4);
        }else{
            polygon(c,new float[]{-14,-26,-28,-47,-2,-29},darken(body,.2f));
            polygon(c,new float[]{14,-26,28,-47,2,-29},darken(body,.2f));
        }
        circle(c,-7,-22,4,0xffffe899);circle(c,7,-22,4,0xffffe899);
        line(c,-4,-9,6,-9,0xff3f3038,3);
        if(e.hp<e.maxHp) {
            rect(c,-25,-52,25,-47,0xbb111c21);
            rect(c,-24,-51,-24+48*Math.max(0,e.hp/e.maxHp),-48,0xffff8c78);
        }
        c.restore();
    }
    private void drawHero(Canvas c) {
        float bob=(Math.abs(axisX)+Math.abs(axisY)>.1f)?
                (float)Math.sin(gameTime*14)*4:(float)Math.sin(gameTime*3)*2;
        c.save();c.translate(heroX,heroY+bob);
        oval(c,-24,24,28,38,0x88000000);
        float step=(float)Math.sin(gameTime*14)*8;
        line(c,-8,5,-11+step,28,0xff29374e,11);
        line(c,8,5,11-step,28,0xff29374e,11);
        polygon(c,new float[]{-19,-26,19,-26,23,9,0,20,-23,9},0xff2f718a);
        polygon(c,new float[]{-16,-22,0,-30,16,-22,13,10,0,18,-13,10},0xff82b5bb);
        oval(c,-15,-48,15,-17,0xffd5c3a8);
        polygon(c,new float[]{-19,-39,-12,-56,10,-57,19,-39,10,-33,-10,-33},0xff334f65);
        polygon(c,new float[]{-16,-43,-4,-66,8,-47},0xff8ed0d6);
        circle(c,-6,-35,2,0xff29424a);circle(c,6,-35,2,0xff29424a);
        // Shield arm and shimmering sword, animated on attacks.
        circle(c,-25,-5,19,0xffb79a65);
        circle(c,-25,-5,13,0xff355d73);circle(c,-25,-5,5,GOLD);
        c.save();
        c.rotate((float)Math.sin(gameTime*8)*5+(fireFlash>0?36:0),26,-13);
        WorldArt.drawWeapon(c,equippedWeapon,26,-18,.66f,gameTime);
        c.restore();
        if(fireFlash>0 && (equippedWeapon==1 || equippedWeapon==2)){
            circleStroke(c,0,0,Progression.WEAPONS[equippedWeapon].range*.55f,
                    Progression.WEAPONS[equippedWeapon].color&0x88ffffff,6);
        }
        if(hurtCooldown>0)circle(c,0,-7,32,0x44ff6464);
        c.restore();
    }

    private void drawHud(Canvas c,int biome) {
        drawHudTop(c,biome);
        // Left joystick and right action cluster: no UI obstruction in the center.
        circle(c,153,568,94,0x77202c38);circleStroke(c,153,568,93,0x77d3ede6,3);
        circle(c,153+axisX*61,568+axisY*61,45,0xbba3c4c6);
        circle(c,153+axisX*61,568+axisY*61,26,0xff416578);
        int nextTier=nextTowerTier();
        action(c,849,589,50,0xff4c837d,"BUILD",
                nextTier>=BattleRules.MAX_TOWER_TIER?"MAX":""+BattleRules.towerCost(nextTier));
        action(c,974,593,49,0xff6476a6,"PULSE",
                pulseCooldown>0?""+(int)Math.ceil(pulseCooldown)+"s":"READY");
        action(c,1130,560,70,0xffb3684c,"ATTACK","FIRE");
        if(waveNotice>0) {
            String text=waiting?"WAVE CLEARED!  +25 GOLD":"WAVE "+wave+"  •  DEFEND THE CORE";
            text(c,text,640,159,34,0xfff2e4bf,true);
        }
        if(noticeTimer>0) text(c,notice,640,207,30,GOLD,true);
        text(c,"MOVE",153,688,17,0xffb5cfc5,true);
        WorldArt.drawWeapon(c,equippedWeapon,624,646,.59f,gameTime);
        text(c,Progression.WEAPONS[equippedWeapon].name.toUpperCase(),
                624,703,17,GOLD,true);
    }
    private int nextTowerTier() {
        int tier=BattleRules.MAX_TOWER_TIER;
        for(int t:towers) tier=Math.min(tier,t);
        return tier;
    }
    private void drawHudTop(Canvas c,int biome) {
        round(c,15,12,1250,102,22,0xee13252c);
        text(c,Campaign.title(stage),45,48,25,GOLD,false);
        text(c,Campaign.worldName(stage),45,82,20,INK,false);
        text(c,"CORE",319,38,15,0xffafc6c6,false);
        bar(c,316,47,243,18,baseHp/Progression.maxCoreHp(fortressRank),0xff79cfad);
        text(c,"HERO",319,84,15,0xffafc6c6,false);
        bar(c,377,69,182,13,heroHp/100,0xffe7a07e);
        text(c,"WAVE "+wave+" / "+Campaign.waves(stage),617,50,26,INK,false);
        text(c,"GOLD  "+gold,618,83,21,GOLD,false);
        text(c,"KILLS "+kills,870,51,21,INK,false);
        text(c,"FORT "+fortressRank+"   /   "+Progression.WEAPONS[equippedWeapon].name,870,83,17,GOLD,false);
        round(c,1146,25,1223,89,16,0xff344c55);
        text(c,"II",1184,71,31,INK,true);
    }
    private void action(Canvas c,float x,float y,float r,int color,String title,String sub) {
        circle(c,x,y+5,r+5,0x88000000);
        circle(c,x,y,r,color);
        circleStroke(c,x,y,r-5,0x88ffffff,3);
        text(c,title,x,y+2,20,0xffffffff,true);
        text(c,sub,x,y+25,13,0xffe3e5d5,true);
    }

    private void drawHome(Canvas c) {
        drawBackdrop(c);
        // Campaign art in the main menu changes with saved progression.
        roundedPanel(c,644,66,1222,646);
        WorldArt.drawScene(c,Campaign.world(Math.min(unlocked,299)),2,
                656,80,553,548,gameTime);
        rect(c,656,522,1209,630,0xd0142b33);
        text(c,"GREENHAVEN  /  THE LAST BASTION",679,563,24,GOLD,false);
        text(c,"A kingdom in danger. Your fortress grows with every victory.",
                679,595,16,INK,false);
        round(c,54,65,628,650,25,0xe6183039);
        text(c,"RIFTGUARD",87,183,80,GOLD,false);
        text(c,"LAST BASTION",91,240,35,INK,false);
        rect(c,93,259,577,264,0xff709c9d);
        text(c,"RECLAIM THE TEN REALMS",94,318,26,0xffe2b995,false);
        text(c,"Defend • Earn Supplies • Upgrade • Unlock",94,355,23,INK,false);
        button(c,95,415,590,490,"CAMPAIGN  /  WORLD MAP",0xffa56a49);
        button(c,95,509,328,572,"ARMORY",0xff3d6971);
        button(c,348,509,590,572,"FORTRESS",0xff44645c);
        text(c,"FORTRESS RANK "+fortressRank+"   •   SUPPLIES "+supplies,
                98,614,21,GOLD,false);
        text(c,"V0.2 VISUAL & EQUIPMENT FOUNDATION",101,643,15,0xff9eb8bb,false);
    }

    private void roundedPanel(Canvas c,float l,float t,float r,float b) {
        round(c,l-5,t-5,r+5,b+5,18,0xffd0a878);
        round(c,l,t,r,b,14,0xff25434b);
    }

    private void drawMap(Canvas c) {
        drawBackdrop(c);
        round(c,37,21,1243,698,26,0xf0162c34);
        text(c,"REALM ATLAS",67,83,47,GOLD,false);
        text(c,"Ten illustrated kingdoms   •   100 levels   •   300 missions",
                469,80,22,INK,false);
        int currentRegion=(selectedLevel-1)/10;
        for(int i=0;i<10;i++){
            int col=i%5,row=i/5;
            float x=66+col*237,y=119+row*123;
            round(c,x-4,y-4,x+224,y+116,12,
                    currentRegion==i?GOLD:0xff43535b);
            WorldArt.drawScene(c,i,i%3,x,y,216,80,gameTime);
            rect(c,x,y+81,x+216,y+112,0xe01e3540);
            String region=Campaign.WORLDS[i];
            if(region.length()>18)region=region.substring(0,18);
            text(c,region.toUpperCase(),x+8,y+102,17,INK,false);
            if(unlocked<(i*30)) {
                circle(c,x+193,y+14,12,0xcc26303a);
                text(c,"X",x+193,y+20,16,0xffe6b6a0,true);
            } else {
                circle(c,x+193,y+14,12,0xdd43835d);
                text(c,"+",x+193,y+20,15,INK,true);
            }
        }
        text(c,"<",104,413,46,GOLD,true);
        text(c,Campaign.WORLDS[currentRegion]+"    •    LEVEL "+selectedLevel+" / 100",
                640,404,29,GOLD,true);
        text(c,">",1185,413,46,GOLD,true);
        for(int i=0;i<3;i++){
            float x=176+i*311, y=429;
            int s=Campaign.stage(selectedLevel,i);
            boolean can=Campaign.accessible(s,unlocked);
            round(c,x-5,y-5,x+297,y+148,15,i==selectedMap?GOLD:0xff4e656d);
            WorldArt.drawScene(c,currentRegion,i,x,y,287,83,gameTime);
            rect(c,x,y+84,x+287,y+143,can?0xff304c52:0xff293840);
            text(c,(char)('A'+i)+"  "+Campaign.MAPS[i],x+9,y+110,23,INK,false);
            text(c,can?(s<unlocked?"CLEARED  •  REPLAY":"AVAILABLE"):"LOCKED  •  PROGRESS FIRST",
                    x+9,y+133,17,can?0xffa5dbb7:0xffd5aba6,false);
        }
        int chosen=Campaign.stage(selectedLevel,selectedMap);
        button(c,406,598,873,654,Campaign.accessible(chosen,unlocked)?"DEPLOY TO THE BATTLEFIELD":"STAGE LOCKED",
                Campaign.accessible(chosen,unlocked)?0xffac704e:0xff485259);
        text(c,"BACK",78,648,24,INK,false);
        text(c,"CLEARED  "+Math.min(300,unlocked)+" / 300",1004,649,21,GOLD,false);
    }

    private void drawArmory(Canvas c) {
        drawBackdrop(c);
        round(c,43,28,1237,695,25,0xf0172d35);
        text(c,"WARDEN'S ARMORY",80,87,51,GOLD,false);
        text(c,"Equip unlocked weapons. Each has different damage, reach and attack speed.",
                83,123,22,INK,false);
        text(c,"FORTRESS "+fortressRank+"   |   EQUIPPED: "+
                Progression.WEAPONS[equippedWeapon].name,1194,88,21,GOLD,true);
        for(int i=0;i<Progression.WEAPONS.length;i++){
            Progression.Weapon w=Progression.WEAPONS[i];
            boolean available=Progression.canEquip(fortressRank,i);
            float x=79+(i%3)*375,y=155+(i/3)*218;
            round(c,x-5,y-4,x+345,y+198,18,
                    i==equippedWeapon?GOLD:0xff49616b);
            round(c,x,y,x+340,y+192,14,available?0xff294955:0xff293640);
            round(c,x+12,y+15,x+128,y+167,12,0xff172d39);
            WorldArt.drawWeapon(c,i,x+69,y+96,1.14f,gameTime);
            text(c,w.name,x+146,y+54,24,available?INK:0xffa2a6aa,false);
            text(c,w.type,x+147,y+85,19,w.color,false);
            text(c,"DMG "+(int)w.damage+"  •  RANGE "+(int)w.range,
                    x+147,y+113,16,INK,false);
            text(c,available?(i==equippedWeapon?"EQUIPPED  ✓":"TAP TO EQUIP"):
                    "UNLOCK: FORT RANK "+w.requiredRank,
                    x+148,y+162,17,available?GOLD:0xffffaa9c,false);
        }
        button(c,80,625,317,679,"BACK TO HOME",0xff3d6067);
        button(c,351,625,648,679,"UPGRADE FORTRESS",0xff9d6b4e);
        text(c,"Weapon choices are saved permanently.",871,659,18,0xffaec5c9,false);
    }

    private void drawFortress(Canvas c) {
        drawBackdrop(c);
        round(c,48,29,1233,694,25,0xf0172e36);
        text(c,"FORTRESS WORKSHOP",85,86,50,GOLD,false);
        text(c,"Upgrade the base to unlock weapons, towers and extra core health.",
                88,125,21,INK,false);
        WorldArt.drawScene(c,Math.min(9,Campaign.world(Math.min(unlocked,299))),
                2,75,158,600,390,gameTime);
        round(c,77,554,669,632,13,0xee1c3942);
        text(c,"RANK "+fortressRank+"  /  5     "+Progression.RANK_NAMES[fortressRank],
                98,587,29,GOLD,false);
        text(c,"CORE HP: "+Progression.maxCoreHp(fortressRank)+
                "     STARTING TOWERS: "+(fortressRank>=5?3:fortressRank>=4?2:1),
                100,619,20,INK,false);
        round(c,718,153,1204,531,22,0xff283e4a);
        text(c,"KINGDOM SUPPLIES",758,216,25,INK,false);
        text(c,""+supplies,758,280,59,GOLD,false);
        text(c,"CURRENT UPGRADE",758,329,21,0xffb6c9ca,false);
        if(fortressRank<Progression.MAX_RANK){
            int next=fortressRank+1;
            text(c,Progression.RANK_NAMES[next],758,371,30,GOLD,false);
            text(c,"Unlock: "+Progression.WEAPONS[next].name,758,410,22,INK,false);
            text(c,"Cost: "+Progression.upgradeCost(fortressRank)+" Supplies",
                    758,453,25,INK,false);
            text(c,Progression.canUpgrade(fortressRank,supplies)?
                    "READY TO CONSTRUCT":"WIN MISSIONS TO EARN MORE",
                    759,496,18,Progression.canUpgrade(fortressRank,supplies)?
                            0xff8cdea5:0xffe3af9c,false);
        }else{
            text(c,"ARC CITADEL - MAX RANK",759,378,29,GOLD,false);
            text(c,"All six launch weapons are available.",759,423,21,INK,false);
        }
        button(c,738,554,1195,627,fortressRank==Progression.MAX_RANK?
                "FORTRESS COMPLETE":"UPGRADE / CONSTRUCT",
                Progression.canUpgrade(fortressRank,supplies)?0xffb0704e:0xff566166);
        text(c,"BACK",92,669,24,INK,false);
        text(c,"ARMORY",453,669,25,GOLD,false);
    }

    private void upgradeFortress() {
        if(!Progression.canUpgrade(fortressRank,supplies))return;
        supplies-=Progression.upgradeCost(fortressRank);
        fortressRank++;
        prefs.edit().putInt("supplies",supplies).putInt("fortress_rank",fortressRank).apply();
        announce(Progression.RANK_NAMES[fortressRank]+" CONSTRUCTED");
    }
    private void equipWeapon(int id) {
        if(!Progression.canEquip(fortressRank,id))return;
        equippedWeapon=id;
        prefs.edit().putInt("weapon",equippedWeapon).apply();
    }
    private void drawBackdrop(Canvas c) {
        p.setShader(new LinearGradient(0,0,0,720,0xff0b1c26,0xff293d43,Shader.TileMode.CLAMP));
        c.drawRect(0,0,1280,720,p);p.setShader(null);
        circle(c,1046,145,90,0x55ffca92);
        circle(c,1046,145,58,0xffdfa778);
        for(int i=0;i<9;i++){
            float x=i*170-80;
            polygon(c,new float[]{x,630,x+138,210+(i%4)*45,x+300,630},0xff22363d);
            polygon(c,new float[]{x+20,630,x+139,330+(i%3)*30,x+270,630},0xff142a34);
        }
        rect(c,0,630,1280,720,0xff182b2b);
        for(int i=0;i<35;i++){
            float x=(i*131)%1280,y=60+(i*71)%530;
            circle(c,x,y,1.8f,0x88d7dbcd);
        }
    }
    private void drawResult(Canvas c) {
        rect(c,0,0,W,H,0xb900131b);
        round(c,348,164,932,624,32,0xff1b343e);
        border(c,348,164,932,624,32,GOLD,3);
        text(c,scene==Scene.WIN?"FORTRESS SECURED":"THE CORE HAS FALLEN",
                640,249,42,scene==Scene.WIN?GOLD:0xffff9f8c,true);
        text(c,Campaign.title(stage)+"   •   "+Campaign.worldName(stage),640,298,26,INK,true);
        text(c,scene==Scene.WIN?"Campaign progress saved!":"Upgrade your defenses and try again.",
                640,346,22,0xffc1d9d4,true);
        text(c,"Enemies defeated: "+kills+"     Gold earned: "+gold,
                640,388,21,INK,true);
        button(c,435,429,845,502,scene==Scene.WIN?"NEXT STAGE":"RETRY STAGE",0xffae7652);
        button(c,435,520,845,585,"CAMPAIGN MAP",0xff3f626e);
    }
    private void drawPause(Canvas c) {
        rect(c,0,0,W,H,0xc3001117);
        round(c,390,166,890,582,27,0xff1f3741);
        text(c,"PAUSED",640,282,66,GOLD,true);
        button(c,456,332,825,414,"RESUME BATTLE",0xffae7652);
        button(c,456,439,825,517,"RETURN TO MAP",0xff42616b);
    }
    private void button(Canvas c,float l,float t,float r,float b,String label,int color){
        round(c,l,t+6,r,b+6,17,0x77000000);
        round(c,l,t,r,b,17,color);
        text(c,label,(l+r)/2,(t+b)/2+10,29,INK,true);
    }

    @Override public boolean onTouchEvent(MotionEvent ev) {
        int action=ev.getActionMasked(),index=ev.getActionIndex(),id=ev.getPointerId(index);
        float scale=Math.min(getWidth()/W,getHeight()/H);
        if(scale<=0)return true;
        float offsetX=(getWidth()-W*scale)*.5f,offsetY=(getHeight()-H*scale)*.5f;
        float x=(ev.getX(index)-offsetX)/scale,y=(ev.getY(index)-offsetY)/scale;
        if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_POINTER_DOWN) {
            if(scene!=Scene.PLAY) { tapMenu(x,y);invalidate();return true; }
            if(paused) {
                if(x>450 && x<830 && y>330 && y<421) { paused=false;clearTouchControls(); }
                else if(x>450 && x<830 && y>435 && y<525) {scene=Scene.MAP;paused=false;clearTouchControls();}
            }else if(x>1138 && y<112) {paused=true;clearTouchControls();}
            else if(x<350 && y>367) {joyPointer=id;updateStick(x,y);}
            else if(distance(x,y,1130,560)<82) {attackPointer=id;firing=true;fire();}
            else if(distance(x,y,974,593)<62) pulse();
            else if(distance(x,y,849,589)<65) build();
        }else if(action==MotionEvent.ACTION_MOVE) {
            if(joyPointer!=-1){
                int at=ev.findPointerIndex(joyPointer);
                if(at>=0)updateStick((ev.getX(at)-offsetX)/scale,(ev.getY(at)-offsetY)/scale);
            }
        }else if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_POINTER_UP || action==MotionEvent.ACTION_CANCEL){
            if(action==MotionEvent.ACTION_CANCEL) clearTouchControls();
            else {
                if(id==joyPointer){joyPointer=-1;axisX=axisY=0;}
                if(id==attackPointer){attackPointer=-1;firing=false;}
            }
        }
        invalidate();
        return true;
    }
    private void updateStick(float x,float y) {
        float dx=x-153,dy=y-568,d=(float)Math.hypot(dx,dy);
        axisX=d>0?dx/d*Math.min(d/90f,1f):0;
        axisY=d>0?dy/d*Math.min(d/90f,1f):0;
    }
    private void tapMenu(float x,float y) {
        if(scene==Scene.HOME) {
            if(x>93 && x<610 && y>408 && y<492)scene=Scene.MAP;
            else if(x>93 && x<337 && y>502 && y<580)scene=Scene.ARMORY;
            else if(x>345 && x<610 && y>502 && y<580)scene=Scene.FORTRESS;
        }else if(scene==Scene.MAP) {
            if(y>=112 && y<362 && x>=66 && x<1251) {
                int col=(int)((x-66)/237);
                int row=(int)((y-119)/123);
                if(col>=0 && col<5 && row>=0 && row<2) {
                    float localX=(x-66)%237, localY=(y-119)%123;
                    if(localX<=216 && localY>=0 && localY<114) {
                        selectedLevel=(row*5+col)*10+1;
                        selectedMap=0;
                    }
                }
            }else if(y>363 && y<428 && x<160) {
                selectedLevel=Math.max(1,selectedLevel-1);
                selectedMap=0;
            }else if(y>363 && y<428 && x>1120) {
                selectedLevel=Math.min(100,selectedLevel+1);
                selectedMap=0;
            }else if(y>=422 && y<581 && x>=171 && x<1104) {
                int col=(int)((x-176)/311);
                if(col>=0 && col<3)selectedMap=col;
            }else if(y>585 && y<664 && x>399 && x<881) {
                int s=Campaign.stage(selectedLevel,selectedMap);
                if(Campaign.accessible(s,unlocked))start(s);
            }else if(x<191 && y>599)scene=Scene.HOME;
        }else if(scene==Scene.ARMORY) {
            if(x>80 && x<329 && y>619)scene=Scene.HOME;
            else if(x>342 && x<655 && y>619)scene=Scene.FORTRESS;
            else if(y>=151 && y<570) {
                int col=(int)((x-79)/375),row=(int)((y-155)/218);
                if(col>=0 && col<3 && row>=0 && row<2) {
                    float localX=(x-79)%375,localY=(y-155)%218;
                    if(localX>=0 && localX<343 && localY>=0 && localY<197)
                        equipWeapon(row*3+col);
                }
            }
        }else if(scene==Scene.FORTRESS) {
            if(y>539 && y<634 && x>722)upgradeFortress();
            else if(y>630 && x<300)scene=Scene.HOME;
            else if(y>630 && x>339 && x<690)scene=Scene.ARMORY;
        }else if(scene==Scene.WIN || scene==Scene.LOSE) {
            if(x>420 && x<860 && y>425 && y<510) {
                if(scene==Scene.WIN && stage==299)scene=Scene.MAP;
                else if(scene==Scene.WIN)start(Math.min(299,stage+1));
                else start(stage);
            }else if(x>420 && x<860 && y>514 && y<602)scene=Scene.MAP;
        }
    }
    private void bar(Canvas c,float x,float y,float w,float h,float fraction,int color) {
        round(c,x,y,x+w,y+h,5,0xff40535d);
        if(fraction>0)round(c,x+2,y+2,x+2+Math.max(0,(w-4)*BattleRules.clamp(fraction,0,1)),y+h-2,3,color);
    }
    private void rect(Canvas c,float l,float t,float r,float b,int color) {
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRect(l,t,r,b,p);
    }
    private void round(Canvas c,float l,float t,float r,float b,float radius,int color) {
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(l,t,r,b,radius,radius,p);
    }
    private void border(Canvas c,float l,float t,float r,float b,float radius,int color,float width) {
        p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setColor(color);
        c.drawRoundRect(l,t,r,b,radius,radius,p);p.setStyle(Paint.Style.FILL);
    }
    private void circle(Canvas c,float x,float y,float rad,int color) {
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawCircle(x,y,rad,p);
    }
    private void circleStroke(Canvas c,float x,float y,float rad,int color,float width) {
        p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setColor(color);
        c.drawCircle(x,y,rad,p);p.setStyle(Paint.Style.FILL);
    }
    private void oval(Canvas c,float l,float t,float r,float b,int color) {
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawOval(l,t,r,b,p);
    }
    private void line(Canvas c,float x,float y,float x2,float y2,int color,float width) {
        p.setShader(null);p.setColor(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x,y,x2,y2,p);
    }
    private void polygon(Canvas c,float[] pts,int color) {
        p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);
        Path path=new Path();path.moveTo(pts[0],pts[1]);
        for(int i=2;i<pts.length;i+=2)path.lineTo(pts[i],pts[i+1]);
        path.close();c.drawPath(path,p);
    }
    private void text(Canvas c,String str,float x,float y,float size,int color,boolean center) {
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        p.setTypeface(FONT);p.setTextSize(size);
        p.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);
        c.drawText(str,x,y,p);
    }
    private static int lighten(int color,float amount){
        return Color.rgb((int)Math.min(255,Color.red(color)+(255-Color.red(color))*amount),
                (int)Math.min(255,Color.green(color)+(255-Color.green(color))*amount),
                (int)Math.min(255,Color.blue(color)+(255-Color.blue(color))*amount));
    }
    private static int darken(int color,float amount){
        return Color.rgb((int)(Color.red(color)*(1-amount)),
                (int)(Color.green(color)*(1-amount)),
                (int)(Color.blue(color)*(1-amount)));
    }
}
