package orz.kassy.twistgesture;

/**
 * Created by kashimoto on 16/04/05.
 */
public class FeaturePoint {
    private long mTime;
    private float mValue;

    public FeaturePoint(long time, float value) {
        mTime = time;
        mValue = value;
    }

    public long getTime() {
        return mTime;
    }

    public float getValue() {
        return mValue;
    }
}
