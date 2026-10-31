import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, effect, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatOption, MatSelect } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { RadarApi } from '../core/api';
import { Fonte, Page, Proposicao } from '../core/api.models';
import { paginaDaRota, paramsDaRota, semVazios } from '../core/consulta';
import { TIPOS, UFS, contagem } from '../core/rotulos';
import { UltimaVisita } from '../core/ultima-visita';
import { EstadoDaCarga } from '../shared/estado-da-carga';
import { ProposicaoCard } from './proposicao-card';

/** Filtros que vão da URL direto para /api/proposicoes. */
export const FILTROS_DO_FEED = [
  'desde',
  'ate',
  'fonte',
  'tipo',
  'uf',
  'partido',
  'autor',
  'tamanho',
] as const;
const TAMANHO_PADRAO = 20;

/** Feed do que foi protocolado: filtros na URL (dá para compartilhar o link), mais recente primeiro. */
@Component({
  selector: 'app-feed',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatFormField,
    MatLabel,
    MatInput,
    MatSelect,
    MatOption,
    MatCheckbox,
    MatButton,
    MatPaginator,
    ProposicaoCard,
    EstadoDaCarga,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './feed.html',
  styleUrl: './feed.scss',
})
export class Feed {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly ultimaVisita = inject(UltimaVisita).anterior;

  protected readonly ufs = UFS;
  protected readonly tipos = TIPOS;
  protected readonly contagem = contagem;

  protected readonly filtros = inject(NonNullableFormBuilder).group({
    desde: '',
    ate: '',
    fonte: '',
    tipo: [[] as string[]],
    uf: '',
    partido: '',
    autor: '',
    novas: false,
  });

  private readonly query = toSignal(this.rota.queryParamMap, { requireSync: true });
  protected readonly pagina = computed(() => paginaDaRota(this.query()));
  protected readonly tamanho = computed(
    () => Number(this.query().get('tamanho')) || TAMANHO_PADRAO,
  );
  protected readonly soNovas = computed(
    () => this.query().get('novas') === 'true' && this.ultimaVisita !== null,
  );

  protected readonly fontes = httpResource<Fonte[]>(() => RadarApi.fontes(), { defaultValue: [] });
  protected readonly feed = httpResource<Page<Proposicao>>(() => {
    let params = paramsDaRota(this.query(), FILTROS_DO_FEED).set('pagina', this.pagina());
    if (this.soNovas()) params = params.set('novasDesde', this.ultimaVisita!.toISOString());
    return RadarApi.feed(params);
  });

  constructor() {
    // URL → formulário: vale para a primeira carga e para voltar/avançar no navegador.
    effect(() => {
      const q = this.query();
      this.filtros.setValue({
        desde: q.get('desde') ?? '',
        ate: q.get('ate') ?? '',
        fonte: q.get('fonte') ?? '',
        tipo: q.getAll('tipo'),
        uf: q.get('uf') ?? '',
        partido: q.get('partido') ?? '',
        autor: q.get('autor') ?? '',
        novas: q.get('novas') === 'true',
      });
    });
  }

  protected aplicar(): void {
    const { novas, ...filtros } = this.filtros.getRawValue();
    const tamanho = this.tamanho() === TAMANHO_PADRAO ? null : this.tamanho();
    this.router.navigate([], {
      queryParams: semVazios({ ...filtros, novas: novas || null, tamanho }),
    });
  }

  protected limpar(): void {
    this.router.navigate([], { queryParams: {} });
  }

  protected mudarPagina(evento: PageEvent): void {
    this.router.navigate([], {
      queryParams: {
        pagina: evento.pageIndex || null,
        tamanho: evento.pageSize === TAMANHO_PADRAO ? null : evento.pageSize,
      },
      queryParamsHandling: 'merge',
    });
    window.scrollTo({ top: 0 });
  }
}
