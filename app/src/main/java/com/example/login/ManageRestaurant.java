package com.example.login;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.mitarbeiterverwaltung.MitarbeiterVerwalten;
import com.example.qrcodepdf.PdfActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class ManageRestaurant extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView name;
    ConstraintLayout menu, schluessel, qrcode, orders;
    Button delete;
    FloatingActionButton back;
    Daten restaurantDaten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyDarkMode();
        setContentView(R.layout.activity_manage_restaurant);
        schluessel = findViewById(R.id.buttonEmployeeKeys);
        name = findViewById(R.id.text);
        menu = findViewById(R.id.buttonMenu);
        delete = findViewById(R.id.buttonDelete);
        qrcode = findViewById(R.id.buttonGenerateQR);
        orders = findViewById(R.id.buttonOrders);
        back = findViewById(R.id.btn_back);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            getData(user.getUid());
        }

        DropdownManager dropdownManager = new DropdownManager(this, R.menu.dropdown_menu, R.id.imageMenu);
        dropdownManager.setupDropdown();

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ManageMenu.class);
                intent.putExtra("restaurantId", restaurantDaten.getId());
                startActivity(intent);
            }
        });

        schluessel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MitarbeiterVerwalten.class);
                intent.putExtra("restaurantId", restaurantDaten.getId());
                startActivity(intent);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.RoundedDialog);
                LayoutInflater inflater = LayoutInflater.from(v.getContext());
                View view = inflater.inflate(R.layout.dialog_delete_dish, null);
                builder.setView(view);

                Button deleteButton = view.findViewById(R.id.delete_button);
                Button cancelButton = view.findViewById(R.id.cancel_button);

                AlertDialog dialog = builder.create();

                deleteButton.setOnClickListener(v1 -> {
                    dialog.dismiss();
                    deleteRestaurant(user.getUid());
                });
                cancelButton.setOnClickListener(v2 -> dialog.dismiss());
                TextView confirm = view.findViewById(R.id.confirm);
                confirm.setText("Möchten Sie das Restaurant wirklich löschen?");

                dialog.show();
            }
        });
        qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (restaurantDaten != null && restaurantDaten.getId() != null) {
                    Intent intent = new Intent(ManageRestaurant.this, PdfActivity.class);
                    intent.putExtra("restaurantId", restaurantDaten.getId());
                    startActivity(intent);
                } else {}
            }
        });
        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (restaurantDaten != null && restaurantDaten.getId() != null) {
                    Intent intent = new Intent(getApplicationContext(), com.example.Tische.TischeActivity.class);
                    intent.putExtra("restaurantId", restaurantDaten.getId()); // Pass the restaurant ID to TischeActivity activity
                    startActivity(intent);
                } else {}
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Einstellungen.class);
                startActivity(intent);
            }
        });
    }

    private void getData(String uid) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Restaurants");
        dbRef.orderByChild("daten/uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot restaurantSnapshot : dataSnapshot.getChildren()) {
                        Restaurant restaurant = restaurantSnapshot.getValue(Restaurant.class);
                        restaurantDaten = restaurant.getDaten();
                        Map<String, Speisekarte> speisekarte = restaurant.getSpeisekarte();
                        Map<String, Tisch> tische = restaurant.getTische();
                    }
                    displayData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void displayData() {
        if (restaurantDaten != null) {
            name.setText(restaurantDaten.getName());
            FloatingActionButton icon = findViewById(R.id.btn_back);
            icon.setImageResource(R.drawable.ic_manage);
            TextView dashboard = findViewById(R.id.textViewBack);
            dashboard.setText("Dashboard");
        }
    }

    private void deleteRestaurant(String uid) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Restaurants");
        DatabaseReference dbRef2 = FirebaseDatabase.getInstance().getReference("Schluessel");
        dbRef2.child(restaurantDaten.getId()).removeValue();
        dbRef.orderByChild("daten/uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot restaurantSnapshot : dataSnapshot.getChildren()) {
                        restaurantSnapshot.getRef().removeValue();
                    }
                    user.delete().addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                        } else {}   //ja würd mir stinken
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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