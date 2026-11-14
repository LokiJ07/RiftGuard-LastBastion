package com.lokij07.riftguard;

import android.graphics.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * v0.81 story presentation geometry shared by rendering and touch dispatch.
 * All positions are in the existing 1280x720 logical canvas; RiftGameView
 * applies the same aspect-safe scale and letterbox origin to draw and input.
 * No battle physics, inventory or persistence state lives here.
 */
public final class StoryPresentationV081 {
    private StoryPresentationV081(){}

    public static final float TEXT_LEFT=79f, TEXT_TOP=548f;
    public static final float TEXT_MAX_WIDTH=1110f, TEXT_LINE_HEIGHT=27f;
    public static final int TEXT_MAX_LINES=5;

    public static final float CHOICE_TOP=405f, CHOICE_BOTTOM=502f;
    public static final float CHOICE_LEAVE_TOP=526f, CHOICE_LEAVE_BOTTOM=600f;

    public static final class TextLayout {
        public final List<String> lines;
        public final float fontSize;
        public final float maxLineWidth;
        private TextLayout(List<String> lines,float fontSize,float maxLineWidth){
            this.lines=Collections.unmodifiableList(new ArrayList<>(lines));
            this.fontSize=fontSize;
            this.maxLineWidth=maxLineWidth;
        }
        public float bottomBaseline(){
            return TEXT_TOP+Math.max(0,lines.size()-1)*TEXT_LINE_HEIGHT;
        }
    }

    /**
     * Measure once per dialogue beat rather than splitting/allocating on
     * every Canvas frame. Fits the whole authored sentence in <=5 lines.
     * A very long token is split at character boundaries without omission.
     */
    public static TextLayout fitText(String content,Paint measure){
        if(content==null||content.trim().isEmpty())
            return new TextLayout(Collections.singletonList(""),24f,0f);
        for(int size=26;size>=15;size--){
            measure.setTextSize(size);
            List<String> lines=wrapWords(content,measure,TEXT_MAX_WIDTH);
            if(lines.size()<=TEXT_MAX_LINES){
                float longest=0f;
                for(String line:lines)longest=Math.max(longest,measure.measureText(line));
                return new TextLayout(lines,size,longest);
            }
        }
        // Unexpected strings should be discovered in QA rather than silently
        // losing narrative text. The authored texts fit by construction.
        throw new IllegalArgumentException("Story text is too long for the subtitle panel");
    }

    private static List<String> wrapWords(String source,Paint paint,float limit){
        List<String> out=new ArrayList<>();
        StringBuilder current=new StringBuilder();
        for(String word:source.trim().split("\\s+")){
            if(word.isEmpty())continue;
            String candidate=current.length()==0?word:current+" "+word;
            if(paint.measureText(candidate)<=limit){
                current.setLength(0);
                current.append(candidate);
                continue;
            }
            if(current.length()>0){
                out.add(current.toString());
                current.setLength(0);
            }
            if(paint.measureText(word)<=limit){
                current.append(word);
                continue;
            }
            int offset=0;
            while(offset<word.length()){
                int n=paint.breakText(word,offset,word.length(),true,limit,null);
                if(n<=0)n=1;
                String slice=word.substring(offset,Math.min(word.length(),offset+n));
                offset+=slice.length();
                if(offset<word.length())out.add(slice);
                else current.append(slice);
            }
        }
        if(current.length()>0)out.add(current.toString());
        if(out.isEmpty())out.add("");
        return out;
    }

    private static boolean contains(float x,float y,
            float left,float top,float right,float bottom){
        return x>=left&&x<=right&&y>=top&&y<=bottom;
    }

    /** Returns StoryProgress.ENDING_* or ENDING_NONE (disabled/outside). */
    public static int caveChoiceHit(float x,float y,boolean canSaveCael){
        if(contains(x,y,102,CHOICE_TOP,608,CHOICE_BOTTOM))
            return StoryProgress.ENDING_GOOD;
        if(contains(x,y,651,CHOICE_TOP,1174,CHOICE_BOTTOM))
            return canSaveCael?StoryProgress.ENDING_TRUE:StoryProgress.ENDING_NONE;
        if(contains(x,y,101,CHOICE_LEAVE_TOP,1174,CHOICE_LEAVE_BOTTOM))
            return StoryProgress.ENDING_PARTIAL;
        return StoryProgress.ENDING_NONE;
    }

    /** Exact painted replay/return button rectangles on the Cave ending. */
    public static int caveEndingHit(float x,float y){
        if(contains(x,y,153,522,619,613))return 1;
        if(contains(x,y,650,522,1125,613))return 2;
        return 0;
    }

    /** Identifies only actually painted Chronicle world rows (not 4px gaps). */
    public static int chronicleWorldHit(float x,float y){
        if(x<72||x>572||y<149||y>=599)return -1;
        int row=(int)((y-149)/45);
        if(row<0||row>=10)return -1;
        return y<=149+row*45+41?row:-1;
    }

    /** Identifies only actual 273x62 memory cards (not grid gutters). */
    public static int chronicleMemoryHit(float x,float y){
        if(y<210||y>=552)return -1;
        int row=(int)((y-210)/70);
        if(row<0||row>=5||y>210+row*70+62)return -1;
        int col=x>=615&&x<=888?0:x>=906&&x<=1179?1:-1;
        return col<0?-1:row*2+col;
    }
}
