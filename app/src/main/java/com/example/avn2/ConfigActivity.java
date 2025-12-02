package com.example.avn2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;
    EditText etPeso, etAltura, etNascimento;
    RadioGroup rgGenero, rgMapa, rgNavegacao;
    Button btnCancelar, btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        sharedPreferences = getSharedPreferences("My preferences", Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        etPeso = findViewById(R.id.editPeso);
        etPeso.setText(sharedPreferences.getString("peso", ""));

        etAltura = findViewById(R.id.editAltura);
        etAltura.setText(sharedPreferences.getString("altura", ""));

        etNascimento = findViewById(R.id.editNascimento);
        etNascimento.setText(sharedPreferences.getString("nascimento", ""));

        rgGenero = findViewById(R.id.radioGroupGenero);
        rgGenero.check(sharedPreferences.getInt("genero", -1));

        rgMapa = findViewById(R.id.radioGroupMapa);
        rgMapa.check(sharedPreferences.getInt("mapa", R.id.radioButtonVetorial));
//        RadioButton mapaDefault = findViewById(R.id.radioButtonVetorial);
//        rgMapa.check(mapaDefault.getId());

        rgNavegacao = findViewById(R.id.radioGroupNavegacao);
        rgNavegacao.check(sharedPreferences.getInt("navegacao", R.id.radioButtonNorth));

        btnCancelar = findViewById(R.id.buttonConfigCancelar);
        btnCancelar.setOnClickListener(this);

        btnSalvar = findViewById(R.id.buttonConfigSalvar);
        btnSalvar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonConfigSalvar) {
            String pesoNoComponente = etPeso.getText().toString();
            String alturaNoComponente = etAltura.getText().toString();
            String nascimentoNoComponente = etNascimento.getText().toString();
            int generoSelecionado = rgGenero.getCheckedRadioButtonId();
            int mapaSelecionado = rgMapa.getCheckedRadioButtonId();
            int navegacaoSelecionada = rgNavegacao.getCheckedRadioButtonId();

            sharedPreferencesEditor.putString("peso", pesoNoComponente);
            sharedPreferencesEditor.putString("altura", alturaNoComponente);
            sharedPreferencesEditor.putString("nascimento", nascimentoNoComponente);
            sharedPreferencesEditor.putInt("genero", generoSelecionado);
            sharedPreferencesEditor.putInt("mapa", mapaSelecionado);
            sharedPreferencesEditor.putInt("navegacao", navegacaoSelecionada);

            sharedPreferencesEditor.commit();
            finish();
        }
        if (v.getId() == R.id.buttonConfigCancelar) {
            finish();
        }
    }
}