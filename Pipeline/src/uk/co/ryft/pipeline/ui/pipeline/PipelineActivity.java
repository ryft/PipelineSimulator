package uk.co.ryft.pipeline.ui.pipeline;

import java.util.ArrayList;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.model.Colour;
import uk.co.ryft.pipeline.model.PipelineRenderer;
import uk.co.ryft.pipeline.model.element.Element;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
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

public class PipelineActivity extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = "SimulatorActivity";

    protected PipelineSurface mSurfaceNOAA;
    protected PipelineSurface mSurfaceMSAA;
    protected TextView mPipelineIndicator;
    protected SlidingLayer mPipelineNavigator;

    protected ArrayList<Element> mElements;
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
        mSurfaceNOAA.setPadding(2, 2, 2, 2);
        mSurfaceMSAA.setPadding(2, 2, 2, 2);
        mSurfaceMSAA.setAlpha(0);
        pipelineFrame.addView(mSurfaceNOAA);
        pipelineFrame.addView(mSurfaceMSAA);

        // Initialise pipeline navigator
        setupPipelineNavigator();

        // Set up a gesture listener for double-taps
        final GestureDetector gestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mSurfaceNOAA.getRenderer().getCurrentState() < PipelineRenderer.STEP_CLIPPING) {
                    mSurfaceNOAA.toggleEditMode();
                    mSurfaceMSAA.toggleEditMode();

                    if (mSurfaceNOAA.isEditMode())
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
                if (!mSurfaceNOAA.isEditMode()) {
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
                                    || e2.getX() - mScrollOriginX >= mSurfaceNOAA.getWidth() / 3);

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

        // Combine previous listeners and detect left- and right-swipes
        final OnTouchListener touchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Listen for scroll finish events
                if (mIsScrolling && event.getAction() == MotionEvent.ACTION_UP) {
                    mIsScrolling = false;

                    if (mScrollOriginX - event.getX() >= mSurfaceNOAA.getWidth() / 5) {
                        // Scrolled left
                        mSurfaceNOAA.getRenderer().next();
                        mSurfaceMSAA.getRenderer().next();
                        if (mSurfaceNOAA.getRenderer().getCurrentState() == PipelineRenderer.STEP_MULTISAMPLING)
                            new Thread(crossFader).start();
                        updatePipelineNavigator(true);

                    } else if (event.getX() - mScrollOriginX >= mSurfaceNOAA.getWidth() / 5) {
                        // Scrolled right
                        if (mSurfaceNOAA.getRenderer().getCurrentState() == PipelineRenderer.STEP_MULTISAMPLING)
                            new Thread(crossFader).start();
                        mSurfaceNOAA.getRenderer().previous();
                        mSurfaceMSAA.getRenderer().previous();
                        updatePipelineNavigator(false);

                    } else if (mScrollOriginY - event.getY() >= mSurfaceNOAA.getHeight() / 5) {
                        if (!mPipelineNavigator.isOpened())
                            mPipelineNavigator.openLayer(true);

                    } else if (event.getY() - mScrollOriginY >= mSurfaceNOAA.getHeight() / 5) {
                        if (mPipelineNavigator.isOpened())
                            mPipelineNavigator.closeLayer(true);
                    }

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
                if (!gestureDetector.onTouchEvent(event) && mSurfaceNOAA.isEditMode()) {

                    // XXX There is a bug in ScaleGestureDetector where it always returns true
                    // See https://code.google.com/p/android/issues/detail?id=42591
                    scaleDetector.onTouchEvent(event);
                    onSceneMove(event);
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

    private boolean multisampled = false;
    private CrossFader crossFader = new CrossFader();

    private class CrossFader implements Runnable {

        @Override
        public void run() {

            final PipelineSurface viewSrc = (multisampled) ? mSurfaceMSAA : mSurfaceNOAA;
            final PipelineSurface viewDst = (multisampled) ? mSurfaceNOAA : mSurfaceMSAA;

            Runnable showDst = new Runnable() {
                @Override
                public void run() {
                    viewDst.setAlpha(0);
                    viewDst.setVisibility(View.VISIBLE);
                }
            };
            Runnable hideSrc = new Runnable() {
                @Override
                public void run() {
                    viewSrc.setVisibility(View.GONE);
                }
            };
            Runnable raiseSrc = new Runnable() {
                @Override
                public void run() {
                    viewSrc.bringToFront();
                }
            };

            // Calculate number of steps from (duration = #steps * interval)
            // Interval is fixed length (10ms)
            int steps = mAnimationDuration / 20;

            viewSrc.post(raiseSrc);
            for (int step = 0; step <= steps; step++) {
                viewSrc.setAlpha(1.0f - ((float) step / steps));
                threadSleep(10);
            }
            viewDst.post(showDst);
            viewSrc.post(hideSrc);
            for (int step = 0; step <= steps; step++) {
                viewDst.setAlpha((float) step / steps);
                threadSleep(10);
            }

            multisampled = !multisampled;

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
            TextView[] blocks = new TextView[] { blockVertexAssembly, blockVertexShading, blockClipping, blockMultisampling,
                    blockFaceCulling, blockFragmentShading, blockDepthBufferTest, blockBlending };
            if (step < 0 || step >= blocks.length)
                return null;
            else
                return blocks[step];
        }

        LinearLayout getStepGroup(int step) {
            LinearLayout[] groups = new LinearLayout[] { groupVertexProcessing, groupVertexProcessing,
                    groupPrimitiveProcessing, groupRasterisation, groupRasterisation, groupFragmentProcessing,
                    groupFragmentProcessing, groupPixelProcessing, groupPixelProcessing };
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
                mPipelineIndicator.setTextColor(Colour.RED.toArgb());
            else if (mSurfaceNOAA.getRenderer().getCurrentState() == PipelineRenderer.STEP_FINAL && !right)
                mPipelineIndicator.setTextColor(Colour.RED.toArgb());
            else
                mPipelineIndicator.setTextColor(Colour.GREEN.toArgb());
        } else
            mPipelineIndicator.setTextColor(Colour.WHITE.toArgb());

        if (right) {
            mPipelineIndicator.setGravity(Gravity.LEFT);
            mPipelineIndicator.setText(">> Undo " + mSurfaceNOAA.getRenderer().getPrevStepDescription());
        } else {
            mPipelineIndicator.setGravity(Gravity.RIGHT);
            mPipelineIndicator.setText("Apply " + mSurfaceNOAA.getRenderer().getNextStepDescription() + " <<");
        }
    }

    protected void updatePipelineIndicator(final String text) {
        mPipelineIndicator.post(new Runnable() {

            @Override
            public void run() {
                mPipelineIndicator.setTextColor(Colour.WHITE.toArgb());
                mPipelineIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                mPipelineIndicator.setText(text);
                mIndicatorCleared = true;
            }
        });
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

    private float mPreviousX = 0;
    private float mPreviousY = 0;
    private float TOUCH_SCALE_FACTOR = 0.3f;

    public boolean onSceneMove(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > mSurfaceNOAA.getHeight() / 2)
                    dx = dx * -1;

                // reverse direction of rotation to left of the mid-line
                if (x < mSurfaceNOAA.getWidth() / 2)
                    dy = dy * -1;

                mSurfaceNOAA.getRenderer().setRotation(
                        mSurfaceNOAA.getRenderer().getRotation() - (dx + dy) * TOUCH_SCALE_FACTOR); // = 180.0f / 320
                mSurfaceMSAA.getRenderer().setRotation(
                        mSurfaceMSAA.getRenderer().getRotation() - (dx + dy) * TOUCH_SCALE_FACTOR); // = 180.0f / 320
        }

        mPreviousX = x;
        mPreviousY = y;

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mPipelineNavigator.isOpened()) {
            mPipelineNavigator.closeLayer(true);
        } else if (mSurfaceNOAA.isEditMode()) {
            mSurfaceNOAA.toggleEditMode();
            mSurfaceMSAA.toggleEditMode();
        } else
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
        // TODO check out GLSurfaceView.setPreserveEGLContextOnPause()

        savedInstanceState.putBoolean("edit_mode", mSurfaceNOAA.isEditMode());
        savedInstanceState.putFloat("angle", renderer.getRotation());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        PipelineRenderer rendererNOAA = mSurfaceNOAA.getRenderer();
        PipelineRenderer rendererMSAA = mSurfaceMSAA.getRenderer();

        mSurfaceNOAA.setEditMode(savedInstanceState.getBoolean("edit_mode", false));
        mSurfaceMSAA.setEditMode(savedInstanceState.getBoolean("edit_mode", false));
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
