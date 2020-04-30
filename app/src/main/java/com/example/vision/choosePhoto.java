package com.example.vision;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.app.androidkt.googlevisionapi.R;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class choosePhoto extends AppCompatActivity{

    private final int CODE_IMG_GALLERY = 1;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";

    private Uri croppedUri;
    private byte[] imageBytes;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CODE_IMG_GALLERY && resultCode == RESULT_OK){
            Uri imgUri = data.getData();
            if(imgUri != null){
                startCrop(imgUri);
            }
        }else if(requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            Uri imgUriResultCrop = UCrop.getOutput(data);
            if(imgUriResultCrop != null){
                try{
                    croppedUri = imgUriResultCrop;
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), croppedUri);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    imageBytes = byteArrayOutputStream.toByteArray();
                    if(imageBytes != null){
                        Intent intentSearch = new Intent(choosePhoto.this, crop.class);
                        intentSearch.putExtra("imageBytesCrop", imageBytes);
                        startActivity(intentSearch);
                    }

                }  catch (IOException e) {
                    Log.d("choosePhoto", "failed to make API request because of other IOException " + e.getMessage());
                }
            }
        }
    }



    private void startCrop(@NonNull Uri uri){
        String destinationFileName = SAMPLE_CROPPED_IMG_NAME;
        destinationFileName += ".jpg";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(),destinationFileName)));
        uCrop.withMaxResultSize(640,640);
        uCrop.withOptions(getCropOptions());
        uCrop.start(choosePhoto.this);

    }

    private UCrop.Options getCropOptions(){
        UCrop.Options options = new UCrop.Options();

        options.setCompressionQuality(100);

        // CompressType
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

        // UI
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);

        // Colors
        options.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        options.setToolbarColor(getResources().getColor(R.color.colorPrimary));

//        options.setToolbarTitle("CROP");

        return options;
    }


    /**
     * Request permission
     *
     * @param permissions
     */
    public void requestPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permissions[i]);
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        Toast.makeText(this, "Failure to open permissions will result in some features not being available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            ActivityCompat.requestPermissions(this, list.toArray(new String[permissions.length]), 0);
        }
    }
}
