import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="topo">
      <div class="marca">
        <span class="logo" aria-hidden="true">◎</span>
        <span>Radar Legislativo</span>
      </div>
      <nav aria-label="Seções">
        <a routerLink="/proposicoes" routerLinkActive="ativo" ariaCurrentWhenActive="page">Feed</a>
        <a routerLink="/candidatos" routerLinkActive="ativo" ariaCurrentWhenActive="page"
          >Candidatos</a
        >
      </nav>
    </header>
    <main>
      <router-outlet />
    </main>
    <footer>Dados abertos do TSE, da Câmara dos Deputados e do Senado Federal.</footer>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      min-height: 100%;
    }
    .topo {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0 24px;
      padding: 0 16px;
      background: var(--mat-sys-surface-container);
      border-bottom: 1px solid var(--mat-sys-outline-variant);
    }
    .marca {
      display: flex;
      align-items: center;
      gap: 8px;
      font: var(--mat-sys-title-medium);
      padding: 12px 0;
    }
    .logo {
      color: var(--mat-sys-primary);
      font-size: 22px;
    }
    nav {
      display: flex;
      align-self: stretch;
      gap: 4px;
    }
    nav a {
      display: flex;
      align-items: center;
      padding: 0 16px;
      font: var(--mat-sys-title-small);
      color: var(--mat-sys-on-surface-variant);
      text-decoration: none;
      border-bottom: 3px solid transparent;
    }
    nav a:hover {
      color: var(--mat-sys-on-surface);
      background: var(--mat-sys-surface-container-high);
    }
    nav a.ativo {
      color: var(--mat-sys-primary);
      border-bottom-color: var(--mat-sys-primary);
    }
    main {
      flex: 1;
      width: 100%;
      box-sizing: border-box;
      max-width: 960px;
      margin: 0 auto;
      padding: 24px 16px 48px;
    }
    footer {
      padding: 16px;
      text-align: center;
      font: var(--mat-sys-body-small);
      color: var(--mat-sys-on-surface-variant);
    }
  `,
})
export class App {}
