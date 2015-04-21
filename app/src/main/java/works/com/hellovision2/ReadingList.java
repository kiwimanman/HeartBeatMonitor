package works.com.hellovision2;

import org.opencv.core.Scalar;

import java.util.ArrayList;

public class ReadingList extends ArrayList<Reading> {

    private final static int WINDOW_SIZE = 5000; //milliseconds
    private final static int RECALCULATE_INTERVAL = 1000; //milliseconds

    public ReadingList() {
        super();
    }

    long startLastWindow;
    int numWindows = 0;


    public boolean newWindowAvailable() {
        if (numWindows == 0) {
            Reading lastReading = get(size() - 1);
            double windowSpan = lastReading.timeStamp() - startLastWindow;
            return windowSpan >= WINDOW_SIZE;
        } else {
            double fillSpan = getSpan();
            return fillSpan >= WINDOW_SIZE;
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

        long lastTimestamp = startLastWindow + WINDOW_SIZE * 1000;
        for (Reading r : this) {
            long timestampR = r.timeStamp();
            if (timestampR > startLastWindow && timestampR < lastTimestamp) {
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
