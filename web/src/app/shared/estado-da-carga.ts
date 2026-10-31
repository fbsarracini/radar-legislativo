import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatProgressBar } from '@angular/material/progress-bar';

/** Barra de progresso, mensagem de erro com "tentar de novo" e o vazio, num lugar só. */
@Component({
  selector: 'app-estado-da-carga',
  imports: [MatProgressBar, MatButton],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (carregando()) {
      <mat-progress-bar mode="indeterminate" aria-label="Carregando" />
    }
    @if (erro()) {
      <div class="aviso" role="alert">
        <p>{{ mensagemDeErro() }}</p>
        <button mat-stroked-button type="button" (click)="tentarDeNovo.emit()">
          Tentar de novo
        </button>
      </div>
    } @else if (vazio() && !carregando()) {
      <div class="aviso">
        <p>{{ mensagemVazio() }}</p>
      </div>
    }
  `,
  styles: `
    :host {
      display: block;
    }
    mat-progress-bar {
      margin-bottom: 8px;
    }
    .aviso {
      padding: 32px 16px;
      text-align: center;
      color: var(--mat-sys-on-surface-variant);
    }
  `,
})
export class EstadoDaCarga {
  readonly carregando = input(false);
  readonly erro = input(false);
  readonly vazio = input(false);
  readonly mensagemDeErro = input('Não foi possível carregar.');
  readonly mensagemVazio = input('Nada encontrado com esses filtros.');
  readonly tentarDeNovo = output();
}
