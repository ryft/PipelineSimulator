package uk.co.ryft.pipeline.ui.setup;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.model.Camera;
import uk.co.ryft.pipeline.ui.components.EditPointHandler;
import uk.co.ryft.pipeline.ui.components.OnPointChangedListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SetupCameraActivity extends Activity {

    protected Float3 mPointEye;
    protected Float3 mPointFocus;
    protected Float3 mPointUp;

    // Store references to view elements
    protected TextView mTextEye;
    protected TextView mTextFocus;
    protected TextView mTextUp;

    protected EditText mTextLeft;
    protected EditText mTextRight;
    protected EditText mTextBottom;
    protected EditText mTextTop;
    protected EditText mTextNear;
    protected EditText mTextFar;

    protected Button mButtonSave;
    protected Button mButtonDiscard;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_camera);
        
        // Get camera state from saved instance or bundle, if possible
        Bundle extras = getIntent().getExtras();
        Camera camera = null;
        
        if (savedInstanceState != null && savedInstanceState.containsKey("camera"))
            camera = (Camera) savedInstanceState.getSerializable("camera");

        else if (extras != null && extras.containsKey("camera"))
            camera = (Camera) extras.getSerializable("camera");
        
        // Set fields to their current values
        mPointEye = camera.getEye();
        mPointFocus = camera.getFocus();
        mPointUp = camera.getUp();

        // Find interactive components
        mTextEye = (TextView) findViewById(R.id.text_point_eye);
        mTextFocus = (TextView) findViewById(R.id.text_point_focus);
        mTextUp = (TextView) findViewById(R.id.text_point_up);

        mTextLeft = (EditText) findViewById(R.id.edit_camera_projection_left);
        mTextRight = (EditText) findViewById(R.id.edit_camera_projection_right);
        mTextBottom = (EditText) findViewById(R.id.edit_camera_projection_bottom);
        mTextTop = (EditText) findViewById(R.id.edit_camera_projection_top);
        mTextNear = (EditText) findViewById(R.id.edit_camera_projection_near);
        mTextFar = (EditText) findViewById(R.id.edit_camera_projection_far);

        mButtonSave = (Button) findViewById(R.id.button_row_positive);
        mButtonDiscard = (Button) findViewById(R.id.button_row_negative);
        mButtonDiscard.setText(R.string.action_button_cancel);

        // Set up listeners
        mTextEye.setGravity(Gravity.RIGHT);
        mTextEye.setOnClickListener(new EditPointHandler(this, mPointEye, new OnPointChangedListener() {

            @Override
            public void notifyPointChanged(Float3 point) {
                mPointEye = point;
                mTextEye.setText(mPointEye.toString());
            }

        }));

        mTextFocus.setGravity(Gravity.RIGHT);
        mTextFocus.setOnClickListener(new EditPointHandler(this, mPointFocus, new OnPointChangedListener() {

            @Override
            public void notifyPointChanged(Float3 point) {
                mPointFocus = point;
                mTextFocus.setText(mPointFocus.toString());
            }

        }));

        mTextUp.setGravity(Gravity.RIGHT);
        mTextUp.setOnClickListener(new EditPointHandler(this, mPointUp, new OnPointChangedListener() {

            @Override
            public void notifyPointChanged(Float3 point) {
                mPointUp = point;
                mTextUp.setText(mPointUp.toString());
            }

        }));

        mButtonSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveAndQuit();
            }

        });
        mButtonDiscard.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                discardAndQuit();
            }
        });

        // Update view components
        mTextEye.setText(mPointEye.toString());
        mTextFocus.setText(mPointFocus.toString());
        mTextUp.setText(mPointUp.toString());
        
        mTextLeft.setText(Float.toString(camera.getLeft()));
        mTextRight.setText(Float.toString(camera.getRight()));
        mTextBottom.setText(Float.toString(camera.getBottom()));
        mTextTop.setText(Float.toString(camera.getTop()));
        mTextNear.setText(Float.toString(camera.getNear()));
        mTextFar.setText(Float.toString(camera.getFar()));
    }

    @Override
    public void onBackPressed() {
        saveAndQuit();
    }

    protected void saveAndQuit() {
        Intent result = new Intent();
        
        // Build camera object from view components
        float left = Float.valueOf(mTextLeft.getText().toString());
        float right = Float.valueOf(mTextRight.getText().toString());
        float bottom = Float.valueOf(mTextBottom.getText().toString());
        float top = Float.valueOf(mTextTop.getText().toString());
        float near = Float.valueOf(mTextNear.getText().toString());
        float far = Float.valueOf(mTextFar.getText().toString());
        
        result.putExtra("camera", new Camera(mPointEye, mPointFocus, mPointUp, left, right, bottom, top, near, far));
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    protected void discardAndQuit() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

}
