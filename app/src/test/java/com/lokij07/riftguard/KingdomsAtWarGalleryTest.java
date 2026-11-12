package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/**
 * Saves genuine gameplay frames for design review. No static concept mockups.
 * Screens appear beside the existing gallery in GitHub Actions artifacts.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class KingdomsAtWarGalleryTest {
    private static void set(Object obj,String key,Object value)throws Exception{
        Field field=obj.getClass().getDeclaredField(key);field.setAccessible(true);
        field.set(obj,value);
    }
    private static Object get(Object obj,String key)throws Exception{
        Field field=obj.getClass().getDeclaredField(key);field.setAccessible(true);
        return field.get(obj);
    }
    private static void call(RiftGameView v,String method,Class<?>[]sig,Object...args)
            throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(method,sig);
        m.setAccessible(true);m.invoke(v,args);
    }
    private RiftGameView game(int rank,int stage)throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().putInt("fortress_rank",rank).commit();
        RiftGameView view=new RiftGameView(c);
        view.layout(0,0,1280,720);
        call(view,"start",new Class<?>[]{int.class},stage);
        return view;
    }
    private static void render(RiftGameView g,String name)throws Exception{
        File root=new File(System.getProperty("user.dir"));
        File outDir=new File(root,new File(root,"app/build.gradle").exists()
                ?"app/build/screenshots":"build/screenshots");
        if(!outDir.exists())assertTrue(outDir.mkdirs());
        Bitmap bitmap=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        g.draw(new Canvas(bitmap));
        File path=new File(outDir,name+".png");
        try(FileOutputStream out=new FileOutputStream(path))
            {assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}
        assertTrue(path.length()>4000);
        bitmap.recycle();
    }
    @Test public void actualDodgeEightPadsAndBossPhasesRenderAsGameScreens()
            throws Exception{
        RiftGameView roll=game(1,0);
        set(roll,"axisX",1f);
        call(roll,"triggerDodge",new Class<?>[0]);
        call(roll,"update",new Class<?>[]{float.class},.07f);
        assertTrue((Float)get(roll,"dodgeTime")>0);
        render(roll,"60-v040-warden-actual-dodge");

        RiftGameView towers=game(5,152);
        set(towers,"gold",5000);
        for(int k=0;k<5;k++)call(towers,"build",new Class<?>[0]);
        int[] built=(int[])get(towers,"towers");
        for(int pad=0;pad<8;pad++)assertTrue(built[pad]>0);
        render(towers,"61-v040-eight-playable-towers");

        int finale=Campaign.stage(10,2);
        RiftGameView boss=game(5,finale);
        set(boss,"wave",DifficultyRules.waves(finale,DifficultyRules.NORMAL));
        set(boss,"remaining",1);
        call(boss,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> foes=(List<Object>)get(boss,"enemies");
        Object enemy=foes.get(0);
        float max=(Float)get(enemy,"maxHp");
        set(enemy,"hp",max*.26f);set(enemy,"specialTimer",0f);
        set(enemy,"x",1350f);set(enemy,"y",510f);
        call(boss,"update",new Class<?>[]{float.class},.04f);
        assertEquals(3,get(enemy,"bossPhase"));
        render(boss,"62-v040-three-phase-boss-and-reinforcements");
    }
}
