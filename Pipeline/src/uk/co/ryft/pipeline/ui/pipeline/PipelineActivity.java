package uk.co.ryft.pipeline.ui.pipeline;

import java.util.ArrayList;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.model.Element;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
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

    protected PipelineSurface mPipelineSurface;
    protected PipelineSurface mPipelineSurface0;
    protected PipelineSurface mPipelineSurface1;
    protected TextView mPipelineIndicator;

    protected ArrayList<Element> mElements;
    protected Bundle mPipelineParams;

    protected int mAnimationDuration;

    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout and find references to views
        setContentView(R.layout.activity_pipeline);
        FrameLayout pipelineFrame = (FrameLayout) findViewById(R.id.simulator_frame);
        mPipelineIndicator = (TextView) findViewById(R.id.pipeline_indicator);

        // Pipeline surface needs to be constructed here with specific parameters from activity intent
        mPipelineParams = getIntent().getExtras();
        mPipelineSurface0 = new PipelineSurface(this, mPipelineParams, false);
        mPipelineSurface1 = new PipelineSurface(this, mPipelineParams, true);
        mPipelineSurface0.setPadding(2, 2, 2, 2);
        mPipelineSurface1.setPadding(2, 2, 2, 2);
        mPipelineSurface1.setAlpha(0);
        pipelineFrame.addView(mPipelineSurface0);
        pipelineFrame.addView(mPipelineSurface1);
        
        // Create a reference to the current pipeline surface
        mPipelineSurface = mPipelineSurface0;

        // Retrieve and cache the system's default "short" animation time.
        // TODO Am I actually using an animator?
        mAnimationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        // Initialise pipeline navigator
        setupPipelineNavigator();

        // Set up a gesture listener for double-taps
        final GestureDetector gestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mPipelineSurface.toggleEditMode();
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                // Consume all events between a double-tap to prevent "jumping"
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!mPipelineSurface.isEditMode()) {
                    mIsScrolling = true;
                    mScrollStartX = e1.getX();
                    return true;
                } else
                    return false;
            }
        });

        // Set up a scale event listener for pinch-to-zoom gestures
        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(this, new SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mPipelineSurface.getRenderer().setScaleFactor(detector.getScaleFactor());
                return true;
            }
        });

        // Combine previous listeners and detect left- and right-swipes
        mPipelineSurface.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Detect if a scroll event has finished for pipeline transitions
                if (mIsScrolling && event.getAction() == MotionEvent.ACTION_UP) {
                    mIsScrolling = false;

                    if (mScrollStartX - event.getX() >= mPipelineSurface.getWidth() / 3) {
                        // Scrolled left
                        mPipelineSurface.getRenderer().next();
                        updatePipelineNavigator(true);

                    } else if (event.getX() - mScrollStartX >= mPipelineSurface.getWidth() / 3) {
                        // Scrolled right
                        mPipelineSurface.getRenderer().previous();
                        updatePipelineNavigator(false);
                    }
                }

                // Consume all double-tap and swipe events as next highest priority
                if (!gestureDetector.onTouchEvent(event) && mPipelineSurface.isEditMode()) {

                    // XXX There is a bug in ScaleGestureDetector where it always returns true
                    // See https://code.google.com/p/android/issues/detail?id=42591
                    scaleDetector.onTouchEvent(event);
                    onSceneMove(event);
                }

                return true;
            }
        });

    }
    
    private boolean multisampled = false;

    private void crossfade() {

        // // Set the content view to 0% opacity but visible, so that it is visible
        // // (but fully transparent) during the animation.
        // mV.setAlpha(0f);
        // mV.setVisibility(View.VISIBLE);

        final View viewSrc = (multisampled) ? mPipelineSurface1 : mPipelineSurface0;
        final View viewDst = (multisampled) ? mPipelineSurface0 : mPipelineSurface1;

        new Runnable() {
            @Override
            public void run() {

                for (float alpha = 1; alpha >= 0; alpha -= 0.01) {
                    viewSrc.setAlpha(alpha);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                viewDst.setAlpha(0);
                viewDst.setVisibility(View.VISIBLE);
                viewSrc.setVisibility(View.GONE);
                for (float alpha = 0; alpha <= 1; alpha += 0.01) {
                    viewDst.setAlpha(alpha);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        }.run();

        // // Animate the content view to 100% opacity, and clear any animation
        // // listener set on the view.
        // mV.animate()
        // .alpha(1f)
        // .setDuration(5000)
        // .setListener(null);
        //
        // // Animate the loading view to 0% opacity. After the animation ends,
        // // set its visibility to GONE as an optimization step (it won't
        // // participate in layout passes, etc.)
        // mPipelineSurface.animate()
        // .alpha(0f)
        // .setDuration(5000)
        // .setListener(new AnimatorListenerAdapter() {
        // @Override
        // public void onAnimationEnd(Animator animation) {
        // mPipelineSurface.setVisibility(View.GONE);
        // }
        // });
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

    public void updatePipelineNavigator(boolean forward) {

        mPipelineIndicator.setText(mPipelineSurface.getRenderer().getStateDescription());

        // Scroll to the location of the current state's corresponding block
        int currentState = mPipelineSurface.getRenderer().getCurrentState();
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

    protected boolean mIsScrolling = false;
    protected float mScrollStartX = 0;

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
                if (y > mPipelineSurface.getHeight() / 2)
                    dx = dx * -1;

                // reverse direction of rotation to left of the mid-line
                if (x < mPipelineSurface.getWidth() / 2)
                    dy = dy * -1;

                mPipelineSurface.getRenderer().setRotation(
                        mPipelineSurface.getRenderer().getRotation() - (dx + dy) * TOUCH_SCALE_FACTOR); // = 180.0f / 320
                mPipelineSurface.requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mPipelineSurface.isEditMode())
            mPipelineSurface.toggleEditMode();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_pipeline_help:
                crossfade();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        PipelineRenderer renderer = mPipelineSurface.getRenderer();
        // TODO check out GLSurfaceView.setPreserveEGLContextOnPause()

        savedInstanceState.putBoolean("edit_mode", mPipelineSurface.isEditMode());
        savedInstanceState.putFloat("angle", renderer.getRotation());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        PipelineRenderer renderer = mPipelineSurface.getRenderer();

        mPipelineSurface.setEditMode(savedInstanceState.getBoolean("edit_mode", false));
        renderer.setRotation(savedInstanceState.getFloat("angle", 0));
    }

    @Override
    public void onPause() {
        super.onPause();
        mPipelineSurface.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPipelineSurface.onResume();
    }

}
