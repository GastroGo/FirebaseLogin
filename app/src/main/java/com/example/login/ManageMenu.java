package com.example.login;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageMenu extends AppCompatActivity {

    FloatingActionButton buttonAdd;
    FloatingActionButton back;
    private DishAdapter dishAdapter;
    private List<Speisekarte> dishes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyDarkMode();
        setContentView(R.layout.activity_manage_menu);

        DropdownManager dropdownManager = new DropdownManager(this, R.menu.dropdown_menu, R.id.imageMenu);
        dropdownManager.setupDropdown();



        back = findViewById(R.id.btn_back);
        buttonAdd = findViewById(R.id.buttonAdd);
        String restaurantId = getIntent().getStringExtra("restaurantId");   //Übergabe der Restaurant ID

        back.setOnClickListener(v -> {
            onBackPressed();
        });

        buttonAdd.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ManageMenu.this, R.style.RoundedDialog);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_add_dish, null);
            builder.setView(view);

            EditText dishName = view.findViewById(R.id.dish_name);
            EditText dishPrice = view.findViewById(R.id.dish_price);
            Button addDishButton = view.findViewById(R.id.add_dish_button);
            Button cancelButton = view.findViewById(R.id.cancel_button);

            AlertDialog dialog = builder.create();

            addDishButton.setOnClickListener(v1 -> {
                String name = dishName.getText().toString();
                String priceString = dishPrice.getText().toString();

                if (name.isEmpty() || priceString.isEmpty()) {
                    Toast.makeText(ManageMenu.this, "Eingabe unvollständig", Toast.LENGTH_SHORT).show();
                    return;
                }

                double price = Double.parseDouble(priceString);

                //Erstellen Sie eine vordefinierte Liste von Zutaten
                Map<String, Boolean> zutaten = new HashMap<>();
                zutaten.put("eier", true);
                zutaten.put("fleisch", true);
                zutaten.put("milch", true);

                Speisekarte gericht = new Speisekarte(name, price, null, zutaten);

                DatabaseReference dbRefDishes = FirebaseDatabase.getInstance()
                        .getReference("Restaurants")
                        .child(restaurantId)
                        .child("speisekarte");
                dbRefDishes.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long count = dataSnapshot.getChildrenCount();
                        String dishKey = "G" + String.format("%03d", count + 1);

                        dbRefDishes.child(dishKey).setValue(gericht);

                        dishes.add(gericht);
                        dishAdapter.notifyDataSetChanged();

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

                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            });

            cancelButton.setOnClickListener(v2 -> dialog.dismiss());

            dialog.show();
        });

        dishes = new ArrayList<>();
        dishAdapter = new DishAdapter(dishes, restaurantId);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(dishAdapter);

        loadDishes();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Restaurants").child(restaurantId).child("speisekarte");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dishSnapshot : dataSnapshot.getChildren()) {  //Geht durch alle Gerichte in Speisekarte

                    Speisekarte dish = dishSnapshot.getValue(Speisekarte.class);

                    Map<String, Boolean> zutaten = dish.getZutaten();

                    if (zutaten != null) {
                        for (String zutat : zutaten.keySet()) {
                            dbRef.child(dishSnapshot.getKey()).child("zutaten").child(zutat).setValue(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }     //Carglass
        });
    }

    private void loadDishes() {
        String restaurantId = getIntent().getStringExtra("restaurantId");
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Restaurants").child(restaurantId).child("speisekarte");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dishes.clear();
                for (DataSnapshot dishSnapshot : dataSnapshot.getChildren()) {
                    Speisekarte dish = dishSnapshot.getValue(Speisekarte.class);
                    dishes.add(dish);
                }
                dishAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManageMenu.this, "Houston, we have a problem", Toast.LENGTH_SHORT).show();
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