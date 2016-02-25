package com.lecodesoft.safepal.app.firsttime;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lecodesoft.safepal.R;

public class RegisterActivity extends AppCompatActivity {
   //declaration of all variables
    Button registerOk;
    AlertDialog.Builder builder;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //assignments of variables
        registerOk = (Button)findViewById(R.id.register_ok);
        builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        phoneNumber="00000000000";

        //open dialog on click of ok
        registerOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                builder.setTitle("SafePal Number Verification");
                builder.setMessage("we will be verifying this phone number "+ phoneNumber+"\n Is this Ok, or you would like to edit the number");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(RegisterActivity.this, VerifyNumberActivity.class);
                        startActivity(i);


                    }
                });
                builder.setNegativeButton("Edit", null);
                builder.show();


            }
        });
    }
}
