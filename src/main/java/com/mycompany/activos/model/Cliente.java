package com.mycompany.activos.model;

public class Cliente {

    private String cliente;
    private String rut;

    public Cliente() {}

    public Cliente(String cliente, String rut) {
        this.cliente = cliente;
        this.rut = rut;
    }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    @Override
    public String toString() {
        return "Cliente{cliente='" + cliente + "', rut='" + rut + "'}";
    }
}
