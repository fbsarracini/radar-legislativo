# Radar Legislativo

Backend em Java 21 + Spring Boot 3.5 que cruza os **candidatos do TSE** com os **projetos protocolados** nas casas legislativas, usando só dados abertos oficiais. Dados em PostgreSQL (Flyway).

## Como rodar

Precisa de Docker (para o PostgreSQL) e Java 21.

```bash
cp .env.sample .env   # defina DB_PASSWORD
make run              # sobe o Postgres (docker compose) e a app; ou: make db && ./mvnw spring-boot:run
make test             # os testes de banco usam Testcontainers e são pulados se não houver Docker
# Swagger: http://localhost:8080/swagger-ui.html  ·  Health: /actuator/health
```

1. Importe os candidatos de um ano — todos, eleitos ou não; o campo `eleito` diz quem ganhou (baixa ~centenas de MB do TSE na primeira vez e guarda em `./dados/tse`):
   ```bash
   curl -X POST localhost:8080/api/admin/tse/importar -H 'Content-Type: application/json' -d '{"ano":2026}'
   # ou, com o zip já baixado em ./dados/tse (o caminho é relativo a radar.tse.diretorio-cache):
   curl -X POST localhost:8080/api/admin/tse/importar -H 'Content-Type: application/json' \
        -d '{"ano":2026,"arquivo":"consulta_cand_2026.zip"}'
   ```
2. Consulte:
   ```bash
   curl "localhost:8080/api/candidatos?ano=2026&uf=SP&cargo=deputado%20federal&nome=silva&eleito=true&pagina=0&tamanho=20"
   curl "localhost:8080/api/candidatos/{sqCandidato}"               # perfil + vínculos + projetos
   curl "localhost:8080/api/candidatos/{sqCandidato}/vinculos"
   curl "localhost:8080/api/candidatos/{sqCandidato}/proposicoes?ano=2025"
   ```
3. Ou vá direto às casas, sem passar pelo TSE:
   ```bash
   curl "localhost:8080/api/fontes"
   curl "localhost:8080/api/parlamentares?nome=amaral&uf=SP&fonte=camara"
   curl "localhost:8080/api/fontes/camara/parlamentares/{id}/proposicoes?ano=2025"
   ```
4. Coleta do que foi protocolado (roda sozinha de hora em hora; `radar.coleta.*` no `application.yml`):
   ```bash
   curl -X POST localhost:8080/api/admin/coletas/camara   # força uma coleta agora
   curl localhost:8080/api/admin/coletas                  # últimas 20 coletas: janela, status, novas/existentes
   ```
5. Feed do que foi protocolado (lê do banco; mais recente primeiro):
   ```bash
   curl "localhost:8080/api/proposicoes?desde=2026-10-01&tipo=PL,PEC&uf=SP&partido=PT&pagina=0&tamanho=20"
   curl "localhost:8080/api/proposicoes?autor=rosario"                              # nome do autor, sem acento
   curl "localhost:8080/api/proposicoes?novasDesde=2026-10-30T18:00:00-03:00"       # o que a coleta viu depois disso
   curl "localhost:8080/api/proposicoes/{id}"                                       # detalhe + autores
   ```
   `uf`, `partido`, `autor` e `parlamentar` valem para o mesmo autor: `partido=PT&uf=SP` = "um autor do PT de SP".

Erros seguem um formato único (`ApiError`): `{"status":404,"message":"recurso não encontrado","errors":[]}`.
400 = parâmetro inválido, 404 = candidato/fonte inexistente, 422 = regra de domínio, 502 = API pública fora do ar.

## Fontes de dados

| Esfera | Fonte | O que usamos |
|---|---|---|
| Candidatos (todas) | Portal de Dados Abertos do TSE — `consulta_cand_{ano}.zip` | CSV `;` Latin-1; `eleito` = `DS_SIT_TOT_TURNO` começando com `ELEITO` |
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
- **Volume:** ano municipal tem ~450 mil candidatos. A importação grava em lote (upsert JDBC) numa transação, mas ainda é síncrona e carrega o arquivo inteiro em memória.
- **Coleta:** só a Câmara implementa `FonteComColeta` por enquanto, e só os tipos de `radar.camara.tipos` (PL, PLP, PEC, PDL). A primeira coleta volta `dias-iniciais` dias; as seguintes recomeçam um dia antes do fim da última com SUCESSO, então uma falha não deixa buraco. Partido/UF do autor é o do momento em que ele apareceu pela primeira vez.
- O lock da coleta é por instância; rodando mais de uma instância, troque por lock no banco (ex.: ShedLock).
- Vínculos candidato ↔ parlamentar e as proposições do perfil ainda são consultados nas casas a cada request (cache de 6h). Próximo passo: gravar os vínculos e mostrar no feed quando o autor é candidato/eleito do TSE.

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
│               Esfera, Parlamentar, Proposicao, Autor, Nomes, FonteLegislativa, FonteComColeta, FonteRegistry
│               CamaraClient, SenadoClient, SaplClient, Json
│               ListarFontes, BuscarParlamentares (cache), ListarProposicoesDoParlamentar (cache)
│               + *Controller e *Response de cada um
├── candidato/  candidatos do TSE e o cruzamento com as casas
│               Candidato (@Entity), Vinculo, FiltroCandidatos (Specification), PerfilDoCandidato
│               CandidatoRepository (JPA) + SubstituicaoDeCandidatos (upsert em lote)
│               ImportadorTse, CsvTse
│               ImportarCandidatos, ListarCandidatos, BuscarVinculosDoCandidato,
│               ListarProposicoesDoCandidato, ConsultarPerfilDoCandidato
│               + *Controller, *Request/*Data e *Response
├── proposicao/ coleta agendada do que é protocolado nas casas
│               ProposicaoColetada, Autoria, ParlamentarColetado, Coleta (@Entity) + repositórios
│               ColetarProposicoes, GravadorDeProposicoes (uma transação por escrita), AgendadorDeColetas
│               ListarColetas + *Controller e *Response
│               FiltroProposicoes (Specification), ListarProposicoes (feed), ConsultarProposicao
├── config/     RadarProperties (validado no boot), HttpConfig, CacheConfig, CorsConfig, OpenApiConfig
├── exception/  ApiError, GlobalExceptionHandler, NotFoundException, FonteIndisponivelException
└── log/        AppLog
```
