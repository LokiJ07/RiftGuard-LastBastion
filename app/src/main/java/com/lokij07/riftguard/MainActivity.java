package com.lokij07.riftguard;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public final class MainActivity extends Activity {
    private RiftGameView game;
    private LokiSplashView splash;
    private boolean closing;
    private final Runnable enterGame=()->{
        if(closing||isFinishing()||game!=null)return;
        game=new RiftGameView(this);
        setContentView(game);
        game.setActive(true);
        splash=null;
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        immersive();
        // Show the real native LokiJ identity before creating the game view.
        // No fake loading percentage and no added network requirement.
        splash=new LokiSplashView(this);
        setContentView(splash);
        splash.postDelayed(enterGame,850);
    }
    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    @Override protected void onResume() { super.onResume(); immersive(); if (game != null) game.setActive(true); }
    @Override protected void onPause() { if (game != null) game.setActive(false); super.onPause(); }
    @Override public void onTrimMemory(int level){
        super.onTrimMemory(level);
        // Android may pressure cached raster bitmaps while the application
        // remains alive. Never reset the actual gameplay/save model.
        if(game!=null&&level>=TRIM_MEMORY_RUNNING_LOW){
            game.trimGraphicCaches(level>=TRIM_MEMORY_RUNNING_CRITICAL);
        }
    }
    @Override public void onLowMemory(){
        super.onLowMemory();
        if(game!=null)game.trimGraphicCaches(true);
    }
    @Override protected void onDestroy(){
        closing=true;
        if(splash!=null)splash.removeCallbacks(enterGame);
        if(game!=null)game.releaseAudio();
        super.onDestroy();
    }
    @Override public void onBackPressed() {
        if (game != null && game.handleBack()) return;
        super.onBackPressed();
    }
}
