package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.exception.FonteIndisponivelException;
import br.com.radarlegislativo.exception.NotFoundException;
import br.com.radarlegislativo.fonte.BuscarParlamentares;
import br.com.radarlegislativo.fonte.Esfera;
import br.com.radarlegislativo.fonte.FonteLegislativa;
import br.com.radarlegislativo.fonte.FonteRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static br.com.radarlegislativo.TestFixtures.deputado;
import static br.com.radarlegislativo.TestFixtures.deputadoFederal;
import static br.com.radarlegislativo.TestFixtures.eleito;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarVinculosDoCandidatoTest {

    @Mock
    private CandidatoRepository candidatoRepository;
    @Mock
    private FonteRegistry registry;
    @Mock
    private BuscarParlamentares buscarParlamentares;

    @InjectMocks
    private BuscarVinculosDoCandidato buscarVinculosDoCandidato;

    @Test
    @DisplayName("deve classificar a confiança do vínculo pelo nome")
    void deveClassificarConfianca() {
        Candidato e = deputadoFederal("1", "JOÃO PEREIRA DA SILVA", "JOÃO SILVA");

        assertThat(BuscarVinculosDoCandidato.classificar(e, deputado("10", "João Silva"))).contains(Vinculo.Confianca.NOME_URNA_EXATO);
        assertThat(BuscarVinculosDoCandidato.classificar(e, deputado("10", "João Pereira da Silva"))).contains(Vinculo.Confianca.NOME_CIVIL_EXATO);
        assertThat(BuscarVinculosDoCandidato.classificar(e, deputado("10", "Pereira Silva"))).contains(Vinculo.Confianca.NOME_PARCIAL);
        assertThat(BuscarVinculosDoCandidato.classificar(e, deputado("10", "Maria Santos"))).isEmpty();
    }

    @Test
    @DisplayName("deve usar a palavra mais longa do nome de urna como termo de busca")
    void deveEscolherTermoDeBusca() {
        assertThat(BuscarVinculosDoCandidato.termoDeBusca(deputadoFederal("1", "JOÃO PEREIRA DA SILVA", "JOÃO SILVA")))
                .isEqualTo("SILVA");
    }

    @Test
    @DisplayName("deve devolver vínculos ordenados por confiança")
    void deveDevolverVinculosOrdenados() {
        Candidato e = deputadoFederal("1", "JOÃO PEREIRA DA SILVA", "JOÃO SILVA");
        FonteLegislativa camara = mock(FonteLegislativa.class);
        when(camara.codigo()).thenReturn("camara");
        when(candidatoRepository.findById("1")).thenReturn(Optional.of(e));
        when(registry.paraMandato(Esfera.CAMARA_FEDERAL, "SP", "SÃO PAULO")).thenReturn(List.of(camara));
        when(buscarParlamentares.execute("SILVA", "SP", "camara")).thenReturn(List.of(
                deputado("20", "Pereira Silva"),
                deputado("10", "João Silva"),
                deputado("30", "Maria Silva")));

        List<Vinculo> result = buscarVinculosDoCandidato.execute("1");

        assertThat(result).extracting(v -> v.parlamentar().id()).containsExactly("10", "20");
        assertThat(Vinculo.maisConfiaveis(result)).extracting(v -> v.parlamentar().id()).containsExactly("10");
    }

    @Test
    @DisplayName("deve ignorar casa fora do ar")
    void deveIgnorarCasaForaDoAr() {
        Candidato e = deputadoFederal("1", "JOÃO PEREIRA DA SILVA", "JOÃO SILVA");
        FonteLegislativa camara = mock(FonteLegislativa.class);
        when(camara.codigo()).thenReturn("camara");
        when(candidatoRepository.findById("1")).thenReturn(Optional.of(e));
        when(registry.paraMandato(Esfera.CAMARA_FEDERAL, "SP", "SÃO PAULO")).thenReturn(List.of(camara));
        when(buscarParlamentares.execute("SILVA", "SP", "camara"))
                .thenThrow(new FonteIndisponivelException("camara", "timeout", null));

        assertThat(buscarVinculosDoCandidato.execute("1")).isEmpty();
    }

    @Test
    @DisplayName("deve devolver lista vazia para cargo executivo sem consultar as casas")
    void deveDevolverVazioParaExecutivo() {
        when(candidatoRepository.findById("1")).thenReturn(Optional.of(eleito("1", 2026, "GOVERNADOR", "MG", "PEDRINHO")));

        assertThat(buscarVinculosDoCandidato.execute("1")).isEmpty();
        verifyNoInteractions(registry, buscarParlamentares);
    }

    @Test
    @DisplayName("deve lançar NotFound quando o candidato não foi importado")
    void deveLancarNotFoundQuandoCandidatoNaoExiste() {
        when(candidatoRepository.findById("99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buscarVinculosDoCandidato.execute("99"))
                .isInstanceOf(NotFoundException.class);
    }
}
