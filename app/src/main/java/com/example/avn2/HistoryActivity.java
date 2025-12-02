package com.example.avn2;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TrilhaAdapter adapter;
    List<TrilhaModelo> listaTrilhas;
    TrilhasDB trilhasDB;
    TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerViewTrilhas);
        tvEmpty = findViewById(R.id.tvEmptyView);
        trilhasDB = new TrilhasDB(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        carregarDados();
    }

    // Atualiza a lista sempre que voltar para essa tela (ex: ao voltar do Mapa)
    @Override
    protected void onResume() {
        super.onResume();
        carregarDados();
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