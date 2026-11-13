package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.text.InputFilter;
import android.widget.EditText;
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
    private enum Scene { HOME, MAP, ARMORY, WEAPON_TREE, SKILLS, FORTRESS, ASCENSION, RESOURCES, KINGDOM, CHRONICLE, PLAY, WIN, LOSE }
    private Scene scene=Scene.HOME;
    private static final float TREE_LEFT=75f,TREE_TOP=148f,TREE_RIGHT=906f,
            TREE_BOTTOM=592f;
    private float ascensionScroll=AscensionTree.worldHeight()-(TREE_BOTTOM-TREE_TOP);
    private static final float WM_TOP=180f,WM_BOTTOM=584f,WM_LEFT=72f,WM_RIGHT=790f;
    private static final float WM_SCROLL_MAX=310f;
    private int selectedMasteryWeapon=0,selectedWeaponMasteryNode=1;
    private float weaponTreeScroll=WM_SCROLL_MAX,weaponTouchStartX,weaponTouchStartY,
            weaponLastY;
    private boolean weaponTreeDragging,weaponTouchActive;
    private int selectedAscensionNode=1;
    private float treeDownX,treeDownY,treeLastY;
    private boolean treeDragging=false;
    private boolean treeTouchActive=false;
    private boolean briefingOpen=false;
    private boolean storyOpen=false;
    private StoryScript.Line[] storyLines;
    private int storyIndex;
    private final List<StoryScript.Line[]> pendingStory=new ArrayList<>();
    private boolean bossIntroSeen=false;
    private int chronicleWorld=0;
    private MemoryTrial.Objective trial;
    private boolean trialPassed=false,trialAwarded=false;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PaintedArt painted;
    private final CharacterArt characterArt;
    private final MotionArt motionArt;
    // The user's original skeletal actor implementations, extended without
    // replacing the v0.51 combat engine or any purchased-upgrade save keys.
    private final WardenRig wardenRig=new WardenRig();
    private boolean firstBattleActorDiag=false;
    private final EnemyRig enemyRig=new EnemyRig();
    private final EnemySpriteFrameCache enemyFrames=new EnemySpriteFrameCache(enemyRig);
    // Independent, reversible switches for per-species visual QA.
    private static final boolean USE_VECTOR_WARDEN=true;
    private static final boolean USE_VECTOR_ENEMIES=true;
    private final BiomeProps biomeProps;
    private final LivingBattlefield livingWorld=new LivingBattlefield();
    private final NativeDeployedMapArt deployedMap=new NativeDeployedMapArt();
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
    private final DefenseLoadouts defenseLoadouts;
    private int selectedLoadout=0;
    private final float[] towerHp=new float[TowerPadLayout.MAX];
    private final int[] towerTypes=new int[TowerPadLayout.MAX];
    private final int[] destroyedTowerTier=new int[TowerPadLayout.MAX];
    private Enemy focusedEnemy;
    private final StoryProgress storyProgress;
    private final KingdomMeta kingdom;
    private final AscensionTree ascension;
    private final WeaponMastery weaponMastery;
    private final GameAudio audio;
    private final CombatAnimationFeel combatFeel=new CombatAnimationFeel();
    private final WardenAnimationMachine heroAnimation=new WardenAnimationMachine();
    private boolean liveRenderStep;
    private float animationDelta;
    private int kingdomTab=0;
    private String kingdomMessage="Complete challenges to earn rewards!";
    private boolean battleRecorded=false;
    private int battlePulses=0;
    private final Random random=new Random(13);
    private final List<Enemy> enemies=new ArrayList<>();
    private final List<Shot> shots=new ArrayList<>();
    private final List<HostileBolt> hostileBolts=new ArrayList<>();
    private final List<Spark> sparks=new ArrayList<>();
    private final List<DeathEcho> deaths=new ArrayList<>();
    private final List<Impact> impacts=new ArrayList<>();
    private final List<Decor> decor=new ArrayList<>();
    private final List<Pickup> pickups=new ArrayList<>();
    private final int[] towers=new int[TowerPadLayout.MAX];
    private final float[] towerTimers=new float[TowerPadLayout.MAX];
    private final float[] towerRecoil=new float[TowerPadLayout.MAX];
    private final float[] towerAim=new float[TowerPadLayout.MAX];
    private int activeTowerCount=4;
    private int selectedTowerPad=-1;
    private final float[] hazardTimers=new float[4];
    private float hazardSlowTimer=0;
    private final float[] towerX=new float[TowerPadLayout.MAX];
    private final float[] towerY=new float[TowerPadLayout.MAX];
    private int unlocked;
    private int supplies, fortressRank, equippedWeapon, equippedSkill, difficultyMode;
    private int lastCastSkill=WardenSkills.PULSE;
    private float bannerX,bannerY,bannerTime,bannerTick;
    private float trapX,trapY,trapTime;
    private float meteorX,meteorY,meteorTime;
    private int selectedLevel=1, selectedMap=0, stage=0;
    private boolean regionalMap=false;
    private int wave, remaining, gold, kills,missionBuilds,objectiveBonus;
    private boolean defeatedElite;
    private float baseHp, heroHp, heroX, heroY, heroDir=-1.57f, spawnTimer, intermission, waveNotice;
    private float shotTimer, pulseCooldown, pulseFlash, hurtCooldown, fireFlash, noticeTimer, gameTime;
    private float fortressArsenalTimer;
    private int fortressArsenalShots;
    // UI-only preview; never modifies purchased ranks or save data.
    private boolean previewNextFortressLook=false;
    private float evolutionRevealTimer=0f;
    private String evolutionRevealTitle="",evolutionRevealDescription="";
    // 120-ms button tap buffer per the animation skill's responsive cancel window.
    private float bufferedAttackTime;
    private float dodgeTime,dodgeCooldown,dodgeDirX,dodgeDirY;
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
        float x,y,hp,maxHp,phase,attackTimer,attackAnim,slowTimer,specialTimer,specialCharge,
                burnTimer,burnDps,dashWindup,dashTime,dashCooldown,shamanCooldown,
                hitReaction;
        boolean moving,boss;
        int kind,bossPhase=1,variant=0;
        float faceX=-1f;
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
    private static final class HostileBolt {
        float x,y,life=3.6f;
        final float tx,ty;
        HostileBolt(float x,float y,float tx,float ty){
            this.x=x;this.y=y;this.tx=tx;this.ty=ty;
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
        int variant=0;
        float faceX=-1f;
        boolean boss=false;
        int bossPhase=1;
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
        storyProgress=new StoryProgress(prefs);
        defenseLoadouts=new DefenseLoadouts(prefs);
        selectedLoadout=Math.max(0,Math.min(2,prefs.getInt("fort.v065.active",0)));
        kingdom=new KingdomMeta(prefs);
        ascension=new AscensionTree(prefs);
        weaponMastery=new WeaponMastery(prefs);
        audio=new GameAudio(context,prefs);
        combatFeel.setReducedMotion(prefs.getBoolean("reduced_motion",false));
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
        bufferedAttackTime=0;
        dodgeTime=0;
    }
    public void setActive(boolean value) {
        active=value;
        lastNs=0;
        if (!value) clearTouchControls();
        if(active)audio.prepare();
        if (active) postInvalidateOnAnimation();
    }
    public void releaseAudio(){audio.release();enemyFrames.clear();}
    public boolean handleBack() {
        if(storyOpen){dismissStory();return true;}
        if(scene==Scene.CHRONICLE){scene=Scene.HOME;return true;}
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
        if (scene==Scene.ASCENSION) {scene=Scene.FORTRESS;return true;}
        if (scene==Scene.WEAPON_TREE) {scene=Scene.ARMORY;return true;}
        if (scene!=Scene.HOME) { scene=Scene.HOME; return true; }
        return false;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now=System.nanoTime();
        float dt=lastNs==0 ? 0 : Math.min(.045f,(now-lastNs)/1000000000f);
        lastNs=now;
        if (active) {
            // Hitstop freezes only painted-animation clocks, NOT real damage
            // simulation or touch handling. Feedback remains mobile-friendly.
            boolean liveCombat=scene==Scene.PLAY&&!paused&&!briefingOpen&&!storyOpen;
            animationDelta=liveCombat?combatFeel.frame(dt):dt;
            gameTime+=animationDelta;
            evolutionRevealTimer=Math.max(0,evolutionRevealTimer-dt);
            if(liveCombat){
                liveRenderStep=true;
                update(dt);
                liveRenderStep=false;
            }else if(scene==Scene.LOSE){
                heroAnimation.advance(animationDelta,false);
            }
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
        else if (scene==Scene.ASCENSION) drawAscension(canvas);
        else if (scene==Scene.WEAPON_TREE) drawWeaponMastery(canvas);
        else if (scene==Scene.RESOURCES) drawResources(canvas);
        else if (scene==Scene.KINGDOM) drawKingdom(canvas);
        else if (scene==Scene.CHRONICLE) drawChronicle(canvas);
        else {
            drawBattle(canvas);
            if (scene==Scene.WIN || scene==Scene.LOSE) drawResult(canvas);
            else if (paused) drawPause(canvas);
            else if (scene==Scene.PLAY && briefingOpen) drawMissionBriefing(canvas);
        }
        if(storyOpen)drawStoryScene(canvas);
        if(evolutionRevealTimer>0&&scene!=Scene.PLAY){
            drawEvolutionReveal(canvas);
        }
        canvas.restore();
        if (active) postInvalidateOnAnimation();
    }

    private float heroMaxHp(){return kingdom.heroMaxHp()*DifficultyRules.heroHealth(difficultyMode)+ascension.heroHp();}
    private float coreMaxHp(){return Progression.maxCoreHp(fortressRank)+kingdom.coreBonusHp()+ascension.coreHp();}
    private int arsenalTier(){return EvolutionProgress.fortressArsenal(ascension,fortressRank);}
    private int weaponForm(int weapon){return EvolutionProgress.weaponForm(weaponMastery,weapon);}
    private int armorForm(){return EvolutionProgress.armorForm(ascension,fortressRank);}
    private int skillForm(){return EvolutionProgress.skillForm(ascension,fortressRank);}
    private int coreForm(){return EvolutionProgress.coreForm(ascension);}
    private int treasuryForm(){return EvolutionProgress.treasuryForm(ascension);}
    private int speedForm(){return EvolutionProgress.speedForm(ascension);}

    private void start(int s) {
        Campaign.checkStage(s);
        stage=s; scene=Scene.PLAY; paused=false; briefingOpen=false;
        dismissStory();bossIntroSeen=false;
        trial=MemoryTrial.forStage(s);trialPassed=false;trialAwarded=false;
        focusedEnemy=null;
        // First-play mission controls must stay unchanged; the illustrated
        // prologue is available explicitly in the Chronicle until the
        // cinematic-first-launch flow has its own dedicated acceptance tests.
        activeTowerCount=TowerPadLayout.count(stage);
        selectedTowerPad=-1;
        for(int pad=0;pad<activeTowerCount;pad++){
            towerX[pad]=TowerPadLayout.x(stage,pad);
            towerY[pad]=TowerPadLayout.y(stage,pad);
        }
        battleRecorded=false; battlePulses=0;
        missionBuilds=0;objectiveBonus=0;defeatedElite=false;
        hazardSlowTimer=0;
        for(int i=0;i<hazardTimers.length;i++)hazardTimers[i]=.5f;
        // Full Java version of deployed-map-generator.html. No old scenery.
        deployedMap.prepare(s);
        enemyFrames.clear(); // release previous realm's rasterized animation clips
        walkClock=0;heroMoving=false;
        heroAnimation.reset();
        heroX=LaneRules.startX(); heroY=LaneRules.startY(); heroHp=heroMaxHp();
        baseHp=coreMaxHp();
        wave=1; remaining=DifficultyRules.enemies(stage,wave,difficultyMode);
        spawnTimer=3.2f; intermission=0; waiting=false;
        shotTimer=0; pulseCooldown=0; pulseFlash=0; hurtCooldown=0; fireFlash=0;
        fortressArsenalTimer=.95f;fortressArsenalShots=0;
        bufferedAttackTime=0;
        dodgeTime=dodgeCooldown=0;
        dodgeDirX=0;dodgeDirY=0;
        pendingMelee=false;pendingMeleeWeapon=-1;
        bannerTime=0;bannerTick=0;trapTime=0;meteorTime=0;
        waveNotice=2.6f; noticeTimer=0;
        gold=DifficultyRules.startingGold(fortressRank,difficultyMode)+ascension.startingGold();kills=0;
        enemies.clear();shots.clear();hostileBolts.clear();sparks.clear();
        decor.clear();pickups.clear();
        deaths.clear();impacts.clear();
        for (int i=0;i<towers.length;i++){
            towers[i]=0;towerTimers[i]=0;towerRecoil[i]=0;
            towerAim[i]=0;towerHp[i]=0;destroyedTowerTier[i]=0;
            towerTypes[i]=TowerPadLayout.family(i);
        }
        int initial=DifficultyRules.initialTowers(fortressRank,difficultyMode);
        for(int i=0;i<initial;i++){
            towers[i]=1;towerHp[i]=TowerDurability.maxHp(1,fortressRank);
        }
        // Blueprints spend actual battle gold; no free structures or upgrades.
        gold-=defenseLoadouts.deploy(selectedLoadout,towers,towerTypes,
                activeTowerCount,gold);
        for(int i=0;i<activeTowerCount;i++)if(towers[i]>0)
            towerHp[i]=TowerDurability.maxHp(towers[i],fortressRank);
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
        combatFeel.reset();
        audio.play(GameAudio.AMBIENT);
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
        dodgeCooldown=Math.max(0,dodgeCooldown-dt);
        boolean dashing=dodgeTime>0;
        float activeDodgeStep=dashing?Math.min(dt,dodgeTime):0f;
        // Dash ignores movement slow but respects world walls and real towers.
        float speed=245f*ascension.moveMultiplier()*(hazardSlowTimer>0?.71f:1f);
        float mx=dashing?dodgeDirX:axisX,my=dashing?dodgeDirY:axisY;
        float movementTime=dashing?activeDodgeStep:dt;
        float movementSpeed=dashing?DodgeRules.SPEED:speed;
        float previousHeroX=heroX,previousHeroY=heroY;
        // Independent X/Y collision resolution allows sliding along walls.
        float nextX=LaneRules.heroX(MovementRules.heroNextX(
                heroX,heroY,mx*movementSpeed*movementTime));
        if (!hitsBuiltTower(nextX,heroY)) heroX=nextX;
        float nextY=LaneRules.heroY(MovementRules.heroNextY(
                heroX,heroY,my*movementSpeed*movementTime));
        if (!hitsBuiltTower(heroX,nextY)) heroY=nextY;
        if(dashing)dodgeTime=Math.max(0,dodgeTime-dt);
        heroMoving=distance(heroX,heroY,previousHeroX,previousHeroY)>.3f;
        if(heroMoving)walkClock+=animationDelta;
        if (dashing)heroDir=(float)Math.atan2(dodgeDirY,dodgeDirX);
        else if (Math.abs(axisX)+Math.abs(axisY)>.1f && !pendingMelee)
            heroDir=(float)Math.atan2(axisY,axisX);
        // Exactly one animation controller owns clip priority and contact
        // events. Direct Robolectric physics calls use dt; rendered gameplay
        // uses the hitstop-aware animationDelta without freezing simulation.
        heroAnimation.advance(liveRenderStep?animationDelta:dt,heroMoving);
        if(heroAnimation.consume(WardenAnimationMachine.Event.FOOTSTEP)
                &&sparks.size()<160){
            puff(heroX+(float)Math.sin(walkClock*13f)*13f,heroY-3f,
                    2,0xff9eb3a7);
        }
        boolean contact=heroAnimation.consume(
                WardenAnimationMachine.Event.ATTACK_CONTACT);
        if(pendingMelee&&contact)resolveMeleeHit();
        bufferedAttackTime=Math.max(0,bufferedAttackTime-dt);
        if(firing||bufferedAttackTime>0)fire();
        updateSkillObjects(dt);
        if (!waiting) {
            spawnTimer-=dt;
            if (remaining>0 && spawnTimer<=0) {
                spawnEnemy();
                remaining--;
                spawnTimer=DifficultyRules.spawnInterval(stage,difficultyMode);
            }
        }
        for (int i=0;i<activeTowerCount;i++) {
            towerTimers[i]-=dt;
            towerRecoil[i]=Math.max(0,towerRecoil[i]-dt);
            if (towers[i]>0 && towerHp[i]>0 && towerTimers[i]<=0) {
                Enemy target=closest(towerX[i],towerY[i],280+towers[i]*35);
                if (target!=null) {
                    float[] modifiers={1f,1.50f,.77f,1.13f};
                    Shot turretShot=new Shot(towerX[i],towerY[i]-25,
                            BattleRules.towerDamage(towers[i])*modifiers[towerFamily(i)]*
                                    kingdom.towerDamageMultiplier()*ascension.towerMultiplier()*
                                    FortressEvolutionArt.damageBonus(arsenalTier()),
                            towerFamily(i)==1?550:750,target,true);
                    turretShot.towerSlot=i;
                    shots.add(turretShot);
                    int family=towerFamily(i);
                    audio.play(family==1?GameAudio.TOWER_CANNON:
                            family==2?GameAudio.TOWER_FROST:
                            family==3?GameAudio.TOWER_ARC:GameAudio.SHOT);
                    towerAim[i]=(float)Math.atan2(target.y-towerY[i],target.x-towerX[i]);
                    towerRecoil[i]=.20f;
                    towerTimers[i]=Math.max(.25f,(towerFamily(i)==1?1.35f:
                             towerFamily(i)==2?1.02f:.92f)
                            -towers[i]*.11f);
                    puff(towerX[i],towerY[i]-38,6,towerColor(towerFamily(i)));
                }
            }
        }
        // Purchased fortress arsenal is genuine independently firing artillery.
        // The attached weapons in FortressEvolutionArt launch these projectiles;
        // they do not consume or occupy any of the 5–8 usable tower pads.
        fortressArsenalTimer-=dt;
        int keepArsenal=arsenalTier();
        if(keepArsenal>0 && fortressArsenalTimer<=0){
            Enemy siegeTarget=closest(BASE_X,BASE_Y,
                    FortressArmamentRules.range(keepArsenal));
            if(siegeTarget!=null&&shots.size()<110){
                float spawnX=FortressArmamentRules.muzzleX(BASE_X,fortressArsenalShots,keepArsenal);
                float spawnY=FortressArmamentRules.muzzleY(BASE_Y,keepArsenal);
                Shot battery=new Shot(spawnX,spawnY,
                        FortressArmamentRules.damage(keepArsenal)*
                                kingdom.towerDamageMultiplier()*ascension.towerMultiplier(),
                        FortressArmamentRules.speed(keepArsenal),siegeTarget,true);
                battery.towerSlot=FortressArmamentRules.family(keepArsenal);
                shots.add(battery);
                fortressArsenalShots++;
                fortressArsenalTimer=FortressArmamentRules.cooldown(keepArsenal);
                int category=FortressArmamentRules.family(keepArsenal);
                audio.play(category==1?GameAudio.TOWER_CANNON:
                        category==3?GameAudio.TOWER_ARC:GameAudio.SHOT);
                puff(spawnX,spawnY,8,keepArsenal>=3?0xffb4edfb:0xffffca8e);
            }
        }
        // Boss reinforcements are collected separately so the active enemy
        // iterator cannot be invalidated by a phase-transition summon.
        final List<Enemy> reinforcements=new ArrayList<>();
        Iterator<Enemy> it=enemies.iterator();
        while(it.hasNext()) {
            Enemy e=it.next();
            if(e.hp<=0) {
                if(e.boss)defeatedElite=true;
                gold+=BattleRules.killCoins(e.kind);
                kills++;
                if(pickups.size()<28 && random.nextFloat()<.22f)
                    pickups.add(new Pickup(e.x,e.y,random.nextFloat()<.58f?0:1));
                puff(e.x,e.y,9,GLOWS[Campaign.world(stage)]);
                if(deaths.size()<32){
                    DeathEcho echo=new DeathEcho(e.x,e.y,e.kind,e.phase);
                    echo.variant=e.variant;echo.faceX=e.faceX;
                    echo.boss=e.boss;echo.bossPhase=e.bossPhase;
                    deaths.add(echo);
                }
                if(impacts.size()<64)impacts.add(new Impact(e.x,e.y,
                        GLOWS[Campaign.world(stage)],e.boss?86:41));
                it.remove();continue;
            }
            e.attackAnim=Math.max(0,e.attackAnim-dt);
            e.hitReaction=Math.max(0,e.hitReaction-dt);
            e.slowTimer=Math.max(0,e.slowTimer-dt);
            if(e.burnTimer>0) {
                float burnStep=Math.min(e.burnTimer,dt);
                e.burnTimer-=burnStep;
                e.hp-=burnStep*e.burnDps;
                if(e.hp<=0)continue; // credited by normal enemy death processing next frame
            }
            float dHero=distance(e.x,e.y,heroX,heroY);
            // Boars telegraph before sprinting; shamans fire dodgeable bolts
            // rather than applying ranged damage instantly without a visual.
            e.dashCooldown-=dt;
            e.shamanCooldown-=dt;
            if(EnemySpecials.canBoarCharge(e.kind,dHero,e.dashCooldown) &&
                    e.dashWindup<=0 && e.dashTime<=0){
                e.dashWindup=EnemySpecials.BOAR_WINDUP;e.dashCooldown=5.2f;
            }
            if(e.dashWindup>0){
                e.dashWindup=Math.max(0,e.dashWindup-dt);
                if(e.dashWindup<=0)e.dashTime=EnemySpecials.BOAR_DASH;
            }else if(e.dashTime>0)e.dashTime=Math.max(0,e.dashTime-dt);
            if(EnemySpecials.canShamanCast(e.kind,dHero,e.shamanCooldown) &&
                    hostileBolts.size()<24){
                hostileBolts.add(new HostileBolt(e.x,e.y-44,heroX,heroY-25));
                e.shamanCooldown=3.8f;
                e.attackAnim=.42f;
            }
            boolean hunt=MovementRules.huntsHero(dHero,e.kind)||
                    e.dashWindup>0||e.dashTime>0;
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
            if(e.moving&&Math.abs(tx-e.x)>1.5f)e.faceX=tx<e.x?-1f:1f;
            if(e.moving) {
                float speedFactor=(e.kind==3?1.15f:1f)*
                        DifficultyRules.enemySpeed(difficultyMode)*
                        (e.slowTimer>0?.64f:1f)*
                        EnemySpecials.mobilityMultiplier(e.dashWindup,e.dashTime);
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
                int newPhase=BossTactics.phase(e.hp,e.maxHp);
                if(newPhase>e.bossPhase){
                    // Each threshold summons *real* enemies with health,
                    // pathfinding, collision and combat like a normal wave.
                    for(int phase=e.bossPhase+1;phase<=newPhase;phase++){
                        int num=BossTactics.minions(phase);
                        for(int k=0;k<num&&enemies.size()+reinforcements.size()<35;k++){
                            int kind=BossTactics.minionKind(stage);
                            float x=Math.min(LaneRules.ENEMY_SPAWN_X-30,e.x+90+k*46);
                            float y=LaneRules.stagePathY(stage,x)+(k%2==0?38:-38);
                            float hp=BattleRules.enemyHealth(stage,kind)*
                                    DifficultyRules.health(difficultyMode);
                            Enemy ally=new Enemy(x,y,kind,hp,(k+1)*1.2f);
                            reinforcements.add(ally);
                        }
                    }
                    e.bossPhase=newPhase;
                    audio.play(GameAudio.BOSS_PHASE);
                    announce("BOSS PHASE "+newPhase+" • REINFORCEMENTS!");
                }
                e.specialTimer-=dt;
                if(e.specialCharge<=0&&e.specialTimer<=0&&
                        distance(e.x,e.y,BASE_X,BASE_Y)<440f){
                    e.specialCharge=BossTactics.chargeTime(stage,e.bossPhase);
                    e.specialTimer=BossTactics.interval(stage,e.bossPhase);
                    audio.play(GameAudio.BOSS_WARN);
                }else if(e.specialCharge>0){
                    e.specialCharge-=dt;
                    if(e.specialCharge<=0){
                        float strength=DifficultyRules.enemyDamage(difficultyMode)*
                                BossTactics.damageMultiplier(e.bossPhase);
                        int tactic=BossTactics.tactic(stage);
                        if(distance(e.x,e.y,BASE_X,BASE_Y)<445){
                            baseHp-=(tactic==1?20f:28f)*strength;
                            puff(BASE_X,BASE_Y,19,tactic==2?0xffa7eb9c:0xffffac83);
                            // Telegraphed boss siege impacts can destroy artillery.
                            int t=nearestBuiltTower(BASE_X,BASE_Y,365f);
                            if(t>=0)damageTower(t,55f*strength);
                        }
                        if(distance(e.x,e.y,heroX,heroY)<265&&
                                !DodgeRules.invulnerable(dodgeTime)){
                            heroHp-=(tactic==0?24f:18f)*strength;
                            puff(heroX,heroY,18,tactic==2?0xffa1eeae:0xffe5a0fa);
                            if(tactic==2)hazardSlowTimer=Math.max(hazardSlowTimer,1.3f);
                        }
                        if(tactic==1){
                            // Sustain bosses can recover health but not exceed
                            // their original maximum; interrupt by killing fast.
                            e.hp=Math.min(e.maxHp,e.hp+e.maxHp*.05f);
                            puff(e.x,e.y,14,0xff9fd7c8);
                        }
                        if(baseHp<=0||heroHp<=0){
                            baseHp=Math.max(0,baseHp);
                            heroHp=Math.max(0,heroHp);
                            markDefeat();return;
                        }
                        announce(BossTactics.warning(stage)+"!");
                    }
                }
            }
            if(e.kind==6 && e.dashTime>0 &&
                    distance(e.x,e.y,heroX,heroY)<65 && hurtCooldown<=0 &&
                    !DodgeRules.invulnerable(dodgeTime)){
                heroHp-=29*DifficultyRules.enemyDamage(difficultyMode);
                e.dashTime=0;hurtCooldown=.85f;
                heroAnimation.hurt();pendingMelee=false;pendingMeleeWeapon=-1;
                puff(heroX,heroY,14,0xffffb27b);
                audio.play(GameAudio.HURT);
                if(heroHp<=0){heroHp=0;markDefeat();return;}
            }
            if (distance(e.x,e.y,heroX,heroY)<44 && hurtCooldown<=0 &&
                    !DodgeRules.invulnerable(dodgeTime)) {
                heroHp-=(e.kind==2?20:11)*
                        DifficultyRules.enemyDamage(difficultyMode);
                e.attackAnim=.42f;
                hurtCooldown=.8f;
                heroAnimation.hurt();pendingMelee=false;pendingMeleeWeapon=-1;
                puff(heroX,heroY,6,0xffff8086);
                audio.play(GameAudio.HURT);
                if(heroHp<=0){heroHp=0;markDefeat();return;}
            }
            int siegeTower=(!hunt&&(e.kind==2||e.kind==4||e.kind==5))?
                    nearestBuiltTower(e.x,e.y,174f):-1;
            if(siegeTower>=0){
                e.attackTimer-=dt;
                if(e.attackTimer<=0){
                    damageTower(siegeTower,(e.kind==2?46f:e.kind==5?32f:26f)*
                            DifficultyRules.enemyDamage(difficultyMode));
                    e.attackAnim=.42f;
                    e.attackTimer=e.kind==2?1.55f:2.05f;
                }
            }else if(!hunt && (distance(e.x,e.y,BASE_X,BASE_Y)<190 || (e.kind==5 &&
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
        // Commit the summon queue only after enemy iteration has finished.
        enemies.addAll(reinforcements);
        updateStageHazards(dt);
        if(scene!=Scene.PLAY)return;
        Iterator<Shot> sit=shots.iterator();
        while(sit.hasNext()) {
            Shot shot=sit.next();shot.life-=dt;
            if(shot.life<=0 || shot.target.hp<=0){sit.remove();continue;}
            float d=distance(shot.x,shot.y,shot.target.x,shot.target.y-10);
            if(d<24) {
                shot.target.hp-=shot.damage;
                shot.target.hitReaction=.21f;
                if(!shot.tower)
                    combatFeel.onDamage(shot.target.x,shot.target.y-16,
                            Progression.WEAPONS[shot.weaponId].color,false,
                            shot.damage);
                int impact=shot.tower?GOLD:Progression.WEAPONS[shot.weaponId].color;
                // Each bought fortress arsenal unlocks combat, not only models.
                if(shot.tower){
                    int arsenal=arsenalTier();
                    if(FortressEvolutionArt.emberShell(arsenal)){
                        shot.target.burnTimer=Math.max(shot.target.burnTimer,2.3f);
                        shot.target.burnDps=Math.max(shot.target.burnDps,9f+arsenal*4f);
                        puff(shot.target.x,shot.target.y-18,4,0xffffa46a);
                    }
                    if(FortressEvolutionArt.stormChain(arsenal)){
                        for(Enemy nearby:enemies)
                            if(nearby!=shot.target&&nearby.hp>0&&
                                    distance(nearby.x,nearby.y,shot.target.x,shot.target.y)<140){
                                nearby.hp-=shot.damage*.24f;
                                nearby.hitReaction=.21f;
                                puff(nearby.x,nearby.y-15,5,0xffa8d7ff);
                                break;
                            }
                    }
                    if(FortressEvolutionArt.guardianRepair(arsenal)&&
                            shot.target.hp<=0){
                        baseHp=Math.min(coreMaxHp(),baseHp+3f);
                        puff(BASE_X,BASE_Y-110,3,0xffabf7d8);
                    }
                }
                if(shot.tower && TowerPadLayout.family(shot.towerSlot)==1) {
                    // Cannon shells hit clustered enemies around the impact point.
                    for(Enemy nearby:enemies)
                        if(nearby!=shot.target && nearby.hp>0 &&
                           distance(nearby.x,nearby.y,shot.target.x,shot.target.y)<95)
                            nearby.hp-=shot.damage*.44f;
                } else if(shot.tower && TowerPadLayout.family(shot.towerSlot)==2) {
                    // Frost turrets meaningfully reduce enemy movement for 2.6 seconds.
                    shot.target.slowTimer=2.6f;
                } else if(shot.tower && TowerPadLayout.family(shot.towerSlot)==3) {
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
                // End-tier mastery attacks have weapon-specific real combat
                // effects, not merely glowing upgrade-tree icons.
                if(shot.weaponId>=0 && weaponMastery.mastered(shot.weaponId)){
                    switch(shot.weaponId){
                        case 0: // Phantom Volley: second spectral bolt hits target.
                            shot.target.hp-=shot.damage*.40f;
                            puff(shot.target.x,shot.target.y-32,10,0xffa5f4f3);
                            break;
                        case 3: // Tempest Crown: arc jumps to up to two allies.
                            int chained=0;
                            for(Enemy chainedEnemy:enemies){
                                if(chainedEnemy==shot.target||chainedEnemy.hp<=0)continue;
                                if(distance(chainedEnemy.x,chainedEnemy.y,
                                        shot.target.x,shot.target.y)<170){
                                    chainedEnemy.hp-=shot.damage*.48f;
                                    chainedEnemy.hitReaction=.21f;
                                    puff(chainedEnemy.x,chainedEnemy.y,8,0xffb9b7ff);
                                    if(++chained>=2)break;
                                }
                            }
                            break;
                        case 4: // War Drum: piercing suppressive burst.
                            for(Enemy nearby:enemies)if(nearby!=shot.target&&
                                    nearby.hp>0&&distance(nearby.x,nearby.y,
                                            shot.target.x,shot.target.y)<85){
                                nearby.hp-=shot.damage*.30f;
                                nearby.slowTimer=Math.max(nearby.slowTimer,1f);
                            }
                            break;
                        case 5: // Absolute Zero: freeze into brittle condition.
                            shot.target.slowTimer=Math.max(shot.target.slowTimer,5f);
                            shot.target.hp-=shot.damage*.32f;
                            puff(shot.target.x,shot.target.y-30,12,0xffa6f3ff);
                            break;
                    }
                }
                puff(shot.target.x,shot.target.y,7,impact);
                if(impacts.size()<64)impacts.add(new Impact(
                        shot.target.x,shot.target.y-15,impact,
                        shot.tower&&TowerPadLayout.family(shot.towerSlot)==1?85:39));
                audio.play(GameAudio.IMPACT);
                sit.remove();
            } else {
                // Clamp each step to target distance, preventing projectile oscillation.
                float step=Math.min(d,shot.speed*dt);
                shot.x+=(shot.target.x-shot.x)/Math.max(1,d)*step;
                shot.y+=(shot.target.y-10-shot.y)/Math.max(1,d)*step;
            }
        }
        Iterator<HostileBolt> bolts=hostileBolts.iterator();
        while(bolts.hasNext()){
            HostileBolt b=bolts.next();
            b.life-=dt;
            float distance=distance(b.x,b.y,b.tx,b.ty);
            if(b.life<=0){bolts.remove();continue;}
            if(distance<22){
                if(EnemySpecials.boltConnects(b.tx,b.ty,heroX,heroY-25)
                        &&hurtCooldown<=0&&!DodgeRules.invulnerable(dodgeTime)){
                    heroHp-=17*DifficultyRules.enemyDamage(difficultyMode);
                    hurtCooldown=.65f;
                    audio.play(GameAudio.HURT);
                    puff(heroX,heroY-26,8,0xffcda5f3);
                    if(heroHp<=0){heroHp=0;markDefeat();return;}
                }
                if(impacts.size()<64)impacts.add(new Impact(b.tx,b.ty,
                        0xffc7a1f2,32));
                bolts.remove();
            }else{
                float step=Math.min(distance,EnemySpecials.SHAMAN_BOLT_SPEED*dt);
                b.x+=(b.tx-b.x)/Math.max(1,distance)*step;
                b.y+=(b.ty-b.y)/Math.max(1,distance)*step;
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
                if(drop.kind==0){heroHp=Math.min(heroMaxHp(),heroHp+30);announce("HEALTH RESTORED +30");}
                else {gold+=35;announce("SUPPLY CACHE +35 GOLD");}
                puff(drop.x,drop.y,9,drop.kind==0?0xfff4949c:GOLD);
                drops.remove();
            }
        }
        if(remaining==0 && enemies.isEmpty()) {
            if(!waiting){waiting=true;
                intermission=WavePacingV065.seconds(difficultyMode,
                        DifficultyRules.worldBoss(stage)&&wave+1==
                        DifficultyRules.waves(stage,difficultyMode));
                gold+=25;waveNotice=2.5f;}
            else {
                intermission-=dt;
                if(intermission<=0)advanceWave(false);
            }
        }
    }
    /** One-shot countdown or manual early start: no duplicated gold or waves. */
    private void advanceWave(boolean early){
        if(!waiting)return;
        int cap=DifficultyRules.waves(stage,difficultyMode);
        if(wave>=cap){complete();return;}
        boolean bossIncoming=DifficultyRules.worldBoss(stage)&&wave+1==cap;
        int bonus=early?WavePacingV065.earlyBonus(intermission,difficultyMode,bossIncoming):0;
        gold+=bonus;
        wave++;remaining=DifficultyRules.enemies(stage,wave,difficultyMode);
        spawnTimer=DifficultyRules.spawnInterval(stage,difficultyMode);
        intermission=0;waiting=false;waveNotice=2.2f;
        if(early)announce("EARLY WAVE • +"+bonus+" BATTLE GOLD");
    }
    private void spawnEnemy() {
        float x=LaneRules.ENEMY_SPAWN_X-random.nextFloat()*26f;
        float y=LaneRules.stageEnemyY(stage,random.nextFloat()*96f-48f);
        int pool=Math.min(6,2+Campaign.world(stage)/2+(wave>=3?1:0));
        int kind=random.nextInt(Math.max(1,pool));
        // The Forest Boar becomes a recognizable charging enemy from wave two.
        if(wave>=2 && random.nextFloat()<.15f)kind=6;
        boolean elite=DifficultyRules.eliteWave(stage,wave,difficultyMode)
                && remaining==1;
        if(elite)kind=5;
        float hp=BattleRules.enemyHealth(stage,kind)*DifficultyRules.health(difficultyMode);
        if(elite)hp*=DifficultyRules.eliteMultiplier(stage,wave,difficultyMode);
        Enemy enemy=new Enemy(x,y,kind,hp,random.nextFloat()*6.28f);
        enemy.variant=random.nextInt(4);
        if(kind==6)enemy.dashCooldown=1.6f;
        if(kind==4)enemy.shamanCooldown=1.8f;
        enemy.boss=elite;
        if(elite)enemy.specialTimer=5.0f;
        enemies.add(enemy);
        if(elite) {
            waveNotice=2.7f;
            announce(DifficultyRules.worldBoss(stage)?
                    "WORLD BOSS  •  "+DifficultyRules.worldBossName(stage):
                    "ELITE SIEGE COMMANDER INCOMING");
            if(DifficultyRules.worldBoss(stage)&&!bossIntroSeen){
                bossIntroSeen=true;
                openStory(StoryScript.beforeWorldBoss(Campaign.world(stage)));
            }
        }
    }
    private Enemy closest(float x,float y,float range) {
        // Player-assigned tactical focus marker overrides ordinary turret AI
        // only when the requested target is alive and in true weapon range.
        if(focusedEnemy!=null&&focusedEnemy.hp>0&&enemies.contains(focusedEnemy) &&
                distance(focusedEnemy.x,focusedEnemy.y,x,y)<range)
            return focusedEnemy;
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
        if(heroAnimation.locked())return;
        Progression.Weapon weapon=Progression.WEAPONS[equippedWeapon];
        Enemy target=closest(heroX,heroY,
                weapon.range+weaponMastery.bonusRange(equippedWeapon));
        if(target==null) {shotTimer=Math.min(.2f,weapon.cooldown);return;}
        heroDir=(float)Math.atan2(target.y-heroY,target.x-heroX);
        if(!heroAnimation.attack(equippedWeapon))return;
        bufferedAttackTime=0;
        // Spawn the projectile precisely at the displayed weapon's rotating tip,
        // not at the center of the player sprite. This must use the same animation
        // time, attack fraction, and facing transform as MotionArt.hero().
        float animTime=heroMoving?walkClock:gameTime;
        float cast=pulseFlash>0?1-pulseFlash/.55f:-1f;
        WeaponRig.Point muzzle=WeaponRig.muzzle(equippedWeapon,0f,cast,
                animTime,heroMoving,heroDir);
        float muzzleX=heroX+muzzle.x,muzzleY=heroY+muzzle.y;
        shotTimer=weapon.cooldown*weaponMastery.cooldownMultiplier(equippedWeapon);
        audio.play("MELEE".equals(weapon.type)?GameAudio.BLADE:GameAudio.SHOT);
        fireFlash=WeaponAnimation.duration(equippedWeapon);
        if("MELEE".equals(weapon.type)) {
            // Queue contact damage at the MIDPOINT of the animated sword swing.
            // No 360-degree instant hit the moment the attack button is pressed.
            pendingMelee=true;
            pendingMeleeWeapon=equippedWeapon;
            pendingMeleeFacing=heroDir;
        } else {
            Shot shot=new Shot(muzzleX,muzzleY,weapon.damage*
                    ascension.attackMultiplier()*weaponMastery.damageMultiplier(equippedWeapon),
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
            if(!MeleeSwingRules.hits(heroX,heroY,pendingMeleeFacing,
                    weapon.range+weaponMastery.bonusRange(pendingMeleeWeapon),
                    e.x,e.y))continue;
            e.hp-=weapon.damage*ascension.attackMultiplier()*
                    weaponMastery.damageMultiplier(pendingMeleeWeapon);
            if(weaponMastery.mastered(pendingMeleeWeapon)) {
                // Titan Splitter has a concussive shockwave; Phoenix Drive
                // applies actual burning pressure on each sword contact.
                if(pendingMeleeWeapon==1){
                    e.slowTimer=Math.max(e.slowTimer,1.7f);
                    e.hp-=weapon.damage*.22f;
                }else if(pendingMeleeWeapon==2){
                    e.burnTimer=Math.max(e.burnTimer,4f);
                    e.burnDps=30f;
                }
            }
            e.hitReaction=.21f;
            combatFeel.onDamage(e.x,e.y-18,weapon.color,true,weapon.damage);
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
        if(pulseCooldown>0||scene!=Scene.PLAY||paused||briefingOpen||storyOpen||
                heroAnimation.locked())return;
        if(!WardenSkills.unlocked(fortressRank,equippedSkill))return;
        int skill=equippedSkill;
        WardenSkills.Skill info=WardenSkills.SKILLS[skill];
        pulseCooldown=Math.max(3f,WardenSkills.effectiveCooldown(skill,kingdom.shopRank(3))
                -ascension.spellCooldownReduction());
        if(heroAnimation.cast()){
            // Casting interrupts a melee windup. No invisible late sword hit
            // is allowed after the visibly different skill clip begins.
            pendingMelee=false;pendingMeleeWeapon=-1;
        }
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
            heroHp=WardenSkills.clampHealth(heroHp,24,heroMaxHp());
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
                    heroHp=WardenSkills.clampHealth(heroHp,7,heroMaxHp());
                if(WardenSkills.inRadius(bannerX,bannerY,550,BASE_X,BASE_Y))
                    baseHp=WardenSkills.clampHealth(baseHp,11,
                            coreMaxHp());
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
        for (int i=0;i<activeTowerCount;i++)
            if (towers[i]>0 && distance(x,y,towerX[i],towerY[i])<29) return true;
        return false;
    }
    private void repairTower(int choice){
        float max=TowerDurability.maxHp(towers[choice],fortressRank);
        int cost=TowerDurability.repairCost(towerHp[choice],max);
        if(cost<=0)return;
        if(gold<cost){announce("REPAIR NEEDS "+(cost-gold)+" MORE GOLD");return;}
        gold-=cost;towerHp[choice]=TowerDurability.repair(towerHp[choice],max);
        puff(towerX[choice],towerY[choice],12,0xffb4e5cc);
        audio.play(GameAudio.BUILD);announce("TOWER REPAIRED • "+cost+" GOLD");
    }
    private void damageTower(int slot,float amount){
        if(slot<0||slot>=activeTowerCount||towers[slot]<=0)return;
        towerHp[slot]=TowerDurability.damage(towerHp[slot],amount);
        puff(towerX[slot],towerY[slot]-30,12,0xffff9d7d);
        if(towerHp[slot]<=0){
            destroyedTowerTier[slot]=towers[slot];
            towers[slot]=0;towerTimers[slot]=0;
            announce("TOWER "+(slot+1)+" DESTROYED • REBUILD!");
        }else announce("TOWER "+(slot+1)+" DAMAGED • REPAIR!");
    }
    private int nearestBuiltTower(float x,float y,float range){
        int index=-1;float best=range*range;
        for(int pad=0;pad<activeTowerCount;pad++)if(towers[pad]>0){
            float dx=x-towerX[pad],dy=y-towerY[pad],d=dx*dx+dy*dy;
            if(d<best){index=pad;best=d;}
        }
        return index;
    }
    private void build(){
        int choice=selectedTowerPad;
        if(choice<0||choice>=activeTowerCount){
            choice=-1;
            for(int i=0;i<activeTowerCount;i++)
                if(choice==-1||towers[i]<towers[choice])choice=i;
        }
        if(choice<0){announce("NO TOWER SLOT AVAILABLE");return;}
        if(towers[choice]>0&&towerHp[choice]<
                TowerDurability.maxHp(towers[choice],fortressRank)-.01f){
            repairTower(choice);return;
        }
        if(towers[choice]>=BattleRules.MAX_TOWER_TIER){
            announce("SELECTED TOWER IS MAXED");return;
        }
        int cost=destroyedTowerTier[choice]>0&&towers[choice]==0?
                TowerDurability.rebuildCost(destroyedTowerTier[choice]):
                BattleRules.towerCost(towers[choice]);
        if(gold<cost){announce("NEED "+(cost-gold)+" MORE GOLD");return;}
        if(towers[choice]==0)missionBuilds++;
        gold-=cost;towers[choice]++;
        destroyedTowerTier[choice]=0;
        towerHp[choice]=TowerDurability.maxHp(towers[choice],fortressRank);
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
        audio.play(GameAudio.HURT);
        scene=Scene.LOSE;paused=false;
        heroAnimation.die();
        clearTouchControls();
    }
    private void complete() {
        recordBattleResult(true);
        audio.play(GameAudio.VICTORY);
        scene=Scene.WIN;paused=false;
        boolean firstClear=stage>=unlocked;
        int reward=Progression.stageReward(stage,firstClear);
        objectiveBonus=MissionObjectives.completed(stage,missionBuilds,
                baseHp/Math.max(1,coreMaxHp()),heroHp/Math.max(1,heroMaxHp()),
                defeatedElite)?MissionObjectives.bonus(stage):0;
        supplies+=reward+DifficultyRules.supplyBonus(difficultyMode,reward)
                +objectiveBonus;
        unlocked=Campaign.unlockedAfter(unlocked,stage);
        prefs.edit().putInt("unlocked",unlocked).putInt("supplies",supplies).apply();
        trialPassed=MemoryTrial.evaluate(trial,missionBuilds,kills,
                baseHp/Math.max(1,coreMaxHp()),heroHp/Math.max(1,heroMaxHp()),
                defeatedElite);
        trialAwarded=storyProgress.claimMemory(stage,trialPassed,true);
        if(firstClear&&DifficultyRules.worldBoss(stage))
            openStory(StoryScript.afterWorldBoss(Campaign.world(stage)));
        if(trialAwarded)openStory(StoryScript.memory(KeyMemories.forStage(stage)));
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
        // Shake the WORLD only; HUD and touch hitboxes stay fixed.
        c.translate(combatFeel.shakeX(),combatFeel.shakeY());
        c.translate(W*.5f-camX*BATTLE_ZOOM,H*.5f-camY*BATTLE_ZOOM);
        c.scale(BATTLE_ZOOM,BATTLE_ZOOM);
        // The HTML map is now the actual game terrain and route; old
        // mountain/castle/lake scenery no longer overlays the battlefield.
        // Full stage art, light shafts and shadows are cached at load time.
        // Only sparse weather/actor effects are painted on each battle frame.
        deployedMap.draw(c,stage,gameTime,combatFeel.reducedMotion());
        // Architecture is generated natively and world-positioned.\n        // Mission identity, terrain props and lanes are now generated by
        // NativeDeployedMapArt from the same seed as real enemy AI.
        drawStageHazards(c);
        drawPlacedSkills(c);
        if(selectedTowerPad>=0&&selectedTowerPad<activeTowerCount){
            float sx=towerX[selectedTowerPad],sy=towerY[selectedTowerPad];
            float pulse=(float)Math.sin(gameTime*3.4f)*4;
            circleStroke(c,sx,sy,58+pulse,0xfff2d68e,5);
            circleStroke(c,sx,sy,44,0x99b8fff0,2);
            text(c,"SELECTED PAD "+(selectedTowerPad+1),sx,sy-81,19,
                    0xffffe8ac,true);
        }
        // Stable depth sorting by world-space foot position. Trees can pass in
        // front of the hero while structures and monsters retain proper layering.
        drawQueue.clear();
        // HTML-native scenery is already baked into the cached terrain;
        // avoid overlaying the obsolete decorative actor placements.
        // The castle walls and masonry mounts render before the working guns.
        // Previously upper turret weapons were depth-sorted behind the castle art.
        layer(KEEP,BASE_Y-305,null,0);
        for(int i=0;i<activeTowerCount;i++)layer(TOWER,towerY[i]+31,null,i);
        // Only submit actors whose geometry intersects the world camera.
        // Their movement, collisions, damage and boss AI are still updated
        // independently; this avoids doing heavy skeletal drawing off-screen.
        final float actorHalfW=W*.5f/BATTLE_ZOOM+145f;
        final float actorHalfH=H*.5f/BATTLE_ZOOM+145f;
        for(Enemy e:enemies)
            if(Math.abs(e.x-camX)<=actorHalfW && Math.abs(e.y-camY)<=actorHalfH)
                layer(MONSTER,e.y+8,e,0);
        for(Pickup drop:pickups)
            if(Math.abs(drop.x-camX)<=actorHalfW && Math.abs(drop.y-camY)<=actorHalfH)
                layer(DROP,drop.y+12,drop,0);
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
            else if(entry.type==WARDEN){
                BattlefieldLighting.drawHeroContact(c,heroX,heroY,heroMoving,
                        walkClock,biome);
                drawHero(c);
            }
        }
        // Draw actual class-specific projectile silhouettes instead of identical dots.
        for(Shot s:shots){
            CombatVfx.projectile(c,s.x,s.y,s.target.x,s.target.y-10,
                    s.weaponId,s.tower?s.towerSlot:-1,gameTime);
        }
        // Enemy shaman bolts are animated physical objects with fixed targets.
        for(HostileBolt b:hostileBolts){
            float a=(float)Math.atan2(b.ty-b.y,b.tx-b.x);
            float bx=b.x-(float)Math.cos(a)*21,by=b.y-(float)Math.sin(a)*21;
            line(c,bx,by,b.x,b.y,0xff8959cb,10);
            line(c,bx+6,by-5,b.x,b.y,0xffe9caff,3);
            circle(c,b.x,b.y,8,0x88b777f0);
            polygon(c,new float[]{b.x+(float)Math.cos(a)*14,b.y+
                    (float)Math.sin(a)*14,b.x-8,b.y-7,b.x-8,b.y+7},0xffe7caff);
        }
        // Death animation persists after combat removal; rewards still count once.
        for(DeathEcho d:deaths){
            float progress=1f-Math.max(0,d.life)/.64f;
            int alpha=Math.max(0,Math.min(255,(int)(195*(1f-progress))));
            c.save();
            c.rotate(70f*progress,d.x,d.y);
            int layer=c.saveLayerAlpha(d.x-100,d.y-150,d.x+100,d.y+40,alpha);
            if(!USE_VECTOR_ENEMIES || !enemyRig.enemy(c,d.kind,Campaign.world(stage),
                    d.variant,d.x,d.y+progress*22f,d.phase,false,-1,true,
                    d.faceX,d.boss,d.bossPhase))
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
        combatFeel.drawContact(c);
        for(Spark s:sparks){
            p.setColor(s.color);p.setAlpha((int)(255*Math.max(0,s.life/s.maxLife)));
            c.drawCircle(s.x,s.y,2.5f+3*s.life/s.maxLife,p);p.setAlpha(255);
        }
        if(pulseFlash>0)drawActiveSkillVfx(c);
        BiomeAtmosphere.drawFront(c,biome,stage,gameTime);
        // Atmospheric particles are already drawn below the HUD.
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
        EvolutionEffects.cast(c,lastCastSkill,skillForm(),
                heroX,heroY,gameTime,t);
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
                if(!DodgeRules.invulnerable(dodgeTime))
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
        // BiomeAtmosphere owns animated weather and depth, keeping the terrain
        // pass focused on roads and landforms without doubled random dots.
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
        // The real game fort uses the exact HTML-deployed fort vocabulary
        // (wood walls, corner towers, hut and banner) with rank-aware skins.
        NativeDeployedMapArt.fort(c,BASE_X,BASE_Y,fortressRank,gameTime,
                baseHp/Math.max(1f,coreMaxHp()));
        FortressStructures.rankStructures(c,BASE_X,BASE_Y,fortressRank,gameTime);
        FortressSilhouetteArt.draw(c,BASE_X,BASE_Y,fortressRank,gameTime);
        FortressEvolutionArt.draw(c,BASE_X,BASE_Y,fortressRank,
                arsenalTier(),gameTime);
        FortressWeaponShowcaseArt.draw(c,BASE_X,BASE_Y,arsenalTier(),gameTime);
        EvolutionEffects.fortressBranches(c,BASE_X,BASE_Y,
                coreForm(),treasuryForm());
    }
    private int towerFamily(int slot){return towerTypes[slot];}
    private static int towerColor(int slot) {
        return new int[]{0xffffc678,0xffed9064,0xff91e4fb,0xffaf8dff}[slot];
    }
    /** Four distinctive functional fortress-mounted weapons. No generic sticks. */
    private void drawTower(Canvas c,float x,float y,int tier,int slot,int biome) {
        final int accent=towerColor(towerFamily(slot));
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
            if(destroyedTowerTier[slot]>0){
                for(int j=-2;j<=2;j++)circle(c,x+j*10,y+3+(j%2)*5,11,
                        j%2==0?0xff655451:0xff3c4046);
                line(c,x-23,y-17,x+16,y+8,0xffc38b70,5);
                if((int)(gameTime*3+slot)%3==0)circle(c,x-4,y-27,7,0x66545a5d);
                return;
            }
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
        if(towerFamily(slot)==0) { // Mounted ballista: twin limbs and visible bow string.
            rect(c,-20-back,-8,30-back,8,0xff9a7858);
            polygon(c,new float[]{-14,0,-36,-30,-47,-22,-32,4},0xffcda877);
            polygon(c,new float[]{-14,0,-36,30,-47,22,-32,-4},0xffcda877);
            line(c,-44,-23,30,0,0xfff6d6a0,2);
            line(c,-44,23,30,0,0xfff6d6a0,2);
            polygon(c,new float[]{47-back,0,27-back,-7,27-back,7},0xffdbe9ec);
        } else if(towerFamily(slot)==1) { // Siege cannon: blackened iron barrel and orange vent.
            round(c,-25-back,-15,38-back,15,5,0xff414852);
            round(c,-11-back,-19,27-back,19,4,0xff6f747a);
            rect(c,29-back,-20,43-back,20,0xffa8a9a4);
            circle(c,43-back,0,13,0xff252b36);
            circle(c,43-back,0,6,0xffce6d48);
            for(int bolt=-1;bolt<=1;bolt++)
                circle(c,bolt*14-back,-14,3,0xffecc27c);
        } else if(towerFamily(slot)==2) { // Frost launcher: icy prongs and rotating crystal.
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
        float fraction=Math.max(0,Math.min(1,towerHp[slot]/
                TowerDurability.maxHp(tier,fortressRank)));
        rect(c,x-29,y+37,x+29,y+43,0xff23323b);
        rect(c,x-27,y+38,x-27+54*fraction,y+42,
                fraction>.5?0xff82d7a9:fraction>.25?0xfff2ba6a:0xffee7770);
        if(fraction<.6f){
            line(c,x-22,top+12,x+4,top+29,0xff252c34,3);
            line(c,x+4,top+29,x-9,top+37,0xffe6c2a0,2);
            if(fraction<.32f)circle(c,x+23,top-36,8,0x55e4b0a6);
        }
    }
    private void drawEnemy(Canvas c,Enemy e) {
        if(e.kind==6 && e.dashWindup>0){
            line(c,e.x,e.y-30,heroX,heroY-18,0xaaffb778,4);
            text(c,"CHARGE!",e.x,e.y-98,20,0xffffd69b,true);
        }
        if(e.kind==6 && e.dashTime>0){
            for(int n=0;n<4;n++)
                line(c,e.x+40+n*14,e.y-22+n*7,e.x+62+n*14,
                        e.y-19+n*7,0xaaffc58c,3);
        }
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
                CombatImpactPolish.boss(c,e.x,e.y,e.specialCharge,
                        e.bossPhase,Campaign.world(stage),gameTime);
                circleStroke(c,e.x,e.y-38,95+e.specialCharge*25,
                        0xffffc18f,5);
                text(c,BossTactics.warning(stage),e.x,e.y-118,18,
                     0xffffcb9c,true);
            }
        }
        float attack=e.attackAnim>0?1-e.attackAnim/.42f:-1f;
        // Physical, timed hit-stagger rotates the actual actor in world space,
        // rather than showing only detached impact rings.
        c.save();
        if(e.hitReaction>0){
            float t=e.hitReaction/.21f;
            float jerk=(float)Math.sin(t*Math.PI*2f)*11f*t;
            c.rotate(jerk,e.x,e.y-48);
        }
        boolean painted=(USE_VECTOR_ENEMIES &&
                (enemyFrames.draw(c,e.kind,Campaign.world(stage),e.variant,
                        e.x,e.y,e.phase,e.moving,attack,
                        e.hitReaction>0||e.hp<e.maxHp*.20f,
                        e.faceX,e.boss,e.bossPhase)
                ||enemyRig.enemy(c,e.kind,
                        Campaign.world(stage),e.variant,e.x,e.y,e.phase,e.moving,
                        attack,e.hitReaction>0||e.hp<e.maxHp*.20f,e.faceX,
                        e.boss,e.bossPhase)))
                ||motionArt.enemy(c,e.kind,e.x,e.y,e.phase,e.moving,
                        attack,e.hitReaction>0||e.hp<e.maxHp*.20f)
                ||characterArt.enemy(c,e.kind,e.x,e.y,gameTime+e.phase,
                        e.hitReaction>0||e.hp<e.maxHp*.20f);
        c.restore();
        if(painted) {
            // Detailed actor-attached equipment uses the same real attack,
            // movement, boss phase and hit clocks as the illustrated sprite.
            EnemyAnimationPolish.draw(c,e.kind,e.x,e.y,e.phase,attack,
                    e.hitReaction,e.boss,e.bossPhase);
            CombatImpactPolish.statuses(c,e.x,e.y,gameTime,
                    e.slowTimer,e.burnTimer);
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
        if(dodgeTime>0){
            float progress=1-dodgeTime/DodgeRules.DURATION;
            CombatImpactPolish.dodge(c,heroX,heroY,dodgeDirX,dodgeDirY,progress);
            // Actual directional after-image trail, attached to the moving hero.
            for(int k=1;k<=3;k++){
                float offset=k*25f*progress;
                float xx=heroX-dodgeDirX*offset,yy=heroY-dodgeDirY*offset;
                oval(c,xx-20,yy+21,xx+20,yy+34,0x3359d3cf);
                circle(c,xx,yy-35,17,0x2276e2ee);
            }
            circleStroke(c,heroX,heroY-29,37,0x6677e9ef,3);
        }
        if(heroAnimation.state()==WardenAnimationMachine.State.DEATH){
            float t=heroAnimation.deathProgress();
            c.save();
            c.rotate(78f*t,heroX,heroY-47);
            int alpha=Math.max(64,Math.min(255,Math.round(255f-160f*t)));
            int layer=c.saveLayerAlpha(heroX-140,heroY-185,
                    heroX+140,heroY+65,alpha);
            drawHeroActor(c,true);
            // Armor, greaves and Warden share the SAME death transform/fade.
            EvolutionEffects.armor(c,heroX,heroY,armorForm(),gameTime);
            EvolutionEffects.greaves(c,heroX,heroY,speedForm());
            c.restoreToCount(layer);c.restore();
            return;
        }
        if(dodgeTime>0){
            float t=1-Math.max(0,dodgeTime)/DodgeRules.DURATION;
            // Armor and greaves lean with the real Warden body rather than
            // floating upright after the animated dodge transform is restored.
            c.save();
            c.rotate((float)Math.sin(Math.PI*t)*(-24f),heroX,heroY-53);
            c.scale(1.08f,.87f,heroX,heroY-4);
            drawHeroActor(c,false);
            EvolutionEffects.armor(c,heroX,heroY,armorForm(),gameTime);
            EvolutionEffects.greaves(c,heroX,heroY,speedForm());
            c.restore();
        }else{
            drawHeroActor(c,false);
            EvolutionEffects.armor(c,heroX,heroY,armorForm(),gameTime);
            EvolutionEffects.greaves(c,heroX,heroY,speedForm());
        }
    }
    private void drawHeroActor(Canvas c,boolean forcedHurt) {
        // State-machine clocks drive actual poses. Legacy preview callers
        // can still set fireFlash/pulseFlash directly for sprite QA captures.
        boolean interrupted=heroAnimation.isHurt()||
                heroAnimation.state()==WardenAnimationMachine.State.CAST||
                heroAnimation.state()==WardenAnimationMachine.State.DEATH;
        float attackProgress=heroAnimation.attackProgress();
        if(attackProgress<0&&!interrupted)
            attackProgress=WeaponAnimation.progress(equippedWeapon,fireFlash);
        float castProgress=heroAnimation.castProgress();
        if(castProgress<0&&!interrupted&&pulseFlash>0)
            castProgress=1-pulseFlash/.55f;
        float deathProgress=heroAnimation.state()==WardenAnimationMachine.State.DEATH
                ?heroAnimation.deathProgress():-1f;
        final boolean rigRendered=USE_VECTOR_WARDEN&&wardenRig.hero(c,heroX,heroY,
                heroMoving?walkClock:gameTime,heroDir,heroMoving,
                attackProgress,castProgress,
                forcedHurt||heroAnimation.isHurt()||hurtCooldown>.1f,
                equippedWeapon,weaponMastery.count(equippedWeapon),
                weaponForm(equippedWeapon),armorForm(),deathProgress);
        if(!firstBattleActorDiag && scene==Scene.PLAY) {
            firstBattleActorDiag=true;
            android.util.Log.i("RiftGuardVisual",
                    "hero-rig="+rigRendered+" weapon="+equippedWeapon+
                    " form="+weaponForm(equippedWeapon)+
                    " armor="+armorForm()+" facing="+heroDir+
                    " clock="+(heroMoving?walkClock:gameTime)+
                    " style="+WardenVisualStyles.name(
                            WardenVisualStyles.forWeapon(equippedWeapon)));
        }
        if(rigRendered)return;
        if(motionArt.hero(c,heroX,heroY,heroMoving?walkClock:gameTime,
                heroDir,heroMoving,attackProgress,castProgress,
                forcedHurt||heroAnimation.isHurt()||hurtCooldown>.1f,equippedWeapon,
                weaponMastery.count(equippedWeapon),weaponForm(equippedWeapon)))return;
        if(characterArt.available()) {
            boolean moving=Math.abs(axisX)+Math.abs(axisY)>.1f;
            characterArt.hero(c,heroX,heroY,gameTime,moving,fireFlash>0,hurtCooldown>.1f);
            // The currently equipped weapon still changes independently of hero art.
            c.save(); c.translate(heroX+22,heroY-27);
            c.rotate(fireFlash>0?28:0);
            EvolvedWeaponArt.draw(c,equippedWeapon,weaponForm(equippedWeapon),
                    0,0,.38f,gameTime);
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
        EvolvedWeaponArt.draw(c,equippedWeapon,weaponForm(equippedWeapon),
                26,-18,.66f,gameTime);
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
    private float dodgeHudX(){return 729f+hudSideBleed()*.32f;}
    private float buildHudX(){return 849f+hudSideBleed()*.49f;}
    private float pulseHudX(){return 974f+hudSideBleed()*.65f;}
    private float attackHudX(){return 1130f+hudSideBleed()*.8f;}

    private void drawHud(Canvas c,int biome) {
        drawHudTop(c,biome);
        if(focusedEnemy!=null&&focusedEnemy.hp>0&&enemies.contains(focusedEnemy)){
            float px=640+(focusedEnemy.x-LaneRules.VIEW_CENTER_X)*BATTLE_ZOOM;
            float py=360+(focusedEnemy.y-LaneRules.VIEW_CENTER_Y)*BATTLE_ZOOM;
            circleStroke(c,px,py-49,31,0xffffd18b,3);
            line(c,px-37,py-49,px-23,py-49,0xffffe1a2,3);
            text(c,"FOCUS FIRE",px,py-92,16,0xffffe1a2,true);
        }else if(focusedEnemy!=null)focusedEnemy=null;
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
        action(c,dodgeHudX(),590,42,
                dodgeTime>0?0xff66b8bc:0xff487d95,"DODGE",
                dodgeCooldown>0?""+(int)Math.ceil(dodgeCooldown)+"s":"READY");
        int chosen=selectedTowerPad;
        boolean canRepair=chosen>=0&&chosen<activeTowerCount&&towers[chosen]>0&&
                towerHp[chosen]<TowerDurability.maxHp(towers[chosen],fortressRank);
        boolean rubble=chosen>=0&&chosen<activeTowerCount&&destroyedTowerTier[chosen]>0;
        action(c,buildHudX(),589,50,canRepair?0xff4d937d:0xff4c837d,
                canRepair?"REPAIR":rubble?"REBUILD":"BUILD",
                canRepair?""+TowerDurability.repairCost(towerHp[chosen],
                        TowerDurability.maxHp(towers[chosen],fortressRank)):
                        rubble?""+TowerDurability.rebuildCost(destroyedTowerTier[chosen]):
                        nextTier>=BattleRules.MAX_TOWER_TIER?"MAX":
                        ""+BattleRules.towerCost(nextTier));
        action(c,pulseHudX(),593,49,WardenSkills.SKILLS[equippedSkill].color,
                WardenSkills.SKILLS[equippedSkill].name.toUpperCase().split(" ")[0],
                pulseCooldown>0?""+(int)Math.ceil(pulseCooldown)+"s":"READY");
        action(c,attackHudX(),560,70,0xffb3684c,"ATTACK","FIRE");
        if(waiting && wave<DifficultyRules.waves(stage,difficultyMode)){
            boolean bossNext=DifficultyRules.worldBoss(stage)&&
                    wave+1==DifficultyRules.waves(stage,difficultyMode);
            int bonus=WavePacingV065.earlyBonus(intermission,difficultyMode,bossNext);
            button(c,452,216,828,270,"NEXT WAVE  •  "+
                    (int)Math.ceil(intermission)+"s  •  +"+bonus+" GOLD",0xff77604d);
        }
        if(waveNotice>0 && !bossActive) {
            String text=waiting?"WAVE CLEARED!  +25 GOLD":"WAVE "+wave+"  •  DEFEND THE CORE";
            text(c,text,640,159,34,0xfff2e4bf,true);
        }
        if(noticeTimer>0 && !bossActive)
            text(c,notice,640,207,30,GOLD,true);
        if(!bossActive && waveNotice<=0 && noticeTimer<=0){
            String challenge=MissionObjectives.name(stage)+" • "+
                    MissionObjectives.status(stage,missionBuilds,
                            baseHp/Math.max(1f,coreMaxHp()),
                            heroHp/Math.max(1f,heroMaxHp()),defeatedElite);
            round(c,448,119,830,153,8,0xcc203941);
            textFit(c,challenge,639,142,365,18,0xffe5d9b1,true);
        }
        text(c,"MOVE",stickX,688,17,0xffb5cfc5,true);
        text(c,selectedTowerPad<0?"PADS "+activeTowerCount:
                "PAD "+(selectedTowerPad+1)+"/"+activeTowerCount,
                buildHudX(),680,15,0xffb9ddd2,true);
        EvolvedWeaponArt.draw(c,equippedWeapon,weaponForm(equippedWeapon),
                624,652,.61f,gameTime);
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
                text(c,"PHASE "+e.bossPhase+" / 3 • "+
                        BossTactics.warning(stage)+" • DODGE!",640,190,21,
                        0xffffc18f,true);
            return true;
        }
        return false;
    }

    private int nextTowerTier() {
        int tier=BattleRules.MAX_TOWER_TIER;
        for(int pad=0;pad<activeTowerCount;pad++)tier=Math.min(tier,towers[pad]);
        return tier;
    }
    private void drawHudTop(Canvas c,int biome) {
        round(c,15,12,1250,102,22,0xee13252c);
        text(c,Campaign.title(stage),45,48,25,GOLD,false);
        text(c,Campaign.worldName(stage),45,82,20,INK,false);
        text(c,"CORE",319,38,15,0xffafc6c6,false);
        bar(c,316,47,243,18,baseHp/coreMaxHp(),0xff79cfad);
        text(c,"HERO",319,84,15,0xffafc6c6,false);
        bar(c,377,69,182,13,heroHp/heroMaxHp(),0xffe7a07e);
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
        button(c,95,415,336,490,"CAMPAIGN",0xffa56a49);
        button(c,348,415,590,490,"CHRONICLE",0xff73567e);
        button(c,95,509,328,572,"ARMORY",0xff3d6971);
        button(c,348,509,590,572,"FORTRESS",0xff44645c);
        text(c,"FORTRESS RANK "+fortressRank+"   •   SUPPLIES "+supplies,
                98,614,21,GOLD,false);
        text(c,"NO ADS  •  OFFLINE PLAY  •  OPTIONAL DOWNLOADS",101,643,15,0xff9eb8bb,false);
    }

    /** Identical layered fortress model in the Workshop and previews. */
    private void paintFullFortress(Canvas c,float x,float y,int rank,int arsenal){
        NativeDeployedMapArt.fort(c,x,y,rank,gameTime,1);
        FortressStructures.rankStructures(c,x,y,rank,gameTime);
        FortressSilhouetteArt.draw(c,x,y,rank,gameTime);
        FortressEvolutionArt.draw(c,x,y,rank,arsenal,gameTime);
        FortressWeaponShowcaseArt.draw(c,x,y,arsenal,gameTime);
        EvolutionEffects.fortressBranches(c,x,y,coreForm(),treasuryForm());
    }
    /** Standalone previews are explicitly labeled; never imply unowned gear. */
    private void workshopMiniature(Canvas c,float left,float top,int rank,
                                   int arsenal,String caption,boolean owned){
        round(c,left,top,left+202,top+142,12,0xff122c36);
        border(c,left,top,left+202,top+142,12,
                owned?0xff83cbbc:0xffab9271,1.5f);
        textFit(c,caption,left+101,top+19,184,16,
                owned?0xffd7f8e8:0xffffe2a4,true);
        c.save();
        c.clipRect(left+5,top+24,left+197,top+137);
        c.translate(left+102,top+112);
        c.scale(.31f,.31f);
        paintFullFortress(c,0,0,rank,arsenal);
        c.restore();
    }
    private void triggerEvolutionReveal(String title,String description){
        evolutionRevealTitle=title;
        evolutionRevealDescription=description;
        evolutionRevealTimer=2.35f;
        audio.play(GameAudio.EVOLUTION);
    }
    private void drawEvolutionReveal(Canvas c){
        float t=evolutionRevealTimer/2.35f;
        float spread=(1f-t)*8f;
        // Keep purchase celebrations clear of SUPPLIES, current/next models,
        // the upgrade cost and all four bottom buttons in the Workshop.
        // The top-right header strip is intentionally unoccupied there.
        if(scene==Scene.FORTRESS){
            round(c,712-spread,45,1210+spread,136,18,0xf118414b);
            border(c,713-spread,46,1209+spread,135,18,0xfff5d18d,3);
            circle(c,738,91,12+(float)Math.sin(gameTime*7)*2,
                    0xffe3c580);
            textFit(c,evolutionRevealTitle,972,83,436,23,GOLD,true);
            textFit(c,evolutionRevealDescription,972,111,436,16,
                    0xffc2e6df,true);
            return;
        }
        // Other tree screens have different headers, so preserve their
        // established centered celebration position.
        round(c,329-spread,142,951+spread,232,19,0xf118414b);
        border(c,330-spread,143,950+spread,231,19,0xfff5d18d,3);
        circle(c,369,188,16+(float)Math.sin(gameTime*7)*3,0xffe3c580);
        textFit(c,evolutionRevealTitle,659,180,518,28,GOLD,true);
        textFit(c,evolutionRevealDescription,659,210,520,18,
                0xffc2e6df,true);
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
        // One actual visual model for battle, Home and interactive Workshop.
        // The optional NEXT view does not change costs, stats or save data.
        int paintedRank=workshop&&previewNextFortressLook?
                Math.min(Progression.MAX_RANK,fortressRank+1):fortressRank;
        int paintedArsenal=workshop&&previewNextFortressLook&&
                paintedRank==Progression.MAX_RANK&&fortressRank==Progression.MAX_RANK?
                Math.min(4,arsenalTier()+1):
                EvolutionProgress.fortressArsenal(ascension,paintedRank);
        c.save();
        float sz=workshop?.94f:.83f;
        c.translate(centerX,bottomY+15);c.scale(sz,sz);
        paintFullFortress(c,0,0,paintedRank,paintedArsenal);
        c.restore();
        // The Home scene keeps animated characters. In the Workshop the
        // Warden previously occluded upgraded side bastions and weapon
        // mounts, so reserve the entire preview for the actual fortress.
        if(!workshop){
            float heroX=left+width*.74f,heroY=top+height*.63f;
            // Home preview uses the SAME earned detailed rig as live combat.
            boolean rendered=(USE_VECTOR_WARDEN&&wardenRig.hero(c,heroX,heroY,
                    gameTime,-2.2f,false,-1,-1,false,equippedWeapon,
                    weaponMastery.count(equippedWeapon),
                    weaponForm(equippedWeapon),armorForm(),-1))
                    ||motionArt.hero(c,heroX,heroY,gameTime,
                    -2.2f,false,-1,-1,false,equippedWeapon,
                    weaponMastery.count(equippedWeapon),weaponForm(equippedWeapon));
            if(!rendered)EvolvedWeaponArt.draw(c,equippedWeapon,
                    weaponForm(equippedWeapon),heroX,heroY-58,.9f,gameTime);
            EvolutionEffects.armor(c,heroX,heroY,armorForm(),gameTime);
            EvolutionEffects.greaves(c,heroX,heroY,speedForm());
            if(!USE_VECTOR_ENEMIES||!enemyRig.enemy(c,0,biome,1,
                    left+width*.96f,top+height*.65f,gameTime,
                    true,-1,false,-1))
                motionArt.enemy(c,0,left+width*.96f,top+height*.65f,gameTime,
                        true,-1,false);
            if(!USE_VECTOR_ENEMIES||!enemyRig.enemy(c,6,biome,2,
                    left+width*.89f,top+height*.44f,gameTime,
                    true,-1,false,-1))
                motionArt.enemy(c,6,left+width*.89f,top+height*.44f,gameTime,
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
        text(c,"RIFTGUARD  /  KINGDOM ATLAS",65,82,36,GOLD,false);
        text(c,"10 REALMS  •  300 STAGES",917,82,20,INK,false);
        round(c,60,107,882,581,22,0xffb68e58);
        round(c,64,111,879,578,18,0xff254a4b);
        int current=(selectedLevel-1)/10;
        if(regionalMap)
            RegionJourney.draw(c,current,selectedLevel,unlocked,gameTime,biomeProps);
        else AtlasArt.draw(c,current,unlocked,gameTime);
        button(c,729,48,873,93,regionalMap?"WORLD":"EXPLORE",0xff416c71);
        round(c,893,111,1216,576,20,0xff182f3a);
        textFit(c,Campaign.WORLDS[current].toUpperCase(),912,155,287,23,GOLD,false);
        text(c,"LEVEL "+selectedLevel+" / 100",916,185,22,INK,false);
        bar(c,912,189,284,5,unlocked/300f,0xffb9daa5);
        for(int m=0;m<3;m++) {
            int mission=Campaign.stage(selectedLevel,m);
            boolean accessible=Campaign.accessible(mission,unlocked);
            float top=203+m*108;
            round(c,901,top-3,1208,top+103,12,selectedMap==m?GOLD:0xff45616b);
            c.save();
            c.clipRect(905,top,1204,top+68);
            deployedMap.thumbnail(c,mission,905,top,299,68);
            drawStagePreview(c,mission,905,top);
            round(c,912,top+4,1010,top+27,6,0xdd1a3640);
            text(c,TowerPadLayout.count(mission)+" REAL PADS",
                    961,top+21,13,0xffffe3aa,true);
            // Name the actual stage's landscape directly on the illustrated
            // A/B/C mission preview, instead of another anonymous map number.
            rect(c,905,top+43,1204,top+67,0xd0182b35);
            text(c,EnvironmentDistricts.name(mission).toUpperCase(),
                    912,top+61,13,0xffe2f2dc,false);
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
        button(c,904,532,1049,571,"PREVIOUS",0xff42616a);
        button(c,1057,532,1206,571,"NEXT LEVEL",0xff42616a);
        int chosen=Campaign.stage(selectedLevel,selectedMap);
        button(c,406,599,875,655,
                Campaign.accessible(chosen,unlocked)?
                        "DEPLOY TO MAP "+(char)('A'+selectedMap):"UNLOCK PREVIOUS STAGE",
                Campaign.accessible(chosen,unlocked)?0xffb1764c:0xff46515b);
        button(c,77,602,325,660,"BACK TO HOME",0xff3e626b);
        // Fully interactive difficulty selector; affects actual combat, not art alone.
        text(c,"BATTLE DIFFICULTY",905,583,17,INK,false);
        for(int mode=0;mode<4;mode++) {
            float x=903+mode*78;
            round(c,x,594,x+74,633,9,mode==difficultyMode?0xffb97944:0xff344d58);
            text(c,DifficultyRules.NAMES[mode],x+37,620,mode==3?13:14,
                    mode==difficultyMode?0xffffffff:0xffd0dee0,true);
        }
        int baseReward=100;
        text(c,"HP x"+String.format(java.util.Locale.US,"%.2f",DifficultyRules.health(difficultyMode))+
                        "   HIT x"+String.format(java.util.Locale.US,"%.2f",DifficultyRules.enemyDamage(difficultyMode)),
                905,658,15,difficultyMode==3?0xffffa78e:GOLD,false);
        text(c,"+"+DifficultyRules.bonusWaves(difficultyMode)+" WAVES    BONUS +"+
                        DifficultyRules.supplyBonus(difficultyMode,baseReward)+"%",
                905,679,15,INK,false);
        text(c,"CLEARED  "+unlocked+" / 300",1110,699,15,GOLD,true);
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
            EvolvedWeaponArt.draw(c,i,weaponForm(i),
                    x+69,y+109,1.10f,gameTime);
            textFit(c,EvolutionProgress.formName(i,weaponForm(i)),
                    x+146,y+54,180,22,available?INK:0xffa2a6aa,false);
            text(c,w.type+"  /  MASTERY "+weaponMastery.count(i)+"/11",
                    x+147,y+85,16,w.color,false);
            text(c,"DMG "+(int)w.damage+"  •  RANGE "+(int)w.range,
                    x+147,y+113,16,INK,false);
            text(c,available?(i==equippedWeapon?"EQUIPPED  ✓":"TAP TO EQUIP"):
                    "UNLOCK: FORT RANK "+w.requiredRank,
                    x+148,y+162,17,available?GOLD:0xffffaa9c,false);
        }
        // Navigation is in the same bottom row as the fortress and both trees.
        upgradeActions(c,"HOME","FORTRESS","ABILITIES","WEAPON TREES",true);
        text(c,"TAP A WEAPON TO EQUIP  •  USE WEAPON TREES FOR UPGRADES",
                92,141,17,0xffb7d7d0,false);
    }

    /** Six separate bottom-to-top permanent upgrade trees, one for each weapon. */
    private void drawWeaponMastery(Canvas c){
        drawBackdrop(c);
        round(c,43,25,1237,696,25,0xf0152935);
        text(c,"WEAPON MASTERY",77,78,45,GOLD,false);
        text(c,"Six independent weapon trees. Purchase from bottom to summit.",
                80,109,19,INK,false);
        // Switching tabs never borrows another weapon's purchased upgrades.
        for(int weapon=0;weapon<6;weapon++){
            float left=77+weapon*189;
            boolean selected=weapon==selectedMasteryWeapon;
            round(c,left,122,left+176,166,11,
                    selected?0xffa27853:0xff405768);
            EvolvedWeaponArt.draw(c,weapon,weaponForm(weapon),
                    left+23,159,.33f,gameTime);
            String label=Progression.WEAPONS[weapon].name.toUpperCase();
            if(label.length()>15)label=label.substring(0,15);
            text(c,label,left+104,151,15,selected?0xffffe0a6:INK,true);
        }
        round(c,64,170,806,605,20,0xff35515d);
        round(c,WM_LEFT,WM_TOP,WM_RIGHT,WM_BOTTOM,13,0xff0c2030);
        c.save();c.clipRect(WM_LEFT,WM_TOP,WM_RIGHT,WM_BOTTOM);
        for(int i=0;i<45;i++){
            float px=WM_LEFT+((i*149+(int)(gameTime*2.4f))%718);
            float py=WM_TOP+((i*103+(int)(gameTime*1.2f))%405);
            circle(c,px,py,1.2f+(i%3)*.4f,0x665fa7bb);
        }
        for(int id=1;id<WeaponMastery.NODES_PER_WEAPON;id++){
            WeaponMastery.Node n=WeaponMastery.node(selectedMasteryWeapon,id);
            WeaponMastery.Node parent=WeaponMastery.node(selectedMasteryWeapon,n.parent);
            float px=WM_LEFT+WeaponMastery.x(parent.id);
            float py=WM_TOP+WeaponMastery.worldY(parent.id)-weaponTreeScroll;
            float x=WM_LEFT+WeaponMastery.x(n.id);
            float y=WM_TOP+WeaponMastery.worldY(n.id)-weaponTreeScroll;
            if(y<WM_TOP-80&&py<WM_TOP-80)continue;
            if(y>WM_BOTTOM+80&&py>WM_BOTTOM+80)continue;
            Path path=new Path();path.moveTo(px,py-30);
            path.cubicTo(px,py-65,x,y+65,x,y+30);
            boolean owned=weaponMastery.owned(selectedMasteryWeapon,id);
            boolean ready=weaponMastery.available(selectedMasteryWeapon,id,fortressRank);
            p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(owned?9:6);
            p.setColor(owned?0x7753dfb7:0x88445a67);c.drawPath(path,p);
            p.setStrokeWidth(owned?4:3);
            p.setColor(owned?0xffa6f4d2:ready?0xffd6bf80:0xff596e7b);
            c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
            if(owned){
                float t=(gameTime*.40f+id*.13f)%1f;
                float fx=px+(x-px)*t;
                float fy=py+(y-py)*t;
                circle(c,fx,fy-16,3.5f,0xffcefcef);
            }
        }
        for(int id=0;id<WeaponMastery.NODES_PER_WEAPON;id++){
            float x=WM_LEFT+WeaponMastery.x(id);
            float y=WM_TOP+WeaponMastery.worldY(id)-weaponTreeScroll;
            if(y<WM_TOP-65||y>WM_BOTTOM+65)continue;
            drawWeaponMasteryNode(c,selectedMasteryWeapon,id,x,y);
        }
        c.restore();p.setStyle(Paint.Style.FILL);
        text(c,"ASCEND  ▲",90,204,15,0xffc7dcc7,false);
        text(c,"ORIGIN  /  SWIPE UP",91,577,15,0xffbfd9c7,false);
        // Live details/purchase panel.
        round(c,822,177,1212,605,19,0xff273f4b);
        WeaponMastery.Node node=WeaponMastery.node(
                selectedMasteryWeapon,selectedWeaponMasteryNode);
        Progression.Weapon weapon=Progression.WEAPONS[selectedMasteryWeapon];
        text(c,"WEAPON PROGRESSION",846,214,23,GOLD,false);
        text(c,weapon.name.toUpperCase(),845,246,22,weapon.color,false);
        text(c,weaponMastery.count(selectedMasteryWeapon)+"/11 NODES  •  FORT "+
                fortressRank,845,276,18,INK,false);
        round(c,846,297,1191,365,13,0xff152c38);
        int currentForm=weaponForm(selectedMasteryWeapon);
        int previewForm=Math.max(currentForm,node.tier);
        EvolvedWeaponArt.draw(c,selectedMasteryWeapon,currentForm,883,350,.52f,gameTime);
        EvolvedWeaponArt.draw(c,selectedMasteryWeapon,previewForm,954,350,.52f,gameTime);
        textFit(c,"NOW  ->  NEXT APPEARANCE",1080,321,200,15,GOLD,true);
        textFit(c,EvolutionProgress.formName(selectedMasteryWeapon,previewForm),
                1080,348,205,16,0xffc3eddb,true);
        text(c,"TIER "+node.tier+"   /   "+WeaponMastery.PATH_NAMES[node.lane],
                846,403,18,INK,false);
        drawStoryParagraph(c,WeaponMastery.benefit(node),846,440,329,18,26,INK);
        text(c,"COST  "+node.cost+"   •   SUPPLIES  "+supplies,847,478,18,GOLD,false);
        String status=weaponMastery.owned(selectedMasteryWeapon,node.id)?
                "OWNED / ACTIVE":
                weaponMastery.available(selectedMasteryWeapon,node.id,fortressRank)?
                supplies>=node.cost?"READY TO PURCHASE":"NEED MORE SUPPLIES":
                "LOCKED: PARENT / FORT RANK";
        text(c,status,846,510,17,weaponMastery.owned(selectedMasteryWeapon,node.id)?
                0xff8eebbd:0xffffb18e,false);
        round(c,846,531,1191,579,11,0xff1c3844);
        textFit(c,"SELECT NODE • PURCHASE BELOW",1018,562,326,18,
                0xffd8e6dc,true);
        upgradeActions(c,"ARMORY",
                selectedMasteryWeapon==equippedWeapon?"EQUIPPED":"EQUIP",
                "CLIMB TREE",weaponMastery.owned(selectedMasteryWeapon,node.id)?
                "OWNED":"UNLOCK NODE",
                weaponMastery.available(selectedMasteryWeapon,node.id,fortressRank)
                        &&supplies>=node.cost);
    }

    private void drawWeaponMasteryNode(Canvas c,int weapon,int id,float x,float y){
        WeaponMastery.Node node=WeaponMastery.node(weapon,id);
        boolean owned=weaponMastery.owned(weapon,id);
        boolean available=weaponMastery.available(weapon,id,fortressRank);
        if(selectedWeaponMasteryNode==id){
            float pulse=(float)Math.sin(gameTime*3.6f)*3;
            round(c,x-45-pulse,y-44-pulse,x+45+pulse,y+42+pulse,
                    16,0x9986bad3);
        }
        round(c,x-37,y-38,x+37,y+36,13,
                owned?0xff82ddb4:available?0xffd6bf77:0xff455e6a);
        round(c,x-32,y-33,x+32,y+31,11,
                owned?0xff224d46:available?0xff29495a:0xff1b2f39);
        if(id==0||id==10){
            EvolvedWeaponArt.draw(c,weapon,node.tier,x,y+21,.34f,gameTime);
        }else if(node.lane==0){
            EvolvedWeaponArt.draw(c,weapon,node.tier,x,y+21,.30f,gameTime);
        }else if(node.lane==1){
            polygon(c,new float[]{x-16,y+9,x-9,y-10,x+3,y-15,
                    x+19,y+5,x+1,y+18},0xffb5d9e9);
            circle(c,x+2,y+3,5,0xff42677a);
        }else{
            circle(c,x,y-2,19,Progression.WEAPONS[weapon].color);
            polygon(c,new float[]{x,y-17,x+15,y,x,y+18,x-15,y},0xfff6e7c8);
        }
        if(owned){
            circle(c,x+31,y-31,10,0xff65cfa6);
            text(c,"✓",x+31,y-26,14,0xff18342d,true);
        }else if(!available){
            circle(c,x+31,y-31,9,0xff785563);
            text(c,"×",x+31,y-26,13,INK,true);
        }
        String title=node.name.length()>16?node.name.substring(0,16):node.name;
        text(c,title.toUpperCase(),x,y+55,14,
                owned?0xffd2ffea:available?0xffffddb0:0xff94aeb7,true);
    }

    private void tapWeaponTree(float x,float y){
        int action=UpgradeActionLayout.hit(x,y);
        if(action>=0){
            if(action==0)scene=Scene.ARMORY;
            else if(action==1){
                if(Progression.canEquip(fortressRank,selectedMasteryWeapon))
                    equipWeapon(selectedMasteryWeapon);
            }else if(action==2)
                weaponTreeScroll=Math.max(0,weaponTreeScroll-195);
            else if(action==3){
                int before=weaponForm(selectedMasteryWeapon);
                if(weaponMastery.buy(selectedMasteryWeapon,
                        selectedWeaponMasteryNode,fortressRank)){
                    supplies=prefs.getInt("supplies",supplies);
                    int after=weaponForm(selectedMasteryWeapon);
                    if(after>before)
                        triggerEvolutionReveal("NEW WEAPON FORM UNLOCKED",
                                EvolutionProgress.formName(selectedMasteryWeapon,after));
                    else
                        triggerEvolutionReveal("WEAPON MASTERY UPGRADED",
                                WeaponMastery.node(selectedMasteryWeapon,
                                        selectedWeaponMasteryNode).name);
                }
            }
            return;
        }
        if(y>=118&&y<169){
            int index=(int)((x-77)/189);
            if(index>=0&&index<6&&x>=77+index*189&&
                    x<=77+index*189+176){
                selectedMasteryWeapon=index;
                selectedWeaponMasteryNode=1;
                weaponTreeScroll=WM_SCROLL_MAX;
            }
            return;
        }
        if(x<WM_LEFT||x>WM_RIGHT||y<WM_TOP||y>WM_BOTTOM)return;
        for(int id=0;id<WeaponMastery.NODES_PER_WEAPON;id++){
            float nx=WM_LEFT+WeaponMastery.x(id);
            float ny=WM_TOP+WeaponMastery.worldY(id)-weaponTreeScroll;
            if(Math.abs(x-nx)<42&&Math.abs(y-ny)<44){
                selectedWeaponMasteryNode=id;return;
            }
        }
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
            // Wrap descriptions INSIDE the ability card. Long skills like
            // Meteor Strike must never cross borders or obscure other cards.
            drawStoryParagraph(c,skill.detail,x+146,y+114,184,16,18,INK);
            text(c,unlockedSkill?(i==equippedSkill?"EQUIPPED  ✓":"TAP TO EQUIP"):
                    "LOCKED UNTIL RANK "+skill.requiredRank,
                    x+145,y+160,17,unlockedSkill?GOLD:0xffffaa9c,false);
        }
        upgradeActions(c,"HOME","ARMORY","FORTRESS","WEAPON TREES",true);
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
        EvolutionEffects.badge(c,x,y,skillForm(),id);
    }

    private void equipSkill(int skillId){
        if(!WardenSkills.unlocked(fortressRank,skillId))return;
        equippedSkill=skillId;
        prefs.edit().putInt("skill",skillId).apply();
    }


    /** Upward-growing upgrade tree, with tangible node illustrations and real lines. */
    private void drawAscension(Canvas c) {
        drawBackdrop(c);
        round(c,40,24,1242,705,25,0xf0142734);
        text(c,"ASCENSION  /  BASTION TECH TREE",65,75,40,GOLD,false);
        text(c,"START AT THE BOTTOM   •   CONNECT UPWARDS   •   EACH NODE CHANGES COMBAT",
                67,109,18,INK,false);
        round(c,62,132,923,606,22,0xff415b64);
        round(c,TREE_LEFT,TREE_TOP,TREE_RIGHT,TREE_BOTTOM,16,0xff0c202c);
        c.save();
        c.clipRect(TREE_LEFT,TREE_TOP,TREE_RIGHT,TREE_BOTTOM);
        // Parallax rift lights and small dust particles make the tree alive.
        for(int i=0;i<52;i++){
            float px=TREE_LEFT+((i*191)%811);
            float py=TREE_TOP+((i*139+(int)(gameTime*(i%4+2)*3))%460);
            circle(c,px,py,1.3f+(i%3)*.45f,0x557cc3d0);
        }
        for(AscensionTree.Node node:AscensionTree.NODES) {
            if(node.predecessor<0)continue;
            AscensionTree.Node parent=AscensionTree.byId(node.predecessor);
            float x0=TREE_LEFT+AscensionTree.x(parent);
            float y0=TREE_TOP+AscensionTree.worldY(parent)-ascensionScroll;
            float x1=TREE_LEFT+AscensionTree.x(node);
            float y1=TREE_TOP+AscensionTree.worldY(node)-ascensionScroll;
            if(Math.min(y0,y1)>TREE_BOTTOM+70||
                    Math.max(y0,y1)<TREE_TOP-70)continue;
            boolean lit=ascension.owned(node.id);
            int color=lit?0xff9ce9d3:ascension.available(node.id,fortressRank)?
                    0xffa7c7bb:0xff415a69;
            // Connect the preceding bottom tier to each new branch above it.
            Path track=new Path();
            track.moveTo(x0,y0-38);
            track.cubicTo(x0,y0-96,x1,y1+96,x1,y1+39);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(lit?9:6);
            p.setColor(lit?0x4471ffee:0x88374f5d);
            c.drawPath(track,p);
            p.setStrokeWidth(lit?4.5f:3);p.setColor(color);
            c.drawPath(track,p);
            p.setStyle(Paint.Style.FILL);
            if(lit) {
                float t=(gameTime*.32f+(node.id%7)*.14f)%1f;
                float mx=x0+(x1-x0)*t;
                float my=(y0-38)+(y1-y0+77)*(t*t*(3-2*t));
                circle(c,mx,my,5,0xb0c9fff0);
            }
        }
        for(AscensionTree.Node node:AscensionTree.NODES) {
            float x=TREE_LEFT+AscensionTree.x(node);
            float y=TREE_TOP+AscensionTree.worldY(node)-ascensionScroll;
            if(y<TREE_TOP-75||y>TREE_BOTTOM+75)continue;
            drawAscensionNode(c,node,x,y);
        }
        c.restore();
        p.setStyle(Paint.Style.FILL);p.setShader(null);
        // Vertical reading rail indicates the current climb through the tree.
        round(c,912,160,919,574,3,0xff395463);
        float amount=ascensionScroll/
                Math.max(1,AscensionTree.worldHeight()-(TREE_BOTTOM-TREE_TOP));
        round(c,911,160+(1-amount)*358,920,217+(1-amount)*358,4,
                0xffc7dcac);
        text(c,"SUMMIT",85,169,15,0xffa1c9c8,false);
        text(c,"ORIGIN  /  SWIPE UP",86,576,16,0xffa5d7cb,false);
        // Right details panel communicates precise requirements and benefits.
        round(c,937,132,1224,606,22,0xff253f4c);
        text(c,"YOUR ASCENSION",960,167,23,GOLD,false);
        text(c,ascension.purchasedCount()+"/40 NODES",960,195,19,INK,false);
        text(c,"SUPPLIES  "+supplies,960,224,21,0xffffd59b,false);
        AscensionTree.Node choice=AscensionTree.byId(selectedAscensionNode);
        int tint=AscensionTree.COLORS[choice.category];
        round(c,960,243,1202,328,15,0xff172e3c);
        drawAscensionGlyph(c,choice.category,998,289,tint);
        drawStoryParagraph(c,choice.name.toUpperCase(),1035,279,162,21,25,GOLD);
        text(c,AscensionTree.CATEGORIES[choice.category],961,358,18,tint,false);
        drawStoryParagraph(c,AscensionTree.benefit(choice),961,388,245,20,26,INK);
        text(c,"TIER "+choice.tier+"  /  FORT RANK "+choice.requiredRank,961,443,18,INK,false);
        if(choice.category==AscensionTree.TOWER)
            textFit(c,"ARSENAL: "+EvolutionProgress.fortressWeapon(arsenalTier()),
                    962,461,235,16,0xffa8e6d9,false);
        else if(choice.category==AscensionTree.HERO)
            text(c,"ARMOR FORM "+armorForm()+"/4",961,461,16,0xffa8e6d9,false);
        else if(choice.category==AscensionTree.SKILL)
            text(c,"SKILL FORM "+skillForm()+"/4",961,461,16,0xffa8e6d9,false);
        else if(choice.category==AscensionTree.CORE)
            text(c,"MASONRY FORM "+coreForm()+"/4",961,461,16,
                    0xffa8e6d9,false);
        else if(choice.category==AscensionTree.GOLD)
            text(c,"TREASURY FORM "+treasuryForm()+"/4",961,461,16,
                    0xffa8e6d9,false);
        else if(choice.category==AscensionTree.SPEED)
            text(c,"GREAVES FORM "+speedForm()+"/4",961,461,16,
                    0xffa8e6d9,false);
        String status=ascension.owned(choice.id)?"OWNED / ACTIVE":
                fortressRank<choice.requiredRank?"FORT RANK TOO LOW":
                choice.predecessor>=0&&!ascension.owned(choice.predecessor)?
                        "UNLOCK PREVIOUS NODE":
                supplies<choice.cost?"NOT ENOUGH SUPPLIES":"READY TO PURCHASE";
        text(c,status,961,485,17,ascension.owned(choice.id)?
                0xff83e0b8:0xffffb18e,false);
        text(c,"COST  "+choice.cost,961,513,21,GOLD,false);
        round(c,957,526,1205,577,11,0xff173441);
        textFit(c,"UNLOCK FROM THE BOTTOM ROW",1081,556,233,16,
                0xffc3dad9,true);
        upgradeActions(c,"FORTRESS","ARMORY","CLIMB TREE",
                ascension.owned(choice.id)?"OWNED":"UNLOCK NODE",
                ascension.available(choice.id,fortressRank)&&supplies>=choice.cost);
    }

    private void drawAscensionNode(Canvas c,AscensionTree.Node node,float x,float y){
        boolean bought=ascension.owned(node.id);
        boolean open=ascension.available(node.id,fortressRank);
        boolean chosen=node.id==selectedAscensionNode;
        int accent=AscensionTree.COLORS[node.category];
        int border=bought?0xff8be3b7:open?0xffe9bd77:0xff4a6470;
        if(chosen){
            float pulse=(float)Math.sin(gameTime*3.4f)*3f;
            round(c,x-47-pulse,y-51-pulse,x+47+pulse,y+49+pulse,19,
                    0x998dd5eb);
        }
        oval(c,x-32,y+34,x+35,y+47,0x77040912);
        round(c,x-39,y-44,x+39,y+38,16,border);
        round(c,x-34,y-39,x+34,y+33,13,
                bought?0xff225348:open?0xff344e56:0xff22343e);
        drawAscensionGlyph(c,node.category,x,y-4,
                bought||open?accent:0xff78909c);
        if(bought){
            circle(c,x+32,y-38,11,0xff65cc9e);
            text(c,"✓",x+32,y-32,14,0xff112c25,true);
        }else if(!open){
            circle(c,x+32,y-38,10,0xff6b4f55);
            text(c,"×",x+32,y-32,13,INK,true);
        }else{
            float star=(float)Math.sin(gameTime*4+node.id)*2f;
            circle(c,x+30,y-35,9+star,0xffd8bd7a);
        }
        // Short type labels beneath actual illustrated nodes, not node numbers.
        String name=node.name.length()>18?node.name.substring(0,17):node.name;
        text(c,name.toUpperCase(),x,y+65,15,bought?0xffd3f5e5:
                open?0xffffddae:0xff90a3a9,true);
    }

    private void drawAscensionGlyph(Canvas c,int category,float x,float y,int color){
        c.save();c.translate(x,y);
        switch(category){
            case AscensionTree.ATTACK:
                // The actual Warden's independent greatsword model.
                EvolvedWeaponArt.draw(c,1,weaponForm(1),0,19,.45f,gameTime);
                break;
            case AscensionTree.CORE:
                polygon(c,new float[]{-23,27,-23,-10,-14,-10,-14,-25,
                        -5,-25,-5,-10,7,-10,7,-25,17,-25,17,-10,25,-10,
                        25,27},color);
                rect(c,-8,3,10,28,0xff1a3646);
                circle(c,1,-6,6,0xfff1d7a2);
                break;
            case AscensionTree.TOWER:
                round(c,-24,11,24,29,4,0xff81959b);
                polygon(c,new float[]{-20,8,0,-24,20,8},color);
                rect(c,-6,-10,7,20,0xff324c58);
                line(c,0,-20,0,-32,0xffe8d5a5,4);
                break;
            case AscensionTree.HERO:
                circle(c,0,-17,11,color);
                polygon(c,new float[]{-23,25,-15,-4,0,2,14,-4,24,25},color);
                polygon(c,new float[]{-10,10,0,20,10,10,0,1},0xff274458);
                break;
            case AscensionTree.GOLD:
                circle(c,0,0,24,0xffa0763c);
                circle(c,0,0,18,color);
                circleStroke(c,0,0,15,0xffffe9b5,3);
                text(c,"✦",0,8,25,0xff684c2d,true);
                break;
            case AscensionTree.SKILL:
                polygon(c,new float[]{-5,-32,11,-14,-2,-3,17,4,1,30,
                        -16,8,-4,-8,-20,-12},color);
                circle(c,17,-16,6,0xfff0defe);
                break;
            case AscensionTree.SPEED:
                for(int i=0;i<3;i++)
                    line(c,-25,-15+i*16,13,-24+i*15,color,5);
                polygon(c,new float[]{0,-30,29,-16,2,7,12,-9},0xffbaf4f0);
                break;
        }
        c.restore();
    }

    private void tapAscension(float x,float y) {
        int action=UpgradeActionLayout.hit(x,y);
        if(action>=0){
            if(action==0)scene=Scene.FORTRESS;
            else if(action==1)scene=Scene.ARMORY;
            else if(action==2)
                ascensionScroll=Math.max(0,ascensionScroll-225);
            else if(action==3){
                int oldArsenal=arsenalTier();
                if(ascension.buy(selectedAscensionNode,fortressRank)){
                    supplies=prefs.getInt("supplies",supplies);
                    int currentArsenal=arsenalTier();
                    if(currentArsenal>oldArsenal)
                        triggerEvolutionReveal("NEW FORTRESS WEAPON",
                                EvolutionProgress.fortressWeapon(currentArsenal));
                    else
                        triggerEvolutionReveal("ASCENSION FORM EVOLVED",
                                AscensionTree.byId(selectedAscensionNode).name);
                }
            }
            return;
        }
        if(x<TREE_LEFT||x>TREE_RIGHT||y<TREE_TOP||y>TREE_BOTTOM)return;
        for(AscensionTree.Node node:AscensionTree.NODES){
            float nx=TREE_LEFT+AscensionTree.x(node);
            float ny=TREE_TOP+AscensionTree.worldY(node)-ascensionScroll;
            if(Math.abs(x-nx)<43&&Math.abs(y-ny)<48){
                selectedAscensionNode=node.id;
                audio.play(GameAudio.BUILD);
                return;
            }
        }
    }

    private void drawFortress(Canvas c) {
        drawBackdrop(c);
        round(c,48,29,1233,694,25,0xf0172e36);
        text(c,"FORTRESS WORKSHOP",85,86,50,GOLD,false);
        text(c,"Every rank changes the building. TURRET tree nodes evolve the weapons.",
                88,125,19,INK,false);
        drawLiveMenuScene(c,75,158,600,390,true);
        boolean canPreview=fortressRank<Progression.MAX_RANK||arsenalTier()<4;
        if(canPreview){
            button(c,439,504,661,544,
                    previewNextFortressLook?"SHOW CURRENT":"PREVIEW NEXT",
                    previewNextFortressLook?0xff9a794e:0xff4e8081);
        }
        if(previewNextFortressLook&&canPreview){
            round(c,86,171,339,204,8,0xe62c5053);
            text(c,"FUTURE FORM  •  PREVIEW",212,193,16,0xffffe1a5,true);
        }
        round(c,77,553,669,607,13,0xee1c3942);
        textFit(c,"RANK "+fortressRank+" / 5   "+Progression.RANK_NAMES[fortressRank],
                98,577,536,24,GOLD,false);
        textFit(c,"CORE HP "+(Progression.maxCoreHp(fortressRank)+ascension.coreHp())+
                "  •  STARTING TOWERS "+(fortressRank>=5?3:fortressRank>=4?2:1),
                98,597,536,18,INK,false);
        round(c,718,153,1204,531,22,0xff283e4a);
        text(c,"SUPPLIES",752,196,24,INK,false);
        text(c,""+supplies,752,260,55,GOLD,false);
        textFit(c,"ACTIVE ARSENAL: "+EvolutionProgress.fortressWeapon(arsenalTier()),
                752,301,436,18,0xffb0e9e3,false);
        int nextRank=Math.min(Progression.MAX_RANK,fortressRank+1);
        int nextArsenal=EvolutionProgress.fortressArsenal(ascension,nextRank);
        if(nextRank==fortressRank)
            nextArsenal=Math.min(4,arsenalTier()+1);
        String nextName=nextRank>fortressRank?
                Progression.RANK_NAMES[nextRank]:
                EvolutionProgress.fortressWeapon(nextArsenal);
        textFit(c,"UP NEXT: "+nextName.toUpperCase(),752,331,430,19,GOLD,false);
        workshopMiniature(c,750,343,fortressRank,arsenalTier(),"CURRENT • OWNED",true);
        workshopMiniature(c,973,343,nextRank,nextArsenal,"NEXT • PREVIEW",false);
        String requirement=fortressRank<Progression.MAX_RANK?
                "FORTRESS COST  "+Progression.upgradeCost(fortressRank)+" SUPPLIES":
                (arsenalTier()<4?"UPGRADE TURRET NODES IN ASCENSION":
                        "ALL FORTRESS FORMS UNLOCKED");
        textFit(c,requirement,750,518,434,19,
                fortressRank<Progression.MAX_RANK?INK:0xffaee8dc,false);
        upgradeActions(c,"HOME","ARMORY","ASCENSION",
                fortressRank==Progression.MAX_RANK?"MAX RANK":"UPGRADE FORT",
                Progression.canUpgrade(fortressRank,supplies));
    }

    private void upgradeFortress() {
        if(!Progression.canUpgrade(fortressRank,supplies))return;
        supplies-=Progression.upgradeCost(fortressRank);
        fortressRank++;
        previewNextFortressLook=false;
        prefs.edit().putInt("supplies",supplies).putInt("fortress_rank",fortressRank).apply();
        triggerEvolutionReveal("FORTRESS FORM "+fortressRank+" UNLOCKED",
                Progression.RANK_NAMES[fortressRank]+" • REAL STRUCTURE EVOLVED");
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
        if(scene==Scene.WIN&&trial!=null){
            String memoryLabel=storyProgress.hasMemory(stage)?
                    (trialAwarded?"MEMORY KEY RECOVERED":"MEMORY KEY ALREADY OWNED"):
                    "MEMORY TRIAL MISSED • REPLAY TO RECOVER";
            textFit(c,memoryLabel,640,364,510,17,
                    storyProgress.hasMemory(stage)?0xffabf2d1:0xffffbb97,true);
        }
        text(c,"Enemies defeated: "+kills+"     Gold earned: "+gold,
                640,388,21,INK,true);
        if(scene==Scene.WIN)
            textFit(c,"TACTICAL BONUS  +"+objectiveBonus+" SUPPLIES",640,418,
                    430,19,objectiveBonus>0?0xff9bedd0:0xffc0c6c8,true);
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
        deployedMap.preview(c,stage,127,200,429,324);
        drawStagePreview(c,stage,127,200);
        c.restore();
        border(c,127,200,556,524,11,0xffb78a57,3);
        text(c,story.headline,593,225,27,GOLD,false);
        text(c,"THE CHRONICLE",596,270,18,0xff9cb6ba,false);
        drawStoryParagraph(c,story.lore,594,305,552,23,33,0xffecede5);
        text(c,"WARDEN'S ORDERS",596,384,18,0xff9cb6ba,false);
        drawStoryParagraph(c,story.order,594,418,552,22,31,INK);
        textFit(c,story.objective,134,570,410,21,0xffc0e0bf,false);
        textFit(c,trial!=null?"MEMORY: "+trial.title:
                TowerPadLayout.count(stage)+" PADS • "+MissionObjectives.name(stage),
                134,604,413,16,0xffffd7a1,false);
        drawStoryParagraph(c,story.warning,594,523,538,19,26,0xffffc38f);
        button(c,688,460,1134,510,"FORMATION "+(selectedLoadout+1)+" : "+
                defenseLoadouts.name(selectedLoadout)+"  (CHANGE)",0xff546478);
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


    private void openStory(StoryScript.Line[] lines){
        if(lines==null||lines.length==0)return;
        pendingStory.add(lines);
        if(!storyOpen)nextStoryBlock();
    }
    private void nextStoryBlock(){
        if(pendingStory.isEmpty()){
            storyOpen=false;storyLines=null;storyIndex=0;return;
        }
        storyLines=pendingStory.remove(0);
        storyIndex=0;storyOpen=true;clearTouchControls();
    }
    private void advanceStoryLine(){
        if(!storyOpen)return;
        if(++storyIndex>=storyLines.length)nextStoryBlock();
    }
    private void dismissStory(){
        pendingStory.clear();storyOpen=false;storyLines=null;storyIndex=0;
        clearTouchControls();
    }
    /** Dialogue overlay: built on the existing playable scene, no UI-only demo. */
    private void drawStoryScene(Canvas c){
        if(storyLines==null||storyIndex<0||storyIndex>=storyLines.length)return;
        StoryScript.Line ln=storyLines[storyIndex];
        boolean cael=ln.speaker.equals("TITAN")||ln.speaker.equals("CAEL")||
                ln.speaker.equals("CAEL'S MEMORY");
        boolean memory=ln.speaker.endsWith("MEMORY");
        rect(c,0,0,W,H,0xd3091722);
        round(c,73,48,1207,672,25,0xf41b303c);
        border(c,73,48,1207,672,25,cael?0xffbca7eb:0xffd6ad70,3);
        text(c,memory?"THE WARDEN'S CHRONICLE":cael?"ECHOES OF THE RIFT":
                "ECHOES OF THE RIFT  •  STORY",112,110,30,GOLD,false);
        // Reuse live detailed actor rigs for visual identity while authored
        // illustrated portrait/fully animated cinematic media is produced.
        c.save();
        c.clipRect(93,148,1187,426);
        round(c,102,145,1178,430,12,cael?0xff302d4b:0xff304f52);
        for(int i=0;i<16;i++){
            float x=130+i*65,y=158+(i*43)%212;
            circle(c,x+(float)Math.sin(gameTime+i)*8,y,2+(i%3),0x99dac7a5);
        }
        if(cael){
            c.save();c.translate(683,414);c.scale(2.0f,2.0f);
            enemyRig.enemy(c,5,9,3,0,0,gameTime,true,-1,true,2);
            c.restore();
        }else{
            c.save();c.translate(625,420);c.scale(1.65f,1.65f);
            wardenRig.hero(c,0,0,gameTime,-2.0f,false,-1,-1,false,
                    equippedWeapon,weaponMastery.count(equippedWeapon),
                    weaponForm(equippedWeapon),armorForm(),-1);
            c.restore();
        }
        c.restore();
        String speaker=ln.speaker.equals("WARDEN")?storyProgress.wardenName():ln.speaker;
        text(c,speaker,113,473,28,cael?0xffd8bdfa:GOLD,false);
        drawStoryParagraph(c,ln.text,113,523,1044,24,38,INK);
        text(c,"TAP TO CONTINUE   "+(storyIndex+1)+" / "+storyLines.length+
                "   •   BACK TO SKIP",934,641,17,0xffc2d3d3,true);
    }

    private void drawChronicle(Canvas c){
        drawBackdrop(c);
        round(c,43,24,1237,691,24,0xf017303d);
        text(c,"THE WARDEN'S CHRONICLE",79,84,47,GOLD,false);
        text(c,"WARDEN: "+storyProgress.wardenName(),79,123,21,INK,false);
        text(c,"MEMORIES "+storyProgress.total()+"/100   •   WORLD SEALS "+
                storyProgress.ownedSeals()+"/10",810,115,20,GOLD,false);
        for(int world=0;world<10;world++){
            float y=149+world*45;
            round(c,72,y,572,y+41,8,chronicleWorld==world?0xff70674d:0xff2c4b55);
            textFit(c,(world+1)+". "+Campaign.WORLDS[world],90,y+27,372,19,INK,false);
            text(c,storyProgress.count(world)+"/10",540,y+27,19,
                    storyProgress.count(world)==10?0xff91dfb6:GOLD,true);
        }
        round(c,594,148,1202,597,16,0xff233f4b);
        textFit(c,Campaign.WORLDS[chronicleWorld],898,189,556,26,GOLD,true);
        for(int key=0;key<10;key++){
            int level=chronicleWorld*10+key+1;
            int stageId=Campaign.stage(level,2);
            boolean owned=storyProgress.hasMemory(stageId);
            int row=key/2,col=key%2;
            float x=615+col*291,y=210+row*70;
            round(c,x,y,x+273,y+62,9,owned?0xff426a64:0xff354653);
            text(c,owned?"MEMORY "+(key+1)+" • OPEN":"MEMORY "+(key+1)+" • LOCKED",
                    x+19,y+28,19,owned?INK:0xffadbaba,false);
            text(c,owned?"RECOVERED":"REPLAY LEVEL "+level+"-C",
                    x+19,y+51,14,owned?0xffb5e9c7:0xffd1b6a2,false);
        }
        button(c,76,610,298,673,"BACK",0xff43606e);
        button(c,312,610,795,673,"RENAME WARDEN",0xff705b54);
        button(c,814,610,1202,673,"REPLAY PROLOGUE",0xff59567a);
    }
    private void tapChronicle(float x,float y){
        if(y>=610&&y<=680){
            if(x<303)scene=Scene.HOME;
            else if(x<805)editWardenName();
            else openStory(StoryScript.prologue(storyProgress.wardenName()));
            return;
        }
        if(x>=72&&x<=572&&y>=149&&y<599){
            int selected=(int)((y-149)/45);
            if(selected>=0&&selected<10)chronicleWorld=selected;
            return;
        }
        if(x>=615&&x<=1189&&y>=210&&y<560){
            int col=x>=906?1:0,row=(int)((y-210)/70);
            if(row<0||row>=5)return;
            int level=chronicleWorld*10+row*2+col+1;
            int stageId=Campaign.stage(level,2);
            if(storyProgress.hasMemory(stageId))
                openStory(StoryScript.memory(KeyMemories.forStage(stageId)));
        }
    }
    private void editWardenName(){
        final EditText field=new EditText(getContext());
        field.setSingleLine(true);
        field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        field.setText(storyProgress.wardenName());
        new AlertDialog.Builder(getContext()).setTitle("Name your Warden")
                .setMessage("Your story identity stays the same. You can rename anytime.")
                .setView(field)
                .setPositiveButton("SAVE",(dialog,which)->{
                    storyProgress.setWardenName(field.getText().toString());
                    invalidate();
                })
                .setNegativeButton("CANCEL",null).show();
    }

    private void drawPause(Canvas c) {
        rect(c,0,0,W,H,0xc3001117);
        round(c,390,166,890,582,27,0xff1f3741);
        text(c,"PAUSED",640,282,66,GOLD,true);
        button(c,456,332,825,414,"RESUME BATTLE",0xffae7652);
        button(c,456,439,825,517,"RETURN TO MAP",0xff42616b);
        button(c,456,526,825,581,"SAVE FORMATION "+(selectedLoadout+1),0xff766347);
    }
    /**
     * Fixed, accessible upgrade navigation across Armory, Skills, Fortress,
     * Weapon Mastery and Ascension. Rectangles match UpgradeActionLayout.hit().
     */
    private void upgradeActions(Canvas c,String a,String b,String d,String action,
                                boolean enabled){
        round(c,55,614,1224,702,16,0xf0142d38);
        String[] names={a,b,d,action};
        for(int i=0;i<4;i++){
            int color=i==3?(enabled?0xffac7950:0xff52626b):
                    i==0?0xff41636e:i==1?0xff4a6772:0xff566f70;
            button(c,UpgradeActionLayout.LEFT[i],UpgradeActionLayout.TOP,
                    UpgradeActionLayout.RIGHT[i],UpgradeActionLayout.BOTTOM,
                    names[i],color);
        }
    }
    private void textFit(Canvas c,String value,float x,float y,float maxWidth,
                         float size,int color,boolean centered){
        p.setTypeface(FONT);p.setTextSize(size);
        while(size>13&&p.measureText(value)>maxWidth){
            size-=1;p.setTextSize(size);
        }
        text(c,value,x,y,size,color,centered);
    }
    private void button(Canvas c,float l,float t,float r,float b,String label,int color){
        round(c,l,t+5,r,b+5,14,0x88000000);
        round(c,l,t,r,b,14,color);
        round(c,l+3,t+3,r-3,t+6,3,0x66e8f5ea);
        p.setTypeface(FONT);
        float size=b-t<49?21f:26f;
        p.setTextSize(size);
        while(size>15f&&p.measureText(label)>r-l-26f){
            size-=1f;p.setTextSize(size);
        }
        text(c,label,(l+r)/2,(t+b)/2+size*.34f,size,0xfff8f5e9,true);
    }

    @Override public boolean onTouchEvent(MotionEvent ev) {
        int action=ev.getActionMasked(),index=ev.getActionIndex(),id=ev.getPointerId(index);
        float scale=Math.min(getWidth()/W,getHeight()/H);
        if(scale<=0)return true;
        float offsetX=(getWidth()-W*scale)*.5f,offsetY=(getHeight()-H*scale)*.5f;
        float x=(ev.getX(index)-offsetX)/scale,y=(ev.getY(index)-offsetY)/scale;
        if(storyOpen){
            if(action==MotionEvent.ACTION_DOWN)advanceStoryLine();
            invalidate();return true;
        }
        if(scene==Scene.CHRONICLE){
            if(action==MotionEvent.ACTION_DOWN)tapChronicle(x,y);
            invalidate();return true;
        }
        if(scene==Scene.WEAPON_TREE){
            if(action==MotionEvent.ACTION_DOWN){
                weaponTouchActive=true;weaponTreeDragging=false;
                weaponTouchStartX=x;weaponTouchStartY=y;weaponLastY=y;
            }else if(action==MotionEvent.ACTION_MOVE&&weaponTouchActive&&
                    weaponTouchStartX>=WM_LEFT&&weaponTouchStartX<=WM_RIGHT&&
                    weaponTouchStartY>=WM_TOP&&weaponTouchStartY<=WM_BOTTOM){
                float delta=y-weaponLastY;
                if(Math.abs(y-weaponTouchStartY)>8)weaponTreeDragging=true;
                if(weaponTreeDragging)
                    weaponTreeScroll=BattleRules.clamp(weaponTreeScroll+delta,
                            0,WM_SCROLL_MAX);
                weaponLastY=y;
            }else if(action==MotionEvent.ACTION_UP){
                // Never treat the RELEASE of the Armory -> Tree button
                // as a second, unrelated click inside the new screen.
                if(weaponTouchActive&&!weaponTreeDragging)tapWeaponTree(x,y);
                weaponTouchActive=false;weaponTreeDragging=false;
            }else if(action==MotionEvent.ACTION_CANCEL){
                weaponTouchActive=false;weaponTreeDragging=false;
            }
            invalidate();return true;
        }
        if(scene==Scene.ASCENSION){
            if(action==MotionEvent.ACTION_DOWN){
                treeTouchActive=true;
                treeDownX=x;treeDownY=y;treeLastY=y;treeDragging=false;
            }else if(action==MotionEvent.ACTION_MOVE&&treeTouchActive&&
                    treeDownX>=TREE_LEFT&&treeDownX<=TREE_RIGHT&&
                    treeDownY>=TREE_TOP&&treeDownY<=TREE_BOTTOM){
                float delta=y-treeLastY;
                if(Math.abs(y-treeDownY)>9)treeDragging=true;
                if(treeDragging)
                    ascensionScroll=BattleRules.clamp(ascensionScroll+delta,0,
                            AscensionTree.worldHeight()-(TREE_BOTTOM-TREE_TOP));
                treeLastY=y;
            }else if(action==MotionEvent.ACTION_UP){
                // A touch begun in Fortress may have opened this scene on DOWN.
                // Its corresponding UP must never act as an unrelated Tree tap.
                if(treeTouchActive&&!treeDragging)tapAscension(x,y);
                treeDragging=false;treeTouchActive=false;
            }
            invalidate();return true;
        }
        if(scene==Scene.PLAY && briefingOpen) {
            if(action==MotionEvent.ACTION_DOWN && x>680 && x<1150 && y>460 && y<512){
                selectedLoadout=(selectedLoadout+1)%DefenseLoadouts.COUNT;
                prefs.edit().putInt("fort.v065.active",selectedLoadout).commit();
                start(stage);briefingOpen=true;
            } else if(action==MotionEvent.ACTION_DOWN && x>680 && x<1150 && y>570 && y<670) {
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
                else if(x>450 && x<830 && y>525 && y<589){
                    defenseLoadouts.save(selectedLoadout,towers,towerTypes,activeTowerCount);
                    announce("FORMATION "+(selectedLoadout+1)+" SAVED");
                    paused=false;clearTouchControls();
                }
            }else if(x>1138 && y<112) {paused=true;clearTouchControls();}
            else if(waiting && x>452 && x<828 && y>216 && y<270)
                advanceWave(true);
            else if(distance(x,y,stickHudX(),568)<150) {joyPointer=id;updateStick(x,y);}
            else if(distance(x,y,attackHudX(),560)<82) {
                attackPointer=id;firing=true;bufferedAttackTime=.12f;fire();
            }
            else if(distance(x,y,pulseHudX(),593)<62) pulse();
            else if(distance(x,y,buildHudX(),589)<65) build();
            else if(distance(x,y,dodgeHudX(),590)<54) triggerDodge();
            else selectTowerPadAt(x,y);
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
    /**
     * Convert physical battle screen taps back to the same world camera used
     * by drawBattle. Only *actual* active build sites are selectable.
     * Tapping one only selects it; the visible BUILD button confirms spending.
     */
    private void selectTowerPadAt(float screenX,float screenY){
        float worldX=(screenX-W*.5f)/BATTLE_ZOOM+LaneRules.VIEW_CENTER_X;
        float worldY=(screenY-H*.5f)/BATTLE_ZOOM+LaneRules.VIEW_CENTER_Y;
        for(int slot=0;slot<activeTowerCount;slot++){
            if(distance(worldX,worldY,towerX[slot],towerY[slot])<59f){
                selectedTowerPad=slot;
                announce("PAD "+(slot+1)+" SELECTED • TAP BUILD");
                return;
            }
        }
        for(Enemy e:enemies)if(e.hp>0&&distance(worldX,worldY-20,e.x,e.y-20)<66){
            focusedEnemy=e;selectedTowerPad=-1;
            announce(e.boss?"FOCUS FIRE • BOSS":"FOCUS FIRE • MARKED ENEMY");
            return;
        }
        if(focusedEnemy!=null){focusedEnemy=null;announce("FOCUS FIRE CLEARED");}
    }
    /** Dash input is responsive but cannot be chained or used while paused. */
    private void triggerDodge(){
        if(scene!=Scene.PLAY||paused||briefingOpen||storyOpen||
                !DodgeRules.canStart(dodgeTime,dodgeCooldown))return;
        float dx=DodgeRules.normalized(axisX,axisY,true,heroDir);
        float dy=DodgeRules.normalized(axisX,axisY,false,heroDir);
        if(!heroAnimation.dodge())return;
        dodgeDirX=dx;dodgeDirY=dy;
        dodgeTime=DodgeRules.DURATION;
        dodgeCooldown=DodgeRules.COOLDOWN;
        audio.play(GameAudio.DODGE);
        // Drop pending strike contact so dodging cannot deal invisible damage.
        pendingMelee=false;pendingMeleeWeapon=-1;
        bufferedAttackTime=0;
        puff(heroX,heroY,6,0xffa6dfe0);
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
            else if(x>93 && x<345 && y>408 && y<492)scene=Scene.MAP;
            else if(x>=345 && x<610 && y>408 && y<492)scene=Scene.CHRONICLE;
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
            }else if(x>=77 && x<=330 && y>=598 && y<=666) scene=Scene.HOME;
        }else if(scene==Scene.ARMORY) {
            int action=UpgradeActionLayout.hit(x,y);
            if(action==0)scene=Scene.HOME;
            else if(action==1)scene=Scene.FORTRESS;
            else if(action==2)scene=Scene.SKILLS;
            else if(action==3){
                selectedMasteryWeapon=equippedWeapon;
                selectedWeaponMasteryNode=1;
                weaponTreeScroll=WM_SCROLL_MAX;
                weaponTouchActive=false;scene=Scene.WEAPON_TREE;
            } else if(y>=151 && y<570) {
                int col=(int)((x-79)/375),row=(int)((y-155)/218);
                if(col>=0 && col<3 && row>=0 && row<2) {
                    float localX=(x-79)%375,localY=(y-155)%218;
                    if(localX>=0 && localX<343 && localY>=0 && localY<197)
                        equipWeapon(row*3+col);
                }
            }
        }else if(scene==Scene.SKILLS) {
            int action=UpgradeActionLayout.hit(x,y);
            if(action==0)scene=Scene.HOME;
            else if(action==1)scene=Scene.ARMORY;
            else if(action==2)scene=Scene.FORTRESS;
            else if(action==3){
                selectedMasteryWeapon=equippedWeapon;
                selectedWeaponMasteryNode=1;
                weaponTreeScroll=WM_SCROLL_MAX;
                weaponTouchActive=false;scene=Scene.WEAPON_TREE;
            }else if(y>=151 && y<570){
                int col=(int)((x-79)/375),row=(int)((y-155)/218);
                if(col>=0 && col<3 && row>=0 && row<2) {
                    float localX=(x-79)%375,localY=(y-155)%218;
                    if(localX>=0 && localX<343 && localY>=0 && localY<197)
                        equipSkill(row*3+col);
                }
            }
        }else if(scene==Scene.FORTRESS) {
            if(x>=439&&x<=661&&y>=504&&y<=546 &&
                    (fortressRank<Progression.MAX_RANK||arsenalTier()<4)){
                previewNextFortressLook=!previewNextFortressLook;
                return;
            }
            int action=UpgradeActionLayout.hit(x,y);
            if(action==0)scene=Scene.HOME;
            else if(action==1)scene=Scene.ARMORY;
            else if(action==2)scene=Scene.ASCENSION;
            else if(action==3)upgradeFortress();
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
