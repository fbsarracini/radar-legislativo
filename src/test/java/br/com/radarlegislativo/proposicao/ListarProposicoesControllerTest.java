package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.config.RadarProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static br.com.radarlegislativo.TestFixtures.props;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListarProposicoesController.class)
@Import(ListarProposicoesControllerTest.Config.class)
class ListarProposicoesControllerTest {

    @TestConfiguration
    static class Config {
        /** O CorsConfig precisa das propriedades. */
        @Bean
        RadarProperties radarProperties() {
            return props("x");
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListarProposicoes listarProposicoes;

    @Test
    @DisplayName("deve converter os parâmetros do feed no filtro")
    void deveConverterParametros() throws Exception {
        when(listarProposicoes.execute(any(), any())).thenAnswer(inv -> FeedDeProposicoes.vazio(inv.getArgument(1)));

        mvc.perform(get("/api/proposicoes")
                        .param("desde", "2026-10-01").param("ate", "2026-10-31")
                        .param("novasDesde", "2026-10-30T18:00:00-03:00")
                        .param("tipo", "PL,PEC").param("uf", "SP").param("partido", "PT")
                        .param("autor", "rosario").param("parlamentar", "7", "8").param("pagina", "1").param("tamanho", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        ArgumentCaptor<FiltroProposicoes> filtro = ArgumentCaptor.forClass(FiltroProposicoes.class);
        ArgumentCaptor<Pageable> pagina = ArgumentCaptor.forClass(Pageable.class);
        verify(listarProposicoes).execute(filtro.capture(), pagina.capture());
        assertThat(filtro.getValue()).isEqualTo(new FiltroProposicoes(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                Instant.parse("2026-10-30T21:00:00Z"), null, List.of("PL", "PEC"), "SP", "PT", "rosario", List.of(7L, 8L)));
        assertThat(pagina.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pagina.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("deve responder 400 para data ou UF inválida e 422 para período invertido")
    void deveValidarParametros() throws Exception {
        mvc.perform(get("/api/proposicoes").param("desde", "31/10/2026")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/proposicoes").param("uf", "SPX")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/proposicoes").param("desde", "2026-10-31").param("ate", "2026-10-01"))
                .andExpect(status().isUnprocessableEntity());
        verifyNoInteractions(listarProposicoes);
    }
}
