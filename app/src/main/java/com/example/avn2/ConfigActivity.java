package com.example.avn2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etPeso;
    EditText etAltura;
    EditText etNascimento;
    RadioButton rdFem;
    RadioButton rdMasc;
    RadioButton rdVetorial;
    RadioButton rdSatelite;
    RadioButton rdNorth;
    RadioButton rdCourse;
    Button btnCancelar;
    Button btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        etPeso = findViewById(R.id.editPeso);
        etAltura = findViewById(R.id.editAltura);
        etNascimento = findViewById(R.id.editNascimento);

        rdFem = findViewById(R.id.radioButtonFem);
        rdMasc = findViewById(R.id.radioButtonMasc);
        rdVetorial = findViewById(R.id.radioButtonVetorial);
        rdSatelite = findViewById(R.id.radioButtonSatelite);
        rdNorth = findViewById(R.id.radioButtonNorth);
        rdCourse = findViewById(R.id.radioButtonCourse);

        btnCancelar = findViewById(R.id.buttonConfigCancelar);
        btnCancelar.setOnClickListener(this);

        btnSalvar = findViewById(R.id.buttonConfigSalvar);
        btnSalvar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonConfigCancelar) {
            finish();
        } else if (v.getId() == R.id.buttonConfigSalvar) {

        }
    }

}