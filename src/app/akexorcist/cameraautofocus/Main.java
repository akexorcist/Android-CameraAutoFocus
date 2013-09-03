package app.akexorcist.cameraautofocus;

import java.util.List;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class Main extends Activity implements SurfaceHolder.Callback
		, SensorEventListener, AutoFocusCallback {
    Camera mCamera;
    SurfaceView mPreview;
	Sensor mAccelerometer;
	SensorManager mSensorManager;

	float motionX = 0;
	float motionY = 0;
	float motionZ = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN 
        		| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);
		
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
       
        mPreview = (SurfaceView)findViewById(R.id.preview);
        mPreview.getHolder().addCallback(this);
        mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
    
	public void onResume() {
    	Log.d("System","onResume");
        super.onResume();
        mCamera = Camera.open();
        mSensorManager.registerListener(this, mAccelerometer
        		, SensorManager.SENSOR_DELAY_NORMAL);
    }
	
	public void onPause() {
    	Log.d("System","onPause");
        super.onPause();
        mCamera.release();
        mSensorManager.unregisterListener(this, mAccelerometer);
    }

	public void onAccuracyChanged(Sensor arg0, int arg1) { }

	public void onSensorChanged(SensorEvent event) {
		if(Math.abs(event.values[0] - motionX) > 0.2 
			|| Math.abs(event.values[1] - motionY) > 0.2 
			|| Math.abs(event.values[2] - motionZ) > 0.2 ) {
			Log.d("Camera System", "Refocus");
			try {
				mCamera.autoFocus(this);
			} catch (RuntimeException e) { }
		}
		
		motionX = event.values[0];
		motionY = event.values[1];
		motionZ = event.values[2];
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.d("CameraSystem","surfaceChanged");
		Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> previewSize = params.getSupportedPreviewSizes();
        List<Camera.Size> pictureSize = params.getSupportedPictureSizes();
        params.setPictureSize(pictureSize.get(0).width, pictureSize.get(0).height);
        params.setPreviewSize(previewSize.get(0).width, previewSize.get(0).height);
        params.setJpegQuality(100);
        mCamera.setParameters(params);
        
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
            mCamera.startPreview();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	public void surfaceCreated(SurfaceHolder arg0) { }

	public void surfaceDestroyed(SurfaceHolder arg0) { }

	public void onAutoFocus(boolean success, Camera camera) {
		Log.d("CameraSystem","onAutoFocus");
	}
}
