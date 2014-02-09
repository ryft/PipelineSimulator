package uk.co.ryft.pipeline.ui.pipeline;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.model.Element;
import android.app.Activity;
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
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class PipelineActivity extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = "SimulatorActivity";

    protected PipelineSurface mPipelineView;
    protected TextView mPipelineIndicator;
    protected ArrayList<Element> mElements;
    protected Bundle mPipelineParams;

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get data from activity intent
        mPipelineParams = getIntent().getExtras();
        
        // Set up view objects
        mPipelineView = new PipelineSurface(this, mPipelineParams);
        
        setContentView(R.layout.activity_pipeline);
        LinearLayout layout = (LinearLayout) findViewById(R.id.pipeline_layout);
        mPipelineIndicator = (TextView) findViewById(R.id.pipeline_indicator);
        updateIndicator();
        
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
        mPipelineView.setLayoutParams(params);
        layout.addView(mPipelineView, 0);
        mPipelineView.setPadding(2, 2, 2, 2);

        mElements = (ArrayList<Element>) mPipelineParams.getSerializable("elements");
        List<Drawable> scene = new LinkedList<Drawable>();
        for (Element e : mElements) {
            Drawable d = e.getDrawable();
            scene.add(d);
        }
        
        final GestureDetector gestureDetector = new GestureDetector(mPipelineView.mContext, new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mPipelineView.toggleEditMode();
                return true;
            }
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                // Consume all events between a double-tap to prevent "jumping"
                return true;
            }
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!mPipelineView.isEditMode()) {
                    mIsScrolling = true;
                    mScrollStartX = e1.getX();
                    return true;
                } else
                    return false;
            }
        });
        
        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(mPipelineView.mContext, new SimpleOnScaleGestureListener() {
            
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mPipelineView.getRenderer().setScaleFactor(detector.getScaleFactor());
                return true;
            }
        });
        
        mPipelineView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Detect if a scroll event has finished for pipeline transitions
                if (mIsScrolling && event.getAction() == MotionEvent.ACTION_UP) {
                    mIsScrolling = false;
                    
                    if (mScrollStartX - event.getX() >= mPipelineView.getWidth() / 2)
                        // Scrolled left
                        mPipelineView.getRenderer().next();
                    
                    else if (event.getX() - mScrollStartX >= mPipelineView.getWidth() / 2)
                        // Scrolled right
                        mPipelineView.getRenderer().previous();

                    updateIndicator();
                }

                // Consume all double-tap and swipe events as next highest priority
                if (!gestureDetector.onTouchEvent(event) && mPipelineView.isEditMode()) {

                    // XXX There is a bug in ScaleGestureDetector where it always returns true
                    // See https://code.google.com/p/android/issues/detail?id=42591
                    scaleDetector.onTouchEvent(event);
                    onSceneMove(event);
                }
                
                return true;
            }
            
        });

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
                if (y > mPipelineView.getHeight() / 2)
                  dx = dx * -1;

                // reverse direction of rotation to left of the mid-line
                if (x < mPipelineView.getWidth() / 2)
                  dy = dy * -1;

                mPipelineView.getRenderer().setRotation(mPipelineView.getRenderer().getRotation() - (dx + dy) * TOUCH_SCALE_FACTOR);  // = 180.0f / 320
                mPipelineView.requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        
        return true;
    }
    
    protected void updateIndicator() {
        mPipelineIndicator.setText(mPipelineView.getRenderer().getState());
    }

    @Override
    public void onBackPressed() {
        if (mPipelineView.isEditMode())
            mPipelineView.toggleEditMode();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        // Calling super after populating the menu is necessary here to ensure
        // that the bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_change_perspective:
                mPipelineView.toggle();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        PipelineRenderer renderer = mPipelineView.getRenderer();
        // TODO check out GLSurfaceView.setPreserveEGLContextOnPause()

        savedInstanceState.putBoolean("edit_mode", mPipelineView.isEditMode());
        savedInstanceState.putFloat("angle", renderer.getRotation());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        PipelineRenderer renderer = mPipelineView.getRenderer();

        mPipelineView.setEditMode(savedInstanceState.getBoolean("edit_mode", false));
        renderer.setRotation(savedInstanceState.getFloat("angle", 0));
    }

    @Override
    public void onPause() {
        super.onPause();
        mPipelineView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPipelineView.onResume();
    }

}
