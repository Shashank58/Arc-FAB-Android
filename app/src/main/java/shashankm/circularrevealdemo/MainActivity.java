package shashankm.circularrevealdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (FrameLayout) findViewById(R.id.container);

        setLollipopAnimator();
    }

    private void setLollipopAnimator() {
        SceneAnimator.newInstance(this, container, R.layout.scene1, R.layout.scene2, R.transition.arc1);
    }
}
