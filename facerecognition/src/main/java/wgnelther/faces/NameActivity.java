package wgnelther.faces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.wgnelther.faces.R;

import wgnelther.faces.learn.SessionManager;
import wgnelther.faces.learn.Training;

public class NameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        final SessionManager sessionManager = new SessionManager(NameActivity.this);

        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NameActivity.this, MainActivity.class));
                finish();
            }
        });

        Button nextButton = (Button) findViewById(R.id.nextButton);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendName();
            }
        });

        final EditText editText = (EditText) findViewById(R.id.name);
        String sharedName = sessionManager.getName();
        if (!sharedName.equals("null")) {
            editText.setText(sharedName);
        }
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    sendName();
                    sessionManager.putName(editText.getText().toString().trim());
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void sendName() {
        final EditText name = (EditText) findViewById(R.id.name);
        if (!name.getText().toString().equals("")) {
            Intent intent = new Intent(NameActivity.this, Training.class);
            intent.putExtra("name", name.getText().toString().trim());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(NameActivity.this, "Please enter your name", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(NameActivity.this, MainActivity.class));
        super.onBackPressed(); // optional depending on your needs
    }
}
