package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Skeletal, code-drawn monsters. Replaces the cropped-painted-picture enemies.
 *
 * 7 species (same ids the game already uses):
 *   0 goblin runner   1 goblin slinger   2 forest brute   3 flying imp
 *   4 totem shaman    5 thornhide beast (elite / boss)     6 wild boar
 *
 * 10 biome themes (index = Campaign.world(stage)). Each map re-skins every
 * species: palette, headgear, body growths, glow veins and ambient particles,
 * so a Frostpeak goblin is not an Ember goblin with a different tint.
 *
 * Every limb is its own bone: IK legs with planted/swinging feet, hunched
 * torso lean, breathing, ear/tail secondary motion, and one distinct attack
 * per species (stab, sling whirl, club smash, orb throw, totem cast, ground
 * slam, tusk gore). No bitmap atlas required.
 *
 * Units match the old sprites (grounded at (0,0), ~68 tall, boss ~125) so the
 * game's health bars, shadows and hitboxes stay valid. The drawing is
 * allocation-free per frame (one reused Path / Paint) for low-end phones.
 *
 * @param time  Enemy.phase from the game (advances ~7 rad/s walking, 1.3 idle)
 * @param faceX horizontal heading (-1 left .. +1 right); sprites mirror to it
 */
public final class EnemyRig {
    public static final int THEMES = 10;
    private static final int OUT = 0xff0b0f16;

    private static final class Theme {
        final int skin, skinD, armor, armorD, accent, eye, glow, bone;
        Theme(int skin, int skinD, int armor, int armorD, int accent, int eye, int glow, int bone) {
            this.skin = skin; this.skinD = skinD; this.armor = armor; this.armorD = armorD;
            this.accent = accent; this.eye = eye; this.glow = glow; this.bone = bone;
        }
    }

    // Index == Campaign.world(stage)
    private static final Theme[] T = {
        // 0 Greenhaven Frontier: classic green goblins, leather, bronze
        new Theme(0xff72a64c, 0xff4b7c36, 0xff80603c, 0xff4f3b27, 0xffd9b25a, 0xffffe66a, 0xff9be85f, 0xffe8dfc4),
        // 1 Whispering Woods: moss, bark, leaf crowns, antlers
        new Theme(0xff4f8050, 0xff325a3a, 0xff5e4834, 0xff3a2c20, 0xff9ad65a, 0xffd9ff7a, 0xff7fe08a, 0xffcfc6a4),
        // 2 Venom Marsh: sickly yellow-green, toadstools, slime
        new Theme(0xffa0ad3c, 0xff6e7e28, 0xff4f5d3c, 0xff2f3a28, 0xffb36ad8, 0xffffee55, 0xffa8e83c, 0xffd8d2a8),
        // 3 Ember Canyon: charred red skin, lava cracks, big horns
        new Theme(0xffb9533b, 0xff7d3025, 0xff3d2c29, 0xff231918, 0xffff8a2a, 0xffffd24a, 0xffff6a1f, 0xffe0c9a8),
        // 4 Frostpeak Mountains: pale ice skin, fur, crystal shards
        new Theme(0xff8fb9d8, 0xff5f89aa, 0xffdbe8f3, 0xff8fa8be, 0xff9be8ff, 0xffe0f8ff, 0xff7fd8ff, 0xffeaf6ff),
        // 5 Ironfall Ruins: stone-grey, riveted iron plates
        new Theme(0xff8b9087, 0xff5e635d, 0xff6f7a88, 0xff3b434d, 0xffc98a3f, 0xffff9a55, 0xffffb06a, 0xffc9c2b0),
        // 6 Crystal Depths: violet skin, glowing crystal growths
        new Theme(0xff6b64b9, 0xff453f88, 0xff3f3b70, 0xff26234a, 0xff6ff0ff, 0xffa8fff4, 0xff7bf0ff, 0xffd6d0f0),
        // 7 Stormbound Isles: teal skin, shell armor, lightning
        new Theme(0xff4f9b9b, 0xff2f6b71, 0xff406b8b, 0xff25405a, 0xfff0f4ff, 0xfff6ff8a, 0xff9ad0ff, 0xffe6e2d2),
        // 8 Shadowfall Kingdom: near-black, violet wisps, bone spikes
        new Theme(0xff3b3652, 0xff211e34, 0xff2b2539, 0xff161320, 0xffb36bff, 0xffd35bff, 0xff8a4fe0, 0xffdad3c1),
        // 9 Eclipse Citadel: black and gold, halo, corona
        new Theme(0xff2c2c3b, 0xff16161f, 0xff1c1c27, 0xff0d0d14, 0xffffd24a, 0xfffff2b0, 0xffffc84a, 0xfff1e6c0),
    };

    private static final class Spec {
        final float hipY, thigh, shin, hipW, foot, torsoH, shW, waistW, neck, headR,
                armU, armL, stride, lift, hunch;
        final boolean robe;
        Spec(float hipY, float thigh, float shin, float hipW, float foot, float torsoH,
             float shW, float waistW, float neck, float headR, float armU, float armL,
             float stride, float lift, float hunch, boolean robe) {
            this.hipY = hipY; this.thigh = thigh; this.shin = shin; this.hipW = hipW;
            this.foot = foot; this.torsoH = torsoH; this.shW = shW; this.waistW = waistW;
            this.neck = neck; this.headR = headR; this.armU = armU; this.armL = armL;
            this.stride = stride; this.lift = lift; this.hunch = hunch; this.robe = robe;
        }
    }
    private static final Spec[] SPEC = new Spec[7];
    static {
        //                 hipY  thigh shin hipW foot torsoH shW waistW neck headR armU armL stride lift hunch robe
        SPEC[0] = new Spec(-27f, 13f, 13f, 4f, 6f, 19f, 9f, 6.5f, 2f, 9.5f, 10f, 10f, 11f, 5f, 10f, false);
        SPEC[1] = new Spec(-27f, 13f, 13f, 4f, 6f, 19f, 9f, 6.5f, 2f, 9.5f, 10f, 10f, 9f, 4f, 7f, false);
        SPEC[2] = new Spec(-36f, 17f, 17f, 8f, 9f, 30f, 17f, 13f, 2f, 11.5f, 19f, 19f, 10f, 4f, 14f, false);
        SPEC[4] = new Spec(-28f, 14f, 14f, 4.5f, 6f, 22f, 9.5f, 8f, 2f, 9f, 10f, 10f, 7f, 3f, 6f, true);
        SPEC[5] = new Spec(-52f, 26f, 26f, 11f, 12f, 44f, 25f, 19f, 3f, 14f, 28f, 27f, 12f, 5f, 10f, false);
    }
    private static final float[] KIND_SCALE = {1f, .88f, 1f, 1f, 1f, 1f, 1f};
    private static final float[] VAR_SCALE = {.95f, .995f, 1.04f, 1.085f};

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final float[] ik = new float[4];
    private Canvas cv;
    private Theme t;
    private int themeIdx;
    private int variant;
    private boolean boss;
    private int bossPhase;
    private float hurtMix;
    // pose outputs filled by the species pose functions
    private float poseLean, poseBob, poseShiftX, poseShiftY;
    private float handFX, handFY, handBX, handBY, weaponAngle, fx; // fx: effect progress

    /** @return false when kind/inputs are invalid so the caller can fall back. */
    public boolean enemy(Canvas c, int kind, int biome, int variant, float x, float y,
                         float time, boolean moving, float attack, boolean hurt, float faceX) {
        return enemy(c,kind,biome,variant,x,y,time,moving,attack,hurt,faceX,false,1);
    }

    /** Bosses derive visible phase details from real BossTactics, never UI preview.
     * Variant upgrades now change equipment silhouette, not only character scale.
     */
    public boolean enemy(Canvas c, int kind, int biome, int variant, float x, float y,
                         float time, boolean moving, float attack, boolean hurt,
                         float faceX,boolean boss,int bossPhase) {
        if (kind < 0 || kind > 6 || !Float.isFinite(time) || !Float.isFinite(x)
                || !Float.isFinite(y)) return false;
        cv = c;
        themeIdx = Math.floorMod(biome, THEMES);
        t = T[themeIdx];
        this.variant = Math.floorMod(variant, 4);
        this.boss=boss;
        this.bossPhase=Math.max(1,Math.min(3,bossPhase));
        hurtMix = hurt ? (((int) (time * 3f)) % 2 == 0 ? .5f : .22f) : 0f;
        float sc = KIND_SCALE[kind] * VAR_SCALE[this.variant];
        float w = kind == 5 ? 58 : kind == 2 ? 40 : kind == 6 ? 44 : 28;

        c.save();
        c.translate(x, y);
        if (faceX < 0f) c.scale(-1, 1);
        // contact shadow (not scaled by the sprite height)
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x55000000);
        c.drawOval(-w * sc, -5, w * sc, 7, p);
        c.scale(sc, sc);

        switch (kind) {
            case 3: imp(time, moving, attack); break;
            case 6: boar(time, moving, attack); break;
            default: humanoid(kind, time, moving, attack); break;
        }
        // Accessories are attached to the animated actor and belong to the
        // same world-depth layer, never floating overlay sprites.
        drawVariantEquipment(kind,time);
        if(this.boss)drawBossPhase(kind,time);
        c.restore();
        return true;
    }

    /** Body-attached upgrades give each seeded variant a distinct outline.
     * Keep them small enough not to cover hit reactions or combat telegraphs.
     */
    private void drawVariantEquipment(int kind,float time){
        if(variant<=0)return;
        float upper=kind==5?-91:kind==2?-72:kind==6?-52:-58;
        float shoulder=kind==5?28:kind==2?23:kind==6?28:15;
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(tint(t.accent));
        p.setStrokeWidth(variant>=3?2.7f:1.65f);
        if(kind==6){
            // Leather-backed boar harness, reinforced tusk bands.
            cv.drawArc(-29,upper+7,29,upper+32,190,160,false,p);
            if(variant>=2)cv.drawLine(25,upper+25,37,upper+18,p);
        }else if(kind==3){
            // Animated wing-bone ornament; imp silhouette stays winged.
            cv.drawLine(-24,upper+6,-38,upper-10,p);
            if(variant>=2)cv.drawLine(24,upper+6,39,upper-11,p);
        }else{
            cv.drawArc(-shoulder-7,upper-6,-shoulder+8,upper+9,
                    185,150,false,p);
            if(variant>=2)cv.drawArc(shoulder-8,upper-6,
                    shoulder+7,upper+9,208,150,false,p);
            if(variant>=3){
                float sway=(float)Math.sin(time*3.4f)*2f;
                path.reset();path.moveTo(-shoulder-5,upper);
                path.lineTo(-shoulder-10+sway,upper-15);
                path.lineTo(-shoulder,upper-6);path.close();
                p.setStyle(Paint.Style.FILL);cv.drawPath(path,p);
            }
        }
        p.setStyle(Paint.Style.FILL);
    }

    /** Distinct boss-stage anatomy, driven exclusively by real combat phase. */
    private void drawBossPhase(int kind,float time){
        float top=kind==5?-114f:kind==2?-89f:-79f;
        float width=kind==5?35f:kind==2?27f:20f;
        float pulse=.65f+.25f*(float)Math.sin(time*4f);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2f);
        p.setColor(withAlpha(t.glow,(int)(180*pulse)));
        cv.drawArc(-width,top-16,width,top+15,200,140,false,p);
        if(bossPhase>=2){
            horn(-width*.75f,top+3,-.32f,14f,t.bone);
            horn(width*.75f,top+3,.32f,14f,t.bone);
        }
        if(bossPhase>=3){
            // Phase three activates separate shoulder shard shapes.
            shard(-width,top+18,-.6f,14f,3.5f);
            shard(width,top+18,.6f,14f,3.5f);
            p.setStyle(Paint.Style.FILL);
            p.setColor(withAlpha(t.glow,210));
            cv.drawCircle(0,top+29,4f+pulse*2f,p);
        }
        p.setAlpha(255);p.setStyle(Paint.Style.FILL);
    }

    // =====================================================================
    // Humanoid species: runner, slinger, brute, shaman, beast
    // =====================================================================
    private void humanoid(int kind, float time, boolean moving, float a) {
        Spec s = SPEC[kind];
        float phase = time * (kind == 2 || kind == 5 ? .8f : 1.15f);
        float breath = (float) Math.sin(time * 1.9f);
        pose(kind, s, phase, breath, moving, a);

        float hipY = s.hipY + (moving ? -Math.abs((float) Math.sin(phase)) * s.stride * .22f : breath * .6f)
                + poseShiftY * .7f;
        float shY = hipY - s.torsoH;

        // 1) far (back) arm sits behind the torso
        float sBx = -s.shW * .35f, sFx = s.shW * .35f;
        cv.save();
        cv.translate(poseShiftX, poseBob);
        cv.rotate(poseLean, 0, hipY);

        // ambient glow behind the body for the high-tier themes
        backGrowth(kind, s, shY, hipY);
        limb(sBx, shY + 3, handBX, handBY, s.armU, s.armL, 5.5f * armW(kind), true);

        cv.restore();

        // 2) legs (outside the leaning group; feet stay on the ground)
        legs(kind, s, hipY, phase, moving, a);

        // 3) torso + head + near arm + weapon inside the leaning group
        cv.save();
        cv.translate(poseShiftX, poseBob);
        cv.rotate(poseLean, 0, hipY);
        torso(kind, s, hipY, shY, breath);
        float headX = s.shW * .3f + (float) Math.sin(Math.toRadians(poseLean)) * 0;
        float headY = shY - s.neck - s.headR * .7f;
        head(kind, s, headX, headY, time, a, breath);
        // weapon before the near arm so the hand overlaps the grip
        weapon(kind, s, handFX, handFY, time, a);
        limb(sFx, shY + 3, handFX, handFY, s.armU, s.armL, 6f * armW(kind), false);
        veins(kind, s, hipY, shY, time);
        cv.restore();

        effects(kind, s, time, a);
        aura(0, shY - s.headR, s.shW * 1.8f, s.torsoH * 1.4f, time);
    }

    private static float armW(int kind) { return kind == 5 ? 1.8f : kind == 2 ? 1.6f : 1f; }

    // ---- pose tables ---------------------------------------------------
    private void pose(int kind, Spec s, float phase, float breath, boolean moving, float a) {
        float swing = moving ? (float) Math.sin(phase) : 0f;
        float cs = moving ? (float) Math.cos(phase) : 0f;
        float shY = s.hipY - s.torsoH;
        float sFx = s.shW * .35f, sBx = -s.shW * .35f;
        poseLean = s.hunch + (moving ? swing * 1.6f : breath * .8f);
        poseBob = 0f; poseShiftX = 0f; poseShiftY = 0f; fx = -1f;
        // default hand rest (arms swing against the legs)
        handFX = sFx + s.armU * .55f - swing * s.armU * .45f;
        handFY = shY + s.armU * 1.15f + Math.abs(cs) * 1.5f;
        handBX = sBx + s.armU * .35f + swing * s.armU * .45f;
        handBY = shY + s.armU * 1.2f;
        weaponAngle = 24f;
        float t = a >= 0f ? clamp01(a) : -1f;
        switch (kind) {
            case 0: // runner: dagger stab
                if (t >= 0f) {
                    float wind = sin01(t / .38f), lunge = sin01((t - .32f) / .58f);
                    poseLean += -9f * wind + 14f * lunge;
                    poseShiftX = 11f * lunge - 3f * wind;
                    handFX = sFx + 4f + 20f * lunge - 7f * wind;
                    handFY = shY + 6f - 8f * lunge + 6f * wind;
                    handBX = sBx - 4f - 6f * lunge; handBY = shY + 14f;
                    weaponAngle = 62f * lunge - 15f * wind + 20f;
                    fx = t;
                } else { weaponAngle = 24f + swing * 8f; }
                break;
            case 1: // slinger: whirl a sling overhead, then hurl
                if (t >= 0f) {
                    float up = sin01(t / .85f);
                    float rel = t > .55f ? sin01((t - .55f) / .4f) : 0f;
                    handFX = sFx + 2f + 22f * rel;
                    handFY = shY - 8f - 14f * up + 8f * rel + 10f * (1f - up);
                    handBX = sBx + 5f; handBY = shY + 10f;
                    poseLean += -4f * up + 9f * rel;
                    poseShiftX = 4f * rel;
                    fx = t;
                } else { handFX = sFx + 8f - swing * 5f; handFY = shY + 14f; }
                break;
            case 2: // brute: two-handed club slam
                if (t >= 0f) {
                    float raise = t < .5f ? ease01(t / .5f) : 1f;
                    float slam = t > .5f ? ease01((t - .5f) / .25f) : 0f;
                    float rec = t > .78f ? ease01((t - .78f) / .22f) : 0f;
                    float down = slam * (1f - rec);
                    float hold = raise * (1f - down);
                    poseLean += -12f * hold + 22f * down;
                    poseShiftX = 12f * down;
                    poseShiftY = 8f * down - 4f * hold;
                    handFX = sFx + 2f + 22f * down - 4f * hold;
                    handFY = shY - 36f * hold + 20f * down + 8f * (1f - raise);
                    handBX = handFX - 6f; handBY = handFY + 8f;
                    weaponAngle = -38f * hold + 112f * down + 30f * (1f - raise);
                    fx = t;
                } else {
                    handFX = sFx + 12f - swing * 3f; handFY = shY + 8f;
                    handBX = handFX - 7f; handBY = handFY + 9f;
                    weaponAngle = 34f + swing * 4f;
                }
                break;
            case 4: // shaman: raise totem staff and cast
                if (t >= 0f) {
                    float up = sin01(t / .6f), hold = t < .88f ? 1f : 1f - (t - .88f) / .12f;
                    handFX = sFx + 10f; handFY = shY + 4f - 24f * up * hold;
                    handBX = sBx + 14f; handBY = shY - 10f * up * hold + 12f * (1f - up);
                    poseLean += -5f * up; poseShiftY = -3f * up;
                    fx = t;
                } else {
                    handFX = sFx + 9f; handFY = shY + 8f + breath * .8f;
                    handBX = sBx + 6f; handBY = shY + 14f;
                }
                weaponAngle = 3f;
                break;
            case 5: // beast: rear up, then pound the ground
                if (t >= 0f) {
                    float raise = t < .5f ? ease01(t / .5f) : 1f;
                    float slam = t > .5f ? ease01((t - .5f) / .22f) : 0f;
                    float rec = t > .78f ? ease01((t - .78f) / .22f) : 0f;
                    float down = slam * (1f - rec), hold = raise * (1f - down);
                    poseLean += -14f * hold + 26f * down;
                    poseShiftX = 14f * down; poseShiftY = 10f * down - 5f * hold;
                    handFX = sFx + 6f + 30f * down; handFY = shY - 34f * hold + 66f * down + 10f * (1f - raise);
                    handBX = sBx + 4f + 24f * down; handBY = handFY + 4f;
                    fx = t;
                } else {
                    handFX = sFx + 10f - swing * 7f; handFY = shY + s.armU * 1.5f;
                    handBX = sBx + 8f + swing * 7f; handBY = shY + s.armU * 1.5f;
                }
                break;
            default: break;
        }
    }

    // ---- legs ---------------------------------------------------------
    private void legs(int kind, Spec s, float hipY, float phase, boolean moving, float a) {
        float baseShift = poseShiftX * .7f;
        for (int k = 0; k < 2; k++) {
            int side = k == 0 ? -1 : 1; // far leg first
            float hx = baseShift + side * s.hipW * .6f;
            float angle = phase + (side == 1 ? (float) Math.PI : 0f);
            float sw = moving ? (float) Math.sin(angle) : 0f;
            float cs = moving ? (float) Math.cos(angle) : -1f;
            float fx2 = hx + sw * s.stride + (a >= 0 ? side * 2f + (side == 1 ? poseShiftX * .5f : 0) : 0);
            float lift = moving ? Math.max(0f, cs) * s.lift : 0f;
            float ax = fx2, ay = -lift - 3f;
            solve(hx, hipY, ax, ay, s.thigh, s.shin, 1f, 0f);
            int skin = side == -1 ? t.skinD : t.skin;
            int arm = side == -1 ? t.armorD : t.armor;
            float w = (kind == 5 ? 12.5f : kind == 2 ? 10.5f : 6.5f);
            if (s.robe && kind == 4) w = 5.5f;
            capsule(ik[0], ik[1], ik[2], ik[3], w * .88f, skin);
            capsule(hx, hipY, ik[0], ik[1], w, skin);
            // greave / boot
            float ex = ik[2], ey = ik[3];
            ball(ik[0], ik[1], w * .52f, arm);
            oval(ex - s.foot * .45f, ey - 2f, ex + s.foot * .85f, ey + 4f, arm, true);
            if (kind != 4) { // claws
                float cx = ex + s.foot * .85f;
                mv(cx - 1f, ey - 1f); ln(cx + 3.5f, ey + 1.5f); ln(cx - 1f, ey + 3f); done(t.bone, false);
            }
        }
    }

    // ---- torso --------------------------------------------------------
    private void torso(int kind, Spec s, float hipY, float shY, float breath) {
        float b = breath * .5f;
        // belly / back silhouette (skin)
        mv(-s.waistW, hipY + 1);
        ln(s.waistW, hipY + 1);
        ln(s.shW, shY + b);
        qd(0, shY - 4 + b, -s.shW, shY + b);
        done(t.skin, true);
        // darker lower belly shade
        mv(-s.waistW + 1, hipY); ln(s.waistW - 1, hipY);
        ln(s.waistW * .8f, hipY - s.torsoH * .35f); ln(-s.waistW * .8f, hipY - s.torsoH * .35f);
        done(t.skinD, false);
        if (s.robe) { // shaman robe over legs + shoulders
            mv(-s.shW - 1, shY + 2 + b); ln(s.shW + 1, shY + 2 + b);
            ln(s.waistW + 7, hipY + 10); qd(0, hipY + 16 + (float) Math.sin(poseLean) * 2, -s.waistW - 7, hipY + 10);
            done(t.armor, true);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2f); p.setColor(tint(t.accent));
            cv.drawLine(-s.waistW - 5, hipY + 9, s.waistW + 5, hipY + 9, p);
            cv.drawLine(0, shY + 4 + b, 0, hipY + 14, p);
        } else {
            // chest armor + belt (kind-specific heft)
            float armH = kind == 0 || kind == 1 ? .55f : .75f;
            mv(-s.shW * .9f, shY + 1.5f + b); ln(s.shW * .9f, shY + 1.5f + b);
            ln(s.waistW * .85f, shY + s.torsoH * armH); ln(-s.waistW * .85f, shY + s.torsoH * armH);
            done(t.armor, true);
            p.setStyle(Paint.Style.FILL);
            p.setColor(tint(t.armorD));
            cv.drawRect(-s.waistW - 1, hipY - 3.5f, s.waistW + 1, hipY + 1, p);
            ball(0, hipY - 1.3f, kind == 5 ? 4.5f : 2.6f, t.accent);
            // shoulder pads
            if (kind >= 2) {
                ball(-s.shW * .75f, shY + 3, s.shW * .32f, t.armorD);
                ball(s.shW * .75f, shY + 3, s.shW * .36f, t.armor);
            }
        }
        frontGrowth(kind, s, shY, hipY, b);
    }

    // ---- head ---------------------------------------------------------
    private void head(int kind, Spec s, float hx, float hy, float time, float a, float breath) {
        float r = s.headR;
        boolean hooded = kind == 1 || kind == 4;
        float wig = (float) Math.sin(time * 3.2f + kind) * 2.2f;
        // ears (secondary motion): goblins have long ears, others small
        if (kind == 0 || kind == 1) {
            for (int e = -1; e <= 1; e += 2) {
                float ex = hx + e * r * .8f, ey = hy - r * .1f;
                mv(ex, ey - 3); ln(ex + e * (r * 1.4f), ey - 5 + wig * e * .6f - (e > 0 ? 0 : 0));
                ln(ex, ey + 3); done(t.skin, true);
            }
        }
        if (kind != 4) { // face base
            oval(hx - r, hy - r * .95f, hx + r, hy + r * .9f, t.skin, true);
        }
        if (hooded) {
            // hood / mask silhouette
            mv(hx - r * 1.15f, hy + r * .7f);
            qd(hx - r * 1.35f, hy - r * 1.4f, hx + r * .1f, hy - r * 1.5f - (kind == 4 ? 4 : 0));
            qd(hx + r * 1.4f, hy - r * 1.2f, hx + r * 1.1f, hy + r * .7f);
            ln(hx + r * .5f, hy + r * .1f); ln(hx - r * .6f, hy + r * .1f);
            done(kind == 4 ? t.armor : t.armorD, true);
        }
        if (kind == 4) { // totem mask: bone plate with glowing eye slits
            oval(hx - r * .9f, hy - r * .9f, hx + r * .9f, hy + r * .95f, t.bone, true);
            p.setStyle(Paint.Style.FILL); p.setColor(tint(t.accent));
            cv.drawRect(hx - r * .75f, hy - r * .1f, hx + r * .75f, hy + r * .12f, p);
            cv.drawRect(hx - 1, hy - r * .85f, hx + 1, hy + r * .85f, p);
        }
        // eyes: glowing, angry brow; pupils narrow
        float ex = hx + r * .35f, ey = hy - r * .12f;
        boolean hurtEye = hurtMix > 0f;
        for (int e = 0; e < (kind == 5 || kind == 2 ? 2 : 2); e++) {
            float exx = hx + (e == 0 ? -r * .28f : r * .5f), eyy = ey;
            if (kind == 4) { exx = hx + (e == 0 ? -r * .4f : r * .42f); eyy = hy - r * .02f; }
            p.setStyle(Paint.Style.FILL);
            p.setColor(tint(t.eye));
            cv.drawCircle(exx, eyy, r * (kind == 4 ? .2f : .24f), p);
            p.setColor(0xff10141c);
            cv.drawCircle(exx + r * .06f, eyy, r * .1f, p);
        }
        // brow
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(kind == 5 || kind == 2 ? 3f : 2f);
        p.setColor(tint(t.skinD));
        if (kind != 4) {
            cv.drawLine(hx - r * .6f, hy - r * .42f, hx + r * .05f, hy - r * .22f, p);
            cv.drawLine(hx + r * .1f, hy - r * .22f, hx + r * .85f, hy - r * .45f, p);
        }
        // mouth with fangs (opens wide when attacking)
        float open = a >= 0f ? 2.5f + 2.5f * sin01(a) : 1f;
        if (kind != 4) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xff2a0f12);
            cv.drawOval(hx - r * .35f, hy + r * .38f, hx + r * .7f, hy + r * .38f + open + 1.5f, p);
            p.setColor(tint(t.bone));
            float fy = hy + r * .38f;
            mv(hx - r * .22f, fy); ln(hx - r * .05f, fy); ln(hx - r * .14f, fy + 3.2f); done(t.bone, false);
            mv(hx + r * .38f, fy); ln(hx + r * .58f, fy); ln(hx + r * .5f, fy + 3.2f); done(t.bone, false);
            if (kind == 2 || kind == 5) { // tusks
                mv(hx - r * .5f, hy + r * .55f); ln(hx - r * .3f, hy + r * .5f);
                ln(hx - r * .62f, hy - r * .1f); done(t.bone, true);
                mv(hx + r * .62f, hy + r * .5f); ln(hx + r * .85f, hy + r * .5f);
                ln(hx + r * .85f, hy - r * .2f); done(t.bone, true);
            }
        }
        headgear(kind, hx, hy, r, time);
    }

    // ---- limbs and weapons --------------------------------------------
    private void limb(float sx, float sy, float tx, float ty, float up, float low,
                      float w, boolean back) {
        solve(sx, sy, tx, ty, up, low, -.4f, 1f);
        float ex = ik[0], ey = ik[1], hx = ik[2], hy = ik[3];
        int skin = back ? t.skinD : t.skin;
        capsule(sx, sy, ex, ey, w * 1.05f, skin);
        capsule(ex, ey, hx, hy, w * .95f, skin);
        // bracer
        float mx = (ex + hx) * .5f, my = (ey + hy) * .5f;
        capsule(mx, my, hx, hy, w * 1.12f, back ? t.armorD : t.armor);
        ball(hx, hy, w * .72f, skin);
        // claws
        float dx = hx - ex, dy = hy - ey, l = (float) Math.hypot(dx, dy);
        if (l > .1f) {
            dx /= l; dy /= l;
            mv(hx + dx * w * .5f - dy * w * .4f, hy + dy * w * .5f + dx * w * .4f);
            ln(hx + dx * w * 1.6f, hy + dy * w * 1.6f);
            ln(hx + dx * w * .5f + dy * w * .4f, hy + dy * w * .5f - dx * w * .4f);
            done(t.bone, false);
        }
    }

    private void weapon(int kind, Spec s, float hx, float hy, float time, float a) {
        cv.save();
        cv.translate(hx, hy);
        cv.rotate(weaponAngle);
        switch (kind) {
            case 0: { // dagger: steel blade, themed hilt
                p.setStyle(Paint.Style.FILL);
                p.setColor(tint(0xff3a2c22)); cv.drawRect(-1.6f, -2, 1.6f, 6, p);
                p.setColor(tint(t.accent)); cv.drawRect(-5, -4, 5, -2, p);
                mv(-3, -4); ln(3, -4); ln(0, -22); done(0xffc9d4dc, true);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1f); p.setColor(0xffffffff);
                cv.drawLine(0, -5, 0, -19, p);
                break;
            }
            case 1: { // sling: cord + pouch, whirling when attacking
                float ang = a >= 0f ? a * 1080f : (float) Math.sin(time * 2.4f) * 15f;
                cv.rotate(-weaponAngle + ang);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.6f); p.setColor(tint(0xff5a4430));
                float len = a >= 0f && a < .55f ? 17f : 13f;
                cv.drawLine(0, 0, 0, -len, p);
                ball(0, -len, 3.4f, t.armor);
                ball(0, -len, 1.8f, t.bone);
                break;
            }
            case 2: { // club: wooden haft, spiked head in the biome accent
                capsule(0, 6, 0, -22, 5f, 0xff5e4630);
                mv(-7, -18); ln(7, -18); ln(9, -36); qd(0, -41, -9, -36); done(0xff6e5236, true);
                spike(-8, -28, -13, -30); spike(8, -28, 13, -30); spike(-7, -22, -12, -20);
                spike(7, -22, 12, -20); spike(0, -40, 0, -46);
                break;
            }
            case 4: { // totem staff
                capsule(0, 12, 0, -40, 3.6f, 0xff5a4430);
                oval(-6, -53, 6, -38, t.bone, true);
                p.setStyle(Paint.Style.FILL); p.setColor(tint(t.accent));
                cv.drawRect(-4, -48, 4, -46, p);
                float pulse = .75f + .25f * (float) Math.sin(time * 6f);
                int gl = withAlpha(t.glow, a >= 0f ? 170 : 90);
                p.setColor(gl);
                cv.drawCircle(0, -58, (a >= 0f ? 11f + 6f * sin01(a) : 6f) * pulse, p);
                p.setColor(tint(t.glow)); cv.drawCircle(0, -58, 4.2f, p);
                p.setColor(0xffffffff); cv.drawCircle(-1, -59, 1.6f, p);
                break;
            }
            default: break;
        }
        cv.restore();
    }

    private void spike(float x0, float y0, float x1, float y1) {
        mv(x0, y0 - 2); ln(x1, y1); ln(x0, y0 + 2); done(t.bone, true);
    }

    private void effects(int kind, Spec s, float time, float a) {
        if (a < 0f) return;
        float g = a;
        if (kind == 0 && g > .3f && g < .85f) { // stab flash
            p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(withAlpha(0xffffffff, (int) (200 * sin01((g - .3f) / .55f))));
            p.setStrokeWidth(2.4f);
            float ex = s.shW + 22 + 10 * g;
            cv.drawLine(ex, -34, ex + 14, -38, p);
            cv.drawLine(ex, -30, ex + 16, -30, p);
        } else if (kind == 1 && g > .55f) { // stone in flight
            float f = (g - .55f) / .45f;
            ball(s.shW + 18 + f * 56, -38 + f * 8 - (float) Math.sin(Math.PI * f) * 10, 3.4f, t.bone);
        } else if ((kind == 2 || kind == 5) && g > .55f && g < 1f) { // ground shock ring
            float f = (g - .55f) / .45f;
            float cx = s.shW + (kind == 5 ? 40 : 28);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(kind == 5 ? 5f : 3.5f);
            p.setColor(withAlpha(t.glow, (int) (210 * (1f - f))));
            float rr = (kind == 5 ? 62f : 40f) * f + 6f;
            cv.drawOval(cx - rr, -rr * .28f - 2, cx + rr, rr * .28f - 2, p);
            for (int i = 0; i < 5; i++) { // dust chunks
                float ang = i * 1.25f + 1f, d = rr * .9f;
                p.setStyle(Paint.Style.FILL);
                p.setColor(withAlpha(t.armorD, (int) (200 * (1f - f))));
                cv.drawCircle(cx + (float) Math.cos(ang) * d, -4 - f * 14 - (float) Math.abs(Math.sin(ang)) * 6, 2.6f, p);
            }
        } else if (kind == 4) { // rune ring + bolt charge
            float f = sin01(g);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2f);
            p.setColor(withAlpha(t.glow, (int) (180 * f)));
            cv.drawOval(-26, -9, 26, 6, p);
            for (int i = 0; i < 6; i++) {
                float ang = time * 4f + i * 1.047f;
                p.setStyle(Paint.Style.FILL);
                cv.drawCircle((float) Math.cos(ang) * 22, -1.5f + (float) Math.sin(ang) * 6, 1.8f, p);
            }
        }
    }

    // =====================================================================
    // Flying imp
    // =====================================================================
    private void imp(float time, boolean moving, float a) {
        float flap = (float) Math.sin(time * 9f);
        float hover = -24f + (float) Math.sin(time * 3.2f) * 4f;
        float lean = moving ? 10f : 3f;
        float lunge = a >= 0f ? sin01(a) : 0f;
        cv.save();
        cv.translate(lunge * 4f, hover);
        cv.rotate(lean + (a >= 0 ? -8f * lunge : 0), 0, -34);
        // far wing
        wing(-1, flap, time);
        // tail with spade tip
        float tw = (float) Math.sin(time * 4f) * 6f;
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(2.6f); p.setColor(tint(t.skinD));
        path.reset(); path.moveTo(-6, -26); path.quadTo(-22, -16 + tw, -18, -4 + tw * .6f);
        cv.drawPath(path, p);
        mv(-18, -4 + tw * .6f); ln(-23, 0 + tw * .5f); ln(-14, 1 + tw * .5f); done(t.accent, true);
        // dangling legs
        for (int k = 0; k < 2; k++) {
            float sw = (float) Math.sin(time * 5f + k * 2f) * 3f;
            capsule(-3 + k * 6, -24, -4 + k * 7 + sw, -10, 4.2f, k == 0 ? t.skinD : t.skin);
            oval(-7 + k * 7 + sw, -12, -1 + k * 7 + sw + 4, -8, t.armor, true);
        }
        // torso
        oval(-10, -50, 10, -22, t.skin, true);
        oval(-8, -42, 8, -26, t.armor, true);
        ball(0, -34, 2.3f, t.accent);
        frontGrowthImp();
        // arms: pulled back, then hurl the orb
        float ax = 7 + 6 * lunge, ay = -40 - 4 * (1 - lunge);
        float handX = 12 + 14 * lunge - (a >= 0f && a < .5f ? 6f * sin01(a / .5f) : 0), handY = -34 - 8 * sin01(a >= 0f ? a : 0);
        solve(5, -44, handX, handY, 8f, 8f, -.3f, 1f);
        capsule(5, -44, ik[0], ik[1], 3.8f, t.skin); capsule(ik[0], ik[1], ik[2], ik[3], 3.4f, t.skin);
        ball(ik[2], ik[3], 2.8f, t.skin);
        if (a >= 0f) { // charging magic orb
            float size = (a < .6f ? 4f + 8f * sin01(a / .6f) : 12f * (1f - (a - .6f) / .4f) + 2f);
            float ox = a < .6f ? handX + 5 : handX + 5 + (a - .6f) * 90f;
            p.setStyle(Paint.Style.FILL);
            p.setColor(withAlpha(t.glow, 110)); cv.drawCircle(ox, handY - 2, size * 1.6f, p);
            p.setColor(withAlpha(t.glow, 220)); cv.drawCircle(ox, handY - 2, size, p);
            p.setColor(0xfff8ffff); cv.drawCircle(ox, handY - 2, size * .45f, p);
        }
        // head: big ears, horns, glowing eyes
        float hx = 3, hy = -58;
        for (int e = -1; e <= 1; e += 2) {
            mv(hx + e * 8, hy - 2); ln(hx + e * 20, hy - 8 + flap * 1.5f); ln(hx + e * 8, hy + 5); done(t.skin, true);
        }
        oval(hx - 10, hy - 10, hx + 10, hy + 9, t.skin, true);
        headgear(10, hx, hy, 9f, time);
        p.setStyle(Paint.Style.FILL); p.setColor(tint(t.eye));
        cv.drawCircle(hx - 3.5f, hy - 1, 2.6f, p); cv.drawCircle(hx + 5f, hy - 1, 2.6f, p);
        p.setColor(0xff10141c);
        cv.drawCircle(hx - 3f, hy - 1, 1.1f, p); cv.drawCircle(hx + 5.5f, hy - 1, 1.1f, p);
        p.setColor(0xff2a0f12);
        cv.drawOval(hx - 3, hy + 3, hx + 5, hy + 3 + 2.2f + 2.6f * lunge, p);
        mv(hx - 2, hy + 3); ln(hx - 1, hy + 3); ln(hx - 1.5f, hy + 6); done(t.bone, false);
        mv(hx + 3, hy + 3); ln(hx + 4, hy + 3); ln(hx + 3.5f, hy + 6); done(t.bone, false);
        // near wing on top
        wing(1, flap, time + .3f);
        cv.restore();
        aura(1, -48 + hover, 24, 36, time);
    }

    private void wing(int side, float flap, float time) {
        float f = flap * (side == 1 ? 1f : .85f);
        float tipX = side * (46 + f * 6f), tipY = -78 - f * 18f;
        float rootX = side * 5, rootY = -42;
        // membrane
        mv(rootX, rootY);
        qd(side * 28, -66 - f * 8f, tipX, tipY);
        qd(side * 40, -52 - f * 7f, side * (38 + f * 3f), -40 - f * 6f);
        qd(side * 28, -42 - f * 4f, side * 24, -34 - f * 2f);
        qd(side * 14, -38 - f * 2f, rootX, rootY + 4);
        done(side == 1 ? mixColor(t.armor, t.accent, .25f) : t.armorD, true);
        // finger bones
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(1.8f); p.setColor(tint(t.skinD));
        cv.drawLine(rootX, rootY, tipX, tipY, p);
        cv.drawLine(rootX, rootY, side * (38 + f * 3f), -40 - f * 6f, p);
        cv.drawLine(rootX, rootY, side * 24, -34 - f * 2f, p);
        p.setColor(withAlpha(t.glow, 160)); p.setStrokeWidth(1f);
        cv.drawLine(side * 14, -52 - f * 4f, tipX * .8f, tipY * .93f, p);
    }

    // =====================================================================
    // Wild boar (quadruped)
    // =====================================================================
    private void boar(float time, boolean moving, float a) {
        float phase = time * 1.35f;
        float breath = (float) Math.sin(time * 2f);
        float bob = moving ? -Math.abs((float) Math.sin(phase)) * 3.4f : breath * .8f;
        float dip = 0f, gore = 0f, shiftX = 0f;
        if (a >= 0f) {
            dip = a < .45f ? sin01(a / .45f) : 0f;
            gore = a > .4f ? sin01((a - .4f) / .6f) : 0f;
            shiftX = 12f * gore - 4f * dip;
        }
        float bodyY = -35f + bob + dip * 4f;
        cv.save();
        cv.translate(shiftX, 0);
        // legs: diagonal pairs (far legs darker)
        float[] lx = {-20f, 16f, -14f, 22f};
        for (int i = 0; i < 4; i++) {
            boolean front = i % 2 == 1;
            boolean far = i < 2;
            float pa = phase + (i == 0 || i == 3 ? 0f : (float) Math.PI);
            float sw = moving ? (float) Math.sin(pa) : 0f;
            float cs = moving ? (float) Math.cos(pa) : -1f;
            float hx = lx[i] + (far ? 0 : 3f), hy = bodyY + 8f;
            float fxx = hx + sw * 10f, fyy = -3f - (moving ? Math.max(0f, cs) * 7f : 0f);
            solve(hx, hy, fxx, fyy, 15f, 15f, front ? -1f : 1f, 0f);
            int c1 = far ? t.skinD : t.skin;
            capsule(ik[0], ik[1], ik[2], ik[3], 6.5f, c1);
            capsule(hx, hy, ik[0], ik[1], 8.5f, c1);
            oval(ik[2] - 3.5f, ik[3] - 2, ik[2] + 5.5f, ik[3] + 3.5f, t.bone, true);
        }
        // tail with tuft
        float tw = (float) Math.sin(time * 5f) * 4f;
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(2.4f); p.setColor(tint(t.skinD));
        path.reset(); path.moveTo(-30, bodyY - 4); path.quadTo(-40, bodyY - 14 + tw, -36, bodyY - 24 + tw);
        cv.drawPath(path, p);
        ball(-36, bodyY - 25 + tw, 2.6f, t.accent);
        // barrel body
        mv(-31, bodyY + 10);
        qd(-36, bodyY - 12, -22, bodyY - 20); // rump
        qd(0, bodyY - 28 + breath * .6f, 20, bodyY - 18); // back hump
        qd(32, bodyY - 6, 24, bodyY + 12);
        qd(0, bodyY + 18, -31, bodyY + 10);
        done(t.skin, true);
        // belly shade
        mv(-27, bodyY + 10); qd(0, bodyY + 17, 22, bodyY + 11); qd(0, bodyY + 7, -27, bodyY + 10); done(t.skinD, false);
        // mane ridge: theme spikes along the spine
        for (int i = 0; i < 7; i++) {
            float bx = -24 + i * 7.2f;
            float by = bodyY - 20 - (float) Math.sin((i + 1) * .45f) * 5f + breath * .4f;
            float h = 6.5f + (i % 2) * 2.5f + (float) Math.sin(time * 4f + i) * .8f;
            mv(bx - 3, by + 1); ln(bx + .5f, by - h); ln(bx + 3.5f, by + 1);
            done(i % 3 == 0 ? t.accent : t.armorD, true);
        }
        // armor saddle plate
        mv(-14, bodyY - 21); ln(10, bodyY - 20); ln(8, bodyY - 6); ln(-12, bodyY - 5); done(t.armor, true);
        ball(-2, bodyY - 12, 2.6f, t.accent);
        veinsBoar(bodyY, time);
        backGrowthBoar(bodyY, time);
        // head (lowers to gore, then tosses up)
        float hx = 31 + gore * 6f, hy = bodyY - 2f + dip * 12f - gore * 16f + (float) Math.sin(time * 2f) * .6f;
        cv.save();
        cv.rotate(-18f * gore + 14f * dip, hx - 6, hy);
        oval(hx - 12, hy - 11, hx + 12, hy + 10, t.skin, true);
        // snout
        oval(hx + 6, hy - 3, hx + 21, hy + 9, t.skinD, true);
        ball(hx + 17, hy + 2, 1.5f, 0xff10141c); ball(hx + 12, hy + 2, 1.5f, 0xff10141c);
        // tusks
        mv(hx + 7, hy + 8); qd(hx + 17, hy + 12, hx + 16, hy - 4); ln(hx + 13, hy + 4); done(t.bone, true);
        mv(hx - 3, hy + 8); qd(hx + 6, hy + 14, hx + 7, hy - 1); ln(hx + 3, hy + 5); done(t.bone, true);
        // ear + eye
        mv(hx - 6, hy - 9); ln(hx - 2, hy - 20); ln(hx + 4, hy - 10); done(t.skinD, true);
        p.setStyle(Paint.Style.FILL); p.setColor(tint(t.eye)); cv.drawCircle(hx + 3, hy - 3, 2.6f, p);
        p.setColor(0xff10141c); cv.drawCircle(hx + 3.6f, hy - 3, 1.1f, p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2f); p.setColor(tint(t.skinD));
        cv.drawLine(hx - 3, hy - 7, hx + 8, hy - 5, p);
        headgear(11, hx - 2, hy - 2, 11f, time);
        cv.restore();
        // dash streaks when sprinting
        if (moving) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.6f);
            p.setColor(withAlpha(t.glow, 90));
            for (int i = 0; i < 3; i++) cv.drawLine(-34 - i * 8, -14 - i * 9, -52 - i * 10, -14 - i * 9, p);
        }
        cv.restore();
        aura(2, bodyY - 10, 36, 30, time);
    }

    // =====================================================================
    // Biome theme decoration
    // =====================================================================
    /** Headgear/horns chosen by the map. style 10/11 = imp/boar variants. */
    private void headgear(int kind, float hx, float hy, float r, float time) {
        int theme = themeIndex();
        float top = hy - r * .85f;
        float sc = r / 9.5f;
        switch (theme) {
            case 0: // small bone horns + headband
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2.2f * sc); p.setColor(tint(0xffa8402e));
                cv.drawLine(hx - r * .95f, hy - r * .35f, hx + r * .95f, hy - r * .45f, p);
                horn(hx - r * .5f, top, -.55f, 6f * sc, t.bone);
                horn(hx + r * .55f, top, .5f, 6f * sc, t.bone);
                break;
            case 1: // antlers with leaves
                antler(hx - r * .35f, top, -1, sc, time);
                antler(hx + r * .45f, top, 1, sc, time);
                break;
            case 2: // toadstool cap + spots
                oval(hx - r * 1.05f, top - 5 * sc, hx + r * 1.05f, top + 3 * sc, t.accent, true);
                p.setStyle(Paint.Style.FILL); p.setColor(0xfff1e8f8);
                cv.drawCircle(hx - r * .5f, top - 1.5f * sc, 1.5f * sc, p);
                cv.drawCircle(hx + r * .3f, top - 3f * sc, 1.9f * sc, p);
                cv.drawCircle(hx + r * .75f, top - .5f * sc, 1.2f * sc, p);
                break;
            case 3: // big curved lava horns
                bigHorn(hx - r * .45f, top + 2, -1, sc, t.armorD, t.glow);
                bigHorn(hx + r * .5f, top + 2, 1, sc, t.armorD, t.glow);
                break;
            case 4: // ice shards
                shard(hx - r * .6f, top + 2, -.45f, 9f * sc, 3f * sc);
                shard(hx, top, 0f, 12f * sc, 3.6f * sc);
                shard(hx + r * .6f, top + 2, .45f, 9f * sc, 3f * sc);
                break;
            case 5: // iron cap helmet with rivets and a nose guard
                mv(hx - r * 1.05f, hy - r * .15f); qd(hx - r * 1.1f, top - 4 * sc, hx + r * .1f, top - 4.5f * sc);
                qd(hx + r * 1.1f, top - 4 * sc, hx + r * 1.05f, hy - r * .15f);
                ln(hx + r * .2f, hy - r * .3f); ln(hx + r * .2f, hy + r * .25f); ln(hx - r * .05f, hy + r * .25f);
                ln(hx - r * .1f, hy - r * .3f); done(t.armor, true);
                p.setStyle(Paint.Style.FILL); p.setColor(tint(t.accent));
                cv.drawCircle(hx - r * .65f, top + 1.5f * sc, 1.2f * sc, p);
                cv.drawCircle(hx + r * .8f, top + 1.5f * sc, 1.2f * sc, p);
                break;
            case 6: // glowing crystal cluster
                crystal(hx - r * .5f, top + 2, -.3f, 9f * sc, time);
                crystal(hx + r * .1f, top, 0f, 13f * sc, time + 1f);
                crystal(hx + r * .65f, top + 2, .35f, 8f * sc, time + 2f);
                break;
            case 7: // storm: shell cap + lightning fork
                oval(hx - r * 1.05f, top - 3 * sc, hx + r * 1.05f, hy - r * .1f, t.armor, true);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.4f); p.setColor(tint(t.armorD));
                cv.drawLine(hx, top - 3 * sc, hx, hy - r * .1f, p);
                cv.drawLine(hx - r * .5f, top - 2 * sc, hx - r * .6f, hy - r * .1f, p);
                cv.drawLine(hx + r * .5f, top - 2 * sc, hx + r * .6f, hy - r * .1f, p);
                bolt(hx + r * .3f, top - 3 * sc, 9f * sc, time);
                break;
            case 8: // bone spikes + wisp
                horn(hx - r * .6f, top + 2, -.8f, 10f * sc, t.bone);
                horn(hx + r * .1f, top, 0f, 12f * sc, t.bone);
                horn(hx + r * .7f, top + 2, .8f, 10f * sc, t.bone);
                break;
            default: // 9 eclipse: gold halo + dark crown
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2.2f * sc);
                p.setColor(withAlpha(t.glow, 230));
                cv.drawOval(hx - r * 1.05f, top - 8 * sc, hx + r * 1.05f, top - 1 * sc, p);
                p.setColor(withAlpha(t.glow, 70)); p.setStrokeWidth(5f * sc);
                cv.drawOval(hx - r * 1.05f, top - 8 * sc, hx + r * 1.05f, top - 1 * sc, p);
                horn(hx - r * .6f, top + 2, -.5f, 8f * sc, t.armor);
                horn(hx + r * .6f, top + 2, .5f, 8f * sc, t.armor);
                break;
        }
    }

    /** Growths on the torso front / shoulders. */
    private void frontGrowth(int kind, Spec s, float shY, float hipY, float b) {
        int theme = themeIndex();
        float sc = Math.max(.8f, s.shW / 9f);
        float sx = s.shW * .72f, sy = shY + 1f + b;
        switch (theme) {
            case 1: // vines + leaves
                leaf(-s.shW * .6f, shY + 3, -.9f, 6 * sc); leaf(s.shW * .6f, shY + 2, .9f, 6 * sc);
                leaf(0, hipY - 3, 0f, 5 * sc);
                break;
            case 2: // toadstool on shoulder + slime drips
                oval(sx - 4 * sc, sy - 5 * sc, sx + 4 * sc, sy, t.accent, true);
                p.setStyle(Paint.Style.FILL); p.setColor(withAlpha(t.glow, 200));
                cv.drawOval(-s.waistW * .5f, hipY - s.torsoH * .45f, -s.waistW * .1f, hipY - s.torsoH * .45f + 7 * sc, p);
                break;
            case 4: // ice shards on shoulders
                shard(sx, sy, .3f, 9 * sc, 3 * sc); shard(-sx, sy, -.3f, 7 * sc, 2.6f * sc);
                break;
            case 5: // rivets + plate seam
                p.setStyle(Paint.Style.FILL); p.setColor(tint(t.accent));
                for (int i = -1; i <= 1; i++) cv.drawCircle(i * s.waistW * .5f, shY + s.torsoH * .3f, 1.1f * sc, p);
                break;
            case 6: crystal(sx, sy, .25f, 8 * sc, 1f); crystal(-sx, sy, -.25f, 6 * sc, 2f); break;
            case 7: // lightning scar
                bolt(0, shY + 3, 8 * sc, 3f);
                break;
            case 8: horn(sx, sy, .9f, 7 * sc, t.bone); horn(-sx, sy, -.9f, 6 * sc, t.bone); break;
            case 9: // gold sigil
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.4f); p.setColor(tint(t.accent));
                cv.drawCircle(0, shY + s.torsoH * .38f, 3.8f * sc, p);
                cv.drawLine(-5.5f * sc, shY + s.torsoH * .38f, 5.5f * sc, shY + s.torsoH * .38f, p);
                break;
            default: break;
        }
    }

    private void frontGrowthImp() {
        int theme = themeIndex();
        if (theme == 1) leaf(-6, -42, -.8f, 5);
        else if (theme == 4) shard(6, -46, .4f, 7, 2.4f);
        else if (theme == 6) crystal(-6, -46, -.3f, 7, 2f);
    }

    /** Large growths behind the body (back crystals, spikes, cloth). */
    private void backGrowth(int kind, Spec s, float shY, float hipY) {
        int theme = themeIndex();
        float sc = Math.max(.9f, s.shW / 9f);
        float cx = -s.shW * .55f, cy = shY + 6;
        switch (theme) {
            case 0: break;
            case 1: leaf(cx, cy - 2, -1.1f, 9 * sc); leaf(cx - 2, cy + 6, -1.4f, 8 * sc); break;
            case 2:
                oval(cx - 6 * sc, cy - 7 * sc, cx + 3 * sc, cy - 1, t.accent, true);
                oval(cx - 2 * sc, cy - 1, cx + 6 * sc, cy + 5 * sc, mixColor(t.accent, t.glow, .4f), true);
                break;
            case 3: horn(cx, cy, -.5f, 12 * sc, t.armorD); horn(cx + 3, cy + 7, -.8f, 9 * sc, t.armorD); break;
            case 4: shard(cx, cy, -.6f, 12 * sc, 3.4f * sc); shard(cx + 2, cy + 8, -.9f, 9 * sc, 3f * sc); break;
            case 5: oval(cx - 5 * sc, cy - 3, cx + 3 * sc, cy + 9 * sc, t.armor, true); break;
            case 6: crystal(cx, cy, -.5f, 14 * sc, 3f); crystal(cx + 2, cy + 8, -.8f, 10 * sc, 4f); break;
            case 7: // tattered cloth sail
                mv(cx, cy); qd(cx - 14 * sc, cy + 4, cx - 12 * sc, cy + 20 * sc); ln(cx - 3, cy + 12 * sc); ln(cx, cy + 8); done(t.armor, true);
                break;
            case 8: horn(cx, cy, -.6f, 13 * sc, t.bone); horn(cx + 2, cy + 8, -.9f, 10 * sc, t.bone); break;
            default:
                mv(cx, cy); qd(cx - 16 * sc, cy + 2, cx - 10 * sc, cy + 22 * sc); ln(cx, cy + 10); done(t.armor, true);
                break;
        }
    }

    private void backGrowthBoar(float bodyY, float time) {
        int theme = themeIndex();
        float gx = -4, gy = bodyY - 27;
        switch (theme) {
            case 2: oval(gx - 5, gy - 6, gx + 5, gy, t.accent, true); break;
            case 4: shard(gx - 4, gy, -.2f, 11, 3.4f); shard(gx + 6, gy + 1, .3f, 9, 3f); break;
            case 6: crystal(gx - 4, gy, -.2f, 12, time); crystal(gx + 7, gy + 1, .3f, 9, time + 1); break;
            case 9:
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.8f); p.setColor(withAlpha(t.glow, 200));
                cv.drawOval(gx - 13, gy - 8, gx + 13, gy + 1, p);
                break;
            default: break;
        }
    }

    /** Pulsing glow veins for the hot / magical themes. */
    private void veins(int kind, Spec s, float hipY, float shY, float time) {
        int theme = themeIndex();
        if (theme != 3 && theme != 6 && theme != 9 && theme != 8) return;
        float pulse = .55f + .45f * (float) Math.sin(time * 3f + kind);
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(Math.max(1.2f, s.shW * .11f));
        p.setColor(withAlpha(t.glow, (int) (235 * pulse)));
        float w = s.waistW;
        path.reset(); path.moveTo(-w * .5f, hipY - 3); path.lineTo(-w * .2f, hipY - s.torsoH * .35f);
        path.lineTo(-w * .6f, hipY - s.torsoH * .55f); path.lineTo(-w * .1f, shY + 3);
        cv.drawPath(path, p);
        path.reset(); path.moveTo(w * .5f, hipY - 4); path.lineTo(w * .3f, hipY - s.torsoH * .4f);
        path.lineTo(w * .7f, hipY - s.torsoH * .6f);
        cv.drawPath(path, p);
    }

    private void veinsBoar(float bodyY, float time) {
        int theme = themeIndex();
        if (theme != 3 && theme != 6 && theme != 9 && theme != 8) return;
        float pulse = .55f + .45f * (float) Math.sin(time * 3f);
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeWidth(2f);
        p.setColor(withAlpha(t.glow, (int) (230 * pulse)));
        path.reset(); path.moveTo(-26, bodyY + 2); path.lineTo(-16, bodyY - 6); path.lineTo(-22, bodyY - 14);
        cv.drawPath(path, p);
        path.reset(); path.moveTo(12, bodyY + 4); path.lineTo(18, bodyY - 5); path.lineTo(14, bodyY - 12);
        cv.drawPath(path, p);
    }

    /** Ambient motes: embers, snow, spores, fireflies, sparks ... one per map. */
    private void aura(int mode, float cy, float halfW, float h, float time) {
        int theme = themeIndex();
        int n = theme == 0 ? 0 : 4;
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < n; i++) {
            float ph = time * (theme == 3 || theme == 8 ? .9f : .6f) + i * 1.9f;
            float u = (ph % 6.2831f) / 6.2831f;       // 0..1 life
            float x = (float) Math.sin(ph * 1.3f + i * 2.1f) * halfW * .8f;
            float y;
            int alpha = (int) (170 * (float) Math.sin(Math.PI * u));
            float r = 1.5f;
            switch (theme) {
                case 3: y = cy + h * .4f - u * h; r = 1.8f; break;          // embers rise
                case 4: y = cy - h * .4f + u * h; r = 1.6f; break;          // snow falls
                case 2: y = cy + h * .2f - u * h * .8f; r = 2.2f; break;    // spores
                case 8: y = cy + h * .3f - u * h * 1.1f; r = 2.6f; break;   // smoke wisps
                default: y = cy + (float) Math.sin(ph * 2f + i) * h * .35f; break; // hover motes
            }
            p.setColor(withAlpha(theme == 8 ? t.accent : t.glow, Math.max(0, alpha)));
            cv.drawCircle(x, y, r, p);
        }
        if (theme == 7 && ((int) (time * 2.4f)) % 5 == 0) { // occasional spark arc
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.2f);
            p.setColor(withAlpha(0xffffffff, 200));
            cv.drawLine(-halfW * .6f, cy, -halfW * .2f, cy - h * .2f, p);
            cv.drawLine(-halfW * .2f, cy - h * .2f, halfW * .1f, cy + h * .05f, p);
        }
    }

    // ---- decoration primitives -----------------------------------------
    private void horn(float x, float y, float tilt, float len, int color) {
        float dx = (float) Math.sin(tilt) * len, dy = -(float) Math.cos(tilt) * len;
        float w = Math.max(1.8f, len * .22f);
        mv(x - w, y); ln(x + dx, y + dy); ln(x + w, y); done(color, true);
    }
    private void bigHorn(float x, float y, int side, float sc, int color, int tip) {
        mv(x - 2.4f * sc, y);
        qd(x + side * 9 * sc, y - 3 * sc, x + side * 8 * sc, y - 15 * sc);
        qd(x + side * 3 * sc, y - 7 * sc, x + 2.4f * sc, y);
        done(color, true);
        p.setStyle(Paint.Style.FILL); p.setColor(withAlpha(tip, 220));
        cv.drawCircle(x + side * 8 * sc, y - 15 * sc, 1.9f * sc, p);
    }
    private void antler(float x, float y, int side, float sc, float time) {
        float sway = (float) Math.sin(time * 2f) * .6f;
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(2f * sc); p.setColor(tint(t.armor));
        cv.drawLine(x, y + 1, x + side * 4 * sc, y - 10 * sc + sway, p);
        cv.drawLine(x + side * 4 * sc, y - 10 * sc + sway, x + side * 9 * sc, y - 17 * sc, p);
        cv.drawLine(x + side * 2.5f * sc, y - 6 * sc, x - side * 2 * sc, y - 13 * sc, p);
        leaf(x + side * 9 * sc, y - 17 * sc, side * .6f, 5 * sc);
    }
    private void leaf(float x, float y, float tilt, float len) {
        float dx = (float) Math.sin(tilt) * len, dy = -(float) Math.cos(tilt) * len;
        float px = -(float) Math.cos(tilt) * len * .34f, py = -(float) Math.sin(tilt) * len * .34f;
        mv(x, y); qd(x + dx * .5f + px, y + dy * .5f + py, x + dx, y + dy);
        qd(x + dx * .5f - px, y + dy * .5f - py, x, y); done(t.accent, true);
    }
    private void shard(float x, float y, float tilt, float len, float w) {
        float dx = (float) Math.sin(tilt) * len, dy = -(float) Math.cos(tilt) * len;
        mv(x - w, y); ln(x + dx * .55f - w * .5f, y + dy * .6f); ln(x + dx, y + dy);
        ln(x + dx * .55f + w * .5f, y + dy * .6f); ln(x + w, y); done(mixColor(t.accent, 0xffffffff, .25f), true);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1f); p.setColor(0xccffffff);
        cv.drawLine(x - w * .2f, y - 1, x + dx * .5f, y + dy * .55f, p);
    }
    private void crystal(float x, float y, float tilt, float len, float time) {
        shard(x, y, tilt, len, Math.max(2f, len * .26f));
        float pulse = .5f + .5f * (float) Math.sin(time * 3f);
        p.setStyle(Paint.Style.FILL); p.setColor(withAlpha(t.glow, (int) (90 + 110 * pulse)));
        cv.drawCircle(x + (float) Math.sin(tilt) * len * .6f, y - (float) Math.cos(tilt) * len * .6f, len * .2f, p);
    }
    private void bolt(float x, float y, float len, float time) {
        float j = (float) Math.sin(time * 9f) * 1.2f;
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeWidth(1.8f); p.setColor(withAlpha(0xfff6fbff, 240));
        path.reset(); path.moveTo(x, y); path.lineTo(x - len * .25f + j, y - len * .35f);
        path.lineTo(x + len * .15f, y - len * .5f); path.lineTo(x - len * .15f + j, y - len);
        cv.drawPath(path, p);
    }

    // =====================================================================
    // Low-level helpers
    // =====================================================================
    private int themeIndex() { return themeIdx; }
    private int tint(int col) {
        if (hurtMix <= 0f) return col;
        int a = Color.alpha(col);
        int r = Math.round(Color.red(col) + (255 - Color.red(col)) * hurtMix * .85f);
        int g = Math.round(Color.green(col) + (120 - Color.green(col)) * hurtMix * .45f);
        int b = Math.round(Color.blue(col) + (120 - Color.blue(col)) * hurtMix * .45f);
        return Color.argb(a, Math.min(255, r), g, b);
    }
    private static int withAlpha(int col, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)), Color.red(col), Color.green(col), Color.blue(col));
    }
    private static int mixColor(int a, int b, float t) {
        return Color.argb(255,
                Math.round(Color.red(a) + (Color.red(b) - Color.red(a)) * t),
                Math.round(Color.green(a) + (Color.green(b) - Color.green(a)) * t),
                Math.round(Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t));
    }
    private void mv(float x, float y) { path.reset(); path.moveTo(x, y); }
    private void ln(float x, float y) { path.lineTo(x, y); }
    private void qd(float cx, float cy, float x, float y) { path.quadTo(cx, cy, x, y); }
    private void done(int color, boolean outline) {
        path.close();
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(color));
        cv.drawPath(path, p);
        if (outline) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.3f);
            p.setStrokeJoin(Paint.Join.ROUND); p.setColor(tint(OUT));
            cv.drawPath(path, p);
            p.setStyle(Paint.Style.FILL);
        }
    }
    private void oval(float l, float tp, float r, float b, int color, boolean outline) {
        p.setStyle(Paint.Style.FILL);
        if (outline) { p.setColor(tint(OUT)); cv.drawOval(l - 1.1f, tp - 1.1f, r + 1.1f, b + 1.1f, p); }
        p.setColor(tint(color));
        cv.drawOval(l, tp, r, b, p);
    }
    private void ball(float x, float y, float r, int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(OUT)); cv.drawCircle(x, y, r + 1.1f, p);
        p.setColor(tint(color)); cv.drawCircle(x, y, r, p);
    }
    private void capsule(float ax, float ay, float bx, float by, float w, int color) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(w + 2.4f); p.setColor(tint(OUT));
        cv.drawLine(ax, ay, bx, by, p);
        p.setStrokeWidth(w); p.setColor(tint(color));
        cv.drawLine(ax, ay, bx, by, p);
        float dx = bx - ax, dy = by - ay, l = (float) Math.hypot(dx, dy);
        if (l < .01f) return;
        float nx = -dy / l, ny = dx / l;
        if (ny > 0) { nx = -nx; ny = -ny; }
        p.setStrokeWidth(w * .28f);
        p.setColor(withAlpha(0xffffffff, 55 + (hurtMix > 0 ? 40 : 0)));
        cv.drawLine(ax + nx * w * .2f, ay + ny * w * .2f, bx + nx * w * .2f, by + ny * w * .2f, p);
    }
    private void solve(float ax, float ay, float tx, float ty, float l1, float l2, float biasX, float biasY) {
        float dx = tx - ax, dy = ty - ay;
        float d = (float) Math.hypot(dx, dy);
        if (d < .001f) { dx = 0; dy = 1; d = 1f; }
        float maxR = l1 + l2 - .05f, minR = Math.abs(l1 - l2) + .05f;
        float dd = Math.max(minR, Math.min(maxR, d));
        float ux = dx / d, uy = dy / d;
        float a = (l1 * l1 - l2 * l2 + dd * dd) / (2f * dd);
        float h = (float) Math.sqrt(Math.max(0f, l1 * l1 - a * a));
        float nx = -uy, ny = ux;
        if (nx * biasX + ny * biasY < 0f) { nx = -nx; ny = -ny; }
        ik[0] = ax + ux * a + nx * h; ik[1] = ay + uy * a + ny * h;
        ik[2] = ax + ux * dd; ik[3] = ay + uy * dd;
    }
    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
    private static float sin01(float v) { return (float) Math.sin(Math.PI * clamp01(v)); }
    private static float ease01(float v) { v = clamp01(v); return v * v * (3f - 2f * v); }
}
