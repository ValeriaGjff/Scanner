package com.example.sanner;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MaterialButton buttonCamera;
    private ImageView pdfImage;
    private MaterialButton ButtonScan;
    private TextView result;
    private static final int RequestCamera = 100;
    private Uri Uri = null;
    private BarcodeScannerOptions scannerOptions;
    private BarcodeScanner barcodeScanner;
    private static final String TAG = "ГлавныйТег";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonCamera = findViewById(R.id.buttonCamera);
        pdfImage = findViewById(R.id.pdfImage);
        ButtonScan = findViewById(R.id.ButtonScan);
        result = findViewById(R.id.result);



        scannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();

        barcodeScanner = BarcodeScanning.getClient(scannerOptions);


        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    pickImage();
                } else {
                    сameraPermission();
                }
            }
        });

        ButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Uri == null) {
                    Toast.makeText(MainActivity.this, "Сначала выберите изображение", Toast.LENGTH_SHORT).show();
                } else {
                    detectResult();
                }
            }
        });
    }

    private void detectResult() { //обработка изображения
        try {
            InputImage inputImage = InputImage.fromFilePath(this, Uri);
            Task<List<Barcode>> barcodeResult = barcodeScanner.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                    extractBarCodeQRCodeInfo(barcodes);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Неудачное сканирование из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void extractBarCodeQRCodeInfo(List<Barcode> barcodes) { //получение информации и отображение результата
        for (Barcode barcode : barcodes) {
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();
            String rawValue = barcode.getRawValue();
            Log.d(TAG, "Извлечь информацию о штрих-коде QR-коде: Необработанное значение: " + rawValue);

            int valueType = barcode.getValueType();

            switch (valueType) {
                case Barcode.TYPE_WIFI: {
                    Barcode.WiFi typeWifi = barcode.getWifi();

                    String ssid = "" + typeWifi.getSsid();
                    String password = "" + typeWifi.getPassword();
                    String encryptionType = "" + typeWifi.getEncryptionType();

                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_WIFI: ");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: ssid:" + ssid);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: password:" + password);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: encryptionType:" + encryptionType);

                    result.setText("TYPE: TYPE_WIFI \nssid: " + ssid + "\npassword: " + password + "\nencryptionType: " + encryptionType + "\nraw value: " + rawValue);
                }
                break;
                case Barcode.TYPE_URL: {

                    Barcode.UrlBookmark typeUrl = barcode.getUrl();

                    String title = "" + typeUrl.getTitle();
                    String url = "" + typeUrl.getUrl();

                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_URL ");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: title: " + title);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: url: " + url);

                    result.setText("TYPE: TYPE_URL \ntitle: " + title + "\nurl: " + url + "\nraw value: " + rawValue);
                }
                break;

                case Barcode.TYPE_EMAIL: {
                    Barcode.Email typeEmail = barcode.getEmail();

                    String address = "" + typeEmail.getAddress();
                    String body = "" + typeEmail.getBody();
                    String subject = "" + typeEmail.getSubject();

                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_EMAIL ");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: address: " + address);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: body: " + body);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: subject: " + subject);

                    result.setText("TYPE: TYPE_EMAIL \naddress: " + address + "\nbody: " + body + "\nsubject: " + subject + "\nraw value: " + rawValue);
                }
                break;
                case Barcode.TYPE_CONTACT_INFO: {
                    Barcode.ContactInfo typeContact = barcode.getContactInfo();

                    String title = "" + typeContact.getTitle();
                    String organized = "" + typeContact.getOrganization();
                    String name = "" + typeContact.getName().getFirst() + "" + typeContact.getName().getLast();
                    String phone = "" + typeContact.getPhones().get(0).getNumber();

                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_CONTACT_INFO ");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: title: " + title);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: organized: " + organized);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: name: " + name);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: phones: " + phone);

                    result.setText("TYPE: TYPE_CONTACT_INFO \ntitle: " + title + "\norganized: " + organized + "\nname: " + name + "\nphone: " + phone + "\nraw value: " + rawValue);
                }
                break;
                default: {
                    result.setText("Значение: " + rawValue);
                }
            }
        }
    }
    private void pickImage() { //выбор изображения из камеры
        ContentValues content = new ContentValues();
        content.put(MediaStore.Images.Media.TITLE, "Sample Title");
        content.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");

        ContentResolver contentResolver = getContentResolver();
        if (contentResolver != null) {
            Uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri);
            cameraActivityResultLauncher.launch(intent);
        }
    }
    private final ActivityResultLauncher cameraActivityResultLauncher = registerForActivityResult( //отслеживает результат вызова камеры
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "onActivityResult: Uri: " + Uri);
                    pdfImage.setImageURI(Uri);
                } else {
                    Toast.makeText(MainActivity.this, "Отменено", Toast.LENGTH_SHORT).show();
                }
            }
    );
    private boolean checkPermission() { //проверка разрешения камеры
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }
    private void сameraPermission() { //запрос разрешения камеры
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, RequestCamera);
    }
    @Override
    public void onRequestPermissionsResult(int request, @NonNull String[] permiss, @NonNull int[] results) { //обработка результатов запроса
        super.onRequestPermissionsResult(request, permiss, results);
        if (request == RequestCamera) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(this, "Требуется разрешение камеры", Toast.LENGTH_SHORT).show();
            }
        }
    }
}