package com.example.avn2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class TrilhaAdapter extends RecyclerView.Adapter<TrilhaAdapter.TrilhaViewHolder> {

    private Context context;
    private List<TrilhaModelo> listaTrilhas;
    private TrilhasDB db;

    public TrilhaAdapter(Context context, List<TrilhaModelo> listaTrilhas) {
        this.context = context;
        this.listaTrilhas = listaTrilhas;
        this.db = new TrilhasDB(context);
    }

    @NonNull
    @Override
    public TrilhaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trilha, parent, false);
        return new TrilhaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrilhaViewHolder holder, int position) {
        TrilhaModelo trilha = listaTrilhas.get(position);

        holder.tvTitulo.setText(trilha.getNome());
        holder.tvData.setText(trilha.getDataInicio());
        holder.tvDistancia.setText(String.format("%.2f km", trilha.getDistancia() / 1000));

        holder.btnVisualizar.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("ID_TRILHA", trilha.getId()); // Passa o ID para o mapa carregar
            context.startActivity(intent);
        });

        holder.btnEditar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Renomear Trilha");
            final EditText input = new EditText(context);
            input.setText(trilha.getNome());
            builder.setView(input);

            builder.setPositiveButton("Salvar", (dialog, which) -> {
                String novoNome = input.getText().toString();
                db.renomearTrilha(trilha.getId(), novoNome);
                trilha.setNome(novoNome); // Atualiza lista local
                notifyItemChanged(position); // Atualiza tela
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });

        // Parte de compartilhar
        holder.btnCompartilhar.setOnClickListener(v -> {
            Cursor cursor = db.buscarTrilhaPorId(trilha.getId());
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    // 1. Cria o objeto JSON com os dados da Trilha
                    JSONObject json = new JSONObject();
                    json.put("nome", cursor.getString(cursor.getColumnIndexOrThrow("nome")));
                    json.put("data_inicio", cursor.getString(cursor.getColumnIndexOrThrow("data_inicio")));
                    json.put("data_fim", cursor.getString(cursor.getColumnIndexOrThrow("data_fim")));
                    json.put("distancia_total", cursor.getDouble(cursor.getColumnIndexOrThrow("distancia_total")));
                    json.put("tempo_duracao", cursor.getLong(cursor.getColumnIndexOrThrow("tempo_duracao")));
                    json.put("velocidade_media", cursor.getDouble(cursor.getColumnIndexOrThrow("velocidade_media")));
                    json.put("velocidade_maxima", cursor.getDouble(cursor.getColumnIndexOrThrow("velocidade_maxima")));
                    json.put("gasto_calorico", cursor.getDouble(cursor.getColumnIndexOrThrow("gasto_calorico")));

                    // 2. Recupera e adiciona os Waypoints (coordenadas)
                    List<Waypoint> waypoints = db.recuperarWaypointsDaTrilha(trilha.getId());
                    JSONArray jsonWaypoints = new JSONArray();
                    for (Waypoint wp : waypoints) {
                        JSONObject jsonWp = new JSONObject();
                        jsonWp.put("lat", wp.getLatitude());
                        jsonWp.put("lng", wp.getLongitude());
                        jsonWp.put("alt", wp.getAltitude());
                        jsonWaypoints.put(jsonWp);
                    }
                    json.put("caminho", jsonWaypoints);

                    // 3. Envia o texto formatado como JSON
                    String textoShare = json.toString(2); // Indentação de 2 espaços para ficar legível

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, textoShare);
                    sendIntent.setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Trilha (JSON)"));

                } catch (Exception e) {
                    Toast.makeText(context, "Erro ao gerar dados para compartilhamento.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        });

        holder.btnExcluir.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Excluir")
                    .setMessage("Deseja apagar esta trilha permanentemente?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        db.excluirTrilha(trilha.getId());
                        listaTrilhas.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, listaTrilhas.size());
                        Toast.makeText(context, "Trilha apagada", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listaTrilhas.size();
    }

    public static class TrilhaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvData, tvDistancia;
        Button btnVisualizar, btnEditar, btnCompartilhar, btnExcluir;

        public TrilhaViewHolder(@NonNull View itemView) {
            super(itemView);
            // IDs baseados no XML item_trilha.xml que você mandou
            tvTitulo = itemView.findViewById(R.id.tvTituloTrilha);
            tvData = itemView.findViewById(R.id.tvDataTrilha);
            tvDistancia = itemView.findViewById(R.id.tvDistanciaTrilha);
            btnVisualizar = itemView.findViewById(R.id.btnVisualizar);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnCompartilhar = itemView.findViewById(R.id.btnCompartilhar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}