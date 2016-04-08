package orz.kassy.shakegesture;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kashimoto on 16/04/07.
 */
public class ShakeGestureManager {

    private static final String TAG = "Gesture";
    private static final int NON_DETECTION_TIME = 600;
    private final GestureListener mListener;
    private SensorManager mSensorManager;
    private Context mContext;

    private float mAccelX = 0.f;
    private float mAccelY = 0.f;
    private float mAccelZ = 0.f;

    private float mLinearAccelX = 0.f;
    private float mLinearAccelY = 0.f;
    private float mLinearAccelZ = 0.f;

    private float mRotationX = 0.f;
    private float mRotationY = 0.f;
    private float mRotationZ = 0.f;

    private float mGravityX = 1.f;
    private float mGravityY = 1.f;
    private float mGravityZ = 1.f;

    private float mGyroX = 0.f;
    private float mGyroY = 0.f;
    private float mGyroZ = 0.f;

    private float mLinearAccelTotal = 0.f;

    // スラッシュのしきい値となる加速度
    private static final float SLASH_ACCEL = 40.0f;
    // Shake or Twistの検出閾値
    private static final float TWIST_DETECT_ACCEL = 15.f;

    private boolean mSlashFlag = false;
    private boolean mShakeAfterFlag = false;
    private Handler mHandler;

    // ジェスチャーのタイプ
    private static final int GESTURE_TYPE_NONE = 0;
    private static final int GESTURE_TYPE_SHAKE = 1;
    private static final int GESTURE_TYPE_TWIST = 2;
    private static final int GESTURE_TYPE_SLASH_LEFT = 3;
    private static final int GESTURE_TYPE_SLASH_RIGHT = 4;
    private static final int GESTURE_TYPE_SLASH_UP = 5;
    private static final int GESTURE_TYPE_SLASH_DOWN = 6;
    private int mGestureType = GESTURE_TYPE_NONE;

    // Feature Point
    private ArrayList<FeaturePoint> mountainFPListZ;
    private boolean mShakeDetecting = false;
    private FeaturePoint mTmpFeaturePoint;

    private float mOldLinearAccelZ;
    private int mDurationAve = 0;

    /**
     * ログ用
     */
    private FileWriter mFilewriter = null;
    private Timer mFileTimer;

    /**
     * Constructor
     * @param context
     * @param listener
     */
    public ShakeGestureManager(Context context, GestureListener listener) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mListener = listener;
        mHandler = new Handler();
        mContext = context;
    }

    /**
     * ジェスチャー検知を開始
     */
    public void startSensing() {
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
    }

    /**
     * ジェスチャー検知を終了
     */
    public void stopSensing() {
        mSensorManager.unregisterListener(mAccelListener);
    }

    /**
     * ロギングを開始する
     */
    public void startLogging() {
        if (mFilewriter != null) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
        String fileName = sdf.format(Calendar.getInstance().getTime());
        try {
            File file = new File(mContext.getFilesDir() + "/" +fileName + ".txt");
            mFilewriter = new FileWriter(file);

            mFileTimer = new Timer();
            mFileTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    writeLog();
                }
            }, 33, 33);

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

    /**
     * ロギングを終了する
     */
    public void stopLogging() {
        if (mFilewriter == null) {
            return;
        }
        try {
            mFilewriter.close();

            mFileTimer.cancel();
            mFileTimer = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        mFilewriter = null;

    }

    SensorEventListener mAccelListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            /**
             * 加速度センサー
             */
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // 実質なにもしない
                mAccelX = event.values[0];
                mAccelY = event.values[1];
                mAccelZ = event.values[2];
            }

            /**
             * ジャイロセンサー
             */
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                // 実質なにもしない
                mGyroX = event.values[0];
                mGyroY = event.values[1];
                mGyroZ = event.values[2];
            }

            /**
             * 地磁気センサー
             */
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // do nothing
            }

            /**
             * Gravity Sensor
             */
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                // これは重要
                mGravityX = event.values[0];
                mGravityY = event.values[1];
                mGravityZ = event.values[2];
            }

            /**
             * rotation
             */
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // 実質なにもしない
                mRotationX = event.values[0];
                mRotationY = event.values[1];
                mRotationZ = event.values[2];
            }

            /**
             * Linear Accel
             */
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                mLinearAccelX = event.values[0];
                mLinearAccelY = event.values[1];
                mLinearAccelZ = event.values[2];
                mLinearAccelTotal = (float)computeVectorLength(mLinearAccelX, mLinearAccelY, mLinearAccelZ);

                // スラッシュしきい値超えた（スラッシュ開始）
                if (mLinearAccelTotal > SLASH_ACCEL) {
                    if (!mSlashFlag && !mShakeAfterFlag && mGestureType == GESTURE_TYPE_NONE) {

                        int gravityAngle = (int) Math.toDegrees(Math.atan2(mGravityZ, mGravityX));
                        int deviceRoll = 90 - gravityAngle;

                        int accelAngle = (int) Math.toDegrees(Math.atan2(mLinearAccelZ, mLinearAccelX));
                        int slashAngle = (accelAngle + deviceRoll) >= 0 ? accelAngle + deviceRoll : accelAngle + deviceRoll + 360;

                        if (slashAngle > 360) {
                            slashAngle -= 360;
                        }

                        if (0 <= slashAngle && slashAngle <= 45) {
                            Log.i(TAG, "右方向へのスラッシュストライク！ " + mLinearAccelTotal + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle + ", SlashAngle:" + slashAngle);
                            mListener.onMessage("右方向へのスラッシュストライク！ " + mLinearAccelTotal + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle + ", SlashAngle:" + slashAngle);
                            mGestureType = GESTURE_TYPE_SLASH_RIGHT;
                        } else if (slashAngle <= 135) {
                            Log.i(TAG, "上方向へのスラッシュストライク！ " + mLinearAccelTotal + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle +", SlashAngle:" + slashAngle);
                            mListener.onMessage("上方向へのスラッシュストライク！ " + mLinearAccelTotal + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle + ", SlashAngle:" + slashAngle);
                            mGestureType = GESTURE_TYPE_SLASH_UP;
                        } else if (slashAngle <= 225) {
                            Log.i(TAG, "左方向へのスラッシュストライク！ " + mLinearAccelTotal + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle +", SlashAngle:" + slashAngle);
                            mListener.onMessage("左方向へのスラッシュストライク！ " + mLinearAccelTotal + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle + ", SlashAngle:" + slashAngle);
                            mGestureType = GESTURE_TYPE_SLASH_LEFT;
                        } else if (slashAngle <= 315) {
                            Log.i(TAG, "下方向へのスラッシュストライク！ " + mLinearAccelTotal + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle +", SlashAngle:" + slashAngle);
                            mListener.onMessage("下方向へのスラッシュストライク！ " + mLinearAccelTotal + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle + ", SlashAngle:" + slashAngle);
                            mGestureType = GESTURE_TYPE_SLASH_DOWN;
                        } else if (slashAngle <= 360) {
                            Log.i(TAG, "右方向へのスラッシュストライク！ " + mLinearAccelTotal + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle +", SlashAngle:" + slashAngle);
                            mListener.onMessage("右方向へのスラッシュストライク！ " + mLinearAccelTotal + ", DeviceRoll:" + deviceRoll + ", AccelAngle:" + accelAngle + ", SlashAngle:" + slashAngle);
                            mGestureType = GESTURE_TYPE_SLASH_RIGHT;
                        }

                        // スラッシュの不検知時間を設定
                        mSlashFlag = true;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mSlashFlag = false;
                            }
                        }, NON_DETECTION_TIME);
                    }
                }

                // Shake or Twist を検知した
                if (mLinearAccelTotal > TWIST_DETECT_ACCEL && !mShakeDetecting) {
                    mShakeDetecting = true;
                    mountainFPListZ = new ArrayList<FeaturePoint>();
                    Log.i(TAG, "シェイクないしツイストを検知しました");

                    // 仮山情報リセット（適当な値にしておくか）
                    mTmpFeaturePoint = new FeaturePoint(0, 1);

                }
                // Shake or Twist の終了を検知した
                else if (mLinearAccelTotal < TWIST_DETECT_ACCEL && mShakeDetecting){
                    mShakeDetecting = false;
                    Log.i(TAG, "シェイクないしツイストの終了を検知しました");
                    if (mountainFPListZ != null && mountainFPListZ.size() > 0) {
                        for (int i = 0; i < mountainFPListZ.size(); i++) {
                            Log.i(TAG, "シェイクの山は " + mountainFPListZ.get(i).getTime() + ", " + mountainFPListZ.get(i).getValue());
                        }
                    }
                    if (mGestureType != GESTURE_TYPE_TWIST && mGestureType != GESTURE_TYPE_SHAKE) {
                        // スラッシュで確定（確定するのが遅いかもしれんが）
                        mListener.onGestureDetected(mGestureType, 1);
                    }
                    // ジェスチャー検知をデフォにする
                    mGestureType = GESTURE_TYPE_NONE;

                    // 不検知時間を設定
                    mShakeAfterFlag = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mShakeAfterFlag = false;
                        }
                    }, NON_DETECTION_TIME);
                }

                // Shake or Twistの検知中であるなら
                if (mShakeDetecting) {
                    // 山を検知した
                    if (mOldLinearAccelZ > mLinearAccelZ && mLinearAccelZ > 0) {
                        // その山が今までで一番大きい -> 仮山情報を更新する
                        if (mTmpFeaturePoint != null && mLinearAccelZ > mTmpFeaturePoint.getValue()) {
                            long time = System.currentTimeMillis();
                            Log.i(TAG, "create tmp mount time:" + time + ", value:" + mLinearAccelZ );
                            mTmpFeaturePoint = new FeaturePoint(time, mLinearAccelZ);
                        }
                    }
                    // あきらかな山の終了を検知した
                    if (mLinearAccelZ < 0 && mTmpFeaturePoint.getValue() > 2) {
                        // 仮山を山と確定する
                        mountainFPListZ.add(mTmpFeaturePoint);
                        Log.i(TAG, "add mountain list : " + mountainFPListZ.size() + ", time : " + mTmpFeaturePoint.getTime() + ", " + mTmpFeaturePoint.getValue());
                        // 仮山情報リセット（適当な値にしておくか）
                        mTmpFeaturePoint = new FeaturePoint(0, 1);

                        // 連続が続いたらスラッシュの可能性を破棄する
                        if (mountainFPListZ.size() == 2) {
                            // ツイストに設定しておく、これはすぐにシェイクに振り替わる可能性はある
                            mGestureType = GESTURE_TYPE_TWIST;
                            Log.i(TAG, "スラッシュの可能性を排除しました");

                        }
                        // ある程度続いたらSlash or Twistとして確定する（垂れ流す）
                        else if (mountainFPListZ.size() > 2) {
                            mDurationAve = computeAveDuration(mountainFPListZ);
                            if (mGestureType == GESTURE_TYPE_TWIST) {
                                Log.i(TAG, "これはツイスト : " + mountainFPListZ.size() + "回目 dur:" + mDurationAve);
                                mListener.onMessage("これはツイスト : " + mountainFPListZ.size() + "回目 dur:" + mDurationAve);
                                mListener.onGestureDetected(GESTURE_TYPE_TWIST, mountainFPListZ.size());
                            } else if (mGestureType == GESTURE_TYPE_SHAKE) {
                                Log.i(TAG, "これはシェイク : " + mountainFPListZ.size() + "回目 dur:" + mDurationAve);
                                mListener.onMessage("これはシェイク : " + mountainFPListZ.size() + "回目 dur:" + mDurationAve);
                                mListener.onGestureDetected(GESTURE_TYPE_SHAKE, mountainFPListZ.size());
                            }
                        }
                    }

                    // Twist検知中、LinearYが正に向いたらShakeとみなす
                    if (mLinearAccelY > 0 && mGestureType == GESTURE_TYPE_TWIST) {
                        mGestureType = GESTURE_TYPE_SHAKE;
                        Log.i(TAG, "ああ、これはシェイクだ");
                    }
                }
                mOldLinearAccelZ = mLinearAccelZ;
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

    /**
     * ジェスチャーの通知をおこなうインターフェース
     */
    public interface GestureListener {
        /**
         * ジェスチャーの検知を伝える
         * @param gestureType
         * @param gestureCount
         */
        abstract public void onGestureDetected(int gestureType, int gestureCount);

        /**
         * メッセージを伝える
         * @param message
         */
        abstract public void onMessage(String message);
    }
}
