"""Regression: emulator QA may not turn a broken Continue into a green run."""
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

HERE=Path(__file__).resolve().parent

class RecoveryGateTest(unittest.TestCase):
    def prepare(self, root:Path):
        for n in ("01-home.png","02-map.png","03-brief.png","04-battle.png",
                  "05-touch-combat.png","06-relaunch-with-continue.png",
                  "07-restored-wave-paused.png"):
            (root/n).write_bytes(b"\x89PNG"+bytes(2400))
        (root/"launch.txt").write_text("Status: ok\n")
        (root/"relaunch.txt").write_text("Status: ok\n")
        (root/"gfxinfo-framestats.txt").write_text(
            "---PROFILEDATA---\nFlags,IntendedVsync,FrameCompleted,\n"+
            "".join(f"0,{1_000_000_000 + i*30_000_000},"+
                    f"{1_010_000_000 + i*30_000_000},\n" for i in range(12))+
            "---PROFILEDATA---\n")
        (root/"memory.txt").write_text("TOTAL PSS: 85000\n")
        (root/"logcat.txt").write_text("I/GAME: no crashes\n")
        (root/"qa-state.txt").write_text(
            "scene=PLAY story=false paused=false briefing=false stage=0 wave=1 enemies=6\n"+
            "".join("control="+c+" scene=PLAY story=false\n" for c in
                    ("MOVE","DODGE","BUILD","SKILL","ATTACK")))
        (root/"qa-recovery-state.txt").write_text(
            "scene=HOME story=false paused=false briefing=false stage=0 wave=0 enemies=0\n"+
            "scene=PLAY story=false paused=true briefing=false stage=0 wave=1 enemies=0\n")
    def run_analysis(self, root):
        return subprocess.run([sys.executable,str(HERE/"analyze_emulator_qa.py"),
                               str(root)],capture_output=True,text=True)

    def test_valid_force_stop_continue_required(self):
        with tempfile.TemporaryDirectory() as td:
            root=Path(td); self.prepare(root)
            result=self.run_analysis(root)
            self.assertEqual(0,result.returncode,result.stderr+result.stdout)
            summary=json.loads((root/"summary.json").read_text())
            self.assertTrue(summary["android_force_stop_relaunch_recovery_verified"])
            self.assertTrue(summary["enemy_wave_spawn_verified"])

    def test_stuck_on_home_after_force_stop_is_failure(self):
        with tempfile.TemporaryDirectory() as td:
            root=Path(td); self.prepare(root)
            (root/"qa-recovery-state.txt").write_text(
                "scene=HOME story=false paused=false briefing=false stage=0 wave=0 enemies=0\n")
            result=self.run_analysis(root)
            self.assertNotEqual(0,result.returncode)
            self.assertIn("Continue failed",result.stderr+result.stdout)

    def test_relaunch_with_error_is_failure(self):
        with tempfile.TemporaryDirectory() as td:
            root=Path(td); self.prepare(root)
            (root/"relaunch.txt").write_text("Error: Activity not started\n")
            result=self.run_analysis(root)
            self.assertNotEqual(0,result.returncode)
            self.assertIn("relaunch",result.stderr+result.stdout)

    def test_no_combat_enemy_never_passes_recovery_gate(self):
        with tempfile.TemporaryDirectory() as td:
            root=Path(td); self.prepare(root)
            (root/"qa-state.txt").write_text(
                "scene=PLAY story=false paused=false briefing=false stage=0 wave=1 enemies=0\n"+
                "".join("control="+c+" scene=PLAY story=false\n" for c in
                        ("MOVE","DODGE","BUILD","SKILL","ATTACK")))
            result=self.run_analysis(root)
            self.assertNotEqual(0,result.returncode)
            self.assertIn("No real enemy wave",result.stderr+result.stdout)

if __name__ == "__main__":
    unittest.main()
