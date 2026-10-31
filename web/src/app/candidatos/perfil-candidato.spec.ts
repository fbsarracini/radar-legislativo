import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { umaPagina } from '../../testing/fixtures';
import { PerfilDoCandidato, Vinculo } from '../core/api.models';
import { PerfilCandidato } from './perfil-candidato';

const SQ = '250001234567';

function umVinculo(parcial: Partial<Vinculo> = {}): Vinculo {
  return {
    id: 7,
    parlamentar: {
      id: 10,
      fonte: 'camara',
      idExterno: '74398',
      nome: 'Maria do Rosário',
      partido: 'PT',
      uf: 'RS',
      fotoUrl: null,
    },
    confianca: 'NOME_URNA_EXATO',
    status: 'SUGERIDO',
    decididoEm: null,
    ...parcial,
  };
}

function umPerfil(vinculos: Vinculo[]): PerfilDoCandidato {
  return {
    candidato: {
      sqCandidato: SQ,
      anoEleicao: 2026,
      nome: 'MARIA DO ROSARIO NUNES',
      nomeUrna: 'MARIA DO ROSÁRIO',
      cargo: 'DEPUTADO FEDERAL',
      esfera: 'CAMARA_FEDERAL',
      uf: 'RS',
      municipio: null,
      partido: 'PT',
      numero: '1313',
      situacao: 'ELEITO POR QP',
      eleito: true,
    },
    vinculos,
    proposicoes: [],
    totalProposicoes: 0,
  };
}

describe('PerfilCandidato', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter(
          [{ path: 'candidatos/:sq', component: PerfilCandidato }],
          withComponentInputBinding(),
        ),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function abrir(perfil: PerfilDoCandidato): Promise<RouterTestingHarness> {
    const harness = await RouterTestingHarness.create(`/candidatos/${SQ}`);
    TestBed.tick();
    http.expectOne(`/api/candidatos/${SQ}`).flush(perfil);
    http.expectOne((r) => r.url === `/api/candidatos/${SQ}/proposicoes`).flush(umaPagina([]));
    await harness.fixture.whenStable();
    return harness;
  }

  function botao(harness: RouterTestingHarness, texto: string): HTMLButtonElement | undefined {
    return [...harness.routeNativeElement!.querySelectorAll('button')].find((b) =>
      b.textContent?.includes(texto),
    );
  }

  it('deve confirmar o vínculo e recarregar o perfil e os projetos', async () => {
    const harness = await abrir(umPerfil([umVinculo()]));
    expect(harness.routeNativeElement!.querySelector('h1')?.textContent).toContain(
      'MARIA DO ROSÁRIO',
    );
    expect(harness.routeNativeElement!.textContent).toContain('vêm das sugestões');

    botao(harness, 'É esta pessoa')!.click();
    const put = http.expectOne(`/api/candidatos/${SQ}/vinculos/7/confirmacao`);
    expect(put.request.method).toBe('PUT');
    expect(put.request.body).toEqual({ confirmado: true });
    put.flush(umVinculo({ status: 'CONFIRMADO' }));
    TestBed.tick();

    http.expectOne(`/api/candidatos/${SQ}`).flush(umPerfil([umVinculo({ status: 'CONFIRMADO' })]));
    http.expectOne((r) => r.url === `/api/candidatos/${SQ}/proposicoes`).flush(umaPagina([]));
    await harness.fixture.whenStable();
    expect(botao(harness, 'É esta pessoa')).toBeUndefined();
    expect(botao(harness, 'Homônimo')).toBeDefined();
    expect(harness.routeNativeElement!.textContent).not.toContain('vêm das sugestões');
  });

  it('deve deixar desfazer a rejeição, sem oferecer rejeitar de novo', async () => {
    const harness = await abrir(umPerfil([umVinculo({ status: 'REJEITADO' })]));
    expect(botao(harness, 'É esta pessoa')).toBeDefined();
    expect(botao(harness, 'Homônimo')).toBeUndefined();
  });

  it('deve explicar quando não há parlamentar ligado', async () => {
    const harness = await abrir(umPerfil([]));
    expect(harness.routeNativeElement!.textContent).toContain('Nenhum parlamentar coletado');
  });
});
