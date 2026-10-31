# Radar Legislativo — web

Front em Angular 22 (standalone, signals, zoneless) + Angular Material, consumindo a API do backend deste repo.

```bash
npm ci
npx ng serve              # http://localhost:4200; proxy.conf.json manda /api para http://localhost:8080
npx ng test --watch=false # Vitest
npx ng build              # web/dist/web
```

```
src/app
├── core/         api.models.ts (espelho dos *Response), api.ts (RadarApi: URLs e escritas),
│                 consulta.ts (URL ⇄ filtros), rotulos.ts, erro.interceptor.ts (ApiError → snackbar),
│                 ultima-visita.ts, paginador.ts
├── shared/       selo-candidatura, estado-da-carga, _pagina.scss
├── proposicoes/  feed (filtros na URL), proposicao-detalhe, proposicao-card
└── candidatos/   busca-candidatos, perfil-candidato (confirmar/rejeitar vínculo)
```

Os filtros ficam nos query params: o link da tela filtrada pode ser compartilhado, e voltar no navegador refaz a busca.
