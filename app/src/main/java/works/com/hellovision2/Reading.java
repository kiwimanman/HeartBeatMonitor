package works.com.hellovision2;

import android.util.Pair;

import java.util.Date;

public class Reading extends Pair<Double, Date> {
    public Reading(Double first) {
        super(first, new Date());
    }

    public Reading(Double first, Date date) {
        super(first, date);
    }

    public long timeStamp() {
        return second.getTime();
    }

    public Double value() {
        return first;
    }
}
