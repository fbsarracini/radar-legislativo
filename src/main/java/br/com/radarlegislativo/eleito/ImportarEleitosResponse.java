package br.com.radarlegislativo.eleito;

public record ImportarEleitosResponse(int ano, int eleitosImportados) {

    public static ImportarEleitosResponse of(int ano, int eleitosImportados) {
        return new ImportarEleitosResponse(ano, eleitosImportados);
    }
}
