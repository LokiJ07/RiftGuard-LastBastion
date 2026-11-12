package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Native Android 8-depth environment inspired by the user's uploaded
 * RiftGuard World and Environment.html, WITHOUT an embedded WebView:
 * sky, far ridge, near ridge, castle cliff, drifting mist, pines/props,
 * lake/reflections and the safe foreground particle veil.
 *
 * 10 authored palette/landmark families x 3 map types x 10 local level seeds
 * produce 300 deterministic stage compositions. They are procedural
 * variations, NOT 300 individually illustrated HD background bitmaps.
 *
 * Keep one stage's five 960x260 ARGB bitmap layers in memory (~5 MB),
 * repaint them ONLY on a stage change, and pan each layer at a different
 * rate with the player's movement. The battle ground and path geometry
 * remain untouched. Camera shake and controls remain screen independent.
 */
public final class LivingBattlefield {
    private LivingBattlefield(boolean unused) {} // utility-only opt-out
    public LivingBattlefield(){}

    public static final int REGIONS=10;
    public static final int DEPTH_LAYERS=8;
    private static final int MAP_W=1900, BACK_H=520;
    private static final int CACHE_W=960,CACHE_H=264;
    private static final int[] SKY_TOP={
            0xff3f78bd,0xff1b3c50,0xff284b3f,0xff2a0e1c,0xff1b2f52,
            0xff26313a,0xff17294c,0xff0c2037,0xff110e29,0xff210c1c};
    private static final int[] SKY_BOTTOM={
            0xfff7e4b5,0xffa1baaa,0xffbfd5a0,0xffffb347,0xffe1f1ff,
            0xffd4a678,0xffbed5ee,0xff929fb5,0xff966e8e,0xffcb6d67};
    private static final int[] FAR={
            0xff8aa9ca,0xff456879,0xff577862,0xff6a2a2a,0xffb4cbe2,
            0xff5b616b,0xff7799c0,0xff3b5879,0xff4b375c,0xff653c55};
    private static final int[] MID={
            0xff6389b2,0xff345660,0xff3e6152,0xff451a20,0xff86a5c8,
            0xff3f4854,0xff506e98,0xff2f3b62,0xff302444,0xff422a3e};
    private static final int[] NEAR={
            0xff446f92,0xff1c3940,0xff304e3d,0xff2d1017,0xff5f80a8,
            0xff30353b,0xff3b527c,0xff23304d,0xff1e142c,0xff27142c};
    private static final int[] ROCK={
            0xff6d6b62,0xff405c56,0xff445645,0xff3a1e22,0xff8fa7c0,
            0xff45484c,0xff4a6886,0xff35485c,0xff34283b,0xff352438};
    private static final int[] STONE={
            0xffd3ccbd,0xff86b4a5,0xff74876d,0xff5a3535,0xffe8f0f8,
            0xff99988a,0xff9dceef,0xff93a6b5,0xff78637c,0xff96606d};
    private static final int[] ROOF={
            0xff3b6fa8,0xff317769,0xff536d43,0xffb13a20,0xff4a7cb6,
            0xff76493a,0xff7158ac,0xff475d91,0xff663b68,0xffa34050};
    private static final int[] WATER={
            0xff58b0d2,0xff79aaa2,0xff73a47c,0xffff6a2a,0xffa5d8ee,
            0xff7a9baa,0xff85e8ff,0xff618cac,0xff876fb8,0xffb65c8a};
    private static final int[] FOREST={
            0xff2b5f45,0xff245045,0xff325f42,0xff26121a,0xff24444f,
            0xff56524a,0xff477e98,0xff315064,0xff302546,0xff432b3c};
    private static final int[] DARK={
            0xff163a2b,0xff112c29,0xff192e25,0xff150a0f,0xff14303a,
            0xff262a2e,0xff27365e,0xff182c40,0xff120e24,0xff170c1b};
    private static final int[] LIGHT={
            0xffffd98a,0xffc7fff1,0xffd9e1a1,0xffffb347,0xffffe3a0,
            0xffffc18e,0xffc8afff,0xffb8cfff,0xffccb1e6,0xffffbf86};

    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap[] layers=new Bitmap[5];
    private int renderedStage=-1;
    private int generation=0;

    public static int environmentSignature(int stage){
        return Campaign.seed(stage); // unique odd-multiplier encoding, all 300 stages
    }
    public static int biome(int stage){return Campaign.world(stage);}
    public static int mapType(int stage){return Campaign.map(stage);}
    /** Stage-local visual seed; each of the 10 levels within a world is distinct. */
    public static int chapterBeat(int stage){return (Campaign.level(stage)-1)%10;}
    public static String stageMood(int stage){
        final String[] mood={"DAWN","MORNING","MIST","GOLDEN HOUR",
                "DUSK","OVERCAST","TWILIGHT","NIGHT","STORM","RIFT SURGE"};
        return mood[chapterBeat(stage)];
    }
    static int mix(int seed,int index){
        int v=seed^index*0x9e3779b9;
        v^=v>>>16;v*=0x7feb352d;v^=v>>>15;
        v*=0x846ca68b;v^=v>>>16;
        return v&0x7fffffff;
    }
    static float noise(int seed,int index){
        return (mix(seed,index)%10000)/10000f;
    }
    private static int tint(int color,int alpha){
        return ((Math.min(255,Math.max(0,alpha)))<<24)|(color&0x00ffffff);
    }
    private static int shift(int c,float scale){
        return Color.rgb(Math.min(255,(int)(Color.red(c)*scale)),
                Math.min(255,(int)(Color.green(c)*scale)),
                Math.min(255,(int)(Color.blue(c)*scale)));
    }
    private void shape(Canvas c,int color,float...v){
        if(v.length<6)return;
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        Path a=new Path();a.moveTo(v[0],v[1]);
        for(int i=2;i<v.length;i+=2)a.lineTo(v[i],v[i+1]);
        a.close();c.drawPath(a,p);
    }
    private void rect(Canvas c,float x,float y,float w,float h,int color){
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        c.drawRect(x,y,x+w,y+h,p);
    }
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width){
        p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(width);
        c.drawLine(x,y,xx,yy,p);p.setStyle(Paint.Style.FILL);
    }
    private void oval(Canvas c,float x,float y,float rx,float ry,int color){
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        c.drawOval(x-rx,y-ry,x+rx,y+ry,p);
    }
    private void buildSky(Canvas c,int world,int stage){
        int beat=chapterBeat(stage);
        // Ten distinct local-level lighting beats, generated once per stage.
        float[] moodBrightness={1.08f,1.16f,.87f,1.09f,.78f,
                .89f,.69f,.47f,.58f,.93f};
        float light=moodBrightness[beat];
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,-10,0,505,
                new int[]{shift(SKY_TOP[world],light),
                        shift(SKY_BOTTOM[world],.76f*light),
                        shift(SKY_BOTTOM[world],light)},
                new float[]{0,.57f,1},Shader.TileMode.CLAMP));
        c.drawRect(-80,-20,1980,BACK_H,p);p.setShader(null);
        int seed=environmentSignature(stage);
        int hue=beat==4?0xffff916d:beat==6?0xffae8dea:
                beat==7?0xff365b9b:beat==8?0xff526779:
                beat==9?0xffcd70e3:0;
        if(hue!=0){
            p.setColor(tint(hue,beat==9?43:68));
            c.drawRect(-80,-20,1980,BACK_H,p);
        }
        float sx=450+noise(seed,2)*1070,sy=56+noise(seed,3)*46;
        int sun=(beat==7||beat==9||world==8||world==9)?
                0xffe3b9f6:0xffffe9b4;
        p.setShader(new RadialGradient(sx,sy,160,
                new int[]{tint(sun,175),tint(sun,26),tint(sun,0)},
                new float[]{0,.32f,1},Shader.TileMode.CLAMP));
        c.drawCircle(sx,sy,160,p);p.setShader(null);
        oval(c,sx,sy,world==9?39:26,world==9?42:26,sun);
        if(world==9){
            oval(c,sx-6,sy-4,32,35,SKY_TOP[world]);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);
            p.setColor(0xfff9b3cb);c.drawCircle(sx,sy,43,p);
            p.setStyle(Paint.Style.FILL);
        }
        if(world>=4||beat>=7){
            for(int k=0;k<45;k++){
                float x=noise(seed,k+62)*1900,y=noise(seed,k+238)*210;
                oval(c,x,y,.7f,.7f,tint(0xfff1f8ff,(int)(80+noise(seed,k+400)*140)));
            }
        }
        for(int k=0;k<7;k++){
            float x=noise(seed,k+18)*1960-30;
            float y=55+noise(seed,k+38)*160;
            oval(c,x,y,63+noise(seed,k+68)*85,7+noise(seed,k+81)*13,
                    tint(SKY_BOTTOM[world],world==3?22:42));
        }
    }
    private void ridge(Canvas c,int stage,int color,float base,float height,int salt){
        int seed=environmentSignature(stage)+salt;
        Path a=new Path();a.moveTo(-90,BACK_H);
        for(int x=-90;x<=2000;x+=36){
            float broad=(float)Math.sin((x+salt)*.0068f+noise(seed,51)*4);
            float sharp=(float)Math.abs(Math.sin((x+salt)*.014f+noise(seed,52)*4));
            float jitter=(noise(seed,300+(x+90)/36)-.5f)*25;
            float y=base-height*(.30f+.38f*sharp+.26f*broad)+jitter;
            a.lineTo(x,y);
        }
        a.lineTo(2000,BACK_H);a.close();
        p.setColor(color);p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,base-height,0,base+70,
                shift(color,1.24f),shift(color,.73f),Shader.TileMode.CLAMP));
        c.drawPath(a,p);p.setShader(null);
        p.setColor(tint(0xffffffff,46));p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);c.drawPath(a,p);p.setStyle(Paint.Style.FILL);
        if(Campaign.world(stage)==4||Campaign.world(stage)==7){
            for(int k=0;k<10;k++){
                float xx=k*206-25;
                float yy=base-height*(.49f+.24f*noise(seed,k+77));
                shape(c,tint(0xfff5fcff,100),xx-19,yy+24,xx,yy-7,xx+25,yy+25);
            }
        }
    }
    private void drawLake(Canvas c,int stage,int world,float time){
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,367,0,525,
                tint(WATER[world],236),tint(DARK[world],196),
                Shader.TileMode.CLAMP));
        c.drawRect(-80,367,1980,520,p);p.setShader(null);
        int seed=environmentSignature(stage);
        for(int i=0;i<46;i++){
            float x=noise(seed,610+i)*1900;
            float y=382+i*2.75f;
            float len=19+(i*29)%96;
            line(c,x,y,x+len,y,tint(SKY_BOTTOM[world],30+i%4*8),1.4f);
        }
    }
    private void drawCastle(Canvas c,int stage,int world){
        int seed=environmentSignature(stage),map=mapType(stage),beat=chapterBeat(stage);
        float cx=915+(noise(seed,75)-.5f)*420;
        float y=262+noise(seed,76)*30;
        int rock=ROCK[world],stone=STONE[world];
        // Long cliff/rock waterfall: silhouettes and lit facets ported from
        // the uploaded HTML castle(c,..) layer.
        shape(c,shift(rock,.62f),cx-285,520,cx-273,y+40,
                cx-170,y+6,cx+180,y+22,cx+290,520);
        shape(c,rock,cx-236,520,cx-218,y+10,cx-95,y+28,
                cx+135,y,cx+215,y+21,cx+258,520);
        shape(c,tint(stone,66),cx-210,y+14,cx-89,y+25,
                cx-145,y+180,cx-260,505);
        for(int i=0;i<22;i++){
            float xx=cx-194+noise(seed,170+i)*398;
            float yy=y+38+noise(seed,190+i)*154;
            line(c,xx,yy,xx+noise(seed,211+i)*19-5,yy+12+noise(seed,231+i)*22,
                    tint(DARK[world],85),2);
        }
        // Waterfall occupies a fixed cliff crevice with staggered spray.
        float fallX=cx-132;
        p.setShader(new LinearGradient(fallX-19,0,fallX+21,0,
                new int[]{tint(WATER[world],70),tint(SKY_BOTTOM[world],210),
                        tint(WATER[world],110)},null,Shader.TileMode.CLAMP));
        c.drawRect(fallX-18,y+43,fallX+18,491,p);p.setShader(null);
        oval(c,fallX,487,57,9,tint(SKY_BOTTOM[world],80));
        // Stone battlement + parapets, carefully behind actual working keep.
        rect(c,cx-174,y-54,348,64,stone);
        rect(c,cx-174,y-54,348,6,shift(stone,1.17f));
        for(int i=0;i<14;i++){
            float bx=cx-169+i*25;
            rect(c,bx,y-65,15,13,shift(stone,.93f));
            line(c,bx,y-54,bx+15,y-54,tint(DARK[world],90),1);
        }
        for(int row=0;row<4;row++)for(int i=0;i<9;i++){
            float bx=cx-162+i*38+(row%2)*18;
            float yy=y-42+row*12;
            line(c,bx,yy,bx+24,yy,tint(ROCK[world],91),1);
        }
        int[] dx={-157,-83,0,88,157};
        for(int i=0;i<5;i++){
            float xx=cx+dx[i],width=(i==2?49:28);
            float high=(i==2?(map==2?193:169):93+(i%2)*24);
            rect(c,xx-width/2,y-high,width,high,stone);
            rect(c,xx-width/2+3,y-high+10,5,high-12,tint(0xfff1ffff,25));
            shape(c,ROOF[world],xx-width*.75f,y-high,
                    xx,y-high-width*(map==2?1.42f:1.03f),
                    xx+width*.75f,y-high);
            for(int j=0;j<(i==2?4:2);j++){
                float yy=y-high+22+j*31;
                rect(c,xx-3,yy,6,11,tint(LIGHT[world],195));
            }
        }
        // Real medieval gate silhouette, not a HUD castle sticker.
        oval(c,cx,y+1,27,41,DARK[world]);
        rect(c,cx-27,y-5,54,29,DARK[world]);
        for(int i=0;i<7;i++){
            line(c,cx-22+i*7,y+14,cx-22+i*7,y+28,
                    tint(stone,80),1);
        }
        if(map==2){
            float portalX=cx+225,portalY=y+22;
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(7);
            p.setColor(tint(LIGHT[world],178));
            c.drawOval(portalX-28,portalY-60,portalX+28,portalY+24,p);
            p.setStyle(Paint.Style.FILL);
        }
        if(map==1){ // Broken Pass: broken battlement & a natural arch.
            line(c,cx+169,y+13,cx+239,y+34,shift(stone,.92f),10);
            line(c,cx+187,y+10,cx+235,y+28,shift(stone,1.19f),3);
        }
        for(int i=0;i<5;i++){
            float rx=cx-154+i*77;
            line(c,rx,y-50,rx,y-81-10*(i%2),tint(DARK[world],220),2);
            shape(c,ROOF[world],rx,y-82,rx+22,y-74,rx,y-63);
        }
        // Slightly taller silhouette every boss/last-stand map.
        if(map==2||beat==9){
            line(c,cx,y-212,cx,y-250,shift(stone,1.15f),4);
            shape(c,ROOF[world],cx,y-251,cx+39,y-243,cx,y-234);
        }
    }
    private void tree(Canvas c,float x,float y,float size,int world,float sway){
        if(world==6){ // Crystal Depths: crystalline monoliths
            shape(c,FOREST[world],x-size*.33f,y,x,y-size*1.13f,
                    x+size*.30f,y-size*.18f,x+size*.13f,y);
            shape(c,tint(LIGHT[world],130),x,y-size*1.13f,
                    x+size*.30f,y-size*.18f,x+size*.10f,y-size*.29f);
            return;
        }
        if(world==5){ // Ironfall: smokestacks / industrial ruins
            rect(c,x-size*.13f,y-size*.85f,size*.26f,size*.85f,ROCK[world]);
            rect(c,x-size*.19f,y-size*.85f,size*.38f,size*.09f,STONE[world]);
            line(c,x-size*.32f,y-size*.25f,x+size*.32f,y-size*.25f,
                    tint(STONE[world],150),4);
            return;
        }
        if(world==2){ // Venom Marsh: hanging ancient willow
            line(c,x,y,x+sway*.12f,y-size*.76f,shift(DARK[world],1.3f),8);
            oval(c,x+sway*.5f,y-size*.76f,size*.40f,size*.22f,FOREST[world]);
            for(int i=-2;i<=2;i++)
                line(c,x+i*size*.13f,y-size*.72f,
                        x+i*size*.14f+sway,y-size*.24f,FOREST[world],3);
            return;
        }
        line(c,x,y,x+sway*.2f,y-size*.49f,shift(DARK[world],1.6f),5);
        for(int j=0;j<4;j++){
            float ty=y-size*(.18f+j*.18f);
            float rad=size*(.30f-j*.047f);
            shape(c,j%2==0?FOREST[world]:shift(FOREST[world],1.2f),
                    x-rad+sway,ty,x+sway*.25f,ty-size*.36f,x+rad+sway,ty);
            if(world==4||world==7)
                line(c,x-rad*.45f,ty-size*.17f,x+rad*.37f,ty-size*.17f,
                        tint(0xffecf7ff,180),2);
        }
    }
    private void drawPines(Canvas c,int stage,int world){
        int seed=environmentSignature(stage);
        for(int i=0;i<31;i++){
            float x=(i*71+noise(seed,i+330)*54)-50;
            float y=365+noise(seed,i+420)*107;
            float height=38+noise(seed,i+440)*91;
            if(Math.abs(x-930)<120&&y>435)continue;
            tree(c,x,y,height,world,0);
        }
        // Cliff-edge thorn trees frame but do not occlude the playable lane.
        for(int i=0;i<7;i++){
            float x=(i%2==0?150:1780)+(noise(seed,500+i)-.5f)*170;
            float y=415+noise(seed,520+i)*63;
            tree(c,x,y,96+noise(seed,540+i)*80,world,0);
        }
    }
    private void fadeLower(Bitmap bitmap){
        int[] row=new int[CACHE_W];
        for(int y=0;y<CACHE_H;y++){
            float worldY=(y+.5f)*BACK_H/CACHE_H;
            if(worldY<=390)continue;
            float factor=Math.max(0,Math.min(1,(512-worldY)/122f));
            bitmap.getPixels(row,0,CACHE_W,0,y,CACHE_W,1);
            for(int x=0;x<CACHE_W;x++){
                int value=row[x];
                int alpha=(int)(((value>>>24)&255)*factor);
                row[x]=(alpha<<24)|(value&0x00ffffff);
            }
            bitmap.setPixels(row,0,CACHE_W,0,y,CACHE_W,1);
        }
    }
    /** 5 stages of independent imagery, one cached atlas per selected mission. */
    public synchronized void prepare(int stage){
        Campaign.checkStage(stage);
        if(stage==renderedStage&&layers[0]!=null)return;
        release();
        int world=biome(stage);
        for(int i=0;i<layers.length;i++){
            Bitmap bitmap=Bitmap.createBitmap(CACHE_W,CACHE_H,
                    Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(bitmap);
            c.scale(CACHE_W/(float)MAP_W,CACHE_H/(float)BACK_H);
            switch(i){
                case 0:buildSky(c,world,stage);break;
                case 1:ridge(c,stage,FAR[world],365,210,37);
                       ridge(c,stage,MID[world],402,161,92);break;
                case 2:ridge(c,stage,NEAR[world],435,101,158);break;
                case 3:drawLake(c,stage,world,0);drawCastle(c,stage,world);break;
                default:drawPines(c,stage,world);break;
            }
            fadeLower(bitmap);
            layers[i]=bitmap;
        }
        renderedStage=stage;
        generation++;
    }
    public int currentStage(){return renderedStage;}
    public int cacheGenerations(){return generation;}
    public int activeBitmapCount(){
        int count=0;for(Bitmap b:layers)if(b!=null&&!b.isRecycled())count++;
        return count;
    }
    public synchronized void release(){
        for(int i=0;i<layers.length;i++){
            if(layers[i]!=null&&!layers[i].isRecycled())layers[i].recycle();
            layers[i]=null;
        }
        renderedStage=-1;
    }
    private void lay(Canvas c,int i,float offset){
        if(layers[i]==null||layers[i].isRecycled())return;
        p.setShader(null);p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);p.setAlpha(255);
        // Extra scale prevents edge gaps when the hero moves.
        c.drawBitmap(layers[i],null,new RectF(-45+offset,0,
                MAP_W+45+offset,BACK_H),p);
    }
    /** Called AFTER textured ground, BEFORE dynamic geometry and enemy actors. */
    public void draw(Canvas c,int stage,float time,float heroX,float heroY,
                     boolean reducedMotion){
        if(stage!=renderedStage||layers[0]==null)prepare(stage);
        float look=reducedMotion?0:
                Math.max(-1,Math.min(1,(heroX-LaneRules.startX())/360f));
        float vertical=reducedMotion?0:
                Math.max(-1,Math.min(1,(heroY-LaneRules.startY())/400f));
        c.save();c.clipRect(0,0,MAP_W,BACK_H);
        lay(c,0,0);
        lay(c,1,-look*12f);
        lay(c,2,-look*26f);
        lay(c,3,-look*43f+vertical*6f);
        if(!reducedMotion){
            int world=biome(stage),seed=environmentSignature(stage);
            // Real parallax mist between cliff and foreground trees.
            for(int i=0;i<7;i++){
                float x=noise(seed,i+690)*1900+
                        (float)Math.sin(time*.18f+i*2.9f)*22f;
                float y=302+noise(seed,i+710)*137;
                p.setStyle(Paint.Style.FILL);
                p.setShader(new RadialGradient(x,y,136,
                        new int[]{tint(SKY_BOTTOM[world],world==2?75:47),
                                tint(SKY_BOTTOM[world],0)},
                        new float[]{0,1},Shader.TileMode.CLAMP));
                c.drawOval(x-136,y-65,x+136,y+65,p);
                p.setShader(null);
            }
        }
        lay(c,4,-look*65f+vertical*12f);
        // An independent storm weather beat even in otherwise sunny worlds.
        if(!reducedMotion&&chapterBeat(stage)==8){
            int stormSeed=environmentSignature(stage);
            for(int i=0;i<25;i++){
                float wx=(noise(stormSeed,870+i)*1900+time*85f)%1900;
                float wy=(noise(stormSeed,900+i)*540+time*160f)%540;
                line(c,wx,wy,wx-12f,wy+18f,0x5599dbe5,1.5f);
            }
        }
        // Actual moving lake reflections, carefully above the water strip only.
        if(!reducedMotion){
            int world=biome(stage),seed=environmentSignature(stage);
            for(int i=0;i<16;i++){
                float x=(noise(seed,i+930)*1890+time*(5+i%3*3))%1900;
                float y=389+noise(seed,i+960)*84;
                line(c,x,y,x+22+i%5*11,y,tint(SKY_BOTTOM[world],65),1.7f);
            }
        }
        c.restore();
    }
    /** Small foreground veil drawn just before HUD, always away from controls. */
    public void drawForeground(Canvas c,int stage,float time,boolean reducedMotion){
        int world=biome(stage);
        if(reducedMotion)return;
        int seed=environmentSignature(stage);
        c.save();c.clipRect(0,0,MAP_W,500);
        for(int i=0;i<24;i++){
            float phase=noise(seed,i+1110)*6.283f;
            float x=(noise(seed,i+1120)*1900+time*(world==3?19:8))%1900;
            float y=82+noise(seed,i+1140)*330+
                    (float)Math.sin(time*1.3f+phase)*17;
            int color=world==3||world==9?0xffffb371:
                    world==4||world==7?0xfff8f9ff:
                    world==6?0xff9ef3ff:0xffcde5b0;
            oval(c,x,y,world==4?2.0f:3.0f,world==7?1.0f:2.1f,
                    tint(color,(int)(55+noise(seed,1160+i)*90)));
        }
        c.restore();
    }
    /** Actual game scene image for mission/map cards, not a hardcoded world number. */
    public void preview(Canvas c,int stage,float left,float top,float width,float height){
        if(stage!=renderedStage||layers[0]==null)prepare(stage);
        c.save();c.translate(left,top);
        c.clipRect(0,0,width,height);
        c.scale(width/(float)MAP_W,height/(float)BACK_H);
        for(int i=0;i<layers.length;i++)lay(c,i,0);
        c.restore();
    }
}
