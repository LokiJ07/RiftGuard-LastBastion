"""Regression tests for Android 10 and Android 16 GPU metrics."""
import unittest
from gfxinfo_parser import frame_times_ms

class GfxinfoParserTest(unittest.TestCase):
    def test_android_10_legacy_csv(self):
        src = """
---PROFILEDATA---
Flags,IntendedVsync,Vsync,OldA,OldB,OldC,OldD,OldE,OldF,OldG,OldH,OldI,OldJ,FrameCompleted,
0,1000000000,1,0,0,0,0,0,0,0,0,0,0,1016667000,
0,2000000000,1,0,0,0,0,0,0,0,0,0,0,2033333000,
---PROFILEDATA---
"""
        self.assertEqual([16.667,33.333], [round(x,3) for x in frame_times_ms(src)])

    def test_android_16_frame_timeline_column_names(self):
        src = """
Graphics summary with no PROFILEDATA
---PROFILEDATA---
Flags,FrameTimelineVsyncId,IntendedVsync,Vsync,InputEventId,HandleInputStart,AnimationStart,PerformTraversalsStart,DrawStart,FrameDeadline,FrameStartTime,FrameInterval,WorkloadTarget,SyncQueued,SyncStart,IssueDrawCommandsStart,SwapBuffers,FrameCompleted,DequeueBufferDuration,QueueBufferDuration,GpuCompleted,
0,14915,86096634566,86163301230,0,0,0,0,0,0,0,0,0,0,0,0,0,86330439934,0,0,0,
0,14916,86100000000,86170000000,0,0,0,0,0,0,0,0,0,0,0,0,0,86300000000,0,0,0,
---PROFILEDATA---
"""
        values=frame_times_ms(src)
        self.assertEqual(2,len(values))
        self.assertAlmostEqual(233.805368,values[0],places=4)
        self.assertAlmostEqual(200.0,values[1],places=3)

    def test_header_required_and_invalid_timestamps_ignored(self):
        src = """
---PROFILEDATA---
Flags,IntendedVsync,FrameCompleted,
0,100,99,
0,0,123,
0,1000000000,7000000000,
0,1000000000,1040000000,
---PROFILEDATA---
"""
        self.assertEqual([40.0],frame_times_ms(src))
        self.assertEqual([],frame_times_ms("---PROFILEDATA---\\n0,1,2\\n---PROFILEDATA---"))
if __name__ == "__main__":
    unittest.main()
