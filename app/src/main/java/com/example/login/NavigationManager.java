package com.example.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationManager {

    public static void setupBottomNavigationView(BottomNavigationView bottomNavigationView, Context context) {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menuHome) {
                    Intent intent = new Intent(context, Homepage.class);
                    context.startActivity(intent);
                    if(context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                    return true;
                } else if (id == R.id.menuSettings) {
                    Intent intent = new Intent(context, Settings.class);
                    context.startActivity(intent);
                    return true;
                } else if (id == R.id.qrCode) {
                    Intent intent = new Intent(context, com.example.qrcodereader.QRCodeReader.class);
                    context.startActivity(intent);
                    return true;
                    //Weitere If Anweisungen für andere Icons
                } return false;
            }
        });
    }

}