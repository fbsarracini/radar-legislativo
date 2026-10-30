package br.com.radarlegislativo.fonte;

import br.com.radarlegislativo.exception.FonteIndisponivelException;
import br.com.radarlegislativo.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static br.com.radarlegislativo.TestFixtures.deputado;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarParlamentaresTest {

    @Mock
    private FonteRegistry registry;

    @InjectMocks
    private BuscarParlamentares buscarParlamentares;

    @Test
    @DisplayName("deve buscar só na fonte informada")
    void deveBuscarSoNaFonteInformada() {
        FonteLegislativa camara = mock(FonteLegislativa.class);
        when(registry.porCodigo("camara")).thenReturn(camara);
        when(camara.buscarParlamentares("silva", "SP")).thenReturn(List.of(deputado("1", "João Silva")));

        List<Parlamentar> result = buscarParlamentares.execute("silva", "SP", "camara");

        assertThat(result).extracting(Parlamentar::id).containsExactly("1");
    }

    @Test
    @DisplayName("deve ignorar fonte fora do ar quando busca em todas")
    void deveIgnorarFonteForaDoAr() {
        FonteLegislativa camara = mock(FonteLegislativa.class);
        FonteLegislativa senado = mock(FonteLegislativa.class);
        when(registry.todas()).thenReturn(List.of(camara, senado));
        when(camara.buscarParlamentares("silva", null)).thenReturn(List.of(deputado("1", "João Silva")));
        when(senado.buscarParlamentares("silva", null)).thenThrow(new FonteIndisponivelException("senado", "timeout", null));
        when(senado.codigo()).thenReturn("senado");

        List<Parlamentar> result = buscarParlamentares.execute("silva", null, null);

        assertThat(result).extracting(Parlamentar::id).containsExactly("1");
    }

    @Test
    @DisplayName("deve lançar NotFound para fonte desconhecida")
    void deveLancarNotFoundParaFonteDesconhecida() {
        when(registry.porCodigo("xpto")).thenThrow(new NotFoundException());

        assertThatThrownBy(() -> buscarParlamentares.execute("silva", null, "xpto"))
                .isInstanceOf(NotFoundException.class);
    }
}
