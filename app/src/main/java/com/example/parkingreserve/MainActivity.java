package com.example.parkingreserve;



import android.content.Intent;
import android.os.Bundle;


import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;



import java.util.ArrayList;
import java.util.Arrays;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;



public class MainActivity extends AppCompatActivity {




    EditText numberInput;
    Button nextBtn;



    SpinnerDialog spinnerModel,spinnerColor;
    TextView selectCarModel;
    TextView selectCarColor;

    TextView modelHide,colorHide,numberHide;
     EditText plateNumber;


     String sModel,sColor,sPlate,sNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            initViews();
        initModelPicker();
        initColorPicker();



        selectCarColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                      spinnerColor.showSpinerDialog();
            }
        });



        selectCarModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerModel.showSpinerDialog();
            }
        });



        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                sNum=numberInput.getText().toString();
                sPlate=plateNumber.getText().toString();

                if (isChecked()) {


                   sNum = Utils.AM_PHONE_CODE.concat(sNum);

                    Intent intent = new Intent(MainActivity.this, VerifyPhoneActivity.class);
                    intent.putExtra(Utils.PHONE_INTENT_KEY, sNum);
                     intent.putExtra(Utils.MODEL_INTENT_KEY,sModel);
                     intent.putExtra(Utils.COLOR_INTENT_KEY,sColor);
                     intent.putExtra(Utils.PLATE_INTENT_KEY,sPlate);
                    startActivity(intent);
                }
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

    public void initModelPicker(){


        ArrayList<String> mCarModels=new ArrayList<String>(Arrays.asList(Utils.carModelList));

        spinnerModel =new SpinnerDialog(MainActivity.this,mCarModels,Utils.CAR_MODEL_DIALOG_TEXT);
        spinnerModel.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {

                sModel=item;
                selectCarModel.setText(item);
                modelHide.setVisibility(View.VISIBLE);
                selectCarModel.setError(null);
            }
        });
    }


public void initColorPicker(){


final ArrayList<Integer> colors =new ArrayList<>(Utils.getCarColorList(this).keySet());

    spinnerColor =new SpinnerDialog(MainActivity.this,new ArrayList<String>(Utils.getCarColorList(this).values()),Utils.CAR_COLOR_DIALOG_TEXT);
    spinnerColor.bindOnSpinerListener(new OnSpinerItemClick() {
        @Override
        public void onClick(String item, int position) {


            sColor=item;
          selectCarColor.setText(item);
          selectCarColor.setTextColor(colors.get(position));
          colorHide.setVisibility(View.VISIBLE);

          selectCarColor.setError(null);



        }
    });


}




public void initViews(){

    selectCarModel=findViewById(R.id.car_model_select);
    selectCarColor=findViewById(R.id.car_color_select);
    nextBtn=findViewById(R.id.sign_in_next_btn);
    numberInput=findViewById(R.id.sign_in_number_input);
    plateNumber=findViewById(R.id.car_plate_input);



     numberHide=findViewById(R.id.number_hide);
     colorHide=findViewById(R.id.color_hide);
     modelHide=findViewById(R.id.model_hide);

}


public boolean isChecked(){

        int counter=0;

    if (sNum==null || sNum.length()<8){
        numberInput.setError("valid number is required");
        numberInput.requestFocus();
        counter++;
    }
   if (sModel==null){

        selectCarModel.setError("car model is required");
        selectCarModel.requestFocus();
        counter++;
    }
    if (sColor==null){

        selectCarColor.setError("car color is required");
        selectCarColor.requestFocus();
        counter++;
    }
    if (sPlate.length()<7){
       plateNumber.setError("valid number is required");
       plateNumber.requestFocus();
        counter++;
    }

if (counter>0){
    return false;
}
else return true;

}










}