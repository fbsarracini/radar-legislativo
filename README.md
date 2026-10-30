# Radar Legislativo

Backend em Java 21 + Spring Boot 3.5 que cruza os **eleitos do TSE** com os **projetos que eles protocolaram** nas casas legislativas, usando só dados abertos oficiais.

## Como rodar

```bash
make run        # ou ./mvnw spring-boot:run
make test
# Swagger: http://localhost:8080/swagger-ui.html  ·  Health: /actuator/health
```

1. Importe os eleitos de um ano (baixa ~centenas de MB do TSE na primeira vez e guarda em `./dados/tse`):
   ```bash
   curl -X POST localhost:8080/api/admin/tse/importar -H 'Content-Type: application/json' -d '{"ano":2026}'
   # ou, com o zip já baixado em ./dados/tse (o caminho é relativo a radar.tse.diretorio-cache):
   curl -X POST localhost:8080/api/admin/tse/importar -H 'Content-Type: application/json' \
        -d '{"ano":2026,"arquivo":"consulta_cand_2026.zip"}'
   ```
2. Consulte:
   ```bash
   curl "localhost:8080/api/eleitos?ano=2026&uf=SP&cargo=deputado%20federal&nome=silva&pagina=0&tamanho=20"
   curl "localhost:8080/api/eleitos/{sqCandidato}"               # perfil + vínculos + projetos
   curl "localhost:8080/api/eleitos/{sqCandidato}/vinculos"
   curl "localhost:8080/api/eleitos/{sqCandidato}/proposicoes?ano=2025"
   ```
3. Ou vá direto às casas, sem passar pelo TSE:
   ```bash
   curl "localhost:8080/api/fontes"
   curl "localhost:8080/api/parlamentares?nome=amaral&uf=SP&fonte=camara"
   curl "localhost:8080/api/fontes/camara/parlamentares/{id}/proposicoes?ano=2025"
   ```

Erros seguem um formato único (`ApiError`): `{"status":404,"message":"recurso não encontrado","errors":[]}`.
400 = parâmetro inválido, 404 = eleito/fonte inexistente, 422 = regra de domínio, 502 = API pública fora do ar.

## Fontes de dados

| Esfera | Fonte | O que usamos |
|---|---|---|
| Eleitos (todas) | Portal de Dados Abertos do TSE — `consulta_cand_{ano}.zip` | CSV `;` Latin-1; filtro `DS_SIT_TOT_TURNO` começando com `ELEITO` |
| Federal — Câmara | `https://dadosabertos.camara.leg.br/api/v2` | `/deputados?nome&siglaUf&idLegislatura`, `/proposicoes?idDeputadoAutor&ano` |
| Federal — Senado | `https://legis.senado.leg.br/dadosabertos` | `/senador/lista/atual`, `/senador/lista/legislatura/{n}`, `/processo` |
| Estadual / Municipal | SAPL (Interlegis), uma instância por casa | `/api/base/autor/`, `/api/materia/materialegislativa/?autores=` |

## Limitações que valem saber antes de evoluir

- **Não existe API nacional para assembleias e câmaras municipais.** O SAPL cobre muitas casas, mas não todas (ex.: ALESP e a Câmara de São Paulo têm portais próprios). Para essas, crie uma nova implementação de `FonteLegislativa`.
- **Eleitos de 2026 tomam posse em 1º/02/2027.** Até lá, só os **reeleitos** (ou quem já teve outro mandato) têm projetos. Por isso a Câmara é consultada nas legislaturas 57 e 58.
- **Cargos executivos** (presidente, governador, prefeito) não aparecem como autores parlamentares; o vínculo devolve lista vazia.
- **O casamento TSE ↔ casa é por nome + UF**, com grau de confiança (`NOME_URNA_EXATO`, `NOME_CIVIL_EXATO`, `NOME_PARCIAL`). Homônimos acontecem: mostre a confiança na tela e permita confirmação manual.
- **Senado:** o serviço antigo `/senador/{codigo}/autorias` foi descontinuado e substituído por `/processo`. Confira o nome do parâmetro de autoria no Swagger do Senado e ajuste `radar.senado.parametro-autor` se necessário.
- **Câmara:** sem filtro de data, `/proposicoes` devolve só os últimos 30 dias — o cliente já trata isso.
- Os eleitos ficam em memória. Próximo passo natural: PostgreSQL + Spring Data JPA, gravando eleitos, vínculos confirmados e proposições (com atualização agendada via `@Scheduled`).

## Design de código

Segue o mesmo design do [poc-design-codigo-java](https://github.com/fbsarracini/poc-design-codigo-java):

- **Package by feature**: cada fatia vertical mora junto; nada de `controller/`, `service/`, `repository/`.
- **Casos de uso explícitos** anotados com `@UseCase` (`@Service` + `@Validated`), com um único método `execute`.
- **Um controller por caso de uso**, fino: valida a entrada, chama o caso de uso e converte para `XxxResponse.of(...)`.
- **Request implementa uma interface `XxxData`**, então o caso de uso não conhece o transporte HTTP.
- **Invariantes no domínio** com `Assert` (vira `IllegalArgumentException` → 422 na borda).
- **Exceções traduzidas na borda** por `GlobalExceptionHandler`; logs estruturados com `AppLog` (`who | what | where | why`).

```
br.com.radarlegislativo
├── UseCase, Application
├── fonte/      casas legislativas
│               Esfera, Parlamentar, Proposicao, Nomes, FonteLegislativa, FonteRegistry
│               CamaraClient, SenadoClient, SaplClient, Json
│               ListarFontes, BuscarParlamentares (cache), ListarProposicoesDoParlamentar (cache)
│               + *Controller e *Response de cada um
├── eleito/     eleitos do TSE e o cruzamento com as casas
│               CandidatoEleito, Vinculo, FiltroEleitos, PerfilDoEleito, EleitoRepository (memória)
│               ImportadorTse, CsvTse
│               ImportarEleitos, ListarEleitos, BuscarVinculosDoEleito,
│               ListarProposicoesDoEleito, ConsultarPerfilDoEleito
│               + *Controller, *Request/*Data e *Response
├── config/     RadarProperties (validado no boot), HttpConfig, CacheConfig, CorsConfig, OpenApiConfig
├── exception/  ApiError, GlobalExceptionHandler, NotFoundException, FonteIndisponivelException
└── log/        AppLog
```
