package br.com.radarlegislativo.fonte;

public record FonteResponse(String codigo, String casa, Esfera esfera, String nivel, String uf, String municipio) {

    public static FonteResponse of(FonteLegislativa fonte) {
        return new FonteResponse(
                fonte.codigo(),
                fonte.nomeCasa(),
                fonte.esfera(),
                fonte.esfera().nivel(),
                fonte.uf(),
                fonte.municipio()
        );
    }
}
