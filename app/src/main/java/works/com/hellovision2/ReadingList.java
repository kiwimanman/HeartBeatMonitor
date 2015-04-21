package works.com.hellovision2;

import android.util.Log;

import org.opencv.core.Scalar;

import java.util.ArrayList;

public class ReadingList extends ArrayList<Reading> {
    private final static String TAG = "READING_LIST";
    private final static int WINDOW_SIZE = 13000; //milliseconds
    private final static int RECALCULATE_INTERVAL = 1000; //milliseconds

    public ReadingList() {
        super();
    }

    ReadingWindow lastWindow;
    int numWindows = 0;


    public boolean newWindowAvailable() {
        if (isEmpty())
            return false;

        Reading lastReading = get(size() - 1);

        if (lastWindow == null) {
            Reading firstReading = get(0);
            double windowSpan = lastReading.timeStamp() - firstReading.timeStamp();
            // Log.d(TAG, "Window Span: " + windowSpan);
            return windowSpan >= WINDOW_SIZE;
        } else {
            return lastReading.timeStamp() - lastWindow.getStart() >= WINDOW_SIZE;
        }
    }

    public long getSpan() {
        Reading firstReading = get(0);
        Reading lastReading = get(size() - 1);
        return lastReading.timeStamp() - firstReading.timeStamp();
    }

    public BPM processSignal() {
        ReadingWindow window = nextWindow();

        return window.getBpm();
    }

    private ReadingWindow nextWindow() {
        long startTimestamp;
        if (lastWindow == null)
            startTimestamp = get(0).timeStamp();
        else
            startTimestamp = lastWindow.getStart() + RECALCULATE_INTERVAL;

        long lastTimestamp = startTimestamp + WINDOW_SIZE;
        lastWindow = new ReadingWindow();

        Log.d(TAG, "Start Timestamp: " + startTimestamp);
        // Log.d(TAG, "End Timestamp: " + lastTimestamp);

        for (Reading r : this) {
            long timestampR = r.timeStamp();
            // Log.d(TAG, "Query Timestamp: " + timestampR);
            if (timestampR >= startTimestamp && timestampR < lastTimestamp) {
                // Log.d(TAG, "Added: " + timestampR);
                lastWindow.add(r);
            }
        }
        numWindows++;

        return lastWindow;
    }

    @Override
    public void clear() {
        super.clear();
        lastWindow = null;
        numWindows = 0;
    }

    public void recordMeans(Scalar means) {
        add(new Reading(means.val[0]));
    }

    public Reading last() {
        return isEmpty() ? null : get(size() - 1);
    }

    public ReadingWindow getLastWindow() {
        return lastWindow;
    }
}
