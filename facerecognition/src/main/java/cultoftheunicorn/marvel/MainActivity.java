package cultoftheunicorn.marvel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opencv.cultoftheunicorn.marvel.R;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView loc;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loc = (TextView) findViewById(R.id.loc);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                onLocationChanged(location);
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Please enable your GPS")
                        .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .show();
            }
        }

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
                        Intent intent = new Intent(MainActivity.this, Recognize.class);
                        intent.putExtra("loc", loc.getText());
                        startActivity(intent);
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
                Intent intent = new Intent(MainActivity.this, NameActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Close Apps?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        loc.setText("Lat: " + latitude + ", " + "Long: " + longitude);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }
}
