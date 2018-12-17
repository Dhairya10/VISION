package com.datadit.vision;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.auth.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.translate.AmazonTranslateAsyncClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.github.chrisbanes.photoview.PhotoView;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import pl.droidsonroids.gif.GifImageView;

import static com.datadit.vision.AWSCredentials.getCognitoPoolId;
import static com.datadit.vision.AWSCredentials.getCognitoRegion;

// This Activity contains all the business logic of the project

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private HashMap<String, String> rekognitionMap = new HashMap<>();
    private TextView textViewDetails;
    byte[] bytes;
    private ProgressBar progressBar;
    private boolean listIsEmpty = false;
    private volatile String translatedText;
    private ImageButton imageButtonPhoto, imageButtonCamera;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int FILE_CODE = 2;
    private static final String FILTER_TITLE = "Select an Image";
    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private PhotoView imageViewRekognition;
    private static final String COGNITO_POOL_ID = getCognitoPoolId();
    private static final Regions MY_REGION = getCognitoRegion();
    private AWSCredentialsProvider credentialsProvider;
    private StringBuilder stringBuilderKey = new StringBuilder();
    private static String languageNotDetected = "Unable to detect text language";
    private Dialog dialog;
    private GifImageView gifImageViewSound;
    private MediaPlayer mediaPlayer;
    private TextView textViewHeading;
    private String voiceAssistant = "Kendra";
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "In onCreate");
        textViewDetails = findViewById(R.id.textViewDetails);
        imageViewRekognition = findViewById(R.id.imageViewRekognition);
        progressBar = findViewById(R.id.progressBar);
        imageButtonPhoto = findViewById(R.id.imageButtonPhoto);
        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        dialog = new Dialog(this);
        gifImageViewSound = findViewById(R.id.imageViewSound);
        gifImageViewSound.setVisibility(View.GONE);
        LinearLayout linearLayoutImage = findViewById(R.id.linearLayoutImage);
        LinearLayout linearLayoutText = findViewById(R.id.linearLayoutText);
        textViewHeading = findViewById(R.id.textViewHeading);
        textViewHeading.setText(R.string.select_image_processing);
        imageButtonCamera.setOnClickListener(this);
        imageButtonPhoto.setOnClickListener(this);

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                COGNITO_POOL_ID,
                MY_REGION
        );

        // PERMISSION CHECK : User is required to give the permission explicitly
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, "Permission Denied, cannot read external files", Toast.LENGTH_LONG).show();
                Log.d(LOG_TAG, "Permission Denied");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
                Log.d(LOG_TAG, "Asking for permission");
            }
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                Log.i(LOG_TAG, "User is connected to Internet");
                linearLayoutText.setVisibility(View.VISIBLE);
                linearLayoutImage.setVisibility(View.VISIBLE);
            } else {
                linearLayoutImage.setVisibility(View.GONE);
                linearLayoutText.setVisibility(View.GONE);
                Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
                Log.i(LOG_TAG, "Internet Connectivity Issue");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "Permission Granted");
                } else {
                    Toast.makeText(this, "Permission Denied, cannot read external files", Toast.LENGTH_LONG).show();
                    Log.d(LOG_TAG, "Permission Denied");
                }
            }
        }
    }

    // Handling Button Clicks
    @Override
    public void onClick(View view) {

        if (view == imageButtonCamera) {
            Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (imageIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(imageIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else if (view == imageButtonPhoto) {

            new MaterialFilePicker()
                    .withActivity(this)
                    .withRequestCode(FILE_CODE)
                    .withTitle(FILTER_TITLE)
                    .withFilterDirectories(true)
                    .start();
        }
    }

    //Handling Button Click Events
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }
            imageViewRekognition.setImageBitmap(imageBitmap);
            encodeImage(imageBitmap);
        }

        if (requestCode == FILE_CODE && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            File file = new File(String.valueOf(filePath));
            if (file.exists()) {
                Log.d(LOG_TAG, "File Exists");
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageViewRekognition.setImageBitmap(bitmap);
                encodeImage(bitmap);
            } else {
                Log.e(LOG_TAG, "File is null");
            }
        }
    }

    // Encoding the image into a byte array
    public void encodeImage(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        int length = byteArrayOutputStream.toByteArray().length;
        bytes = new byte[length];
        bytes = byteArrayOutputStream.toByteArray();
        RekognitionAsyncTask rekognitionAsyncTask = new RekognitionAsyncTask();
        rekognitionAsyncTask.execute();
    }

    // Encoded byte array is passed to AWS Rekognition
    public HashMap<String, String> recogniseImage() {

        AmazonRekognition client = new AmazonRekognitionClient(credentialsProvider);
        DetectTextRequest request = new DetectTextRequest()
                .withImage(new Image()
                        .withBytes(ByteBuffer.wrap(bytes)));

        try {
            DetectTextResult result = client.detectText(request);
            List<TextDetection> textDetections = result.getTextDetections();

            Log.d(LOG_TAG, "List : " + textDetections);
            if (textDetections.isEmpty()) {
                listIsEmpty = true;
                Log.d(LOG_TAG, "List is Empty");
            }

            for (TextDetection text : textDetections) {
                rekognitionMap.put(text.getDetectedText(), text.getConfidence().toString());

            }

        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception : " + e);
            e.printStackTrace();
        }
        return rekognitionMap;
    }

    // Extracted text is passed onto AWS Translate
    public Void translateText(String text) {
        Log.d(LOG_TAG, "Text Received for translation : " + text);
        AWSCredentials awsCredentials = new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return "AKIAIAAJDIQQ3K2WG4QA";
            }

            @Override
            public String getAWSSecretKey() {
                return "RXZxe7xD7OvQD6AztncfQJmGSdFO0T5+WkUQcIl6";
            }
        };

        AmazonTranslateAsyncClient translateAsyncClient = new AmazonTranslateAsyncClient(awsCredentials);
        final TranslateTextRequest translateTextRequest = new TranslateTextRequest()
                .withText(text)
                .withSourceLanguageCode("auto")
                .withTargetLanguageCode("en");
        translateAsyncClient.translateTextAsync(translateTextRequest, new AsyncHandler<TranslateTextRequest, TranslateTextResult>() {
            @Override
            public void onError(Exception e) {
                Log.e(LOG_TAG, "Error occurred in translating the text: " + e.getLocalizedMessage());
                //convertTextToSpeech(languageNotDetected);
                translatedText = languageNotDetected;
                PollyAsyncTask pollyAsyncTask = new PollyAsyncTask();
                pollyAsyncTask.execute(languageNotDetected);
            }

            @Override
            public void onSuccess(TranslateTextRequest request, TranslateTextResult translateTextResult) {
                translatedText = translateTextResult.getTranslatedText() + " ";
                Log.d(LOG_TAG, "Translated Text : " + translatedText);
                PollyAsyncTask pollyAsyncTask = new PollyAsyncTask();
                pollyAsyncTask.execute(translatedText);
            }
        });

        return null;
    }

    // Translated text is passed onto AWS Polly
    public Void convertTextToSpeech(String text) {

        AmazonPollyPresigningClient client = new AmazonPollyPresigningClient(credentialsProvider);

        SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest = new SynthesizeSpeechPresignRequest()
                .withText(text)
                .withVoiceId(voiceAssistant)
                .withOutputFormat(OutputFormat.Mp3);
        Log.d(LOG_TAG, "Voice :" + voiceAssistant);

        URL presignedSynthesizeSpeechUrl =
                client.getPresignedSynthesizeSpeechUrl(synthesizeSpeechPresignRequest);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to set data source for media player" + e.getMessage());
        }

        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.d(LOG_TAG, "Text To Speech Translation complete");
            }
        });
        return null;
    }

    // RekognitionAsyncTask is used for background execution of recogniseImage(),since we can't make network calls on the main thread
    private class RekognitionAsyncTask extends AsyncTask<Void, Void, HashMap<String, String>> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            textViewDetails.setText("");
            rekognitionMap.clear();
            listIsEmpty = false;
            stringBuilderKey.setLength(0);
        }

        @Override
        protected HashMap<String, String> doInBackground(Void... voids) {
            return recogniseImage();
        }

        @Override
        protected void onPostExecute(HashMap<String, String> stringStringHashMap) {
            progressBar.setVisibility(View.GONE);
            textViewHeading.setText(R.string.rekognition_heading);
            if (listIsEmpty) {
                textViewDetails.setTextColor(getResources().getColor(R.color.colorNoText));
                textViewDetails.setText(R.string.text_identify);

            } else {
                for (Map.Entry<String, String> entry : stringStringHashMap.entrySet()) {
                    String key = entry.getKey();
                    stringBuilderKey.append(key);
                    stringBuilderKey.append(" ");
                    String value = entry.getValue();
                    String text = key + "  :  " + value + "\n";
                    Log.d(LOG_TAG, "Extracted Text : " + text);
                    textViewDetails.setTextColor(getResources().getColor(R.color.colorTextRecognise));
                    textViewDetails.append(text);
                }
                TranslateAsyncTask translateAsyncTask = new TranslateAsyncTask();
                translateAsyncTask.execute(stringBuilderKey.toString());
            }
        }
    }

    // TranslateAsyncTask is used for background execution of translateText()
    private class TranslateAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            return translateText(strings[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            gifImageViewSound.setVisibility(View.VISIBLE);
        }
    }

    // PollyAsyncTask is used for background execution of convertTextToSpeech()
    private class PollyAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            return convertTextToSpeech(strings[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String text = "Translated Text : " + translatedText;
            textViewDetails.append("\n" + text);
            gifImageViewSound.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying() && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer = new MediaPlayer();
    }

    // To create the menu which allows the user to change Voice Assistant
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_speaker) {
            showPopup();
        }
        return super.onOptionsItemSelected(item);
    }

    // Popup for changing Voice Assistant
    private void showPopup() {
        dialog.setContentView(R.layout.layout_voice_assistant);
        Spinner spinnerVoices = dialog.findViewById(R.id.spinnerVoices);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.voices_array, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVoices.setAdapter(arrayAdapter);

        spinnerVoices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i) {
                    case 0:
                        voiceAssistant = "Kendra";
                        break;
                    case 1:
                        voiceAssistant = "Kendra";
                        dialog.dismiss();
                        break;
                    case 2:
                        voiceAssistant = "Aditi";
                        dialog.dismiss();
                        break;
                    case 3:
                        voiceAssistant = "Amy";
                        dialog.dismiss();
                        break;
                    case 4:
                        voiceAssistant = "Miguel";
                        dialog.dismiss();
                        break;
                    case 5:
                        voiceAssistant = "Zhiyu";
                        dialog.dismiss();
                        break;
                    case 6:
                        voiceAssistant = "Takumi";
                        dialog.dismiss();
                        break;
                    case 7:
                        voiceAssistant = "Mathieu";
                        dialog.dismiss();
                        break;
                    case 8:
                        voiceAssistant = "Giorgio";
                        dialog.dismiss();
                        break;
                    case 9:
                        voiceAssistant = "Maxim";
                        dialog.dismiss();
                        break;
                    case 10:
                        voiceAssistant = "Cristiano";
                        dialog.dismiss();
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(LOG_TAG, "Nothing Selected");
            }
        });
        dialog.show();
        Log.d(LOG_TAG, "Voice :" + voiceAssistant);
    }

    public void onBackPressed () {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}