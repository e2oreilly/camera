package com.example.camerajuice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //FIREBASE DECLARATIONS
    FirebaseVisionBarcodeDetector detector;
    String rawValue = null;
    TextView text;

    //CAMERA DECLARATIONS
    static final int REQUEST_TAKE_PHOTO = 2;
    ImageView iv;
    String currentPhotoPath;
    File photoFile;
    boolean takingPic = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.iv);

        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_PDF417)
                        .build();

        detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);
        text = (TextView) findViewById(R.id.resulttext);

        try {
            Uri uri = Uri.parse("android.resource://com.example.camerajuice/drawable/numbers");
            InputStream stream = getContentResolver().openInputStream(uri);
            detectorResultFromURI(detector, uri);

        } catch (IOException e) {
            e.printStackTrace();
        }

        dispatchTakePictureIntent();
    }
    public String detectorResultFromURI(FirebaseVisionBarcodeDetector detector, Uri uri)
    {

        try {
            FirebaseVisionImage firebaseImage = FirebaseVisionImage.fromFilePath(this, uri);
            Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(firebaseImage)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                            Log.e("ERIC", "Firebase Listener Returned");

                            int barcodeCount = 0;
                            for (FirebaseVisionBarcode barcode : barcodes) {
                                //Rect bounds = barcode.getBoundingBox();
                                //Point[] corners = barcode.getCornerPoints();
                                barcodeCount++;
                                rawValue = barcode.getRawValue();
                                Log.e("BARCODE #"+barcodeCount+" VALUE", rawValue);
                            }
                            if (barcodeCount == 0)
                            {
                                text.setTextColor(Color.parseColor("#FF0000"));
                                    text.setText("FIREBASE  FAILED");
                            }
                            else
                                {
                                    text.setTextColor(Color.parseColor("#00FF00"));
                                    text.setText("BARCODE VALUE :  "+rawValue);
                                }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("ERIC", "FAIL");
                            Log.e("ERIC", e.toString());// Task failed with an exception
                        }
                    });
            return rawValue;

        }
        catch (IOException e)
        {
            Log.e("ERIC", "fromFilePath   FAIL");
            return null;
        }

    }
/*
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e("GOOGLE", "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }
    */

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
                Log.e("ERIC", "Re-made File");
            } catch (IOException ex) {Log.e("Eric", "Failed at line 193"); }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider3",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
        takingPic = false;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.e("FilePath", currentPhotoPath);
        return image;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {
            //Uri imgUri = Uri.parse(currentPhotoPath);
            Uri imgUri = FileProvider.getUriForFile(this,"com.example.android.fileprovider3",
                    photoFile);
            iv.setImageURI(imgUri);
            text.setText("FIREBASE  PROCESSING");
            text.setTextColor(Color.parseColor("#0000FF"));

            detectorResultFromURI(detector, imgUri);

            iv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!takingPic)
                        takingPic = true;
                        {dispatchTakePictureIntent();}

                }
            });
        }
        }
}
