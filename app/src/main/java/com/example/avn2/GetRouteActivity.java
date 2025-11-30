package com.example.avn2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;

public class GetRouteActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_UPDATES = 1;

    // Objetos da API de localização
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    // Banco de dados
    TrilhasDB trilhadb;

    int waypoint_counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_route);

        Button startButton = findViewById(R.id.StartGetRouteButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // instancia/abre o banco de dados
                trilhadb = new TrilhasDB(GetRouteActivity.this);
                trilhadb.apagaTrilha();
                startLocationUpdates();
                waypoint_counter = 0;
            }
        });

        Button stopButton = findViewById(R.id.StopGetRouteButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationUpdates();
                trilhadb.close();
            }
        });
    }

    private void startLocationUpdates() {
        // Se a app já possui a permissão, ativa a chamada de localização
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            // A permissão foi dada– OK vá em frente
            // Cria o cliente (FusedLocationProviderClient)
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            // Configura a solicitação de localizações (LocationRequest)
            long timeInterval = 5 * 1000;
            mLocationRequest = new com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, timeInterval).build();

            // Programa o escutador para consumir as novas localizações geradas (LocationCallback)
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    // Processa a localização aqui
                    addWayPoint(location);
                }
            };

            // Manda o cliente começar a gerar atualizações de localização.
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        } else {
            // Solicite a permissão
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_UPDATES);
        }
    }

    public void addWayPoint(Location location) {
        Waypoint waypoint = new Waypoint(location);
        trilhadb.registrarWaypoint(waypoint);
        TextView logTextView = findViewById(R.id.logTextView);
        waypoint_counter++;
        String log = "Adicionado("+waypoint_counter+"):"+waypoint.getLatitude()+","+waypoint.getLongitude();
        logTextView.setText(log);
    }

    public void stopLocationUpdates() {
        if (mFusedLocationProviderClient != null)
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

        TextView logTextView = findViewById(R.id.logTextView);
        ArrayList<Waypoint> waypoints = trilhadb.recuperarWaypoints();
        String log = "";
        for (int i = 0; i < waypoints.size(); i++) {
            log += "(" + (i+1) + ")" + waypoints.get(i).getLatitude() + "," + waypoints.get(i).getLongitude() + "\n";
        }
        logTextView.setText(log);
    }
}