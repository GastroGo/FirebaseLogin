package com.example.mitarbeiterverwaltung;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login.DropdownManager;
import com.example.login.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;

public class MitarbeiterVerwalten extends AppCompatActivity {

    public long count;
    RecyclerView recyclerView;
    DatabaseReference database;
    MyAdapter myadapter;
    ArrayList<User> list;
    HashSet<String> keysSet;
    FloatingActionButton mAnlegen;
    Dialog dialog;
    Button mErstellen;
    EditText etName;
    TextView tvName, tvKey;
    FloatingActionButton back;

    public static String generateKey() {
        SecureRandom random = new SecureRandom();
        StringBuilder key = new StringBuilder();
        Model model = new Model();

        for (int i = 0; i < model.getKeyLength(); i++) {
            int randomIndex = random.nextInt(model.getKeyCharacters().length());
            char randomChar = model.getKeyCharacters().charAt(randomIndex);
            key.append(randomChar);
        }

        return key.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mitarbeiter_verwalten);
        String restaurantId = getIntent().getStringExtra("restaurantId");

        TextView headerText = findViewById(R.id.text);
        headerText.setText("Mitarbeiter");

        DropdownManager dropdownManager = new DropdownManager(this, R.menu.dropdown_menu, R.id.imageMenu);
        dropdownManager.setupDropdown();

        dialog = new Dialog(MitarbeiterVerwalten.this);
        dialog.setContentView(R.layout.m_anlegen);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.m_anlegen_bg));
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setDimAmount(0.5f);

        back = findViewById(R.id.btn_back);
        etName = dialog.findViewById(R.id.etName);
        mAnlegen = findViewById(R.id.mAnlegen);
        recyclerView = findViewById(R.id.mListe);
        database = FirebaseDatabase.getInstance().getReference("Schluessel").child(restaurantId);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        mErstellen = dialog.findViewById(R.id.mErstellen);

        mErstellen.setOnClickListener(v -> {
            String nM = "M" + String.format("%03d", count + 1);
            database.child(nM).child("name").setValue(etName.getText().toString());
            database.child(nM).child("key").setValue(generateKey());
            database.child(nM).child("UID").setValue("");
            dialog.dismiss();
            etName.setText("");
        });

        back.setOnClickListener(v -> {
            finish();
        });

        mAnlegen.setOnClickListener(v -> {
            dialog.show();
        });

        list = new ArrayList<>();
        keysSet = new HashSet<>();
        myadapter = new MyAdapter(this, list, restaurantId);
        recyclerView.setAdapter(myadapter);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                count = snapshot.getChildrenCount();
                list.clear(); // Clear the list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    list.add(user);
                }
                myadapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}