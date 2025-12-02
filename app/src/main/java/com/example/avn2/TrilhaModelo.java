package com.example.avn2;
//Classe para segurar os dados que são tirados do banco 3 mostrar na lista
public class TrilhaModelo {
        long id;
        String nome;
        String dataInicio;
        double distancia;
        long tempoDuracao;

        public TrilhaModelo(long id, String nome, String dataInicio, double distancia, long tempoDuracao) {
            this.id = id;
            this.nome = nome;
            this.dataInicio = dataInicio;
            this.distancia = distancia;
            this.tempoDuracao = tempoDuracao;
        }
        // Getters
        public long getId() { return id; }
        public String getNome() { return nome; }
        public String getDataInicio() {
            return dataInicio;
        }
        public double getDistancia() {
            return distancia;
        }
        public long getTempoDuracao() {
            return tempoDuracao;
        }
        public void setNome(String nome) {
            this.nome = nome;
        }

    }