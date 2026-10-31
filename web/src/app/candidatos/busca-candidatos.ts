import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, effect, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatAutocomplete, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatOption, MatSelect } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { RadarApi } from '../core/api';
import { Candidato, Page } from '../core/api.models';
import { paginaDaRota, paramsDaRota, semVazios } from '../core/consulta';
import { CARGOS, UFS, capitalizar, contagem, nomeProprio } from '../core/rotulos';
import { EstadoDaCarga } from '../shared/estado-da-carga';

export const FILTROS_DE_CANDIDATOS = [
  'ano',
  'uf',
  'cargo',
  'nome',
  'municipio',
  'eleito',
  'tamanho',
] as const;
const TAMANHO_PADRAO = 20;

/** Busca nos candidatos importados do TSE; cada linha leva ao perfil. */
@Component({
  selector: 'app-busca-candidatos',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormField,
    MatLabel,
    MatInput,
    MatSelect,
    MatOption,
    MatAutocomplete,
    MatAutocompleteTrigger,
    MatButton,
    MatPaginator,
    MatTableModule,
    EstadoDaCarga,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './busca-candidatos.html',
  styleUrl: './busca-candidatos.scss',
})
export class BuscaCandidatos {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly ufs = UFS;
  protected readonly cargos = CARGOS;
  protected readonly colunas = ['nome', 'cargo', 'uf', 'partido', 'situacao'];
  protected readonly capitalizar = capitalizar;
  protected readonly nomeProprio = nomeProprio;
  protected readonly contagem = contagem;

  protected readonly filtros = inject(NonNullableFormBuilder).group({
    nome: '',
    ano: '',
    uf: '',
    cargo: '',
    municipio: '',
    eleito: '',
  });

  private readonly query = toSignal(this.rota.queryParamMap, { requireSync: true });
  private readonly pagina = computed(() => paginaDaRota(this.query()));
  private readonly tamanho = computed(() => Number(this.query().get('tamanho')) || TAMANHO_PADRAO);

  /** A ordem (nome de urna) é fixa no backend. */
  protected readonly candidatos = httpResource<Page<Candidato>>(() =>
    RadarApi.candidatos(
      paramsDaRota(this.query(), FILTROS_DE_CANDIDATOS).set('pagina', this.pagina()),
    ),
  );

  constructor() {
    effect(() => {
      const q = this.query();
      this.filtros.setValue({
        nome: q.get('nome') ?? '',
        ano: q.get('ano') ?? '',
        uf: q.get('uf') ?? '',
        cargo: q.get('cargo') ?? '',
        municipio: q.get('municipio') ?? '',
        eleito: q.get('eleito') ?? '',
      });
    });
  }

  protected aplicar(): void {
    const tamanho = this.tamanho() === TAMANHO_PADRAO ? null : this.tamanho();
    this.router.navigate([], {
      queryParams: semVazios({ ...this.filtros.getRawValue(), tamanho }),
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
  }
}
