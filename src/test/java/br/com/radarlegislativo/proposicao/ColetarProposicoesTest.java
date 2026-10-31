package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.exception.FonteIndisponivelException;
import br.com.radarlegislativo.exception.NotFoundException;
import br.com.radarlegislativo.fonte.Autor;
import br.com.radarlegislativo.fonte.FonteComColeta;
import br.com.radarlegislativo.fonte.FonteRegistry;
import br.com.radarlegislativo.fonte.Parlamentar;
import br.com.radarlegislativo.fonte.Proposicao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static br.com.radarlegislativo.TestFixtures.autorDeputado;
import static br.com.radarlegislativo.TestFixtures.deputado;
import static br.com.radarlegislativo.TestFixtures.props;
import static br.com.radarlegislativo.TestFixtures.proposicao;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColetarProposicoesTest {

    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), BRASILIA);
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 31);
    private static final Instant AGORA = RELOGIO.instant();

    @Mock
    private FonteRegistry registry;
    @Mock
    private ColetaRepository coletaRepository;
    @Mock
    private GravadorDeProposicoes gravador;
    @Mock
    private FonteComColeta camara;

    private ColetarProposicoes coletarProposicoes;

    @BeforeEach
    void setUp() {
        coletarProposicoes = new ColetarProposicoes(registry, coletaRepository, gravador, props("x"), RELOGIO);
        lenient().when(camara.codigo()).thenReturn("camara");
        lenient().when(registry.comColeta("camara")).thenReturn(camara);
        lenient().when(coletaRepository.save(any(Coleta.class))).then(returnsFirstArg());
    }

    private void semColetaAnterior() {
        when(coletaRepository.findFirstByFonteAndStatusOrderByAteDesc("camara", Coleta.Status.SUCESSO)).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("deve começar dias-iniciais para trás na primeira coleta da casa")
    void deveUsarDiasIniciaisNaPrimeiraColeta() {
        semColetaAnterior();
        when(camara.proposicoesApresentadasEntre(HOJE.minusDays(30), HOJE)).thenReturn(List.of());

        Coleta coleta = coletarProposicoes.execute("camara");

        assertThat(coleta.de()).isEqualTo(HOJE.minusDays(30));
        assertThat(coleta.ate()).isEqualTo(HOJE);
        assertThat(coleta.status()).isEqualTo(Coleta.Status.SUCESSO);
    }

    @Test
    @DisplayName("deve recomeçar um dia antes do fim da última coleta bem-sucedida")
    void deveSobreporUmDiaComAUltimaColeta() {
        Coleta ultima = new Coleta("camara", LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 29), AGORA);
        ultima.concluir(AGORA);
        when(coletaRepository.findFirstByFonteAndStatusOrderByAteDesc("camara", Coleta.Status.SUCESSO)).thenReturn(Optional.of(ultima));
        when(camara.proposicoesApresentadasEntre(LocalDate.of(2026, 10, 28), HOJE)).thenReturn(List.of());

        Coleta coleta = coletarProposicoes.execute("camara");

        assertThat(coleta.de()).isEqualTo(LocalDate.of(2026, 10, 28));
    }

    @Test
    @DisplayName("deve buscar autores e só os parlamentares desconhecidos para proposição nova")
    void deveGravarProposicaoNovaComAutores() {
        semColetaAnterior();
        Proposicao pl = proposicao("2648825", "PL", 5660, 2026);
        List<Autor> autores = List.of(autorDeputado("1", "Fulana"), autorDeputado("2", "Beltrano"),
                new Autor("Poder Executivo", "Órgão do Poder Executivo", null, true));
        Parlamentar beltrano = deputado("2", "Beltrano");
        when(camara.proposicoesApresentadasEntre(any(), any())).thenReturn(List.of(pl));
        when(gravador.atualizarSeExistir(pl, AGORA)).thenReturn(false);
        when(camara.autoresDa("2648825")).thenReturn(autores);
        when(gravador.parlamentaresDesconhecidos("camara", List.of("1", "2"))).thenReturn(Set.of("2"));
        when(camara.parlamentar("2")).thenReturn(beltrano);

        Coleta coleta = coletarProposicoes.execute("camara");

        verify(camara, never()).parlamentar("1");
        verify(gravador).registrarParlamentar(beltrano, AGORA);
        verify(gravador).criar(pl, autores, AGORA);
        assertThat(coleta.novas()).isEqualTo(1);
        assertThat(coleta.existentes()).isZero();
    }

    @Test
    @DisplayName("não deve pedir autores de proposição que já está no banco")
    void naoDevePedirAutoresDeProposicaoExistente() {
        semColetaAnterior();
        Proposicao pl = proposicao("2648825", "PL", 5660, 2026);
        when(camara.proposicoesApresentadasEntre(any(), any())).thenReturn(List.of(pl));
        when(gravador.atualizarSeExistir(pl, AGORA)).thenReturn(true);

        Coleta coleta = coletarProposicoes.execute("camara");

        verify(camara, never()).autoresDa(anyString());
        verify(gravador, never()).criar(any(), any(), any());
        assertThat(coleta.existentes()).isEqualTo(1);
    }

    @Test
    @DisplayName("deve registrar FALHA, guardando o que já gravou, quando a casa cai no meio")
    void deveRegistrarFalhaQuandoCasaCai() {
        semColetaAnterior();
        Proposicao pl1 = proposicao("1", "PL", 1, 2026);
        Proposicao pl2 = proposicao("2", "PL", 2, 2026);
        when(camara.proposicoesApresentadasEntre(any(), any())).thenReturn(List.of(pl1, pl2));
        when(gravador.atualizarSeExistir(any(), eq(AGORA))).thenReturn(false);
        when(camara.autoresDa("1")).thenReturn(List.of());
        when(camara.autoresDa("2")).thenThrow(new FonteIndisponivelException("camara", "timeout", null));

        Coleta coleta = coletarProposicoes.execute("camara");

        assertThat(coleta.status()).isEqualTo(Coleta.Status.FALHA);
        assertThat(coleta.erro()).isEqualTo("timeout");
        assertThat(coleta.novas()).isEqualTo(1);
        verify(gravador).criar(pl1, List.of(), AGORA);
    }

    @Test
    @DisplayName("deve lançar NotFound para casa sem coleta")
    void deveLancarNotFoundParaCasaSemColeta() {
        when(registry.comColeta("senado")).thenThrow(new NotFoundException());

        assertThatThrownBy(() -> coletarProposicoes.execute("senado")).isInstanceOf(NotFoundException.class);
        verify(coletaRepository, never()).save(any());
    }
}
