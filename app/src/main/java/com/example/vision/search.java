package com.example.vision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.androidkt.googlevisionapi.R;
import com.google.api.services.vision.v1.model.Image;

public class search extends AppCompatActivity {
    private TextView labelInfo;
    private ImageView imageView;
//    private ImageButton choose;
//    private final int CODE_IMG_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        labelInfo = (TextView)findViewById(R.id.labelInfo);
        imageView = (ImageView)findViewById(R.id.imageView);
//        choose = findViewById(R.id.selectPhotoBtn);

        byte[] byteArray = getIntent().getByteArrayExtra("imageBytes");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        imageView.setImageBitmap(bmp);

        String labels = getIntent().getStringExtra("labels");
        labelInfo.setText(labels);

        Image base64EncodedImage = new Image();
        base64EncodedImage.encodeContent(byteArray);

//        choose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivityForResult(new Intent()
//                        .setAction(Intent.ACTION_GET_CONTENT)
//                        .setType("image/*"),CODE_IMG_GALLERY);
//            }
//        });
    }
}