package works.com.hellovision2;

import android.util.Log;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;

import java.util.ArrayList;

public class ReadingWindow extends ArrayList<Reading> {
    private Double[] output;
    public static String TAG = "BPM_WINDOW";
    public static double[] filter = {
           -0.06705209519487083,
           -0.09128730259643934,
            0.004217000804097453,
           -0.08151335948188285,
           -0.06786950971928182,
            0.020060597837801977,
           -0.21809513503421102,
            0.1492411596190222,
            0.6975994782885324,
            0.1492411596190222,
           -0.21809513503421102,
            0.020060597837801963,
           -0.06786950971928182,
           -0.08151335948188287,
            0.004217000804097453,
           -0.09128730259643934,
           -0.06705209519487083,
    };

    public void bandPass() {
        Double[] signal = new Double[size()];
        for (int i = 0; i < signal.length; i++) {
            signal[i] = 0.0;
        }

        for (int i = 0; i < size() - filter.length; i++) {
            double avg = 0.0;
            for (int j = 0; j < filter.length; j++) {
                int index = i + j;
                avg += filter[j] * get(index).value();
            }
            signal[filter.length + i] = avg;
        }

        output = signal;

        for (int i = 0; i < size(); i++) {
            Reading r = get(i);
            this.set(i, new Reading(signal[i], r.second));
        }
    }

    public void demean() {
        // De-mean
        double avg = 0.0;
        for (Reading r : this) {
            avg += r.value();
        }
        avg /= size();
        Log.d(TAG, "Avg: " + avg);

        for (int i = 0; i < size(); i++) {
            Reading r = get(i);
            this.set(i, new Reading(r.first - avg, r.second));
        }
    }

    public void derive() {
        // De-mean
        Double[] derivitives = new Double[size()];
        for (int i = 0; i < size() - 1; i++) {
            Reading next = get(i + 1);
            // Log.d(TAG, "Next: " + next);
            Reading prev = get(i);
            // Log.d(TAG, "Prev: " + prev);
            derivitives[i] = (next.value() - prev.value()) / (double)(next.timeStamp() - prev.timeStamp());
        }

        for (int i = 0; i < size() - 1; i++) {
            Reading r = get(i);
            this.set(i, new Reading(derivitives[i], r.second));
        }
        this.set(size() - 1, new Reading(0.0, get(size() - 1).second));
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

    public long span() {
        return get(size() - 1).timeStamp() - get(0).timeStamp();
    }

    public double periodMinutes() {
        return span() / 60000.0;
    }

    public BPM getBpm() {
        Log.d(TAG, "Starting BPM Calculations");

        Log.d(TAG, "Demean");
        demean();
        Log.d(TAG, "Bandpass");
        bandPass(); // 0.8 to 2.5 hz
        Log.d(TAG, "Derive");
        derive();
        int beats = zeroCrossings();
        //Log.d(TAG, "Beats: " + beats);
        double minutes = periodMinutes();
        //Log.d(TAG, "Span: " + span());
        //Log.d(TAG, "Minutes: " + minutes);

        BPM bpm = new BPM(beats / minutes, get(0).second);

        Log.d(TAG, "Readings: " + size());
        Log.d(TAG, "Hz: " + size() / (span() / 1000.0));
        Log.d(TAG, "BPM: " + bpm.bpm());

        return bpm;
    }

    public long getStart() {
        return get(0).timeStamp();
    }

    public XYSeries getAsSeries() {
        SimpleXYSeries series = new SimpleXYSeries(String.valueOf(get(0).timeStamp()/1000000));
        for (int i = 0; i < size(); i++) {
            //series.addLast(get(i).timeStamp(), get(i).value());
            series.addLast(get(i).timeStamp(), output[i]);
        }
        return series;
    }
}
