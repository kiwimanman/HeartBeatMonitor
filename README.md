# HeartBeatMonitor
Keith Stone
Heart Beat Monitor App for CSE 590 Spring 2015

## Instructions

Hold your finger still over the front camera. Illuminate from on top by leaving a camera flash light resting on it or by some other method.
Just make sure to stay still. Simply press record and your heart rate will begin to show after several seconds.

## Methodology

The UI simply pushes and retrieves data from a ReadingList object. The reading list's sole job is to coordinate different reference windows of the raw data while the sum total of all the data.
The reading list then makes several windows as they become available. Each window is off a fixed size. The ui thread can then ask what the current BPM is for a given window. By averaging the bpm of several windows it arrives at a final answer.

The window computes the heart rate by demeaning and applying a band pass filter. It was tuned to sample at 16hz, and only show frequencies between 0.8 and 2.5. The sampling rate is contolled by other code out in the main android handling class. Finally apply a median filter of width 3. Differentite and count zero crossing.

This gives the number of "beats" in a window. The first time and the last time that is still valid after filtering is used to compute the span in milliseconds. This is converted to minutes and the bpm for the window is found.

By averaging several windows together a better reading is reached.

