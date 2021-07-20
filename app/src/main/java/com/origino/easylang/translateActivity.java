package com.origino.easylang;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

public class translateActivity extends AppCompatActivity {
    EditText text;
    TextView englishText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        text = findViewById(R.id.enter_text);
        englishText = findViewById(R.id.translatedText);
        String data = getIntent().getExtras().getString("keyName","defaultKey");
        text.setText(data);

    }

    public void translate(View v) {
        translateTextToEnglish(text.getText().toString());
    }

    public void translateText(FirebaseTranslator langTranslator) {
        //translate source text to english
        langTranslator.translate(text.getText().toString())
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                englishText.setText(translatedText);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(translateActivity.this,
                                        "Problem in translating the text",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

    }

    public void downloadTranslatorAndTranslate(String langCode) {
        //get source language id from bcp code
        int sourceLanguage = FirebaseTranslateLanguage
                .languageForLanguageCode(langCode);

        //create translator for source and target languages
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()

                        .setSourceLanguage(sourceLanguage)
                        .setTargetLanguage(FirebaseTranslateLanguage.EN)
                        .build();
        final FirebaseTranslator langTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        //download language models if needed
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        langTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                Log.d("translator", "downloaded lang  model");
                                //after making sure language models are available
                                //perform translation
                                translateText(langTranslator);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(translateActivity.this,
                                        "Problem in translating the text entered",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
    }

    public void translateTextToEnglish(String text) {
        //First identify the language of the entered text
        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (languageCode != "und") {
                                    Log.d("translator", "lang "+languageCode);
                                    //download translator for the identified language
                                    // and translate the entered text into english
                                    downloadTranslatorAndTranslate(languageCode);
                                } else {
                                    Toast.makeText(translateActivity.this,
                                            "Could not identify language of the text entered",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(translateActivity.this,
                                        "Problem in identifying language of the text entered",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
    }
}