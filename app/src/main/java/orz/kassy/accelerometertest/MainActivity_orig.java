package orz.kassy.accelerometertest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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
import orz.kassy.twistgesture.FeaturePoint;

public class MainActivity_orig extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SensorManager mSensorManager;


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
    TextView mTxtSlashStrike;

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

    private Timer mFileTimer;

    private float mAccelX = 0.f;
    private float mAccelY = 0.f;
    private float mAccelZ = 0.f;
    private float mTotalAccel = 0.f;

    private float mLinearAccelX = 0.f;
    private float mLinearAccelY = 0.f;
    private float mLinearAccelZ = 0.f;
    private float mRotationX = 0.f;
    private float mRotationY = 0.f;
    private float mRotationZ = 0.f;
    private float mGravityX = 1.f;
    private float mGravityY = 1.f;
    private float mGravityZ = 1.f;

    private float mFilterTotalAccel = 0.f;

    private float mGyroX = 0.f;
    private float mGyroY = 0.f;
    private float mGyroZ = 0.f;
    private FileWriter mFilewriter = null;

    // 静止状態の加速度の振れ範囲 9.8プラスマイナスいくらか、テキトー
    private static final float STATE_ACCEL_RANGE = 0.5f;
    // スラッシュのしきい値となる加速度
    private static final float SLASH_ACCEL = 40.0f;

    private boolean mSlashFlag = false;
    private Handler mHandler;

    // ジェスチャーのタイプ
    private int mGestureType = 0;
    private static final int GESTURE_TYPE_SHAKE = 1;
    private static final int GESTURE_TYPE_TWIST = 2;
    private static final int GESTURE_TYPE_SLASH = 3;

    /**
     * 振動の検知をがんばるお
     */
    // Feature Point
    private ArrayList<FeaturePoint> mountainFPListZ;
    private boolean mNowDetecting = false;
    private FeaturePoint mTmpFeaturePoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 加速度センサー
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // ButterKnife
        ButterKnife.inject(this);

        // Handler
        mHandler = new Handler();

        // FeaturePoint

    }

    @Override
    protected void onResume() {
        super.onResume();

        // 加速度センサー
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            mSensorManager.registerListener(mAccelListener, s, SensorManager.SENSOR_DELAY_UI);
        }

        // ジャイロセンサー
        List<Sensor> sensors2 = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (sensors2.size() > 0) {
            Sensor s = sensors2.get(0);
            mSensorManager.registerListener(mAccelListener, s, SensorManager.SENSOR_DELAY_UI);
        }

        // 地磁気センサー
        List<Sensor> sensors3 = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensors3.size() > 0) {
            Sensor s = sensors3.get(0);
            mSensorManager.registerListener(mAccelListener, s, SensorManager.SENSOR_DELAY_UI);
        }

        /**
         * 追加センサー
         */
        // Linear Accelerometer（重力加速度除外）
        List<Sensor> sensors4 = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        if (sensors4.size() > 0) {
            Sensor s = sensors4.get(0);
            mSensorManager.registerListener(mAccelListener, s, SensorManager.SENSOR_DELAY_UI);
        }

        // Rotation Vector Sensor
        List<Sensor> sensors5 = mSensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
        if (sensors5.size() > 0) {
            Sensor s = sensors5.get(0);
            mSensorManager.registerListener(mAccelListener, s, SensorManager.SENSOR_DELAY_UI);
        }

        // Gravity Sensor
        List<Sensor> sensors6 = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY);
        if (sensors6.size() > 0) {
            Sensor s = sensors6.get(0);
            mSensorManager.registerListener(mAccelListener, s, SensorManager.SENSOR_DELAY_UI);
        }

        mFileTimer = new Timer();
        mFileTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                writeLog();
            }
        }, 33, 33);
    }


    private int mAtan;
    private int mGravityAngle;
    private int mDeviceRoll;
    private int mAccelAngle;
    private int mSlashAngle;
    private float mOldLinearAccelZ;

    private int mDurationAve = 0;


    SensorEventListener mAccelListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            /**
             * 加速度センサー
             */
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // 基本の加速度入力
                mAccelX = event.values[0];
                mAccelY = event.values[1];
                mAccelZ = event.values[2];
                mTxtAccelX.setText("x : " + String.format("%.3f", event.values[0]));
                mTxtAccelY.setText("y : " + String.format("%.3f", event.values[1]));
                mTxtAccelZ.setText("z : " + String.format("%.3f", event.values[2]));

                /**
                 * kassy add スラッシュ動作の検知を頑張る
                 */
                // 静止してるか否か判定
                mFilterTotalAccel = (float)Math.sqrt(Math.pow(mLinearAccelX, 2) + Math.pow(mLinearAccelY, 2) + Math.pow(mLinearAccelZ, 2));
                if (mFilterTotalAccel < STATE_ACCEL_RANGE) {
                }
                // スラッシュしきい値超えた（スラッシュ開始）
                else if (mFilterTotalAccel > SLASH_ACCEL) {
                    if (!mSlashFlag) {

                        int atan = (int) Math.toDegrees(Math.atan2(mGravityZ, mGravityX));
                        int gravityAngle = atan;
                        int deviceRoll = 90 - gravityAngle;

                        int atan2 = (int) Math.toDegrees(Math.atan2(mLinearAccelZ, mLinearAccelX));
                        int accelAngle = atan2;
                        int slashAngle = (accelAngle + deviceRoll) >= 0 ? accelAngle + deviceRoll : accelAngle + deviceRoll + 360;

                        if (slashAngle > 360) {
                            slashAngle -= 360;
                        }

                        if (0 <= slashAngle && slashAngle <= 45) {
                            Log.i(TAG, "右方向へのスラッシュストライク！ " + mFilterTotalAccel + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle +", SlashAngle:" + slashAngle);
                            mTxtSlashStrike.setText("右方向へのスラッシュストライク！ " + mFilterTotalAccel + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle +", SlashAngle:" + slashAngle);
                        } else if (slashAngle <= 135) {
                            Log.i(TAG, "上方向へのスラッシュストライク！ " + mFilterTotalAccel + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle +", SlashAngle:" + slashAngle);
                            mTxtSlashStrike.setText("上方向へのスラッシュストライク！ " + mFilterTotalAccel + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle + ", SlashAngle:" + slashAngle);
                        } else if (slashAngle <= 225) {
                            Log.i(TAG, "左方向へのスラッシュストライク！ " + mFilterTotalAccel + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle +", SlashAngle:" + slashAngle);
                            mTxtSlashStrike.setText("左方向へのスラッシュストライク！ " + mFilterTotalAccel + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle + ", SlashAngle:" + slashAngle);
                        } else if (slashAngle <= 315) {
                            Log.i(TAG, "下方向へのスラッシュストライク！ " + mFilterTotalAccel + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle +", SlashAngle:" + slashAngle);
                            mTxtSlashStrike.setText("下方向へのスラッシュストライク！ " + mFilterTotalAccel + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle + ", SlashAngle:" + slashAngle);
                        } else if (slashAngle <= 360) {
                            Log.i(TAG, "右方向へのスラッシュストライク！ " + mFilterTotalAccel + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle +", SlashAngle:" + slashAngle);
                            mTxtSlashStrike.setText("右方向へのスラッシュストライク！ " + mFilterTotalAccel + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle + ", SlashAngle:" + slashAngle);
                        }

                        mSlashFlag = true;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mSlashFlag = false;
                            }
                        }, 600);

                    }
                } else {
                }
            }

            /**
             * ジャイロセンサー
             */
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                mGyroX = event.values[0];
                mGyroY = event.values[1];
                mGyroZ = event.values[2];
                mTxtGyroX.setText("x : " + String.format("%.3f", event.values[0]));
                mTxtGyroY.setText("y : " + String.format("%.3f", event.values[1]));
                mTxtGyroZ.setText("z : " + String.format("%.3f", event.values[2]));
            }

            /**
             * 地磁気センサー
             */
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // do nothing
            }

            /**
             * Linear Accel
             */
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                mLinearAccelX = event.values[0];
                mLinearAccelY = event.values[1];
                mLinearAccelZ = event.values[2];
                mTxtLinearAccelX.setText("x : " + String.format("%.3f", event.values[0]));
                mTxtLinearAccelY.setText("y : " + String.format("%.3f", event.values[1]));
                mTxtLinearAccelZ.setText("z : " + String.format("%.3f", event.values[2]));

                int atan = (int) Math.toDegrees(Math.atan2(mGravityZ, mGravityX));
                int gravityAngle = atan;
                int deviceRoll = 90 - gravityAngle;

                int atan2 = (int) Math.toDegrees(Math.atan2(mLinearAccelZ, mLinearAccelX));
                int accelAngle = atan2;
                int slashAngle = (accelAngle + deviceRoll) >= 0 ? accelAngle + deviceRoll : accelAngle + deviceRoll + 360;
                if (slashAngle > 360) {
                    slashAngle -= 360;
                }

                mAtan = atan;
                mGravityAngle = gravityAngle;
                mDeviceRoll = deviceRoll;
                mAccelAngle = accelAngle;
                mSlashAngle = slashAngle;

                mTxtAtan.setText("atan : " + atan);
                mTxtGravityAngle.setText("gravity angle : " + gravityAngle);
                mTxtDeviceRoll.setText("device roll : " + deviceRoll);
                mTxtAccelAngle.setText("accel angle : " + accelAngle);
                mTxtSlashAngle.setText("slash angle : " + slashAngle);

                if (computeVectorLength(mLinearAccelX, mLinearAccelY, mLinearAccelZ) > 15 && mNowDetecting == false) {
                    mNowDetecting = true;
                    mountainFPListZ = new ArrayList<FeaturePoint>();
                    Log.i(TAG, "シェイクないしぐるぐるを検知しました");

                    // まずツイストに設定しておく
                    mGestureType = GESTURE_TYPE_TWIST;

                    // 仮山情報リセット（適当な値にしておくか）
                    mTmpFeaturePoint = new FeaturePoint(0, 1);

                } else if (computeVectorLength(mLinearAccelX, mLinearAccelY, mLinearAccelZ) < 15 && mNowDetecting){
                    mNowDetecting = false;
                    Log.i(TAG, "シェイクないしぐるぐるの終了を検知しました");
                    if (mountainFPListZ != null && mountainFPListZ.size() > 0) {
                        for (int i = 0; i < mountainFPListZ.size(); i++) {
                            Log.i(TAG, "シェイクの山は " + mountainFPListZ.get(i).getTime() + ", " + mountainFPListZ.get(i).getValue());
                        }
                    }
                }
                if (mNowDetecting) {
                    // 山を検知した
                    if (mOldLinearAccelZ > mLinearAccelZ && mLinearAccelZ > 0) {
                        // その山が今までで一番大きい -> 仮山情報を更新する
                        if (mTmpFeaturePoint != null && mLinearAccelZ > mTmpFeaturePoint.getValue()) {
                            long time = System.currentTimeMillis();
                            Log.i(TAG, "create tmp mount time:" + time + ", value:" + mLinearAccelZ );
                            mTmpFeaturePoint = new FeaturePoint(time, mLinearAccelZ);
                        }
                    }
                    if (mLinearAccelZ < 0 && mTmpFeaturePoint.getValue() > 2) {
                        // 山情報を確定する
                        Log.i(TAG, "add mountain list : " + mTmpFeaturePoint.getTime() + ", " + mTmpFeaturePoint.getValue());
                        mountainFPListZ.add(mTmpFeaturePoint);
                        if (mountainFPListZ.size() > 3) {
                            mDurationAve = computeAveDuration(mountainFPListZ);
//                            if (mDurationAve > 300) {
//                                Log.i(TAG, "これはツイスト : " + mountainFPListZ.size() + "回目 dur:" + mDurationAve);
//                            } else {
//                                Log.i(TAG, "これはシェイク : " + mountainFPListZ.size() + "回目 dur:" + mDurationAve);
//                            }
                            if (mGestureType == GESTURE_TYPE_TWIST) {
                                Log.i(TAG, "これはツイスト : " + mountainFPListZ.size() + "回目 dur:" + mDurationAve);
                            } else if (mGestureType == GESTURE_TYPE_SHAKE) {
                                Log.i(TAG, "これはシェイク : " + mountainFPListZ.size() + "回目 dur:" + mDurationAve);
                            }
                        }
                        // 仮山情報リセット（適当な値にしておくか）
                        mTmpFeaturePoint = new FeaturePoint(0, 1);
                    }

                    if (mLinearAccelY > 0) {
                        mGestureType = GESTURE_TYPE_SHAKE;
                        Log.i(TAG, "ああ、これはシェイクだ");
                    }
                }
                mOldLinearAccelZ = mLinearAccelZ;
            }

            /**
             * Gravity Sensor
             */
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                mGravityX = event.values[0];
                mGravityY = event.values[1];
                mGravityZ = event.values[2];
                mTxtGravityX.setText("x : " + String.format("%.3f", event.values[0]));
                mTxtGravityY.setText("y : " + String.format("%.3f", event.values[1]));
                mTxtGravityZ.setText("z : " + String.format("%.3f", event.values[2]));
            }

            /**
             * rotation
             */
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                mRotationX = event.values[0];
                mRotationY = event.values[1];
                mRotationZ = event.values[2];
                mTxtRotationX.setText("x : " + String.format("%.3f", event.values[0]));
                mTxtRotationY.setText("y : " + String.format("%.3f", event.values[1]));
                mTxtRotationZ.setText("z : " + String.format("%.3f", event.values[2]));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /**
     * 山の間隔の平均を出す
     * @param pointList
     */
    private int computeAveDuration(ArrayList<FeaturePoint> pointList) {
        int sum =0;
        for (int i=1; i<pointList.size(); i++) {
            sum += (pointList.get(i).getTime() - pointList.get(i-1).getTime());
        }
        return sum / (pointList.size()-1);
    }

    private int radianToDegrees(float angrad) {
        return (int) Math.floor(angrad >= 0 ? Math.toDegrees(angrad) : 360 + Math.toDegrees(angrad));
    }

    @OnClick(R.id.btnStart)
    public void startLogging() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
        String fileName = sdf.format(Calendar.getInstance().getTime());
        try {
            File file = new File(getFilesDir() + "/" +fileName + ".txt");
            Log.i(TAG, "file path:" + file.getAbsolutePath());
            mFilewriter = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLog() {
        if (mFilewriter  == null) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("mmssSSS");
        String time = sdf.format(Calendar.getInstance().getTime());

        try {
            mFilewriter.write(time + ", "
                                + mAccelX + ", " + mAccelY + ", " + mAccelZ + ", "
                                + mGyroX + ", " + mGyroY + ", " + mGyroZ + ", "
                                + mLinearAccelX + ", " + mLinearAccelY + ", " + mLinearAccelZ + ", "
                                + mGravityX + ", " + mGravityY + ", " + mGravityZ + ", "
                                + mRotationX + ", " + mRotationY + ", " + mRotationZ  + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btnStop)
    public void stopLogging() {
        try {
            mFilewriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mFilewriter = null;
    }

    /**
     * ベクトルの長さを出す
     * @param valueX
     * @param valueY
     * @param valueZ
     * @return
     */
    private double computeVectorLength(double valueX, double valueY, double valueZ) {
        return Math.sqrt(Math.pow(valueX, 2) + Math.pow(valueY, 2) + Math.pow(valueZ, 2));
    }

}
