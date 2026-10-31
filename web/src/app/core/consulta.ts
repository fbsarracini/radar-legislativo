import { HttpParams } from '@angular/common/http';
import { ParamMap, Params } from '@angular/router';

/** Valor de filtro aceito nos query params: vazio/nulo some da URL e da chamada à API. */
export type ValorFiltro = string | number | boolean | string[] | null | undefined;

/** Remove o que está vazio, para a URL e a API só verem filtros de verdade. */
export function semVazios(valores: Record<string, ValorFiltro>): Params {
  const params: Params = {};
  for (const [chave, valor] of Object.entries(valores)) {
    if (valor === null || valor === undefined) continue;
    if (typeof valor === 'string' && valor.trim() === '') continue;
    if (Array.isArray(valor) && valor.length === 0) continue;
    params[chave] = typeof valor === 'string' ? valor.trim() : valor;
  }
  return params;
}

/** Copia os query params da rota (inclusive repetidos, como tipo=PL&tipo=PEC) para a chamada HTTP. */
export function paramsDaRota(rota: ParamMap, chaves: readonly string[]): HttpParams {
  let params = new HttpParams();
  for (const chave of chaves) {
    for (const valor of rota.getAll(chave)) {
      if (valor !== '') params = params.append(chave, valor);
    }
  }
  return params;
}

/** Página da URL (0-based, como o backend), tolerante a lixo. */
export function paginaDaRota(rota: ParamMap): number {
  const pagina = Number(rota.get('pagina'));
  return Number.isInteger(pagina) && pagina > 0 ? pagina : 0;
}
