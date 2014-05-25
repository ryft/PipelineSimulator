package uk.co.ryft.pipeline.ui.pipeline;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseView.ConfigOptions;
import com.espian.showcaseview.SimpleShowcaseEventListener;
import com.espian.showcaseview.targets.ViewTarget;
import com.slidinglayer.SlidingLayer;
import com.slidinglayer.SlidingLayer.OnInteractListener;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.model.Colour;

public class PipelineActivity extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = "SimulatorActivity";

    protected PipelineSurface mSurfaceNOAA;
    protected PipelineSurface mSurfaceMSAA;
    protected TextView mPipelineIndicator;
    protected SlidingLayer mPipelineNavigator;
    protected Bundle mPipelineParams;

    // Animation length in milliseconds
    protected int mAnimationDuration = 2000;

    protected boolean mIndicatorCleared = true;
    protected boolean mIsScrolling = false;
    protected boolean mIsScrollingRight = false;
    protected float mScrollOriginX = 0;
    protected float mScrollOriginY = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout and find references to views
        setContentView(R.layout.activity_pipeline);
        FrameLayout pipelineFrame = (FrameLayout) findViewById(R.id.simulator_frame);
        mPipelineIndicator = (TextView) findViewById(R.id.pipeline_indicator);
        mPipelineNavigator = (SlidingLayer) findViewById(R.id.pipeline_navigator_layer);

        // Pipeline surface needs to be constructed here with specific parameters from activity intent
        mPipelineParams = getIntent().getExtras();
        mSurfaceNOAA = new PipelineSurface(this, mPipelineParams, false);
        mSurfaceMSAA = new PipelineSurface(this, mPipelineParams, true);
        mSurfaceNOAA.setPreserveEGLContextOnPause(true);
        mSurfaceMSAA.setPreserveEGLContextOnPause(true);
        mSurfaceNOAA.setPadding(2, 2, 2, 2);
        mSurfaceMSAA.setPadding(2, 2, 2, 2);
        mSurfaceMSAA.setAlpha(0);
        mSurfaceMSAA.setVisibility(View.GONE);
        pipelineFrame.addView(mSurfaceNOAA);
        pipelineFrame.addView(mSurfaceMSAA);

        // Initialise pipeline navigator
        setupPipelineNavigator();

        // Set up a gesture listener for double-taps
        final GestureDetector gestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mSurfaceNOAA.getRenderer().getCurrentState() < PipelineRenderer.STEP_CLIPPING) {
                    toggleEditMode();

                    if (isEditMode())
                        updatePipelineIndicator("Move mode");
                    else
                        updatePipelineIndicator("Simulator mode");

                } else
                    updatePipelineIndicator("Move mode unavailable after viewport mapping");

                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                // Consume all events between a double-tap to prevent "jumping"
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!isEditMode()) {
                    mIsScrolling = true;

                    // Store y-coordinate for simple vertical swipe detection
                    mScrollOriginY = e1.getY();

                    // Detect direction of scroll
                    boolean scrollingRight = mIsScrollingRight;
                    if (distanceX > 0)
                        scrollingRight = true;
                    else if (distanceX < 0)
                        scrollingRight = false;

                    updatePipelineIndicator(!scrollingRight,
                            mScrollOriginX - e2.getX() >= mSurfaceNOAA.getWidth() / 3
                                    || e2.getX() - mScrollOriginX >= mSurfaceNOAA.getWidth() / 3
                    );

                    // Update the indicator if we've changed direction or it's been cleared
                    if (scrollingRight != mIsScrollingRight || mIndicatorCleared) {

                        // Set a new scroll origin if we've changed direction
                        if (scrollingRight != mIsScrollingRight) {
                            mScrollOriginX = e2.getX();
                            mIsScrollingRight = scrollingRight;
                        }
                    }
                    return true;

                } else
                    return false;
            }
        });

        // Set up a scale event listener for pinch-to-zoom gestures
        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(this, new SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mSurfaceNOAA.getRenderer().updateScaleFactor(detector.getScaleFactor());
                mSurfaceMSAA.getRenderer().updateScaleFactor(detector.getScaleFactor());
                return true;
            }
        });

        final int surfaceWidth = mSurfaceNOAA.getWidth();
        final int surfaceHeight = mSurfaceNOAA.getHeight();
        final int currentState = mSurfaceNOAA.getRenderer().getCurrentState();

        // Combine double-tap, scale and swipe listeners
        final OnTouchListener touchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Listen for scroll finish events
                if (mIsScrolling && event.getAction() == MotionEvent.ACTION_UP) {
                    mIsScrolling = false;

                    // Scrolled left event
                    if (mScrollOriginX - event.getX() >= surfaceWidth / 5) {
                        nextStep();
                        if (currentState == PipelineRenderer.STEP_MULTISAMPLING)
                            new Thread(crossFader).start();
                        updatePipelineNavigator(true);

                        // Scrolled right event
                    } else if (event.getX() - mScrollOriginX >= surfaceWidth / 5) {
                        if (currentState == PipelineRenderer.STEP_MULTISAMPLING)
                            new Thread(crossFader).start();
                        prevStep();
                        updatePipelineNavigator(false);

                        // Scrolled up event
                    } else if (mScrollOriginY - event.getY() >= surfaceHeight / 5) {
                        if (!mPipelineNavigator.isOpened())
                            mPipelineNavigator.openLayer(true);

                        // Scrolled down event
                    } else if (event.getY() - mScrollOriginY >= surfaceHeight / 5) {
                        if (mPipelineNavigator.isOpened())
                            mPipelineNavigator.closeLayer(true);

                    }

                    // Reset the indicator text after the transition animation completes
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                threadSleep(mAnimationDuration);
                                if (!mPipelineNavigator.isOpened())
                                    updatePipelineIndicator("Swipe up to show navigator");
                            }
                        }).start();
                    }
                }

                // Consume all double-tap and swipe events as second highest priority
                if (!gestureDetector.onTouchEvent(event) && isEditMode()) {

                    // There is a bug in ScaleGestureDetector where it always returns true
                    // See https://code.google.com/p/android/issues/detail?id=42591
                    scaleDetector.onTouchEvent(event);

                    // We only need to move the scene in the NOAA surface
                    // because viewport mapping happens before multisampling
                    mSurfaceNOAA.onSceneMove(event);
                }
                return true;
            }
        };

        mSurfaceNOAA.setOnTouchListener(touchListener);
        mSurfaceMSAA.setOnTouchListener(touchListener);

        mPipelineNavigator.setOnInteractListener(new OnInteractListener() {

            @Override
            public void onOpen() {
                updatePipelineIndicator("");
            }

            @Override
            public void onClose() {
            }

            @Override
            public void onOpened() {
            }

            @Override
            public void onClosed() {
                updatePipelineIndicator("Swipe up to show navigator");
            }

        });

        updatePipelineIndicator("Swipe up to show navigator");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            findViewById(R.id.simulator_parent).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    private boolean mEditMode;

    private boolean isEditMode() {
        return mEditMode;
    }

    private void toggleEditMode() {
        setEditMode(!isEditMode());
    }

    private void setEditMode(boolean editMode) {
        mEditMode = editMode;

        if (editMode)
            mSurfaceNOAA.setBackgroundResource(R.drawable.surface_border);
        else
            mSurfaceNOAA.setBackgroundResource(0);
    }

    private boolean mMultisampled = false;

    private void nextStep() {
        mSurfaceNOAA.getRenderer().applyNextStep();
        mSurfaceMSAA.getRenderer().applyNextStep();
    }

    private void prevStep() {
        mSurfaceNOAA.getRenderer().undoPreviousStep();
        mSurfaceMSAA.getRenderer().undoPreviousStep();
    }

    private CrossFader crossFader = new CrossFader();

    private class CrossFader implements Runnable {

        @Override
        public void run() {

            final PipelineSurface viewSrc = (mMultisampled) ? mSurfaceMSAA : mSurfaceNOAA;
            final PipelineSurface viewDst = (mMultisampled) ? mSurfaceNOAA : mSurfaceMSAA;

            // Ensure source view is positioned above the destination in the z-order
            viewSrc.post(new Runnable() {
                @Override
                public void run() {
                    viewSrc.bringToFront();
                }
            });

            // Calculate number of steps from (duration = #steps * interval)
            // Interval is fixed length (10ms)
            int steps = mAnimationDuration / 20;
            for (int step = 0; step <= steps; step++) {
                viewSrc.setAlpha(1.0f - ((float) step / steps));
                viewSrc.postInvalidate();
                threadSleep(10);
            }

            // Swap the visibility of the source & destination views
            viewDst.post(new Runnable() {
                @Override
                public void run() {
                    viewDst.setVisibility(View.VISIBLE);
                }
            });
            viewSrc.post(new Runnable() {
                @Override
                public void run() {
                    viewSrc.setVisibility(View.GONE);
                }
            });

            // Fade in the destination view
            for (int step = 0; step <= steps; step++) {
                viewDst.setAlpha((float) step / steps);
                threadSleep(10);
            }

            mMultisampled = !mMultisampled;
        }
    }

    private void threadSleep(int length) {
        try {
            Thread.sleep(length);
        } catch (InterruptedException e) {
        }
    }

    static class Navigator {
        HorizontalScrollView scrollView;

        LinearLayout groupVertexProcessing;
        LinearLayout groupPrimitiveProcessing;
        LinearLayout groupRasterisation;
        LinearLayout groupFragmentProcessing;
        LinearLayout groupPixelProcessing;

        TextView blockVertexAssembly;
        TextView blockVertexShading;
        TextView blockClipping;
        TextView blockMultisampling;
        TextView blockFaceCulling;
        TextView blockFragmentShading;
        TextView blockDepthBufferTest;
        TextView blockBlending;

        TextView getStepBlock(int step) {
            TextView[] blocks = new TextView[]{blockVertexAssembly, blockVertexShading, blockClipping, blockMultisampling,
                    blockFaceCulling, blockFragmentShading, blockDepthBufferTest, blockBlending};
            if (step < 0 || step >= blocks.length)
                return null;
            else
                return blocks[step];
        }

        LinearLayout getStepGroup(int step) {
            LinearLayout[] groups = new LinearLayout[]{groupVertexProcessing, groupVertexProcessing,
                    groupPrimitiveProcessing, groupRasterisation, groupRasterisation, groupFragmentProcessing,
                    groupFragmentProcessing, groupPixelProcessing, groupPixelProcessing};
            if (step < 0 || step >= groups.length)
                return null;
            else
                return groups[step];
        }
    }

    Navigator mNavigator = new Navigator();

    private void setupPipelineNavigator() {

        mNavigator.scrollView = (HorizontalScrollView) findViewById(R.id.pipeline_navigator);

        mNavigator.groupVertexProcessing = (LinearLayout) findViewById(R.id.group_vertex_processing);
        mNavigator.groupPrimitiveProcessing = (LinearLayout) findViewById(R.id.group_primitive_processing);
        mNavigator.groupRasterisation = (LinearLayout) findViewById(R.id.group_rasterisation);
        mNavigator.groupFragmentProcessing = (LinearLayout) findViewById(R.id.group_fragment_processing);
        mNavigator.groupPixelProcessing = (LinearLayout) findViewById(R.id.group_pixel_processing);

        mNavigator.blockVertexAssembly = (TextView) findViewById(R.id.navigator_title_vertex_assembly);
        mNavigator.blockVertexShading = (TextView) findViewById(R.id.navigator_title_vertex_shading);
        mNavigator.blockClipping = (TextView) findViewById(R.id.navigator_title_clipping);
        mNavigator.blockMultisampling = (TextView) findViewById(R.id.navigator_multisampling);
        mNavigator.blockFaceCulling = (TextView) findViewById(R.id.navigator_face_culling);
        mNavigator.blockFragmentShading = (TextView) findViewById(R.id.navigator_fragment_shading);
        mNavigator.blockDepthBufferTest = (TextView) findViewById(R.id.navigator_depth_buffer_test);
        mNavigator.blockBlending = (TextView) findViewById(R.id.navigator_blending);

        setupTutorial(mNavigator.blockVertexAssembly, R.string.label_vertex_assembly, R.string.tutorial_vertex_assembly);
        setupTutorial(mNavigator.blockVertexShading, R.string.label_vertex_shading, R.string.tutorial_vertex_shading);
        setupTutorial(mNavigator.blockClipping, R.string.label_clipping, R.string.tutorial_clipping);
        setupTutorial(mNavigator.blockMultisampling, R.string.label_multisampling, R.string.tutorial_multisampling);
        setupTutorial(mNavigator.blockFaceCulling, R.string.label_face_culling, R.string.tutorial_face_culling);
        setupTutorial(mNavigator.blockFragmentShading, R.string.label_fragment_shading, R.string.tutorial_fragment_shading);
        setupTutorial(mNavigator.blockDepthBufferTest, R.string.label_depth_buffer_test, R.string.tutorial_depth_buffer_test);
        setupTutorial(mNavigator.blockBlending, R.string.label_blending, R.string.tutorial_blending);

        updatePipelineNavigator(true);
    }

    private boolean mShowcaseShown = false;

    private void setupTutorial(final View view, final int title, final int description) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mShowcaseShown) {
                    mShowcaseShown = true;
                    insertShowcaseView(view, title, description, 0, null, new SimpleShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            mShowcaseShown = false;
                        }
                    });
                }
            }
        });
    }

    protected void updatePipelineIndicator(boolean right, boolean thresholdExceeded) {

        if (thresholdExceeded) {
            if (mSurfaceNOAA.getRenderer().getCurrentState() == PipelineRenderer.STEP_INITIAL && right)
                mPipelineIndicator.setTextColor(Colour.RED().toArgb());
            else if (mSurfaceNOAA.getRenderer().getCurrentState() == PipelineRenderer.STEP_FINAL && !right)
                mPipelineIndicator.setTextColor(Colour.RED().toArgb());
            else
                mPipelineIndicator.setTextColor(Colour.GREEN().toArgb());
        } else
            mPipelineIndicator.setTextColor(Colour.WHITE().toArgb());

        if (right) {
            mPipelineIndicator.setGravity(Gravity.LEFT);
            mPipelineIndicator.setText(">> Undo " + mSurfaceNOAA.getRenderer().getPrevStepDescription());
        } else {
            mPipelineIndicator.setGravity(Gravity.RIGHT);
            mPipelineIndicator.setText("Apply " + mSurfaceNOAA.getRenderer().getNextStepDescription() + " <<");
        }
    }

    protected void updatePipelineIndicator(final String text) {

        Runnable updater = new Runnable() {
            @Override
            public void run() {
                mPipelineIndicator.setTextColor(Colour.WHITE().toArgb());
                mPipelineIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                mPipelineIndicator.setText(text);
                mIndicatorCleared = true;
            }
        };

        mPipelineIndicator.post(updater);
    }

    public void updatePipelineNavigator(boolean forward) {

        // Fetch the current state of the pipeline from the renderer
        int currentState = mSurfaceNOAA.getRenderer().getCurrentState();

        // Step defines the block which is to be modified
        // If we've moving backwards, the block following the current state needs to be un-shaded
        int step = (forward) ? currentState - 1 : currentState;
        View currentBlock = mNavigator.getStepBlock(step);

        if (currentBlock != null) {
            Resources r = getResources();
            int resource = (forward) ? R.drawable.navigator_box_inner_dark : R.drawable.navigator_box_inner_light;
            android.graphics.drawable.Drawable background = r.getDrawable(resource);
            setBackgroundDrawable(currentBlock, background);
        }

        // Scroll to the location of the current state's corresponding block
        View currentGroup = mNavigator.getStepGroup(currentState);

        // Calculate scroll location required to centre the current group
        int groupWidth = (int) (currentGroup.getWidth() - getResources().getDimension(R.dimen.navigator_block_connector_length));
        int margin = (mNavigator.scrollView.getWidth() - groupWidth) / 2;
        final int location = currentGroup.getLeft() - margin;

        // Pause before scrolling onwards so the user can see that the previous step is complete
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(mAnimationDuration / 2);
                } catch (InterruptedException e) {
                } finally {
                    mNavigator.scrollView.smoothScrollTo(location, 0);
                }
            }
        }).start();
    }

    private void setBackgroundDrawable(View view, android.graphics.drawable.Drawable background) {

        // Use deprecated BackgroundDrawable methods for older APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            setBackgroundDrawableJB(view, background);
        else
            setBackgroundDrawableOld(view, background);
    }

    @SuppressWarnings("deprecation")
    private void setBackgroundDrawableOld(View view, android.graphics.drawable.Drawable background) {
        view.setBackgroundDrawable(background);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBackgroundDrawableJB(View view, android.graphics.drawable.Drawable background) {
        view.setBackground(background);
    }

    @Override
    public void onBackPressed() {
        if (mPipelineNavigator.isOpened())
            mPipelineNavigator.closeLayer(true);
        else if (isEditMode())
            toggleEditMode();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.pipeline, menu);

        // Calling super after populating the menu is necessary here to ensure
        // that the bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    protected ShowcaseView insertShowcaseView(View target, int title, int description, float scale, ConfigOptions options,
                                              OnShowcaseEventListener listener) {
        ShowcaseView sv = ShowcaseView.insertShowcaseView(new ViewTarget(target), this, title, description, options);
        sv.setOnShowcaseEventListener(listener);
        sv.setScaleMultiplier(scale);
        return sv;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_pipeline_help:

                final OnShowcaseEventListener tutorialNavigator = new SimpleShowcaseEventListener() {

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        insertShowcaseView(mPipelineIndicator, R.string.help_navigator_title, R.string.help_navigator_desc,
                                1.2f, null, null);
                    }
                };

                // Close navigator if necessary
                if (mPipelineNavigator.isOpened())
                    mPipelineNavigator.closeLayer(true);

                insertShowcaseView(mSurfaceNOAA, R.string.help_scene_viewer_title, R.string.help_scene_viewer_desc, 0, null,
                        tutorialNavigator);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        PipelineRenderer renderer = mSurfaceNOAA.getRenderer();

        savedInstanceState.putBoolean("edit_mode", isEditMode());
        savedInstanceState.putFloat("angle", renderer.getRotation());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        PipelineRenderer rendererNOAA = mSurfaceNOAA.getRenderer();
        PipelineRenderer rendererMSAA = mSurfaceMSAA.getRenderer();

        setEditMode(savedInstanceState.getBoolean("edit_mode", false));
        rendererNOAA.setRotation(savedInstanceState.getFloat("angle", 0));
        rendererMSAA.setRotation(savedInstanceState.getFloat("angle", 0));
    }

    @Override
    public void onPause() {
        super.onPause();
        mSurfaceNOAA.onPause();
        mSurfaceMSAA.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSurfaceNOAA.onResume();
        mSurfaceMSAA.onResume();
    }

}
