package orz.kassy.accelerometertest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements KassyGestureManager.GestureListener {

    private static final String TAG = "MainActivity";

    /**
     * UI部品
     */
    @InjectView(R.id.txtAccelX)
    TextView mTxtAccelX;
    @InjectView(R.id.txtAccelY)
    TextView mTxtAccelY;
    @InjectView(R.id.txtAccelZ)
    TextView mTxtAccelZ;

    @InjectView(R.id.txtLinearAccelX)
    TextView mTxtLinearAccelX;
    @InjectView(R.id.txtLinearAccelY)
    TextView mTxtLinearAccelY;
    @InjectView(R.id.txtLinearAccelZ)
    TextView mTxtLinearAccelZ;

    @InjectView(R.id.txtAccelX2)
    TextView mTxtAccelX2;
    @InjectView(R.id.txtAccelY2)
    TextView mTxtAccelY2;
    @InjectView(R.id.txtAccelZ2)
    TextView mTxtAccelZ2;

    @InjectView(R.id.txtMessage)
    TextView mTxtMessage;

    @InjectView(R.id.txtGyroX)
    TextView mTxtGyroX;
    @InjectView(R.id.txtGyroY)
    TextView mTxtGyroY;
    @InjectView(R.id.txtGyroZ)
    TextView mTxtGyroZ;

    @InjectView(R.id.txtRotationX)
    TextView mTxtRotationX;
    @InjectView(R.id.txtRotationY)
    TextView mTxtRotationY;
    @InjectView(R.id.txtRotationZ)
    TextView mTxtRotationZ;

    @InjectView(R.id.txtGravityX)
    TextView mTxtGravityX;
    @InjectView(R.id.txtGravityY)
    TextView mTxtGravityY;
    @InjectView(R.id.txtGravityZ)
    TextView mTxtGravityZ;

    @InjectView(R.id.txtAtan)
    TextView mTxtAtan;
    @InjectView(R.id.txtGravityAngle)
    TextView mTxtGravityAngle;
    @InjectView(R.id.txtDeviceRoll)
    TextView mTxtDeviceRoll;
    @InjectView(R.id.txtAccelAngle)
    TextView mTxtAccelAngle;
    @InjectView(R.id.txtSlashAngle)
    TextView mTxtSlashAngle;

    @InjectView(R.id.txtPitch)
    TextView mTxtPitch;
    @InjectView(R.id.txtRoll)
    TextView mTxtRoll;
    @InjectView(R.id.txtRoll2)
    TextView mTxtRoll2;
    @InjectView(R.id.txtAzimuth)
    TextView mTxtAzimuth;

    private KassyGestureManager mGestureManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 加速度センサー
        mGestureManager = new KassyGestureManager(this, this);

        // ButterKnife
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGestureManager.startSensing();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGestureManager.stopSensing();
    }

    /**
     * From KassyGestureManager
     *
     * @param gestureType
     * @param gestureCount
     */
    @Override
    public void onGestureDetected(int gestureType, int gestureCount) {
        Log.w(TAG, "Gesture type : " + gestureType + ", count : " + gestureCount);
    }

    @Override
    public void onMessage(String message) {
        Log.i(TAG, "msg:" + message);
        mTxtMessage.setText(message);
    }

    @OnClick(R.id.btnStart)
    public void startLogging() {
        mGestureManager.startLogging();
    }

    @OnClick(R.id.btnStop)
    public void stopLogging() {
        mGestureManager.stopLogging();
    }
}