package com.teapink.ocr_reader.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.teapink.ocr_reader.R;

import java.io.File;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_STORAGE_PERMISSION = 1;

    public static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    public AppExecutor mAppExcutor;

    public ImageView mImageView;

    public Button mStartCamera;

    public String mTempPhotoPath;

    public Bitmap mResultsBitmap;

    public FloatingActionButton mClear,mSave,mShare;
    public TextView accuracyView;
    public TextView computerView;
    //public TextView accuracyViewHolder;
    public TextView computerViewHolder;
    SharedPreferences sp;
    public static final String USER_PREF_COMPUTER = "USER_PREF_COMPUTER" ;
    public static final String KEY_TEXT_COMPUTER = "KEY_TEXT_COMPUTER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_main);

        mAppExcutor = new AppExecutor();

        mImageView = findViewById(R.id.imageView);
        mClear = findViewById(R.id.clear);
        mSave = findViewById(R.id.Save);
        mShare = findViewById(R.id.Share);
        mStartCamera = findViewById(R.id.startCamera);
        //accuracyView = findViewById(R.id.accuracy);
        computerView = findViewById(R.id.computerName);
        //accuracyViewHolder = findViewById(R.id.accuracyHolder);
        computerViewHolder = findViewById(R.id.computerNameHolder);

        mImageView.setVisibility(View.GONE);
        mShare.setVisibility(View.GONE);
        mSave.setVisibility(View.GONE);
        mClear.setVisibility(View.GONE);

        //accuracyViewHolder.setVisibility(View.GONE);
       // accuracyView.setVisibility(View.GONE);
        computerView.setVisibility(View.GONE);
        computerViewHolder.setVisibility(View.GONE);
        sp = getSharedPreferences(USER_PREF_COMPUTER, Context.MODE_PRIVATE);


        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            launchCamera();
        }


        mStartCamera.setOnClickListener(v -> {
            // Check for the external storage permission
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // If you do not have permission, request it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                // Launch the camera if the permission exists
                launchCamera();
            }
        });

        mSave.setOnClickListener((View v) -> {
            mAppExcutor.diskIO().execute(() -> {
                // Delete the temporary image file
                BitmapUtils.deleteImageFile(this, mTempPhotoPath);

                // Save the image
                BitmapUtils.saveImage(this, mResultsBitmap);
                //save to database start the other app
                Intent t;
                PackageManager manager = getPackageManager();

                t= manager.getLaunchIntentForPackage("info.androidhive.sqlite");
                if (t != null) {
                    //Toast.makeText(getApplicationContext(), "opening other app", Toast.LENGTH_SHORT).show();
                    t.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK));
                    //t.addCategory(Intent.CATEGORY_LAUNCHER);
                    t.setAction(Intent.ACTION_SEND);
                    t.putExtra("EXTRA_TEXT", "hdfhdhjfgjf");
                    t.setType("text/plain");
                    startActivity(t);
                }
                {
                    //Toast.makeText(getApplicationContext(), "not opening other app", Toast.LENGTH_SHORT).show();
//                    t= new Intent(Intent.ACTION_VIEW);
//                    t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(t);
//                            Toast.makeText(getApplicationContext(),
//                                    R.string.no_email_client_error, Toast.LENGTH_SHORT).show();
                }

            });

            Toast.makeText(this,"Image Saved",Toast.LENGTH_LONG).show();

        });

        mClear.setOnClickListener(v -> {
            // Clear the image and toggle the view visibility
            mImageView.setImageResource(0);
            mStartCamera.setVisibility(View.VISIBLE);
            mSave.setVisibility(View.GONE);
            mShare.setVisibility(View.GONE);
            mClear.setVisibility(View.GONE);

            mAppExcutor.diskIO().execute(() -> {
                // Delete the temporary image file
                BitmapUtils.deleteImageFile(this, mTempPhotoPath);
            });

        });

        mShare.setOnClickListener((View v) -> {

            mAppExcutor.diskIO().execute(() ->{
                // Delete the temporary image file
                BitmapUtils.deleteImageFile(this, mTempPhotoPath);

                // Save the image
                BitmapUtils.saveImage(this, mResultsBitmap);

            });

            // Share the image
            BitmapUtils.shareImage(this, mTempPhotoPath);

        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Process the image and set it to the TextView
            processAndSetImage();

        } else {

            // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        }
    }

    /**
     * Creates a temporary image file and captures a picture to store in it.
     */
    public void launchCamera() {

        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    /**
     * Method for processing the captured image and setting it to the TextView.
     */
    private void processAndSetImage() {

        // Toggle Visibility of the views
        mStartCamera.setVisibility(View.GONE);
        mSave.setVisibility(View.VISIBLE);
        mShare.setVisibility(View.VISIBLE);
        mClear.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.VISIBLE);
        //.setVisibility(View.VISIBLE);
        //accuracyView.setVisibility(View.VISIBLE);
        computerView.setVisibility(View.VISIBLE);
        computerViewHolder.setVisibility(View.VISIBLE);

        //call model value return method
        //but for now give fixed name
        computerViewHolder.setText("DELL");
        SharedPreferences.Editor editor = sp.edit();
        String computerText = computerViewHolder.getText().toString();
        editor.putString(KEY_TEXT_COMPUTER, computerText);

        editor.commit();

        // Resample the saved image to fit the ImageView
        mResultsBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);


        // Set the new bitmap to the ImageView
        mImageView.setImageBitmap(mResultsBitmap);
    }




}
