import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Proposicao } from '../core/api.models';
import { partidoUf } from '../core/rotulos';
import { SeloCandidatura } from '../shared/selo-candidatura';

/** Quantos autores aparecem no card; o resto fica no detalhe. */
const AUTORES_NO_CARD = 3;

/** Item do feed e do perfil: identificação, casa, data, ementa e autores com o selo do TSE. */
@Component({
  selector: 'app-proposicao-card',
  imports: [RouterLink, DatePipe, SeloCandidatura],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let p = proposicao();
    <article class="card">
      <header>
        <a class="identificacao" [routerLink]="['/proposicoes', p.id]">{{ p.identificacao }}</a>
        <span class="casa">{{ p.casa }}</span>
        @if (nova()) {
          <span class="nova">nova</span>
        }
        <span class="data">
          @if (p.dataApresentacao) {
            apresentada em {{ p.dataApresentacao | date: 'dd/MM/yyyy' }}
          }
        </span>
      </header>
      <p class="ementa">{{ p.ementa || 'Sem ementa.' }}</p>
      @if (autores().length) {
        <ul class="autores" aria-label="Autores">
          @for (a of autores(); track a.ordem) {
            <li>
              <span class="nome">{{ a.nome }}</span>
              @if (a.parlamentar; as parl) {
                @if (siglas(parl.partido, parl.uf); as s) {
                  <span class="partido">{{ s }}</span>
                }
              }
              @for (c of a.candidaturas; track c.vinculo) {
                <app-selo-candidatura [candidatura]="c" />
              }
            </li>
          }
          @if (outrosAutores() > 0) {
            <li class="outros">
              <a [routerLink]="['/proposicoes', p.id]">+{{ outrosAutores() }} autores</a>
            </li>
          }
        </ul>
      }
    </article>
  `,
  styles: `
    .card {
      padding: 16px;
      border-radius: 12px;
      background: var(--mat-sys-surface-container-low);
      border: 1px solid var(--mat-sys-outline-variant);
    }
    header {
      display: flex;
      flex-wrap: wrap;
      align-items: baseline;
      gap: 4px 12px;
    }
    .identificacao {
      font: var(--mat-sys-title-medium);
      color: var(--mat-sys-primary);
      text-decoration: none;
    }
    .identificacao:hover {
      text-decoration: underline;
    }
    .casa,
    .data {
      font: var(--mat-sys-body-small);
      color: var(--mat-sys-on-surface-variant);
    }
    .data {
      margin-left: auto;
    }
    .nova {
      font: var(--mat-sys-label-small);
      padding: 1px 8px;
      border-radius: 999px;
      background: var(--mat-sys-primary-container);
      color: var(--mat-sys-on-primary-container);
    }
    .ementa {
      margin: 8px 0 12px;
      font: var(--mat-sys-body-medium);
      display: -webkit-box;
      -webkit-line-clamp: 4;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .autores {
      list-style: none;
      margin: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .autores li {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 6px;
      font: var(--mat-sys-body-small);
    }
    .nome {
      font-weight: 500;
    }
    .partido {
      color: var(--mat-sys-on-surface-variant);
    }
    .outros a {
      color: var(--mat-sys-primary);
    }
  `,
})
export class ProposicaoCard {
  readonly proposicao = input.required<Proposicao>();
  /** Itens que a coleta viu depois disso ganham a marca "nova". */
  readonly ultimaVisita = input<Date | null>(null);

  protected readonly autores = computed(() => this.proposicao().autores.slice(0, AUTORES_NO_CARD));
  protected readonly outrosAutores = computed(
    () => this.proposicao().autores.length - AUTORES_NO_CARD,
  );
  protected readonly nova = computed(() => {
    const visita = this.ultimaVisita();
    return visita !== null && new Date(this.proposicao().primeiraVezVistaEm) > visita;
  });

  protected siglas(partido: string | null, uf: string | null): string {
    return partidoUf(partido, uf);
  }
}
