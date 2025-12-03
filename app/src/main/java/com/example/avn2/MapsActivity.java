package com.example.avn2;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor; // Adicionado
import android.database.sqlite.SQLiteDatabase; // Adicionado
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView; // Adicionado
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.example.avn2.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private TrilhasDB trilhasDB;

    //  Variáveis para os textos sobrepostos
    private TextView tvDistancia, tvDuracao, tvVelMedia, tvVelMax, tvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tvDistancia = findViewById(R.id.tvInfoDistancia);
        tvDuracao = findViewById(R.id.tvInfoDuracao);
        tvVelMedia = findViewById(R.id.tvInfoVelMedia);
        tvVelMax = findViewById(R.id.tvInfoVelMax);
        tvData = findViewById(R.id.tvInfoData);

        trilhasDB = new TrilhasDB(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        configurarPreferencias(mMap);

        // Recuperar o ID passado pelo Adapter
        long idTrilha = getIntent().getLongExtra("ID_TRILHA", -1);

        if (idTrilha != -1) {
            carregarTrilhaNoMapa(idTrilha);
            carregarDetalhesTexto(idTrilha);
        } else {
            Toast.makeText(this, "Erro ao carregar trilha.", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarPreferencias(GoogleMap googleMap) {
        mMap = googleMap;
        SharedPreferences preferences = getSharedPreferences("My preferences", Context.MODE_PRIVATE);

        int mapaSalvo = preferences.getInt("mapa", R.id.radioButtonVetorial);

        if (mapaSalvo == R.id.radioButtonVetorial) mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (mapaSalvo == R.id.radioButtonSatelite) mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    private void carregarTrilhaNoMapa(long idTrilha) {
        ArrayList<Waypoint> listaPontos = trilhasDB.recuperarWaypointsDaTrilha(idTrilha);

        if (listaPontos != null && !listaPontos.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.RED) // Cor da linha (pode mudar se quiser)
                    .width(15);       // Espessura da linha

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            // adiciona cada ponto à linha e ao construtor de limites (para focar a câmera)
            for (Waypoint wp : listaPontos) {
                LatLng ponto = new LatLng(wp.getLatitude(), wp.getLongitude());
                polylineOptions.add(ponto);
                builder.include(ponto);
            }

            // adiciona a linha ao mapa
            mMap.addPolyline(polylineOptions);

            // Move a câmera
            try {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            } catch (Exception e) {
                // foca no primeiro ponto
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polylineOptions.getPoints().get(0), 15));
            }
        } else {
            Toast.makeText(this, "Esta trilha não possui pontos gravados.", Toast.LENGTH_SHORT).show();
        }
    }

    // Metodo para buscar dados e preencher o CardView
    private void carregarDetalhesTexto(long idTrilha) {
        SQLiteDatabase db = trilhasDB.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM trilhas WHERE _id = ?", new String[]{String.valueOf(idTrilha)});

        if (cursor.moveToFirst()) {
            double distMetros = cursor.getDouble(cursor.getColumnIndexOrThrow("distancia_total"));
            long tempoMs = cursor.getLong(cursor.getColumnIndexOrThrow("tempo_duracao"));
            double velMedia = cursor.getDouble(cursor.getColumnIndexOrThrow("velocidade_media"));
            double velMax = cursor.getDouble(cursor.getColumnIndexOrThrow("velocidade_maxima"));
            String dataInicio = cursor.getString(cursor.getColumnIndexOrThrow("data_inicio"));

            // Conversões
            double distKm = distMetros / 1000.0;
            long segundosTotal = tempoMs / 1000;
            long minutos = (segundosTotal / 60) % 60;
            long horas = segundosTotal / 3600;

            // Sets
            tvDistancia.setText(String.format("Distância: %.2f km", distKm));
            tvVelMedia.setText(String.format("Vel. Média: %.1f km/h", velMedia));
            tvVelMax.setText(String.format("Vel. Max: %.1f km/h", velMax));
            tvData.setText("Data: " + dataInicio);

            if (horas > 0) {
                tvDuracao.setText(String.format("Duração: %02d:%02d:%02d", horas, minutos, segundosTotal % 60));
            } else {
                tvDuracao.setText(String.format("Duração: %02d:%02d", minutos, segundosTotal % 60));
            }
        }
        cursor.close();
    }
}