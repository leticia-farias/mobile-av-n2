package com.example.avn2;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GetRouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_UPDATES = 1;

    // Componentes de Interface
    private GoogleMap mMap;
    private TextView tvVelocidade, tvDistancia, tvCalorias, tvVelMax;
    private Chronometer cronometro;
    private Button btnStart, btnStop;

    // Localização e Banco
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private TrilhasDB trilhasDB;

    // Variáveis de Estado da Trilha
    private boolean isRecording = false;
    private ArrayList<Waypoint> tempWaypoints; // Guarda pontos na memória antes de salvar
    private Location lastLocation;
    private double distanciaTotal = 0.0;
    private double velocidadeMaxima = 0.0;
    private double somaVelocidades = 0.0;
    private int contadorLocais = 0;
    private long startTimeMillis;
    private String dataInicioStr;

    // Preferências do Usuário
    private float pesoUsuario = 70.0f; // Default 70kg
    private int tipoMapaPref;
    private int tipoNavegacaoPref; // 0 = North Up, 1 = Course Up

    // Desenho no Mapa
    private Polyline rotaDesenhada;
    private Circle circuloPrecisao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_route);

        // Inicializa UI
        tvVelocidade = findViewById(R.id.tvVelocidade);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvCalorias = findViewById(R.id.tvCalorias);
        tvVelMax = findViewById(R.id.tvVelMax);
        cronometro = findViewById(R.id.tvCronometro);
        btnStart = findViewById(R.id.StartGetRouteButton);
        btnStop = findViewById(R.id.StopGetRouteButton);

        // Inicializa Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Inicializa Banco e LocationClient
        trilhasDB = new TrilhasDB(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Carregar Preferências (Peso, Mapa, etc)
        carregarPreferencias();

        // Botão INICIAR
        btnStart.setOnClickListener(v -> {
            iniciarTrilha();
        });

        // Botão PARAR
        btnStop.setOnClickListener(v -> {
            pararTrilhaESalvar();
        });
    }

    private void carregarPreferencias() {
        SharedPreferences prefs = getSharedPreferences("My preferences", Context.MODE_PRIVATE);

        // Peso (para cálculo de calorias)
        String pesoStr = prefs.getString("peso", "70");
        try {
            pesoUsuario = Float.parseFloat(pesoStr);
        } catch (NumberFormatException e) {
            pesoUsuario = 70.0f;
        }

        // Tipo de Mapa e Navegação
        tipoMapaPref = prefs.getInt("mapa", R.id.radioButtonVetorial);
        tipoNavegacaoPref = prefs.getInt("navegacao", R.id.radioButtonNorth);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configura tipo de mapa conforme preferência salva
        if (tipoMapaPref == R.id.radioButtonSatelite) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        // Ativa botão de "ir para minha localização" se tiver permissão
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void iniciarTrilha() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_UPDATES);
            return;
        }

        // Reseta Variáveis
        tempWaypoints = new ArrayList<>();
        distanciaTotal = 0.0;
        velocidadeMaxima = 0.0;
        somaVelocidades = 0.0;
        contadorLocais = 0;
        lastLocation = null;
        dataInicioStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        // Limpa mapa anterior
        mMap.clear();
        PolylineOptions polyOptions = new PolylineOptions().width(10).color(Color.BLUE);
        rotaDesenhada = mMap.addPolyline(polyOptions);

        // UI Updates
        isRecording = true;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        cronometro.setBase(SystemClock.elapsedRealtime());
        cronometro.start();
        startTimeMillis = System.currentTimeMillis();

        iniciarLocationUpdates();
    }

    private void iniciarLocationUpdates() {
        long timeInterval = 3000; // 3 segundos
        mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, timeInterval)
                .setMinUpdateDistanceMeters(2) // Só atualiza se andar 2 metros
                .build();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (!isRecording) return;

                for (Location location : locationResult.getLocations()) {
                    processarNovaLocalizacao(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private void processarNovaLocalizacao(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // 1. Adiciona à lista temporária (memória)
        tempWaypoints.add(new Waypoint(location));

        // 2. Desenha no mapa (Linha)
        java.util.List<LatLng> points = rotaDesenhada.getPoints();
        points.add(latLng);
        rotaDesenhada.setPoints(points);

        // 3. Desenha acurácia (Círculo)
        if (circuloPrecisao != null) circuloPrecisao.remove();
        circuloPrecisao = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(location.getAccuracy())
                .strokeColor(Color.RED)
                .strokeWidth(2)
                .fillColor(0x22FF0000)); // Transparente

        // 4. Move Câmera (Respeitando navegação North Up ou Course Up)
        CameraPosition.Builder builder = new CameraPosition.Builder().target(latLng).zoom(18);

        // Se for Course Up, rotaciona o mapa na direção do movimento
        if (tipoNavegacaoPref == R.id.radioButtonCourse && location.hasBearing()) {
            builder.bearing(location.getBearing());
        } else {
            builder.bearing(0); // North Up (padrão)
        }
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));

        // 5. Cálculos (Distância e Velocidade)
        double velInstantanea = location.getSpeed() * 3.6; // m/s para km/h
        if (velInstantanea > velocidadeMaxima) {
            velocidadeMaxima = velInstantanea;
        }
        somaVelocidades += velInstantanea;
        contadorLocais++;

        if (lastLocation != null) {
            distanciaTotal += lastLocation.distanceTo(location); // Em metros
        }
        lastLocation = location;

        // 6. Cálculo Calorias (Fórmula MET simples)
        // MET médio caminhada = 3.5. Calorias = MET * peso * horas
        long tempoCorridoHoras = (SystemClock.elapsedRealtime() - cronometro.getBase()) / 3600000;
        // Simplificação: Cálculo instantâneo aproximado baseado na distância e peso
        // Fator: 0.75 caloria por kg por km (aproximação comum para caminhada)
        double caloriasQueimadas = (distanciaTotal / 1000.0) * pesoUsuario * 0.75;

        // Atualiza Textos
        tvVelocidade.setText(String.format("%.1f km/h", velInstantanea));
        tvVelMax.setText(String.format("Max: %.1f", velocidadeMaxima));
        tvDistancia.setText(String.format("%.2f km", (distanciaTotal / 1000.0))); // Mostra em KM
        tvCalorias.setText(String.format("%.0f kcal", caloriasQueimadas));
    }

    private void pararTrilhaESalvar() {
        isRecording = false;
        cronometro.stop();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        // Calcula dados finais
        String dataFimStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        long duracaoMs = System.currentTimeMillis() - startTimeMillis;
        double velMedia = (contadorLocais > 0) ? (somaVelocidades / contadorLocais) : 0.0;
        double caloriasFinais = (distanciaTotal / 1000.0) * pesoUsuario * 0.75;

        // 1. Salva a TRILHA (Cabeçalho) no banco e pega o ID
        long idTrilha = trilhasDB.salvarTrilha(
                "Trilha " + dataInicioStr, // Nome automático
                dataInicioStr,
                dataFimStr,
                distanciaTotal,
                duracaoMs,
                velMedia,
                velocidadeMaxima,
                caloriasFinais
        );

        // 2. Salva os WAYPOINTS vinculados a esse ID
        if (idTrilha != -1) {
            for (Waypoint wp : tempWaypoints) {
                trilhasDB.registrarWaypoint(idTrilha, wp);
            }
            Toast.makeText(this, "Trilha salva com sucesso!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Erro ao salvar trilha.", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}