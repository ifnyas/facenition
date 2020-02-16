package cultoftheunicorn.marvel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.opencv.cultoftheunicorn.marvel.R;

public class NameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NameActivity.this, MainActivity.class));
                finish();
            }
        });

        final EditText name = (EditText) findViewById(R.id.name);
        Button nextButton = (Button) findViewById(R.id.nextButton);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!name.getText().toString().equals("")) {
                    Intent intent = new Intent(NameActivity.this, Training.class);
                    intent.putExtra("name", name.getText().toString().trim());
                    startActivity(intent);
                } else {
                    Toast.makeText(NameActivity.this, "Please enter your name", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(NameActivity.this, MainActivity.class));
        super.onBackPressed(); // optional depending on your needs
    }
}
