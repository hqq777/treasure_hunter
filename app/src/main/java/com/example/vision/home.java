package com.example.vision;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.app.androidkt.googlevisionapi.R;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class home extends AppCompatActivity {
    private ImageButton choose;

    private byte[] imageBytes;
    private static final int REQUEST_CODE_CHOOSE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        choose = findViewById(R.id.selectPhotoBtn);
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matisse.from(home.this)
                        .choose(MimeType.allOf())//image type
                        .countable(true)//true:show number in select circle ;false:show icon in circle
                        .maxSelectable(1)//maximum number of pictures selected
                        .capture(true)//add camera
                        .captureStrategy(new CaptureStrategy(true, "com.example.jie.photopickerdemo.fileprovider"))//参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                        .theme(R.style.Matisse_Dracula)//theme style
                        .imageEngine(new GlideEngine())// choose Glide as image engine
                        .forResult(REQUEST_CODE_CHOOSE);//
            }
        });

        initPermission();
    }
    private void initPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            try {
                Uri result = Matisse.obtainResult(data).get(0);
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                imageBytes = byteArrayOutputStream.toByteArray();
                if (imageBytes != null) {
                    Intent intentSearch = new Intent(home.this, crop.class);
                    intentSearch.putExtra("imageUri", result.toString());
                    intentSearch.putExtra("imageBytes", imageBytes);
                    startActivity(intentSearch);
                }
            } catch (IOException e) {
                Log.d("choosePhoto", "failed to make API request because of other IOException " + e.getMessage());
            }
        }
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
