package uk.co.ryft.pipeline.ui.pipeline;

import java.util.ArrayList;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.model.Element;
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
import android.widget.Toast;

public class PipelineActivity extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = "SimulatorActivity";

    protected PipelineSurface mSurfaceNOAA;
    protected PipelineSurface mSurfaceMSAA;
    protected TextView mPipelineIndicator;

    protected ArrayList<Element> mElements;
    protected Bundle mPipelineParams;

    protected int mAnimationDuration;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout and find references to views
        setContentView(R.layout.activity_pipeline);
        FrameLayout pipelineFrame = (FrameLayout) findViewById(R.id.simulator_frame);
        mPipelineIndicator = (TextView) findViewById(R.id.pipeline_indicator);

        // Pipeline surface needs to be constructed here with specific parameters from activity intent
        mPipelineParams = getIntent().getExtras();
        mSurfaceNOAA = new PipelineSurface(this, mPipelineParams, false);
        mSurfaceMSAA = new PipelineSurface(this, mPipelineParams, true);
        mSurfaceNOAA.setPadding(2, 2, 2, 2);
        mSurfaceMSAA.setPadding(2, 2, 2, 2);
        mSurfaceMSAA.setAlpha(0);
        pipelineFrame.addView(mSurfaceNOAA);
        pipelineFrame.addView(mSurfaceMSAA);

        // Retrieve and cache the system's default "short" animation time.
        // TODO Am I actually using an animator?
        mAnimationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        // Initialise pipeline navigator
        setupPipelineNavigator();

        // Set up a gesture listener for double-taps
        final GestureDetector gestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mSurfaceNOAA.toggleEditMode();
                mSurfaceMSAA.toggleEditMode();
                
                mPipelineIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                if (mSurfaceNOAA.isEditMode())
                    mPipelineIndicator.setText("Move mode");
                else
                    mPipelineIndicator.setText("Simulator mode");
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
                    if (!mIsScrolling)
                        mScrollOrigin = e1.getX();
                    mIsScrolling = true;
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

                // Detect if a scroll event has finished for pipeline transitions
                if (mIsScrolling) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        mIsScrolling = false;

                        if (mScrollOrigin - mScrollCurrentX >= mSurfaceNOAA.getWidth() / 3) {
                            // Scrolled left
                            mSurfaceNOAA.getRenderer().next();
                            mSurfaceMSAA.getRenderer().next();
                            if (mSurfaceNOAA.getRenderer().getCurrentState() == PipelineRenderer.STEP_MULTISAMPLING)
                                new Thread(crossFader).start();
                            updatePipelineNavigator(true);

                            mPipelineIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                            mPipelineIndicator.setText("Swipe up to show navigator");
                            mIndicatorCleared = true;

                        } else if (event.getX() - mScrollCurrentX >= mSurfaceNOAA.getWidth() / 3) {
                            // Scrolled right
                            if (mSurfaceNOAA.getRenderer().getCurrentState() == PipelineRenderer.STEP_MULTISAMPLING)
                                new Thread(crossFader).start();
                            mSurfaceNOAA.getRenderer().previous();
                            mSurfaceMSAA.getRenderer().previous();
                            updatePipelineNavigator(false);

                            mPipelineIndicator.setText("");
                            mIndicatorCleared = true;
                        }

                    } else { // Still scrolling

                        boolean scrollingRight = mScrollingRight;
                        // Detect direction of scroll
                        if (mScrollCurrentX > event.getX())
                            scrollingRight = false;
                        else if (event.getX() > mScrollCurrentX)
                            scrollingRight = true;
                        

                        if (scrollingRight != mScrollingRight || mIndicatorCleared) {
                            // Update the indicator if we've changed direction or it's cleared
                            updatePipelineIndicator();

                            if (scrollingRight != mScrollingRight) {
                                // Set a new scroll origin if we've changed direction
                                mScrollOrigin = event.getX();
                                mScrollingRight = scrollingRight;
                            }
                        }
                        
                        mScrollCurrentX = event.getX();
                    }
                }

                // Consume all double-tap and swipe events as next highest priority
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

            viewDst.post(showDst);
            viewSrc.post(raiseSrc);
            for (float alpha = 1; alpha >= 0; alpha -= 0.01) {
                viewSrc.setAlpha(alpha);
                threadSleep(10);
            }
            viewSrc.post(hideSrc);
            for (float alpha = 0; alpha <= 1; alpha += 0.01) {
                viewDst.setAlpha(alpha);
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

        TextView headingVertexProcessing;
        TextView headingPrimitiveProcessing;
        TextView headingRasterisation;
        TextView headingFragmentProcessing;
        TextView headingPixelProcessing;

        TextView blockVertexAssembly;
        TextView blockVertexShading;
        TextView blockClipping;
        TextView blockMultisampling;
        TextView blockFaceCulling;
        TextView blockFragmentShading;
        TextView blockDepthBufferTest;
        TextView blockBlending;

        View getStateBlock(int state) {
            switch (state) {
                case PipelineRenderer.STEP_VERTEX_ASSEMBLY:
                    return blockVertexAssembly;
                case PipelineRenderer.STEP_VERTEX_SHADING:
                    return blockVertexShading;
                case PipelineRenderer.STEP_CLIPPING:
                    return blockClipping;
                case PipelineRenderer.STEP_MULTISAMPLING:
                    return blockMultisampling;
                case PipelineRenderer.STEP_FACE_CULLING:
                    return blockFaceCulling;
                case PipelineRenderer.STEP_FRAGMENT_SHADING:
                    return blockFragmentShading;
                case PipelineRenderer.STEP_DEPTH_BUFFER:
                    return blockDepthBufferTest;
                case PipelineRenderer.STEP_BLENDING:
                    return blockBlending;
                default:
                    return null;
            }
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

        mNavigator.headingVertexProcessing = (TextView) mNavigator.groupVertexProcessing.findViewById(R.id.group_heading);
        mNavigator.headingPrimitiveProcessing = (TextView) mNavigator.groupPrimitiveProcessing.findViewById(R.id.group_heading);
        mNavigator.headingRasterisation = (TextView) mNavigator.groupRasterisation.findViewById(R.id.group_heading);
        mNavigator.headingFragmentProcessing = (TextView) mNavigator.groupFragmentProcessing.findViewById(R.id.group_heading);
        mNavigator.headingPixelProcessing = (TextView) mNavigator.groupPixelProcessing.findViewById(R.id.group_heading);

        mNavigator.headingVertexProcessing.setText(R.string.heading_group_vertex_processing);
        mNavigator.headingPrimitiveProcessing.setText(R.string.heading_group_primitive_processing);
        mNavigator.headingRasterisation.setText(R.string.heading_group_rasterisation);
        mNavigator.headingFragmentProcessing.setText(R.string.heading_group_fragment_processing);
        mNavigator.headingPixelProcessing.setText(R.string.heading_group_pixel_processing);

        mNavigator.blockVertexAssembly = (TextView) mNavigator.groupVertexProcessing.findViewById(R.id.block_1);
        mNavigator.blockVertexShading = (TextView) mNavigator.groupVertexProcessing.findViewById(R.id.block_2);
        mNavigator.blockClipping = (TextView) mNavigator.groupPrimitiveProcessing.findViewById(R.id.block);
        mNavigator.blockMultisampling = (TextView) mNavigator.groupRasterisation.findViewById(R.id.block_1);
        mNavigator.blockFaceCulling = (TextView) mNavigator.groupRasterisation.findViewById(R.id.block_2);
        mNavigator.blockFragmentShading = (TextView) mNavigator.groupFragmentProcessing.findViewById(R.id.block_1);
        mNavigator.blockDepthBufferTest = (TextView) mNavigator.groupFragmentProcessing.findViewById(R.id.block_2);
        mNavigator.blockBlending = (TextView) mNavigator.groupPixelProcessing.findViewById(R.id.block);

        Resources r = getResources();
        setBackgroundDrawable(mNavigator.groupVertexProcessing.findViewById(R.id.map_block_wrapper),
                r.getDrawable(R.drawable.navigator_box_outer_1));
        setBackgroundDrawable(mNavigator.groupPrimitiveProcessing.findViewById(R.id.map_block_wrapper),
                r.getDrawable(R.drawable.navigator_box_outer_2));
        setBackgroundDrawable(mNavigator.groupRasterisation.findViewById(R.id.map_block_wrapper),
                r.getDrawable(R.drawable.navigator_box_outer_3));
        setBackgroundDrawable(mNavigator.groupFragmentProcessing.findViewById(R.id.map_block_wrapper),
                r.getDrawable(R.drawable.navigator_box_outer_4));
        setBackgroundDrawable(mNavigator.groupPixelProcessing.findViewById(R.id.map_block_wrapper),
                r.getDrawable(R.drawable.navigator_box_outer_5));

        mNavigator.blockVertexAssembly.setText(R.string.label_vertex_assembly);
        mNavigator.blockVertexShading.setText(R.string.label_vertex_shading);
        mNavigator.blockClipping.setText(R.string.label_clipping);
        mNavigator.blockMultisampling.setText(R.string.label_multisampling);
        mNavigator.blockFaceCulling.setText(R.string.label_face_culling);
        mNavigator.blockFragmentShading.setText(R.string.label_fragment_shading);
        mNavigator.blockDepthBufferTest.setText(R.string.label_depth_buffer_test);
        mNavigator.blockBlending.setText(R.string.label_blending);

        TextView connectorTitle;
        connectorTitle = (TextView) mNavigator.groupVertexProcessing.findViewById(R.id.map_connector).findViewById(
                R.id.map_connector_title);
        connectorTitle.setText("> vertices >");
        connectorTitle = (TextView) mNavigator.groupPrimitiveProcessing.findViewById(R.id.map_connector).findViewById(
                R.id.map_connector_title);
        connectorTitle.setText("> primitives >");
        connectorTitle = (TextView) mNavigator.groupRasterisation.findViewById(R.id.map_connector).findViewById(
                R.id.map_connector_title);
        connectorTitle.setText("> fragments >");
        connectorTitle = (TextView) mNavigator.groupFragmentProcessing.findViewById(R.id.map_connector).findViewById(
                R.id.map_connector_title);
        connectorTitle.setText("> pixels >");
        mNavigator.groupPixelProcessing.removeView(mNavigator.groupPixelProcessing.findViewById(R.id.map_connector));

        LinearLayout detailsLayout;
        detailsLayout = (LinearLayout) mNavigator.groupVertexProcessing.findViewById(R.id.navigator_details);
        detailsLayout.addView(getLayoutInflater().inflate(R.layout.navigator_vertex_assembly, null));
        detailsLayout.addView(getLayoutInflater().inflate(R.layout.navigator_shading, null));

        detailsLayout = (LinearLayout) mNavigator.groupPrimitiveProcessing.findViewById(R.id.navigator_details);
        detailsLayout.addView(getLayoutInflater().inflate(R.layout.navigator_clipping, null));

        detailsLayout = (LinearLayout) mNavigator.groupRasterisation.findViewById(R.id.navigator_details);
        detailsLayout.addView(getLayoutInflater().inflate(R.layout.navigator_multisampling, null));
        detailsLayout.addView(getLayoutInflater().inflate(R.layout.navigator_face_culling, null));

        detailsLayout = (LinearLayout) mNavigator.groupFragmentProcessing.findViewById(R.id.navigator_details);
        detailsLayout.addView(getLayoutInflater().inflate(R.layout.navigator_shading, null));
        detailsLayout.addView(getLayoutInflater().inflate(R.layout.navigator_depth_buffer_test, null));

        detailsLayout = (LinearLayout) mNavigator.groupPixelProcessing.findViewById(R.id.navigator_details);
        detailsLayout.addView(getLayoutInflater().inflate(R.layout.navigator_blending, null));

        mNavigator.groupFragmentProcessing.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "debug", Toast.LENGTH_SHORT).show();
            }
        });

        updatePipelineNavigator(true);
    }

    protected void updatePipelineIndicator() {

        if (mScrollingRight) {
            mPipelineIndicator.setGravity(Gravity.RIGHT);
            mPipelineIndicator.setText("Apply " + mSurfaceNOAA.getRenderer().getNextStepDescription() + " >>");
        } else {
            mPipelineIndicator.setGravity(Gravity.LEFT);
            mPipelineIndicator.setText("<< Undo " + mSurfaceNOAA.getRenderer().getPrevStepDescription());
        }
        mIndicatorCleared = false;

    }

    public void updatePipelineNavigator(boolean forward) {

        updatePipelineIndicator();

        // Scroll to the location of the current state's corresponding block
        int currentState = mSurfaceNOAA.getRenderer().getCurrentState();
        int[] location = new int[] { 0, 0 };
        View currentBlock = mNavigator.getStateBlock(currentState);
        if (currentBlock != null) {
            currentBlock.getLocationInWindow(location);
            location[0] += currentBlock.getWidth() / 2;
        }
        // We don't need to perform a smooth scroll as the navigator should always be hidden
        mNavigator.scrollView.scrollTo(location[0], location[1]);

        // Step defines the block which is to be modified
        // If we've moving backwards, the block following the current state needs to be un-shaded
        int step = (forward) ? currentState : currentState + 1;
        View block = mNavigator.getStateBlock(step);

        if (block == null)
            return;

        Resources r = getResources();
        int resource = (forward) ? R.drawable.navigator_box_inner_dark : R.drawable.navigator_box_inner_light;
        android.graphics.drawable.Drawable background = r.getDrawable(resource);
        setBackgroundDrawable(block, background);
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

    protected boolean mIndicatorCleared = true;
    protected boolean mIsScrolling = false;
    protected boolean mScrollingRight = true;
    protected float mScrollOrigin = 0;
    protected float mScrollCurrentX = 0;

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
        if (mSurfaceNOAA.isEditMode()) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_pipeline_help:
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
