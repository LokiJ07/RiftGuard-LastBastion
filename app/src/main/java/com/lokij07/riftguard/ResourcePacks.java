package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Ad-free optional asset delivery, no accounts, no telemetry, no forced internet.
 * Downloads approved resource names only, verifies the exact Git blob hash, and
 * atomically installs content to private app storage for offline re-use.
 */
public final class ResourcePacks {
    private ResourcePacks() {}
    public static final String MANIFEST_URL =
            "https://raw.githubusercontent.com/LokiJ07/RiftGuard-LastBastion/main/game-content/manifest.json";
    private static final String TRUSTED_PREFIX =
            "https://raw.githubusercontent.com/LokiJ07/RiftGuard-LastBastion/";
    private static final String RESOURCE_SUFFIX =
            "/app/src/main/res/drawable-nodpi/";
    private static final long MAX_FILE_BYTES=20L*1024*1024;
    private static final int MAX_MANIFEST_BYTES=32*1024;
    private static final String[] APPROVED={
        "riftguard_atlas.webp",
        "riftguard_castle_game.webp",
        "riftguard_enemy_motion.webp",
        "riftguard_hero_motion.webp",
        "riftguard_terrain_tiles.webp",
        "riftguard_weapons_motion.webp",
        "riftguard_world_props.webp"
    };
    public interface Listener {
        void onStatus(String message,int percent,boolean done,boolean success);
    }
    public static final class Asset {
        public final String name,url,gitSha;
        public final long size;
        Asset(String name,String url,String gitSha,long size) {
            this.name=name; this.url=url; this.gitSha=gitSha; this.size=size;
        }
    }
    public static final class Manifest {
        public final int version;
        public final Asset[] assets;
        Manifest(int version,Asset[] assets){this.version=version;this.assets=assets;}
    }
    public static final class State {
        public final int present,total;
        public final long bytes;
        State(int present,int total,long bytes) {
            this.present=present;this.total=total;this.bytes=bytes;
        }
    }
    static boolean approved(String name) {
        for(String a:APPROVED)if(a.equals(name))return true;
        return false;
    }
    static boolean validGitSha(String value){
        if(value==null || value.length()!=40)return false;
        for(int i=0;i<40;i++){
            char c=value.charAt(i);
            if(!((c>='0'&&c<='9')||(c>='a'&&c<='f')))return false;
        }
        return true;
    }
    static boolean trustedUrl(String url) {
        if(url==null||!url.startsWith(TRUSTED_PREFIX))return false;
        String tail=url.substring(TRUSTED_PREFIX.length());
        int slash=tail.indexOf('/');
        if(slash!=40||!validGitSha(tail.substring(0,slash)))return false;
        String remainder=tail.substring(slash);
        if(!remainder.startsWith(RESOURCE_SUFFIX))return false;
        return approved(remainder.substring(RESOURCE_SUFFIX.length()));
    }
    static Manifest parseManifest(String raw) throws Exception {
        JSONObject doc=new JSONObject(raw);
        int version=doc.getInt("version");
        if(version<1||version>100000)throw new IllegalArgumentException("Unsupported pack version");
        JSONArray array=doc.getJSONArray("assets");
        if(array.length()!=APPROVED.length)
            throw new IllegalArgumentException("Incomplete resource pack");
        Set<String> seen=new HashSet<>();
        Asset[] all=new Asset[array.length()];
        for(int i=0;i<array.length();i++){
            JSONObject a=array.getJSONObject(i);
            String name=a.getString("name");
            String url=a.getString("url");
            String sha=a.getString("git_sha");
            long size=a.getLong("bytes");
            if(!approved(name)||!seen.add(name)||!trustedUrl(url)
                    ||!url.endsWith("/"+name)
                    ||!validGitSha(sha)||size<100||size>MAX_FILE_BYTES)
                throw new IllegalArgumentException("Unsafe or invalid resource: "+name);
            all[i]=new Asset(name,url,sha,size);
        }
        return new Manifest(version,all);
    }
    public static File directory(Context context) {
        return new File(context.getNoBackupFilesDir(),"riftguard-resources");
    }
    public static State state(Context context) {
        File root=directory(context);
        int count=0;long size=0;
        for(String name:APPROVED){
            File file=new File(root,name);
            if(file.isFile()&&file.length()>0&&file.length()<MAX_FILE_BYTES) {
                count++;size+=file.length();
            }
        }
        return new State(count,APPROVED.length,size);
    }
    public static File installed(Context context,String name) {
        if(!approved(name))return null;
        File f=new File(directory(context),name);
        return f.isFile()&&f.length()>100&&f.length()<=MAX_FILE_BYTES?f:null;
    }
    public static Bitmap decodeOrBundled(Context context,int drawable,String name,
                                          int width,int height) {
        File file=installed(context,name);
        if(file!=null) {
            Bitmap cached=decodeFile(file);
            if(valid(cached,width,height))return cached;
            if(cached!=null)cached.recycle();
        }
        BitmapFactory.Options o=new BitmapFactory.Options();
        o.inScaled=false;
        try {return BitmapFactory.decodeResource(context.getResources(),drawable,o);}
        catch(RuntimeException ex){return null;}
    }
    private static Bitmap decodeFile(File file) {
        try {
            BitmapFactory.Options opts=new BitmapFactory.Options();
            opts.inScaled=false;
            return BitmapFactory.decodeFile(file.getAbsolutePath(),opts);
        }catch(RuntimeException ex){return null;}
    }
    private static boolean valid(Bitmap bitmap,int width,int height) {
        return bitmap!=null&&!bitmap.isRecycled()
                &&bitmap.getWidth()==width&&bitmap.getHeight()==height;
    }
    static String gitBlobSha(byte[] data) throws Exception {
        MessageDigest digest=MessageDigest.getInstance("SHA-1");
        digest.update(("blob "+data.length+"\0").getBytes(StandardCharsets.UTF_8));
        digest.update(data);
        return hex(digest.digest());
    }
    private static String hex(byte[] b) {
        StringBuilder out=new StringBuilder(b.length*2);
        for(byte v:b)out.append(String.format(Locale.US,"%02x",v&255));
        return out.toString();
    }
    private static HttpURLConnection connect(String url) throws Exception {
        if(!trustedUrl(url)&&!MANIFEST_URL.equals(url))
            throw new SecurityException("Untrusted resource host/path");
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
        c.setInstanceFollowRedirects(false);
        c.setConnectTimeout(12000);
        c.setReadTimeout(20000);
        c.setRequestProperty("User-Agent","RiftGuard-Android-Resources");
        c.setRequestProperty("Accept","application/json,image/webp,application/octet-stream");
        int status=c.getResponseCode();
        if(status!=200) {
            c.disconnect();
            throw new java.io.IOException("Server status "+status);
        }
        return c;
    }
    private static String getManifest() throws Exception {
        HttpURLConnection c=connect(MANIFEST_URL);
        try(InputStream in=new BufferedInputStream(c.getInputStream())){
            byte[] buf=new byte[4096];
            java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();
            int n;
            while((n=in.read(buf))!=-1){
                if(out.size()+n>MAX_MANIFEST_BYTES)
                    throw new java.io.IOException("Manifest too large");
                out.write(buf,0,n);
            }
            return new String(out.toByteArray(),StandardCharsets.UTF_8);
        }finally{c.disconnect();}
    }
    private static boolean matchesGitBlob(File file,Asset asset) throws Exception {
        if(!file.isFile()||file.length()!=asset.size)return false;
        MessageDigest digest=MessageDigest.getInstance("SHA-1");
        digest.update(("blob "+asset.size+"\0").getBytes(StandardCharsets.UTF_8));
        try(InputStream in=new FileInputStream(file)){
            byte[] buffer=new byte[8192];int len;
            while((len=in.read(buffer))!=-1)digest.update(buffer,0,len);
        }
        return asset.gitSha.equals(hex(digest.digest()));
    }
    private static void download(File root,Asset asset) throws Exception {
        if(matchesGitBlob(new File(root,asset.name),asset))return;
        File part=new File(root,asset.name+".partial");
        if(part.exists()&&!part.delete())
            throw new java.io.IOException("Cannot reset incomplete download");
        HttpURLConnection c=connect(asset.url);
        long received=0;
        try(InputStream in=new BufferedInputStream(c.getInputStream());
            FileOutputStream fout=new FileOutputStream(part);
            BufferedOutputStream out=new BufferedOutputStream(fout)){
            byte[] buf=new byte[8192];int len;
            while((len=in.read(buf))!=-1){
                received+=len;
                if(received>asset.size||received>MAX_FILE_BYTES)
                    throw new java.io.IOException("Unexpected resource size");
                out.write(buf,0,len);
            }
            out.flush();
            fout.getFD().sync();
        }finally {c.disconnect();}
        if(received!=asset.size||!matchesGitBlob(part,asset)) {
            part.delete();
            throw new java.io.IOException("Checksum failed for "+asset.name);
        }
        File result=new File(root,asset.name);
        if(!part.renameTo(result)){
            part.delete();
            throw new java.io.IOException("Cannot activate "+asset.name);
        }
    }
    private static void callback(Handler handler,Listener listener,String msg,
                                 int pct,boolean done,boolean success) {
        handler.post(()->listener.onStatus(msg,pct,done,success));
    }
    public static void start(Context context,Listener listener) {
        Context app=context.getApplicationContext();
        Handler handler=new Handler(Looper.getMainLooper());
        Thread worker=new Thread(()->{
            try {
                callback(handler,listener,"Checking GitHub resource pack...",0,false,false);
                Manifest manifest=parseManifest(getManifest());
                File root=directory(app);
                if(!root.isDirectory()&&!root.mkdirs())
                    throw new java.io.IOException("No storage for resources");
                for(int i=0;i<manifest.assets.length;i++) {
                    Asset asset=manifest.assets[i];
                    callback(handler,listener,"Downloading "+asset.name,
                            (int)(i*100f/manifest.assets.length),false,false);
                    download(root,asset);
                }
                callback(handler,listener,"Downloaded and verified. Reopen to apply.",
                        100,true,true);
            }catch(Exception ex){
                callback(handler,listener,"Download paused: "+ex.getMessage()+
                        ". Existing offline game still works.",0,true,false);
            }
        },"RiftGuard-Resource-Downloader");
        worker.start();
    }
}
