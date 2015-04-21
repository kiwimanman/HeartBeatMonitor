package works.com.hellovision2;

import android.util.Log;

import org.opencv.core.Scalar;

import java.util.ArrayList;

public class ReadingList extends ArrayList<Reading> {
    private final static String TAG = "READING_LIST";
    private final static int WINDOW_SIZE = 5000; //milliseconds
    private final static int RECALCULATE_INTERVAL = 1000; //milliseconds

    public ReadingList() {
        super();
    }

    Long startLastWindow;
    int numWindows = 0;


    public boolean newWindowAvailable() {
        if (isEmpty())
            return false;

        if (startLastWindow == null)
            startLastWindow = get(0).timeStamp();

        Reading lastReading = get(size() - 1);

        if (numWindows == 0) {

            double windowSpan = lastReading.timeStamp() - startLastWindow;
            // Log.d(TAG, "Window Span: " + windowSpan);
            return windowSpan >= WINDOW_SIZE;
        } else {
            return lastReading.timeStamp() - startLastWindow >= WINDOW_SIZE;
        }
    }

    public long getSpan() {
        Reading firstReading = get(0);
        Reading lastReading = get(size() - 1);
        return lastReading.timeStamp() - firstReading.timeStamp();
    }

    public BPM processSignal() {
        ReadingWindow window = nextWindow();
        advanceWindow();

        return window.getBpm();
    }

    private ReadingWindow nextWindow() {
        ReadingWindow window = new ReadingWindow();
        long lastTimestamp = startLastWindow + WINDOW_SIZE;

        Log.d(TAG, "Start Timestamp: " + startLastWindow);
        // Log.d(TAG, "End Timestamp: " + lastTimestamp);

        for (Reading r : this) {
            long timestampR = r.timeStamp();
            // Log.d(TAG, "Query Timestamp: " + timestampR);
            if (timestampR >= startLastWindow && timestampR < lastTimestamp) {
                // Log.d(TAG, "Added: " + timestampR);
                window.add(r);
            }
        }

        return window;
    }

    public void advanceWindow() {
        startLastWindow += RECALCULATE_INTERVAL;
        numWindows++;
    }

    @Override
    public void clear() {
        super.clear();
    }

    public void recordMeans(Scalar means) {
        add(new Reading(means.val[0]));
    }

    public Reading last() {
        return isEmpty() ? null : get(size() - 1);
    }
}
