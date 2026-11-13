package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Runtime sprite-frame cache: same real EnemyRig, lower repeated draw cost. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class EnemySpriteFrameCacheV060Test {
    private static int hash(Bitmap b){
        int r=17;
        for(int y=5;y<b.getHeight()-5;y+=4)
            for(int x=5;x<b.getWidth()-5;x+=4)r=r*31+b.getPixel(x,y);
        return r;
    }
    @Test public void attackClipHasExactlyTenNonwrappingFrames(){
        Set<Integer> values=new HashSet<>();
        for(int i=0;i<10;i++){
            int index=EnemySpriteFrameCache.frame(0,(i+.05f)/10f);
            assertEquals(i,index);values.add(index);
        }
        assertEquals(10,values.size());
        assertEquals(9,EnemySpriteFrameCache.frame(0,1f));
        assertEquals(0,EnemySpriteFrameCache.frame(0,-1f));
        assertEquals(5,EnemySpriteFrameCache.frame((float)Math.PI,-1f));
    }
    @Test public void cachesActualRigAndReusesRenderedSprites()throws Exception {
        EnemySpriteFrameCache cache=new EnemySpriteFrameCache(new EnemyRig());
        try{
            Bitmap first=Bitmap.createBitmap(320,290,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(first);
            c.drawColor(0xff152c3a);
            assertTrue(cache.draw(c,2,4,3,160,264,1.2f,true,.5f,
                    false,-1f,false,1));
            int h=hash(first);
            assertEquals(1,cache.frameBuilds());
            Bitmap again=Bitmap.createBitmap(320,290,Bitmap.Config.ARGB_8888);
            Canvas d=new Canvas(again);d.drawColor(0xff152c3a);
            assertTrue(cache.draw(d,2,4,3,160,264,1.2f,true,.5f,
                    false,-1f,false,1));
            assertEquals(h,hash(again));
            assertEquals(1,cache.frameBuilds());
            assertEquals(1,cache.cachedFrames());
            // Real image proof in the CI gallery.
            File cwd=new File(System.getProperty("user.dir"));
            File dir=new File(cwd,new File(cwd,"app/build.gradle").exists()
                    ?"app/build/screenshots":"build/screenshots");
            if(!dir.isDirectory())assertTrue(dir.mkdirs());
            try(FileOutputStream out=new FileOutputStream(new File(dir,
                    "133-v060-cached-skeletal-enemy.png"))){
                assertTrue(first.compress(Bitmap.CompressFormat.PNG,100,out));
            }
            first.recycle();again.recycle();
        }finally{cache.clear();}
    }
    @Test public void lruMemoryBoundAndStageResetAreEnforced(){
        EnemySpriteFrameCache cache=new EnemySpriteFrameCache(new EnemyRig());
        Bitmap b=Bitmap.createBitmap(360,300,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        try {
            for(int kind=0;kind<7;kind++)
                for(int variant=0;variant<4;variant++)
                    for(int frame=0;frame<4;frame++)
                        assertTrue(cache.draw(c,kind,2,variant,175,265,
                                frame*.61f,true,-1,false,1,false,1));
            assertTrue("Frames must remain bounded",
                    cache.cachedFrames()<=EnemySpriteFrameCache.MAX_CACHED_FRAMES);
            assertTrue("Actually exercised LRU capacity",
                    cache.frameBuilds()>EnemySpriteFrameCache.MAX_CACHED_FRAMES);
        }finally{cache.clear();b.recycle();}
        assertEquals(0,cache.cachedFrames());
        assertFalse(cache.draw(new Canvas(),7,0,0,0,0,0,
                false,-1,false,1,false,1));
    }
}
