package com.origino.easylang;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICTURE_RESULT = 0;
    ImageView imageView;
    Uri imageUri;
    TextView foundtext;
    TextView foundLangCode;
    TextView translated;
    String langcode;
    String foundtextstring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        foundtext = findViewById(R.id.foundtext);
        foundLangCode = findViewById(R.id.foundlang);
        translated = findViewById(R.id.translated);
    }


    private void dispatchTakePictureIntent() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PICTURE_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case PICTURE_RESULT:
                if (requestCode == PICTURE_RESULT)
                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            Bitmap thumbnail = MediaStore.Images.Media.getBitmap(
                                    getContentResolver(), imageUri);

                            float degrees = 90; //rotation degree
                            Matrix matrix = new Matrix();
                            matrix.setRotate(degrees);
                            thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
                            imageView.setImageBitmap(thumbnail);
                            String imageurl = getRealPathFromURI(imageUri);
                            textrecog(thumbnail);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
        }
    }

    void textrecog(Bitmap imageBitmap) {
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
                                Log.i("info", String.valueOf(visionText.getText()));
                                foundtext.setText(visionText.getText());
                                foundtextstring = visionText.getText();
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.i("info", "String.valueOf(visionText)");

                                    }
                                });

    }

    public void textcamera(View view) {
        dispatchTakePictureIntent();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }



    public void translate(View view) {
        Intent intent = new Intent(MainActivity.this, translateActivity.class);
        intent.putExtra("keyName", foundtextstring);  // pass your values and retrieve them in the other Activity using keyName
        startActivity(intent);
    }


}