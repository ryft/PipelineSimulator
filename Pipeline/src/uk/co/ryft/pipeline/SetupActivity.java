package uk.co.ryft.pipeline;

import uk.co.ryft.pipeline.ui.SceneActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SetupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        
        // TODO return from saved instance state or extras bundle
        
        Button sceneButton = (Button) findViewById(R.id.button_edit_scene);
        sceneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SetupActivity.this, SceneActivity.class));
            }
        });
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setup, menu);
        return true;
    }

}
