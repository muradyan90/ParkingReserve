package com.example.parkingreserve;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneActivity extends AppCompatActivity {


    Button signIn;
    EditText codeInput;
    ProgressBar progressBar;
    private String verificationId;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        final String phoneNumber=getIntent().getStringExtra(Utils.PHONE_INTENT_KEY);

        sendVerificationCode(phoneNumber);


        mAuth=FirebaseAuth.getInstance();

        signIn=findViewById(R.id.sign_in_btn);
        codeInput=findViewById(R.id.code_input);
        progressBar=findViewById(R.id.progressbar);


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                  String code=codeInput.getText().toString().trim();
                  if (code.isEmpty() || code.length()<6){

                      codeInput.setError("enter code");
                      codeInput.requestFocus();
                      return;
                  }
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);

            }
        });


    }


    private void verifyCode(String code){

        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId,code);
        signInwithCredential(credential);
    }

    private void signInwithCredential(PhoneAuthCredential credential) {

               mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                   @Override
                   public void onComplete(@NonNull Task<AuthResult> task) {

                       if (task.isSuccessful()){

                           userDataToFbase();
                           Intent intent=new Intent(VerifyPhoneActivity.this,MapActivity.class);
                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                           startActivity(intent);


                       }
                       else {
                           Toast.makeText(VerifyPhoneActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();

                       }


                   }
               });
    }


    private void sendVerificationCode(String number){

        PhoneAuthProvider.getInstance().verifyPhoneNumber(number,60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD,mCallback);

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationId=s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            String code=phoneAuthCredential.getSmsCode();
            if (code!=null){
                progressBar.setVisibility(View.VISIBLE);
                codeInput.setText(code);
                verifyCode(code);
            }

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            Toast.makeText(VerifyPhoneActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    };


    public void userDataToFbase(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref=database.getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ref.child("Color").setValue(getIntent().getStringExtra(Utils.COLOR_INTENT_KEY));
        ref.child("Model").setValue(getIntent().getStringExtra(Utils.MODEL_INTENT_KEY));
        ref.child("Plate").setValue(getIntent().getStringExtra(Utils.PLATE_INTENT_KEY));
        ref.child("Phone").setValue(getIntent().getStringExtra(Utils.PHONE_INTENT_KEY));
    }
}
