import { Candidatura, Page, Proposicao } from '../app/core/api.models';

export function umaCandidatura(parcial: Partial<Candidatura> = {}): Candidatura {
  return {
    vinculo: 7,
    sqCandidato: '250001234567',
    anoEleicao: 2026,
    cargo: 'DEPUTADO FEDERAL',
    uf: 'RS',
    partido: 'PT',
    situacao: 'ELEITO POR QP',
    eleito: true,
    confianca: 'NOME_URNA_EXATO',
    status: 'SUGERIDO',
    ...parcial,
  };
}

export function umaProposicao(parcial: Partial<Proposicao> = {}): Proposicao {
  return {
    id: 1,
    fonte: 'camara',
    idExterno: '2501234',
    esfera: 'CAMARA_FEDERAL',
    casa: 'Câmara dos Deputados',
    sigla: 'PL',
    numero: 1234,
    ano: 2026,
    identificacao: 'PL 1234/2026',
    ementa: 'Dispõe sobre a transparência dos dados legislativos.',
    dataApresentacao: '2026-10-30',
    url: 'https://www.camara.leg.br/proposicoesWeb/fichadetramitacao?idProposicao=2501234',
    primeiraVezVistaEm: '2026-10-30T18:00:00Z',
    autores: [
      {
        ordem: 1,
        nome: 'Maria do Rosário',
        tipo: 'Deputado(a)',
        proponente: true,
        parlamentar: {
          id: 10,
          fonte: 'camara',
          idExterno: '74398',
          nome: 'Maria do Rosário',
          partido: 'PT',
          uf: 'RS',
          fotoUrl: null,
        },
        candidaturas: [umaCandidatura()],
      },
    ],
    ...parcial,
  };
}

export function umaPagina<T>(content: T[], totalElements = content.length): Page<T> {
  return {
    content,
    page: { size: 20, number: 0, totalElements, totalPages: Math.ceil(totalElements / 20) },
  };
}
