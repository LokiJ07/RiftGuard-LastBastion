package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Thirty original 2D district silhouettes for the 300-stage campaign.
 *
 * The four HTML demo environments inspired the layered composition and
 * colors; this extra scenery is authored for native Android Canvas, not an
 * embedded HTML WebView or 300 copies of the same static background.
 *
 * Ten biomes x three evolving chapters per biome. The A/B/C mission castle,
 * route, weather and deterministic stage seeds remain independent.
 *
 * Important: render ONLY into an offscreen landscape bitmap when the stage
 * changes, never as per-frame heavy geometry. Keep below y=480 to prevent
 * overlap with the live combat path (approximately y=510+).
 */
public final class EnvironmentDistricts {
    private EnvironmentDistricts(){}
    public static final int DISTRICTS=30;
    private static final String[][] NAMES={
        {"Mossfall Glade","Sunroot Canopy","Highwatch Cliffs"},
        {"Wisp Lanterns","Elderwood Ruins","Moonbough Sanctuary"},
        {"Sunken Reeds","Drowned Courtyard","Venomwatch Temple"},
        {"Cinder Foothills","Basalt Aqueduct","Inferno Crucible"},
        {"Snowveil Pines","Frozen Viaduct","Glacier Sanctum"},
        {"Rustfield Towers","Broken Railworks","Forgeheart Citadel"},
        {"Prism Thicket","Crystal Causeway","Astral Geode"},
        {"Thunderhead Ridge","Stormwind Bridge","Tempest Beacon"},
        {"Raven Barrows","Fallen Cathedral","Nightshade Gate"},
        {"Bloodmoon Spires","Eclipse Causeway","Last Rift Gateway"}
    };
    private static final int[] STONE={
        0xff829d7d,0xff758b7b,0xff708b79,0xff674a4c,0xffafc7df,
        0xff7b888c,0xff8196cc,0xff7c91b5,0xff77658d,0xff9a657f
    };
    private static final int[] DARK={
        0xff254a3a,0xff193c34,0xff2a4644,0xff3b2024,0xff456479,
        0xff414850,0xff36496b,0xff29405b,0xff292139,0xff281a37
    };
    private static final int[] GLOW={
        0xffdbeea7,0xffa4e8dc,0xffa8ea9b,0xffff9a58,0xffd6f6ff,
        0xffffb176,0xff9be6ff,0xffa7c7ff,0xffbc9ae2,0xffff79ab
    };
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);

    public static int phase(int stage){
        Campaign.checkStage(stage);
        // Three real subregions across each 10-level world, not just
        // a new name for each map A/B/C: levels 1-3, 4-6, 7-10.
        return ((Campaign.level(stage)-1)%10)*3/10;
    }
    public static int district(int stage){
        return Campaign.world(stage)*3+phase(stage);
    }
    public static String name(int stage){
        return NAMES[Campaign.world(stage)][phase(stage)];
    }
    public static String[] namesForWorld(int world){
        if(world<0||world>=NAMES.length)
            throw new IllegalArgumentException("world");
        return NAMES[world].clone();
    }
    private static int alpha(int color,int opacity){
        return ((Math.max(0,Math.min(255,opacity)))<<24)|(color&0xffffff);
    }
    private static int lighter(int color,float factor){
        int r=Math.min(255,Math.round((color>>>16&255)*factor));
        int g=Math.min(255,Math.round((color>>>8&255)*factor));
        int b=Math.min(255,Math.round((color&255)*factor));
        return 0xff000000|(r<<16)|(g<<8)|b;
    }
    private static void oval(Canvas c,float x,float y,float rx,float ry,int color){
        P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(color);
        c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void rect(Canvas c,float x,float y,float w,float h,int color){
        P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(color);
        c.drawRect(x,y,x+w,y+h,P);
    }
    private static void line(Canvas c,float x,float y,float x2,float y2,int color,float width){
        P.setShader(null);P.setStyle(Paint.Style.STROKE);P.setColor(color);
        P.setStrokeCap(Paint.Cap.ROUND);P.setStrokeWidth(width);
        c.drawLine(x,y,x2,y2,P);
        P.setStyle(Paint.Style.FILL);
    }
    private static void shape(Canvas c,int color,float...pts){
        if(pts.length<6)return;
        Path path=new Path();path.moveTo(pts[0],pts[1]);
        for(int j=2;j<pts.length;j+=2)path.lineTo(pts[j],pts[j+1]);
        path.close();P.setShader(null);P.setStyle(Paint.Style.FILL);
        P.setColor(color);c.drawPath(path,P);
    }
    /** A forest elder tree or, for hostile realms, a dangerous standing spire. */
    private static void wild(Canvas c,int world,float x,float y,float k,int seed){
        c.save();c.translate(x,y);c.scale(k,k);
        int dark=DARK[world],stone=STONE[world],glow=GLOW[world];
        if(world==0||world==1||world==2||world==8){
            // Silhouette with separated branches; no untextured circle crown.
            Path trunk=new Path();
            trunk.moveTo(-13,0);trunk.cubicTo(-26,-53,-8,-98,-21,-141);
            trunk.lineTo(-8,-190);trunk.lineTo(13,-160);
            trunk.cubicTo(25,-99,8,-55,20,0);trunk.close();
            P.setColor(dark);P.setStyle(Paint.Style.FILL);c.drawPath(trunk,P);
            for(int n=0;n<5;n++){
                float yy=-171+n*18;
                line(c,0,yy,(n%2==0?-1:1)*(74-n*8),yy-44,
                        lighter(dark,1.12f),9-n*.5f);
            }
            for(int i=0;i<6;i++){
                float px=(i%2==0?-1:1)*(28+(i*13)%43);
                float py=-173+(i/2)*32;
                oval(c,px,py,34+(i%3)*5,18+(i%2)*7,alpha(stone,182));
                oval(c,px+8,py-9,16,6,alpha(glow,44));
            }
            if(world==2){
                for(int i=0;i<8;i++){
                    float xx=-67+i*18;
                    line(c,xx,-115+(i%3)*15,xx+6,-43,
                            alpha(glow,88),2);
                }
            }
        }else{
            for(int i=-1;i<=1;i++){
                float sx=i*44;
                float size=120+((seed+i*13)&31);
                shape(c,lighter(dark,1.08f),sx-19,0,sx-16,-size*.75f,
                        sx,-size,sx+19,-size*.62f,sx+24,0);
                shape(c,alpha(stone,183),sx-11,-size*.72f,
                        sx,-size,sx+8,-size*.60f,sx+6,0);
                if(world==3||world==9)
                    line(c,sx-4,-size*.45f,sx+5,-size*.66f,
                            alpha(glow,190),3);
                if(world==4)
                    shape(c,alpha(0xfff6fdff,230),sx-15,-size*.77f,
                            sx,-size,sx+12,-size*.71f);
                if(world==6)
                    shape(c,alpha(glow,173),sx,-size,sx+12,-size*.64f,
                            sx+3,-size*.48f);
            }
            if(world==5){
                // Industrial chimneys, rivets and slow smoke silhouette.
                for(int i=0;i<3;i++){
                    float xx=i*31-30;
                    rect(c,xx,-117-(i%2)*26,18,132+(i%2)*26,stone);
                    rect(c,xx-4,-125-(i%2)*26,27,10,dark);
                }
            }
            if(world==7){
                for(int i=-1;i<=1;i++)
                    line(c,i*43,-137,i*43+12,-185,alpha(glow,170),3);
            }
        }
        c.restore();
    }
    /** Ancient bridge/aqueduct; cracked and differently adorned by biome. */
    private static void crossing(Canvas c,int world,float x,float y,float k,int seed){
        c.save();c.translate(x,y);c.scale(k,k);
        int stone=STONE[world],dark=DARK[world],glow=GLOW[world];
        rect(c,-134,-81,39,91,stone);
        rect(c,97,-81,39,91,stone);
        rect(c,-136,-91,46,12,lighter(stone,1.15f));
        rect(c,91,-91,48,12,lighter(stone,1.15f));
        shape(c,dark,-96,-44,-78,-91,82,-91,98,-43,97,-16,-95,-16);
        shape(c,lighter(stone,1.12f),-90,-88,-54,-107,58,-107,91,-88,
                74,-75,-75,-75);
        // Central arch opening is a real cut-out silhouette, not a flat bar.
        oval(c,0,-44,69,56,alpha(dark,235));
        rect(c,-67,-43,134,53,alpha(dark,235));
        for(int j=0;j<11;j++){
            float xx=-125+j*25;
            line(c,xx,-80,xx+2,-96,alpha(dark,155),1.7f);
        }
        for(int j=0;j<5;j++){
            float xx=-129+j*64;
            rect(c,xx,-102,19,15,lighter(stone,.87f));
        }
        if(world==0||world==1||world==2){
            for(int j=0;j<9;j++){
                float xx=-117+j*30;
                line(c,xx,-28,xx-3,-10-(j%3)*7,
                        alpha(GLOW[world],120),2);
            }
        }else if(world==3||world==9){
            for(int j=0;j<5;j++)
                line(c,-118+j*54,-75,-100+j*54,-63,
                        alpha(glow,170),3.4f);
        }else if(world==4){
            for(int j=0;j<5;j++)
                shape(c,alpha(0xffeafcff,203),-121+j*59,-84,
                        -112+j*59,-62,-99+j*59,-84);
        }else if(world==6||world==7){
            for(int j=0;j<3;j++){
                float xx=-103+j*97;
                shape(c,alpha(glow,157),xx,-111,xx+12,-128,
                        xx+20,-111);
            }
        }else if(world==5){
            for(int j=0;j<3;j++){
                oval(c,-102+j*95,-90,8,8,dark);
                oval(c,-102+j*95,-90,3,3,alpha(glow,214));
            }
        }
        c.restore();
    }
    /** World-specific portal/gate for the final third of each realm. */
    private static void gateway(Canvas c,int world,float x,float y,float k,int seed){
        c.save();c.translate(x,y);c.scale(k,k);
        int stone=STONE[world],dark=DARK[world],glow=GLOW[world];
        for(int side=-1;side<=1;side+=2){
            float xx=side*80;
            shape(c,stone,xx-20,0,xx-26,-129,
                    xx,-170,xx+24,-130,xx+20,0);
            shape(c,lighter(stone,1.2f),xx-8,-150,
                    xx,-170,xx+8,-137,xx+8,-21);
        }
        // Dark portal interior + physically rimmed arch with rune sockets.
        P.setStyle(Paint.Style.FILL);P.setColor(alpha(dark,241));
        c.drawOval(-73,-153,73,16,P);
        P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(11);
        P.setColor(alpha(stone,242));
        c.drawOval(-79,-159,79,18,P);
        P.setStrokeWidth(3.6f);P.setColor(alpha(glow,213));
        c.drawOval(-70,-151,70,13,P);
        P.setStyle(Paint.Style.FILL);
        // Light originates from INSIDE portal rather than an opaque disk.
        P.setShader(new RadialGradient(0,-65,65,
                new int[]{alpha(glow,76),alpha(glow,12),alpha(glow,0)},
                new float[]{0,.57f,1},Shader.TileMode.CLAMP));
        c.drawOval(-61,-145,61,9,P);P.setShader(null);
        for(int i=0;i<9;i++){
            float theta=(float)(Math.PI*2*i/9);
            float xx=(float)Math.cos(theta)*74;
            float yy=-72+(float)Math.sin(theta)*81;
            oval(c,xx,yy,3.1f,5.0f,alpha(glow,170));
        }
        if(world==3||world==9){
            for(int i=-1;i<=1;i++)
                shape(c,alpha(glow,158),i*48-10,-15,
                        i*48+7,-41,i*48+21,-10);
        }else if(world==4){
            shape(c,alpha(0xffeff9ff,186),-35,-120,0,-159,
                    35,-120);
        }else if(world==6){
            for(int i=-1;i<=1;i++)
                shape(c,alpha(glow,150),i*37-10,-16,
                        i*37,-43,i*37+11,-16);
        }else if(world==8){
            for(int i=-1;i<=1;i++)line(c,i*39,-9,i*39-15,-29,
                    alpha(stone,155),2);
        }else if(world==1){
            oval(c,0,-67,13,13,alpha(glow,120));
        }
        c.restore();
    }

    public static void draw(Canvas c,int stage){
        int world=Campaign.world(stage),phase=phase(stage);
        int seed=Campaign.seed(stage);
        // Intentionally behind the player's combat lane and the real keep.
        // The 3 district types change the silhouette, not only the tint.
        c.save();
        c.clipRect(0,0,1900,490);
        for(int i=0;i<3;i++){
            float x=300+i*635+(LivingBattlefield.noise(seed,123+i)-.5f)*92;
            float y=438+(LivingBattlefield.noise(seed,142+i)-.5f)*18;
            float scale=(.68f+(i==1?.25f:0)+
                    LivingBattlefield.noise(seed,160+i)*.12f);
            if(phase==0)wild(c,world,x,y,scale,seed+i);
            else if(phase==1)crossing(c,world,x,y,scale,seed+i);
            else gateway(c,world,x,y,scale,seed+i);
        }
        c.restore();
    }
}
