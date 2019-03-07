package pl.noritoshi_scarlett.pathflytha.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pl.noritoshi_scarlett.pathflytha.Pathflytha;
import pl.noritoshi_scarlett.pathflytha.R;

public class ScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);

        // SHARED PREFERENCES -> Pobranie osobistych ustawień aplikacji
        SharedPreferences settings = getSharedPreferences(Pathflytha.PATHFLYTHA_USER_SETTINGS, 0);

        // Opoznienie startu
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
            // ... Hide splash image and show the real UI
        }, 2000);
    }
}
