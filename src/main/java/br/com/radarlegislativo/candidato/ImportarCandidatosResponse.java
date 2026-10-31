package br.com.radarlegislativo.candidato;

public record ImportarCandidatosResponse(int ano, int candidatosImportados) {

    public static ImportarCandidatosResponse of(int ano, int candidatosImportados) {
        return new ImportarCandidatosResponse(ano, candidatosImportados);
    }
}
