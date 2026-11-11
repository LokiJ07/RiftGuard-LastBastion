package com.lokij07.riftguard;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class ResourcePacksTest {
    private static final String SHA="1ae38631f21ef343ebd769f362135e63589facf9";
    private static final String[] NAMES={
        "riftguard_atlas.webp", "riftguard_castle_game.webp",
        "riftguard_enemy_motion.webp", "riftguard_hero_motion.webp",
        "riftguard_terrain_tiles.webp", "riftguard_weapons_motion.webp",
        "riftguard_world_props.webp"
    };
    private JSONObject validManifest() throws Exception {
        JSONObject root=new JSONObject(); root.put("version",1);
        JSONArray arr=new JSONArray();
        for(String name:NAMES) {
            JSONObject a=new JSONObject();
            a.put("name",name); a.put("bytes",500);
            a.put("git_sha","05b8d43e6f6c7131d90910f1bd95597d3ab2e629");
            a.put("url","https://raw.githubusercontent.com/LokiJ07/RiftGuard-LastBastion/"
                    +SHA+"/app/src/main/res/drawable-nodpi/"+name);
            arr.put(a);
        }
        root.put("assets",arr);
        return root;
    }
    @Test public void manifestMustIncludeAllSevenSafeAssets() throws Exception {
        ResourcePacks.Manifest manifest=ResourcePacks.parseManifest(validManifest().toString());
        assertEquals(1,manifest.version);
        assertEquals(7,manifest.assets.length);
        for(ResourcePacks.Asset a:manifest.assets) {
            assertTrue(ResourcePacks.trustedUrl(a.url));
            assertTrue(ResourcePacks.approved(a.name));
        }
    }
    @Test public void rejectsBadHostsAndPathTraversal() {
        String name="riftguard_world_props.webp";
        assertFalse(ResourcePacks.trustedUrl("https://evil.example/"+name));
        assertFalse(ResourcePacks.trustedUrl("http://raw.githubusercontent.com/LokiJ07/RiftGuard-LastBastion/"
                +SHA+"/app/src/main/res/drawable-nodpi/"+name));
        assertFalse(ResourcePacks.approved("../dangerous.webp"));
        assertFalse(ResourcePacks.trustedUrl(
                "https://raw.githubusercontent.com/LokiJ07/RiftGuard-LastBastion/"
                +SHA+"/app/src/main/res/drawable-nodpi/../../key"));
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectsDuplicateAsset() throws Exception {
        JSONObject json=validManifest();
        JSONArray assets=json.getJSONArray("assets");
        assets.getJSONObject(1).put("name",NAMES[0]);
        ResourcePacks.parseManifest(json.toString());
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectsOversizedAssets() throws Exception {
        JSONObject json=validManifest();
        json.getJSONArray("assets").getJSONObject(0).put("bytes",90_000_000);
        ResourcePacks.parseManifest(json.toString());
    }
    @Test public void verifiesGitContentDigestNotJustFileName() throws Exception {
        assertEquals("b6fc4c620b67d95f953a5c1c1230aaab5db5a1b0",
                ResourcePacks.gitBlobSha("hello".getBytes(StandardCharsets.UTF_8)));
    }
    @Test public void offlineFallbackHasAllOriginalArt() {
        Context ctx=RuntimeEnvironment.getApplication();
        assertNull(ResourcePacks.installed(ctx,"../../secret"));
        assertNotNull(ResourcePacks.decodeOrBundled(ctx,R.drawable.riftguard_atlas,
                "riftguard_atlas.webp",1024,768));
        assertNotNull(ResourcePacks.decodeOrBundled(ctx,R.drawable.riftguard_hero_motion,
                "riftguard_hero_motion.webp",656,752));
        ResourcePacks.State state=ResourcePacks.state(ctx);
        assertEquals(7,state.total);
    }
}
