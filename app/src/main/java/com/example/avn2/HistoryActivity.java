package com.example.avn2;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TrilhaAdapter adapter;
    List<TrilhaModelo> listaTrilhas;
    TrilhasDB trilhasDB;
    TextView tvEmpty;
    Button btnLimpar;
    Button btnApagarIntervalo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerViewTrilhas);
        tvEmpty = findViewById(R.id.tvEmptyView);
        trilhasDB = new TrilhasDB(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnLimpar = findViewById(R.id.btnLimparHistorico);
        btnLimpar.setOnClickListener(v -> confirmarExclusaoGeral());

        btnApagarIntervalo = findViewById(R.id.btnApagarIntervalo);
        btnApagarIntervalo.setOnClickListener(v -> mostrarSeletorDeIntervalo());

        carregarDados();
    }

    // Atualiza a lista sempre que voltar para essa tela (ex: ao voltar do Mapa)
    @Override
    protected void onResume() {
        super.onResume();
        carregarDados();
    }

    private void confirmarExclusaoGeral() {
        new AlertDialog.Builder(this)
                .setTitle("Apagar Tudo")
                .setMessage("Tem certeza que deseja apagar todas as trilhas do histórico?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    trilhasDB.excluirTodasTrilhas();
                    Toast.makeText(this, "Histórico limpo!", Toast.LENGTH_SHORT).show();
                    carregarDados();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void mostrarSeletorDeIntervalo() {
        final Calendar c = Calendar.getInstance();
        int ano = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        // 1. Escolher Data INICIAL
        DatePickerDialog datePickerInicio = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth1) -> {
                    // Formata para YYYY-MM-DD (para o DB)
                    String dataInicioFormatada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth1);

                    // Após escolher a primeira, abre logo o seletor da segunda
                    escolherDataFinal(dataInicioFormatada);

                }, ano, mes, dia);

        datePickerInicio.setTitle("Selecione a Data INICIAL");
        datePickerInicio.show();
    }

    private void escolherDataFinal(String dataInicio) {
        final Calendar c = Calendar.getInstance();
        int ano = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        // 2. Escolher Data FINAL
        DatePickerDialog datePickerFim = new DatePickerDialog(this,
                (view, year2, month2, dayOfMonth2) -> {
                    // Formata para YYYY-MM-DD
                    String dataFimFormatada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year2, month2 + 1, dayOfMonth2);

                    confirmarExclusaoIntervalo(dataInicio, dataFimFormatada);

                }, ano, mes, dia);

        datePickerFim.setTitle("Selecione a Data FINAL");
        datePickerFim.show();
    }

    private void confirmarExclusaoIntervalo(String dataInicio, String dataFim) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar Intervalo")
                .setMessage("Deseja apagar as trilhas entre " + dataInicio + " e " + dataFim + "?")
                .setPositiveButton("Sim", (dialog, which) -> {

                    // Chama o método do banco
                    trilhasDB.excluirTrilhasPorIntervalo(dataInicio, dataFim);

                    Toast.makeText(this, "Trilhas apagadas!", Toast.LENGTH_SHORT).show();
                    carregarDados(); // Atualiza a lista na tela
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private void carregarDados() {
        listaTrilhas = new ArrayList<>();
        Cursor cursor = trilhasDB.buscarTodasTrilhas(); // Consulta as Trilhas

        if (cursor.getCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                String data = cursor.getString(cursor.getColumnIndexOrThrow("data_inicio"));
                double dist = cursor.getDouble(cursor.getColumnIndexOrThrow("distancia_total"));
                long duracao = cursor.getLong(cursor.getColumnIndexOrThrow("tempo_duracao"));

                listaTrilhas.add(new TrilhaModelo(id, nome, data, dist, duracao));
            }
        }

        adapter = new TrilhaAdapter(this, listaTrilhas);
        recyclerView.setAdapter(adapter);
    }
}