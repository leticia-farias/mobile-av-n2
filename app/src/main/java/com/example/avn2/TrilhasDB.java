package com.example.avn2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class TrilhasDB extends SQLiteOpenHelper {
    // Aumentei a versão para 2 para forçar a atualização da estrutura
    private static final int VERSION = 2;
    private static final String DATABASE = "trilha_database";

    // Nomes das tabelas
    private static final String TABLE_TRILHAS = "trilhas";
    private static final String TABLE_WAYPOINTS = "waypoints";

    public TrilhasDB(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Tabela para os dados gerais da trilha (Cabeçalho)
        // Requisito: nome, data/hora, gasto calórico, vel média, vel máxima [cite: 27]
        String create_trilhas = "CREATE TABLE " + TABLE_TRILHAS + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "nome TEXT, " +
                "data_inicio TEXT, " +
                "data_fim TEXT, " +
                "distancia_total REAL, " +
                "tempo_duracao LONG, " +
                "velocidade_media REAL, " +
                "velocidade_maxima REAL, " +
                "gasto_calorico REAL" +
                ");";
        db.execSQL(create_trilhas);

        // 2. Tabela para os pontos (Waypoints)
        // Adicionei 'id_trilha' para vincular este ponto à trilha correta
        String create_waypoints = "CREATE TABLE " + TABLE_WAYPOINTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "id_trilha INTEGER, " +
                "latitude NUMERIC NOT NULL, " +
                "longitude NUMERIC NOT NULL, " +
                "altitude NUMERIC NOT NULL, " +
                "FOREIGN KEY(id_trilha) REFERENCES " + TABLE_TRILHAS + "(_id)" +
                ");";
        db.execSQL(create_waypoints);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Se a versão mudar, apaga tudo e cria de novo (simples para desenvolvimento)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WAYPOINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRILHAS);
        onCreate(db);
    }

    //  TABELA DE TRILHAS

    // Salva o resumo da trilha ao finalizar
    public long salvarTrilha(String nome, String dataInicio, String dataFim, double distancia,
                             long tempo, double velMedia, double velMax, double calorias) {
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("data_inicio", dataInicio);
        values.put("data_fim", dataFim);
        values.put("distancia_total", distancia);
        values.put("tempo_duracao", tempo);
        values.put("velocidade_media", velMedia);
        values.put("velocidade_maxima", velMax);
        values.put("gasto_calorico", calorias);

        return getWritableDatabase().insert(TABLE_TRILHAS, null, values);
    }

    // Retorna todas as trilhas para a tela de Consultar
    public Cursor buscarTodasTrilhas() {
        return getReadableDatabase().query(TABLE_TRILHAS, null, null, null, null, null, "_id DESC");
    }

    //   TABELA DE WAYPOINTS

    // Agora precisa receber o ID da trilha a qual esse ponto pertence
    public void registrarWaypoint(long idTrilha, Waypoint waypoint) {
        ContentValues values = new ContentValues();
        values.put("id_trilha", idTrilha);
        values.put("latitude", waypoint.getLatitude());
        values.put("longitude", waypoint.getLongitude());
        values.put("altitude", waypoint.getAltitude());
        getWritableDatabase().insert(TABLE_WAYPOINTS, null, values);
    }

    // Recupera os pontos de uma trilha específica (usado para desenhar no mapa depois)
    public ArrayList<Waypoint> recuperarWaypointsDaTrilha(long idTrilha) {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        String selection = "id_trilha = ?";
        String[] selectionArgs = {String.valueOf(idTrilha)};

        try (Cursor cursor = getReadableDatabase().query(TABLE_WAYPOINTS, null, selection, selectionArgs, null, null, "id ASC")) {
            while (cursor.moveToNext()) {
                Waypoint waypoint = new Waypoint();
                // Nota: O índice das colunas pode mudar, é mais seguro buscar pelo nome ou garantir a ordem
                waypoint.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                waypoint.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
                waypoint.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
                waypoint.setAltitude(cursor.getDouble(cursor.getColumnIndexOrThrow("altitude")));
                waypoints.add(waypoint);
            }
        }
        return waypoints;
    }

    //* Método auxiliar para limpar tudo (útil para testes)
    public void apagarTudo() {
        getWritableDatabase().execSQL("DELETE FROM " + TABLE_WAYPOINTS);
        getWritableDatabase().execSQL("DELETE FROM " + TABLE_TRILHAS);
    }
    public void excluirTrilha(long idTrilha) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("waypoints", "id_trilha = ?", new String[]{String.valueOf(idTrilha)});
        db.delete("trilhas", "_id = ?", new String[]{String.valueOf(idTrilha)});
    }
    // Primeiro remove os waypoints vinculados para não ficar lixo no banco
    // Depois remove a trilha

    public void renomearTrilha(long idTrilha, String novoNome) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", novoNome);
        db.update("trilhas", values, "_id = ?", new String[]{String.valueOf(idTrilha)});
    }

    public void excluirTodasTrilhas() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("waypoints", null, null);
        db.delete("trilhas", null, null);
    }

    public Cursor buscarTrilhaPorId(long idTrilha) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query("trilhas", null, "_id = ?", new String[]{String.valueOf(idTrilha)}, null, null, null);
    }
}