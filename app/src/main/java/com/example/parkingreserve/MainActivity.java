package com.example.parkingreserve;



import android.content.Intent;
import android.os.Bundle;


import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    public static final String AM_PHONE_CODE = "+374";
    public static final String PHONE_INTENT_KEY="phone number";

    EditText numberInput;
    Button nextBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        numberInput=findViewById(R.id.sign_in_number_input);
        nextBtn=findViewById(R.id.sign_in_next_btn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String number=numberInput.getText().toString();
                if (number.isEmpty() || number.length()<8){
                    numberInput.setError("Valid number is required");
                    numberInput.requestFocus();
                    return;
                }

                String phoneNumber=AM_PHONE_CODE.concat(number);

                Intent intent=new Intent(MainActivity.this,VerifyPhoneActivity.class);
                intent.putExtra(PHONE_INTENT_KEY,phoneNumber);
                startActivity(intent);

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser()!=null){

            Intent intent=new Intent(MainActivity.this,MapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}