/**
 Name of the module : onGetImageListener.java

 Date on which the module was created : 20/04/2018

 Author's name : Phoolchandra

 Modification history : by Savinay  21/04/2018
 : Savsani kevin 22/04/2018

 Synopsis of the module : onGetImageListener file is executed when button named camera is pressed/clicked

 Different functions supported along with their input/Output parameters.

 Globel variables accessed/modified by the module.

 */

package kps.arnutrition;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import kps.arnutrition.database.DbAccessFoodTypes;
import kps.arnutrition.detectableObjects.Food;
import kps.arnutrition.detectableObjects.FoodType;
import kps.arnutrition.information.NutritionalValue;




//////////////////////////////////////////// END IMPORT ////////////////////////////////////////////




public class onGetImageListener
        extends Activity
        implements CvCameraViewListener2,
        View.OnClickListener,
        View.OnTouchListener {

    // load opencv library
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("ARNutrition");
    }




    //// global constant section
    // constants for permissions
    private static final int CAMERA_PERMISSIONS = 0;

    // constants for development purposes only
    private static final boolean DEACTIVATE_HSV_MODE = true; //deactives the long hsv mode information toast
    private static final boolean ACTIVATE_COMPARISON = false;
    //activates use of testHistogramComparisonMethods

    // constants for clearer mode operations
    private static final int COLOUR_DETECTION_VIEW = 0;
    private static final int BORDER_DETECTION_VIEW = 1;
    private static final int KEYPOINTS_DETECTION_VIEW = 2;
    private static final int HSV_FILTER_VIEW = 3;

    // constants regarding tags for debugging purposes
    private static final String LABEL = "ARNutrition::onGetImageListener";
    private static final String DETECTION_MODE = "modeDetection";
    private static final String EDGE_DETECTION = "modeEdge";
    private static final String LABEL_FEATURES = "modeFeatures";
    private static final String HSV_FILTER_MODE = "modeHsvFilter";
    private static final String HIST_COMP_TESTS = "HistCompTests";
    private static final String DEBUG = "Debug";

    // constants regarding menu operations
    private static final int MAX_DETECTOR_MODE = 3;

    // histogram related settings
    private static final int HIST_SIZE = 256;  // size of histogram (value pairs) [max=256]
    private static final double HIST_BORDER_MIN = 0.07; // min value for autofit borders
    private static final int HIST_COMPARISON_METHOD = Imgproc.CV_COMP_CORREL;
    // used method for histogram comparison
    // histogram drawing related settings
    final int HIST_SETTING = 20;
    final int HIST_AMOUNT = 2 + 1;                 // one more to save space for the buttons

    // defines, which histograms should be compared - choose ONE
    private static final boolean COMPARE_HUE = false;
    private static final boolean COMPARE_SATURATION = false;
    private static final boolean COMPARE_VALUE = true;

    // threshold for comparison results
    private static final double HUE_MIN = 0.85;
    private static final double HUE_MAX = 1;
    private static final double SATURATION_MIN = 0.75;
    private static final double SATURATION_MAX = 1;

    // constants for contour relating settings
    private static final boolean DETECTED_DESIRED_CONTOUR = true;
    private static final int CONTOUR_LEASTVALUE = 15000;   // threshold for found contours
    private static final int CONTOUR_MaxValue = 300000;     // threshold for found contours
    private static final double CONTOUR_DISTANCE = 5;     // distance between points of contour line

    // constants relating tracking operations
    private static final int FRAMES_TRACKING_RANGE = 10;
    // amount of frames to consider in tracking

    // constants related to the dynamically evenly generated hue scalar
    private static final int HUE_PHASES = 5;
    private static final int STEPS_PER_PHASE = HIST_SIZE /HUE_PHASES;

    // constants related to morphing operations
    private static final Size CV_ERODE = new Size(3, 3);
    private static final Size CV_DILATE = new Size(5, 5);

    // food label and information display related settings
    private static final int INFORMATION_DISPLAY_VALUE = 5;
    private static final int BUFFER_TEXT = INFORMATION_DISPLAY_VALUE;
    private static final int INFORMATION_HEIGHT = 30;
    private static final int INFORMATION_WIDTH = 30;
    private static final int INFORMATION_THICKNESS = 5;
    private static final int LABEL_THICKNESS = 3;
    private static final int INFORMATION_X = 8;  // dig line into object, if no contour is applied (x)
    private static final int INFORMATION_Y = 0;  // as above, (y)
    private static final int TEXT_FONT_STYLE = 3;
    private static final int TEXT_FONT_SCALE = 1;
    private static final int TEXT_THICKNESS = 2;
    private static final int TEXT_LINE = 0;
    private static final int TEXT_MARGIN = 3 + LABEL_THICKNESS;




    //// global variable section
    // opencv specific variables
    private CameraBridgeViewBase OpenCVCameraView;
    private int OpenCVViewMode;


    // various general mats
    private Mat color;
    private Mat HsvValue;
    private Mat BorderMap;
    private Mat IntermediateMat;
    private Mat Maskcolor;

    // variables for hsv mode (including histogram operations)
    private Mat Hsv;
    private Mat HsvRange;
    private List<Mat> hsvHistograms;          // saves histograms for hue and saturation
    private Mat OriginalMat;
    private MatOfInt Channels[];
    private MatOfInt HistSize;
    private MatOfFloat Ranges;
    private Scalar HueColor[];
    private Scalar White;

    // control variables
    private int detectorChoice = MAX_DETECTOR_MODE;
    private boolean Pause;    // freeze camera, e.g. during input operations

    //// Tracking Targets
    // vector for user defined filter values: hMin, sMin, vMin, hMax, sMax, vMax
    // initialized with values for a good detection of a banana
    private int[] HsvFilterValues = new int[]{0, 36, 141, 44, 188, 255};

    // objects for food and food type handling
    private List<FoodType> foodTypes = new ArrayList<>();
    private List<Food> detectedFoods = new ArrayList<>();
    private Food detectedFood;

    // objects for tracking of detected foods --> using for remembering past states
    private List<LinkedList<Food>> foodTracker = new ArrayList<LinkedList<Food>>();
    private DbAccessFoodTypes dbFoodTypes;


    /////////////////////////////////// END INITIALIZATION /////////////////////////////////////////



    /**
     * empty standard constructor
     *
     */
    public onGetImageListener() {}


    /**
     * executes various initialization operations
     * - initializes camera view from opencv
     * - initializes food type list with data from database
     * - initializes user interface
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(LABEL, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_on_get_image_listener);

        // check for permissions if API is lvl 23 or higher
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    // permission available, no further steps needed
                } else {
                    // ask for permission of camera
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSIONS);
                }
            }
        }

        //// initialize variables
        // initialize opencv variables
        OpenCVCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_main_surface_view);
        OpenCVCameraView.setCvCameraViewListener(this);
        OpenCVCameraView.enableFpsMeter();

        // initialize existing foodTypes from database
        initializeFromDatabase();

        // build seekbars and disable  them, since hsv mode is not default
        buildRangeSeekBars();

        // build floating button and menu
        buildFab();
    }


    /**
     * cleanup operations, if app is paused
     *
     */
    @Override
    public void onPause() {
        super.onPause();
        if (OpenCVCameraView != null)
            OpenCVCameraView.disableView();
    }


    /**
     * cleanup operations, if app is resumed
     *
     */
    @Override
    public void onResume() {
        super.onResume();
        OpenCVCameraView.enableView();
        OpenCVCameraView.setOnTouchListener(onGetImageListener.this);
    }


    /**
     * cleanup operations, if app is resumed
     *
     */
    public void onDestroy() {
        super.onDestroy();
        if (OpenCVCameraView != null)
            OpenCVCameraView.disableView();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    // feedback to user for not allowing camera
                    String text = "Sorry, without a camera this app does not work.";
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                    toast.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * initializes all mats, scalars and other variables, which are needed for the frame processing
     *
     * @param width     the width of the frames that will be delivered
     * @param height    the height of the frames that will be delivered
     */
    public void onCameraViewStarted(int width, int height) {
        // initialize main mats
        color = new Mat(height, width, CvType.CV_8UC4);
        BorderMap = new Mat(height, width, CvType.CV_8UC1);
        IntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        Hsv = new Mat(height, width, CvType.CV_8UC4);
        HsvValue = new Mat(height, width, CvType.CV_8UC4);
        HsvRange = new Mat(height, width, CvType.CV_8UC1);
        Maskcolor = new Mat(height, width, CvType.CV_8UC3);

        // initialize variables for hsv histograms
        IntermediateMat = new Mat();
        hsvHistograms = new ArrayList<>(Arrays.asList(
                new Mat(HIST_SIZE, 1, CvType.CV_32F),
                new Mat(HIST_SIZE, 1, CvType.CV_32F)));
        Channels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
        HistSize = new MatOfInt(HIST_SIZE);
        Ranges = new MatOfFloat(0f, 256f);
        OriginalMat = new Mat();
        White = Scalar.all(255);

        // fill hue scalar with as much values as histogram bars (HIST_SIZE)
        HueColor = createEvenHueScalar(HIST_SIZE);

        // initialize other variables
        Pause = false;
    }


    /**
     * cleanup operations, when camera view stops
     *
     */
    public void onCameraViewStopped() {
        color.release();
        BorderMap.release();
        IntermediateMat.release();

        Hsv.release();
        HsvValue.release();
        HsvRange.release();
        Maskcolor.release();
    }


    /**
     * starting point for every frame, arranges steps depending on the active mode
     *
     * @param inputFrame        contains data from the live frame of the camera
     * @return                  returns rgba frame as a result of the frame handling processes
     *                          to display it in the next step
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        final int VIEW_MODE = OpenCVViewMode;

        if (!Pause) {
            switch (VIEW_MODE) {
                case COLOUR_DETECTION_VIEW:
                    // input frame has RBGA format
                    color = inputFrame.rgba();
                    Imgproc.cvtColor(color, HsvValue, Imgproc.COLOR_RGB2HSV_FULL);

                    // execute food detection, if at least one food type is saved
                    if (foodTypes.size() > 0)
                        for (FoodType ft : foodTypes)
                            detectFoodType(ft);

                    // handle detected foods and clear the list afterwards
                    if (detectedFoods.size() > 0)
                        for (Food f : detectedFoods)
                            handleDetectedFood(f, false);
                    detectedFoods.clear();

                    break;
                case BORDER_DETECTION_VIEW:
                    // initialize needed mats
                    BorderMap = inputFrame.gray();
                    color = inputFrame.rgba();
                    Imgproc.Canny(inputFrame.gray(), IntermediateMat, 80, 100);
                    Imgproc.cvtColor(IntermediateMat, color, Imgproc.COLOR_GRAY2RGBA, 4);

                    break;
                case KEYPOINTS_DETECTION_VIEW:
                    // initialize needed mats
                    BorderMap = inputFrame.gray();
                    color = inputFrame.rgba();
                    ARNutritionJNI.findFeatures(BorderMap.getNativeObjAddr(),
                            color.getNativeObjAddr(), detectorChoice);

                    break;
                case HSV_FILTER_VIEW:
                    // initialize needed mats -> backup mats to be able to reset in loops
                    color = inputFrame.rgba();
                    Imgproc.cvtColor(color, HsvValue, Imgproc.COLOR_RGB2HSV_FULL);

                    // apply hsv filter from range seek bar settings as a mask to the screen
                    // also show color histograms of hue and saturation
                    handleHsvMode();

                    // execute food detection, if at least one food type is saved
                    if (foodTypes.size() > 0)
                        for (FoodType ft : foodTypes)
                            detectFoodType(ft);

                    // handle detected foods and clear the list afterwards
                    if (detectedFoods.size() > 0)
                        for (Food f : detectedFoods)
                            handleDetectedFood(f, true);
                    detectedFoods.clear();

                    break;
            }
        }
        return color;
    }


    /**
     * handle onClick events, such as the manipulation of hsv filter values
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        // variable declaration
        Context context = getApplicationContext();
        String text = "";
        int length = Toast.LENGTH_SHORT;

        // find out, which button was pressed and execute corresponding actions
        if (v.getTag().equals(HSV_FILTER_MODE)) {
            // handle the option 'hsv mode'
            Log.i(LABEL, "Activated hsv filter");
            setHsvModeGuiVisibility(true);

            // if it's already activated, ask for input to save current hsv filter values
            if (OpenCVViewMode != HSV_FILTER_VIEW) {
                OpenCVViewMode = HSV_FILTER_VIEW;

                // prepare tutorial text and set toast length to long,
                // since it's fairly a bit of text
                if (!DEACTIVATE_HSV_MODE) {        // skip the introduction, when debugging, since it's long
                    length = Toast.LENGTH_LONG;
                    int lengthHsvIntroduction = 3;
                    text = "HSV filter mode for food type storing" +
                            "\nTop left: HSV range filter" +
                            "\nBottom left: Color histograms (hue, saturation)" +
                            "\nPress again the 'hsv filter mode' button to save the food!";

                    // show toast multiple times, since TOAST_LONG is still too short
                    for (int i = 0; i < lengthHsvIntroduction; i++) {
                        Toast toast = Toast.makeText(context, text, length);
                        toast.show();
                    }
                }
            } else {
                dialogInputSaveFoodType();
            }
        } else {
            // disable seekbars
            Log.i(LABEL, "Activated another mode");
            setHsvModeGuiVisibility(false);

            // reset detectorChoice when another mode is chosen
            // -> initialization with last mode so it starts with first one
            if (!(v.getTag().equals(LABEL_FEATURES))) detectorChoice = MAX_DETECTOR_MODE;

            // handle the selection of all modes besides the hsv mode
            if (v.getTag().equals(DETECTION_MODE)) {
                // handle the option 'normal mode'
                Log.i(LABEL, "activated rgba mode");
                OpenCVViewMode = COLOUR_DETECTION_VIEW;

                // prepare introduction text
                text = "RGB camera mode with active food detection.";
            } else if (v.getTag().equals(EDGE_DETECTION)) {
                // handle the option 'edge mode'
                Log.i(LABEL, "activated canny mode");
                OpenCVViewMode = BORDER_DETECTION_VIEW;

                // prepare introduction text
                text = "Explore the edges in your environment!";
            } else if (v.getTag().equals(LABEL_FEATURES)) {
                // handle the option 'feature mode'
                Log.i(LABEL, "activated feature mode");
                OpenCVViewMode = KEYPOINTS_DETECTION_VIEW;
                detectorChoice = (detectorChoice + 1) % (MAX_DETECTOR_MODE + 1);

                // prepare introduction text and increase length, since it's longer
                text = "Explore features with different detectors!";
                length = Toast.LENGTH_LONG;

                // give feedback to chosen mode
                switch (detectorChoice) {
                    case 0:
                        text += "\nFeature detector: FAST";
                        break;
                    case 1:
                        text += "\nFeature detector: AGAST";
                        break;
                    case 2:
                        text += "\nFeature detector: GFTT";
                        break;
                    case 3:
                        text += "\nFeature detector: SimpleBlobDetector";
                        break;
                    default:
                        text += "\nError in detectorChoice feedback";
                        Log.i(LABEL, "Error in detectorChoice feedback");
                        break;
                }
                text += "\nPress again for another detector!";
            } else {
                // handle exceptions
                Log.i(LABEL, "error in mode activation");
                OpenCVViewMode = COLOUR_DETECTION_VIEW;
            }
            // show prepared toast
            Toast toast = Toast.makeText(context, text, length);
            toast.show();
        }
    }


    /**
     * handle onTouch events (such as displaying nutritional values to a touched food)
     *
     * @param arg0      standard transfer variable
     * @param event     standard transfer variable, which contains information about the touch event
     * @return
     */
    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        // variable declaration
        double[] coordinates = new double[2];
        Point touchedPoint;

        // define size of color
        double cols = color.cols();// color is your image frame
        double rows = color.rows();

        // determine size of used screen and define offset of color frame
        int width = OpenCVCameraView.getWidth();
        int height = OpenCVCameraView.getHeight();
        double scaleFactor = cols / width;
        double xOffset = (width * scaleFactor - cols) / 2;
        double yOffset = (height * scaleFactor - rows) / 2;

        // temporary variable for conversion tasks
        MatOfPoint2f TempMat = new MatOfPoint2f();

        // determine coordinates of touched point in color frame and create a Point for the result
        coordinates[0] = (event).getX() * scaleFactor - xOffset;
        coordinates[1] = (event).getY() * scaleFactor - yOffset;
        touchedPoint = new Point(coordinates[0], coordinates[1]);

        //// check depending on detection scale if a touch occured within the contour of a food
        // convert contour to needed format
        for(LinkedList<Food> currentList : foodTracker)
            if (currentList.size() > 0) {
                if (currentList.getLast() != null) {
                    currentList.getLast().getContour().convertTo(TempMat, CvType.CV_32FC2);

                    // toggle, that information should be displayed, since a touch
                    // occured within the contour
                    if (Imgproc.pointPolygonTest(TempMat, touchedPoint, false) > 0) {
                        // set, that information is desired, if it's not set - reset, if it's set
                        if (!currentList.getLast().getIsInformationDisplayed())
                            currentList.getLast().setIsInformationDisplayed(true);
                        else
                            currentList.clear();
                    }
                }
            }

        return false;       //false: no subsequent events ; true: subsequent events
    }


    ///////////////////////////////// END ACTIVITY CYCLE METHODS ///////////////////////////////////


    /**
     * initialize foodTypes with saved values from database
     *
     */
    public void initializeFromDatabase() {
        // get database
        dbFoodTypes = new DbAccessFoodTypes(this);

        // get data if table already exists
        if (dbFoodTypes.isTableExisting(dbFoodTypes.TABLE_NAME_FOOD_TYPES)) {
            // process data
            foodTypes = dbFoodTypes.getAllData();
        }

        // update food tracker
        for(FoodType ft : foodTypes)
            foodTracker.add(new LinkedList<Food>());
    }


    /**
     * create floating main button and floating menu, which is activated through a click on the
     * main button
     *
     */
    public void buildFab() {
        // create floating button
        final ImageView mainFab = new ImageView(this);
        mainFab.setImageResource(R.drawable.ic_plus);
        final FloatingActionButton actionButton = new FloatingActionButton.Builder(this)
                .setContentView(mainFab)
                .setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.selector_button_gold, getTheme()))
                .build();

        // create menu items
        final ImageView fabModeNormal = new ImageView(this);
        final ImageView fabModeEdge = new ImageView(this);
        final ImageView fabModeFeatures = new ImageView(this);
        final ImageView fabModeHsvFilter = new ImageView(this);
        fabModeNormal.setImageResource(R.drawable.ic_camera);
        fabModeEdge.setImageResource(R.drawable.ic_edge);
        fabModeFeatures.setImageResource(R.drawable.ic_feature);
        fabModeHsvFilter.setImageResource(R.drawable.ic_filter);

        // create menu buttons
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        itemBuilder.setBackgroundDrawable(getResources()
                .getDrawable(R.drawable.selector_button_green, getTheme()));
        SubActionButton buttonFabModeNormal = itemBuilder.setContentView(fabModeNormal).build();
        itemBuilder.setBackgroundDrawable(getResources()
                .getDrawable(R.drawable.selector_button_blue, getTheme()));
        SubActionButton buttonFabModeEdge = itemBuilder.setContentView(fabModeEdge).build();
        itemBuilder.setBackgroundDrawable(getResources()
                .getDrawable(R.drawable.selector_button_red, getTheme()));
        SubActionButton buttonFabModeFeatures = itemBuilder.setContentView(fabModeFeatures).build();
        itemBuilder.setBackgroundDrawable(getResources()
                .getDrawable(R.drawable.selector_button_purple, getTheme()));
        SubActionButton buttonFabModeHsvFilter =
                itemBuilder.setContentView(fabModeHsvFilter).build();

        // set tags
        buttonFabModeNormal.setTag(DETECTION_MODE);
        buttonFabModeEdge.setTag(EDGE_DETECTION);
        buttonFabModeFeatures.setTag(LABEL_FEATURES);
        buttonFabModeHsvFilter.setTag(HSV_FILTER_MODE);
        //  set onClickListener
        buttonFabModeNormal.setOnClickListener(this);
        buttonFabModeEdge.setOnClickListener(this);
        buttonFabModeFeatures.setOnClickListener(this);
        buttonFabModeHsvFilter.setOnClickListener(this);


        // create floating menu
        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(buttonFabModeNormal)
                .addSubActionView(buttonFabModeEdge)
                .addSubActionView(buttonFabModeFeatures)
                .addSubActionView(buttonFabModeHsvFilter)
                .attachTo(actionButton)
                .build();


        // create animation for the main fab, so it becomes an x (for exit) when active
        actionMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
                // rotate the icon of rightLowerButton 45 degrees clockwise
                mainFab.setRotation(0);
                PropertyValuesHolder propertyValuesHolder =
                        PropertyValuesHolder.ofFloat(View.ROTATION, 45);
                ObjectAnimator animation =
                        ObjectAnimator.ofPropertyValuesHolder(mainFab, propertyValuesHolder);
                animation.start();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                // rotate the icon of rightLowerButton 45 degrees counter-clockwise
                mainFab.setRotation(45);
                PropertyValuesHolder propertyValuesHolder =
                        PropertyValuesHolder.ofFloat(View.ROTATION, 0);
                ObjectAnimator animation =
                        ObjectAnimator.ofPropertyValuesHolder(mainFab, propertyValuesHolder);
                animation.start();
            }
        });
    }


    /**
     * create RangeSeekBars for setting HSV ranges in HSV Filter mode
     *
     */
    public void buildRangeSeekBars() {
        // setup the new range seek bars
        final RangeSeekBar<Integer> FilterH = new RangeSeekBar<>(this);
        final RangeSeekBar<Integer> FilterS = new RangeSeekBar<>(this);
        final RangeSeekBar<Integer> FilterV = new RangeSeekBar<>(this);

        // set the range
        FilterH.setRangeValues(0, 255);
        FilterH.setSelectedMinValue(HsvFilterValues[0]);
        FilterH.setSelectedMaxValue(HsvFilterValues[3]);
        FilterS.setRangeValues(0, 255);
        FilterS.setSelectedMinValue(HsvFilterValues[1]);
        FilterS.setSelectedMaxValue(HsvFilterValues[4]);
        FilterV.setRangeValues(0, 255);
        FilterV.setSelectedMinValue(HsvFilterValues[2]);
        FilterV.setSelectedMaxValue(HsvFilterValues[5]);


        // set onChangeListeners for all three range seek bars
        FilterH.setOnRangeSeekBarChangeListener(
                new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer miAugmentValue,
                                                            Integer maxValue) {
                        // handle changed range values
                        HsvFilterValues[0] = miAugmentValue;
                        HsvFilterValues[3] = maxValue;
                    }
                });

        FilterS.setOnRangeSeekBarChangeListener(
                new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer miAugmentValue,
                                                            Integer maxValue) {
                        // handle changed range values
                        HsvFilterValues[1] = miAugmentValue;
                        HsvFilterValues[4] = maxValue;
                    }
                });

        FilterV.setOnRangeSeekBarChangeListener(
                new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer miAugmentValue,
                                                            Integer maxValue) {
                        // handle changed range values
                        HsvFilterValues[2] = miAugmentValue;
                        HsvFilterValues[5] = maxValue;
                    }

                });

        // add RangeSeekBars to layout
        LinearLayout layout = (LinearLayout) findViewById(R.id.seekbar_placeholder);
        layout.addView(FilterH);
        layout.addView(FilterS);
        layout.addView(FilterV);

        setLinearLayoutVisibility(false, layout);
    }


    /**
     * updates range bar values to the ones defines in the transfer variable
     *
     * @param values        defines values, with which the range bars need to be updated
     */
    public void setRangeBarValues(int[] values) {
        // variable declaration and initialization
        RangeSeekBar<Integer> RangeSeekBar;
        LinearLayout layout = (LinearLayout) findViewById(R.id.seekbar_placeholder);

        // set values of all rangeseekbars according to given values
        for(int i=0; i < 3; i++) {
            // get a range seek bar
            RangeSeekBar = (RangeSeekBar<Integer>) layout.getChildAt(i);

            // set the range bar values
            RangeSeekBar.setSelectedMinValue(values[i]);
            RangeSeekBar.setSelectedMaxValue(values[i + 3]);

            // update the HsvFilterValues
            HsvFilterValues[i] = values[i];
            HsvFilterValues[i+3] = values[i+3];
        }
    }


    /**
     * function for visibility setting of all seekbars in defined LinearLayout
     *
     * @param visible       determines, if items are made visible or gone (invisible-like)
     * @param layoutParent  determines layout parent item, whose children are made (in-)visible
     */
    public void setLinearLayoutVisibility(boolean visible, LinearLayout layoutParent) {
        int childCount = layoutParent.getChildCount();

        Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                android.R.anim.fade_out);
        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                android.R.anim.fade_in);


        if (visible) {
            layoutParent.setAnimation(animFadeIn);
            for (int i = 0; i < childCount; i++) {
                View v = layoutParent.getChildAt(i);
                v.setVisibility(View.VISIBLE);
            }
        } else {
            layoutParent.setAnimation(animFadeOut);
            for (int i = 0; i < childCount; i++) {
                View v = layoutParent.getChildAt(i);
                v.setVisibility(View.GONE);
            }
        }
    }


    /**
     * creates an array of scalars, according to defined size, with an even color range
     * for the visualization of hue vector values
     *
     * @param size      specifies the size of the scalar vector
     */
    public Scalar[] createEvenHueScalar(int size) {
        // variable declaration
        Scalar[] HueColor = new Scalar[size];
        double IntermediateSize = 255/(STEPS_PER_PHASE - 1);
        double r = 255, g = 0, b = 0;                   // starting values for the scalar
        double progress = 0;
        double channelValue;


        /*
            * create steady scalar interpolation between
            * How to create a hue scalar:
            * start with 255,0,0 (rgb)
            * phase 1: increase g to max (255)
            * phase 2: decrease r to min (0)
            * phase 3: increase b to max
            * phase 4: increase r to max
            * phase 5: decrease b to min
            * progress: 0...5x255
        */
        for (int i=0; i < size; i++) {

            channelValue = progress % 255;

            if (progress < 255) {
                g = channelValue;
            }
            else if (progress < 255*2 && progress >= 255) {
                if (g < 255) g = 255;
                r -= channelValue;
            }
            else if (progress < 255*3 && progress >= 255*2) {
                if (r > 0) r = 0;
                b = channelValue;
            }
            else if (progress < 255*4 && progress >= 255*3) {
                if (b < 255) b = 255;
                r = channelValue;
            }
            else if (progress < 255*5 && progress >= 255*4) {
                if (r < 255) r = 255;
                b -= channelValue;
                if (i == (size -1)) b = 0;      // safety measures for the last iteration
            }

            // create new scalar with calculated values
            HueColor[i] = new Scalar(r, g, b, 255);

            // progress 1 step further
            progress += IntermediateSize;
        }
        return HueColor;
    }


    /**
     * handles the hsv mode: applies mask to live screen, depending on the hsv filter values
     * of the range seek bars, also show color histogram of the filtered image
     *
     */
    public void handleHsvMode() {
        // variable declaration
        List<MatOfPoint> contours;
        MatOfPoint LargestContour;

        // copy rgb frame to temporary mat
        color.copyTo(IntermediateMat);

        // apply hsv filter values to screen
        getHsvFilterMask(HsvFilterValues);

        // find biggest contour in binary mask from hsv filter mask
        contours = findContoursAboveThreshold(DETECTED_DESIRED_CONTOUR);

        // apply biggest contour as a mask to frame, if a contour was found
        if (contours.size() > 0) {
            // get the biggest contour
            LargestContour = contours.get(0);
            Log.e(DEBUG, "Biggest Contour Area: " + Imgproc.contourArea(LargestContour));

            // apply biggest contour as a new mask (with reseted HsvRange) to rgb frame
            Core.setIdentity(HsvRange, new Scalar(0, 0, 0));
            Imgproc.drawContours(HsvRange,
                    new ArrayList<>(Arrays.asList(LargestContour)), 0, White, -1);
            Imgproc.cvtColor(HsvRange, Maskcolor, Imgproc.COLOR_GRAY2RGBA, 4);
            Core.bitwise_and(IntermediateMat, Maskcolor, IntermediateMat);
            Imgproc.drawContours(IntermediateMat,
                    new ArrayList<>(Arrays.asList(LargestContour)), 0, White, 1);
        }
        else {
            // apply mask to rgba frame without using any contour information
            Imgproc.cvtColor(HsvRange, Maskcolor, Imgproc.COLOR_GRAY2RGBA, 4);
            Core.bitwise_and(IntermediateMat, Maskcolor, IntermediateMat);
        }

        // give feedback about histograms of picture in hsv mode (calc them and then draw them)
        drawHistograms(calcHsvHistograms());
        IntermediateMat.copyTo(color);
    }


    /**
     * executes the whole food detection process, shows histograms and mask in hsv mode
     *
     * @param ft         defines food type, for which the check occures
     */
    public void detectFoodType(FoodType ft) {
        // variable declaration for temporary saving
        Food detectedFood = null;
        List<MatOfPoint> contours;
        MatOfPoint LargestContour;
        List<Mat> PresentHist;
        LinkedList<Food> currentList = foodTracker.get(foodTypes.indexOf(ft));


        //// handle single detection --> use largest contour of food candidates
        // apply hsv filter from currently checked food type to screen
        getHsvFilterMask(ft.getHsvFilterValues());

        // find biggest contour in binary mask from hsv filtered frame
        // (uses: HsvRange for contour searching, without modifying it (uses clone))
        contours = findContoursAboveThreshold(DETECTED_DESIRED_CONTOUR);

        // reject further actions if there's no contour within the bottom and top threshold
        if (contours.size() > 0) {
            // get the biggest contour
            LargestContour = contours.get(0);

            //// apply the biggest contour as a new mask to reseted mask
            // reset needed mats
            Core.setIdentity(HsvRange, new Scalar(0, 0, 0));
            color.copyTo(IntermediateMat);

            // draw contour
            Imgproc.drawContours(HsvRange,
                    new ArrayList<>(Arrays.asList(LargestContour)), 0, White, -1);

            // convert to rgba mask and apply mask to rgb frame
            Imgproc.cvtColor(HsvRange, Maskcolor, Imgproc.COLOR_GRAY2RGBA, 4);
            Core.bitwise_and(IntermediateMat, Maskcolor, IntermediateMat);

            // calculate the histograms from IntermediateMat
            PresentHist = calcHsvHistograms();

            // create new food with detected type and accompanying contour and handle it
            if (compareHsHistograms(ft, PresentHist)) {
                detectedFood = new Food(ft, LargestContour);

                // information should be displayed, when the detected type appeared in the last
                // few frames (according to FRAMES_TRACKING_RANGE)
                if (currentList.size() > 0)
                    for (Food f : currentList)
                        if (f != null)
                            if (f.getIsInformationDisplayed() && (f.getType().getName()
                                    .compareToIgnoreCase(ft.getName()) == 0))
                                detectedFood.setIsInformationDisplayed(true);

                // save found object in tracker to remember the state (fifo list)
                if (currentList.size() < FRAMES_TRACKING_RANGE)
                    currentList.add(detectedFood);
                else {
                    currentList.removeFirst();
                    currentList.add(detectedFood);
                }

                //save detected food
                detectedFoods.add(detectedFood);

                // clear object of detected food, so it's clean for the next frame
                detectedFood = null;
            } else {
                // put null as an object into the list, so it can act as a counter for frames
                // with abandoned candidates
                if (currentList.size() < FRAMES_TRACKING_RANGE)
                    currentList.add(null);
                else {
                    currentList.removeFirst();
                    currentList.add(null);
                }
            }
        } else {
            // put null as an object into the list, so it can act as a counter for frames
            // without detected objects of this food type
            if (currentList.size() < FRAMES_TRACKING_RANGE)
                currentList.add(null);
            else {
                currentList.removeFirst();
                currentList.add(null);
            }
        }
    }


    /**
     * applies defined filter values to hsv image, copies result to intermediate mat
     * also be done in the background - another result is a binary mask
     * (mask is saved in HsvRange)
     * (modified global variable: IntermediateMat gets original rgba values)
     * (modified global variable: Hsv gets hsv values from original rgba values)
     * (modified global variable: HsvRange gets mask from hsv image with
     *      applied hsv filter values)
     *
     * @param hsvFilterValues       defines the min/max values for h, s and v filtering
     */
    public void getHsvFilterMask(int[] hsvFilterValues) {
        // create elements for morphing operations
        Mat Eroding = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, CV_ERODE);
        Mat Dilating = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, CV_DILATE);

        // save color value (live screen) temporary in IntermediateMat, to use the latter here
        HsvValue.copyTo(Hsv);

        // apply hsv filter values to hsv frame
        Core.inRange(Hsv,
                new Scalar(hsvFilterValues[0], hsvFilterValues[1], hsvFilterValues[2]),
                new Scalar(hsvFilterValues[3], hsvFilterValues[4], hsvFilterValues[5]),
                HsvRange);

        //// erode, dilate and blur frame to get rid of interference
        // erode and dilate in each two iterations
        Imgproc.erode(HsvRange, HsvRange, Eroding);
        Imgproc.erode(HsvRange, HsvRange, Eroding);
        Imgproc.dilate(HsvRange, HsvRange, Dilating);
        Imgproc.dilate(HsvRange, HsvRange, Dilating);
    }


    /**
     * calculates hsv histograms and feedback graph and puts the feedback graph of the color
     * histograms (hue, saturation) into a copy of color (IntermediateMat), so it can also
     * be done in the background without affecting the live frame
     * (needs rgba copy in IntermediateMat, which is converted to Hsv)
     *
     */
    public List<Mat> calcHsvHistograms() {
        // declaration and initialization of variables
        float MaxValue;
        float Buffer[] = new float[HIST_SIZE];
        Mat hist = new Mat(HIST_SIZE, 1, CvType.CV_32F);
        List<Mat> resultHistograms = new ArrayList<>();

        // get hsv mat from mat buffer (intermediate mat --> frame with applied mask)
        Imgproc.cvtColor(IntermediateMat, Hsv, Imgproc.COLOR_RGB2HSV_FULL);

        //// calculate and display histogram for hue
        Imgproc.calcHist(Arrays.asList(Hsv), Channels[0], OriginalMat, hist, HistSize, Ranges);

        //// normalize, so the values are [0..1] and reset the first value, when it's at maximum
        //// (this happens, when the mask is applied, since a lot of area is black)
        Core.normalize(hist, hsvHistograms.get(0), 1, 0, Core.NORM_INF);
        hsvHistograms.get(0).get(0, 0, Buffer);

        // find out highest value
        MaxValue = 0;
        for (float d : Buffer)
            if (d > MaxValue) MaxValue = d;

        // reset first value to zero, if it's the highest value, or trim it to zero
        if (Buffer[0] == MaxValue) {
            // reset the freak value (comes from the mask probably)
            Buffer[0] = 0;

            // update histogram and normalize it again
            hsvHistograms.get(0).put(0, 0, Buffer);
            Core.normalize(hsvHistograms.get(0), hsvHistograms.get(0), 1, 0, Core.NORM_INF);
        }

        //// calculate and display histogram for saturation
        Imgproc.calcHist(Arrays.asList(Hsv), Channels[1], OriginalMat, hist, HistSize, Ranges);

        // normalize, so the values are [0..1] and reset the first value, when it's at maximum
        // (this happens, when the mask is applied, since a lot of area is black)
        Core.normalize(hist, hsvHistograms.get(1), 1, 0, Core.NORM_INF);
        hsvHistograms.get(1).get(0, 0, Buffer);

        // find out highest value
        MaxValue = 0;
        for (float d : Buffer)
            if (d > MaxValue) MaxValue = d;

        // reset first value to zero, if it's the highest value, or trim it to zero
        if (Buffer[0] == MaxValue) {
            Buffer[0] = 0;       //reset

            // update histogram and normalize it again
            hsvHistograms.get(1).put(0, 0, Buffer);
            Core.normalize(hsvHistograms.get(1), hsvHistograms.get(1), 1, 0, Core.NORM_INF);
        }

        // clone calculated histograms and return them as a result
        for (Mat m : hsvHistograms)
            resultHistograms.add(m.clone());

        return resultHistograms;
    }


    /**
     * draws the two first histograms from a given list
     *
     * @param histograms    defines list containing histograms
     */
    public void drawHistograms(List<Mat> histograms) {
        // constant declaration
        final double BAR_HEIGHT = color.size().height / 16 * 7;

        // declaration and initialization of variables
        int thickness = (color.width() - HIST_SETTING * (HIST_AMOUNT + 1)) /
                (HIST_SIZE * HIST_AMOUNT);
        if (thickness > 7) thickness = 7;               // limit thickness
        float Buffer[] = new float[HIST_SIZE];
        Point mPoint1 = new Point();
        Point mPoint2 = new Point();
        Mat hist = new Mat(HIST_SIZE, 1, CvType.CV_32F);

        // normalize to the desired height of the color histogram bars
        Core.normalize(hsvHistograms.get(0), hist, BAR_HEIGHT, 0, Core.NORM_INF);
        hist.get(0, 0, Buffer);
        for (int h = 0; h < HIST_SIZE; h++) {
            mPoint1.x = mPoint2.x = HIST_SETTING + h * thickness;
            mPoint1.y = color.height() - 1;
            mPoint2.y = mPoint1.y - (int) Buffer[h];
            Imgproc.line(IntermediateMat, mPoint1, mPoint2, HueColor[h], thickness);
        }

        // normalize to the desired height of the color histogram bars
        Core.normalize(histograms.get(1), hist, BAR_HEIGHT, 0, Core.NORM_INF);
        hist.get(0, 0, Buffer);
        for (int h = 0; h < HIST_SIZE; h++) {
            mPoint1.x = mPoint2.x = 2 * HIST_SETTING + (HIST_SIZE + h) * thickness;
            mPoint1.y = color.height() - 1;
            mPoint2.y = mPoint1.y - (int) Buffer[h];
            Imgproc.line(IntermediateMat, mPoint1, mPoint2, White, thickness);
        }
    }


    /**
     * applies HIST_BORDER_MIN to given histogram
     *
     * @param histograms    histograms, to which autofit should be applied
     * @return              returns a vector of new filter values
     */
    public int[] autofitHistograms(List<Mat> histograms) {
        // declaration and initialization of variables
        float Buffer[] = new float[HIST_SIZE];
        int[] hsvFilterValuesAutofit =
                new int[]{0,0,HsvFilterValues[2],255,255,HsvFilterValues[5]};
        int i = 0,j = 0, k = 0;

        // handle all available histograms
        for (Mat hist : histograms) {
            // get data from histogram
            hist.get(0, 0, Buffer);

            // apply left border --> set all histogram values to zero,
            // if they're beneath the threshold
            for (i = 0; i < Buffer.length && Buffer[i] < HIST_BORDER_MIN; i++)
                if (Buffer[i] < HIST_BORDER_MIN)
                    Buffer[i] = 0;

            // apply right border --> set all histogram values to zero,
            // if they're beneath the threshold
            for (j = Buffer.length-1; j > 0 && Buffer[j] < HIST_BORDER_MIN; j--)
                if (Buffer[j] < HIST_BORDER_MIN)
                    Buffer[j] = 0;

            // save data to histogram
            hist.put(0, 0, Buffer);

            // save settings, if the histogram consists of more than just zeros
            if (j > 0) {
                hsvFilterValuesAutofit[k] = i;
                hsvFilterValuesAutofit[k + 3] = j;
            }
            else if (j == 0) {
                hsvFilterValuesAutofit[k] = i;
                hsvFilterValuesAutofit[k + 3] = 255;
            }

            k++;
        }

        return hsvFilterValuesAutofit;
    }


    /**
     * find closest match to picture --> hs color histogram comparison with specified method
     *
     * @return      returns the detected FoodType, which is most likely the captured food
     */
    public boolean compareHsHistograms(FoodType ft, List<Mat> PresentHist) {
        double Highest_H_Value = 0;
        double Highest_S_Value = 0;
        boolean MatchFound = false;

        // find closest match to picture --> hs color histogram comparison with chi square method
        // histogram comparison for the currently checked food type
        double Comparison_H_Value = 0;
        double Comparison_S_Value = 0;

        // check if all histograms are available
        if (ft.getHsvHistogramH() != null && ft.getHsvHistogramS() != null
                && PresentHist.get(0) != null && PresentHist.get(1) != null) {

            // <debugSection>
            // compare histograms with all available methods and print results in the log
            if (ACTIVATE_COMPARISON) {
                String results;
                results = testHistogramComparisonMethods(ft, PresentHist);
                Log.e(HIST_COMP_TESTS, results);
            }
            // </debugSection>

            // compare histograms with intersect method
            Comparison_H_Value = Imgproc.compareHist(ft.getHsvHistogramH(),
                    PresentHist.get(0), HIST_COMPARISON_METHOD);
            Comparison_S_Value = Imgproc.compareHist(ft.getHsvHistogramS(),
                    PresentHist.get(1), HIST_COMPARISON_METHOD);

            if (Comparison_H_Value > Highest_H_Value) {
                Highest_H_Value = Comparison_H_Value;
            }
            if (Comparison_S_Value > Highest_S_Value) {
                Highest_S_Value = Comparison_S_Value;
            }

            Log.e(HIST_COMP_TESTS, ft.getName() + " - H: " + Comparison_H_Value +
                    " ; S:" + Comparison_S_Value);
        }

        // apply threshold, depending on the defined scope
        if (COMPARE_HUE) {
            if (Highest_H_Value >= HUE_MIN
                    && Highest_H_Value <= HUE_MAX) {
                MatchFound = true;
            }
        }
        else if (COMPARE_SATURATION) {
            if (Highest_S_Value >= SATURATION_MIN
                    && Highest_S_Value <= SATURATION_MAX) {
                MatchFound = true;
            }
        }
        else if (COMPARE_VALUE) {
            if (Highest_H_Value >= HUE_MIN
                    && Highest_H_Value <= HUE_MAX
                    && Highest_S_Value >= SATURATION_MIN
                    && Highest_S_Value <= SATURATION_MAX) {
                MatchFound = true;
            }
        }

        return MatchFound;
    }


    /**
     * finds biggest contour in HsvRange (binary mask from hsv threshold filtering) above
     * the defined threshold, or even all contours above this threshold,
     * depending on the parameter isOnlyBiggestDesired
     *
     * @param isOnlyBiggestDesired      defines, if the task is to find the biggest contour or all
     *                                  above the threshold
     * @return                          list of contours with biggest contour only are all above
     *                                  the threshold
     */
    public List<MatOfPoint> findContoursAboveThreshold(boolean isOnlyBiggestDesired) {
        //// prepare variables for getting the contours
        //// out of the binary image (filter threshold result)
        // general variables
        List<MatOfPoint> results = new ArrayList<>();
        Mat temp = new Mat();       // needed, since original mat is modified

        // contour specific variables
        List<MatOfPoint> contours = new ArrayList<>();
        MatOfPoint LargestContour = new MatOfPoint();

        // temporary variable for conversion tasks
        MatOfPoint2f TempMat1 = new MatOfPoint2f();
        MatOfPoint2f TempMat2 = new MatOfPoint2f();

        // apply hsv filter values from each registered food type and clone the resulting
        // binary mask into temporary Mat for further processing
        HsvRange.copyTo(temp);

        // find all contours in clone of binary mask
        Imgproc.findContours(temp, contours, new Mat(), Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);

        // security measures for the case of no found contours
        if (contours.size() > 0) {
            // walk through all contours
            for (int i = 0; i < contours.size(); i++) {
                // operations, if only the biggest contour is desired
                if (isOnlyBiggestDesired) {
                    // save index, if the contour is the biggest yet
                    if (i == 0)
                        LargestContour = contours.get(i);
                    else if (Imgproc.contourArea(contours.get(i))
                            > Imgproc.contourArea(LargestContour)
                            && Imgproc.contourArea(contours.get(i)) > CONTOUR_LEASTVALUE
                            && Imgproc.contourArea(contours.get(i)) < CONTOUR_MaxValue)
                        LargestContour = contours.get(i);
                }
                // operations, if multiple contours are desired
                else {
                    if (Imgproc.contourArea(contours.get(i)) > CONTOUR_LEASTVALUE
                            && Imgproc.contourArea(contours.get(i)) < CONTOUR_MaxValue)
                        results.add(contours.get(i));
                }
            }

            //// handle found contours
            if (isOnlyBiggestDesired) {
                // if there was found one above the threshold size: smooth
                // and save the found contour
                if (Imgproc.contourArea(LargestContour) > CONTOUR_LEASTVALUE
                        && Imgproc.contourArea(LargestContour) < CONTOUR_MaxValue) {
                    // convert mats of contour (forth and back) and smooth contour
                    LargestContour.convertTo(TempMat1, CvType.CV_32FC2);
                    Imgproc.approxPolyDP(TempMat1, TempMat2, CONTOUR_DISTANCE, true);
                    TempMat2.convertTo(LargestContour, CvType.CV_32S);
                    results.add(LargestContour);
                }
            }
            else {
                if (results.size()!=0) {
                    //smooth each found contours, if any are found
                    for (MatOfPoint mop : results) {
                        mop.convertTo(TempMat1, CvType.CV_32FC2);
                        Imgproc.approxPolyDP(TempMat1, TempMat2, CONTOUR_DISTANCE, true);
                        TempMat2.convertTo(mop, CvType.CV_32S);
                    }
                }
                else {
                    // no contour found
                    results = null;
                }
            }
        }

        return results;
    }


    /**
     * handle the detected food: draw contour, line and label
     *
     * @param f                     food, which should be handled
     * @param isContourDesired      determines, if contour should be drawn
     */
    public void handleDetectedFood(Food f, boolean isContourDesired) {
        // variable declaration
        Size textSize;
        int labelLine2Width;

        Point start;
        Point Point1;
        Point Point2;
        Point locationName;
        Point locationAddInformation;

        List<String> AddInformation = new ArrayList<>();
        String format = "%3.1f";  // width = 3 and 2 digits after the dot
        NutritionalValue AugmentValue;

        // safety measurement
        if (f != null) {
            // variable initialization
            textSize = Imgproc.getTextSize(f.getType().getName(), TEXT_FONT_STYLE, TEXT_FONT_SCALE,
                    TEXT_THICKNESS, null);
            labelLine2Width = (int) (textSize.width
                    + INFORMATION_DISPLAY_VALUE + BUFFER_TEXT);
            start =  f.getLocationLabel().clone();

            // draw contour if wished with transfer variable
            if (isContourDesired) {
                List<MatOfPoint> contours = new ArrayList<>(Arrays.asList(f.getContour()));
                Imgproc.drawContours(color, contours, 0, f.getType().getMarkerColor(),
                        2, 8, new Mat(), 0, new Point());
            }
            else {
                // dig line of label into object (otherwise it could float)
                start.x -= INFORMATION_X;
                start.y -= INFORMATION_Y;
            }

            // calculate points for the label lines and the text
            Point1 = new Point(start.x + INFORMATION_WIDTH, start.y - INFORMATION_HEIGHT);
            Point2 = new Point(Point1.x + labelLine2Width, Point1.y);
            locationName = new Point(Point1.x + INFORMATION_DISPLAY_VALUE, Point1.y - TEXT_MARGIN);

            // draw lines of label
            Imgproc.line(color, start, Point1, f.getType().getMarkerColor(),
                    INFORMATION_THICKNESS);
            Imgproc.line(color, Point1, Point2, f.getType().getMarkerColor(), LABEL_THICKNESS);

            // draw text of label with name of detected food type
            Imgproc.putText(color, f.getType().getName(), locationName,
                    TEXT_FONT_STYLE, TEXT_FONT_SCALE, f.getType().getMarkerColor(),
                    TEXT_THICKNESS, TEXT_LINE, false);

            // display further information, if it's desired (state of food)
            if (f.getIsInformationDisplayed()) {
                // prepare string for further information
                AugmentValue = f.getType().getNutritionalValue();

                // security measures
                if (AugmentValue != null) {
                    AddInformation.add("" + (int) (AugmentValue.getCalorie() *
                            AugmentValue.getMeanWeight()/100) + " kcal");
                    AddInformation.add("Protein: " + String.format(Locale.ENGLISH, format,
                            (AugmentValue.getProtien() *
                                    AugmentValue.getMeanWeight()/100)) + "g");
                    AddInformation.add("Carbs: " + String.format(Locale.ENGLISH, format,
                            (AugmentValue.getCarbohydrate() *
                                    AugmentValue.getMeanWeight()/100)) + "g");
                    AddInformation.add("Fat: " + String.format(Locale.ENGLISH, format,
                            (AugmentValue.getFat() *
                                    AugmentValue.getMeanWeight()/100)) + "g");
                    AddInformation.add("Weight: "
                            + (int) AugmentValue.getMeanWeight() + "g");
                }

                // prepare location
                locationAddInformation = new Point(Point1.x, Point1.y + TEXT_MARGIN + textSize.height);

                // check if any text was prepared yet before drawing it
                if (!AddInformation.isEmpty()) {
                    for(int i=0; i < AddInformation.size(); i++) {
                        Imgproc.putText(color, AddInformation.get(i),
                                new Point(locationAddInformation.x,
                                        locationAddInformation.y
                                                + i*(textSize.height + TEXT_MARGIN)),
                                TEXT_FONT_STYLE, TEXT_FONT_SCALE, f.getType().getMarkerColor(),
                                TEXT_THICKNESS, TEXT_LINE, false);
                    }
                }
            }
        }
    }


    /**
     * method for testing all histogram comparison methods from opencv with given food type on
     * live frames
     *
     * @param ft                    FoodType, which histogram is compared with the live screen
     * @return Result    String with all formatted results, can be used for the log
     */
    public String testHistogramComparisonMethods(FoodType ft, List<Mat> PresentHist) {
        // variable declaration for string and format of results
        String Result;
        String format = "%7.2f";  // width = 7 and 2 digits after the dot

        // compare h and s histogram and add results to the corresponding string
        Result = "Food type: " + ft.getName() + "\n";
        Result += "Method: CV_COMP_CHISQR        ---   Values: H: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramH(),
                        PresentHist.get(0), Imgproc.CV_COMP_CHISQR)) + ", S: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramS(),
                        PresentHist.get(1), Imgproc.CV_COMP_CHISQR)) + "\n";
        Result += "Method: CV_COMP_CHISQR_ALT    ---   Values: H: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramH(),
                        PresentHist.get(0), Imgproc.CV_COMP_CHISQR_ALT)) + ", S: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramS(),
                        PresentHist.get(1), Imgproc.CV_COMP_CHISQR_ALT)) + "\n";
        Result += "Method: CV_COMP_INTERSECT     ---   Values: H: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramH(),
                        PresentHist.get(0), Imgproc.CV_COMP_INTERSECT)) + ", S: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramS(),
                        PresentHist.get(1), Imgproc.CV_COMP_INTERSECT)) + "\n";
        Result += "Method: CV_COMP_BHATTACHARYYA ---   Values: H: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramH(),
                        PresentHist.get(0), Imgproc.CV_COMP_BHATTACHARYYA)) + ", S: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramS(),
                        PresentHist.get(1), Imgproc.CV_COMP_BHATTACHARYYA)) + "\n";
        Result += "Method: CV_COMP_CORREL        ---   Values: H: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramH(),
                        PresentHist.get(0), Imgproc.CV_COMP_CORREL)) + ", S: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramS(),
                        PresentHist.get(1), Imgproc.CV_COMP_CORREL)) + "\n";
        Result += "Method: CV_COMP_HELLINGER     ---   Values: H: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramH(),
                        PresentHist.get(0), Imgproc.CV_COMP_HELLINGER)) + ", S: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramS(),
                        PresentHist.get(1), Imgproc.CV_COMP_HELLINGER)) + "\n";
        Result += "Method: CV_COMP_KL_DIV        ---   Values: H: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramH(),
                        PresentHist.get(0), Imgproc.CV_COMP_KL_DIV)) + ", S: " +
                String.format(Locale.ENGLISH, format, Imgproc.compareHist(ft.getHsvHistogramS(),
                        PresentHist.get(1), Imgproc.CV_COMP_KL_DIV)) + "\n";

        return Result;
    }


    /**
     * Creates a dialog box and asks for a name of the currently filtered object
     * afterwards saves the object with calculated histograms as a food type, locally and
     * also as an update in the database
     *
     */
    public void dialogInputSaveFoodType() {
        // initialization of dialog
        String Title = "Name of currently filtered food:";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Title);

        // use an EditText view to get user input.
        final EditText input = new EditText(this);
        builder.setView(input);

        // define actions for left button of dialog
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // variable declaration
                String name = input.getText().toString();

                // save food type
                saveFoodType(name);

                // abolish frame freeze, which was initiated for inputs
                Pause = false;
                return;
            }
        });

        // define actions for right button of dialog
        builder.setNegativeButton("Clear Database", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // clear locally saved food types and also the ones in the database
                foodTypes.clear();
                dbFoodTypes.clear(dbFoodTypes.TABLE_NAME_FOOD_TYPES);

                String text = "Registered food types deleted.";
                Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                toast.show();

                // abolish frame freeze, which was initiated for inputs
                Pause = false;
            }
        });

        // resets frozen screen, if user cancels with a touch anywhere outside the dialog box
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Pause = false;
            }
        });

        // freeze camera, so the current picture is saved instead of the live picture
        // unfreeze, when a button is pressed (unfreeze action declared in button declarations)
        Pause = true;

        // now show this dialog
        builder.show();
    }


    /**
     * saves food type with given name
     * gives feedback, if name was empty and nothing was saved
     *
     * @param name      defines name of the to be saved food type
     */
    public void saveFoodType(String name) {
        // variable declaration
        FoodType ft;

        if (!name.isEmpty()) {
            // clone hsv histograms since otherwise just references are copied
            List<Mat> clonedHistograms = new ArrayList<>();
            //List<Mat> clonedHistograms = new ArrayList<>(hsvHistograms);

            // clone histograms
            for (Mat m : hsvHistograms)
                clonedHistograms.add(m.clone());

            // apply autofit to the histograms and apply new histogram borders to
            // hsv filter settings
            //HsvFilterValues = autofitHistograms(clonedHistograms);
            setRangeBarValues(autofitHistograms(clonedHistograms));

            // create food type with current data and name
            ft = new FoodType(name, HsvFilterValues.clone(), clonedHistograms.get(0),
                    clonedHistograms.get(1), null, null, getNutritionalValue(name));

            // register the new food type to the local food type list and also save it in the
            // database, and in the tracker (every food type id of the tracker is thus
            // corresponding with the id in the foodTypes List)
            foodTypes.add(ft);
            dbFoodTypes.add(ft);
            foodTracker.add(new LinkedList<Food>());
        } else {
            String text = "No name was entered, thus nothing could be saved.\n" +
                    "Please enter a name the next time.";
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    /**
     * makes all declared gui elements of the hsv mode visible or invisible, as defined
     *
     * @param isVisible     distinguishes if goal of actions is visibility or invisibility
     */
    public void setHsvModeGuiVisibility(boolean isVisible) {
        // handle seekbars
        LinearLayout layoutSeekbars = (LinearLayout) findViewById(R.id.seekbar_placeholder);
        setLinearLayoutVisibility(isVisible, layoutSeekbars);

        //// handle other objects
        // ... (not needed for the moment)
    }


    /**
     * delivers the nutritional value from a food with the specified name
     * the result is depending on the name of the object and is hard-coded for the demonstration
     * purposes - a crawler would be a better solution
     *
     * @param name      specifies name of the food
     */
    public NutritionalValue getNutritionalValue(String name) {
        // variable declaration
        NutritionalValue result = null;
        float Calorie;
        float Protien;
        float Carbohydrate;
        float Fat;
        float MeanWeight;
        String healthHazardInfo;

        if (name != null) {
            if (!name.isEmpty()) {
                if (name.contains("Banana") || name.contains("banana")) {
                    // get nutritional values for a banana
                    Calorie = 89;
                    Protien = (float) 1.1;
                    Carbohydrate = (float) 22.8;
                    Fat = (float) 0.3;
                    MeanWeight = 118;
                    healthHazardInfo = " Banana can lead to Weight Gain,Migraine,Respiratory Problems,Constipation,Risk Of Type 2 Diabetes\nBanana contain calcium which make bones strong ";
                    result = new NutritionalValue(Calorie, Protien,
                            Carbohydrate, Fat, MeanWeight);

                } else if (name.contains("Lemon") || name.contains("lemon")) {
                    // get nutritional values for a lemon
                    Calorie = 29;
                    Protien = (float) 1.1;
                    Carbohydrate = (float) 9.3;
                    Fat = (float)0.3;
                    MeanWeight = (float) 58;
                    healthHazardInfo = " Lemon can lead to Tooth decay, Ulcers or gastroesphageal reflux disorder \n Lemon is rich in Vitamin C which boost to your immune system";
                    result = new NutritionalValue(Calorie, Protien,
                            Carbohydrate, Fat, MeanWeight);

                } else if (name.contains("Apple") || name.contains("apple")) {
                    // get nutritional values for a apple
                    Calorie = 52;
                    Protien = (float) 0.3;
                    Carbohydrate = (float) 13.8;
                    Fat = (float) 0.2;
                    MeanWeight = 182;
                    healthHazardInfo = "The apple seeds contain cyanide and which can cause death. \nApples are used to control diarrhea, constipation, cancer, diabetes, dysentery, fever, heart problems\n";
                    result = new NutritionalValue(Calorie, Protien,
                            Carbohydrate, Fat, MeanWeight);

                } else if (name.contains("Tomato") || name.contains("Tomato")) {
                    // get nutritional values for a mandarin
                    Calorie = 53;
                    Protien = (float) 0.8;
                    Carbohydrate = (float) 13.3;
                    Fat = (float) 0.3;
                    MeanWeight = 76;
                    healthHazardInfo = "Tomato may cause Acid Reflux/Heartburn,Kidney Problems,Diarrhea or Body Aches\nTomato is safe for pregnant and breast-feeding women in food amounts";
                    result = new NutritionalValue(Calorie, Protien,
                            Carbohydrate, Fat, MeanWeight);

                } else if (name.contains("Orange") || name.contains("orange")) {
                    // get nutritional values for a mandarin
                    Calorie = 49;
                    Protien = (float) 0.9;
                    Carbohydrate = (float) 12.5;
                    Fat = (float) 0.2;
                    MeanWeight = 140;
                    healthHazardInfo = "Taking large amounts of sweet orange peel can cause colic, convulsions, or death\n Orange can be used against Cancer,Heart health and Diabetes";
                    result = new NutritionalValue(Calorie, Protien,
                            Carbohydrate, Fat, MeanWeight);

                }else {
                    // no values available, return null
                }
            }
        }
        return result;
    }
}

