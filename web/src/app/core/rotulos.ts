import { Candidatura, Confianca, StatusVinculo } from './api.models';

export const UFS = [
  'AC',
  'AL',
  'AM',
  'AP',
  'BA',
  'CE',
  'DF',
  'ES',
  'GO',
  'MA',
  'MG',
  'MS',
  'MT',
  'PA',
  'PB',
  'PE',
  'PI',
  'PR',
  'RJ',
  'RN',
  'RO',
  'RR',
  'RS',
  'SC',
  'SE',
  'SP',
  'TO',
] as const;

/** Tipos acompanhados pela coleta (radar.<casa>.tipos). */
export const TIPOS = [
  { sigla: 'PL', nome: 'Projeto de lei' },
  { sigla: 'PLP', nome: 'Projeto de lei complementar' },
  { sigla: 'PEC', nome: 'Proposta de emenda à Constituição' },
  { sigla: 'PDL', nome: 'Projeto de decreto legislativo' },
] as const;

export const CARGOS = [
  'Deputado federal',
  'Senador',
  'Deputado estadual',
  'Deputado distrital',
  'Governador',
  'Presidente',
  'Prefeito',
  'Vereador',
] as const;

export const CONFIANCA: Record<Confianca, string> = {
  NOME_URNA_EXATO: 'nome de urna idêntico',
  NOME_CIVIL_EXATO: 'nome civil idêntico',
  NOME_PARCIAL: 'nome parecido',
};

export const STATUS_VINCULO: Record<StatusVinculo, string> = {
  SUGERIDO: 'Sugerido',
  CONFIRMADO: 'Confirmado',
  REJEITADO: 'Rejeitado',
};

/** "Deputado Federal" → "Deputado federal": o TSE manda tudo em maiúsculas. */
export function capitalizar(texto: string | null | undefined): string {
  if (!texto) return '';
  const minusculo = texto.toLocaleLowerCase('pt-BR');
  return minusculo.charAt(0).toLocaleUpperCase('pt-BR') + minusculo.slice(1);
}

const PARTICULAS = new Set(['da', 'das', 'de', 'do', 'dos', 'e']);

/** "RIO GRANDE DO SUL" → "Rio Grande do Sul": municípios e UEs do TSE. */
export function nomeProprio(texto: string | null | undefined): string {
  if (!texto) return '';
  return texto
    .toLocaleLowerCase('pt-BR')
    .split(' ')
    .map((p, i) =>
      i > 0 && PARTICULAS.has(p) ? p : p.charAt(0).toLocaleUpperCase('pt-BR') + p.slice(1),
    )
    .join(' ');
}

const CASAS: Record<string, string> = { camara: 'Câmara dos Deputados', senado: 'Senado Federal' };

/** Nome da casa a partir do código da fonte ("camara" → "Câmara dos Deputados"). */
export function nomeDaCasa(fonte: string): string {
  return CASAS[fonte] ?? capitalizar(fonte);
}

/** "1 proposição", "45 proposições". */
export function contagem(total: number, singular: string, plural: string): string {
  return `${total.toLocaleString('pt-BR')} ${total === 1 ? singular : plural}`;
}

/** Texto curto do selo: "Eleito 2026 · Deputado federal". */
export function rotuloCandidatura(c: Candidatura): string {
  const resultado = c.eleito ? 'Eleito' : 'Candidato';
  return `${resultado} ${c.anoEleicao} · ${capitalizar(c.cargo)}`;
}

/** Explicação do selo para o tooltip: deixa claro quando ainda pode ser homônimo. */
export function explicacaoCandidatura(c: Candidatura): string {
  const base =
    `${capitalizar(c.cargo)} ${c.uf} pelo ${c.partido} em ${c.anoEleicao}` +
    (c.situacao ? ` (${capitalizar(c.situacao)})` : '');
  return c.status === 'CONFIRMADO'
    ? `${base}. Vínculo confirmado.`
    : `${base}. Sugestão por ${CONFIANCA[c.confianca]}: pode ser homônimo.`;
}

/** "PT-SP", "PT", "SP" ou "" conforme o que houver. */
export function partidoUf(
  partido: string | null | undefined,
  uf: string | null | undefined,
): string {
  return [partido, uf].filter(Boolean).join('-');
}
