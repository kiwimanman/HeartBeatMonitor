package works.com.hellovision2;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.Random;


public class VisionActivity extends ActionBarActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    String TAG = "APP";
    CameraBridgeViewBase mOpenCvCameraView;
    float lastTouchY=0;
    int cannyThreshold=50;
    boolean recording = false;

    XYPlot plot;
    SimpleXYSeries redSeries;
    SimpleXYSeries bpmSeries;
    LineAndPointFormatter redFormatter;
    LineAndPointFormatter bpmFormatter;

    Button recordButton;
    Mat firstFrame;
    Random random = new Random();
    double runningBPMTotal = 0.0;
    double avgBPM = 0.0;

    RadioGroup radioGroup;

    ReadingList rawReadings;

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
        recordButton = (Button)findViewById(R.id.recordButton);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);

        plot = (XYPlot)findViewById(R.id.XYPlot);
        rawReadings = new ReadingList();

        redSeries = new SimpleXYSeries("Red");
        bpmSeries = new SimpleXYSeries("BPM");

        redFormatter = new LineAndPointFormatter(Color.rgb(200, 0, 0), Color.rgb(250, 0, 0), null, null);
        bpmFormatter = new LineAndPointFormatter(Color.rgb(200, 200, 200), Color.rgb(250, 250, 250), null, null);
        plot.addSeries(redSeries, redFormatter);
        plot.addSeries(bpmSeries, bpmFormatter);

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

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat currentFrame = inputFrame.rgba();
        Scalar means = Core.mean(currentFrame);

        if (recording) {
            rawReadings.recordMeans(means);
            int checkedRadioButton = radioGroup.getCheckedRadioButtonId();

            if (rawReadings.newWindowAvailable()) {
                BPM bpm = rawReadings.processSignal();
                runningBPMTotal += bpm.bpm();
                if (checkedRadioButton != R.id.raw && checkedRadioButton != R.id.bpm && checkedRadioButton != R.id.off) {
                    plot.addSeries(
                            rawReadings.getLastWindow().getAsSeries(checkedRadioButton),
                            new LineAndPointFormatter(
                                    Color.rgb(random.nextInt(), random.nextInt(), random.nextInt()),
                                    Color.rgb(random.nextInt(), random.nextInt(), random.nextInt()),
                                    null,
                                    null
                            )
                    );
                }
                if (checkedRadioButton == R.id.bpm) {
                    bpmSeries.addLast(bpm.timeStamp(), bpm.bpm());
                }
                ReadingWindow lastWindow = rawReadings.getLastWindow();
                if (lastWindow != null && lastWindow.getSampleRate() < 6.0) {
                    recording = false;
                }
            }

            Reading lastReading = rawReadings.last();
            if (checkedRadioButton == R.id.raw) {
                redSeries.addLast(lastReading.timeStamp(), lastReading.value());
            }

            if (checkedRadioButton != R.id.off)
                plot.redraw();

            if (rawReadings.numWindows > 0) {
                avgBPM = runningBPMTotal / (double) rawReadings.numWindows;
                final TextView heartRateDisplay = (TextView)findViewById(R.id.heartRateDisplay);
                heartRateDisplay.post(new Runnable() {
                                  public void run() {
                                      TextView heartRateDisplay = (TextView)findViewById(R.id.heartRateDisplay);
                                      heartRateDisplay.setText("Heart Rate: " + Math.floor(avgBPM) + " bpm");
                                  }
                              });
            }
        }

        // Heard this might fix a memory leak.
        if (firstFrame == null) {
            firstFrame = currentFrame;
        } else {
            currentFrame.release();
        }

        return firstFrame;
    }


    public void recordClicked(View view) {
        recording = !recording;
        if (recording) {
            recordButton.setText("Stop");
            TextView heartRateDisplay = (TextView)findViewById(R.id.heartRateDisplay);
            heartRateDisplay.setText("Heart Rate: ??? bpm");
        } else {
            recordButton.setText("Record");
            rawReadings.clear();
            while (redSeries.size() > 0) {
                redSeries.removeFirst();
            }
            while (bpmSeries.size() > 0) {
                bpmSeries.removeFirst();
            }
            plot.clear();
            plot.addSeries(redSeries, redFormatter);
            plot.addSeries(bpmSeries, bpmFormatter);
            runningBPMTotal = 0.0;
        }

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
}
