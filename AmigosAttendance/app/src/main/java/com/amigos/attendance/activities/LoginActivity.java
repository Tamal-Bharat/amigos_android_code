package com.amigos.attendance.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amigos.attendance.R;
import com.amigos.attendance.utilities.DialogUtility;

public class LoginActivity extends AppCompatActivity {

    TextView userPin_textView;
    TextView forgetPin_textView;
    TextView register_textView;
    ImageView submit_pin_imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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

        initializeComponents();

        forgetPin_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder dialogBuilder = new DialogUtility().getEnterMobileDialog(LoginActivity.this);
                AlertDialog dialog = dialogBuilder.show();

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("DDDD-->" + "Cancel Button Clicked");
                        dialog.cancel();
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        EditText enterMobile_editText = dialog.findViewById(R.id.enterMobile_editText);
                        System.out.println("DDDD--> " + enterMobile_editText.getText());
                        String enteredMobileNo = enterMobile_editText.getText().toString().trim();

                        if (enteredMobileNo.isEmpty()) {
                            enterMobile_editText.setError("Enter valid mobile no");
                            return; // Dialog stays open
                        }
                        else if(enteredMobileNo.length()<10){
                            enterMobile_editText.setError("Enter valid mobile no");
                            return;
                        }
                        else{

                        }


                    }
                });

            }
        });

        submit_pin_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String enteredPIN = userPin_textView.getText().toString().trim();

                if(enteredPIN.isEmpty()){
                    userPin_textView.setError("Enter valid PIN");
                }
                else{
                    if(!enteredPIN.equalsIgnoreCase("1234")){
                        userPin_textView.setError("You have entered wrong PIN");
                    }
                    else{
                        Intent homeActivityIntent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(homeActivityIntent);
                        overridePendingTransition(R.anim.home_page_fade_in_anim, R.anim.home_page_anim_fade_out);
                    }
                }
            }
        });

    }

    private void initializeComponents(){
        userPin_textView = findViewById(R.id.userPin_textView);
        forgetPin_textView = findViewById(R.id.forgetPin_textView);
        register_textView = findViewById(R.id.forgetPin_textView);
        submit_pin_imageView = findViewById(R.id.submit_pin_imageView);
    }
}