package wgnelther.faces.learn;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.wgnelther.faces.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import wgnelther.faces.MainActivity;
import wgnelther.faces.NameActivity;

public class Recognize extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "Recognize";
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    public static final int SEARCHING= 1;
    public static final int IDLE= 2;

    private static final int frontCam =1;
    private static final int backCam =2;


    private int faceState=IDLE;

    String loc;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private int mLikely=999;

    String mPath="";

    private FaceCameraView mOpenCvCameraView;

    private ImageView Iv;
    Bitmap mBitmap;
    Handler mHandler;

    PersonRecognizer fr;
    ToggleButton scan;

    Set<String> uniqueNames = new HashSet<String>();

    // max number of people to detect in a session
    String[] uniqueNamesArray = new String[10];

    static final long MAXIMG = 10;

    Labels labelsFile;
    static {
        OpenCVLoader.initDebug();
        //System.load("C:\\Program Files\\My Great Program\\libs\\opencv_java.so");
        String libPath = System.getProperty("java.library.path");
        System.out.println("java.library.path=" + libPath);
        String libraryName = "opencv_java";
        System.out.println("Trying to load '" + libraryName + "'");
        System.loadLibrary(libraryName);
        //System.loadLibrary("opencv_java");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    fr=new PersonRecognizer(mPath);
                    String s = getResources().getString(R.string.Straining);
                    //Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
                    fr.load();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };

    public Recognize() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        scan = (ToggleButton) findViewById(R.id.scan);
        final TextView results = (TextView) findViewById(R.id.results);
        final ProgressBar loading = (ProgressBar) findViewById(R.id.loadingBar);

        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Recognize.this, MainActivity.class));
                finish();
            }
        });

        mOpenCvCameraView = (FaceCameraView) findViewById(R.id.tutorial3_activity_java_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCamFront();

        //mPath=getFilesDir()+"/facerecogOCV/";
        mPath = this.getApplicationInfo().dataDir + "/.rec/";

        Log.d("Path", mPath);

        labelsFile= new Labels(mPath);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                /*
                    display a newline separated list of individual names
                 */
                String tempName = msg.obj.toString();
                if (!(tempName.equals("Unknown"))) {
                    tempName = capitalize(tempName);
                    uniqueNames.add(tempName);
                    uniqueNamesArray = uniqueNames.toArray(new String[uniqueNames.size()]);
                    StringBuilder strBuilder = new StringBuilder();
                    for (int i = 0; i < uniqueNamesArray.length; i++) {
                        strBuilder.append(uniqueNamesArray[i] + "\n");
                    }
                    String textToDisplay = strBuilder.toString();
                    results.setText(tempName);
                    scan.setChecked(false);
                    showLoc(1);

                    if (tempName.equals("Wajah kamu belum pernah terdaftar sebelumnya")) {
                        showLoc(2);
                    }
                    if (tempName.equals("Ups, sepertinya kami menemukan masalah dalam membaca wajah kamu. Kami menyarankan untuk mendaftarkan kembali wajah kamu.")) {
                        showLoc(2);
                    }
                } else {
                    results.setText("Wajah kamu belum pernah terdaftar sebelumnya");
                    showLoc(2);
                    scan.setChecked(false);
                }
            }
        };

        scan.setChecked(true);
        faceState = SEARCHING;

        scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    if(!fr.canPredict()) {
                        results.setText("Wajah kamu belum pernah terdaftar sebelumnya");
                        showLoc(2);
                        scan.setChecked(false);
                        loading.setVisibility(View.GONE);
                        return;
                    }
                    faceState = SEARCHING;
                    loading.setVisibility(View.VISIBLE);
                }
                else {
                    faceState = IDLE;
                    loading.setVisibility(View.GONE);
                    showSecButton();
                }
            }
        });

        boolean success=(new File(mPath)).mkdirs();
        if (!success)
        {
            Log.e("Error","Error creating directory");
        }

//        Button submit = (Button) findViewById(R.id.submit);
//        submit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(uniqueNames.size() > 0) {
//                    Intent intent = new Intent(Recognize.this, ReviewResults.class);
//                    intent.putExtra("list", uniqueNamesArray);
//                    startActivity(intent);
//                }
//                else {
//                    Toast.makeText(Recognize.this, "Empty list cannot be sent further", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
    }

    private void showLoc(int status) {

        loc = getIntent().getStringExtra("loc");
        final TextView locText = (TextView) findViewById(R.id.location);

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String dateformatted = dateFormat.format(date);

        if (status == 1) {
            locText.setText("Attendance Success!\n" + loc + "\nTime: " + dateformatted);
        } else {
            locText.setText("Attendance Failed");
        }
    }

    private void showSecButton() {

        CardView rescanCard = (CardView) findViewById(R.id.rescanCard);
        Button rescanButton = (Button) findViewById(R.id.rescan);
        Button registerButton = (Button) findViewById(R.id.registerButton);
        CardView registerCard = (CardView) findViewById(R.id.registerCard);
        final ProgressBar loading = (ProgressBar) findViewById(R.id.loadingBar);

        rescanCard.setVisibility(View.VISIBLE);
        rescanButton.setVisibility(View.VISIBLE);

        registerCard.setVisibility(View.VISIBLE);
        registerButton.setVisibility(View.VISIBLE);

        rescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan.setChecked(true);
                faceState = SEARCHING;
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Recognize.this, NameActivity.class));
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();
        Core.flip(mRgba.t(), mRgba, -1);
        Core.flip(mGray.t(), mGray, -1);

        Mat rotImage = Imgproc.getRotationMatrix2D(new Point(mRgba.cols() / 2,
                mRgba.rows() / 2), 0, 1.0);
        Imgproc.warpAffine(mRgba, mRgba, rotImage, mRgba.size());
        Imgproc.warpAffine(mGray, mGray, rotImage, mRgba.size());

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            /*if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);*/
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();

        if ((facesArray.length>0) && (faceState==SEARCHING))
        {
            Mat m = new Mat();
            m=mGray.submat(facesArray[0]);
            mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(m, mBitmap);
            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            //mHandler.sendMessage(msg);

            textTochange = fr.predict(m);
            mLikely=fr.getProb();
            msg = new Message();
            msg.obj = textTochange;
            mHandler.sendMessage(msg);
        }

        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Recognize.this, MainActivity.class));
        super.onBackPressed(); // optional depending on your needs
    }

    //    because capitalize is the new black
    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
}
