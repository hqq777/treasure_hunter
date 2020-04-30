package com.example.vision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.app.androidkt.googlevisionapi.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.WebDetection;
import com.google.api.services.vision.v1.model.WebEntity;
import com.google.api.services.vision.v1.model.WebLabel;

import java.util.ArrayList;
import java.util.List;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class recognition extends AppCompatActivity {

    private static final String CLOUD_VISION_API_KEY = "AIzaSyA2pD5O95eC5XSpD1IOpiA5gEu65agLm7U";
    private ProgressBar imageUploadProgress;
    private ImageView imageView;

    private Feature feature;
    private Feature featureL;
    private String saveMessage = "";
    private Bitmap testBtm;

    class Result {
        private byte[] imageBytes;
        private String saveMessage;
        public Result(){ }
        public void setParam(byte[] b, String s){
            imageBytes = b;
            saveMessage = s;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognition);

        imageUploadProgress = (ProgressBar) findViewById(R.id.imageProgress);
        imageView = (ImageView) findViewById(R.id.imageView);

        if (savedInstanceState != null){
            saveMessage = savedInstanceState.getString("labels");
            imageView.setImageBitmap(testBtm);
        }

        byte[] byteArray = getIntent().getByteArrayExtra("imageBytes");
        testBtm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        feature = new Feature();
        feature.setType("WEB_DETECTION");//"LOGO_DETECTION"
        feature.setMaxResults(5);


        featureL = new Feature();
        featureL.setType("LABEL_DETECTION");//"LOGO_DETECTION"

        featureL.setMaxResults(5);

        imageView.setImageBitmap(testBtm);
        awesomeVision(testBtm, feature, featureL);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("save_img", "onSaveInstanceState(Bundle)");
        super.onSaveInstanceState(outState);
        outState.putString("labels",saveMessage);
    }

    private void awesomeVision(final Bitmap bitmap, final Feature feature, final Feature featureL) {
        imageUploadProgress.setVisibility(View.VISIBLE);

        final List<Feature> featureList = new ArrayList<>();
        featureList.add(feature);
        featureList.add(featureL);
        final AnnotateImageRequest annotateImageReq = new AnnotateImageRequest();
        annotateImageReq.setFeatures(featureList);
        annotateImageReq.setImage(getImageEncodeImage(bitmap));

        new AsyncTask<Object, Void, Result>() {
            @Override
            protected Result doInBackground(Object... params) {
                try {

                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(Arrays.asList(annotateImageReq));

                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse response = annotateRequest.execute();

                    AnnotateImageResponse imageResponses = response.getResponses().get(0);
                    List<EntityAnnotation> entityAnnotations;
                    entityAnnotations = imageResponses.getLabelAnnotations();
                    WebDetection web;
                    web = imageResponses.getWebDetection();

                    List<WebLabel> wl =	web.getBestGuessLabels();
                    List<WebEntity>	w = web.getWebEntities();
                    String message = "NONE";
                    if (web != null) {
                        message = "\nBestGuessLabels:\n" + wl.get(0).getLabel() + "\n";
                        message = message + "\nWebEntities:\n";
                        for (WebEntity entity : w) {
                            message = message + entity.getDescription() + " " + entity.getScore() + "\n";
                        }
                    }

                    if (entityAnnotations != null) {
                        message = message + "\nLabelDetections:\n";
                        for (EntityAnnotation entity : entityAnnotations) {
                            message = message + entity.getDescription() + " " + entity.getScore() + "\n";
                        }
                    }

                    Log.d("tagtag", message);
                    saveMessage = message;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    testBtm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();
                    Result R = new Result();
                    R.setParam(imageBytes, saveMessage);
                    return R;
                } catch (GoogleJsonResponseException e) {
                    Log.d("recognition", "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d("recognition", "failed to make API request because of other IOException " + e.getMessage());
                }
                Result R1 = new Result();
                return R1;
            }

            protected void onPostExecute(Result R) {
                imageUploadProgress.setVisibility(View.INVISIBLE);

                Intent intentSearch = new Intent(recognition.this, search.class);
                intentSearch.putExtra("labels", R.saveMessage);
                intentSearch.putExtra("imageBytes", R.imageBytes);
                startActivity(intentSearch);
            }
        }.execute();
    }

    @NonNull
    private Image getImageEncodeImage(Bitmap bitmap) {
        Image base64EncodedImage = new Image();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        base64EncodedImage.encodeContent(imageBytes);
        return base64EncodedImage;
    }
}