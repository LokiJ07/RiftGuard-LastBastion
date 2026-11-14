package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class HudActionFeedbackV067Test {
    @Test public void ringsTrackActualRemainingTimeAndHeldCharge(){
        assertEquals(0f,HudActionFeedbackV067.readyFraction(11,11),.001f);
        assertEquals(.5f,HudActionFeedbackV067.readyFraction(5.5f,11f),.001f);
        assertEquals(1f,HudActionFeedbackV067.readyFraction(0,11),.001f);
        assertEquals(1f,HudActionFeedbackV067.readyFraction(-1,11),.001f);
        assertEquals(0f,HudActionFeedbackV067.chargeFraction(false,10),.001f);
        assertEquals(0f,HudActionFeedbackV067.chargeFraction(true,0),.001f);
        assertEquals(.5f,HudActionFeedbackV067.chargeFraction(true,
                WeaponComboV065.CHARGE_SECONDS*.5f),.001f);
        assertEquals(1f,HudActionFeedbackV067.chargeFraction(true,5),.001f);
        assertEquals(1f,HudActionFeedbackV067.readyFraction(Float.NaN,11),.001f);
    }
    @Test public void movedButtonsKeepRingsOnSameCenterNoExtraTouchLayer(){
        SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences(
                "rg_hud_v067_indicators",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        HudLayoutV065 layout=new HudLayoutV065(prefs);
        layout.move(HudLayoutV065.ATTACK,1020,395);
        layout.move(HudLayoutV065.SKILL,810,360);
        assertEquals(HudLayoutV065.ATTACK,layout.hitControl(
                layout.x(HudLayoutV065.ATTACK),layout.y(HudLayoutV065.ATTACK)));
        assertEquals(HudLayoutV065.SKILL,layout.hitControl(
                layout.x(HudLayoutV065.SKILL),layout.y(HudLayoutV065.SKILL)));
        Bitmap bitmap=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        HudActionFeedbackV067.cooldown(canvas,layout.x(HudLayoutV065.SKILL),
                layout.y(HudLayoutV065.SKILL),layout.radius(HudLayoutV065.SKILL),
                5,10,0xffddc6ff);
        HudActionFeedbackV067.charge(canvas,layout.x(HudLayoutV065.ATTACK),
                layout.y(HudLayoutV065.ATTACK),layout.radius(HudLayoutV065.ATTACK),
                true,WeaponComboV065.CHARGE_SECONDS);
        assertTrue(android.graphics.Color.alpha(bitmap.getPixel(810,305))>0);
        assertTrue(android.graphics.Color.alpha(bitmap.getPixel(1020,318))>0);
        bitmap.recycle();
    }
    @Test public void corruptOldOverlappingCoordinatesFallBackWithoutWipingProgress(){
        SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences(
                "rg_hud_v067_legacy",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("supplies",750).putFloat(
                "hud.v065.control.4.x",849f).putFloat(
                "hud.v065.control.4.y",589f).commit();
        HudLayoutV065 hud=new HudLayoutV065(prefs);
        assertEquals(1130f,hud.x(HudLayoutV065.ATTACK),.01f);
        assertEquals(560f,hud.y(HudLayoutV065.ATTACK),.01f);
        assertEquals(4,hud.hitControl(1130,560));
        assertEquals(750,prefs.getInt("supplies",0));
    }
}
