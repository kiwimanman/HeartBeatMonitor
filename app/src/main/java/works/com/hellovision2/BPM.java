package works.com.hellovision2;

import android.util.Pair;

import java.util.Date;

public class BPM extends Pair<Double, Date> {
    public BPM(Double first, Date second) {
        super(first, second);
    }

    public long timeStamp() {
        return second.getTime();
    }

    public double bpm() {
        return first;
    }
}