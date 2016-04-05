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
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

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

    @InjectView(R.id.txtSlashStrike)
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

    @InjectView(R.id.txtExp1)
    TextView mTxtExp1;
    @InjectView(R.id.txtExp2)
    TextView mTxtExp2;

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

    private float mFilterAccelX = 0.f;
    private float mFilterAccelY = 0.f;
    private float mFilterAccelZ = 0.f;
    private int mFilterRoll2 = 0;
    private float mFilterTotalAccel = 0.f;

    private float mGyroX = 0.f;
    private float mGyroY = 0.f;
    private float mGyroZ = 0.f;
    private FileWriter mFilewriter = null;

    /** 地磁気行列 */
    private float[] mMagneticValues;
    /** 加速度行列 */
    private float[] mAccelerometerValues;

    /** X軸の回転角度 */
    private int mPitchX;
    /** Y軸の回転角度 */
    private int mRollY;
    private int mRollY2;
    /** Z軸の回転角度(方位角) */
    private int mAzimuthZ;

    // スラッシュ前、静止状態の端末の角度、もっとも重要なのはRoll
    private float mStateRoll = 0.f;

    // 静止状態の加速度の振れ範囲 9.8プラスマイナスいくらか、テキトー
    private static final float STATE_ACCEL_RANGE = 0.5f;
    // 重力加速度
    private static final float ACCEL_G = 9.8f;
    // スラッシュのしきい値となる加速度
    private static final float SLASH_ACCEL = 40.0f;

    private boolean mIsState = false;
    private boolean mSlashFlag = false;
    private Handler mHandler;

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
    SensorEventListener mAccelListener = new SensorEventListener() {
        //THRESHOLD ある値以上を検出するための閾値
        protected final static double THRESHOLD=4.0;
        protected final static double THRESHOLD_MIN=1;

        //low pass filter alpha ローパスフィルタのアルファ値
        protected final static float alpha= 0.8f;

        //端末が実際に取得した加速度値。重力加速度も含まれる。This values include gravity force.
        private float[] currentOrientationValues = { 0.0f, 0.0f, 0.0f };
        //ローパス、ハイパスフィルタ後の加速度値 Values after low pass and high pass filter
        private float[] currentAccelerationValues = { 0.0f, 0.0f, 0.0f };

        //diff 差分
        private float dx=0.0f;
        private float dy=0.0f;
        private float dz=0.0f;

        //previous data 1つ前の値
        private float old_x=0.0f;
        private float old_y=0.0f;
        private float old_z=0.0f;

        //ベクトル量
        private double vectorSize=0;

        //カウンタ
        long counter=0;

        //一回目のゆれを省くカウントフラグ（一回の端末の揺れで2回データが取れてしまうのを防ぐため）
        //count flag to prevent aquiring data twice with one movement of a device
        boolean counted=false;

        // X軸加速方向
        boolean vecx = true;
        // Y軸加速方向
        boolean vecy = true;
        // Z軸加速方向
        boolean vecz = true;


        //ノイズ対策
        boolean noiseflg=true;
        //ベクトル量(最大値)
        private double vectorSize_max=0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            /**
             * 加速度センサー
             */
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // 基本の加速度入力
                mAccelX = event.values[SensorManager.DATA_X];
                mAccelY = event.values[SensorManager.DATA_Y];
                mAccelZ = event.values[SensorManager.DATA_Z];
                mTxtAccelX.setText("x : " + String.format("%.3f", event.values[SensorManager.DATA_X]));
                mTxtAccelY.setText("y : " + String.format("%.3f", event.values[SensorManager.DATA_Y]));
                mTxtAccelZ.setText("z : " + String.format("%.3f", event.values[SensorManager.DATA_Z]));

                // 自前でRoll角を出してみる
                mRollY2 = (int) Math.toDegrees(Math.atan2(mAccelX, mAccelZ));
                mTxtRoll2.setText("roll2 : " + mRollY2);

                // 加速度センサー
                mAccelerometerValues = event.values.clone();

                // 端末の角度の計算
                if (mMagneticValues != null && mAccelerometerValues != null) {

                    float[] rotationMatrix = new float[16];
                    float[] inclinationMatrix = new float[16];
                    float[] remapedMatrix = new float[16];
                    float[] orientationValues = new float[3];

                    // 加速度センサーと地磁気センサーから回転行列を取得
                    SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, mAccelerometerValues, mMagneticValues);
                    SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapedMatrix);
                    SensorManager.getOrientation(remapedMatrix, orientationValues);

                    // ラジアン値を変換し、それぞれの回転角度を取得する
                    mAzimuthZ = radianToDegrees(orientationValues[0]);
                    mPitchX = radianToDegrees(orientationValues[1]);
                    mRollY = radianToDegrees(orientationValues[2]);
                    mTxtPitch.setText("pitch : " + mPitchX);
                    mTxtRoll.setText("roll : " + mRollY);
                    mTxtAzimuth.setText("azimuth : " + mAzimuthZ);
                }

                // ローパスハイパスフィルター
                {
                    // ローパスフィルタで重力値を抽出　Isolate the force of gravity with the low-pass filter.
                    currentOrientationValues[0] = event.values[0] * 0.1f + currentOrientationValues[0] * (1.0f - 0.1f);
                    currentOrientationValues[1] = event.values[1] * 0.1f + currentOrientationValues[1] * (1.0f - 0.1f);
                    currentOrientationValues[2] = event.values[2] * 0.1f + currentOrientationValues[2] * (1.0f - 0.1f);

                    // 重力の値を省くRemove the gravity contribution with the high-pass filter.
                    currentAccelerationValues[0] = event.values[0] - currentOrientationValues[0];
                    currentAccelerationValues[1] = event.values[1] - currentOrientationValues[1];
                    currentAccelerationValues[2] = event.values[2] - currentOrientationValues[2];

                    // 記載
                    mTxtAccelX2.setText("x2 : " + String.format("%.3f", currentAccelerationValues[0]));
                    mTxtAccelY2.setText("y2 : " + String.format("%.3f", currentAccelerationValues[1]));
                    mTxtAccelZ2.setText("z2 : " + String.format("%.3f", currentAccelerationValues[2]));
                    mFilterAccelX = currentAccelerationValues[0];
                    mFilterAccelY = currentAccelerationValues[1];
                    mFilterAccelZ = currentAccelerationValues[2];

                    // 自前でRoll角出す
                    mFilterRoll2 = (int) Math.toDegrees(Math.atan2(mFilterAccelX, mFilterAccelZ));
                }

                /**
                 * kassy add スラッシュ動作の検知を頑張る
                 */
                // 静止してるか否か判定
                mFilterTotalAccel = (float)Math.sqrt(Math.pow(mLinearAccelX, 2) + Math.pow(mLinearAccelY, 2) + Math.pow(mLinearAccelZ, 2));
                if (mFilterTotalAccel < STATE_ACCEL_RANGE) {
                    mIsState = true;
                    mTxtExp1.setText("Now State");
                    mStateRoll = mRollY2;
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
                    // こっちが揺れるな...
                    mIsState = false;
                    mTxtExp1.setText("Now Moving");
                }
            }

            /**
             * ジャイロセンサー
             */
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                mGyroX = event.values[SensorManager.DATA_X];
                mGyroY = event.values[SensorManager.DATA_Y];
                mGyroZ = event.values[SensorManager.DATA_Z];
                mTxtGyroX.setText("x : " + String.format("%.3f", event.values[SensorManager.DATA_X]));
                mTxtGyroY.setText("y : " + String.format("%.3f", event.values[SensorManager.DATA_Y]));
                mTxtGyroZ.setText("z : " + String.format("%.3f", event.values[SensorManager.DATA_Z]));
            }

            /**
             * 地磁気センサー
             */
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // 地磁気センサー
                mMagneticValues = event.values.clone();
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
//            mFilewriter.write(time + ", "
//                                + mAccelX + ", " + mAccelY + ", " + mAccelZ + ", "
//                                + mGyroX + ", " + mGyroY + ", " + mGyroZ + ", "
//                                + mLinearAccelX + ", " + mLinearAccelY + ", " + mLinearAccelZ + ", "
//                                + mGravityX + ", " + mGravityY + ", " + mGravityZ + ", "
//                                + mRollY + ", " + mRollY2 + ", " + mFilterRoll2  + "\n");
            mFilewriter.write(time + ", "
                                + mAtan + ", " + mGravityAngle + ", " + mDeviceRoll + ", "
                                + mAccelAngle + ", " + mSlashAngle + "\n");

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

    public static float minusDegree(float d1, float d2) {
        float dr1, dr2;

        if(d1 >= d2) {
            dr1 = d1;
            dr2 = d2;
        } else {
            dr1 = d1 + 360;
            dr2 = d2;
        }
        return dr1 - dr2;
    }



}
