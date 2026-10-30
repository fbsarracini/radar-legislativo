package br.com.radarlegislativo.exception;

/** A API pública da casa falhou ou devolveu algo inesperado. Vira HTTP 502. */
public class FonteIndisponivelException extends RuntimeException {

    private final String fonte;

    public FonteIndisponivelException(String fonte, String motivo, Throwable causa) {
        super(motivo, causa);
        this.fonte = fonte;
    }

    public String fonte() {
        return fonte;
    }
}
