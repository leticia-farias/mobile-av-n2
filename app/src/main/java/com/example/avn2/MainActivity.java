package com.example.avn2;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Armazenamento interno
        String dbPath = "data/data/com.example.avn2/MeuBanco.db";
        try {
            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            // Faça algo com o banco
            // Feche o banco
            db.close();
        } catch (SQLiteException e) {
            System.out.println(e.getMessage());
        }

        Button mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener(this);

        Button configButton = findViewById(R.id.config_button);
        configButton.setOnClickListener(this);

        Button obterTrilhaButton = findViewById(R.id.obter_trilha_button);
        obterTrilhaButton.setOnClickListener(this);

        Button creditosButton = findViewById(R.id.btn_creditos);
        creditosButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.map_button) {
            Intent i = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(i);
        }
        if (view.getId() == R.id.config_button) {
            Intent i = new Intent(MainActivity.this, ConfigActivity.class);
            startActivity(i);
        }
        if (view.getId() == R.id.obter_trilha_button) {
            Intent i = new Intent(MainActivity.this, GetRouteActivity.class);
            startActivity(i);
        }
        if (view.getId() == R.id.btn_creditos) {
            Intent i = new Intent(MainActivity.this, CreditosActivity.class);
            startActivity(i);
        }
    }
}