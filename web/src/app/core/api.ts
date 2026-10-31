import { HttpClient, HttpParams, HttpResourceRequest } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Vinculo } from './api.models';

/** Prefixo da API; no `ng serve` o proxy.conf.json manda /api para o backend em :8080. */
export const API = '/api';

/**
 * Pedidos de leitura para `httpResource` (um por endpoint) e as escritas via HttpClient.
 * As telas não montam URL: tudo que fala com o backend passa por aqui.
 */
@Injectable({ providedIn: 'root' })
export class RadarApi {
  private readonly http = inject(HttpClient);

  static fontes(): HttpResourceRequest {
    return { url: `${API}/fontes` };
  }

  static feed(params: HttpParams): HttpResourceRequest {
    return { url: `${API}/proposicoes`, params };
  }

  static proposicao(id: string): HttpResourceRequest {
    return { url: `${API}/proposicoes/${encodeURIComponent(id)}` };
  }

  static candidatos(params: HttpParams): HttpResourceRequest {
    return { url: `${API}/candidatos`, params };
  }

  static perfil(sq: string): HttpResourceRequest {
    return { url: `${API}/candidatos/${encodeURIComponent(sq)}` };
  }

  static proposicoesDoCandidato(sq: string, params: HttpParams): HttpResourceRequest {
    return { url: `${API}/candidatos/${encodeURIComponent(sq)}/proposicoes`, params };
  }

  /** Confirma ou rejeita o vínculo; o recálculo automático nunca desfaz a decisão. */
  decidirVinculo(sq: string, vinculo: number, confirmado: boolean): Observable<Vinculo> {
    return this.http.put<Vinculo>(
      `${API}/candidatos/${encodeURIComponent(sq)}/vinculos/${vinculo}/confirmacao`,
      { confirmado },
    );
  }
}
