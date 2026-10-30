package br.com.radarlegislativo.fonte;

import br.com.radarlegislativo.config.RadarProperties;
import br.com.radarlegislativo.exception.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Todas as casas legislativas disponíveis, indexadas pelo código usado nas URLs. */
@Component
public class FonteRegistry {

    private final Map<String, FonteLegislativa> fontes = new LinkedHashMap<>();

    public FonteRegistry(CamaraClient camara, SenadoClient senado,
                         RestClient.Builder radarRestClientBuilder, RadarProperties props) {
        registrar(camara);
        registrar(senado);
        props.sapl().forEach(casa -> registrar(new SaplClient(radarRestClientBuilder, casa)));
    }

    private void registrar(FonteLegislativa f) {
        fontes.put(f.codigo(), f);
    }

    public List<FonteLegislativa> todas() {
        return List.copyOf(fontes.values());
    }

    public FonteLegislativa porCodigo(String codigo) {
        return Optional.ofNullable(fontes.get(codigo))
                .orElseThrow(NotFoundException::new);
    }

    /** Casas onde um eleito daquela esfera/UF/município exerceria o mandato. */
    public List<FonteLegislativa> paraMandato(Esfera esfera, String uf, String municipio) {
        return fontes.values().stream()
                .filter(f -> f.esfera() == esfera)
                .filter(f -> f.uf() == null || uf == null || f.uf().equalsIgnoreCase(uf))
                .filter(f -> esfera != Esfera.CAMARA_MUNICIPAL || f.municipio() == null || municipio == null
                        || Nomes.normalizar(f.municipio()).equals(Nomes.normalizar(municipio)))
                .toList();
    }
}
