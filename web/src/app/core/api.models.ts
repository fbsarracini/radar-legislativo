// Espelho dos *Response do backend (br.com.radarlegislativo). Datas chegam como string ISO.

export type Esfera =
  'CAMARA_FEDERAL' | 'SENADO_FEDERAL' | 'ASSEMBLEIA_ESTADUAL' | 'CAMARA_MUNICIPAL';
export type Confianca = 'NOME_URNA_EXATO' | 'NOME_CIVIL_EXATO' | 'NOME_PARCIAL';
export type StatusVinculo = 'SUGERIDO' | 'CONFIRMADO' | 'REJEITADO';

/** Page do Spring Data serializada com `serialization-mode: via-dto`. */
export interface Page<T> {
  content: T[];
  page: { size: number; number: number; totalElements: number; totalPages: number };
}

/** Formato único de erro da API (GlobalExceptionHandler). */
export interface ApiError {
  status: number;
  message: string;
  errors: string[];
}

export interface Fonte {
  codigo: string;
  casa: string;
  esfera: Esfera;
  nivel: string;
  uf: string | null;
  municipio: string | null;
}

export interface ParlamentarColetado {
  id: number;
  fonte: string;
  idExterno: string;
  nome: string;
  partido: string | null;
  uf: string | null;
  fotoUrl: string | null;
}

export interface Candidatura {
  vinculo: number;
  sqCandidato: string;
  anoEleicao: number;
  cargo: string;
  uf: string;
  partido: string;
  situacao: string | null;
  eleito: boolean;
  confianca: Confianca;
  status: StatusVinculo;
}

export interface Autoria {
  ordem: number;
  nome: string;
  tipo: string | null;
  proponente: boolean;
  parlamentar: ParlamentarColetado | null;
  candidaturas: Candidatura[];
}

export interface Proposicao {
  id: number;
  fonte: string;
  idExterno: string;
  esfera: Esfera;
  casa: string;
  sigla: string;
  numero: number | null;
  ano: number | null;
  identificacao: string;
  ementa: string | null;
  dataApresentacao: string | null;
  url: string | null;
  primeiraVezVistaEm: string;
  autores: Autoria[];
}

export interface Candidato {
  sqCandidato: string;
  anoEleicao: number;
  nome: string;
  nomeUrna: string;
  cargo: string;
  esfera: Esfera | null;
  uf: string;
  municipio: string | null;
  partido: string;
  numero: string;
  situacao: string | null;
  eleito: boolean;
}

export interface Vinculo {
  id: number;
  parlamentar: ParlamentarColetado;
  confianca: Confianca;
  status: StatusVinculo;
  decididoEm: string | null;
}

export interface PerfilDoCandidato {
  candidato: Candidato;
  vinculos: Vinculo[];
  proposicoes: Proposicao[];
  totalProposicoes: number;
}
