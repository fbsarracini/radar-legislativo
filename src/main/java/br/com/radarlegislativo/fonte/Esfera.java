package br.com.radarlegislativo.fonte;

/** Esfera / casa legislativa onde o projeto foi protocolado. */
public enum Esfera {
    CAMARA_FEDERAL("Federal"),
    SENADO_FEDERAL("Federal"),
    ASSEMBLEIA_ESTADUAL("Estadual"),
    CAMARA_MUNICIPAL("Municipal");

    private final String nivel;

    Esfera(String nivel) {
        this.nivel = nivel;
    }

    public String nivel() {
        return nivel;
    }
}
