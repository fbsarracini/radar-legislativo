import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { umaPagina, umaProposicao } from '../../testing/fixtures';
import { UltimaVisita } from '../core/ultima-visita';
import { Feed } from './feed';

describe('Feed', () => {
  let http: HttpTestingController;
  const visita = new Date('2026-10-30T12:00:00Z');

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([{ path: 'proposicoes', component: Feed }]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: UltimaVisita, useValue: { anterior: visita } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function abrir(url: string): Promise<RouterTestingHarness> {
    const harness = await RouterTestingHarness.create(url);
    TestBed.tick();
    http.expectOne('/api/fontes').flush([]);
    return harness;
  }

  it('deve levar os filtros da URL para a API', async () => {
    const harness = await abrir('/proposicoes?tipo=PL&tipo=PEC&uf=SP&partido=PT&pagina=2&lixo=1');
    const req = http.expectOne((r) => r.url === '/api/proposicoes');
    expect(req.request.params.getAll('tipo')).toEqual(['PL', 'PEC']);
    expect(req.request.params.get('uf')).toBe('SP');
    expect(req.request.params.get('partido')).toBe('PT');
    expect(req.request.params.get('pagina')).toBe('2');
    expect(req.request.params.has('lixo')).toBe(false);
    expect(req.request.params.has('novasDesde')).toBe(false);
    req.flush(umaPagina([umaProposicao()], 41));
    await harness.fixture.whenStable();

    const el = harness.routeNativeElement!;
    expect(el.querySelector('.total')?.textContent).toContain('41 proposições');
    expect(el.querySelectorAll('app-proposicao-card').length).toBe(1);
    expect(el.querySelector('.nova')).not.toBeNull();
  });

  it('deve pedir só as novas desde a última visita quando marcado', async () => {
    await abrir('/proposicoes?novas=true');
    const req = http.expectOne((r) => r.url === '/api/proposicoes');
    expect(req.request.params.get('novasDesde')).toBe(visita.toISOString());
    req.flush(umaPagina([]));
  });

  it('deve pôr o formulário na URL, sem os vazios, voltando para a primeira página', async () => {
    const harness = await abrir('/proposicoes?pagina=3');
    http.expectOne((r) => r.url === '/api/proposicoes').flush(umaPagina([]));
    await harness.fixture.whenStable();

    const feed = harness.routeDebugElement!.componentInstance as Feed;
    const filtros = (feed as unknown as { filtros: Feed['filtros'] }).filtros;
    filtros.patchValue({ uf: 'RS', tipo: ['PL'], autor: '  rosario ' });
    (harness.routeNativeElement!.querySelector('form') as HTMLFormElement).dispatchEvent(
      new Event('submit'),
    );

    // whenStable esperaria a resposta; aqui a navegação gera um request novo, que respondemos.
    const req = await vi.waitFor(() => http.expectOne((r) => r.url === '/api/proposicoes'));
    expect(TestBed.inject(Router).url).toBe('/proposicoes?tipo=PL&uf=RS&autor=rosario');
    expect(req.request.params.get('pagina')).toBe('0');
    req.flush(umaPagina([]));
  });

  it('deve mostrar o vazio quando a API não traz nada', async () => {
    const harness = await abrir('/proposicoes');
    http.expectOne((r) => r.url === '/api/proposicoes').flush(umaPagina([]));
    await harness.fixture.whenStable();
    expect(harness.routeNativeElement!.textContent).toContain(
      'Nenhuma proposição com esses filtros',
    );
  });
});
