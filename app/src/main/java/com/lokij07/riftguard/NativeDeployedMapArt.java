package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Native Android Java port of the user-supplied deployed-map-generator.html.
 * This is a new ground/world renderer, NOT a recolor of LivingBattlefield.
 *
 * The HTML's scenic vocabulary (ground palette, distant angular hills,
 * mixed painted pine/broadleaf/rock/crystal/bush/stele/dead props, curved
 * pale roads, metallic tower pads, wooden fortress and mission A/B/C layouts)
 * is reproduced using cached Android Canvas drawing.
 *
 * Real lane geometry remains governed by LaneRules.stagePathY() so enemy AI,
 * route exclusion, ranged hits, and turret combat still match the drawn road.
 * No HTML / WebView, per-frame bitmap creation, or decorative collision.
 */
public final class NativeDeployedMapArt {
    public static final int W=1900,H=1050;
    private static final int[] GROUND={
        0xff5a8f6a,0xff4a8a6a,0xff7a8f58,0xff8a5040,0xff7a98a8,
        0xff8a9098,0xff5a4a8a,0xff6a9fb0,0xff4a4261,0xff7a8a66};
    private static final int[] DARK={
        0xff2f6b4a,0xff24523f,0xff475a34,0xff4a2a22,0xff536c79,
        0xff4a5058,0xff2a2050,0xff386878,0xff2a2050,0xff3a4a3a};
    private static final int[] HILL={
        0xff7fa9c8,0xff6a9a9a,0xff8a9a70,0xff9a5a48,0xffd4e6f2,
        0xff9aa4b4,0xff6a5a9a,0xff8ab4cc,0xff6a5a9a,0xff9ab0a0};
    private static final int[] LEAF={
        0xff2f8a62,0xff1f7a58,0xff5a7a3a,0xff7a4a2a,0xff3a7f78,
        0xff4a6a60,0xff4a3a8a,0xff3a7f88,0xff483672,0xff3a7a5a};
    private static final int[] GLOW={
        0xfff4d35e,0xffa8f0c8,0xffd8e890,0xffff9a4a,0xffbfeaff,
        0xffc0c8d8,0xffe0a8ff,0xffbfeaff,0xffe0a8ff,0xffffe08a};
    // prop families in exact order of source HTML: pine, tree, rock, crystal,
    // bush, stone stele, dead tree. Per-world weights adapted for RiftGuard.
    private static final int[][] MIX={
        {26,10,10,2,10,4,0},{40,14,8,1,8,3,0},
        {14,18,6,1,16,3,6},{4,0,24,10,3,4,26},
        {34,0,14,10,2,3,4},{14,0,30,4,2,6,2},
        {12,4,12,22,4,6,10},{16,4,18,3,8,2,0},
        {12,4,12,22,4,6,10},{22,10,12,3,8,10,0}};
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private Bitmap cached;
    private int stage=-1, builds=0;
    private final LinkedHashMap<Integer,Bitmap> thumbnails=new LinkedHashMap<>(12,.75f,true);

    public int preparedStage(){return stage;}
    public int cacheBuilds(){return builds;}
    public int thumbnailCount(){return thumbnails.size();}
    public static int world(int stage){return Campaign.world(stage);}
    public static int groundColor(int stage){return GROUND[world(stage)];}
    public static int signature(int stage){return Campaign.seed(stage);}
    private static int clamp(float a){return (int)Math.max(0,Math.min(255,a));}
    private static int shade(int col,float scale){
        return Color.rgb(clamp(Color.red(col)*scale),clamp(Color.green(col)*scale),
                clamp(Color.blue(col)*scale));
    }
    private static int alpha(int col,int a){return (Math.max(0,Math.min(255,a))<<24)|(col&0xffffff);}
    private static void fill(Canvas c,int color){
        P.setStyle(Paint.Style.FILL);P.setShader(null);P.setColor(color);
    }
    private static void rect(Canvas c,float l,float t,float r,float b,int color){
        fill(c,color);c.drawRect(l,t,r,b,P);
    }
    private static void oval(Canvas c,float cx,float cy,float rx,float ry,int color){
        fill(c,color);c.drawOval(cx-rx,cy-ry,cx+rx,cy+ry,P);
    }
    private static void line(Canvas c,float x,float y,float x2,float y2,float width,int color){
        fill(c,color);P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(width);
        P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,x2,y2,P);
        P.setStyle(Paint.Style.FILL);
    }
    private static void poly(Canvas c,int col,float... pts){
        Path a=new Path();a.moveTo(pts[0],pts[1]);
        for(int i=2;i<pts.length;i+=2)a.lineTo(pts[i],pts[i+1]);
        a.close();fill(c,col);c.drawPath(a,P);
    }
    private static float sq(float x){return x*x;}
    private static float randomScale(Random r){return .82f+r.nextFloat()*.70f;}

    /** Native equivalent of HTML prop(c,k,x,y,s,R,r), with stable stage RNG. */
    private static void prop(Canvas c,int kind,float x,float y,float size,int world,Random r){
        int col=LEAF[world], dark=DARK[world],glow=GLOW[world];
        c.save();c.translate(x,y);c.scale(size,size);
        oval(c,4,6,19,5,0x330a241c);
        switch(kind){
            case 0: // three-tier split-color conifer
                rect(c,-3,-28,3,3,0xff604f3c);
                for(int i=0;i<3;i++){
                    float w=22-i*5,base=-9-i*18,top=base-36;
                    poly(c,shade(col,1.15f),-w,base,0,top,0,base);
                    poly(c,shade(col,.72f),0,top,w,base,0,base);
                }
                break;
            case 1: // layered broadleaf canopy, never an empty circle
                rect(c,-4,-29,4,3,0xff5a3f2a);
                int[] dx={-13,12,0,-20,17};int[] dy={-36,-35,-48,-27,-25};
                for(int i=0;i<dx.length;i++){
                    oval(c,dx[i],dy[i],17+(i%3)*4,15,
                            shade(col,i%2==0?1.12f:.85f));
                }
                for(int i=0;i<4;i++){
                    float qx=(r.nextFloat()-.5f)*44,qy=-29-r.nextFloat()*29;
                    oval(c,qx,qy,2,2,glow);
                }
                break;
            case 2: // top lit angular field stones
                float hw=21+r.nextFloat()*8;
                poly(c,shade(dark,1.40f),-hw,0,-hw*.75f,-12,
                        hw*.58f,-17,hw,0);
                poly(c,shade(HILL[world],1.05f),-hw*.75f,-12,
                        -hw*.12f,-24,hw*.58f,-17,hw*.95f,-3);
                break;
            case 3:
                for(int i=-1;i<=1;i++){
                    float px=i*10,ht=21+r.nextFloat()*26;
                    poly(c,glow,px-7,0,px,-ht,px,0);
                    poly(c,shade(glow,.69f),px,-ht,px+7,0,px,0);
                }
                break;
            case 4:
                for(int i=-2;i<=2;i++)
                    oval(c,i*8,-5-r.nextFloat()*7,10,9,shade(col,i%2==0?1.16f:.9f));
                for(int i=0;i<4;i++)oval(c,(r.nextFloat()-.5f)*40,
                        -4-r.nextFloat()*13,1.7f,1.7f,glow);
                break;
            case 5: // carved ancient marker
                rect(c,-10,-29,10,0,shade(HILL[world],.78f));
                rect(c,-10,-29,10,-25,shade(HILL[world],1.24f));
                for(int i=0;i<3;i++)line(c,-8,-23+i*9,4,-21+i*9,2,
                        shade(dark,1.04f));
                break;
            default: // broken trees and desolation
                line(c,0,0,2,-42,5,0xff503f32);
                line(c,2,-29,-17,-40,3,0xff503f32);
                line(c,2,-35,20,-49,3,0xff503f32);
                line(c,0,-17,14,-26,2,0xff503f32);
        }
        c.restore();
    }
    public synchronized void prepare(int selected){
        Campaign.checkStage(selected);
        if(stage==selected&&cached!=null&&!cached.isRecycled())return;
        if(cached!=null&&!cached.isRecycled())cached.recycle();
        cached=Bitmap.createBitmap(W,H,Bitmap.Config.RGB_565);
        Canvas c=new Canvas(cached);
        paintScene(c,selected);
        stage=selected;builds++;
    }
    public synchronized void release(){
        if(cached!=null&&!cached.isRecycled())cached.recycle();
        cached=null;stage=-1;
        for(Bitmap b:thumbnails.values())if(b!=null&&!b.isRecycled())b.recycle();
        thumbnails.clear();
    }
    private static void paintScene(Canvas c,int stage){
        int world=world(stage),seed=signature(stage),variant=Campaign.map(stage);
        Random r=new Random(((long)seed<<15)^0x31ac92bdL);
        // HTML's layered ground gradients; old LivingBattlefield castle/lake
        // is deliberately NOT drawn over this flat, legible top-down landscape.
        P.setShader(new LinearGradient(0,0,0,H,shade(GROUND[world],1.12f),
                shade(DARK[world],.93f),Shader.TileMode.CLAMP));
        c.drawRect(0,0,W,H,P);P.setShader(null);
        // Geometric ridge silhouettes from the HTML, scaled to world width.
        for(int layer=0;layer<3;layer++){
            Path mountain=new Path();
            float base=255+layer*95;
            mountain.moveTo(0,base+80);
            for(int x=-110;x<W+100;x+=132){
                float top=base-72-r.nextFloat()*105;
                mountain.lineTo(x,top);
                mountain.lineTo(x+66,base-12);
            }
            mountain.lineTo(W,base+110);mountain.close();
            fill(c,alpha(HILL[world],138+layer*25));c.drawPath(mountain,P);
        }
        // distant fortress silhouette, as in HTML generator; small and far
        float citadelX=1160+(seed%7-3)*47;
        poly(c,shade(DARK[world],1.15f),citadelX-94,286,citadelX-59,158,
                citadelX+52,149,citadelX+96,286);
        rect(c,citadelX-54,170,citadelX+61,205,shade(HILL[world],1.28f));
        for(int k=0;k<7;k++)rect(c,citadelX-55+k*18,160,citadelX-45+k*18,171,
                shade(HILL[world],1.28f));
        // Source HTML uses thousands of tiny blade strokes but no static grid.
        for(int i=0;i<1250;i++){
            float x=r.nextFloat()*W,y=290+r.nextFloat()*750;
            int col=i%3==0?0x227affbb:0x3371aa80;
            line(c,x,y,x+2+(i%3),y-4-r.nextFloat()*5,.8f,col);
        }
        // Kingdom-specific architecture is physically seeded in the live terrain.
        KingdomSetPieces.paint(c,stage);
        // Mission B: river + an actual road bridge, following HTML.
        if(variant==1){
            Path river=new Path();river.moveTo(1160,-55);
            river.cubicTo(1060,255,1265,475,1155,1080);
            fill(c,0xff335969);P.setStyle(Paint.Style.STROKE);
            P.setStrokeWidth(74);c.drawPath(river,P);
            P.setColor(world==3?0xffee763d:world==8?0xff9e76dc:0xff67aaca);
            P.setStrokeWidth(58);c.drawPath(river,P);
            P.setColor(0x6695e7f6);P.setStrokeWidth(6);c.drawPath(river,P);
            P.setStyle(Paint.Style.FILL);
        }
        // The same curved LaneRules stage geometry is used by enemy pathing.
        Path lane=new Path();lane.moveTo(LaneRules.CASTLE_GATE_X-22,
                LaneRules.stagePathY(stage,LaneRules.CASTLE_GATE_X));
        for(int x=(int)LaneRules.CASTLE_GATE_X;x<W+50;x+=13)
            lane.lineTo(x,LaneRules.stagePathY(stage,x));
        fill(c,0xff354c3e);P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(100);P.setStrokeCap(Paint.Cap.ROUND);c.drawPath(lane,P);
        P.setColor(0xffb5d0bf);P.setStrokeWidth(82);c.drawPath(lane,P);
        P.setColor(0xffd9e8dd);P.setStrokeWidth(68);c.drawPath(lane,P);
        P.setColor(0xfffbfff3);P.setStrokeWidth(3);
        for(int x=(int)LaneRules.CASTLE_GATE_X+20;x<W-8;x+=25){
            float y=LaneRules.stagePathY(stage,x);
            c.drawLine(x,y,x+9,LaneRules.stagePathY(stage,x+9),P);
        }
        P.setStyle(Paint.Style.FILL);
        // Bridge wood joints span the real crossing, not an unrelated river.
        if(variant==1)for(int i=0;i<9;i++){
            float xx=1160+i*8,y=LaneRules.stagePathY(stage,xx);
            line(c,xx,y-51,xx,y+51,7,0xffa17b53);
        }
        // Three distinct mission types: more hazards and props on last stand.
        if(variant==2)for(int i=0;i<7;i++){
            float xx=1150+i*85,yy=310+(i%2)*370;
            poly(c,shade(DARK[world],.70f),xx-19,yy,xx+4,yy-28,xx+23,yy+1);
            line(c,xx-16,yy,xx+19,yy-13,3,GLOW[world]);
        }
        // Terrain props never cover the stage's ACTUAL 5–8 playable pads.
        final int activePads=TowerPadLayout.count(stage);
        final float[] padsX=new float[activePads],padsY=new float[activePads];
        for(int slot=0;slot<activePads;slot++){
            padsX[slot]=TowerPadLayout.x(stage,slot);
            padsY[slot]=TowerPadLayout.y(stage,slot);
        }
        int[] mix=MIX[world];
        for(int family=0;family<7;family++){
            int amount=mix[family]*(family==3?1:3);
            for(int placed=0,tries=0;placed<amount&&tries<amount*38;tries++){
                float x=50+r.nextFloat()*1810;
                float y=245+r.nextFloat()*760;
                if(LaneRules.stageCoversPath(stage,x,y)||sq(x-950)+sq(y-510)<sq(230)
                   ||sq(x-1234)+sq(y-614)<sq(100))continue;
                boolean nearPad=false;
                for(int k=0;k<activePads;k++)if(sq(x-padsX[k])+sq(y-padsY[k])<sq(74))
                    nearPad=true;
                if(nearPad)continue;
                prop(c,family,x,y,randomScale(r),world,r);
                placed++;
            }
        }
        // Each drawn construction pad is functional: matches the combat layout.
        for(int i=0;i<activePads;i++)pad(c,padsX[i],padsY[i]);
        // A painted core-footprint shadow holds the existing interactive fort.
        oval(c,950,615,161,26,0x550b291d);
    }
    private static void pad(Canvas c,float x,float y){
        oval(c,x+4,y+24,44,11,0x55051a17);
        rect(c,x-38,y-21,x+38,y+18,0xff818d91);
        rect(c,x-35,y-27,x+35,y-12,0xffe0e4e4);
        rect(c,x-31,y-12,x+31,y+10,0xff9da8aa);
        fill(c,0xff42545a);P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(3);c.drawCircle(x,y-8,13,P);
        c.drawLine(x-8,y-8,x+8,y-8,P);
        c.drawLine(x,y-16,x,y,P);
        P.setStyle(Paint.Style.FILL);
    }
    /** The actual rank-upgradable fortress has the HTML fort silhouette. */
    public static void fort(Canvas c,float cx,float cy,int rank,float time,
                            float healthRatio){
        int tier=Math.max(1,Math.min(5,rank));
        float width=280,height=235,l=cx-width/2,t=cy-height/2;
        oval(c,cx+10,cy+125,161,22,0x660a2018);
        rect(c,l,t,cx+width/2,cy+height/2,0xff765434);
        rect(c,l+13,t+13,cx+width/2-13,cy+height/2-14,
                tier>=3?0xffa6a899:0xffa47a4b);
        if(tier>=3){
            rect(c,l,t,cx+width/2, t+23,0xffc4c9bc);
            for(int i=0;i<12;i++)rect(c,l+i*24,t-13,l+i*24+15,t+5,0xffbec6bf);
        }else{
            for(int i=0;i<16;i++)
                poly(c,0xffae834e,l+i*17.5f,t+4,
                        l+i*17.5f+8,t-18,l+i*17.5f+17,t+4);
        }
        for(int j=0;j<10;j++){
            rect(c,l,t+j*22,l+12,t+j*22+16,0xff67482d);
            rect(c,l+width-12,t+j*22,l+width,t+j*22+16,0xff67482d);
        }
        rect(c,cx-55,cy+1,cx+55,cy+76,0xffc7aa76);
        poly(c,0xff2f4d48,cx-75,cy+6,cx,cy-65,cx+75,cy+6);
        poly(c,0xff597f76,cx-75,cy+6,cx,cy-65,cx,cy+6);
        rect(c,cx-17,cy+33,cx+17,cy+75,0xff6b4b32);
        for(int k=0;k<5;k++)line(c,cx-48+k*23,cy+19,cx-48+k*23,cy+69,3,
                0xff86643c);
        line(c,cx,cy-65,cx,cy-109,4,0xffbd9861);
        poly(c,0xffe2a847,cx+2,cy-108,
                cx+46+(float)Math.sin(time*3)*5,cy-94,cx+2,cy-82);
        if(tier>=2)for(int side=-1;side<=1;side+=2){
            float sx=cx+side*110;
            rect(c,sx-20,cy-135,sx+20,cy-80,0xff81929b);
            poly(c,0xff476b77,sx-27,cy-135,sx,cy-160,sx+27,cy-135);
        }
        if(tier>=4){
            line(c,cx-115,cy+89,cx+115,cy+89,9,0xffc6c7b5);
            line(c,cx-115,cy-106,cx+115,cy-106,6,0xffc3c7bd);
        }
        if(tier>=5){
            float pulse=(float)Math.sin(time*2.7f)*5;
            oval(c,cx,cy-157,23+pulse,23+pulse,0x667fe9f7);
            oval(c,cx,cy-157,8,8,0xffd8fbff);
        }
        // v0.30 MATERIAL PASS: the fort has visible planks, masonry joints,
        // rivets, roof shingles and a rank-specific heraldic core. These are
        // painted only on the actual solid castle, not fake gameplay pads.
        for(int row=0;row<7;row++){
            float yy=t+27+row*25f;
            if(tier>=3){
                for(int column=0;column<8;column++){
                    float xx=l+17+column*32f+((row%2)*12);
                    if(xx>l+width-20)continue;
                    line(c,xx,yy,Math.min(l+width-17,xx+24),yy,
                            1.7f,0x88606b67);
                    if(column%3==0)line(c,xx+24,yy,xx+24,yy+18,
                            1.2f,0x665e6867);
                }
            }else{
                line(c,l+16,yy,l+width-15,yy,1.8f,0x446c4d30);
                for(int column=0;column<6;column++){
                    float xx=l+24+column*43;
                    line(c,xx,yy+3,xx,yy+15,1.4f,0x55805232);
                }
            }
        }
        // Bright roof cap, shadow beneath eaves and a real shield plaque.
        line(c,cx-72,cy+9,cx+72,cy+9,4,0xff223e3e);
        for(int i=0;i<7;i++){
            float shingleX=cx-58+i*18;
            line(c,shingleX,cy-22-Math.max(0,44-Math.abs(shingleX-cx)*.65f),
                    shingleX+15,cy-12-Math.max(0,44-Math.abs(shingleX-cx)*.65f),
                    2,0x99bcd1c0);
        }
        oval(c,cx,cy+53,16,14,tier>=4?0xff9bcbd3:0xffa9844d);
        poly(c,0xff263f46,cx-8,cy+48,cx+8,cy+48,cx+9,cy+57,
                cx,cy+68,cx-9,cy+57);
        // Rank glyph changes as the player advances in the fortress economy.
        for(int star=0;star<Math.min(tier,5);star++){
            float px=cx-19+star*9;
            oval(c,px,cy+57,2.1f,2.1f,0xfff1d49c);
        }
        if(tier>=3){
            for(int side=-1;side<=1;side+=2){
                float bx=cx+side*111;
                // Dressed stone buttresses support the turret parapets.
                poly(c,0xff77878a,bx-15,cy+102,bx-20,cy-37,
                        bx+11,cy-37,bx+22,cy+102);
                line(c,bx-12,cy+34,bx+14,cy+34,4,0xffc3c8bd);
                line(c,bx-13,cy+65,bx+16,cy+65,3,0xff3b565b);
            }
        }
        if(tier>=4){
            // Cast iron struts + stained glass create a visible upper tier.
            line(c,cx-126,cy+89,cx-126,cy-80,8,0xff465866);
            line(c,cx+126,cy+89,cx+126,cy-80,8,0xff465866);
            oval(c,cx-99,cy-69,7,11,0xffefc47e);
            oval(c,cx+99,cy-69,7,11,0xffefc47e);
        }
        if(tier>=5){
            // Energy crest is supported by the spire, not a floating sticker.
            line(c,cx,cy-108,cx,cy-151,6,0xff637e86);
            poly(c,0xffbcf7fa,cx-11,cy-154,cx,cy-184,cx+11,cy-154);
            poly(c,0xff6bacc8,cx,cy-184,cx+11,cy-154,cx,cy-145);
            oval(c,cx,cy-165,27+(float)Math.sin(time*2.7)*3,
                    13,0x5579dff4);
        }
        if(healthRatio<.48f){
            for(int k=0;k<4;k++)
                line(c,l+45+k*51,cy-5,l+35+k*51,cy+27,
                        2,0xff503e38);
        }
    }
    public synchronized void draw(Canvas c,int chosen,float time){
        prepare(chosen);
        fill(c,Color.WHITE);c.drawBitmap(cached,0,0,P);
        int col=GLOW[world(chosen)];
        // Deliberately sparse ambient motion, below characters and HUD.
        KingdomSetPieces.animate(c,chosen,time,false);
        for(int i=0;i<23;i++){
            float x=((i*137+Campaign.seed(chosen)%97)+time*11)%W;
            float y=270+(i*113)%530+(float)Math.sin(time*.9f+i)*8;
            oval(c,x,y,2,2,alpha(col,65+i%3*28));
        }
    }
    /** Same native live-map crop in the mission briefing, not old scenery. */
    public synchronized void preview(Canvas c,int chosen,float left,float top,
                                     float width,float height){
        prepare(chosen);
        c.save();c.translate(left,top);c.clipRect(0,0,width,height);
        c.scale(width/1150f,height/655f);c.translate(-690,-121);
        fill(c,Color.WHITE);c.drawBitmap(cached,0,0,P);
        fort(c,950,510,1,0,1);
        c.restore();
    }
    public synchronized void thumbnail(Canvas c,int chosen,float left,float top,
                                       float width,float height){
        Campaign.checkStage(chosen);
        Bitmap b=thumbnails.get(chosen);
        if(b==null||b.isRecycled()){
            b=Bitmap.createBitmap(320,112,Bitmap.Config.RGB_565);
            Canvas target=new Canvas(b);
            target.scale(320f/1150f,112f/655f);
            target.translate(-690,-121);
            paintScene(target,chosen);
            fort(target,950,510,1,0,1);
            thumbnails.put(chosen,b);
            if(thumbnails.size()>9){
                Map.Entry<Integer,Bitmap> first=thumbnails.entrySet().iterator().next();
                if(first.getValue()!=null)first.getValue().recycle();
                thumbnails.remove(first.getKey());
            }
        }
        fill(c,Color.WHITE);
        c.drawBitmap(b,null,new RectF(left,top,left+width,top+height),P);
    }
}
