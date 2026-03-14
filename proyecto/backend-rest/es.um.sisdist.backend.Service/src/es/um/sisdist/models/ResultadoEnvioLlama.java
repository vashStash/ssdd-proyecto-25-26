package es.um.sisdist.models;

public class ResultadoEnvioLlama {

    private String estado;
    private String respuesta;

    public ResultadoEnvioLlama() {}

    public ResultadoEnvioLlama(String estado, String respuesta) {
        this.estado = estado;
        this.respuesta = respuesta;
    }

    // Getters y Setters
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String ticket) { this.respuesta = ticket; }
    
}
