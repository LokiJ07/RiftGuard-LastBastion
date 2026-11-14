import importlib.util
from pathlib import Path
import tempfile
import unittest

MODULE = Path(__file__).resolve().parent / "release_gate.py"
spec = importlib.util.spec_from_file_location("release_gate", MODULE)
gate = importlib.util.module_from_spec(spec)
spec.loader.exec_module(gate)

GOOD_GRADLE = """
android {
    compileSdk 36
    defaultConfig {
        minSdk 24
        targetSdk 36
        versionCode 69
        versionName '0.72.0-alpha1'
    }
    signingConfigs { production {} }
    buildTypes { release { signingConfig signingConfigs.production } }
}
"""
MANIFEST = """<manifest xmlns:android="http://schemas.android.com/apk/res/android">
<application android:usesCleartextTraffic="false"
    android:appCategory="game" android:label="RIFTGUARD">
    <activity android:name=".MainActivity" android:exported="true"/>
</application></manifest>"""


class ReleaseGateTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        (self.root / "app/src/main").mkdir(parents=True)
        self.gradle = self.root / "app/build.gradle"
        self.manifest = self.root / "app/src/main/AndroidManifest.xml"
        self.gradle.write_text(GOOD_GRADLE, encoding="utf-8")
        self.manifest.write_text(MANIFEST, encoding="utf-8")

    def test_development_preflight_passes_without_secrets_but_is_not_production(self):
        result = gate.analyze(self.root, environ={})
        self.assertEqual("pass", result["status"])
        self.assertEqual("preflight", result["mode"])
        self.assertIn("UNSIGNED", " ".join(result["warnings"]))

    def test_google_play_target_api_is_a_hard_gate(self):
        self.gradle.write_text(GOOD_GRADLE.replace("targetSdk 36", "targetSdk 35"))
        result = gate.analyze(self.root, environ={})
        self.assertEqual("blocked", result["status"])
        self.assertIn("API 36+", " ".join(result["failures"]))

    def test_cleartext_or_debuggable_application_is_blocked(self):
        self.manifest.write_text(MANIFEST.replace('android:usesCleartextTraffic="false"',
                    'android:usesCleartextTraffic="true"'), encoding="utf-8")
        self.assertEqual("blocked", gate.analyze(self.root, environ={})["status"])
        self.manifest.write_text(MANIFEST.replace('android:appCategory="game"',
                    'android:appCategory="game" android:debuggable="true"'),
                    encoding="utf-8")
        self.assertEqual("blocked", gate.analyze(self.root, environ={})["status"])

    def test_preliminary_alpha_cannot_be_signed_off_as_production(self):
        result = gate.analyze(self.root, "production", human_qa=True, environ={})
        self.assertEqual("blocked", result["status"])
        self.assertTrue(any("prerelease" in s for s in result["failures"]))
        self.assertTrue(any("protected" in s for s in result["failures"]))

    def test_manual_signoff_is_not_skippable_even_with_real_keystore(self):
        with tempfile.NamedTemporaryFile() as key:
            key.write(b"encrypted bytes placeholder only" * 10)
            key.flush()
            e = dict(zip(gate.REQUIRED_KEYS, (key.name, "secret", "alias", "secret")))
            self.gradle.write_text(GOOD_GRADLE.replace("0.72.0-alpha1", "1.0.0"))
            result = gate.analyze(self.root, "production", human_qa=False, environ=e)
            self.assertEqual("blocked", result["status"])
            self.assertTrue(any("Human playtest" in s for s in result["failures"]))
            okay = gate.analyze(self.root, "production", human_qa=True, environ=e)
            self.assertEqual("pass", okay["status"])
            self.assertNotIn("secret", str(okay))

    def test_sensitive_permissions_cannot_sneak_into_manifest(self):
        self.manifest.write_text(MANIFEST.replace("<application",
               '<uses-permission android:name="android.permission.RECORD_AUDIO"/><application'),
               encoding="utf-8")
        self.assertEqual("blocked", gate.analyze(self.root, environ={})["status"])


if __name__ == "__main__":
    unittest.main()
