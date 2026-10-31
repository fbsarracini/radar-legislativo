import { HttpParams, httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButton } from '@angular/material/button';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { RadarApi } from '../core/api';
import { Page, PerfilDoCandidato, Proposicao, Vinculo } from '../core/api.models';
import { paginaDaRota } from '../core/consulta';
import {
  CONFIANCA,
  STATUS_VINCULO,
  capitalizar,
  nomeDaCasa,
  nomeProprio,
  partidoUf,
} from '../core/rotulos';
import { EstadoDaCarga } from '../shared/estado-da-carga';
import { ProposicaoCard } from '../proposicoes/proposicao-card';

/** Perfil do candidato: dados do TSE, vínculos com parlamentares (confirmar/rejeitar) e projetos. */
@Component({
  selector: 'app-perfil-candidato',
  imports: [RouterLink, MatButton, MatPaginator, ProposicaoCard, EstadoDaCarga],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './perfil-candidato.html',
  styleUrl: './perfil-candidato.scss',
})
export class PerfilCandidato {
  private readonly api = inject(RadarApi);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  /** Vem da rota /candidatos/:sq (withComponentInputBinding). */
  readonly sq = input.required<string>();

  protected readonly capitalizar = capitalizar;
  protected readonly partidoUf = partidoUf;
  protected readonly nomeProprio = nomeProprio;
  protected readonly nomeDaCasa = nomeDaCasa;
  protected readonly confianca = CONFIANCA;
  protected readonly statusVinculo = STATUS_VINCULO;

  private readonly query = toSignal(inject(ActivatedRoute).queryParamMap, { requireSync: true });
  private readonly pagina = computed(() => paginaDaRota(this.query()));

  protected readonly perfil = httpResource<PerfilDoCandidato>(() => RadarApi.perfil(this.sq()));
  protected readonly proposicoes = httpResource<Page<Proposicao>>(() =>
    RadarApi.proposicoesDoCandidato(this.sq(), new HttpParams().set('pagina', this.pagina())),
  );

  /** Vínculo com decisão em andamento, para travar os botões dele. */
  protected readonly decidindo = signal<number | null>(null);

  /** Os projetos vêm dos confirmados; sem nenhum confirmado, das sugestões de maior confiança. */
  protected readonly temConfirmado = computed(
    () => this.perfil.value()?.vinculos.some((v) => v.status === 'CONFIRMADO') ?? false,
  );

  protected decidir(vinculo: Vinculo, confirmado: boolean): void {
    this.decidindo.set(vinculo.id);
    this.api.decidirVinculo(this.sq(), vinculo.id, confirmado).subscribe({
      next: () => {
        this.decidindo.set(null);
        this.snackBar.open(
          confirmado
            ? `${vinculo.parlamentar.nome} confirmado.`
            : `${vinculo.parlamentar.nome} rejeitado.`,
          undefined,
          { duration: 3000 },
        );
        // A decisão muda os selos e quais projetos entram no perfil.
        this.perfil.reload();
        this.proposicoes.reload();
      },
      error: () => this.decidindo.set(null),
    });
  }

  protected mudarPagina(evento: PageEvent): void {
    this.router.navigate([], {
      queryParams: { pagina: evento.pageIndex || null },
      queryParamsHandling: 'merge',
    });
  }
}
