import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatAnchor } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { RadarApi } from '../core/api';
import { Proposicao } from '../core/api.models';
import { partidoUf } from '../core/rotulos';
import { EstadoDaCarga } from '../shared/estado-da-carga';
import { SeloCandidatura } from '../shared/selo-candidatura';

/** Detalhe de uma proposição: ementa completa, link oficial e todos os autores com o selo do TSE. */
@Component({
  selector: 'app-proposicao-detalhe',
  imports: [DatePipe, RouterLink, MatAnchor, MatIcon, SeloCandidatura, EstadoDaCarga],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './proposicao-detalhe.html',
  styleUrl: './proposicao-detalhe.scss',
})
export class ProposicaoDetalhe {
  /** Vem da rota /proposicoes/:id (withComponentInputBinding). */
  readonly id = input.required<string>();

  protected readonly proposicao = httpResource<Proposicao>(() => RadarApi.proposicao(this.id()));

  protected siglas(partido: string | null, uf: string | null): string {
    return partidoUf(partido, uf);
  }
}
