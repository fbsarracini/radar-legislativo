import { MatPaginatorIntl } from '@angular/material/paginator';
import { Routes } from '@angular/router';
import { PaginadorEmPortugues } from './core/paginador';

export const routes: Routes = [
  {
    path: '',
    // Textos do paginador em português, sem levar o mat-paginator para o bundle inicial.
    providers: [{ provide: MatPaginatorIntl, useClass: PaginadorEmPortugues }],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'proposicoes' },
      {
        path: 'proposicoes',
        title: 'Feed · Radar Legislativo',
        loadComponent: () => import('./proposicoes/feed').then((m) => m.Feed),
      },
      {
        path: 'proposicoes/:id',
        title: 'Proposição · Radar Legislativo',
        loadComponent: () =>
          import('./proposicoes/proposicao-detalhe').then((m) => m.ProposicaoDetalhe),
      },
      {
        path: 'candidatos',
        title: 'Candidatos · Radar Legislativo',
        loadComponent: () => import('./candidatos/busca-candidatos').then((m) => m.BuscaCandidatos),
      },
      {
        path: 'candidatos/:sq',
        title: 'Candidato · Radar Legislativo',
        loadComponent: () => import('./candidatos/perfil-candidato').then((m) => m.PerfilCandidato),
      },
      { path: '**', redirectTo: 'proposicoes' },
    ],
  },
];
