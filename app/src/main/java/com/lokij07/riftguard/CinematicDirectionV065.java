package com.lokij07.riftguard;

/**
 * Deterministic cinematic shot direction shared by live story scenes and battle
 * outros. All animation is time-based, reversible on replay and independent of
 * save data, gameplay damage or real-world frame rate.
 */
public final class CinematicDirectionV065 {
    private CinematicDirectionV065() {}

    private static float unit(float t) {
        if(!Float.isFinite(t))return 0f;
        return Math.max(0f,Math.min(1f,t));
    }
    public static float ease(float t) {
        t=unit(t);
        return t*t*(3f-2f*t);
    }
    /** Slow, weighty scene openings for lore-critical boss, cave and memory shots. */
    public static float reveal(float seconds,boolean dramatic) {
        return ease(seconds/(dramatic?.72f:.42f));
    }
    /** Initial curtain opacity; all content becomes completely readable. */
    public static int curtainAlpha(float seconds,boolean dramatic) {
        float r=reveal(seconds,dramatic);
        return Math.round(210f*(1f-r)*(1f-r));
    }
    /** Camera push is bounded to prevent cropping subtitles and HUD. */
    public static float storyZoom(float seconds,int beat,boolean memory,boolean titan) {
        float base=titan?1.065f:memory?1.045f:1.025f;
        float sweep=titan?.060f:memory?.034f:.025f;
        return base+sweep*ease(seconds/6.5f);
    }
    /** Reverse-screen shot direction alternates between dialogue lines. */
    public static float storyPanX(float seconds,int beat,boolean memory,boolean titan) {
        float direction=(beat&1)==0?1f:-1f;
        float travel=(titan?31f:memory?21f:15f)*ease(seconds/5f);
        return direction*(3f+travel);
    }
    public static float storyPanY(float seconds,int beat) {
        return (float)Math.sin(seconds*.36f+beat*.79f)*5f;
    }
    /** Actors enter deliberately rather than teleporting into a new shot. */
    public static float actorEntrance(float seconds,boolean fromRight,boolean dramatic) {
        float travel=dramatic?96f:42f;
        float direction=fromRight?1f:-1f;
        return direction*travel*(1f-reveal(seconds,dramatic));
    }
    /** A short subtitle settle prevents captions snapping at scene cuts. */
    public static int subtitleAlpha(float seconds,boolean dramatic) {
        return Math.round(255f*ease((seconds-(dramatic?.16f:.06f))/.30f));
    }
    /** Camera follows the playable Warden on the actual victory walk. */
    public static float outroCameraX(BattleOutroV065 outro,float normalX) {
        if(outro==null)return normalX;
        if(outro.victory) {
            float travel=outro.positionX()-outro.startX;
            return normalX+Math.max(0f,Math.min(390f,travel*.72f));
        }
        // A subtle focus on the fallen Warden, never an abrupt full-screen cut.
        float target=normalX+Math.max(-90f,Math.min(90f,(outro.startX-normalX)*.22f));
        return normalX+(target-normalX)*ease(outro.elapsed()/1.15f);
    }
    public static float outroZoom(BattleOutroV065 outro,float normalZoom) {
        if(outro==null)return normalZoom;
        float extra=outro.victory?.035f:.10f;
        return normalZoom+extra*ease(outro.elapsed()/(outro.victory?2.7f:1.35f));
    }
    /** Letterbox settles in during the outcome before opening a result menu. */
    public static float outroLetterbox(BattleOutroV065 outro) {
        return outro==null?0f:ease(outro.elapsed()/.55f);
    }
    /** Subtitles appear after the important Warden movement/fall is readable. */
    public static float outroTitleAlpha(BattleOutroV065 outro) {
        if(outro==null)return 0f;
        float start=outro.victory?2.05f:.90f;
        return ease((outro.elapsed()-start)/.57f);
    }
    /** Fade down only at the end of the finished cinematic, not mid-walk. */
    public static int outroCurtainAlpha(BattleOutroV065 outro) {
        if(outro==null)return 0;
        float start=outro.duration()-.43f;
        return Math.round(92f*ease((outro.elapsed()-start)/.43f));
    }
}
