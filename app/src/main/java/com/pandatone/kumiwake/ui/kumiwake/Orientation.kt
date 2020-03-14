package com.pandatone.kumiwake.ui.kumiwake

import androidx.lifecycle.LiveData
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

// 向き情報を格納するクラス
data class Orientation(
        val azimuth: Float,
        val pitch: Float,
        val roll: Float
)

class OrientationLiveData(
        context: Context,
        private val sensorDelay: Int = SensorManager.SENSOR_DELAY_UI)
    : LiveData<Orientation>(), SensorEventListener {

    private val mSensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val mAccelerometerReading = FloatArray(3)
    private val mMagnetometerReading = FloatArray(3)

    private val mRotationMatrix = FloatArray(9)
    private val mOrientationAngles = FloatArray(3)

    override fun onActive() {
        super.onActive()

        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, sensorDelay)
        mSensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, sensorDelay)
    }

    override fun onInactive() {
        super.onInactive()
        mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {

        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.size)
        } else if (event.sensor == magneticField) {
            System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.size)
        }

        updateOrientationAngles()

        value = Orientation(mOrientationAngles[0], mOrientationAngles[1], mOrientationAngles[2])
    }

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading)
        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles)
    }
}