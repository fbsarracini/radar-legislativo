import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { umaCandidatura, umaProposicao } from '../../testing/fixtures';
import { ProposicaoCard } from './proposicao-card';

describe('ProposicaoCard', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideRouter([])] }));

  async function renderizar(props: Record<string, unknown>): Promise<HTMLElement> {
    const fixture = TestBed.createComponent(ProposicaoCard);
    for (const [k, v] of Object.entries(props)) fixture.componentRef.setInput(k, v);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('deve mostrar identificação, autor com partido-UF e o selo do TSE', async () => {
    const el = await renderizar({ proposicao: umaProposicao() });
    expect(el.querySelector('.identificacao')?.textContent).toContain('PL 1234/2026');
    expect(el.querySelector('.identificacao')?.getAttribute('href')).toBe('/proposicoes/1');
    expect(el.querySelector('.partido')?.textContent).toBe('PT-RS');
    const selo = el.querySelector('app-selo-candidatura a');
    expect(selo?.textContent).toContain('Eleito 2026 · Deputado federal');
    expect(selo?.getAttribute('href')).toBe('/candidatos/250001234567');
    expect(selo?.textContent).toContain('?');
  });

  it('deve marcar como nova o que a coleta viu depois da última visita', async () => {
    const proposicao = umaProposicao({ primeiraVezVistaEm: '2026-10-30T18:00:00Z' });
    expect(
      (
        await renderizar({ proposicao, ultimaVisita: new Date('2026-10-30T12:00:00Z') })
      ).querySelector('.nova'),
    ).not.toBeNull();
    expect(
      (
        await renderizar({ proposicao, ultimaVisita: new Date('2026-10-31T12:00:00Z') })
      ).querySelector('.nova'),
    ).toBeNull();
    expect(
      (await renderizar({ proposicao, ultimaVisita: null })).querySelector('.nova'),
    ).toBeNull();
  });

  it('deve mostrar três autores e mandar o resto para o detalhe', async () => {
    const base = umaProposicao().autores[0];
    const autores = [1, 2, 3, 4, 5].map((ordem) => ({ ...base, ordem, nome: `Autor ${ordem}` }));
    const el = await renderizar({ proposicao: umaProposicao({ autores }) });
    expect(el.querySelectorAll('.nome').length).toBe(3);
    expect(el.querySelector('.outros')?.textContent).toContain('+2 autores');
  });

  it('deve tirar a interrogação do selo confirmado', async () => {
    const proposicao = umaProposicao();
    proposicao.autores[0].candidaturas = [umaCandidatura({ status: 'CONFIRMADO' })];
    const el = await renderizar({ proposicao });
    expect(el.querySelector('app-selo-candidatura .interrogacao')).toBeNull();
    expect(el.querySelector('app-selo-candidatura a')?.classList).toContain('confirmado');
  });
});
