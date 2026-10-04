package com.amigos.attendance;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amigos.attendance.activities.HomeActivity;

import com.amigos.attendance.activities.LoginActivity;
import com.amigos.attendance.utilities.ApplicationConstants;

public class SplashScreenActivity extends AppCompatActivity {

    TextView text1;
    TextView text2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashScreenActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });

        //Calling the functions to initialize all components
        initializeComponents();
        setAnimations();

        //Load the Home Screen after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent loginActivityIntent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(loginActivityIntent);
            overridePendingTransition(R.anim.home_page_fade_in_anim, R.anim.home_page_anim_fade_out);

        }, ApplicationConstants.DELAY_LOAD_HOME_PAGE);
    }

    private void initializeComponents(){
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
    }

    private void setAnimations(){

        Animation text1_animation = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.splash_text_anim);
        text1.startAnimation(text1_animation);

        Animation text2_animation = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.splash_text_anim);
        text2.startAnimation(text2_animation);
    }
}