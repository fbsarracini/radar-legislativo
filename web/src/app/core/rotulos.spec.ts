import { umaCandidatura } from '../../testing/fixtures';
import {
  capitalizar,
  contagem,
  explicacaoCandidatura,
  nomeDaCasa,
  nomeProprio,
  partidoUf,
  rotuloCandidatura,
} from './rotulos';

describe('rotulos', () => {
  it('deve capitalizar o texto em maiúsculas do TSE', () => {
    expect(capitalizar('DEPUTADO FEDERAL')).toBe('Deputado federal');
    expect(capitalizar('SÃO PAULO')).toBe('São paulo');
    expect(capitalizar(null)).toBe('');
  });

  it('deve dizer eleito ou candidato no selo', () => {
    expect(rotuloCandidatura(umaCandidatura())).toBe('Eleito 2026 · Deputado federal');
    expect(rotuloCandidatura(umaCandidatura({ eleito: false, cargo: 'SENADOR' }))).toBe(
      'Candidato 2026 · Senador',
    );
  });

  it('deve avisar que a sugestão pode ser homônimo e calar quando confirmado', () => {
    expect(explicacaoCandidatura(umaCandidatura({ confianca: 'NOME_PARCIAL' }))).toBe(
      'Deputado federal RS pelo PT em 2026 (Eleito por qp). Sugestão por nome parecido: pode ser homônimo.',
    );
    expect(explicacaoCandidatura(umaCandidatura({ status: 'CONFIRMADO' }))).toContain(
      'Vínculo confirmado.',
    );
  });

  it('deve juntar partido e UF só com o que houver', () => {
    expect(partidoUf('PT', 'SP')).toBe('PT-SP');
    expect(partidoUf(null, 'SP')).toBe('SP');
    expect(partidoUf(null, null)).toBe('');
  });

  it('deve escrever nomes de lugar com as partículas em minúsculas', () => {
    expect(nomeProprio('RIO GRANDE DO SUL')).toBe('Rio Grande do Sul');
    expect(nomeProprio('SÃO JOÃO DEL REI')).toBe('São João Del Rei');
    expect(nomeProprio(null)).toBe('');
  });

  it('deve dar o nome da casa a partir do código da fonte', () => {
    expect(nomeDaCasa('camara')).toBe('Câmara dos Deputados');
    expect(nomeDaCasa('sapl-poa')).toBe('Sapl-poa');
  });

  it('deve flexionar a contagem', () => {
    expect(contagem(1, 'proposição', 'proposições')).toBe('1 proposição');
    expect(contagem(3341, 'candidato', 'candidatos')).toBe('3.341 candidatos');
  });
});
