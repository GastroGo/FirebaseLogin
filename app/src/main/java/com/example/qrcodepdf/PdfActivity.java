package com.example.qrcodepdf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.example.login.DropdownManager;
import com.example.login.Model;
import com.example.login.R;
import com.example.login.Tisch;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PdfActivity extends AppCompatActivity {

    int anzahl = 0;
    String restaurantId;
    FloatingActionButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyDarkMode();
        setContentView(R.layout.activity_pdf);
        restaurantId = getIntent().getStringExtra("restaurantId");
        back = findViewById(R.id.btn_back);
        TextView headerText = findViewById(R.id.text);
        headerText.setText("Tisch QR-Codes");



        DropdownManager dropdownManager = new DropdownManager(this, R.menu.dropdown_menu, R.id.imageMenu);
        dropdownManager.setupDropdown();

        back.setOnClickListener(v -> {
            onBackPressed();
        });

        Slider slider = findViewById(R.id.slider);
        TextView sliderValue = findViewById(R.id.counter);

        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                anzahl = (int) value;
                sliderValue.setText(Integer.toString(anzahl));
            }
        });

        FloatingActionButton btnDownloadPDF = findViewById(R.id.btnDownloadPDF);
        btnDownloadPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTables(anzahl);
                downloadAndOpenPDF();
                generateTables();
            }
        });
    }

    public void addTables(int tableCount) {
        DatabaseReference dbRefTables = FirebaseDatabase.getInstance()
                .getReference("Restaurants")
                .child(restaurantId)
                .child("tische");

        for (int i = 0; i < tableCount; i++) {
            String tableKey = "T" + String.format("%03d", i + 1);

            Map<String, Integer> ordersMap = new HashMap<String, Integer>() {{
                put("G001", 0);
                put("G002", 0);
            }};

            Tisch newTable = new Tisch(ordersMap, new HashMap<>(ordersMap), 0);

            dbRefTables.child(tableKey).setValue(newTable);
        }
    }

    public void generateTables() {
        DatabaseReference dbRefDishes = FirebaseDatabase.getInstance()
                .getReference("Restaurants")
                .child(restaurantId)
                .child("speisekarte");
        dbRefDishes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dishSnapshot : dataSnapshot.getChildren()) {
                    String dishKey = dishSnapshot.getKey();

                    DatabaseReference dbRefTables = FirebaseDatabase.getInstance()
                            .getReference("Restaurants")
                            .child(restaurantId)
                            .child("tische");
                    dbRefTables.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot tableSnapshot : dataSnapshot.getChildren()) {
                                tableSnapshot.getRef()
                                        .child("bestellungen")
                                        .child(dishKey)
                                        .setValue(0);
                                tableSnapshot.getRef()
                                        .child("geschlosseneBestellungen")
                                        .child(dishKey)
                                        .setValue(0);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public String[] mergingID(int count) {
        String[] idMerged = new String[count];
        for (int i = 0; i < count; i++) {
            idMerged[i] = restaurantId + String.format("%03d", i + 1);
        }
        return idMerged;
    }

    public Bitmap[] gerneratingQRCode() {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        int index = 0;

        try {
            String[] idMerged = mergingID(anzahl);
            Bitmap[] bitmaps = new Bitmap[anzahl];

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

            for (String mergedId : idMerged) {
                BitMatrix bitMatrix = multiFormatWriter.encode(mergedId, BarcodeFormat.QR_CODE, 290, 290);
                Bitmap bitmap = addTableNumberBelowQRCode(barcodeEncoder.createBitmap(bitMatrix), mergedId);

                bitmaps[index] = bitmap;
                index++;
            }

            return bitmaps;
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }


    public void downloadAndOpenPDF() {

        // Erzeuge die PDF-Datei
        File pdfFile = ImageInPDF.createPDFWithImages(this, gerneratingQRCode());

        if (pdfFile != null) {
            // Öffne die PDF-Datei mit einem Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Fehler beim Erstellen der PDF-Datei", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap addTableNumberBelowQRCode(Bitmap qrCodeBitmap, String tableNumber) {
        Bitmap resultBitmap = Bitmap.createBitmap(qrCodeBitmap.getWidth(), qrCodeBitmap.getHeight() + 50, qrCodeBitmap.getConfig());
        Canvas canvas = new Canvas(resultBitmap);

        // Zeichne den QR-Code
        canvas.drawBitmap(qrCodeBitmap, 0, 0, null);

        // Konfiguriere den Text für die Tischnummer
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(25);
        paint.setAntiAlias(true);

        String finaleNumber = tableNumber.toString().substring(tableNumber.length() - 3, tableNumber.length());

        Rect textBounds = new Rect();
        paint.getTextBounds(finaleNumber, 0, finaleNumber.length(), textBounds);
        float textX = (qrCodeBitmap.getWidth() - textBounds.width()) / 2f;
        float textY = qrCodeBitmap.getHeight() + 48;

        canvas.drawText(finaleNumber, textX, textY, paint);
        return resultBitmap;
    }
    public void applyDarkMode() {
        Model model = Model.getInstance();
        model.load(this);
        if (model.getDarkmode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}