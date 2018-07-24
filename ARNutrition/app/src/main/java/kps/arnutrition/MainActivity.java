/**
 Name of the module : MainActivity.java

 Date on which the module was created : 20/04/2018

 Author's name : Phoolchandra

 Modification history : by Savinay  21/04/2018
 : Savsani kevin 22/04/2018

 Synopsis of the module : Main File which is executed when the app is started

 Different functions supported along with their input/Output parameters.

 Globel variables accessed/modified by the module.

 classes : MainActivity{}

 functions : onCreate(Bundle savedInstanceState )
 CameraMode(View view)



 */
package kps.arnutrition;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;



public class MainActivity extends AppCompatActivity {

    // intent variable used to call new activity
    Intent callonGetImageListener;// callonGetImageListener used to call onGetImageListener

    // onCreate runs when activity is created
     @Override
    protected void onCreate(Bundle savedInstanceState) {
         /**
          * It loads the saved instances to app and link listview to backend
          */
         super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Function to call onGetImageListener activity
    // It is executed when user click the button CamerMode
    // view is an argument generated when a button is pressed

    public void onGetImageListener(View view){

        // variable callCameraModeActivity get the result from Intent Class
         callonGetImageListener = new Intent(this, onGetImageListener.class);

        //starting new activity
         startActivity(callonGetImageListener);
    }
}
