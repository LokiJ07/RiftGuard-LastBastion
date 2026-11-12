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
    private enum Scene { HOME, MAP, ARMORY, SKILLS, FORTRESS, RESOURCES, KINGDOM, PLAY, WIN, LOSE }
    private Scene scene=Scene.HOME;
    private boolean briefingOpen=false;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PaintedArt painted;
    private final CharacterArt characterArt;
    private final MotionArt motionArt;
    private final BiomeProps biomeProps;
    private ResourcePacks.State resourceState;
    private volatile boolean resourceBusy=false;
    private volatile boolean resourceSuccess=false;
    private volatile int resourcePercent=0;
    private volatile String resourceMessage="The bundled base art works without internet.";
    private static final float BATTLE_ZOOM=1.10f;
    private static final int DECOR=0, KEEP=1, TOWER=2, MONSTER=3, DROP=4, WARDEN=5;
    private static final class DepthItem {
        float depth;
        int type,slot;
        Object entity;
    }
    private final ArrayList<DepthItem> drawQueue=new ArrayList<>(256);
    private final ArrayList<DepthItem> drawPool=new ArrayList<>(256);
    private void layer(int type,float depth,Object entity,int slot) {
        int i=drawQueue.size();
        if(i==drawPool.size())drawPool.add(new DepthItem());
        DepthItem item=drawPool.get(i);
        item.type=type;item.depth=depth;item.entity=entity;item.slot=slot;
        drawQueue.add(item);
    }

    private final SharedPreferences prefs;
    private final KingdomMeta kingdom;
    private final GameAudio audio;
    private int kingdomTab=0;
    private String kingdomMessage="Complete challenges to earn rewards!";
    private boolean battleRecorded=false;
    private int battlePulses=0;
    private final Random random=new Random(13);
    private final List<Enemy> enemies=new ArrayList<>();
    private final List<Shot> shots=new ArrayList<>();
    private final List<Spark> sparks=new ArrayList<>();
    private final List<DeathEcho> deaths=new ArrayList<>();
    private final List<Impact> impacts=new ArrayList<>();
    private final List<Decor> decor=new ArrayList<>();
    private final List<Pickup> pickups=new ArrayList<>();
    private final int[] towers=new int[4];
    private final float[] towerTimers=new float[4];
    private final float[] towerRecoil=new float[4];
    private final float[] towerAim=new float[4];
    private final float[] hazardTimers=new float[4];
    private float hazardSlowTimer=0;
    private final float[] towerX={810,1092,811,1093};
    private final float[] towerY={370,370,624,624};
    private int unlocked;
    private int supplies, fortressRank, equippedWeapon, equippedSkill, difficultyMode;
    private int lastCastSkill=WardenSkills.PULSE;
    private float bannerX,bannerY,bannerTime,bannerTick;
    private float trapX,trapY,trapTime;
    private float meteorX,meteorY,meteorTime;
    private int selectedLevel=1, selectedMap=0, stage=0;
    private boolean regionalMap=false;
    private int wave, remaining, gold, kills;
    private float baseHp, heroHp, heroX, heroY, heroDir=-1.57f, spawnTimer, intermission, waveNotice;
    private float shotTimer, pulseCooldown, pulseFlash, hurtCooldown, fireFlash, noticeTimer, gameTime;
    private String notice="";
    private boolean waiting, paused, active=true, firing, heroMoving;
    private boolean pendingMelee=false;
    private int pendingMeleeWeapon=-1;
    private float pendingMeleeFacing=0;
    private float walkClock;
    private int joyPointer=-1, attackPointer=-1;
    private float axisX=0, axisY=0;
    private long lastNs=0;

    private static final class Enemy {
        float x,y,hp,maxHp,phase,attackTimer,attackAnim,slowTimer,specialTimer,specialCharge,burnTimer,burnDps;
        boolean moving,boss;
        int kind;
        Enemy(float x,float y,int kind,float hp,float phase) {
            this.x=x; this.y=y; this.kind=kind; this.hp=hp; this.maxHp=hp; this.phase=phase;
        }
    }
    private static final class Shot {
        float x,y,speed,damage,life;
        boolean tower;
        int weaponId=-1,towerSlot=-1;
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
    private static final class DeathEcho {
        final float x,y,phase;
        final int kind;
        float life=.64f;
        DeathEcho(float x,float y,int kind,float phase){
            this.x=x;this.y=y;this.kind=kind;this.phase=phase;
        }
    }
    private static final class Impact {
        final float x,y,radius;
        final int color;
        float life=.42f;
        Impact(float x,float y,int color,float radius){
            this.x=x;this.y=y;this.color=color;this.radius=radius;
        }
    }
    private static final class Pickup {
        float x,y,age=0;
        int kind;
        Pickup(float x,float y,int kind){this.x=x;this.y=y;this.kind=kind;}
    }
    private static final class Decor {
        float x,y,size; int kind;
        Decor(float x,float y,float size,int kind) { this.x=x;this.y=y;this.size=size;this.kind=kind; }
    }

    public RiftGameView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE,null);
        painted=new PaintedArt(context);
        characterArt=new CharacterArt(context.getResources());
        motionArt=new MotionArt(context);
        biomeProps=new BiomeProps(context);
        resourceState=ResourcePacks.state(context);
        prefs=context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        kingdom=new KingdomMeta(prefs);
        audio=new GameAudio(context,prefs);
        unlocked=Math.max(0,Math.min(Campaign.STAGES,prefs.getInt("unlocked",0)));
        supplies=Math.max(0,prefs.getInt("supplies",0));
        fortressRank=Math.max(1,Math.min(Progression.MAX_RANK,prefs.getInt("fortress_rank",1)));
        equippedWeapon=prefs.getInt("weapon",0);
        equippedSkill=prefs.getInt("skill",WardenSkills.PULSE);
        difficultyMode=DifficultyRules.safe(prefs.getInt("difficulty",DifficultyRules.NORMAL));
        if(!Progression.canEquip(fortressRank,equippedWeapon))equippedWeapon=0;
        if(!WardenSkills.unlocked(fortressRank,equippedSkill))
            equippedSkill=WardenSkills.PULSE;
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
        if(active)audio.prepare();
        if (active) postInvalidateOnAnimation();
    }
    public void releaseAudio(){audio.release();}
    public boolean handleBack() {
        if (scene==Scene.PLAY && briefingOpen) {
            briefingOpen=false;
            scene=Scene.MAP;
            clearTouchControls();
            return true;
        }
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
            if (scene==Scene.PLAY && !paused && !briefingOpen) update(dt);
        }
        canvas.drawColor(0xff101e22);
        // Portrait/notch/ultrawide letterbox gutters now extend the scene palette
        // instead of leaving distracting black pillars. The 16:9 touch layout is
        // preserved without stretching/cropping gameplay or critical controls.
        p.setShader(new LinearGradient(0,0,getWidth(),getHeight(),
                0xff284c52,0xff10252f,Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,getWidth(),getHeight(),p);
        p.setShader(null);
        float scale=Math.min(getWidth()/W,getHeight()/H);
        if (scale<=0) return;
        canvas.save();
        canvas.translate((getWidth()-W*scale)*.5f,(getHeight()-H*scale)*.5f);
        canvas.scale(scale,scale);
        if (scene==Scene.HOME) drawHome(canvas);
        else if (scene==Scene.MAP) drawMap(canvas);
        else if (scene==Scene.ARMORY) drawArmory(canvas);
        else if (scene==Scene.SKILLS) drawSkills(canvas);
        else if (scene==Scene.FORTRESS) drawFortress(canvas);
        else if (scene==Scene.RESOURCES) drawResources(canvas);
        else if (scene==Scene.KINGDOM) drawKingdom(canvas);
        else {
            drawBattle(canvas);
            if (scene==Scene.WIN || scene==Scene.LOSE) drawResult(canvas);
            else if (paused) drawPause(canvas);
            else if (scene==Scene.PLAY && briefingOpen) drawMissionBriefing(canvas);
        }
        canvas.restore();
        if (active) postInvalidateOnAnimation();
    }

    private void start(int s) {
        Campaign.checkStage(s);
        stage=s; scene=Scene.PLAY; paused=false; briefingOpen=false;
        battleRecorded=false; battlePulses=0;
        hazardSlowTimer=0;
        for(int i=0;i<hazardTimers.length;i++)hazardTimers[i]=.5f;
        motionArt.prepareTerrain(s);
        walkClock=0;heroMoving=false;
        heroX=LaneRules.startX(); heroY=LaneRules.startY(); heroHp=kingdom.heroMaxHp();
        baseHp=Progression.maxCoreHp(fortressRank)+kingdom.coreBonusHp();
        wave=1; remaining=DifficultyRules.enemies(stage,wave,difficultyMode);
        spawnTimer=3.2f; intermission=0; waiting=false;
        shotTimer=0; pulseCooldown=0; pulseFlash=0; hurtCooldown=0; fireFlash=0;
        pendingMelee=false;pendingMeleeWeapon=-1;
        bannerTime=0;bannerTick=0;trapTime=0;meteorTime=0;
        waveNotice=2.6f; noticeTimer=0; gold=150+(fortressRank-1)*20; kills=0;
        enemies.clear();shots.clear();sparks.clear();decor.clear();pickups.clear();
        deaths.clear();impacts.clear();
        for (int i=0;i<towers.length;i++){
            towers[i]=0;towerTimers[i]=0;towerRecoil[i]=0;
            towerAim[i]=0;
        }
        towers[0]=1;
        if(fortressRank>=4)towers[1]=1;
        if(fortressRank>=5)towers[2]=1;
        Random r=new Random(Campaign.seed(stage));
        // Scatter environmental art with deliberate negative space around the keep.
        // Poisson-like rejection prevents overlapping forests and improves combat readability.
        int attempts=0;
        while(decor.size()<88 && attempts++<1800) {
            float x=55+r.nextFloat()*(WORLD_W-110);
            float y=55+r.nextFloat()*(WORLD_H-110);
            if(distance(x,y,BASE_X,BASE_Y)<327)continue;
            if(distance(x,y,heroX,heroY)<212)continue;
            if(LaneRules.stageCoversPath(stage,x,y))continue;
            int chance=r.nextInt(100);
            int kind=chance<12?0:chance<24?1:chance<49?2:
                     chance<78?3:chance<88?4:5;
            boolean crowded=false;
            for(Decor other:decor) {
                float min=(kind<=1||other.kind<=1)?123:78;
                if(distance(x,y,other.x,other.y)<min){crowded=true;break;}
            }
            if(crowded)continue;
            decor.add(new Decor(x,y,.75f+r.nextFloat()*.30f,kind));
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
        hazardSlowTimer=Math.max(0,hazardSlowTimer-dt);
        float speed=245f*(hazardSlowTimer>0?.71f:1f);
        float previousHeroX=heroX,previousHeroY=heroY;
        // Independent X/Y collision resolution allows sliding along walls.
        float nextX=LaneRules.heroX(MovementRules.heroNextX(heroX,heroY,axisX*speed*dt));
        if (!hitsBuiltTower(nextX,heroY)) heroX=nextX;
        float nextY=LaneRules.heroY(MovementRules.heroNextY(heroX,heroY,axisY*speed*dt));
        if (!hitsBuiltTower(heroX,nextY)) heroY=nextY;
        heroMoving=distance(heroX,heroY,previousHeroX,previousHeroY)>.3f;
        if(heroMoving)walkClock+=dt;
        if (Math.abs(axisX)+Math.abs(axisY)>.1f && !pendingMelee)
            heroDir=(float)Math.atan2(axisY,axisX);
        if(pendingMelee && MeleeSwingRules.hitWindow(1f-fireFlash/.23f))
            resolveMeleeHit();
        if (firing) fire();
        updateSkillObjects(dt);
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
            towerRecoil[i]=Math.max(0,towerRecoil[i]-dt);
            if (towers[i]>0 && towerTimers[i]<=0) {
                Enemy target=closest(towerX[i],towerY[i],280+towers[i]*35);
                if (target!=null) {
                    float[] modifiers={1f,1.50f,.77f,1.13f};
                    Shot turretShot=new Shot(towerX[i],towerY[i]-25,
                            BattleRules.towerDamage(towers[i])*modifiers[i]*kingdom.towerDamageMultiplier(),
                            i==1?550:750,target,true);
                    turretShot.towerSlot=i;
                    shots.add(turretShot);
                    audio.play(GameAudio.SHOT);
                    towerAim[i]=(float)Math.atan2(target.y-towerY[i],target.x-towerX[i]);
                    towerRecoil[i]=.20f;
                    towerTimers[i]=Math.max(.25f,(i==1?1.35f:i==2?1.02f:.92f)
                            -towers[i]*.11f);
                    puff(towerX[i],towerY[i]-38,6,towerColor(i));
                }
            }
        }
        Iterator<Enemy> it=enemies.iterator();
        while(it.hasNext()) {
            Enemy e=it.next();
            if(e.hp<=0) {
                gold+=BattleRules.killCoins(e.kind);
                kills++;
                if(pickups.size()<28 && random.nextFloat()<.22f)
                    pickups.add(new Pickup(e.x,e.y,random.nextFloat()<.58f?0:1));
                puff(e.x,e.y,9,GLOWS[Campaign.world(stage)]);
                if(deaths.size()<32)deaths.add(new DeathEcho(e.x,e.y,e.kind,e.phase));
                if(impacts.size()<64)impacts.add(new Impact(e.x,e.y,
                        GLOWS[Campaign.world(stage)],e.boss?86:41));
                it.remove();continue;
            }
            e.attackAnim=Math.max(0,e.attackAnim-dt);
            e.slowTimer=Math.max(0,e.slowTimer-dt);
            if(e.burnTimer>0) {
                float burnStep=Math.min(e.burnTimer,dt);
                e.burnTimer-=burnStep;
                e.hp-=burnStep*e.burnDps;
                if(e.hp<=0)continue; // credited by normal enemy death processing next frame
            }
            float dHero=distance(e.x,e.y,heroX,heroY);
            boolean hunt=MovementRules.huntsHero(dHero,e.kind);
            // Follow the actual stage-specific painted lane, not a separate
            // invisible straight line. Hero aggro can temporarily interrupt it.
            boolean traveling=!hunt && e.x>LaneRules.CASTLE_GATE_X+120f;
            float tx=hunt?heroX:traveling?
                    LaneRules.stageWaypointX(e.x,stage):BASE_X;
            float ty=hunt?heroY:traveling?
                    LaneRules.stageWaypointY(e.x,stage):BASE_Y;
            float d=distance(e.x,e.y,tx,ty);
            // Final fortress boundary, not each intermediate lane sample,
            // determines how close monsters may stop to attack.
            float stop=traveling?3f:MovementRules.enemyStopDistance(hunt,e.kind);
            e.moving=d>stop;
            if(e.moving) {
                float speedFactor=(e.kind==3?1.15f:1f)*
                        DifficultyRules.enemySpeed(difficultyMode)*
                        (e.slowTimer>0?.64f:1f);
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
            if(e.boss){
                e.specialTimer-=dt;
                if(e.specialCharge<=0 && e.specialTimer<=0 &&
                        distance(e.x,e.y,BASE_X,BASE_Y)<440f){
                    e.specialCharge=.86f; // visible charge-up before the blast
                    e.specialTimer=7.3f;
                }else if(e.specialCharge>0) {
                    e.specialCharge-=dt;
                    if(e.specialCharge<=0) {
                        float strength=DifficultyRules.enemyDamage(difficultyMode);
                        if(distance(e.x,e.y,BASE_X,BASE_Y)<445){
                            baseHp-=28f*strength;
                            puff(BASE_X,BASE_Y,19,0xffffac83);
                        }
                        if(distance(e.x,e.y,heroX,heroY)<265){
                            heroHp-=21f*strength;
                            puff(heroX,heroY,18,0xffe5a0fa);
                        }
                        if(baseHp<=0||heroHp<=0){
                            baseHp=Math.max(0,baseHp);
                            heroHp=Math.max(0,heroHp);
                            markDefeat();return;
                        }
                        announce("BOSS SHOCKWAVE!");
                    }
                }
            }
            if (distance(e.x,e.y,heroX,heroY)<44 && hurtCooldown<=0) {
                heroHp-=(e.kind==2?20:11)*
                        DifficultyRules.enemyDamage(difficultyMode);
                e.attackAnim=.42f;
                hurtCooldown=.8f; puff(heroX,heroY,6,0xffff8086);
                audio.play(GameAudio.HURT);
                if(heroHp<=0){heroHp=0;markDefeat();return;}
            }
            if(!hunt && (distance(e.x,e.y,BASE_X,BASE_Y)<190 || (e.kind==5 &&
                    distance(e.x,e.y,BASE_X,BASE_Y)<275))) {
                e.attackTimer-=dt;
                if(e.attackTimer<=0) {
                    baseHp-=(e.kind==2?30:(e.kind==5?18:12))*
                            DifficultyRules.enemyDamage(difficultyMode)*(e.boss?2.0f:1f);
                    e.attackAnim=.42f;
                    e.attackTimer=e.kind==2?1.7f:2.2f;
                    puff(BASE_X,BASE_Y,6,0xffff9f74);
                    if(baseHp<=0){baseHp=0;markDefeat();return;}
                }
            }
        }
        updateStageHazards(dt);
        if(scene!=Scene.PLAY)return;
        Iterator<Shot> sit=shots.iterator();
        while(sit.hasNext()) {
            Shot shot=sit.next();shot.life-=dt;
            if(shot.life<=0 || shot.target.hp<=0){sit.remove();continue;}
            float d=distance(shot.x,shot.y,shot.target.x,shot.target.y-10);
            if(d<24) {
                shot.target.hp-=shot.damage;
                int impact=shot.tower?GOLD:Progression.WEAPONS[shot.weaponId].color;
                if(shot.tower && shot.towerSlot==1) {
                    // Cannon shells hit clustered enemies around the impact point.
                    for(Enemy nearby:enemies)
                        if(nearby!=shot.target && nearby.hp>0 &&
                           distance(nearby.x,nearby.y,shot.target.x,shot.target.y)<95)
                            nearby.hp-=shot.damage*.44f;
                } else if(shot.tower && shot.towerSlot==2) {
                    // Frost turrets meaningfully reduce enemy movement for 2.6 seconds.
                    shot.target.slowTimer=2.6f;
                } else if(shot.tower && shot.towerSlot==3) {
                    // Arc turrets chain to a secondary target.
                    for(Enemy nearby:enemies)
                        if(nearby!=shot.target && nearby.hp>0 &&
                           distance(nearby.x,nearby.y,shot.target.x,shot.target.y)<125){
                            nearby.hp-=shot.damage*.65f;
                            break;
                        }
                }
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
                if(impacts.size()<64)impacts.add(new Impact(
                        shot.target.x,shot.target.y-15,impact,
                        shot.tower&&shot.towerSlot==1?85:39));
                audio.play(GameAudio.IMPACT);
                sit.remove();
            } else {
                // Clamp each step to target distance, preventing projectile oscillation.
                float step=Math.min(d,shot.speed*dt);
                shot.x+=(shot.target.x-shot.x)/Math.max(1,d)*step;
                shot.y+=(shot.target.y-10-shot.y)/Math.max(1,d)*step;
            }
        }
        Iterator<DeathEcho> deathIt=deaths.iterator();
        while(deathIt.hasNext()){
            DeathEcho death=deathIt.next();
            death.life-=dt;
            if(death.life<=0)deathIt.remove();
        }
        Iterator<Impact> impactIt=impacts.iterator();
        while(impactIt.hasNext()){
            Impact impact=impactIt.next();
            impact.life-=dt;
            if(impact.life<=0)impactIt.remove();
        }
        Iterator<Spark> pit=sparks.iterator();
        while(pit.hasNext()) {
            Spark s=pit.next();s.life-=dt;
            if(s.life<=0){pit.remove();continue;}
            s.x+=s.vx*dt;s.y+=s.vy*dt;s.vy+=42*dt;
        }
        Iterator<Pickup> drops=pickups.iterator();
        while(drops.hasNext()) {
            Pickup drop=drops.next();drop.age+=dt;
            if(drop.age>26f) {drops.remove();continue;}
            if(distance(heroX,heroY,drop.x,drop.y)<43) {
                if(drop.kind==0){heroHp=Math.min(kingdom.heroMaxHp(),heroHp+30);announce("HEALTH RESTORED +30");}
                else {gold+=35;announce("SUPPLY CACHE +35 GOLD");}
                puff(drop.x,drop.y,9,drop.kind==0?0xfff4949c:GOLD);
                drops.remove();
            }
        }
        if(remaining==0 && enemies.isEmpty()) {
            if(!waiting){waiting=true;intermission=2.5f;gold+=25;waveNotice=2.5f;}
            else {
                intermission-=dt;
                if(intermission<=0) {
                    if(wave>=DifficultyRules.waves(stage,difficultyMode)){complete();return;}
                    wave++;remaining=DifficultyRules.enemies(stage,wave,difficultyMode);
                    spawnTimer=1.6f;waveNotice=2;waiting=false;
                }
            }
        }
    }
    private void spawnEnemy() {
        float x=LaneRules.ENEMY_SPAWN_X-random.nextFloat()*26f;
        float y=LaneRules.stageEnemyY(stage,random.nextFloat()*96f-48f);
        int pool=Math.min(6,2+Campaign.world(stage)/2+(wave>=3?1:0));
        int kind=random.nextInt(Math.max(1,pool));
        // The Forest Boar becomes a recognizable charging enemy from wave two.
        if(wave>=2 && random.nextFloat()<.15f)kind=6;
        boolean elite=DifficultyRules.eliteFinalWave(stage,wave,difficultyMode)
                && remaining==1;
        if(elite)kind=5;
        float hp=BattleRules.enemyHealth(stage,kind)*DifficultyRules.health(difficultyMode);
        if(elite)hp*=DifficultyRules.finalBossMultiplier(stage);
        Enemy enemy=new Enemy(x,y,kind,hp,random.nextFloat()*6.28f);
        enemy.boss=elite;
        if(elite)enemy.specialTimer=5.0f;
        enemies.add(enemy);
        if(elite) {
            waveNotice=2.7f;
            announce(DifficultyRules.worldBoss(stage)?
                    "WORLD BOSS  •  "+DifficultyRules.worldBossName(stage):
                    "ELITE SIEGE COMMANDER INCOMING");
        }
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
        // Spawn the projectile precisely at the displayed weapon's rotating tip,
        // not at the center of the player sprite. This must use the same animation
        // time, attack fraction, and facing transform as MotionArt.hero().
        float animTime=heroMoving?walkClock:gameTime;
        float cast=pulseFlash>0?1-pulseFlash/.55f:-1f;
        WeaponRig.Point muzzle=WeaponRig.muzzle(equippedWeapon,0f,cast,
                animTime,heroMoving,heroDir);
        float muzzleX=heroX+muzzle.x,muzzleY=heroY+muzzle.y;
        shotTimer=weapon.cooldown;
        audio.play("MELEE".equals(weapon.type)?GameAudio.BLADE:GameAudio.SHOT);
        fireFlash=.23f;
        if("MELEE".equals(weapon.type)) {
            // Queue contact damage at the MIDPOINT of the animated sword swing.
            // No 360-degree instant hit the moment the attack button is pressed.
            pendingMelee=true;
            pendingMeleeWeapon=equippedWeapon;
            pendingMeleeFacing=heroDir;
        } else {
            Shot shot=new Shot(muzzleX,muzzleY,weapon.damage,
                    equippedWeapon==4?900:730,target,false);
            shot.weaponId=equippedWeapon;
            shots.add(shot);
        }
        // Recoil and flash emerge from the held weapon; melee sparks are emitted
        // at the cutting edge instead of an unrelated circle around the hero.
        puff(muzzleX,muzzleY,"MELEE".equals(weapon.type)?2:4,weapon.color);
    }
    private void resolveMeleeHit() {
        if(!pendingMelee)return;
        pendingMelee=false; // Exactly once per visible swing.
        if(pendingMeleeWeapon<0||pendingMeleeWeapon>=Progression.WEAPONS.length)
            return;
        Progression.Weapon weapon=Progression.WEAPONS[pendingMeleeWeapon];
        for(Enemy e:enemies) {
            if(e.hp<=0)continue;
            if(!MeleeSwingRules.hits(heroX,heroY,pendingMeleeFacing,weapon.range,
                    e.x,e.y))continue;
            e.hp-=weapon.damage;
            puff(e.x,e.y-18,7,weapon.color);
            if(impacts.size()<64)
                impacts.add(new Impact(e.x,e.y-18,weapon.color,32));
        }
        pendingMeleeWeapon=-1;
    }

    /** Kept as the existing Arc Pulse control entry point for compatibility. */
    private void pulse(){castSkill();}

    /** Every equipable skill has a distinct actual combat effect and world state. */
    private void castSkill() {
        if(pulseCooldown>0||scene!=Scene.PLAY||paused||briefingOpen)return;
        if(!WardenSkills.unlocked(fortressRank,equippedSkill))return;
        int skill=equippedSkill;
        WardenSkills.Skill info=WardenSkills.SKILLS[skill];
        pulseCooldown=WardenSkills.effectiveCooldown(skill,kingdom.shopRank(3));
        pulseFlash=.55f;
        lastCastSkill=skill;
        audio.play(GameAudio.SPELL);
        if(skill==WardenSkills.PULSE){
            battlePulses++;
            int hits=0;
            for(Enemy e:enemies)if(e.hp>0 &&
                    WardenSkills.inRadius(heroX,heroY,info.range,e.x,e.y)){
                e.hp-=70+Campaign.world(stage)*15;
                float dx=e.x-heroX,dy=e.y-heroY,d=Math.max(1,(float)Math.hypot(dx,dy));
                e.x=BattleRules.clamp(e.x+dx/d*75,20,WORLD_W-20);
                e.y=BattleRules.clamp(e.y+dy/d*75,20,WORLD_H-20);
                puff(e.x,e.y-20,5,info.color);hits++;
            }
            announce(hits==0?"ARC PULSE":"ARC PULSE  •  "+hits+" HIT");
        }else if(skill==WardenSkills.EMBER) {
            int hits=0;
            for(Enemy e:enemies)if(e.hp>0 &&
                    MeleeSwingRules.hits(heroX,heroY,heroDir,info.range,e.x,e.y)){
                e.hp-=95;e.burnTimer=3.5f;e.burnDps=22;
                puff(e.x,e.y-18,7,info.color);hits++;
            }
            announce("EMBER CLEAVE  •  "+hits+" BURNING");
        }else if(skill==WardenSkills.FROST){
            int hits=0;
            for(Enemy e:enemies)if(e.hp>0 &&
                    WardenSkills.inRadius(heroX,heroY,info.range,e.x,e.y)){
                e.hp-=48;e.slowTimer=Math.max(e.slowTimer,3.8f);
                puff(e.x,e.y-15,4,info.color);hits++;
            }
            announce("FROST NOVA  •  "+hits+" FROZEN");
        }else if(skill==WardenSkills.BANNER){
            bannerX=heroX;bannerY=heroY;bannerTime=10f;bannerTick=0;
            heroHp=WardenSkills.clampHealth(heroHp,24,kingdom.heroMaxHp());
            announce("HEALING BANNER DEPLOYED");
        }else if(skill==WardenSkills.TRAP){
            trapX=BattleRules.clamp(heroX+(float)Math.cos(heroDir)*88,26,WORLD_W-26);
            trapY=BattleRules.clamp(heroY+(float)Math.sin(heroDir)*88,26,WORLD_H-26);
            trapTime=16f;
            announce("SPIKE TRAP ARMED");
        }else if(skill==WardenSkills.METEOR){
            Enemy target=closest(heroX,heroY,info.range);
            meteorX=target==null?BattleRules.clamp(heroX+
                    (float)Math.cos(heroDir)*240,25,WORLD_W-25):target.x;
            meteorY=target==null?BattleRules.clamp(heroY+
                    (float)Math.sin(heroDir)*240,25,WORLD_H-25):target.y;
            meteorTime=1.1f;
            announce("METEOR INCOMING");
        }
        puff(heroX,heroY-38,12,info.color);
    }

    private void updateSkillObjects(float dt) {
        if(bannerTime>0){
            bannerTime=Math.max(0,bannerTime-dt);bannerTick-=dt;
            if(bannerTick<=0){
                bannerTick=1f;
                if(WardenSkills.inRadius(bannerX,bannerY,220,heroX,heroY))
                    heroHp=WardenSkills.clampHealth(heroHp,7,kingdom.heroMaxHp());
                if(WardenSkills.inRadius(bannerX,bannerY,550,BASE_X,BASE_Y))
                    baseHp=WardenSkills.clampHealth(baseHp,11,
                            Progression.maxCoreHp(fortressRank)+kingdom.coreBonusHp());
            }
        }
        if(trapTime>0){
            trapTime=Math.max(0,trapTime-dt);
            for(Enemy e:enemies)if(e.hp>0 &&
                    WardenSkills.inRadius(trapX,trapY,58,e.x,e.y)){
                for(Enemy hit:enemies)if(hit.hp>0 &&
                        WardenSkills.inRadius(trapX,trapY,106,hit.x,hit.y)){
                    hit.hp-=166;hit.slowTimer=Math.max(hit.slowTimer,2.0f);
                    puff(hit.x,hit.y-17,6,0xffe7c391);
                }
                if(impacts.size()<64)impacts.add(new Impact(trapX,trapY,
                        0xffe8b48a,72));
                trapTime=0;announce("TRAP DETONATED");break;
            }
        }
        if(meteorTime>0){
            meteorTime-=dt;
            if(meteorTime<=0) {
                meteorTime=0;
                for(Enemy e:enemies)if(e.hp>0 &&
                        WardenSkills.inRadius(meteorX,meteorY,170,e.x,e.y)){
                    e.hp-=230;e.burnTimer=3f;e.burnDps=19;
                    puff(e.x,e.y-18,7,0xffffb47b);
                }
                if(impacts.size()<64)impacts.add(new Impact(meteorX,meteorY,
                        0xffffad6b,150));
                puff(meteorX,meteorY,32,0xffffad6b);
                announce("METEOR IMPACT");
            }
        }
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
        audio.play(GameAudio.BUILD);
        puff(towerX[choice],towerY[choice],18,GOLD);
        announce(towers[choice]==1?"TURRET BUILT":"TURRET LEVEL "+towers[choice]);
    }
    private void recordBattleResult(boolean victory) {
        if(battleRecorded)return;
        battleRecorded=true;
        kingdom.recordBattle(stage,difficultyMode,kills,battlePulses,
                victory,System.currentTimeMillis());
    }
    private void markDefeat() {
        recordBattleResult(false);
        scene=Scene.LOSE;paused=false;
        clearTouchControls();
    }
    private void complete() {
        recordBattleResult(true);
        scene=Scene.WIN;paused=false;
        boolean firstClear=stage>=unlocked;
        int reward=Progression.stageReward(stage,firstClear);
        supplies+=reward+DifficultyRules.supplyBonus(difficultyMode,reward);
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
        // Stable one-route composition: fortress left, incoming enemies right.
        float camX=LaneRules.VIEW_CENTER_X,camY=LaneRules.VIEW_CENTER_Y;
        c.save();
        c.translate(W*.5f-camX*BATTLE_ZOOM,H*.5f-camY*BATTLE_ZOOM);
        c.scale(BATTLE_ZOOM,BATTLE_ZOOM);
        drawTerrain(c,biome);
        drawStageLandmarks(c,Campaign.map(stage),biome);
        drawStageIdentity(c,biome);
        drawStageHazards(c);
        drawPlacedSkills(c);
        // Stable depth sorting by world-space foot position. Trees can pass in
        // front of the hero while structures and monsters retain proper layering.
        drawQueue.clear();
        for(Decor d:decor)layer(DECOR,BiomeProps.groundDepth(d.y),d,0);
        // The castle walls and masonry mounts render before the working guns.
        // Previously upper turret weapons were depth-sorted behind the castle art.
        layer(KEEP,BASE_Y-305,null,0);
        for(int i=0;i<towers.length;i++)layer(TOWER,towerY[i]+31,null,i);
        for(Enemy e:enemies)layer(MONSTER,e.y+8,e,0);
        for(Pickup drop:pickups)layer(DROP,drop.y+12,drop,0);
        layer(WARDEN,heroY+9,null,0);
        drawQueue.sort((a,b)->Float.compare(a.depth,b.depth));
        for(DepthItem entry:drawQueue) {
            if(entry.type==DECOR)drawDecor(c,(Decor)entry.entity,biome);
            else if(entry.type==KEEP) {
                drawBase(c,biome);
                FortressDamageFx.draw(c,BASE_X,BASE_Y,fortressRank,baseHp,
                        Progression.maxCoreHp(fortressRank)+kingdom.coreBonusHp(),
                        gameTime);
                FortressStructures.turretMounts(c,BASE_X,BASE_Y,
                        fortressRank,towerX,towerY,gameTime);
            }
            else if(entry.type==TOWER) {
                c.save();
                c.translate(towerX[entry.slot],towerY[entry.slot]);
                c.scale(.83f,.83f);
                drawTower(c,0,0,towers[entry.slot],entry.slot,biome);
                c.restore();
            }
            else if(entry.type==MONSTER)drawEnemy(c,(Enemy)entry.entity);
            else if(entry.type==DROP)drawPickup(c,(Pickup)entry.entity);
            else if(entry.type==WARDEN)drawHero(c);
        }
        // Draw actual class-specific projectile silhouettes instead of identical dots.
        for(Shot s:shots){
            CombatVfx.projectile(c,s.x,s.y,s.target.x,s.target.y-10,
                    s.weaponId,s.tower?s.towerSlot:-1,gameTime);
        }
        // Death animation persists after combat removal; rewards still count once.
        for(DeathEcho d:deaths){
            float progress=1f-Math.max(0,d.life)/.64f;
            int alpha=Math.max(0,Math.min(255,(int)(195*(1f-progress))));
            c.save();
            c.rotate(70f*progress,d.x,d.y);
            int layer=c.saveLayerAlpha(d.x-100,d.y-150,d.x+100,d.y+40,alpha);
            motionArt.enemy(c,d.kind,d.x,d.y+progress*22f,d.phase,
                    false,-1,true);
            c.restoreToCount(layer);
            c.restore();
            CombatVfx.hit(c,d.x,d.y-16,progress,
                    GLOWS[Campaign.world(stage)],d.kind==5?90:47);
        }
        for(Impact impact:impacts) {
            CombatVfx.hit(c,impact.x,impact.y,1f-impact.life/.42f,
                    impact.color,impact.radius);
        }
        for(Spark s:sparks){
            p.setColor(s.color);p.setAlpha((int)(255*Math.max(0,s.life/s.maxLife)));
            c.drawCircle(s.x,s.y,2.5f+3*s.life/s.maxLife,p);p.setAlpha(255);
        }
        if(pulseFlash>0)drawActiveSkillVfx(c);
        c.restore();
        if(scene==Scene.PLAY)drawHud(c,biome);
        else drawHudTop(c,biome);
    }


    /** Persistent rune, trap and delayed meteor visuals are tied to real world positions. */
    private void drawPlacedSkills(Canvas c){
        if(bannerTime>0){
            float sway=(float)Math.sin(gameTime*3f)*4f;
            oval(c,bannerX-21,bannerY-7,bannerX+22,bannerY+7,0x6632a572);
            line(c,bannerX,bannerY-8,bannerX,bannerY-109,0xffe3ca94,7);
            polygon(c,new float[]{bannerX,bannerY-105,bannerX+51+sway,bannerY-93,
                    bannerX+31+sway,bannerY-56,bannerX,bannerY-66},0xff67bc8d);
            polygon(c,new float[]{bannerX+7,bannerY-98,bannerX+27+sway,bannerY-88,
                    bannerX+16,bannerY-75},0xffe7f9bf);
            for(int n=0;n<6;n++){
                float angle=(float)(n*Math.PI/3+gameTime*.5);
                float rad=34+(float)Math.sin(gameTime*2+n)*3;
                circle(c,bannerX+(float)Math.cos(angle)*rad,
                        bannerY-10+(float)Math.sin(angle)*rad*.35f,2.8f,0xffb9fbd0);
            }
        }
        if(trapTime>0){
            oval(c,trapX-29,trapY-12,trapX+29,trapY+12,0xb1373532);
            for(int n=0;n<8;n++){
                float a=(float)(n*Math.PI/4);
                float px=trapX+(float)Math.cos(a)*20,py=trapY+(float)Math.sin(a)*8;
                polygon(c,new float[]{px-6,py+4,px,py-15,px+6,py+4},
                        0xffd6ba84);
            }
            circle(c,trapX,trapY-10,4,0xffff9666);
        }
        if(meteorTime>0) {
            float t=1f-meteorTime/1.1f;
            // Ground-targeted warning telegraph, not a free-floating cast ring.
            oval(c,meteorX-122,meteorY-48,meteorX+122,meteorY+48,0x88cf4732);
            for(int n=0;n<12;n++){
                float a=(float)(n*Math.PI/6);
                float inner=104,outer=122;
                line(c,meteorX+(float)Math.cos(a)*inner,
                        meteorY+(float)Math.sin(a)*inner*.4f,
                        meteorX+(float)Math.cos(a)*outer,
                        meteorY+(float)Math.sin(a)*outer*.4f,0xffffc78d,4);
            }
            float drop=210*(1-t);
            polygon(c,new float[]{meteorX-23,meteorY-14-drop,meteorX+3,
                    meteorY-72-drop,meteorX+33,meteorY-10-drop,
                    meteorX+8,meteorY+9-drop},0xffff925c);
            circle(c,meteorX+3,meteorY-25-drop,13,0xffffdab0);
        }
    }

    /** Six skill families use different geometric effects, never one generic ring. */
    private void drawActiveSkillVfx(Canvas c){
        float t=1f-pulseFlash/.55f;
        int color=WardenSkills.SKILLS[lastCastSkill].color;
        float intensity=(float)Math.sin(Math.PI*Math.max(0,Math.min(1,t)));
        if(lastCastSkill==WardenSkills.PULSE){
            for(int n=0;n<9;n++){
                float a=(float)(n*Math.PI*2/9+heroDir);
                float r=225*Math.max(.15f,t);
                float mx=heroX+(float)Math.cos(a)*r*.55f;
                float my=heroY+(float)Math.sin(a)*r*.55f;
                line(c,heroX,heroY-26,mx+10,my-17,color,3+intensity*3);
                line(c,mx+10,my-17,heroX+(float)Math.cos(a)*r,
                        heroY+(float)Math.sin(a)*r-10,0xffe3fbff,2);
            }
        }else if(lastCastSkill==WardenSkills.EMBER){
            for(int n=-3;n<=3;n++){
                float angle=heroDir+n*.16f;
                float r=(80+Math.max(0,t)*160);
                float px=heroX+(float)Math.cos(angle)*r;
                float py=heroY+(float)Math.sin(angle)*r;
                line(c,heroX,heroY-53,px,py-35,color,5+intensity*7);
                circle(c,px,py-35,4+4*intensity,0xffffdc9e);
            }
        }else if(lastCastSkill==WardenSkills.FROST){
            for(int n=0;n<18;n++){
                float a=(float)(n*Math.PI/9);
                float r=195*Math.max(.12f,t);
                float x=heroX+(float)Math.cos(a)*r,y=heroY+(float)Math.sin(a)*r;
                polygon(c,new float[]{x-7,y,x,y-25-intensity*11,x+7,y,x,y+5},color);
            }
        }else if(lastCastSkill==WardenSkills.BANNER){
            for(int n=0;n<10;n++){
                float x=heroX+(n-4.5f)*15;
                float y=heroY-60-t*65+(float)Math.sin(n*1.3f)*9;
                circle(c,x,y,4*intensity+1,color);
            }
        }else if(lastCastSkill==WardenSkills.TRAP){
            float cx=trapX,cy=trapY;
            for(int n=0;n<12;n++){
                float a=(float)(n*Math.PI/6);
                line(c,heroX,heroY-54,
                        cx+(float)Math.cos(a)*20,cy-12,color,1.5f);
            }
        }else if(lastCastSkill==WardenSkills.METEOR){
            line(c,heroX,heroY-75,heroX+(float)Math.cos(heroDir)*65,
                    heroY-126,0xffffc58c,6);
            circle(c,heroX+(float)Math.cos(heroDir)*65,heroY-126,
                    8*intensity+2,0xffffeeae);
        }
    }

    private void updateStageHazards(float dt) {
        for(int i=0;i<StageHazards.count(stage);i++) {
            hazardTimers[i]=Math.max(0,hazardTimers[i]-dt);
            if(hazardTimers[i]>0||!StageHazards.active(stage,i,gameTime))continue;
            float x=StageHazards.x(stage,i),y=StageHazards.y(stage,i);
            float radius=StageHazards.radius(stage,i);
            boolean hit=false;
            if(distance(heroX,heroY,x,y)<radius) {
                // Venom, mist and shadow cause BOTH slowing and damage.
                // Brambles and frost slow only, to keep early regions approachable.
                if(StageHazards.slows(stage))hazardSlowTimer=1.3f;
                heroHp-=StageHazards.heroDamage(stage,difficultyMode);
                hit=true;
            }
            for(Enemy e:enemies)if(e.hp>0&&distance(e.x,e.y,x,y)<radius) {
                if(StageHazards.slows(stage))e.slowTimer=Math.max(e.slowTimer,1.6f);
                if(StageHazards.heroDamage(stage,difficultyMode)>0)
                    e.hp-=StageHazards.monsterDamage(stage);
                hit=true;
            }
            hazardTimers[i]=.86f;
            if(hit)puff(x,y,8,StageHazards.COLORS[StageHazards.kind(stage)]);
            if(heroHp<=0) {heroHp=0;markDefeat();return;}
        }
    }
    /** Environmental systems are independent terrain elements, never poster pixels. */
    private void drawStageHazards(Canvas c) {
        int kind=StageHazards.kind(stage);
        int color=StageHazards.COLORS[kind];
        for(int i=0;i<StageHazards.count(stage);i++) {
            float x=StageHazards.x(stage,i),y=StageHazards.y(stage,i);
            float r=StageHazards.radius(stage,i);
            float phase=StageHazards.charge(stage,i,gameTime);
            boolean active=StageHazards.active(stage,i,gameTime);
            oval(c,x-r,y-r*.34f,x+r,y+r*.34f,0x77202932);
            circleStroke(c,x,y,r,color&0x88ffffff,active?5:2);
            float pulse=1+(float)Math.sin(gameTime*5+i)*.12f;
            circle(c,x,y,active?14*pulse:9,active?color:0xff526d70);
            int particles=kind==3||kind==7||kind==9?9:5;
            for(int dot=0;dot<particles;dot++){
                float angle=(float)(dot*Math.PI*2/particles+gameTime*(active?1.3:.25));
                float rr=r*(.43f+phase*.47f);
                float px=x+(float)Math.cos(angle)*rr,py=y+(float)Math.sin(angle)*rr*.48f;
                circle(c,px,py,active?4:2,color);
            }
            if(kind==3 || kind==9) { // Magma and eclipse fissures.
                line(c,x-40,y-15,x-8,y+13,color,4);
                line(c,x-8,y+13,x+31,y-19,color,4);
            }else if(kind==4||kind==6){ // Ice and crystal spikes.
                polygon(c,new float[]{x-18,y+11,x-7,y-33,x+1,y+10},color);
                polygon(c,new float[]{x+1,y+9,x+17,y-29,x+26,y+11},0xffd5f3fb);
            }else if(kind==2||kind==8){ // Toxic swamps and shadow pools.
                oval(c,x-40,y-13,x+40,y+15,active?0x6640c77d:0x4444647c);
            }else if(kind==5){ // Rockfall debris.
                polygon(c,new float[]{x-18,y+7,x-14,y-19,x+12,y-28,x+27,y+5},
                        0xff958a77);
            }else { // Enchanted grass, storm and bramble triggers.
                for(int branch=0;branch<4;branch++){
                    float bx=x-25+branch*14;
                    line(c,bx,y+6,bx+4,y-19,0xffa2c8ab,3);
                }
            }
            if(active)circleStroke(c,x,y,r*.58f+(float)Math.sin(gameTime*9)*5,
                    0x99ffffff,2);
        }
    }

    private void drawTerrain(Canvas c,int biome) {
        if(!motionArt.terrain(c)){
            p.setShader(new LinearGradient(0,0,WORLD_W,WORLD_H,
                    lighten(GROUNDS[biome],.13f),darken(GROUNDS[biome],.34f),Shader.TileMode.CLAMP));
            c.drawRect(0,0,WORLD_W,WORLD_H,p);p.setShader(null);
        }
        // Ground tiles and subtle road markings.
        for(int x=0;x<=WORLD_W;x+=95) line(c,x,0,x,WORLD_H,0x163c4d4c,1);
        for(int y=0;y<=WORLD_H;y+=95) line(c,0,y,WORLD_W,y,0x163c4d4c,1);
        drawEnvironmentFeatures(c,biome);
        // True stage-unique lane: renderer, hitbox exclusion, enemy pathing,
        // spawn locations and progress share one pure route definition.
        Path road=new Path();
        road.moveTo(WORLD_W,LaneRules.stagePathY(stage,WORLD_W));
        for(float x=WORLD_W-18;x>=LaneRules.CASTLE_GATE_X-28;x-=18)
            road.lineTo(x,LaneRules.stagePathY(stage,x));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(0x663d473b);p.setStrokeWidth(110);
        c.drawPath(road,p);
        p.setColor(0xb06f6a50);p.setStrokeWidth(88);
        c.drawPath(road,p);
        p.setColor(0x55d3b58a);p.setStrokeWidth(4);
        c.drawPath(road,p);
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
    /** Region and mission specific terrain: swamps, canyons, ice, factories and siege scars. */
    private void drawEnvironmentFeatures(Canvas c,int biome) {
        Random ground=new Random(Campaign.seed(stage)^0x4192aef1);
        for(int i=0;i<26;i++) {
            float x=ground.nextFloat()*WORLD_W,y=ground.nextFloat()*WORLD_H;
            if(distance(x,y,BASE_X,BASE_Y)<260)continue;
            float r=17+ground.nextFloat()*40;
            if(biome==0 || biome==1) {
                oval(c,x-r,y-r*.24f,x+r,y+r*.24f,0x224abb65);
                if(biome==1) {
                    circle(c,x,y,r*.20f,0x6690fae2);
                    circle(c,x+13,y-8,r*.10f,0x88fff3bb);
                }
            } else if(biome==2) {
                oval(c,x-r,y-r*.6f,x+r,y+r*.6f,0xff406d68);
                oval(c,x-r*.73f,y-r*.4f,x+r*.73f,y+r*.38f,0xff789a81);
                circle(c,x-7,y+6,3,0xffb7d89b);
            } else if(biome==3 || biome==9) {
                line(c,x-r,y-r*.5f,x+r*.4f,y+r*.5f,0xff392f3a,12);
                line(c,x-r,y-r*.5f,x+r*.4f,y+r*.5f,0xfff08253,3);
                circle(c,x+r*.3f,y+r*.15f,5,0x66ffb471);
            } else if(biome==4) {
                oval(c,x-r,y-r*.28f,x+r,y+r*.36f,0x77e7f3ef);
                oval(c,x-r*.65f,y-r*.2f,x+r*.66f,y+r*.16f,0xffc4dce1);
            } else if(biome==5) {
                rect(c,x-r,y-r*.7f,x+r,y+r*.7f,0xff626f75);
                rect(c,x-r+4,y-r*.7f+4,x+r-4,y+r*.7f-4,0xff8d9285);
                for(int j=0;j<4;j++)circle(c,x-r*.7f+j*r*.5f,y,3,0xffaabbaf);
            } else if(biome==6) {
                polygon(c,new float[]{x-r,y+r*.4f,x,y-r,x+r,y+r*.4f},0x6686ddec);
                polygon(c,new float[]{x,y-r,x+r,y+r*.4f,x+r*.3f,y+r*.1f},0xff85d9eb);
            } else if(biome==7) {
                oval(c,x-r,y-r*.25f,x+r,y+r*.25f,0x66a0c1d2);
                line(c,x-10,y-12,x+15,y+16,0xffb3d1fa,2);
            } else {
                circleStroke(c,x,y,r*.58f,0x6686a4c1,2);
                circle(c,x,y,5,0x99b7a0df);
            }
        }
        if(Campaign.map(stage)==1) {
            // Variant B always features a real river crossing rather than just a color swap.
            Path river=new Path();
            river.moveTo(320,-60);
            river.cubicTo(120,250,540,510,290,1110);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(95);
            p.setColor(0xff294f5e);c.drawPath(river,p);
            p.setStrokeWidth(75);p.setColor(0xff6397a2);c.drawPath(river,p);
            p.setStrokeWidth(3);p.setColor(0x77d9ebec);c.drawPath(river,p);
            p.setStyle(Paint.Style.FILL);
            for(int i=0;i<9;i++) {
                float xx=240+i*24;
                rect(c,xx,451,xx+17,533,0xff8e7155);
                line(c,xx+2,451,xx+2,531,0xffbfa47b,3);
            }
        } else if(Campaign.map(stage)==2) {
            for(int i=0;i<5;i++) {
                float x=360+i*248,y=195+(i%2)*90;
                oval(c,x-40,y-20,x+40,y+20,0x553e2b2d);
                line(c,x-22,y-3,x+25,y+10,0xffa88672,5);
                line(c,x-19,y+13,x+18,y-13,0xff775f56,5);
            }
        }
    }
    private void drawDecor(Canvas c,Decor d,int biome) {
        int fade=BiomeProps.overlapsHero(d.x,d.y,d.size,heroX,heroY)?155:255;
        if(biomeProps.draw(c,biome,d.kind,d.x,d.y,d.size,gameTime,fade))return;
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
    /** Stage landmark animation: different defensive silhouettes, routes and weather. */
    private void drawStageLandmarks(Canvas c,int variant,int biome) {
        if(variant==0) {
            // Scout Outpost: forest glade, supply stakes and torch-lit watch path.
            for(int i=0;i<8;i++) {
                float x=465+(i%4)*320,y=160+(i/4)*750;
                float r=19+(i%3)*6;
                oval(c,x-r,y-8,x+r,y+9,0x66492f2d);
                for(int k=0;k<5;k++) {
                    float fx=x+(k-2)*8;
                    line(c,fx,y-2,fx+4,y-15,0xff98b56e,3);
                    circle(c,fx+4,y-17,3.5f,i%2==0?0xffe6b46f:0xffbbdd88);
                }
            }
            for(int side=-1;side<=1;side+=2) {
                float x=BASE_X+side*252,y=BASE_Y+115;
                line(c,x,y,x,y-47,0xff70563e,7);
                circle(c,x,y-50,11,0x55ffb96b);
                circle(c,x,y-56,5+(float)Math.sin(gameTime*7+x)*1.2f,0xffffbf68);
            }
        }else if(variant==1) {
            // Bridge Defense: wet stone approaches and riverside signal beacons.
            for(int i=0;i<16;i++) {
                float x=245+(i%4)*33,y=225+(i/4)*160;
                rect(c,x,y,x+17,y+12,0xff868f82);
                line(c,x,y+12,x+17,y+12,0xffb6c1af,2);
            }
            for(int i=0;i<5;i++) {
                float x=420+i*245,y=170+(i%2)*680;
                circle(c,x,y,24,0x444fc4df);
                circleStroke(c,x,y,16,0x99a2f7fb,2);
                circle(c,x,y,5,0xffcbf7f9);
            }
        }else {
            // Last Stand: corrupted siege scars, ballista emplacements and a portal.
            float gx=475,gy=192;
            circle(c,gx,gy,91,0x223c113e);
            circleStroke(c,gx,gy,73+(float)Math.sin(gameTime*2)*4,0x777a4dda,7);
            circleStroke(c,gx,gy,53,0x88e897f3,5);
            for(int k=0;k<12;k++) {
                float a=k*(float)(Math.PI/6)+gameTime*.18f;
                float px=gx+(float)Math.cos(a)*65;
                float py=gy+(float)Math.sin(a)*44;
                circle(c,px,py,4,0xffba8def);
            }
            for(int i=0;i<9;i++) {
                float x=210+i*165,y=880+(i%3)*40;
                oval(c,x-29,y-12,x+32,y+9,0xff504144);
                line(c,x-19,y-5,x+17,y+4,0xffae7359,6);
                line(c,x-8,y+9,x+20,y-17,0xff684e47,5);
            }
            for(int side=-1;side<=1;side+=2){
                float x=BASE_X+side*210,y=BASE_Y-85;
                line(c,x,y+60,x,y-66,0xff5e4e42,7);
                float flutter=(float)Math.sin(gameTime*3+side)*12;
                polygon(c,new float[]{x,y-65,x+side*(58+flutter),y-48,
                        x+side*11,y-16},0xff9c526c);
                circle(c,x,y-68,6,GOLD);
            }
        }
    }

    /** Physical keep footprint remains unchanged: wooden bases are solid in MovementRules. */
    private void drawStageIdentity(Canvas c,int biome) {
        // Every level produces its own deterministic group of structures.
        // Each object is rendered separately, never baked into a poster.
        int variant=StageFlavor.landmarkStyle(stage);
        int count=StageFlavor.props(stage);
        for(int i=0;i<count;i++){
            float x=StageFlavor.landmarkX(stage,i),y=StageFlavor.landmarkY(stage,i);
            float pulse=(float)Math.sin(gameTime*2+i)*3;
            oval(c,x-36,y+7,x+43,y+22,0x55000000);
            if(variant==0 || variant==4) { // Watchtowers / ruined villages
                rect(c,x-19,y-59,x+22,y+9,0xff816f5b);
                polygon(c,new float[]{x-33,y-59,x,y-96,x+33,y-59},
                        variant==0?0xff6f817b:0xff967058);
                rect(c,x-7,y-45,x+8,y-24,0xff253c48);
                if(variant==4)line(c,x-21,y-18,x+19,y-49,0xffb9ab8a,5);
            }else if(variant==1) { // War tents / camps
                polygon(c,new float[]{x-41,y+12,x-3,y-66,x+43,y+12},0xff9d7254);
                polygon(c,new float[]{x-3,y-66,x+43,y+12,x+2,y+12},0xffc59b6f);
                line(c,x-4,y-67,x-4,y+12,0xff604b3b,3);
            }else if(variant==2) { // Crystal and arc shrine
                circle(c,x,y-11,32,0x334cc3d7);
                polygon(c,new float[]{x-23,y,x,y-85-pulse,x+23,y},
                        biome==3?0xffffa066:0xff90ddef);
                polygon(c,new float[]{x,y-85-pulse,x+23,y,x,y+4},
                        0xff426e87);
                circle(c,x,y-51,7,0x99ffffff);
            }else if(variant==3) { // Barricades, carts and supply caches
                polygon(c,new float[]{x-42,y-7,x-37,y-46,x+36,y-46,x+43,y-7},
                        0xff82654e);
                for(int plank=0;plank<5;plank++)
                    line(c,x-33+plank*16,y-45,x-30+plank*16,y-9,0xffc1a077,4);
                circle(c,x-27,y+9,11,0xff35474d);
                circle(c,x+27,y+9,11,0xff35474d);
            }else if(variant==5) { // Broken stone arches
                rect(c,x-37,y-66,x-22,y+9,0xff8a9994);
                rect(c,x+20,y-66,x+35,y+9,0xff8a9994);
                polygon(c,new float[]{x-41,y-66,x-29,y-94,x+32,y-94,
                        x+38,y-66},0xffa3b6ac);
                line(c,x-27,y-12,x+27,y-12,0xff69777b,5);
            }else { // Windmills / waystations
                rect(c,x-17,y-55,x+17,y+10,0xffa99776);
                polygon(c,new float[]{x-25,y-55,x,y-83,x+24,y-55},0xff526e73);
                c.save();c.rotate(gameTime*19,x,y-47);
                line(c,x-31,y-47,x+31,y-47,0xffd9c99b,5);
                line(c,x,y-78,x,y-16,0xffd9c99b,5);
                c.restore();
            }
            // Dynamic beacons make layouts read as active locations.
            if(i%3==0) {
                circle(c,x+29,y-60,9+pulse*.4f,0x44ffa96b);
                circle(c,x+29,y-60,4,0xffffd483);
            }
        }
        int weather=StageFlavor.weather(stage);
        for(int i=0;i<23;i++){
            float x=1080+(i*117+(int)(gameTime*(weather==2?57:22)))%825;
            float y=65+(i*67+(int)(gameTime*(weather==1?48:15)))%910;
            if(weather==1)line(c,x,y,x+3,y+14,0x55d9f1ff,2);
            else if(weather==2)circle(c,x,y,2.3f,0x77d9efff);
            else if(weather==3)circle(c,x,y,2.2f,0x77ffc47c);
            else circle(c,x,y,1.5f,0x559ce4d1);
        }
    }

    private void drawStagePreview(Canvas c,int mission,float x,float top) {
        int signature=StageFlavor.signature(mission);
        int style=StageFlavor.landmarkStyle(mission);
        // Tiny independent silhouettes distinguish levels within the same biome.
        float anchor=x+45+(signature%170),y=top+50;
        if(style==0||style==4||style==6){
            rect(c,anchor-14,y-20,anchor+14,y+4,0xff344d57);
            polygon(c,new float[]{anchor-19,y-20,anchor,y-41,anchor+19,y-20},
                    0xffd2b37a);
            if(style==6)
                line(c,anchor-16,y-31,anchor+18,y-9,0xff8fd5eb,3);
        } else if(style==1||style==3){
            polygon(c,new float[]{anchor-18,y+4,anchor,y-35,anchor+18,y+4},
                    0xffc49c79);
            circle(c,anchor+31,y-9,5,0xffffcc7c);
        }else{
            polygon(c,new float[]{anchor-13,y+4,anchor,y-37,anchor+16,y+4},
                    0xffb0eaf5);
            circle(c,anchor,y-22,4,0xffeffaff);
        }
        text(c,""+Campaign.level(mission),x+274,top+23,17,0xfff6e1b1,true);
    }
    private void drawWoodOutpost(Canvas c,int biome) {
        float x=BASE_X,y=BASE_Y;
        oval(c,x-154,y+105,x+164,y+133,0x6607151a);
        round(c,x-144,y-116,x+144,y+118,18,0xff493c2d);
        round(c,x-129,y-105,x+129,y+100,13,0xff766148);
        for(int i=0;i<14;i++) {
            float px=x-139+i*21;
            // Pointed log stakes are individually shaded for a reinforced look.
            rect(c,px,y-119,px+15,y-93,0xff805f42);
            polygon(c,new float[]{px,y-116,px+7,y-134,px+15,y-116},0xffb79566);
            rect(c,px,y+98,px+15,y+119,0xff79583d);
            line(c,px+3,y+99,px+3,y+117,0xffae8b64,2);
        }
        for(int i=0;i<10;i++) {
            float py=y-106+i*22;
            rect(c,x-148,py,x-131,py+15,0xff866242);
            rect(c,x+131,py,x+148,py+15,0xff866242);
            line(c,x-145,py,x-145,py+16,0xffbe9566,3);
        }
        for(int i=0;i<6;i++) {
            float xx=x-95+i*37;
            line(c,xx,y-85,xx+14,y+86,0x22614233,2);
        }
        // Small living quarters/command hut with deep roof and live beacon.
        oval(c,x-86,y+45,x+88,y+69,0x66000000);
        rect(c,x-64,y-2,x+65,y+50,0xff9d815e);
        for(int i=0;i<7;i++)
            line(c,x-60+i*19,y+5,x-60+i*19,y+44,0xff644933,3);
        polygon(c,new float[]{x-86,y+5,x,y-62,x+86,y+5},0xff394e52);
        polygon(c,new float[]{x-86,y+5,x,y-62,x,y+5},0xff627b72);
        for(int i=0;i<7;i++) {
            float px=x-70+i*22;
            line(c,px,y-5-Math.max(0,55-Math.abs(px-x)*.65f),
                    px+16,y+1-Math.max(0,48-Math.abs(px-x)*.65f),0xff82a499,3);
        }
        round(c,x-20,y+9,x+20,y+50,4,0xff64432e);
        circle(c,x,y+22,15,GLOWS[biome]);
        circle(c,x,y+22,6,0xffffffff);
        round(c,x-38,y+96,x+38,y+128,6,0xff453d35);
        for(int i=0;i<5;i++)line(c,x-30+i*15,y+98,x-30+i*15,y+126,0xffb28c5b,4);
        if(fortressRank>=2) {
            // Visible reinforcement (metal corner braces and elevated outer balconies).
            for(int side=-1;side<=1;side+=2) {
                float tx=x+side*118;
                rect(c,tx-14,y-139,tx+14,y-76,0xff768384);
                polygon(c,new float[]{tx-22,y-138,tx,y-166,tx+22,y-138},
                        0xff496574);
                rect(c,tx-18,y-93,tx+18,y-82,0xffc0a37d);
            }
            rect(c,x-109,y+72,x+109,y+85,0xffa7a7a0);
        }
        line(c,x,y-61,x,y-99,0xffd9bb80,4);
        polygon(c,new float[]{x+2,y-98,x+42+(float)Math.sin(gameTime*3)*5,y-87,
            x+2,y-75},0xffd49c4d);
        if(baseHp<Progression.maxCoreHp(fortressRank)*.45f) {
            for(int i=0;i<3;i++) {
                float px=x-110+i*109;
                circle(c,px,y-109,11+(float)Math.sin(gameTime*7+i)*3,0x66ff9b46);
            }
        }
    }

    private void drawBase(Canvas c,int biome) {
        if(fortressRank>=3 && motionArt.fortress(c,BASE_X,BASE_Y,fortressRank,gameTime)) {
            // Damage and ambient smoke are independent of the painted building image.
            if(baseHp<Progression.maxCoreHp(fortressRank)*.48f) {
                for(int i=0;i<4;i++) {
                    float tx=BASE_X-122+i*79;
                    float drift=(gameTime*22+i*23)%92;
                    circle(c,tx+(float)Math.sin(gameTime+i)*8,
                            BASE_Y-152-drift,13+drift*.23f,0x44545a65);
                }
            }
            return;
        }
        if(fortressRank<3 && Campaign.map(stage)!=2) {
            drawWoodOutpost(c,biome);
            return;
        }
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
    private static int towerColor(int slot) {
        return new int[]{0xffffc678,0xffed9064,0xff91e4fb,0xffaf8dff}[slot];
    }
    /** Four distinctive functional fortress-mounted weapons. No generic sticks. */
    private void drawTower(Canvas c,float x,float y,int tier,int slot,int biome) {
        final int accent=towerColor(slot);
        float recoil=towerRecoil[slot]/.20f;
        oval(c,x-43,y+17,x+43,y+41,0x99091218);
        // Load-bearing parapet joins the tower to the castle structure.
        round(c,x-37,y-13,x+37,y+26,8,0xff455c64);
        polygon(c,new float[]{x-43,y-12,x-28,y-29,x+29,y-29,x+43,y-12},
                0xff829193);
        rect(c,x-34,y+9,x+34,y+20,0xffb0aba1);
        for(int i=0;i<4;i++)
            rect(c,x-36+i*22,y-22,x-24+i*22,y-12,0xffc0c5b9);
        if(tier==0) {
            round(c,x-26,y-10,x+26,y+11,9,0xff293b46);
            circleStroke(c,x,y-4,18,0xff89a6a7,3);
            text(c,"+",x,y+3,20,GOLD,true);
            return;
        }
        float top=y-42-tier*5;
        // Same base frame but four completely different gun assemblies.
        round(c,x-29,top-7,x+29,y-4,7,0xff6d8083);
        polygon(c,new float[]{x-31,top-6,x,top-26,x+31,top-6},0xff334c5e);
        round(c,x-19,top+4,x+20,top+24,5,0xff253b48);
        circle(c,x,top+12,10,accent);
        c.save();
        c.translate(x,top-12);
        c.rotate((float)Math.toDegrees(towerAim[slot]));
        float back=recoil*13;
        if(slot==0) { // Mounted ballista: twin limbs and visible bow string.
            rect(c,-20-back,-8,30-back,8,0xff9a7858);
            polygon(c,new float[]{-14,0,-36,-30,-47,-22,-32,4},0xffcda877);
            polygon(c,new float[]{-14,0,-36,30,-47,22,-32,-4},0xffcda877);
            line(c,-44,-23,30,0,0xfff6d6a0,2);
            line(c,-44,23,30,0,0xfff6d6a0,2);
            polygon(c,new float[]{47-back,0,27-back,-7,27-back,7},0xffdbe9ec);
        } else if(slot==1) { // Siege cannon: blackened iron barrel and orange vent.
            round(c,-25-back,-15,38-back,15,5,0xff414852);
            round(c,-11-back,-19,27-back,19,4,0xff6f747a);
            rect(c,29-back,-20,43-back,20,0xffa8a9a4);
            circle(c,43-back,0,13,0xff252b36);
            circle(c,43-back,0,6,0xffce6d48);
            for(int bolt=-1;bolt<=1;bolt++)
                circle(c,bolt*14-back,-14,3,0xffecc27c);
        } else if(slot==2) { // Frost launcher: icy prongs and rotating crystal.
            polygon(c,new float[]{-22,-18,8,-18,37-back,0,8,18,-22,18},
                    0xff8cb8ca);
            polygon(c,new float[]{48-back,0,18-back,-18,-1,-2,18-back,18},
                    0xffb7f0fb);
            circle(c,14-back,0,9+(float)Math.sin(gameTime*4)*2,0xffdffeff);
        } else { // Arc coil: forked electrodes and crackling voltaic orb.
            rect(c,-16,-11,20-back,11,0xff635a86);
            line(c,4,-9,39-back,-27,0xffb9acef,5);
            line(c,4,9,39-back,27,0xffb9acef,5);
            circle(c,39-back,0,12,0x77c39fff);
            circle(c,39-back,0,6,0xfff7e5ff);
            if(towerRecoil[slot]>0)
                line(c,39-back,-20,46-back,0,0xfff2d3ff,3);
        }
        c.restore();
        // Each purchase physically modifies the gun, not just its level text.
        if(tier>=2) {
            polygon(c,new float[]{x-34,top-2,x-17,top-20,x-14,top+18,x-35,top+30},
                    0xff9daeb0);
            polygon(c,new float[]{x+34,top-2,x+17,top-20,x+14,top+18,x+35,top+30},
                    0xff75898e);
            circle(c,x-27,top+4,5,accent);
            circle(c,x+27,top+4,5,accent);
        }
        if(tier>=3) {
            // Auxiliary barrel housings and metallic cooling vanes.
            round(c,x-31,top-28,x-17,top-4,4,0xff3f555e);
            round(c,x+17,top-28,x+31,top-4,4,0xff3f555e);
            circle(c,x-24,top-25,6,accent);
            circle(c,x+24,top-25,6,accent);
        }
        if(tier>=4) {
            float pulse=3+(float)Math.sin(gameTime*3+slot)*2;
            circleStroke(c,x,top-18,33+pulse,accent&0x99ffffff,3);
            for(int k=0;k<4;k++){
                float a=k*1.57f+gameTime*.6f;
                circle(c,x+(float)Math.cos(a)*35,
                        top-18+(float)Math.sin(a)*21,3.7f,accent);
            }
        }
        if(tier>=5) {
            polygon(c,new float[]{x-15,top-32,x,top-54,x+15,top-32},accent);
            line(c,x-5,top-31,x-5,top-48,0xffe5f7ef,2);
            circle(c,x,top-44,5,0xffefffff);
            circle(c,x,top-19,13,accent&0x55ffffff);
        }
        for(int j=0;j<tier;j++)
            circle(c,x-24+j*12,y+18,3.2f,accent);
        if(recoil>0) {
            circle(c,x,top-12,25+recoil*14,accent&0x55ffffff);
            circle(c,x,top-12,6+recoil*7,accent);
        }
    }
    private void drawEnemy(Canvas c,Enemy e) {
        if(e.boss) {
            float pulse=(float)Math.sin(gameTime*4)*5;
            float outer=e.specialCharge>0?115-30*e.specialCharge:78+pulse;
            circle(c,e.x,e.y-38,outer,0x335a2c85);
            circleStroke(c,e.x,e.y-38,outer,0x99b98dff,e.specialCharge>0?7:3);
            for(int i=0;i<9;i++){
                float angle=i*.698f+gameTime*.4f;
                circle(c,e.x+(float)Math.cos(angle)*outer*.9f,
                        e.y-38+(float)Math.sin(angle)*outer*.43f,
                        4,0xffdbb0ef);
            }
            if(e.specialCharge>0) {
                circleStroke(c,e.x,e.y-38,95+e.specialCharge*25,
                        0xffffc18f,5);
                text(c,"CHARGING",e.x,e.y-118,19,0xffffcb9c,true);
            }
        }
        float attack=e.attackAnim>0?1-e.attackAnim/.42f:-1f;
        if(motionArt.enemy(c,e.kind,e.x,e.y,e.phase,e.moving,attack,e.hp<e.maxHp*.20f)
                || characterArt.enemy(c,e.kind,e.x,e.y,gameTime+e.phase,e.hp<e.maxHp*.2f)) {
            if(e.hp<e.maxHp) {
                float w=e.kind==5?52:28;
                rect(c,e.x-w,e.y-(e.kind==5?110:78),e.x+w,e.y-(e.kind==5?104:72),0xff20313a);
                rect(c,e.x-w+1,e.y-(e.kind==5?109:77),
                        e.x-w+1+(w*2-2)*Math.max(0,e.hp/e.maxHp),
                        e.y-(e.kind==5?105:73),0xfffc987d);
            }
            return;
        }
        float bob=(float)Math.sin(e.phase)*4,scale=e.kind==2?1.6f:e.kind==1?.76f:1f;
        c.save();c.translate(e.x,e.y+bob);c.scale(scale,scale);
        oval(c,-24,15,24,27,0x66000000);
        int[] colors={0xffba6f61,0xffd9a45c,0xff788b9d,
                0xffad84cc,0xff70947a,0xffaa6781,0xffaf7963};
        int body=colors[Math.floorMod(e.kind,colors.length)];
        // Animated legs, shoulders, helmet, glowing eyes and species silhouettes.
        line(c,-12,5,-17+(float)Math.sin(e.phase)*7,22,0xff28333a,8);
        line(c,12,5,17-(float)Math.sin(e.phase)*7,22,0xff28333a,8);
        oval(c,-21,-24,21,14,body);
        oval(c,-15,-36,15,-7,lighten(body,.2f));
        if(e.kind==3){
            float flap=(float)Math.sin(e.phase*3.6f)*22f;
            polygon(c,new float[]{-18,-20,-46,-37-flap,-35,5,-17,0},0xffb7a3e4);
            polygon(c,new float[]{18,-20,46,-37-flap,35,5,17,0},0xffb7a3e4);
        }else if(e.kind==2){
            polygon(c,new float[]{-12,-32,-27,-61,0,-40},0xffd7d1b4);
            polygon(c,new float[]{12,-32,27,-61,0,-40},0xffd7d1b4);
        }else{
            polygon(c,new float[]{-14,-26,-28,-47,-2,-29},darken(body,.2f));
            polygon(c,new float[]{14,-26,28,-47,2,-29},darken(body,.2f));
        }
        circle(c,-7,-22,4,0xffffe899);circle(c,7,-22,4,0xffffe899);
        line(c,-4,-9,6,-9,0xff3f3038,3);
        if(e.kind==1) {
            polygon(c,new float[]{-13,-32,-18,-64,-1,-39},0xffe3b66a);
            polygon(c,new float[]{13,-32,18,-64,1,-39},0xffe3b66a);
        }else if(e.kind==4) {
            polygon(c,new float[]{-22,-33,0,-66,22,-33},0xff315c55);
            line(c,28,5,28,-44,0xffbca578,5);
            circle(c,28,-51,10,0xff86deb7);
        }else if(e.kind==5) {
            rect(c,-25,-3,25,19,0xff596674);
            rect(c,-9,-36,9,-18,0xff3d5264);
            line(c,20,-12,43,-33,0xffa4afb5,8);
            circle(c,43,-33,7,0xffff9a75);
        }
        if(e.hp<e.maxHp) {
            rect(c,-25,-52,25,-47,0xbb111c21);
            rect(c,-24,-51,-24+48*Math.max(0,e.hp/e.maxHp),-48,0xffff8c78);
        }
        c.restore();
    }
    private void drawPickup(Canvas c,Pickup drop) {
        float bob=(float)Math.sin(gameTime*4+drop.x)*5;
        float x=drop.x,y=drop.y+bob;
        oval(c,x-16,y+7,x+17,y+14,0x55000000);
        circle(c,x,y-13,17,drop.kind==0?0x55ff7387:0x66ffce70);
        if(drop.kind==0) {
            round(c,x-10,y-22,x+10,y+2,4,0xffc95a71);
            rect(c,x-6,y-26,x+6,y-19,0xffffd4a4);
            rect(c,x-2,y-17,x+2,y-3,0xffffffff);
            rect(c,x-7,y-12,x+7,y-8,0xffffffff);
        } else {
            polygon(c,new float[]{x-13,y-7,x-7,y-23,x+9,y-23,x+16,y-5,
                   x+5,y+3,x-10,y+3},0xffd6a35d);
            circle(c,x,y-11,7,0xffffd67b);
            text(c,"+",x,y-7,13,0xff665033,true);
        }
        circle(c,x+15,y-23,3,0xfff9edbe);
    }
    private void drawHero(Canvas c) {
        float attackProgress=fireFlash>0?1-fireFlash/.23f:-1f;
        float castProgress=pulseFlash>0?1-pulseFlash/.55f:-1f;
        if(motionArt.hero(c,heroX,heroY,heroMoving?walkClock:gameTime,
                heroDir,heroMoving,attackProgress,castProgress,
                hurtCooldown>.1f,equippedWeapon))return;
        if(characterArt.available()) {
            boolean moving=Math.abs(axisX)+Math.abs(axisY)>.1f;
            characterArt.hero(c,heroX,heroY,gameTime,moving,fireFlash>0,hurtCooldown>.1f);
            // The currently equipped weapon still changes independently of hero art.
            c.save(); c.translate(heroX+22,heroY-27);
            c.rotate(fireFlash>0?28:0);
            WorldArt.drawWeapon(c,equippedWeapon,0,0,.38f,gameTime);
            c.restore();
            return;
        }
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


    /** Fixed-height camera with safe, responsive HUD on 18:9–21:9 phones. */
    private float hudSideBleed() {
        float scale=Math.min(getWidth()/W,getHeight()/H);
        if(scale<=0)return 0;
        return Math.max(0,Math.min(220f,(getWidth()/scale-W)*.5f));
    }
    private float stickHudX(){return 153f-hudSideBleed()*.75f;}
    private float buildHudX(){return 849f+hudSideBleed()*.49f;}
    private float pulseHudX(){return 974f+hudSideBleed()*.65f;}
    private float attackHudX(){return 1130f+hudSideBleed()*.8f;}

    private void drawHud(Canvas c,int biome) {
        drawHudTop(c,biome);
        // Separate safe-area panel keeps game props behind—not over—touch controls.
        p.setShader(new LinearGradient(0,510,0,H,0x00112631,0xe8172b37,
                Shader.TileMode.CLAMP));
        c.drawRect(0,516,W,H,p);
        p.setShader(null);
        boolean bossActive=drawBossHud(c);
        // Left joystick and right action cluster: no UI obstruction in the center.
        float stickX=stickHudX();
        circle(c,stickX,568,94,0x77202c38);
        circleStroke(c,stickX,568,93,0x77d3ede6,3);
        circle(c,stickX+axisX*61,568+axisY*61,45,0xbba3c4c6);
        circle(c,stickX+axisX*61,568+axisY*61,26,0xff416578);
        int nextTier=nextTowerTier();
        action(c,buildHudX(),589,50,0xff4c837d,"BUILD",
                nextTier>=BattleRules.MAX_TOWER_TIER?"MAX":""+BattleRules.towerCost(nextTier));
        action(c,pulseHudX(),593,49,WardenSkills.SKILLS[equippedSkill].color,
                WardenSkills.SKILLS[equippedSkill].name.toUpperCase().split(" ")[0],
                pulseCooldown>0?""+(int)Math.ceil(pulseCooldown)+"s":"READY");
        action(c,attackHudX(),560,70,0xffb3684c,"ATTACK","FIRE");
        if(waveNotice>0 && !bossActive) {
            String text=waiting?"WAVE CLEARED!  +25 GOLD":"WAVE "+wave+"  •  DEFEND THE CORE";
            text(c,text,640,159,34,0xfff2e4bf,true);
        }
        if(noticeTimer>0 && !bossActive)
            text(c,notice,640,207,30,GOLD,true);
        text(c,"MOVE",stickX,688,17,0xffb5cfc5,true);
        if(!motionArt.weaponIcon(c,equippedWeapon,624,646,45,62))
            WorldArt.drawWeapon(c,equippedWeapon,624,646,.59f,gameTime);
        text(c,Progression.WEAPONS[equippedWeapon].name.toUpperCase(),
                624,703,17,GOLD,true);
        for(int i=0;i<StageHazards.count(stage);i++){
            if(StageHazards.active(stage,i,gameTime) &&
               distance(heroX,heroY,StageHazards.x(stage,i),
                       StageHazards.y(stage,i))<StageHazards.radius(stage,i)*2f) {
                text(c,"WARNING: "+StageHazards.NAMES[StageHazards.kind(stage)],
                        628,453,22,0xffffcaa0,true);
                break;
            }
        }
    }
    /** Boss display is a real HP snapshot, not a painted label. */
    private boolean drawBossHud(Canvas c) {
        for(Enemy e:enemies)if(e.boss&&e.hp>0) {
            round(c,415,109,865,166,14,0xe91f2536);
            String name=DifficultyRules.worldBoss(stage)?
                    DifficultyRules.worldBossName(stage):"ELITE SIEGE COMMANDER";
            text(c,name,640,130,20,0xffffd2a0,true);
            bar(c,441,137,399,16,Math.max(0,e.hp/e.maxHp),0xffc881d8);
            if(e.specialCharge>0)
                text(c,"INCOMING SHOCKWAVE  •  MOVE!",640,190,21,
                        0xffffc18f,true);
            return true;
        }
        return false;
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
        bar(c,316,47,243,18,baseHp/(Progression.maxCoreHp(fortressRank)+kingdom.coreBonusHp()),0xff79cfad);
        text(c,"HERO",319,84,15,0xffafc6c6,false);
        bar(c,377,69,182,13,heroHp/kingdom.heroMaxHp(),0xffe7a07e);
        text(c,"WAVE "+wave+" / "+DifficultyRules.waves(stage,difficultyMode),
                617,50,26,INK,false);
        text(c,"GOLD  "+gold,618,83,21,GOLD,false);
        text(c,"KILLS "+kills,870,51,21,INK,false);
        text(c,DifficultyRules.NAMES[difficultyMode],1120,110,17,GOLD,true);
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
        drawLiveMenuScene(c,656,80,553,548,false);
        button(c,667,455,1198,508,"KINGDOM HUB  /  QUESTS + SHOP",0xff805f40);
        rect(c,656,522,1209,630,0xd0142b33);
        text(c,"GREENHAVEN  /  THE LAST BASTION",679,563,24,GOLD,false);
        text(c,"A kingdom in danger. Your fortress grows with every victory.",
                679,595,16,INK,false);
        round(c,54,65,628,650,25,0xe6183039);
        text(c,"RIFTGUARD",87,183,80,GOLD,false);
        round(c,1010,42,1213,97,14,0xff324f5c);
        text(c,audio.enabled()?"SOUND: ON":"SOUND: OFF",
                1112,77,21,audio.enabled()?0xffb7f1c8:0xffffc0aa,true);
        text(c,"LAST BASTION",91,240,35,INK,false);
        rect(c,93,259,577,264,0xff709c9d);
        text(c,"RECLAIM THE TEN REALMS",94,318,26,0xffe2b995,false);
        text(c,"Defend • Earn Supplies • Upgrade • Unlock",94,355,23,INK,false);
        button(c,95,368,590,409,"RESOURCES  /  OFFLINE ART PACK",0xff33596c);
        button(c,95,415,590,490,"CAMPAIGN  /  WORLD MAP",0xffa56a49);
        button(c,95,509,328,572,"ARMORY",0xff3d6971);
        button(c,348,509,590,572,"FORTRESS",0xff44645c);
        text(c,"FORTRESS RANK "+fortressRank+"   •   SUPPLIES "+supplies,
                98,614,21,GOLD,false);
        text(c,"NO ADS  •  OFFLINE PLAY  •  OPTIONAL DOWNLOADS",101,643,15,0xff9eb8bb,false);
    }

    /** The UI and gameplay props here are independent from all artwork layers. */
    private void drawLiveMenuScene(Canvas c,float left,float top,float width,float height,
                                   boolean workshop) {
        int biome=Campaign.world(Math.min(unlocked,299));
        c.save();c.clipRect(left,top,left+width,top+height);
        WorldArt.drawScene(c,biome,workshop?1:2,left,top,width,height,gameTime);
        rect(c,left,top+height*.77f,left+width,top+height,0x440b2431);
        float centerX=left+width*.44f;
        float bottomY=top+height*.69f;
        if(fortressRank>=3 && motionArt.fortressReady()){
            motionArt.fortress(c,centerX,bottomY,fortressRank,gameTime);
        }else{
            c.save();
            float sz=workshop?.88f:.74f;
            c.translate(centerX,bottomY);c.scale(sz,sz);
            c.translate(-BASE_X,-BASE_Y);
            drawWoodOutpost(c,biome);
            c.restore();
        }
        // Individually animated hero, creatures, flag and floating ambient lights.
        float heroX=left+width*.79f,heroY=top+height*.81f;
        boolean rendered=motionArt.hero(c,heroX,heroY,gameTime,
                -2.2f,false,-1,-1,false,equippedWeapon);
        if(!rendered)WorldArt.drawWeapon(c,equippedWeapon,heroX,heroY-58,.9f,gameTime);
        if(!workshop) {
            motionArt.enemy(c,0,left+width*.91f,top+height*.86f,gameTime,
                    true,-1,false);
            motionArt.enemy(c,6,left+width*.87f,top+height*.58f,gameTime,
                    true,-1,false);
        }
        for(int i=0;i<16;i++) {
            float px=left+(i*97+gameTime*(i%2==0?7:12))%width;
            float py=top+42+(i*71)%Math.max(60,height-98);
            float glow=2+(float)Math.sin(gameTime*3+i)*.9f;
            circle(c,px,py,glow*2,0x449cefc8);
            circle(c,px,py,glow*.7f,0xffe7f4c1);
        }
        c.restore();
    }

    private void roundedPanel(Canvas c,float l,float t,float r,float b) {
        round(c,l-5,t-5,r+5,b+5,18,0xffd0a878);
        round(c,l,t,r,b,14,0xff25434b);
    }

    /** All world artwork, route markers and previews are independent game layers. */
    private void drawMap(Canvas c) {
        drawBackdrop(c);
        round(c,35,21,1245,700,27,0xf0162b32);
        text(c,"RIFTGUARD  /  THE KINGDOM ATLAS",65,82,38,GOLD,false);
        text(c,"100 LEVELS   /   300 MISSIONS",917,82,21,INK,false);
        round(c,64,111,879,578,21,0xff9f845f);
        int current=(selectedLevel-1)/10;
        if(regionalMap)
            RegionJourney.draw(c,current,selectedLevel,unlocked,gameTime,biomeProps);
        else AtlasArt.draw(c,current,unlocked,gameTime);
        button(c,729,48,873,93,regionalMap?"WORLD":"EXPLORE",0xff416c71);
        round(c,893,111,1216,576,20,0xff182f3a);
        text(c,Campaign.WORLDS[current].toUpperCase(),912,155,23,GOLD,false);
        text(c,"LEVEL "+selectedLevel+" / 100",916,185,22,INK,false);
        for(int m=0;m<3;m++) {
            int mission=Campaign.stage(selectedLevel,m);
            boolean accessible=Campaign.accessible(mission,unlocked);
            float top=203+m*108;
            round(c,901,top-3,1208,top+103,12,selectedMap==m?GOLD:0xff45616b);
            c.save();
            c.clipRect(905,top,1204,top+68);
            WorldArt.drawScene(c,current,m,905,top,299,68,gameTime);
            drawStagePreview(c,mission,905,top);
            c.restore();
            rect(c,905,top+67,1204,top+99,0xee182e37);
            text(c,(char)('A'+m)+"  "+Campaign.MAPS[m],914,top+90,19,INK,false);
            if(DifficultyRules.worldBoss(mission)) {
                round(c,1090,top+4,1197,top+28,6,0xdd71384e);
                text(c,"WORLD BOSS",1143,top+22,15,0xffffdfbc,true);
            }
            text(c,accessible?(mission<unlocked?"DONE":"GO"):"LOCK",
                    1173,top+91,15,accessible?0xffb6e3a0:0xffffb5a4,true);
        }
        text(c,"< LEVEL",936,551,22,GOLD,false);
        text(c,"LEVEL >",1199,551,22,GOLD,true);
        int chosen=Campaign.stage(selectedLevel,selectedMap);
        button(c,406,599,875,655,
                Campaign.accessible(chosen,unlocked)?
                        "DEPLOY TO MAP "+(char)('A'+selectedMap):"UNLOCK PREVIOUS STAGE",
                Campaign.accessible(chosen,unlocked)?0xffb1764c:0xff46515b);
        text(c,"BACK",81,652,24,INK,false);
        // Fully interactive difficulty selector; affects actual combat, not art alone.
        text(c,"BATTLE DIFFICULTY",905,583,17,INK,false);
        for(int mode=0;mode<4;mode++) {
            float x=903+mode*78;
            round(c,x,594,x+74,633,9,mode==difficultyMode?0xffb97944:0xff344d58);
            text(c,DifficultyRules.NAMES[mode],x+37,620,mode==3?13:14,
                    mode==difficultyMode?0xffffffff:0xffd0dee0,true);
        }
        text(c,"REWARD +" +(int)(100f*new float[]{0f,0f,.22f,.55f}[difficultyMode])+
                "%",906,666,15,GOLD,false);
        text(c,"CLEARED: "+unlocked+" / 300",1087,686,17,GOLD,true);
    }

    private static final String[] KINGDOM_TABS={
            "DAILY","QUESTS","EVENTS","ACHIEVEMENTS","SHOP"};
    private void kingdomToast(int reward,String action) {
        if(reward>0) {
            supplies=kingdom.supplies();
            kingdomMessage=action+"  +"+reward+" SUPPLIES";
        }else kingdomMessage="NOT READY YET  •  CHECK PROGRESS OR SUPPLIES";
    }
    private void drawKingdom(Canvas c) {
        drawBackdrop(c);
        round(c,36,28,1244,695,22,0xf017303d);
        text(c,"KINGDOM HALL",76,91,48,GOLD,false);
        text(c,"OFFLINE REWARDS  •  REAL PERMANENT PROGRESSION",
                79,122,20,0xffb2d6d0,false);
        text(c,"SUPPLIES  "+supplies,1198,91,27,GOLD,true);
        for(int i=0;i<KINGDOM_TABS.length;i++) {
            float x=78+i*226;
            round(c,x,148,x+216,204,10,
                    kingdomTab==i?0xffab7951:0xff3d5863);
            text(c,KINGDOM_TABS[i],x+108,185,22,INK,true);
        }
        long now=System.currentTimeMillis();
        if(kingdomTab==0)drawDailyHall(c,now);
        else if(kingdomTab==1)drawQuestsHall(c,now);
        else if(kingdomTab==2)drawEventHall(c,now);
        else if(kingdomTab==3)drawAchievementHall(c);
        else drawShopHall(c);
        text(c,kingdomMessage,876,126,16,0xffbfe8c2,true);
        button(c,82,632,346,681,"HOME",0xff42646f);
        button(c,366,632,675,681,"CAMPAIGN",0xff956744);
        text(c,"NO ADS  •  NO LOGIN  •  STORED ON DEVICE",
                1200,665,17,0xffa7c8c3,true);
    }
    private void drawDailyHall(Canvas c,long now) {
        text(c,"DAILY SUPPLY CARAVAN",99,263,35,GOLD,false);
        text(c,"Claim once per UTC day. Consecutive days grow your reward.",
                101,301,22,INK,false);
        int streak=kingdom.dailyStreak();
        for(int i=0;i<7;i++) {
            float x=103+i*154,y=338;
            round(c,x,y,x+141,y+125,12,
                    i<streak?0xff577b69:0xff344c57);
            text(c,"DAY "+(i+1),x+70,y+33,22,GOLD,true);
            text(c,""+(90+i*25),x+70,y+75,28,INK,true);
            text(c,i<streak?"COMPLETE":"SUPPLIES",x+70,y+103,16,INK,true);
        }
        boolean available=kingdom.canClaimDaily(now);
        button(c,355,486,927,563,available?
                "CLAIM  +"+kingdom.dailyReward(now)+" SUPPLIES":"ALREADY CLAIMED TODAY",
                available?0xffa36b43:0xff50636b);
    }
    private void drawQuestsHall(Canvas c,long now) {
        text(c,"TODAY'S QUESTS",99,254,32,GOLD,false);
        for(int i=0;i<3;i++) {
            float y=286+i*98;
            int current=kingdom.questProgress(i,now),target=KingdomMeta.QUEST_TARGETS[i];
            boolean claimed=kingdom.questClaimed(i,now),ready=current>=target;
            round(c,95,y,1184,y+85,14,0xff304c59);
            text(c,KingdomMeta.QUEST_NAMES[i],121,y+36,25,INK,false);
            text(c,Math.min(current,target)+" / "+target,
                    696,y+34,21,GOLD,true);
            bar(c,490,y+49,358,13,current/(float)target,0xff8bcea5);
            round(c,882,y+12,1166,y+73,11,
                    claimed?0xff4d625d:ready?0xffab774b:0xff465963);
            text(c,claimed?"CLAIMED":ready?
                    "CLAIM +"+KingdomMeta.QUEST_REWARDS[i]:"IN PROGRESS",
                    1023,y+51,21,INK,true);
        }
        text(c,"Progress saves after each battle.",
                112,589,17,0xffadc6c4,false);
    }
    private void drawEventHall(Canvas c,long now) {
        int index=kingdom.eventIndex(now),target=kingdom.eventTarget(now);
        int current=kingdom.eventProgress(now);
        round(c,96,236,1186,559,23,0xff354a5c);
        text(c,"WEEKLY KINGDOM EVENT",128,284,29,GOLD,false);
        text(c,KingdomMeta.EVENT_NAMES[index],128,344,43,INK,false);
        String objective=index==0?"Slay 70 enemies in any battle":
                index==1?"Win six fortress defenses":"Defeat a world boss on Map C";
        text(c,objective,129,393,23,INK,false);
        bar(c,130,420,720,22,current/(float)target,0xffdca475);
        text(c,Math.min(current,target)+" / "+target,895,438,24,GOLD,true);
        boolean ready=current>=target,claimed=kingdom.eventClaimed(now);
        button(c,414,472,963,538,claimed?"EVENT REWARD CLAIMED":
                ready?"CLAIM +"+kingdom.eventReward(now)+" SUPPLIES":
                        "COMPLETE THE EVENT OBJECTIVE",
                ready&&!claimed?0xffa97948:0xff4d6471);
    }
    private void drawAchievementHall(Canvas c) {
        for(int i=0;i<KingdomMeta.ACHIEVEMENTS.length;i++) {
            float x=89+(i%3)*375,y=232+(i/3)*176;
            int current=kingdom.achievementProgress(i),target=kingdom.achievementTarget(i);
            boolean claimed=kingdom.achievementClaimed(i),ready=current>=target;
            round(c,x,y,x+355,y+158,14,0xff34515b);
            text(c,KingdomMeta.ACHIEVEMENTS[i],x+15,y+36,21,GOLD,false);
            text(c,Math.min(current,target)+" / "+target,x+15,y+75,22,INK,false);
            bar(c,x+140,y+59,185,16,current/(float)target,0xffa3cfa5);
            round(c,x+14,y+93,x+339,y+145,9,
                    claimed?0xff46635c:ready?0xffa9794c:0xff455a63);
            text(c,claimed?"CLAIMED":ready?
                    "CLAIM +"+KingdomMeta.ACHIEVEMENT_REWARDS[i]:"LOCKED",
                    x+176,y+125,21,INK,true);
        }
    }
    private void drawShopHall(Canvas c) {
        for(int i=0;i<KingdomMeta.SHOP_NAMES.length;i++) {
            float x=89+(i%2)*563,y=232+(i/2)*175;
            int rank=kingdom.shopRank(i),cost=kingdom.shopCost(i);
            boolean ready=cost>0&&supplies>=cost;
            round(c,x,y,x+546,y+163,14,0xff354f59);
            text(c,KingdomMeta.SHOP_NAMES[i],x+22,y+39,28,GOLD,false);
            text(c,KingdomMeta.SHOP_DETAILS[i],x+23,y+79,21,INK,false);
            text(c,"RANK "+rank+" / 5",x+25,y+119,20,0xffb2d4cd,false);
            round(c,x+232,y+98,x+532,y+148,10,ready?0xffaa7951:0xff485c67);
            text(c,rank==5?"MAX UPGRADE":"BUY  "+cost+" SUPPLIES",
                    x+382,y+131,20,INK,true);
        }
        text(c,"Upgrades persist through all battles.",110,586,17,INK,false);
    }
    private void tapKingdom(float x,float y) {
        if(y>=142 && y<211 && x>=76 && x<1210) {
            int tab=(int)((x-78)/226);
            kingdomTab=Math.max(0,Math.min(4,tab));return;
        }
        if(y>=629 && y<695) {
            if(x<352)scene=Scene.HOME;
            else if(x<685)scene=Scene.MAP;
            return;
        }
        long now=System.currentTimeMillis();
        if(kingdomTab==0 && y>=475 && y<572)
            kingdomToast(kingdom.claimDaily(now),"DAILY CLAIMED");
        else if(kingdomTab==1 && x>=880 && x<1180 && y>=285 && y<574) {
            int i=(int)((y-286)/98);
            if(i>=0&&i<3&&y<286+i*98+85)
                kingdomToast(kingdom.claimQuest(i,now),"QUEST COMPLETE");
        } else if(kingdomTab==2 && x>=405 && x<975 && y>=468 && y<547)
            kingdomToast(kingdom.claimEvent(now),"EVENT COMPLETE");
        else if(kingdomTab==3 && x>=85 && x<1240 && y>=223 && y<570) {
            int col=(int)((x-89)/375),row=(int)((y-232)/176);
            if(col>=0&&col<3&&row>=0&&row<2) {
                int i=row*3+col;
                float dx=(x-89)-col*375,dy=(y-232)-row*176;
                if(dx>=14&&dx<339&&dy>=93&&dy<148)
                    kingdomToast(kingdom.claimAchievement(i),"ACHIEVEMENT");
            }
        } else if(kingdomTab==4 && x>=85 && x<1240 && y>=223 && y<581) {
            int col=(int)((x-89)/563),row=(int)((y-232)/175);
            if(col>=0&&col<2&&row>=0&&row<2) {
                float dx=(x-89)-col*563,dy=(y-232)-row*175;
                if(dx>=232&&dx<534&&dy>=98&&dy<151) {
                    boolean bought=kingdom.buy(row*2+col);
                    supplies=kingdom.supplies();
                    kingdomMessage=bought?"PERMANENT UPGRADE PURCHASED":
                            "NOT ENOUGH SUPPLIES OR MAX RANK";
                }
            }
        }
    }

    private void drawResources(Canvas c) {
        drawBackdrop(c);
        round(c,65,44,1215,669,29,0xee172d36);
        text(c,"RESOURCE CENTER",113,117,54,GOLD,false);
        text(c,"OPTIONAL DOWNLOAD  •  NO ADS  •  OFFLINE PLAY",
                116,161,25,INK,false);
        ResourcePacks.State pack=resourceState;
        round(c,110,193,1169,381,24,0xff284853);
        text(c,"RIFTGUARD ORIGINAL ART PACK",147,249,32,GOLD,false);
        text(c,"Cached files: "+pack.present+" / "+pack.total,150,298,26,INK,false);
        text(c,"Device storage used: "+(pack.bytes/1024)+" KB",
                150,340,20,0xffa8cecc,false);
        text(c,"The base game already works without an internet connection.",
                116,427,23,INK,false);
        text(c,"After download, the artwork stays on the phone for offline use.",
                116,469,23,INK,false);
        text(c,resourceMessage,116,512,20,resourceSuccess?0xff9fe4b2:0xffffd0ac,false);
        bar(c,116,530,1043,16,resourcePercent/100f,0xff8bd6b2);
        button(c,117,564,859,638,resourceBusy?"DOWNLOADING...":
                "DOWNLOAD / VERIFY ART PACK",resourceBusy?0xff566a72:0xffae704c);
        button(c,888,564,1158,638,"BACK",0xff42606b);
        text(c,"Reopen app to apply the downloaded artwork.",118,666,16,0xffb4c9cb,false);
    }
    private void beginResourceDownload() {
        if(resourceBusy)return;
        resourceBusy=true;resourceSuccess=false;resourcePercent=0;
        resourceMessage="Connecting to GitHub...";
        ResourcePacks.start(getContext(),(message,percent,done,success)->{
            resourceMessage=message;
            resourcePercent=percent;
            if(done){
                resourceBusy=false;
                resourceSuccess=success;
                resourceState=ResourcePacks.state(getContext());
            }
            invalidate();
        });
    }

    private void drawArmory(Canvas c) {
        drawBackdrop(c);
        round(c,43,28,1237,695,25,0xf0172d35);
        text(c,"WARDEN'S ARMORY",80,87,51,GOLD,false);
        text(c,"Equip unlocked weapons. Each has different damage, reach and attack speed.",
                83,123,22,INK,false);
        text(c,"FORT "+fortressRank+"   |   SUPPLIES "+supplies,1008,88,21,GOLD,true);
        for(int i=0;i<Progression.WEAPONS.length;i++){
            Progression.Weapon w=Progression.WEAPONS[i];
            boolean available=Progression.canEquip(fortressRank,i);
            float x=79+(i%3)*375,y=155+(i/3)*218;
            round(c,x-5,y-4,x+345,y+198,18,
                    i==equippedWeapon?GOLD:0xff49616b);
            round(c,x,y,x+340,y+192,14,available?0xff294955:0xff293640);
            round(c,x+12,y+15,x+128,y+167,12,0xff172d39);
            if(!motionArt.weaponIcon(c,i,x+69,y+96,100,125))
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
        button(c,696,625,1118,679,"EQUIP COMBAT ABILITIES",0xff53699a);
    }

    /** Interactive six-skill loadout; one ability active during each defense. */
    private void drawSkills(Canvas c){
        drawBackdrop(c);
        round(c,43,28,1237,695,25,0xf0172d35);
        text(c,"WARDEN'S ABILITIES",80,87,49,GOLD,false);
        text(c,"Equip a spell unlocked by fortress upgrades. Effects change real combat.",
                83,123,21,INK,false);
        text(c,"FORT "+fortressRank+"  /  SKILL EQUIPPED",1042,86,20,GOLD,true);
        for(int i=0;i<WardenSkills.SKILLS.length;i++){
            WardenSkills.Skill skill=WardenSkills.SKILLS[i];
            boolean unlockedSkill=WardenSkills.unlocked(fortressRank,i);
            float x=79+(i%3)*375,y=155+(i/3)*218;
            round(c,x-5,y-4,x+345,y+198,18,
                    i==equippedSkill?GOLD:0xff49616b);
            round(c,x,y,x+340,y+192,14,
                    unlockedSkill?0xff294955:0xff293640);
            round(c,x+10,y+15,x+129,y+167,12,0xff172e3a);
            drawSkillSymbol(c,i,x+69,y+98,44);
            text(c,skill.name,x+146,y+51,24,unlockedSkill?INK:0xffa2a6aa,false);
            text(c,"CD "+(int)skill.cooldown+"s  /  RANK "+skill.requiredRank,
                    x+145,y+87,18,skill.color,false);
            text(c,skill.detail,x+146,y+114,16,INK,false);
            text(c,unlockedSkill?(i==equippedSkill?"EQUIPPED  ✓":"TAP TO EQUIP"):
                    "LOCKED UNTIL RANK "+skill.requiredRank,
                    x+145,y+160,17,unlockedSkill?GOLD:0xffffaa9c,false);
        }
        button(c,80,625,317,679,"BACK TO HOME",0xff3d6067);
        button(c,351,625,648,679,"WEAPONS",0xff9d6b4e);
        button(c,696,625,1118,679,"UPGRADE FORTRESS",0xff53699a);
    }

    private void drawSkillSymbol(Canvas c,int id,float x,float y,float size) {
        int color=WardenSkills.SKILLS[id].color;
        circle(c,x,y,37,0x66334c66);
        if(id==WardenSkills.PULSE){
            polygon(c,new float[]{x-12,y-37,x+6,y-10,x-3,y+1,x+16,y+11,
                    x-11,y+39,x-4,y+10,x-18,y},color);
        }else if(id==WardenSkills.EMBER) {
            polygon(c,new float[]{x,y-43,x+20,y-6,x+8,y+3,x+25,y+25,
                    x,y+40,x-25,y+17,x-5,y-6},color);
        }else if(id==WardenSkills.FROST) {
            for(int n=0;n<6;n++){
                float a=(float)(n*Math.PI/3);
                line(c,x,y,x+(float)Math.cos(a)*38,y+(float)Math.sin(a)*38,color,6);
            }
        }else if(id==WardenSkills.BANNER){
            line(c,x-13,y+37,x-13,y-40,0xffffe5b3,7);
            polygon(c,new float[]{x-11,y-37,x+32,y-29,x+13,y+2,x-11,y-5},color);
        }else if(id==WardenSkills.TRAP) {
            for(int n=0;n<5;n++){
                float px=x+(n-2)*15;
                polygon(c,new float[]{px-9,y+22,px,y-35+Math.abs(n-2)*6,
                        px+9,y+22},color);
            }
        }else{
            polygon(c,new float[]{x+8,y-46,x+32,y-13,x+16,y+28,x-18,y+33,
                    x-30,y+7},color);
            line(c,x-36,y+36,x+35,y-38,0xffffdd9a,5);
        }
    }

    private void equipSkill(int skillId){
        if(!WardenSkills.unlocked(fortressRank,skillId))return;
        equippedSkill=skillId;
        prefs.edit().putInt("skill",skillId).apply();
    }

    private void drawFortress(Canvas c) {
        drawBackdrop(c);
        round(c,48,29,1233,694,25,0xf0172e36);
        text(c,"FORTRESS WORKSHOP",85,86,50,GOLD,false);
        text(c,"Upgrade the base to unlock weapons, towers and extra core health.",
                88,125,21,INK,false);
        drawLiveMenuScene(c,75,158,600,390,true);
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
        audio.play(GameAudio.BUILD);
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
    /**
     * Storybook mission panel is a real tap-gated overlay above a live battlefield.
     * The simulation is frozen until the player deliberately selects BEGIN BATTLE.
     * Artwork, hitboxes and mission state remain separate from narrative text.
     */
    private void drawMissionBriefing(Canvas c) {
        MissionStory.Briefing story=MissionStory.forStage(stage);
        rect(c,0,0,W,H,0xd7081520);
        round(c,76,48,1204,680,30,0xff122732);
        border(c,76,48,1204,680,30,0xffd2ae70,4);
        round(c,103,75,1175,126,9,0xff2a4650);
        text(c,"THE RIFT CHRONICLES  /  MISSION BRIEFING",122,110,29,GOLD,false);
        text(c,story.chapter,125,161,22,0xffc5e5dc,false);
        // Actual stage-specific illustrated environment, not a map number tile.
        round(c,119,192,564,533,16,0xff486676);
        c.save();
        c.clipRect(127,200,556,524);
        WorldArt.drawScene(c,Campaign.world(stage),Campaign.map(stage),
                127,200,429,324,gameTime);
        drawStagePreview(c,stage,127,200);
        c.restore();
        border(c,127,200,556,524,11,0xffb78a57,3);
        text(c,story.headline,593,225,27,GOLD,false);
        text(c,"THE CHRONICLE",596,270,18,0xff9cb6ba,false);
        drawStoryParagraph(c,story.lore,594,305,552,23,33,0xffecede5);
        text(c,"WARDEN'S ORDERS",596,384,18,0xff9cb6ba,false);
        drawStoryParagraph(c,story.order,594,418,552,22,31,INK);
        text(c,story.objective,134,572,21,0xffc0e0bf,false);
        drawStoryParagraph(c,story.warning,594,523,538,19,26,0xffffc38f);
        button(c,700,586,1130,652,"BEGIN BATTLE",0xffa46a45);
        text(c,"BACK TO ATLAS",141,634,20,0xffcad5cc,false);
    }

    /** Pixel-width wrapping: no text can spill across panel borders. */
    private void drawStoryParagraph(Canvas c,String content,float x,float y,
                                    float maxWidth,float size,float leading,int color) {
        p.setTypeface(FONT);p.setTextSize(size);
        StringBuilder line=new StringBuilder();
        float baseline=y;
        for(String word:content.split(" ")) {
            String candidate=line.length()==0?word:line+" "+word;
            if(line.length()>0 && p.measureText(candidate)>maxWidth) {
                text(c,line.toString(),x,baseline,size,color,false);
                baseline+=leading;
                line.setLength(0);
            }
            if(line.length()>0)line.append(' ');
            line.append(word);
        }
        if(line.length()>0)text(c,line.toString(),x,baseline,size,color,false);
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
        if(scene==Scene.PLAY && briefingOpen) {
            if(action==MotionEvent.ACTION_DOWN && x>680 && x<1150 && y>570 && y<670) {
                briefingOpen=false;
                clearTouchControls();
            } else if(action==MotionEvent.ACTION_DOWN && x>106 && x<364 && y>592 && y<656) {
                briefingOpen=false;
                scene=Scene.MAP;
                clearTouchControls();
            }
            invalidate();
            return true;
        }
        if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_POINTER_DOWN) {
            if(scene!=Scene.PLAY) { tapMenu(x,y);invalidate();return true; }
            if(paused) {
                if(x>450 && x<830 && y>330 && y<421) { paused=false;clearTouchControls(); }
                else if(x>450 && x<830 && y>435 && y<525) {scene=Scene.MAP;paused=false;clearTouchControls();}
            }else if(x>1138 && y<112) {paused=true;clearTouchControls();}
            else if(distance(x,y,stickHudX(),568)<150) {joyPointer=id;updateStick(x,y);}
            else if(distance(x,y,attackHudX(),560)<82) {attackPointer=id;firing=true;fire();}
            else if(distance(x,y,pulseHudX(),593)<62) pulse();
            else if(distance(x,y,buildHudX(),589)<65) build();
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
        float dx=x-stickHudX(),dy=y-568,d=(float)Math.hypot(dx,dy);
        axisX=d>0?dx/d*Math.min(d/90f,1f):0;
        axisY=d>0?dy/d*Math.min(d/90f,1f):0;
    }
    private void tapMenu(float x,float y) {
        if(scene==Scene.HOME) {
            if(x>1007 && x<1216 && y>39 && y<104)audio.toggle();
            else if(x>665 && x<1205 && y>447 && y<517)scene=Scene.KINGDOM;
            else if(x>93 && x<610 && y>361 && y<412)scene=Scene.RESOURCES;
            else if(x>93 && x<610 && y>408 && y<492)scene=Scene.MAP;
            else if(x>93 && x<337 && y>502 && y<580)scene=Scene.ARMORY;
            else if(x>345 && x<610 && y>502 && y<580)scene=Scene.FORTRESS;
        }else if(scene==Scene.KINGDOM) {
            tapKingdom(x,y);
        }else if(scene==Scene.RESOURCES) {
            if(x>880 && y>555 && y<654)scene=Scene.HOME;
            else if(x>110 && x<865 && y>555 && y<655)beginResourceDownload();
        }else if(scene==Scene.MAP) {
            if(x>725 && x<881 && y>43 && y<99){
                regionalMap=!regionalMap;
            }else if(x>65 && x<882 && y>109 && y<581) {
                if(regionalMap) {
                    int stop=RegionJourney.hitLevel(x,y);
                    if(stop>=0) {
                        selectedLevel=((selectedLevel-1)/10)*10+stop+1;
                        selectedMap=0;
                    }
                }else{
                    int picked=AtlasArt.hitRegion(x,y);
                    if(picked>=0) {
                        selectedLevel=picked*10+1;
                        selectedMap=0;
                        regionalMap=true;
                    }
                }
            }else if(x>902 && x<1210 && y>=203 && y<523) {
                int map=(int)((y-203)/108);
                if(map>=0 && map<3)selectedMap=map;
            }else if(y>523 && y<579 && x>891 && x<1050) {
                selectedLevel=Math.max(1,selectedLevel-1);
                selectedMap=0;
            }else if(y>523 && y<579 && x>=1050 && x<1223) {
                selectedLevel=Math.min(100,selectedLevel+1);
                selectedMap=0;
            }else if(y>=591 && y<641 && x>=900 && x<1215) {
                difficultyMode=DifficultyRules.safe((int)((x-903)/78));
                prefs.edit().putInt("difficulty",difficultyMode).apply();
            }else if(y>591 && y<665 && x>405 && x<880) {
                int mission=Campaign.stage(selectedLevel,selectedMap);
                if(Campaign.accessible(mission,unlocked)) {
                    start(mission);
                    briefingOpen=true;
                }
            }else if(x<200 && y>594) scene=Scene.HOME;
        }else if(scene==Scene.ARMORY) {
            if(x>80 && x<329 && y>619)scene=Scene.HOME;
            else if(x>342 && x<655 && y>619)scene=Scene.FORTRESS;
            else if(x>687 && x<1125 && y>619)scene=Scene.SKILLS;
            else if(y>=151 && y<570) {
                int col=(int)((x-79)/375),row=(int)((y-155)/218);
                if(col>=0 && col<3 && row>=0 && row<2) {
                    float localX=(x-79)%375,localY=(y-155)%218;
                    if(localX>=0 && localX<343 && localY>=0 && localY<197)
                        equipWeapon(row*3+col);
                }
            }
        }else if(scene==Scene.SKILLS) {
            if(x>79 && x<327 && y>619)scene=Scene.HOME;
            else if(x>345 && x<654 && y>619)scene=Scene.ARMORY;
            else if(x>687 && x<1126 && y>619)scene=Scene.FORTRESS;
            else if(y>=151 && y<570){
                int col=(int)((x-79)/375),row=(int)((y-155)/218);
                if(col>=0 && col<3 && row>=0 && row<2) {
                    float localX=(x-79)%375,localY=(y-155)%218;
                    if(localX>=0 && localX<343 && localY>=0 && localY<197)
                        equipSkill(row*3+col);
                }
            }
        }else if(scene==Scene.FORTRESS) {
            if(y>539 && y<634 && x>722)upgradeFortress();
            else if(y>630 && x<300)scene=Scene.HOME;
            else if(y>630 && x>339 && x<690)scene=Scene.ARMORY;
        }else if(scene==Scene.WIN || scene==Scene.LOSE) {
            if(x>420 && x<860 && y>425 && y<510) {
                if(scene==Scene.WIN && stage==299)scene=Scene.MAP;
                else if(scene==Scene.WIN) {
                    start(Math.min(299,stage+1));
                    briefingOpen=true;
                }
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
