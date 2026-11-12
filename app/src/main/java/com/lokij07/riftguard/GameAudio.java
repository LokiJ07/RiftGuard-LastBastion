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
            BUILD=4, VICTORY=5, HURT=6;
    public static final int DODGE=7, BOSS_WARN=8, BOSS_PHASE=9,
            TOWER_CANNON=10, TOWER_FROST=11, TOWER_ARC=12, AMBIENT=13,
            EVOLUTION=14;
    private static final int RATE=22050;
    public static final int COUNT=15;
    private final Context context;
    private final SharedPreferences prefs;
    private final int[] soundIds=new int[COUNT];
    private final Set<Integer> ready=new HashSet<>();
    private SoundPool pool;
    private boolean initialized=false;
    private final long[] lastByType=new long[COUNT];

    public GameAudio(Context context,SharedPreferences prefs){
        this.context=context.getApplicationContext();
        this.prefs=prefs;
    }
    public boolean enabled(){return prefs.getBoolean("audio.enabled",true);}
    public boolean toggle(){
        boolean next=!enabled();
        prefs.edit().putBoolean("audio.enabled",next).apply();
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
                if(status==0){synchronized(GameAudio.this){ready.add(id);}}
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
        long cooldown=type==AMBIENT?12000:
                type==EVOLUTION?900:
                type==BOSS_PHASE?700:type==BOSS_WARN?450:
                type==TOWER_CANNON?220:type==TOWER_FROST?180:
                type==TOWER_ARC?190:type==SHOT||type==IMPACT?70:40;
        if(lastByType[type]>0&&now-lastByType[type]<cooldown)return;
        try{
            int id=soundIds[type];
            if(id!=0&&ready.contains(id)){
                float volume=type==VICTORY?.73f:type==HURT?.54f:
                        type==AMBIENT?.26f:type==EVOLUTION?.62f:
                        type==BOSS_PHASE?.70f:.47f;
                pool.play(id,volume,volume,1,0,1f);
                lastByType[type]=now;
            }
        }catch(RuntimeException | LinkageError ignored){
            // Some devices disable system audio; gameplay remains fully functional.
        }
    }
    public synchronized void release(){
        if(pool!=null){try{pool.release();}catch(RuntimeException ignored){}}
        pool=null;ready.clear();initialized=false;
    }

    /** 16-bit mono RIFF sample with deterministic tonal/noise layers and decays. */
    public static byte[] synthesize(int type) {
        if(type<0||type>=COUNT)throw new IllegalArgumentException("audio type");
        double[] duration={.17,.29,.39,.14,.33,.55,.26,
                .24,.46,.66,.36,.34,.37,1.15,.88};
        double[] pitch={440,205,260,120,290,392,155,
                360,115,185,95,560,340,147,330};
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
            // Smooth start avoids clicks; zero-crossing window avoids abrupt tail.
            double envelope=Math.min(1,t*120)*life*life;
            seed^=seed<<13;seed^=seed>>>17;seed^=seed<<5;
            double noise=((seed&0xffff)/32767.5)-1;
            double sweep=(type==SHOT?2.0:type==HURT?.35:type==SPELL?1.1:
                    type==DODGE?1.6:type==BOSS_PHASE?-.4:
                    type==TOWER_CANNON?.27:type==TOWER_ARC?2.5:.7);
            double freq=pitch[type]*(1+sweep*life);
            double tonal=Math.sin(2*Math.PI*(pitch[type]*t+
                    sweep*pitch[type]*seconds*(1-life*life)*.5));
            double overtone=Math.sin(2*Math.PI*freq*t)*.28;
            double hiss=(type==IMPACT||type==SHOT||type==HURT||
                    type==TOWER_CANNON)?.6:type==DODGE?.46:
                    type==AMBIENT?.11:.20;
            double signal=(tonal*.58+overtone*.22+noise*hiss)*envelope*.62;
            if(type==VICTORY)
                signal+=Math.sin(2*Math.PI*523.25*t)*.23*envelope;
            if(type==BOSS_PHASE||type==BOSS_WARN)
                signal+=Math.sin(2*Math.PI*(pitch[type]*.5)*t)*.18*envelope;
            if(type==EVOLUTION){
                // Distinct original ascending three-tone heraldic confirmation.
                // Not a copied melody, not dependent on downloaded music.
                double note=t<seconds/3?330:t<seconds*2/3?440:660;
                signal=(Math.sin(2*Math.PI*note*t)*.38+
                        Math.sin(2*Math.PI*note*1.5*t)*.13+
                        Math.sin(2*Math.PI*note*2*t)*.08)*envelope;
            }
            if(type==TOWER_ARC)
                signal+=Math.sin(2*Math.PI*(pitch[type]*3)*t)*.10*envelope;
            if(type==TOWER_FROST)
                signal+=Math.sin(2*Math.PI*(pitch[type]*1.5)*t)*.14*envelope;
            if(type==AMBIENT) {
                // Low, unobtrusive atmospheric drone played infrequently,
                // fully original synthesis, not a copied song or soundtrack.
                double soft=Math.sin(2*Math.PI*147*t)*.14+
                        Math.sin(2*Math.PI*196*t)*.09+
                        noise*.028;
                signal=soft*Math.min(1,t*3)*Math.min(1,(seconds-t)*3);
            }
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
