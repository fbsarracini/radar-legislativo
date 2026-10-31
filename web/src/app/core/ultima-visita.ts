import { Injectable } from '@angular/core';

const CHAVE = 'radar.ultimaVisita';

/**
 * Lembra quando a pessoa abriu o radar pela última vez, para o feed marcar o que é novo.
 * O valor anterior é lido uma vez ao abrir o app e fica fixo durante a sessão; o novo já é gravado.
 * Só conveniência local: sem storage (aba anônima, bloqueio), simplesmente não há "novas".
 */
@Injectable({ providedIn: 'root' })
export class UltimaVisita {
  readonly anterior: Date | null = UltimaVisita.ler();

  constructor() {
    try {
      localStorage.setItem(CHAVE, new Date().toISOString());
    } catch {
      // sem storage: segue sem lembrar
    }
  }

  private static ler(): Date | null {
    try {
      const valor = localStorage.getItem(CHAVE);
      const data = valor ? new Date(valor) : null;
      return data && !Number.isNaN(data.getTime()) ? data : null;
    } catch {
      return null;
    }
  }
}
