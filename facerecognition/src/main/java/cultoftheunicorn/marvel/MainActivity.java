package cultoftheunicorn.marvel;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import org.opencv.cultoftheunicorn.marvel.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FloatingActionButton recognizeButton = (FloatingActionButton) findViewById(R.id.recognizeButton);
        final FloatingActionButton loginButton = (FloatingActionButton) findViewById(R.id.loginButton);
        final FloatingActionButton qrButton = (FloatingActionButton) findViewById(R.id.qrButton);
        final ImageButton trainingButton = (ImageButton) findViewById(R.id.trainingButton);
        final ProgressBar loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
        final View loadingView = findViewById(R.id.loadingView);
        final Handler handler = new Handler();

        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qrButton.setVisibility(View.GONE);
                recognizeButton.setVisibility(View.GONE);
                loadingView.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.VISIBLE);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, Recognize.class));
                        finish();
                    }
                }, 1000);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (qrButton.getVisibility() == View.VISIBLE) {
                    qrButton.setVisibility(View.GONE);
                    recognizeButton.setVisibility(View.GONE);
                } else {
                    qrButton.setVisibility(View.VISIBLE);
                    recognizeButton.setVisibility(View.VISIBLE);
                }
            }
        });

        trainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NameActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        //TODO: create exit dialog
        super.onBackPressed(); // optional depending on your needs
    }
}
