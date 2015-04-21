package works.com.hellovision2;

import android.util.Log;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReadingWindow extends ArrayList<Reading> {
    private Double[] signal;
    private Double[] threshold;
    private Double[] derived;

    public static String TAG = "BPM_WINDOW";

    /* public static double[] filter = {
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
    }; */

    public static double[] filter = {
            0.008268555566108497,
            0.006603448205842543,
            0.0030756413319058483,
            -0.008319122937766665,
            -0.024455552566700064,
            -0.03466828734379641,
            -0.028351163042715347,
            -0.0054844750307687625,
            0.02092195515630869,
            0.03427928072726086,
            0.027958575496968396,
            0.010940524640228353,
            0.00035806073923499223,
            0.006844151117587985,
            0.024726956706767902,
            0.03591119293042689,
            0.025367059889287676,
            -0.003982378734213608,
            -0.031236620881332514,
            -0.03492876204005607,
            -0.01439130738045734,
            0.005142871800433452,
            -0.007798041486261513,
            -0.06031931708628055,
            -0.12042548060894033,
            -0.13484517949926444,
            -0.06941388095659681,
            0.05886461433128996,
            0.18539524203642221,
            0.23798067565904513,
            0.18539524203642221,
            0.05886461433128996,
            -0.06941388095659681,
            -0.13484517949926444,
            -0.12042548060894033,
            -0.06031931708628055,
            -0.007798041486261513,
            0.005142871800433471,
            -0.01439130738045734,
            -0.03492876204005607,
            -0.031236620881332514,
            -0.003982378734213608,
            0.02536705988928767,
            0.03591119293042689,
            0.024726956706767902,
            0.006844151117587993,
            0.00035806073923499223,
            0.010940524640228353,
            0.027958575496968396,
            0.03427928072726086,
            0.02092195515630869,
            -0.0054844750307687625,
            -0.028351163042715344,
            -0.03466828734379641,
            -0.024455552566700064,
            -0.008319122937766665,
            0.0030756413319058483,
            0.006603448205842543,
            0.008268555566108497
    };

    public void bandPass() {
        signal = new Double[size()];
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
        // Log.d(TAG, "Avg: " + avg);

        for (int i = 0; i < size(); i++) {
            Reading r = get(i);
            this.set(i, new Reading(r.first - avg, r.second));
        }
    }
    public void threshold(double thresholdValue) {
        threshold = new Double[size()];

        for (int i = 0; i < size(); i++) {
            double valueAt = get(i).value();
            if (valueAt < thresholdValue) {
                threshold[i] = thresholdValue;
            } else {
                threshold[i] = valueAt;
            }
        }

        for (int i = 0; i < size(); i++) {
            Reading r = get(i);
            this.set(i, new Reading(threshold[i], r.second));
        }
    }

    public void derive(int windowSize) {
        // De-mean
        derived = new Double[size()];
        for (int i = 0; i < derived.length; i++) {
            derived[i] = 0.0;
        }

        for (int i = 0; i < size() - windowSize; i++) {

            // Log.d(TAG, "Next: " + next);
            Reading prev = get(i);
            derived[i] = 0.0;
            // Log.d(TAG, "Prev: " + prev);
            for (int j = 0; j < windowSize; j++) {
                Reading next = get(i + j + 1);
                double derivitive = (next.value() - prev.value()) / (double) (next.timeStamp() - prev.timeStamp());
                //Log.d(TAG, "Derived: " + derivitive);
                derived[i] += derivitive;
            }
            derived[i] = derived[i] / (double)windowSize;
        }

        for (int i = 0; i < size() - 1; i++) {
            Reading r = get(i);
            this.set(i, new Reading(derived[i], r.second));
        }
        this.set(size() - 1, new Reading(0.0, get(size() - 1).second));
    }

    public void medianFilter(int windowSize) {
        Double[] median = new Double[size()];
        threshold = median;
        List<Double> window = new ArrayList<>(windowSize);

        for (int i = 0; i < median.length; i++) {
            median[i] = get(i).value();
        }
        for (int i = filter.length; i < size() - windowSize; i++) {
            for (int j = 0; j < windowSize; j++) {
                window.add(get(i + j).value());
            }
            Collections.sort(window);
            if (median.length % 2 == 0)
                median[i] = (window.get(windowSize / 2) + window.get(windowSize / 2 + 1)) / 2;
            else
                median[i] = window.get(windowSize / 2);
            //Log.d(TAG, "Median: " + median[i]);
            window.clear();
        }

        for (int i = 0; i < size() - 1; i++) {
            Reading r = get(i);
            this.set(i, new Reading(median[i], r.second));
        }
    }

    public int zeroCrossings() {
        int count = 0;

        boolean positive = false;
        for (int i = filter.length; i < size() - 1; i++) {
            double red = get(i).value();
            if (red > 0) {
                positive = true;
            } else if (red < 0 && positive) {
                count++;
                positive = false;
            }
        }

        return count;
    }

    public long span() {
        return get(size() - 1).timeStamp() - get(filter.length).timeStamp();
    }

    public int validReadings() {
        return size() - filter.length;
    }

    public double periodSeconds() {
        return span() / 1000.0;
    }

    public BPM getBpm() {
        //Log.d(TAG, "Starting BPM Calculations");

        //Log.d(TAG, "Demean");
        // medianFilter(4);
        demean();
        //
        //Log.d(TAG, "Bandpass");
        bandPass(); // 0.8 to 2.5 hz
        medianFilter(3);
        //threshold(0.2);
        //Log.d(TAG, "Derive");

        derive(2);
        int beats = zeroCrossings();
        //Log.d(TAG, "Beats: " + beats);
        double minutes = periodSeconds() / 60.0;
        //Log.d(TAG, "Span: " + span());
        //Log.d(TAG, "Minutes: " + minutes);

        BPM bpm = new BPM(beats / minutes, get(0).second);

        Log.d(TAG, "Size: " + size());
        Log.d(TAG, "Readings: " + validReadings());
        Log.d(TAG, "Beats: " + beats);
        Log.d(TAG, "Span: " + span());
        Log.d(TAG, "Hz: " + getSampleRate());
        Log.d(TAG, "BPM: " + bpm.bpm());

        return bpm;
    }

    public long getStart() {
        return get(0).timeStamp();
    }

    public XYSeries getAsSeries(int type) {
        SimpleXYSeries series = new SimpleXYSeries(String.valueOf(get(0).timeStamp()/1000000));
        for (int i = filter.length; i < size(); i++) {
            if (type == R.id.filtered) {
                series.addLast(get(i).timeStamp(), signal[i]);
            } else if (type == R.id.derived) {
                series.addLast(get(i).timeStamp(), derived[i]);
            } else {
                series.addLast(get(i).timeStamp(), threshold[i]);
            }
        }
        return series;
    }

    public double getSampleRate() {
        return validReadings() / (span() / 1000.0);
    }
}
