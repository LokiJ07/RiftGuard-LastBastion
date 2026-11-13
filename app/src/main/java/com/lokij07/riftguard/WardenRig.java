package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Fully skeletal, code-drawn Warden (armored knight with blue cape).
 *
 * Replaces the old "one painted picture, cropped and bobbed" hero. Every part
 * is its own bone and moves on its own clock:
 *   - two-bone IK legs with real alternating steps (planted / swinging foot),
 *     stride direction follows the actual movement heading
 *   - pelvis bob, torso lean and counter-swinging back arm while running
 *   - breathing idle with gaze following the target
 *   - per-weapon attack: torso lunge, lean and wrist travel come from the
 *     SAME WeaponAnimation / WeaponRig functions the hit-detection and the
 *     muzzle spawn use, so visuals and combat can never drift apart
 *   - cast pose (raised free hand with a charging orb), hurt flinch + flash,
 *     death kneel
 *   - cape that trails behind the movement and whips on attacks
 *
 * Coordinates: local units, feet at (0,0), +y is down, hero faces +x. The
 * caller's canvas is mirrored here when the hero faces left.
 * No bitmap atlas is required.
 */
public final class WardenRig {
    // ---- palette (matched to the original painted Warden) ----
    private static final int OUT = 0xff0c121c;
    private static final int STEEL_D = 0xff353e50, STEEL_M = 0xff62708a;
    private static final int STEEL_L = 0xffa3b3c8, STEEL_H = 0xffe4eef8;
    private static final int GOLD = 0xffe5b648, GOLD_D = 0xff94682a;
    private static final int BLUE = 0xff2d5acc, BLUE_D = 0xff1b3a8c, BLUE_DD = 0xff102462;
    private static final int SKIN = 0xffe3ab84, SKIN_D = 0xffb87d5a;
    private static final int HAIR = 0xff68402a, HAIR_L = 0xff8e5e3c;
    private static final int LEATHER = 0xff3b2b21;

    // ---- skeleton dimensions ----
    private static final float PELVIS_Y = -58f;
    private static final float THIGH = 28f, SHIN = 28f;
    private static final float ARM_UP = 22f, ARM_LOW = 22f;
    private static final float SHOULDER_BACK_X = -20f, SHOULDER_Y = -87f;
    private static final float TORSO_CX = -2f;
    private static final float STRIDE_RATE = 11.7f; // same clock as WeaponRig

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final float[] ik = new float[4]; // kneeX, kneeY, endX, endY
    private final float[][] foot = new float[2][3]; // x, y, planted(1)
    private float hurtMix;

    /** Always true: this rig needs no bitmap. Kept for call-site symmetry. */
    public boolean ready() { return true; }

    public boolean hero(Canvas c, float x, float y, float time, float facing,
                        boolean running, float attackProgress, float castProgress,
                        boolean hurt, int weaponId, int masteryNodes) {
        return hero(c, x, y, time, facing, running, attackProgress, castProgress,
                hurt, weaponId, masteryNodes, 0, 0, -1f);
    }

    /** Legacy signature for standalone rig previews. */
    public boolean hero(Canvas c, float x, float y, float time, float facing,
                        boolean running, float attackProgress, float castProgress,
                        boolean hurt, int weaponId, int masteryNodes, float death) {
        return hero(c,x,y,time,facing,running,attackProgress,castProgress,
                hurt,weaponId,masteryNodes,0,0,death);
    }

    /** v0.60: save-backed equipped form and armor tier in the articulated rig.
     * @param death 0..1 while the DEATH clip plays, negative otherwise.
     */
    public boolean hero(Canvas c, float x, float y, float time, float facing,
                        boolean running, float attackProgress, float castProgress,
                        boolean hurt, int weaponId, int masteryNodes,
                        int weaponForm, int armorTier, float death) {
        if(weaponId < 0 || weaponId >= 6 || weaponForm < 0 ||
                weaponForm > 4 || armorTier < 0 || armorTier > 4)
            return false; // original MotionArt fallback can handle invalid state
        if (!Float.isFinite(time) || !Float.isFinite(facing)) return false;
        final int id = Math.max(0, Math.min(5, weaponId));
        final boolean left = Math.cos(facing) < -.28f;
        final float lx = (float) Math.cos(facing) * (left ? -1f : 1f); // local heading
        final float ly = (float) Math.sin(facing);
        final boolean dying = death >= 0f;
        final boolean attacking = attackProgress >= 0f && !dying;
        final boolean casting = castProgress >= 0f && !dying;
        // Quick white-red blink while hurt (invulnerability frames), mild tone on death.
        hurtMix = dying ? .3f : hurt ? (((int) (time * 16f)) % 2 == 0 ? .42f : .1f) : 0f;
        cv = c;

        final float phase = time * STRIDE_RATE;
        final float breath = (float) Math.sin(time * 2.2f);
        final float kneel = dying ? ease(Math.min(1f, death * 1.7f)) : 0f;

        // ---- body motion ----
        float bob = running ? -Math.abs((float) Math.sin(phase)) * 3.2f
                : breath * .7f;
        float shiftX = WeaponAnimation.torsoShiftX(id, attacking ? attackProgress : -1f);
        float shiftY = WeaponAnimation.torsoShiftY(id, attacking ? attackProgress : -1f);
        float lean = running ? lx * 3.2f + (float) Math.sin(phase) * 1.2f : breath * .5f;
        if (attacking) lean += WeaponAnimation.anticipation(id, attackProgress) * .38f;
        float shake = 0;
        if (hurt && !dying) {
            lean -= 6f;
            shake = (float) Math.sin(time * 70f) * 1.3f;
        }
        if (dying) lean += 10f * kneel;
        float drop = 31f * kneel;

        c.save();
        c.translate(x + shake, y);
        if (left) c.scale(-1, 1);

        float capeSway = (running ? -lx * 9f : 0f) - shiftX * .7f
                + (float) Math.sin(time * 2.4f) * 1.3f;

        // ---- cape: furthest back, drawn before the legs ----
        c.save();
        c.translate(shiftX * .6f, bob + drop * .92f + shiftY);
        c.rotate(lean, TORSO_CX, PELVIS_Y);
        drawCape(c, time, phase, running, capeSway, lx, attacking ? attackProgress : -1f);
        c.restore();

        // ---- legs (outside the leaning group so the feet stay planted) ----
        float hipY = PELVIS_Y + bob * .6f + shiftY * .7f + drop;
        float hipCX = TORSO_CX + 1f + shiftX * .55f;
        drawLegs(c, hipCX, hipY, phase, running && !dying, lx, ly, attacking,
                shiftX, id, kneel);

        // ---- upper body group ----
        c.save();
        c.translate(0, bob + drop * .92f);
        c.rotate(lean, TORSO_CX, PELVIS_Y);

        c.save();
        c.translate(shiftX, shiftY);
        boolean grip = !casting && (id == 1 || id == 3 || id == 5) && !dying;
        WeaponRig.Pose pose = WeaponRig.aimedPose(id,
                attacking ? attackProgress : -1f, casting ? castProgress : -1f,
                time, running, facing);

        if (!grip) drawBackArm(c, pose, id, time, phase, running, lx, ly,
                casting ? castProgress : -1f, dying);
        drawLowerTabard(c, time, phase, running, lx);
        drawTorso(c, breath);
        drawHead(c, lx, ly, attacking ? attackProgress : -1f, hurt, dying, kneel);
        if (grip) drawBackArm(c, pose, id, time, phase, running, lx, ly, -1f, false);
        drawPauldron(c, SHOULDER_BACK_X + 1f, SHOULDER_Y + 2.5f, 8.5f, false);
        drawAscendedArmor(c,armorTier,time,breath);
        c.restore();

        // Front (weapon) arm uses the absolute pose that already contains the shift.
        drawFrontArmAndWeapon(c, pose, id, time, attacking ? attackProgress : -1f,
                casting ? castProgress : -1f, running, facing,
                masteryNodes, weaponForm);
        c.restore(); // upper body group
        c.restore();
        return true;
    }

    // =====================================================================
    // Legs
    // =====================================================================
    private void drawLegs(Canvas c, float hipCX, float hipY, float phase,
                          boolean walking, float lx, float ly, boolean attacking,
                          float shiftX, int weaponId, float kneel) {
        for (int s = 0; s < 2; s++) {
            int side = s == 0 ? -1 : 1;
            float baseX = hipCX + side * 9.5f;
            float fx = baseX + side * (attacking ? 2.5f : 0f), fy = 0f, planted = 1f;
            if (walking) {
                float a = phase + (side == 1 ? (float) Math.PI : 0f);
                float sw = (float) Math.sin(a), cs = (float) Math.cos(a);
                fx = baseX + sw * 15f * lx;
                fy = sw * 5f * ly - Math.max(0f, cs) * (5.5f + 2.5f * Math.abs(lx));
                planted = cs <= 0f ? 1f : 0f;
            } else if (attacking) {
                // Lunge stance: lead foot forward, rear foot braced.
                fx += side == 1 ? shiftX * .55f + 3f : -shiftX * .2f - 2f;
            }
            foot[s][0] = fx; foot[s][1] = fy; foot[s][2] = planted;
        }
        // Draw the foot that is lower on screen (closer to camera) last.
        int first = foot[0][1] <= foot[1][1] ? 0 : 1;
        for (int k = 0; k < 2; k++) {
            int s = k == 0 ? first : 1 - first;
            int side = s == 0 ? -1 : 1;
            float hx = hipCX + side * 8.5f;
            drawLeg(c, hx, hipY, foot[s][0], foot[s][1], side, lx, foot[s][2] > 0, kneel);
        }
    }

    private void drawLeg(Canvas c, float hx, float hy, float fx, float fy, int side,
                         float lx, boolean planted, float kneel) {
        float ankleX = fx, ankleY = fy - 5f;
        float biasX = Math.abs(lx) > .25f ? Math.signum(lx) : side * .6f;
        solve(hx, hy, ankleX, ankleY, THIGH, SHIN, biasX, kneel > 0 ? 0 : -.2f);
        float kx = ik[0], ky = ik[1], ex = ik[2], ey = ik[3];
        float toe = Math.abs(lx) > .2f ? lx * 4f : side * 2.2f;
        // boot
        path.reset();
        path.moveTo(ex - 5.2f, ey - 4f);
        path.lineTo(ex + 5.2f, ey - 4f);
        path.lineTo(ex + 6.5f + toe * .5f, ey + 2.5f);
        path.quadTo(ex + 9f + toe, ey + 4.6f, ex + 7f + toe, ey + 5.6f);
        path.lineTo(ex - 6.5f, ey + 5.6f);
        path.quadTo(ex - 7.5f, ey + 2f, ex - 5.2f, ey - 4f);
        path.close();
        fill(path, LEATHER, true);
        poly(new float[]{ex + 2f + toe * .4f, ey + 1.5f, ex + 7.5f + toe, ey + 3f,
                ex + 7f + toe, ey + 5.6f, ex + 1f, ey + 5.6f}, STEEL_D, false);
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(GOLD_D));
        c.drawRect(ex - 5.4f, ey - 5.2f, ex + 5.4f, ey - 3f, p);
        // shin + thigh
        capsule(c, kx, ky, ex, ey - 1f, 9.5f, STEEL_D, STEEL_M, STEEL_L);
        capsule(c, hx, hy, kx, ky, 11.5f, STEEL_D, STEEL_M, STEEL_L);
        // knee cop
        ball(c, kx + (kx - (hx + ex) * .5f) * .15f, ky, 5.4f, GOLD);
        ball(c, kx - 1f, ky - 1.2f, 2.2f, STEEL_H);
    }

    // =====================================================================
    // Cape, tabard, torso, head
    // =====================================================================
    private void drawCape(Canvas c, float time, float phase, boolean running,
                          float sway, float lx, float attack) {
        float f1 = (float) Math.sin(time * 3.1f) * 1.4f + (running ? (float) Math.sin(phase * 2f) * 2.2f : 0);
        float f2 = (float) Math.sin(time * 3.1f + 1.7f) * 1.6f + (running ? (float) Math.sin(phase * 2f + 1.3f) * 2.6f : 0);
        float f3 = (float) Math.sin(time * 3.1f + 3.1f) * 1.4f + (running ? (float) Math.sin(phase * 2f + 2.6f) * 2.2f : 0);
        float whip = attack >= 0 ? (float) Math.sin(Math.PI * attack) * 7f : 0f;
        float sw = sway - whip;
        float lift = running ? 5f : 0f; // streams outward and upward when running
        path.reset();
        path.moveTo(-19f, -88f);
        path.cubicTo(-33f, -76f, -37f + sw * .5f, -44f, -35f + sw, -14f + f1 - lift * .4f);
        path.quadTo(-22f + sw * .8f, -4f + f2 - lift, -8f + sw * .6f, -8f + f3 - lift);
        path.quadTo(5f + sw * .5f, -3f + f1 - lift, 17f + sw * .5f, -8f + f2 - lift);
        path.quadTo(30f + sw * .8f, -4f + f3 - lift, 33f + sw, -14f + f1 - lift * .4f);
        path.cubicTo(37f + sw * .5f, -44f, 31f, -76f, 16f, -88f);
        path.close();
        fill(path, BLUE_D, true);
        // inner folds
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(3f);
        p.setColor(tint(BLUE_DD));
        c.drawLine(-14f, -80f, -22f + sw * .6f, -22f + f1, p);
        c.drawLine(12f, -80f, 21f + sw * .6f, -22f + f3, p);
        p.setStrokeWidth(2f);
        p.setColor(tint(BLUE));
        c.drawLine(-6f, -82f, -9f + sw * .4f, -16f + f2, p);
        c.drawLine(5f, -82f, 7f + sw * .4f, -16f + f2, p);
        // gold hem
        path.reset();
        path.moveTo(-35f + sw, -14f + f1 - lift * .4f);
        path.quadTo(-22f + sw * .8f, -4f + f2 - lift, -8f + sw * .6f, -8f + f3 - lift);
        path.quadTo(5f + sw * .5f, -3f + f1 - lift, 17f + sw * .5f, -8f + f2 - lift);
        path.quadTo(30f + sw * .8f, -4f + f3 - lift, 33f + sw, -14f + f1 - lift * .4f);
        p.setStrokeWidth(2.2f);
        p.setColor(tint(GOLD));
        c.drawPath(path, p);
    }

    private void drawLowerTabard(Canvas c, float time, float phase, boolean running, float lx) {
        float sw = running ? (float) Math.sin(phase) * 2.5f * lx : (float) Math.sin(time * 2.2f) * .6f;
        float hem = running ? (float) Math.abs(Math.sin(phase)) * 1.8f : 0f;
        poly(new float[]{-10f, -60f, 8f, -60f, 10f + sw, -30f - hem, 4f + sw, -25f - hem,
                -2f + sw * 1.2f, -24f - hem, -9f + sw, -25f - hem, -12f + sw, -30f - hem}, BLUE, true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.8f);
        p.setColor(tint(GOLD));
        p.setStrokeCap(Paint.Cap.ROUND);
        path.reset();
        path.moveTo(-12f + sw, -30f - hem);
        path.lineTo(-9f + sw, -25f - hem);
        path.lineTo(-2f + sw * 1.2f, -24f - hem);
        path.lineTo(4f + sw, -25f - hem);
        path.lineTo(10f + sw, -30f - hem);
        c.drawPath(path, p);
        p.setColor(tint(BLUE_DD));
        p.setStrokeWidth(1.6f);
        c.drawLine(-2f, -58f, -2f + sw * .6f, -28f, p);
        // hip plates (tassets)
        poly(new float[]{-18f, -60f, -8f, -60f, -9f, -46f, -17f, -43f}, STEEL_M, true);
        poly(new float[]{12f, -60f, 4f, -60f, 5f, -46f, 13f, -43f}, STEEL_M, true);
        poly(new float[]{-17f, -58f, -13f, -58f, -13f, -50f, -16f, -49f}, STEEL_L, false);
    }

    private void drawTorso(Canvas c, float breath) {
        float ch = breath * .5f; // chest rises with breathing
        // waist + chest plates
        poly(new float[]{-21f, -83f + ch, -16f, -88f + ch, 13f, -88f + ch, 17f, -83f + ch,
                14f, -72f, 12f, -60f, -16f, -60f, -18f, -72f}, STEEL_M, true);
        // blue tabard panel on the chest
        poly(new float[]{-11f, -87f + ch, 7f, -87f + ch, 8f, -64f, -2f, -58f, -12f, -64f}, BLUE, true);
        // plate edge highlights
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(1.8f);
        p.setColor(tint(STEEL_L));
        c.drawLine(-17f, -82f + ch, -13f, -66f, p);
        p.setColor(tint(STEEL_D));
        c.drawLine(14f, -82f + ch, 11f, -66f, p);
        // gold star on the chest
        float sx = -2f, sy = -76f + ch;
        poly(new float[]{sx, sy - 8f, sx + 2.2f, sy - 2.2f, sx + 8f, sy, sx + 2.2f, sy + 2.2f,
                sx, sy + 8f, sx - 2.2f, sy + 2.2f, sx - 8f, sy, sx - 2.2f, sy - 2.2f}, GOLD, true);
        ball(c, sx, sy, 1.9f, STEEL_H);
        // gold trim along the neckline
        p.setStrokeWidth(2f);
        p.setColor(tint(GOLD));
        c.drawLine(-12f, -86f + ch, -2f, -81f + ch, p);
        c.drawLine(8f, -86f + ch, -2f, -81f + ch, p);
        // belt + buckle
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(OUT));
        c.drawRect(-17f, -63f, 13f, -55f, p);
        p.setColor(tint(LEATHER));
        c.drawRect(-16f, -62f, 12f, -56f, p);
        p.setColor(tint(GOLD_D));
        c.drawRect(-16f, -57.4f, 12f, -56f, p);
        ball(c, -2f, -59f, 4.2f, GOLD);
        ball(c, -2f, -59f, 1.8f, BLUE_D);
        // gorget (neck guard)
        p.setStyle(Paint.Style.FILL);
        path.reset();
        path.moveTo(-12f, -90f + ch);
        path.quadTo(-2f, -84f + ch, 8f, -90f + ch);
        path.lineTo(7f, -95f + ch);
        path.lineTo(-11f, -95f + ch);
        path.close();
        fill(path, STEEL_M, true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.6f);
        p.setColor(tint(GOLD));
        path.reset();
        path.moveTo(-11f, -92f + ch);
        path.quadTo(-2f, -86f + ch, 7f, -92f + ch);
        c.drawPath(path, p);
    }

    private void drawHead(Canvas c, float lx, float ly, float attack,
                          boolean hurt, boolean dying, float kneel) {
        float hx = -1f + lx * 1.2f, hy = -107f + (dying ? 5f * kneel : 0f);
        float tilt = dying ? 14f * kneel : (hurt ? -7f : 0f);
        c.save();
        c.rotate(tilt, -1f, -94f);
        // neck
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(SKIN_D));
        c.drawRect(-5f, -98f, 3f, -90f, p);
        // ears
        ball(c, hx - 11.2f, hy + 1.5f, 2.6f, SKIN_D);
        ball(c, hx + 11.2f, hy + 1.5f, 2.6f, SKIN_D);
        // face
        path.reset();
        path.moveTo(hx - 10.5f, hy - 6f);
        path.cubicTo(hx - 11.5f, hy + 6f, hx - 7f, hy + 13f, hx, hy + 13.5f);
        path.cubicTo(hx + 7f, hy + 13f, hx + 11.5f, hy + 6f, hx + 10.5f, hy - 6f);
        path.cubicTo(hx + 8f, hy - 13f, hx - 8f, hy - 13f, hx - 10.5f, hy - 6f);
        path.close();
        fill(path, SKIN, true);
        // jaw shadow
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(SKIN_D));
        path.reset();
        path.moveTo(hx - 9f, hy + 5f);
        path.quadTo(hx, hy + 9f, hx + 9f, hy + 5f);
        path.quadTo(hx + 7f, hy + 13f, hx, hy + 13.5f);
        path.quadTo(hx - 7f, hy + 13f, hx - 9f, hy + 5f);
        path.close();
        c.drawPath(path, p);
        // brows
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(2f);
        p.setColor(tint(HAIR));
        float fierce = attack >= 0 ? 1.6f : 0f;
        c.drawLine(hx - 8f, hy - 3.2f - fierce, hx - 2.5f, hy - 2.2f + fierce * .5f, p);
        c.drawLine(hx + 2.5f, hy - 2.2f + fierce * .5f, hx + 8f, hy - 3.2f - fierce, p);
        // eyes (gaze follows the target, squint when hurt)
        float gx = lx * 1.3f, gy = ly * 0.8f;
        for (int e = -1; e <= 1; e += 2) {
            float ex = hx + e * 5.2f, ey = hy + .6f;
            if (hurt || dying) {
                p.setStrokeWidth(1.8f);
                p.setColor(tint(OUT));
                c.drawLine(ex - 2.6f, ey - 1f, ex + 2.6f, ey + 1f, p);
            } else {
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xfff4f1ea);
                c.drawOval(ex - 3.1f, ey - 2.1f, ex + 3.1f, ey + 2.3f, p);
                p.setColor(0xff1f4f7a);
                c.drawCircle(ex + gx, ey + gy * .6f, 1.9f, p);
                p.setColor(0xff0a0f18);
                c.drawCircle(ex + gx, ey + gy * .6f, 1f, p);
                p.setStyle(Paint.Style.STROKE);
            }
        }
        // nose + mouth
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.4f);
        p.setColor(tint(SKIN_D));
        c.drawLine(hx + lx * .8f, hy + 1.2f, hx + lx * .8f - .8f, hy + 5.4f, p);
        p.setColor(tint(0xff8a4b3c));
        if (attack >= 0.12f && attack <= .8f) { // battle shout
            p.setStyle(Paint.Style.FILL);
            c.drawOval(hx - 3f, hy + 7.4f, hx + 3f, hy + 10.8f, p);
        } else {
            c.drawLine(hx - 3f, hy + 8.8f, hx + 3f, hy + 8.8f + (hurt ? 1f : 0f), p);
        }
        // hair: swept back with a few bangs
        path.reset();
        path.moveTo(hx - 12f, hy - 2f);
        path.cubicTo(hx - 15f, hy - 14f, hx - 6f, hy - 20f, hx + 3f, hy - 19f);
        path.cubicTo(hx + 12f, hy - 18f, hx + 15f, hy - 9f, hx + 12f, hy - 2f);
        path.lineTo(hx + 9.5f, hy - 6.5f);
        path.lineTo(hx + 6f, hy - 9f);
        path.lineTo(hx + 2f, hy - 6.8f);
        path.lineTo(hx - 2f, hy - 9.4f);
        path.lineTo(hx - 6f, hy - 7f);
        path.lineTo(hx - 9.5f, hy - 7.4f);
        path.close();
        fill(path, HAIR, true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.8f);
        p.setColor(tint(HAIR_L));
        path.reset();
        path.moveTo(hx - 8f, hy - 13f);
        path.quadTo(hx, hy - 17.5f, hx + 8f, hy - 12f);
        c.drawPath(path, p);
        c.restore();
    }

    // =====================================================================
    // Arms
    // =====================================================================
    private void drawBackArm(Canvas c, WeaponRig.Pose pose, int id, float time,
                             float phase, boolean running, float lx, float ly,
                             float cast, boolean dying) {
        float sx = SHOULDER_BACK_X + 2f, sy = SHOULDER_Y + 3f;
        float tx, ty;
        if (cast >= 0f) {
            float a = (float) Math.sin(Math.PI * Math.min(1f, cast));
            tx = lerp(-27f, -15f, a);
            ty = lerp(-54f, -113f, a);
        } else if (!dying && (id == 1 || id == 3 || id == 5)) {
            // support hand follows the main hand on two-handed weapons
            float gx = pose.handX - (id == 1 ? 10f : 12f);
            float gy = pose.handY + (id == 1 ? 12f : 8f);
            tx = gx - 3f;
            ty = gy + 2f;
        } else {
            float swing = running ? (float) Math.sin(phase) : 0f;
            tx = -27f + lx * 8f * swing;
            ty = -55f + (running ? 4f * (float) Math.cos(phase) + ly * 3f * swing
                    : (float) Math.sin(time * 2.2f + .6f) * 1.2f);
            if (dying) ty += 10f;
        }
        solve(sx, sy, tx, ty, ARM_UP, ARM_LOW, -.6f, .7f);
        float ex = ik[0], ey = ik[1], hx = ik[2], hy = ik[3];
        capsule(c, sx, sy, ex, ey, 9.5f, STEEL_D, STEEL_M, STEEL_L);
        capsule(c, ex, ey, hx, hy, 8.5f, STEEL_D, STEEL_M, STEEL_L);
        ball(c, ex, ey, 5f, GOLD);
        gauntlet(c, hx, hy, 0f);
        if (cast >= 0f) drawCastOrb(c, hx, hy - 8f, cast, time);
    }

    private void drawCastOrb(Canvas c, float x, float y, float cast, float time) {
        float a = (float) Math.sin(Math.PI * Math.min(1f, cast));
        float r = 4f + 8f * a;
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x33a8c8ff);
        c.drawCircle(x, y, r * 2.1f, p);
        p.setColor(0x88b9d8ff);
        c.drawCircle(x, y, r * 1.35f, p);
        p.setColor(0xfff2fbff);
        c.drawCircle(x, y, r * .7f, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.6f);
        p.setColor(0xccd9ecff);
        float spin = time * 9f;
        for (int i = 0; i < 3; i++) {
            float ang = spin + i * 2.094f;
            c.drawCircle(x + (float) Math.cos(ang) * r * 1.6f,
                    y + (float) Math.sin(ang) * r * 1.6f, 1.6f, p);
        }
    }

    private void drawFrontArmAndWeapon(Canvas c, WeaponRig.Pose pose, int id,
                                       float time, float attack, float castProgress,
                                       boolean running, float facing, int masteryNodes,
                                       int weaponForm) {
        boolean blade = id == 1 || id == 2;
        // blade slash trail follows the real moving tip (same data as hit cone)
        if (blade && attack >= .07f && attack < .92f) {
            float t = clamp01(attack);
            WeaponRig.Pose prev = WeaponRig.aimedPose(id, Math.max(0, t - .13f),
                    castProgress, time, running, facing);
            WeaponRig.Point a = WeaponRig.tip(id, prev), b = WeaponRig.tip(id, pose);
            path.reset();
            path.moveTo(a.x, a.y);
            path.quadTo((a.x + b.x) * .5f, (a.y + b.y) * .5f - 7f, b.x, b.y);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            int tintc = id == 2 ? 0xffff9d55 : 0xffb2edff;
            float fade = (float) Math.sin(Math.PI * t);
            p.setColor(tintc);
            p.setStrokeWidth(10f * (1 - t * .4f));
            p.setAlpha((int) (185 * fade));
            c.drawPath(path, p);
            p.setColor(Color.WHITE);
            p.setStrokeWidth(3f);
            p.setAlpha((int) (210 * fade));
            c.drawPath(path, p);
            p.setAlpha(255);
        }
        // armored arm: shoulder -> elbow -> wrist
        capsule(c, pose.shoulderX, pose.shoulderY + 1f, pose.elbowX, pose.elbowY, 10.5f,
                STEEL_D, STEEL_M, STEEL_L);
        capsule(c, pose.elbowX, pose.elbowY, pose.handX, pose.handY, 9f,
                STEEL_D, STEEL_M, STEEL_L);
        ball(c, pose.elbowX, pose.elbowY, 5.4f, GOLD);
        ball(c, pose.elbowX - 1f, pose.elbowY - 1.2f, 2f, STEEL_H);
        drawPauldron(c, pose.shoulderX, pose.shoulderY + 2.5f, 9.5f, true);

        // weapon rotated around the glove socket
        c.save();
        c.translate(pose.handX, pose.handY);
        c.rotate(pose.angle);
        float rs = WeaponRig.renderScale(id);
        c.scale(rs, rs);
        // Exact save-backed mastery form in the actual glove socket.
        // Do not draw the old static WorldArt weapon over the upgraded blade.
        EvolvedWeaponArt.draw(c, id, weaponForm, 0, 0, 1f, time);
        if (masteryNodes >= 2) drawMasteryMarks(c, id, masteryNodes, time);
        c.restore();
        gauntlet(c, pose.handX, pose.handY, pose.angle - 12f);

        if (attack >= 0 && !blade) { // muzzle flash on ranged / magic
            int color = id == 5 ? 0xffaafaff : id == 3 ? 0xffaca5ff : 0xffffce8c;
            WeaponRig.Point m = WeaponRig.tip(id, pose);
            float fade = 1f - clamp01(attack);
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setAlpha((int) (170 * fade));
            c.drawCircle(m.x, m.y, 7f, p);
            p.setAlpha(240);
            c.drawCircle(m.x, m.y, 3f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2f);
            c.drawLine(m.x - 10f, m.y, m.x + 10f, m.y, p);
            p.setAlpha(255);
        }
    }

    private void drawMasteryMarks(Canvas c, int id, int nodes, float time) {
        int tint = Progression.WEAPONS[id].color;
        float pulse = .68f + .24f * (float) Math.sin(time * 6 + id);
        p.setColor(tint);
        p.setAlpha((int) (220 * pulse));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.2f);
        c.drawLine(-3, -11, -3, -29, p);
        c.drawLine(3, -11, 3, -29, p);
        p.setStyle(Paint.Style.FILL);
        c.drawCircle(0, -32, 2.8f, p);
        if (nodes >= 6) {
            path.reset();
            path.moveTo(0, -42); path.lineTo(7, -35); path.lineTo(0, -27); path.lineTo(-7, -35);
            path.close();
            c.drawPath(path, p);
            p.setAlpha(230);
            c.drawCircle(0, -35, 3.3f, p);
        }
        if (nodes >= 11) {
            path.reset();
            path.moveTo(-8, -30); path.lineTo(0, -40); path.lineTo(8, -30); path.lineTo(0, -35);
            path.close();
            p.setColor(tint);
            p.setAlpha(200);
            c.drawPath(path, p);
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.7f);
            c.drawLine(0, -35, 0, -39, p);
        }
        p.setAlpha(255);
    }

    private void gauntlet(Canvas c, float x, float y, float angle) {
        c.save();
        c.translate(x, y);
        c.rotate(angle);
        path.reset();
        path.moveTo(-6, -8); path.lineTo(5, -9); path.lineTo(8, -1);
        path.lineTo(5, 8); path.lineTo(-4, 8); path.lineTo(-8, 1);
        path.close();
        fill(path, STEEL_D, true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(STEEL_L));
        c.drawRoundRect(-5, -6, 5, 3, 2, 2, p);
        p.setColor(tint(GOLD));
        c.drawRect(-3, 2, 3, 3.5f, p);
        c.restore();
    }

    private void drawPauldron(Canvas c, float x, float y, float r, boolean front) {
        // domed shoulder cap: wider than tall, sits on top of the arm
        float rx = r, ry = r * .78f;
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(OUT));
        c.drawOval(x - rx - 1.2f, y - ry - 1.2f - 1.5f, x + rx + 1.2f, y + ry + 1.2f - 1.5f, p);
        p.setColor(tint(STEEL_M));
        c.drawOval(x - rx, y - ry - 1.5f, x + rx, y + ry - 1.5f, p);
        p.setColor(tint(STEEL_L));
        c.drawOval(x - rx * .78f, y - ry * .92f - 1.5f, x + rx * .25f, y + ry * .2f - 1.5f, p);
        // lower plate lames
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.6f);
        p.setColor(tint(STEEL_D));
        c.drawLine(x - rx * .85f, y + ry * .35f, x + rx * .85f, y + ry * .35f, p);
        // gold rim
        p.setStrokeWidth(1.8f);
        p.setColor(tint(GOLD));
        c.drawLine(x - rx * .9f, y + ry * .62f - 1.5f, x + rx * .9f, y + ry * .62f - 1.5f, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(GOLD));
        c.drawCircle(x - rx * .3f, y - ry * .35f - 1.5f, 1.5f, p);
        if (front) {
            p.setColor(tint(BLUE));
            c.drawCircle(x + rx * .35f, y - ry * .1f, 1.3f, p);
        }
    }

    /** Upgrade effects physically belong to the armor's moving torso group.
     * These are additive to the game's existing EvolutionEffects so neither
     * its save-backed logic nor the original Warden's geometry is removed.
     */
    private void drawAscendedArmor(Canvas c,int rank,float time,float breath) {
        if(rank<=0)return;
        // Tier 1: articulated chest engraving following breathing / torso lean.
        p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(1.7f);p.setColor(tint(GOLD));
        c.drawLine(-11,-65,0,-57, p); c.drawLine(0,-57,11,-65,p);
        c.drawLine(-11,-69,0,-76,p);c.drawLine(0,-76,11,-69,p);
        if(rank>=2) {
            // Tier 2: raised engraved shoulder crests and a gold waist clasp.
            p.setStrokeWidth(2f);
            c.drawArc(-28,-103,-12,-84,204,135,false,p);
            c.drawArc(11,-103,29,-83,201,138,false,p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(tint(STEEL_H));c.drawCircle(0,-38,3.4f,p);
            p.setColor(tint(GOLD));c.drawCircle(0,-38,1.7f,p);
        }
        if(rank>=3) {
            // Tier 3: flexible insignia suspension. Moves with the upper body.
            float sw=(float)Math.sin(time*3f)*2f;
            p.setColor(tint(GOLD));
            p.setStrokeWidth(2.3f);p.setStyle(Paint.Style.STROKE);
            c.drawLine(-16,-82,-20+sw,-67,p);
            c.drawLine(17,-82,20+sw,-67,p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(tint(BLUE));c.drawCircle(-20+sw,-67,3.5f,p);
            c.drawCircle(20+sw,-67,3.5f,p);
        }
        if(rank>=4) {
            // Final Ascension: small crystal sigils, not a detached halo sprite.
            float pulse=.72f+.26f*(float)Math.sin(time*4f);
            p.setColor(Color.argb((int)(200*pulse),146,236,255));
            for(int i=-1;i<=1;i++) {
                float cx=i*8, cy=-60+(i==0?-15:3);
                path.reset();path.moveTo(cx,cy-5);
                path.lineTo(cx+3,cy);path.lineTo(cx,cy+6);
                path.lineTo(cx-3,cy);path.close();
                c.drawPath(path,p);
            }
        }
        p.setAlpha(255);p.setStyle(Paint.Style.FILL);
    }

    // =====================================================================
    // Helpers
    // =====================================================================
    private int tint(int col) {
        if (hurtMix <= 0f) return col;
        int a = Color.alpha(col);
        int r = Math.round(Color.red(col) + (255 - Color.red(col)) * hurtMix * .8f);
        int g = Math.round(Color.green(col) + (110 - Color.green(col)) * hurtMix * .45f);
        int b = Math.round(Color.blue(col) + (110 - Color.blue(col)) * hurtMix * .45f);
        return Color.argb(a, Math.min(255, r), g, b);
    }

    private Canvas cv; // canvas of the hero() call currently being drawn

    private void fill(Path pa, int color, boolean outline) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(color));
        cv.drawPath(pa, p);
        if (outline) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.5f);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(tint(OUT));
            cv.drawPath(pa, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    private void poly(float[] pts, int color, boolean outline) {
        path.reset();
        path.moveTo(pts[0], pts[1]);
        for (int i = 2; i + 1 < pts.length; i += 2) path.lineTo(pts[i], pts[i + 1]);
        path.close();
        fill(path, color, outline);
    }

    private void ball(Canvas c, float x, float y, float r, int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(tint(OUT));
        c.drawCircle(x, y, r + 1.2f, p);
        p.setColor(tint(color));
        c.drawCircle(x, y, r, p);
    }

    /** Rounded limb segment: dark outline, base armor tone, specular streak. */
    private void capsule(Canvas c, float ax, float ay, float bx, float by, float w,
                         int dark, int mid, int light) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(w + 2.8f);
        p.setColor(tint(OUT));
        c.drawLine(ax, ay, bx, by, p);
        p.setStrokeWidth(w);
        p.setColor(tint(mid));
        c.drawLine(ax, ay, bx, by, p);
        float dx = bx - ax, dy = by - ay;
        float len = (float) Math.hypot(dx, dy);
        if (len < .01f) return;
        float nx = -dy / len, ny = dx / len;
        if (ny > 0) { nx = -nx; ny = -ny; } // highlight on the upper side
        p.setStrokeWidth(w * .34f);
        p.setColor(tint(light));
        c.drawLine(ax + nx * w * .22f, ay + ny * w * .22f,
                bx + nx * w * .22f, by + ny * w * .22f, p);
        p.setStrokeWidth(w * .22f);
        p.setColor(tint(dark));
        c.drawLine(ax - nx * w * .3f, ay - ny * w * .3f,
                bx - nx * w * .3f, by - ny * w * .3f, p);
    }

    /** Two-bone IK. Output: knee/elbow in ik[0..1], reachable end point in ik[2..3]. */
    private void solve(float ax, float ay, float tx, float ty, float l1, float l2,
                       float biasX, float biasY) {
        float dx = tx - ax, dy = ty - ay;
        float d = (float) Math.hypot(dx, dy);
        if (d < .001f) { dx = 0; dy = 1; d = 1f; }
        float maxReach = l1 + l2 - .05f, minReach = Math.abs(l1 - l2) + .05f;
        float dd = Math.max(minReach, Math.min(maxReach, d));
        float ux = dx / d, uy = dy / d;
        float ex = ax + ux * dd, ey = ay + uy * dd;
        float a = (l1 * l1 - l2 * l2 + dd * dd) / (2f * dd);
        float h = (float) Math.sqrt(Math.max(0f, l1 * l1 - a * a));
        float nx = -uy, ny = ux;
        if (nx * biasX + ny * biasY < 0f) { nx = -nx; ny = -ny; }
        ik[0] = ax + ux * a + nx * h;
        ik[1] = ay + uy * a + ny * h;
        ik[2] = ex;
        ik[3] = ey;
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
    private static float ease(float t) { t = clamp01(t); return t * t * (3f - 2f * t); }
}
