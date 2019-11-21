package com.example.stegnography;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ImageView imagelay;
    private EditText enterlay;
    private TextView displaytext;
    private Button encode;
    private Button decode;
    private Button savelay;
    private static int GALLERY_REQUEST_CODE = 1;
    private Bitmap mainImg;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                1);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        imagelay = findViewById(R.id.imagelay);
        enterlay = findViewById(R.id.entertextlay);
        displaytext = findViewById(R.id.displaytextlay);
        encode = findViewById(R.id.encode);
        decode = findViewById(R.id.decode);
        savelay = findViewById(R.id.save);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        imagelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
            }
        });

        encode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!imagelay.getDrawable().equals(R.drawable.click) && !enterlay.getText().toString().isEmpty()){
                    encodeMessageInImage();
                }
                else{
                    Toast.makeText(MainActivity.this, "Nothing Present", Toast.LENGTH_SHORT).show();
                }
            }
        });

        decode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!imagelay.getDrawable().equals(R.drawable.click)){
                    decodeMessageFromImage();
                }
                else{
                    Toast.makeText(MainActivity.this, "Nothing Present", Toast.LENGTH_SHORT).show();
                }
            }
        });

        savelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!imagelay.getDrawable().equals(R.drawable.click) && !enterlay.getText().toString().isEmpty()){
                    saveToInternalStorage();
                }
                else{
                    Toast.makeText(MainActivity.this, "Nothing Present", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void encodeMessageInImage(){

        Bitmap bmap;
        BitmapDrawable bmapD = (BitmapDrawable)imagelay.getDrawable();
        bmap = bmapD.getBitmap();

//        Bitmap operation = Bitmap.createBitmap(bmap.getWidth(),bmap.getHeight(),bmap.getConfig());
        Bitmap operation = bmap.copy(bmap.getConfig(), true);

        String msg = enterlay.getText().toString();
        int length = msg.length();

        int p = bmap.getPixel(0, 0);

        int r = 89;
        int g = 69;
        int b = 83;
        int alpha = Color.alpha(p);
        operation.setPixel(0, 0, Color.argb(alpha, r, g, b));

        p = bmap.getPixel(0, 1);
        alpha = Color.alpha(p);
        r = length%255;
        g = length/255;
        int check = r+255*g;
        b = Color.blue(p);
        operation.setPixel(0, 1, Color.argb(alpha, r, g, b));
//        Toast.makeText(MainActivity.this, "Length is : "+Color.alpha(operation.getPixel(0, 1)), Toast.LENGTH_SHORT).show();

        int counterX = 1;
        int counterY = 0;
        int width = bmap.getWidth();
        int height = bmap.getHeight();

        String test = "";
        for(int i=0; i<length; i++) {
            if(counterY == width) {
                counterY = 0;
                counterX++;
            }

            p = bmap.getPixel(counterX, counterY);
            r = msg.charAt(i);
            g = Color.green(p);
            b = Color.blue(p);
            alpha = Color.alpha(p);
            test += Integer.toString(r);
            operation.setPixel(counterX, counterY, Color.argb(alpha, r, g, b));
            counterY++;
        }
//        displaytext.setText("Width : "+width+"\nCounterX : "+counterX+"\nCounterY : "+counterY+"\nr : "+Color.red(operation.getPixel(0, 1))+"\ng : "+Color.green(operation.getPixel(0, 1))+"\nr : "+Color.blue(operation.getPixel(0, 1))+"\nLength : "+length+"\nr : "+check);
        imagelay.setImageBitmap(operation);
        mainImg = operation.copy(operation.getConfig(), true);
        Toast.makeText(MainActivity.this, "Message Encoded", Toast.LENGTH_SHORT).show();
    }


    private void decodeMessageFromImage(){

        Bitmap bmap;
        BitmapDrawable bmapD = (BitmapDrawable)imagelay.getDrawable();
        bmap = bmapD.getBitmap();

        int p = bmap.getPixel(0, 0);
        int r = Color.red(p);
        int g = Color.green(p);
        int b = Color.blue(p);
        int alpha = 0;
        char c;
        String msg = "";

        if(r == 89 && g == 69 && b == 83){
            int length = Color.red(bmap.getPixel(0, 1))+255*Color.green(bmap.getPixel(0, 1));
            int counterX = 1;
            int counterY = 0;
            int width = bmap.getWidth();

            for(int i=0; i<length; i++) {
                if(counterY == width) {
                    counterY = 0;
                    counterX++;
                }

                p = bmap.getPixel(counterX, counterY);
                r = Color.red(p);
                c = (char)r;
                msg += c;
                counterY++;
            }
            displaytext.setText(msg);
        }
        else{
            Toast.makeText(MainActivity.this, "No Message present.", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveToInternalStorage(){

        String root = Environment.getExternalStorageDirectory().toString();
        root = root + "/DCIM/Camera";
        File myDir = new File(root);
        myDir.mkdirs();
        Date currentTime = Calendar.getInstance().getTime();
        String input = currentTime.toString();
        input = input.replace(" ", "");
        String fname = "Image-" + input+ ".jpg";
        displaytext.setText("Image saved to : "+root+"/"+fname);
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            mainImg.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
//            Toast.makeText(MainActivity.this, "Try", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            displaytext.setText(e.toString());
//            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
        return "output";
    }

    public void checkPermission(String permission, int requestCode)
    {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
                permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            MainActivity.this,
                            new String[] { permission },
                            requestCode);
        }
    }


// This function is called when user accept or decline the permission.
// Request Code is used to check which permission called this function.
// This request code is provided when user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void pickFromGallery(){
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    //data.getData returns the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    imagelay.setImageURI(selectedImage);
                    break;
            }
        }
    }
}
