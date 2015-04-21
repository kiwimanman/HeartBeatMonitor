package works.com.hellovision2;

import java.util.ArrayList;

public class ReadingWindow extends ArrayList<Reading> {
    public void demean() {
        // De-mean
        double avg = 0.0;
        for (Reading r : this) {
            avg += r.value();
        }
        avg /= size();

        for (int i = 0; i < size(); i++) {
            Reading r = get(i);
            this.set(i, new Reading(r.first - avg, r.second));
        }
    }

    public int zeroCrossings() {
        int count = 0;

        boolean positive = false;
        for (Reading r : this) {
            double red = r.value();
            if (red > 0) {
                positive = true;
            } else if (red < 0 && positive) {
                count++;
            }
        }

        return count;
    }

    public double periodMinutes() {
        double period = get(size() - 1).timeStamp() - get(0).timeStamp() / 1000.0;
        period /= 60;
        return period;
    }

    public BPM getBpm() {
        demean();

        int beats = zeroCrossings();
        double minutes = periodMinutes();

        return new BPM(beats / minutes, get(0).second);
    }
}
