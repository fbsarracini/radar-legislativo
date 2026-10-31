import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { MatTooltip } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { Candidatura } from '../core/api.models';
import { explicacaoCandidatura, rotuloCandidatura } from '../core/rotulos';

/** Selo do TSE ao lado do autor: sólido quando confirmado, tracejado quando ainda é sugestão. */
@Component({
  selector: 'app-selo-candidatura',
  imports: [RouterLink, MatTooltip],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a
      class="selo"
      [class.confirmado]="candidatura().status === 'CONFIRMADO'"
      [class.eleito]="candidatura().eleito"
      [routerLink]="['/candidatos', candidatura().sqCandidato]"
      [matTooltip]="explicacao()"
      [attr.aria-label]="rotulo() + '. ' + explicacao()"
    >
      @if (candidatura().status !== 'CONFIRMADO') {
        <span class="interrogacao" aria-hidden="true">?</span>
      }
      {{ rotulo() }}
    </a>
  `,
  styles: `
    .selo {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 2px 8px;
      border-radius: 999px;
      font: var(--mat-sys-label-small);
      text-decoration: none;
      color: var(--mat-sys-on-surface-variant);
      border: 1px dashed var(--mat-sys-outline);
      white-space: nowrap;
    }
    .selo.eleito {
      color: var(--mat-sys-on-tertiary-container);
      border-color: var(--mat-sys-tertiary);
    }
    .selo.confirmado {
      border-style: solid;
      background: var(--mat-sys-surface-container-high);
    }
    .selo.confirmado.eleito {
      background: var(--mat-sys-tertiary-container);
    }
    .selo:hover,
    .selo:focus-visible {
      outline: 2px solid var(--mat-sys-primary);
      outline-offset: 1px;
    }
    .interrogacao {
      font-weight: 700;
    }
  `,
})
export class SeloCandidatura {
  readonly candidatura = input.required<Candidatura>();
  protected readonly rotulo = computed(() => rotuloCandidatura(this.candidatura()));
  protected readonly explicacao = computed(() => explicacaoCandidatura(this.candidatura()));
}
