package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Original, offline synthesized combat sounds. No copyrighted songs, external
 * file downloads or audio permissions. Load/engine failures never crash battle.
 * Short samples are generated and stored locally once in the app cache.
 */
public final class GameAudio {
    public static final int SHOT=0, BLADE=1, SPELL=2, IMPACT=3,
            BUILD=4, VICTORY=5, HURT=6,
            DODGE=7, BOSS_CHARGE=8, BOSS_PHASE=9, WEATHER=10,
            AMBIENT_FOREST=11, AMBIENT_STORM=12, AMBIENT_EMBER=13;
    private static final int RATE=22050;
    private static final int COUNT=14;
    private final Context context;
    private final SharedPreferences prefs;
    private final int[] soundIds=new int[COUNT];
    private final Set<Integer> ready=new HashSet<>();
    private SoundPool pool;
    private boolean initialized=false;
    private long lastSoundMs=0;
    private int desiredAmbient=-1,ambientStream=0,activeAmbient=-1;

    public GameAudio(Context context,SharedPreferences prefs){
        this.context=context.getApplicationContext();
        this.prefs=prefs;
    }
    public boolean enabled(){return prefs.getBoolean("audio.enabled",true);}
    public synchronized boolean toggle(){
        boolean next=!enabled();
        prefs.edit().putBoolean("audio.enabled",next).apply();
        if(!next)stopAmbientStream();
        else if(desiredAmbient>=0){
            prepare();
            startAmbientIfReady();
        }
        return next;
    }
    /** Never initialize an audio device when sound is muted. */
    public synchronized void prepare(){
        if(initialized||!enabled())return;
        initialized=true;
        try{
            AudioAttributes attributes=new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            pool=new SoundPool.Builder().setMaxStreams(5)
                    .setAudioAttributes(attributes).build();
            pool.setOnLoadCompleteListener((sp,id,status)->{
                if(status==0){synchronized(GameAudio.this){
                    ready.add(id);
                    startAmbientIfReady();
                }}
            });
            for(int type=0;type<COUNT;type++){
                File target=new File(context.getCacheDir(),"rg_audio_"+type+".wav");
                if(!target.exists()||target.length()<150) {
                    try(FileOutputStream out=new FileOutputStream(target)){
                        out.write(synthesize(type));
                    }
                }
                soundIds[type]=pool.load(target.getAbsolutePath(),1);
            }
        }catch(RuntimeException | IOException | LinkageError ignored){
            if(pool!=null)pool.release();
            pool=null;ready.clear();
        }
    }
    /** Rate limit repeat fire and impacts so rapid weapons stay readable. */
    public synchronized void play(int type) {
        if(type<0||type>=COUNT||!enabled())return;
        if(!initialized)prepare();
        if(pool==null)return;
        long now=android.os.SystemClock.uptimeMillis();
        if((type==SHOT||type==IMPACT)&&now-lastSoundMs<70)return;
        try{
            int id=soundIds[type];
            if(id!=0&&ready.contains(id)){
                float volume=type==VICTORY?.73f:type==HURT?.54f:.47f;
                pool.play(id,volume,volume,1,0,1f);
                lastSoundMs=now;
            }
        }catch(RuntimeException | LinkageError ignored){
            // Some devices disable system audio; gameplay remains fully functional.
        }
    }
    /** Biome ambience loops only while enabled and gameplay is active.
     * Forest, storm and volcanic layers are original procedural audio. */
    public synchronized void setAmbientBiome(int biome){
        int kind=(biome==3||biome==5||biome==9)?AMBIENT_EMBER:
                (biome==4||biome==7||biome==8)?AMBIENT_STORM:
                        AMBIENT_FOREST;
        desiredAmbient=kind;
        if(!enabled()){stopAmbientStream();return;}
        if(!initialized)prepare();
        startAmbientIfReady();
    }
    private void stopAmbientStream(){
        if(pool!=null&&ambientStream!=0){
            try{pool.stop(ambientStream);}catch(RuntimeException ignored){}
        }
        ambientStream=0;activeAmbient=-1;
    }
    /** Temporarily silence audio on paused/offscreen Android activity. */
    public synchronized void stopAmbient(){stopAmbientStream();}
    private void startAmbientIfReady(){
        if(pool==null||!enabled()||desiredAmbient<0)return;
        if(activeAmbient==desiredAmbient&&ambientStream!=0)return;
        int id=soundIds[desiredAmbient];
        if(id==0||!ready.contains(id))return;
        stopAmbientStream();
        try{
            ambientStream=pool.play(id,.12f,.12f,0,-1,1f);
            if(ambientStream!=0)activeAmbient=desiredAmbient;
        }catch(RuntimeException|LinkageError ignored){
            ambientStream=0;activeAmbient=-1;
        }
    }
    public synchronized void release(){
        stopAmbientStream();
        desiredAmbient=-1;
        if(pool!=null){try{pool.release();}catch(RuntimeException ignored){}}
        pool=null;ready.clear();initialized=false;
    }

    /** 16-bit mono RIFF sample with deterministic tonal/noise layers and decays. */
    public static byte[] synthesize(int type) {
        if(type<0||type>=COUNT)throw new IllegalArgumentException("audio type");
        double[] duration={.17,.29,.39,.14,.33,.55,.26,.22,.70,.95,.30,
                2.40,2.40,2.40};
        double[] pitch={440,205,260,120,290,392,155,720,104,165,310,
                170,83,75};
        double seconds=duration[type];
        int count=(int)(RATE*seconds);
        byte[] wav=new byte[44+count*2];
        setAscii(wav,0,"RIFF");le32(wav,4,wav.length-8);
        setAscii(wav,8,"WAVE");setAscii(wav,12,"fmt ");
        le32(wav,16,16);le16(wav,20,1);le16(wav,22,1);
        le32(wav,24,RATE);le32(wav,28,RATE*2);le16(wav,32,2);le16(wav,34,16);
        setAscii(wav,36,"data");le32(wav,40,count*2);
        int seed=0x0BADC0DE^type*19937;
        for(int i=0;i<count;i++){
            double t=i/(double)RATE,life=1-t/seconds;
            // Original ambience uses matched fade-in/out for seamless loops.
            // Event sounds keep crisp attack/decay without sharp digital clicks.
            boolean ambient=type>=AMBIENT_FOREST;
            double envelope=ambient?(.5-.5*Math.cos(Math.PI*2*t/seconds)):
                    Math.min(1,t*120)*life*life;
            seed^=seed<<13;seed^=seed>>>17;seed^=seed<<5;
            double noise=((seed&0xffff)/32767.5)-1;
            double sweep=(type==SHOT?2.0:type==HURT?.35:type==SPELL?1.1:0.7);
            double freq=pitch[type]*(1+sweep*life);
            double tonal=ambient?
                    Math.sin(2*Math.PI*(pitch[type]*t+
                            Math.sin(2*Math.PI*t/seconds)*.016)):
                    Math.sin(2*Math.PI*(pitch[type]*t+
                            sweep*pitch[type]*seconds*(1-life*life)*.5));
            double overtone=Math.sin(2*Math.PI*freq*t)*.28;
            double hiss=(type==IMPACT||type==SHOT||type==HURT)?.6:
                    ambient?.66:.20;
            double signal=(tonal*.58+overtone*.22+noise*hiss)*envelope*
                    (ambient?.095:.62);
            if(type==VICTORY)
                signal+=Math.sin(2*Math.PI*523.25*t)*.23*envelope;
            if(type==DODGE)
                signal=(noise*.40+Math.sin(2*Math.PI*(800-540*t/seconds)*t)*.30)
                        *envelope*.43;
            if(type==BOSS_CHARGE||type==BOSS_PHASE)
                signal+=(Math.sin(2*Math.PI*(42+120*life)*t)*.26+
                        Math.sin(2*Math.PI*220*t)*.17)*envelope;
            if(type==AMBIENT_FOREST)
                signal+=Math.sin(2*Math.PI*535*t)*.022*envelope;
            else if(type==AMBIENT_STORM)
                signal+=noise*.035*(.4+.6*Math.sin(Math.PI*t/seconds)*
                        Math.sin(Math.PI*t/seconds));
            else if(type==AMBIENT_EMBER)
                signal+=Math.sin(2*Math.PI*56*t)*.022*envelope;
            short pcm=(short)(Math.max(-1,Math.min(1,signal))*32767);
            le16(wav,44+i*2,pcm);
        }
        return wav;
    }
    private static void setAscii(byte[] b,int at,String text){
        for(int i=0;i<text.length();i++)b[at+i]=(byte)text.charAt(i);
    }
    private static void le16(byte[] b,int at,int val){
        b[at]=(byte)val;b[at+1]=(byte)(val>>>8);
    }
    private static void le32(byte[] b,int at,int val){
        b[at]=(byte)val;b[at+1]=(byte)(val>>>8);
        b[at+2]=(byte)(val>>>16);b[at+3]=(byte)(val>>>24);
    }
}
