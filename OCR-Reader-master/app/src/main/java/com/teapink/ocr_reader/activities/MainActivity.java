package com.teapink.ocr_reader.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.view.MenuItem;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.teapink.ocr_reader.R;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView textValue;
    private Button copyButton;
    private Button mailTextButton;
    //private Button continueButton;

    private static final int RC_OCR_CAPTURE = 9003;
    private static final String TAG = "MainActivity";
    SharedPreferences sp;
    public static final String USER_PREF = "USER_PREF" ;
    public static final String KEY_TEXT = "KEY_TEXT";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    CameraActivity camactivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        statusMessage = (TextView)findViewById(R.id.status_message);
        textValue = (TextView)findViewById(R.id.text_value);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);
        sp = getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);


//        continueButton = (Button) findViewById(R.id.continue_button);
//        continueButton.setVisibility(View.GONE);
//        //continue click listener
//        continueButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPreferences.Editor editor = sp.edit();
//                String detectedText = textValue.getText().toString();
//                editor.putString(KEY_TEXT, detectedText);
//
//                editor.commit();
//                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
//            }
//        });




        Button readTextButton = (Button) findViewById(R.id.read_text_button);
        readTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // launch Ocr capture activity.
                Intent intent = new Intent(getApplicationContext(), OcrCaptureActivity.class);
                intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                intent.putExtra(OcrCaptureActivity.UseFlash, useFlash.isChecked());

                startActivityForResult(intent, RC_OCR_CAPTURE);
            }
        });

        copyButton = (Button) findViewById(R.id.copy_text_button);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard =
                            (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(textValue.getText().toString());
                } else {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", textValue.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), R.string.clipboard_copy_successful_message, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), clip.toString(), Toast.LENGTH_SHORT).show();
                    //go to the other activity
//                    Intent intent = new Intent(MainActivity.this, com.teapink.ocr_reader.view.MainActivity.class);
//                    startActivity(intent);

                    //Intent intent = new Intent(getApplicationContext(), com.teapink.ocr_reader.view.MainActivity.class);
                    //intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                   // intent.putExtra(OcrCaptureActivity.UseFlash, useFlash.isChecked());

                    //startActivity(intent);

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
                    }else{
                        Toast.makeText(getApplicationContext(), "not opening other app", Toast.LENGTH_SHORT).show();
                        t= new Intent(Intent.ACTION_VIEW);
                        t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(t);
//                            Toast.makeText(getApplicationContext(),
//                                    R.string.no_email_client_error, Toast.LENGTH_SHORT).show();
                    }

                }
                //Toast.makeText(getApplicationContext(), textValue.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

       mailTextButton = (Button) findViewById(R.id.mail_text_button);
        mailTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // saving detected text to shared preferences
                SharedPreferences.Editor editor = sp.edit();
                String detectedText = textValue.getText().toString();
                editor.putString(KEY_TEXT, detectedText);

                editor.commit();
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();

                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                popup.setOnMenuItemClickListener(MainActivity.this);
                popup.inflate(R.menu.popup_menu);
                popup.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textValue.getText().toString().isEmpty()) {
            copyButton.setVisibility(View.GONE);
            mailTextButton.setVisibility(View.GONE);
        } else {
            //copyButton.setVisibility(View.VISIBLE);
            mailTextButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    statusMessage.setText(R.string.ocr_success);
                    textValue.setText(text);
                    Log.d(TAG, "Text read: " + text);


                        //copyButton.setVisibility(View.GONE);
                        //mailTextButton.setVisibility(View.GONE);
                    //continueButton.setVisibility(View.VISIBLE);



                } else {
                    statusMessage.setText(R.string.ocr_failure);
                    Log.d(TAG, "No Text captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.ocr_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Toast.makeText(this, "Selected Item: " +item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.one:
                // do your code
                Toast.makeText(MainActivity.this,"You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.two:
                // do your code
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // If you do not have permission, request it
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                } else {
                    // Launch the camera if the permission exists
                    //camactivity.launchCamera();
                }
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
                return true;

            default:
                return false;
        }
    }
//    public void showPopup(View v) {
//        PopupMenu popup = new PopupMenu(this, v);
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.popup_menu, popup.getMenu());
//        popup.show();
//    }
//

}
