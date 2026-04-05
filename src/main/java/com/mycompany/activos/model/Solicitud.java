package com.mycompany.activos.model;

public class Solicitud {

    private String fechaSolicitud;
    private String clienteSolicitante;
    private String cargo;
    private String solicitudes;
    private String reclutados;
    private String validacion;
    private String citacion;
    private String asistencia;
    private String filtroAprovados;
    private String seleccionados;
    private String estado;      // col K
    private String supervisor;  // col L

    public Solicitud(String fechaSolicitud, String clienteSolicitante, String cargo,
                     String solicitudes, String reclutados, String validacion,
                     String citacion, String asistencia, String filtroAprovados,
                     String seleccionados, String estado, String supervisor) {
        this.fechaSolicitud     = fechaSolicitud;
        this.clienteSolicitante = clienteSolicitante;
        this.cargo              = cargo;
        this.solicitudes        = solicitudes;
        this.reclutados         = reclutados;
        this.validacion         = validacion;
        this.citacion           = citacion;
        this.asistencia         = asistencia;
        this.filtroAprovados    = filtroAprovados;
        this.seleccionados      = seleccionados;
        this.estado             = estado;
        this.supervisor         = supervisor;
    }

    public String getFechaSolicitud()     { return fechaSolicitud; }
    public String getClienteSolicitante() { return clienteSolicitante; }
    public String getCargo()              { return cargo; }
    public String getSolicitudes()        { return solicitudes; }
    public String getReclutados()         { return reclutados; }
    public String getValidacion()         { return validacion; }
    public String getCitacion()           { return citacion; }
    public String getAsistencia()         { return asistencia; }
    public String getFiltroAprovados()    { return filtroAprovados; }
    public String getSeleccionados()      { return seleccionados; }
    public String getEstado()             { return estado; }
    public String getSupervisor()         { return supervisor; }

    public void setFechaSolicitud(String v)     { this.fechaSolicitud = v; }
    public void setClienteSolicitante(String v) { this.clienteSolicitante = v; }
    public void setCargo(String v)              { this.cargo = v; }
    public void setSolicitudes(String v)        { this.solicitudes = v; }
    public void setReclutados(String v)         { this.reclutados = v; }
    public void setValidacion(String v)         { this.validacion = v; }
    public void setCitacion(String v)           { this.citacion = v; }
    public void setAsistencia(String v)         { this.asistencia = v; }
    public void setFiltroAprovados(String v)    { this.filtroAprovados = v; }
    public void setSeleccionados(String v)      { this.seleccionados = v; }
    public void setEstado(String v)             { this.estado = v; }
    public void setSupervisor(String v)         { this.supervisor = v; }
}
