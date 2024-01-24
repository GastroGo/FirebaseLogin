package com.example.Tische;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Bestellungen.BestellungenActivity;
import com.example.DBKlassen.Gericht;
import com.example.DBKlassen.GerichteModel;
import com.example.DBKlassen.TablelistModel;
import com.example.DBKlassen.Tische;
import com.example.login.DropdownManager;
import com.example.login.Model;
import com.example.login.R;
import com.example.login.Tisch;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class TischeActivity extends AppCompatActivity implements RV_Adapter_Tische.OnItemClickListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbRef = database.getReference("Restaurants");

    TablelistModel tableListO = TablelistModel.getInstance();
    GerichteModel gerichteListO = GerichteModel.getInstance();
    RecyclerView recyclerView;

    int NumberOfTables = 20;
    int NumberOfGerichte;
    String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyDarkMode();
        setContentView(R.layout.activity_tische);
        restaurantId = getIntent().getStringExtra("restaurantId");
        TextView headerText = findViewById(R.id.text);
        headerText.setText("Tische");



        DropdownManager dropdownManager = new DropdownManager(this, R.menu.dropdown_menu, R.id.imageMenu);
        dropdownManager.setupDropdown();

        recyclerView = findViewById(R.id.mRecyclerView);
        RV_Adapter_Tische adapterTische = new RV_Adapter_Tische(this, restaurantId);
        recyclerView.setAdapter(adapterTische);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        FloatingActionButton returnButton = findViewById(R.id.btn_back);

        returnButton.setOnClickListener(view -> finish());

        dbRef.child(restaurantId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                NumberOfTables = (int) snapshot.child("tische").getChildrenCount();
                NumberOfGerichte = (int) snapshot.child("speisekarte").getChildrenCount();
                adapterTische.setNumberOfGerichte(NumberOfGerichte);

                tableListO.setup(NumberOfTables);
                gerichteListO.setup(NumberOfGerichte);

                for(int x = 0; x < NumberOfTables; x++){
                    String xString = "T" + String.format("%03d", (x + 1));
                    tableListO.getTischeArray()[x] = snapshot.child("tische").child(xString).getValue(Tisch.class);
                }

                for(int x = 0; x < NumberOfGerichte; x++){
                    String xString = "G" + String.format("%03d", (x + 1));
                    gerichteListO.getGerichte()[x] = snapshot.child("speisekarte").child(xString).getValue(Gericht.class);
                }

                adapterTische.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onItemClick(int tableNumber) {
        Intent intent = new Intent(this, BestellungenActivity.class);
        intent.putExtra("TableNr", tableNumber);
        intent.putExtra("restaurantId", restaurantId);

        startActivity(intent);
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
