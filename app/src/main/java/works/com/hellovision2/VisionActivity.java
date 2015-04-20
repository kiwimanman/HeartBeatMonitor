package works.com.hellovision2;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.androidplot.xy.XYPlot;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Date;


public class VisionActivity extends ActionBarActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    String TAG = "APP";
    CameraBridgeViewBase mOpenCvCameraView;
    float lastTouchY=0;
    int cannyThreshold=50;

    XYPlot plot;

    public class Reading extends Pair<Scalar, Date> {
        public Reading(Scalar first) {
            super(first, new Date());
        }
    }

    ArrayList<Reading> rawReadings;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vision);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        plot = (XYPlot)findViewById(R.id.XYPlot);
        rawReadings = new ArrayList<>();

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }


    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vision, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }
//
    @Override
    public void onCameraViewStopped() {

    }
//
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat currentFrame = inputFrame.rgba();
        Scalar means = Core.mean(currentFrame);

        recordMeans(means);

        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.Canny(currentFrame,currentFrame,cannyThreshold/3,cannyThreshold );
        return currentFrame;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        // MotionEvent reports input details from the touch screen
//        // and other input controls. In this case, you are only
//        // interested in events where the touch position changed.
        float y = e.getY();
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            if (lastTouchY > y)
                cannyThreshold += 5;
            else
                cannyThreshold -= 5;

            lastTouchY = y;
        }
//
        if (e.getAction() == MotionEvent.ACTION_UP)
            lastTouchY = 0;
        return true;
    }

    private void recordMeans(Scalar means) {
         rawReadings.add(new Reading(means));
    }

}
