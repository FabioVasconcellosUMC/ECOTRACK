# EcoTrack

Sistema web para gestao e rastreabilidade operacional de residuos solidos, desenvolvido como Projeto Final de Curso em Bacharelado em Sistemas de Informacao.

O EcoTrack centraliza o cadastro de empresas geradoras, transportadoras e receptoras, o controle de lotes de residuos, o registro de transportes, a confirmacao de recebimento final, a geracao de manifesto interno em PDF e a visualizacao de indicadores gerenciais.

## Objetivo

O objetivo do projeto e oferecer uma solucao web para apoiar o acompanhamento interno do ciclo operacional de residuos solidos, desde a geracao do lote ate a confirmacao de recebimento pela empresa receptora.

A Politica Nacional de Residuos Solidos (PNRS) e utilizada como referencia conceitual para contextualizar a importancia da responsabilidade compartilhada, da gestao integrada e da rastreabilidade. O EcoTrack nao substitui sistemas oficiais, como SINIR/MTR, nem tem a finalidade de comprovar atendimento legal.

## Estado atual

O projeto esta funcional em ambiente publicado, com frontend hospedado na Vercel e API hospedada na AWS. A aplicacao possui autenticacao, controle de acesso por perfil, CRUD operacional de empresas, cadastro e acompanhamento de lotes, criacao e acompanhamento de transportes, confirmacao ou recusa de recebimento final, manifesto PDF interno, dashboards, relatorios e exclusao logica de usuarios.

## Acessos

- Frontend: https://ecotrack-khaki.vercel.app
- Backend API: https://api-ecotrack.duckdns.org

As credenciais de acesso nao sao versionadas neste repositorio. Usuarios e senhas devem ser gerenciados apenas pelos responsaveis do projeto ou pelo ambiente de producao.

## Tecnologias

Backend:

- Java 17
- Spring Boot 3.5.13
- Spring Web
- Spring Security
- JWT com JJWT 0.12.6
- Spring Data JPA
- Bean Validation
- Flyway
- PostgreSQL
- H2 para testes
- Lombok
- OpenPDF
- Spring Mail

Frontend:

- Vue 3.5
- Vite 8
- Tailwind CSS 4
- Vue Router
- Axios
- Chart.js e vue-chartjs
- Lucide Vue

Infraestrutura e integracoes:

- AWS para hospedagem da API
- Vercel para hospedagem do frontend
- DuckDNS para o dominio publico da API
- Caddy como proxy HTTPS/reverso da API publicada
- PostgreSQL em nuvem
- BrasilAPI para consulta de CNPJ no frontend
- Resend para notificacoes operacionais por e-mail

## Arquitetura

O projeto segue uma arquitetura em camadas no backend:

- `controller`: recebe as requisicoes HTTP e expoe os endpoints REST.
- `service`: concentra regras de negocio, validacoes, escopo por perfil, criptografia e efeitos de status.
- `repository`: realiza o acesso ao banco de dados por meio do Spring Data JPA.
- `entity`: representa as tabelas, enums e relacionamentos do banco.
- `dto`: padroniza entradas e respostas especificas da API.
- `security`: implementa autenticacao JWT, filtro de requisicoes e carregamento do usuario autenticado.
- `validation`: concentra validacoes reutilizaveis contra textos inseguros.
- `config`: centraliza configuracoes como CORS, seguranca e rotinas de apoio.

O frontend consome a API REST autenticada por Axios. O token JWT, nome e perfil do usuario sao armazenados no `localStorage` para controle de sessao e envio do header `Authorization`.

## Entidades principais

- `Usuario`: representa os usuarios autenticados, seus perfis, aceite de termos e estado ativo/inativo.
- `Empresa`: representa empresas dos tipos `GERADORA`, `TRANSPORTADORA` e `RECEPTORA`.
- `Lote`: representa o residuo a ser acompanhado, sua quantidade, tipo, empresa geradora e status atual.
- `HistoricoLote`: registra alteracoes de status do lote, usuario responsavel, observacao e data/hora.
- `Transporte`: vincula lote, transportadora e receptora, controlando a movimentacao logistica e o recebimento final.

Enums principais:

- `Perfil`: `ADMIN`, `GERADORA`, `TRANSPORTADORA`, `RECEPTORA`.
- `TipoEmpresa`: `GERADORA`, `TRANSPORTADORA`, `RECEPTORA`.
- `StatusLote`: `AGUARDANDO_COLETA`, `EM_TRANSITO`, `DESCARTADO`, `CANCELADO`.
- `StatusTransporte`: `PENDENTE`, `ACEITO`, `EM_TRANSITO`, `CONCLUIDO`, `RECUSADO`, `RECEBIMENTO_RECUSADO`, `CANCELADO`.

## Funcionalidades

- Autenticacao por e-mail e senha com token JWT.
- Cadastro publico de usuarios operacionais, com aceite de termos de uso.
- Bloqueio de cadastro publico com perfil administrador.
- Controle de acesso por perfis: Administrador, Geradora, Transportadora e Receptora.
- Cadastro, consulta paginada, busca e exclusao administrativa de empresas.
- Consulta de CNPJ via BrasilAPI no frontend.
- Cadastro, consulta paginada, busca e atualizacao de status de lotes.
- Historico de alteracoes de status dos lotes.
- Cadastro de transportes vinculando lote, transportadora e receptora.
- Notificacao operacional por e-mail quando um transporte e criado.
- Atualizacao de status do transporte pela transportadora.
- Confirmacao ou recusa de recebimento final pela empresa receptora.
- Atualizacao automatica do lote conforme o status do transporte.
- Geracao de manifesto interno de transporte em PDF.
- Dashboard e relatorios operacionais com indicadores.
- Exportacao CSV em telas operacionais do frontend.
- Cache simples de dados no frontend para reduzir chamadas repetidas.
- Exclusao logica de usuario com inativacao e criptografia/anonimizacao de dados pessoais.

## Perfis e permissoes

- `ADMIN`: possui visao ampla, consulta usuarios ativos e executa operacoes administrativas, como exclusoes.
- `GERADORA`: cadastra lotes e solicita/coleta transportes para seus residuos.
- `TRANSPORTADORA`: acompanha transportes associados e atualiza status operacionais.
- `RECEPTORA`: confirma ou recusa o recebimento final do residuo.

As principais permissoes HTTP sao configuradas em `SecurityConfig`:

- `/auth/**` e `/`: acesso publico.
- `GET /empresas`, `GET /lotes`, `GET /transportes`, `GET /dashboard/**`, `GET /relatorios/**`: perfis operacionais autenticados.
- `POST /lotes` e `POST /transportes`: `ADMIN` e `GERADORA`.
- `PATCH /transportes/**`: `ADMIN` e `TRANSPORTADORA`, exceto recebimento final.
- `PATCH /transportes/{id}/recebimento-final` e `/recusar`: `ADMIN` e `RECEPTORA`.
- `GET /usuarios` e `DELETE /usuarios/{id}`: `ADMIN`.
- `DELETE /usuarios/me`: usuario autenticado.

## Fluxo operacional

1. O usuario autentica-se no sistema.
2. A empresa geradora cadastra um lote de residuo.
3. O sistema registra o lote com status inicial `AGUARDANDO_COLETA`.
4. A geradora cria um transporte vinculando lote, transportadora e receptora.
5. O transporte nasce como `PENDENTE` e o lote permanece disponivel para acompanhamento.
6. A transportadora pode atualizar o transporte para `ACEITO` e depois `EM_TRANSITO`.
7. Ao entrar em transito, o lote passa automaticamente para `EM_TRANSITO`.
8. A receptora confirma o recebimento final pelo endpoint especifico.
9. O transporte passa para `CONCLUIDO` e o lote para `DESCARTADO`.
10. Se o recebimento for recusado, o transporte passa para `RECEBIMENTO_RECUSADO` e o lote retorna para `AGUARDANDO_COLETA`.
11. O historico do lote registra as movimentacoes relevantes com status anterior, status novo, usuario, observacao e data/hora.
12. O manifesto interno em PDF pode ser gerado para apoio operacional.

## Principais endpoints

| Metodo | Endpoint | Finalidade |
| --- | --- | --- |
| `POST` | `/auth/login` | Autentica usuario e retorna JWT, nome e perfil. |
| `POST` | `/auth/cadastro` | Cadastra usuario operacional, sem permitir perfil `ADMIN`. |
| `GET` | `/empresas` | Lista empresas com busca, limite e paginacao opcional. |
| `GET` | `/empresas/{publicId}` | Busca empresa por identificador publico. |
| `POST` | `/empresas` | Cadastra empresa. |
| `DELETE` | `/empresas/{publicId}` | Remove empresa, restrito a administrador. |
| `GET` | `/lotes` | Lista lotes com busca, limite e paginacao opcional. |
| `POST` | `/lotes` | Cadastra lote de residuo. |
| `GET` | `/lotes/{publicId}` | Busca lote por identificador publico. |
| `PATCH` | `/lotes/{publicId}/status` | Atualiza status do lote com observacao opcional. |
| `GET` | `/lotes/{publicId}/historico` | Lista historico de status do lote. |
| `GET` | `/transportes` | Lista transportes com busca, limite e paginacao opcional. |
| `POST` | `/transportes` | Cria transporte vinculado a lote, transportadora e receptora. |
| `GET` | `/transportes/{publicId}` | Busca transporte por identificador publico. |
| `PATCH` | `/transportes/{publicId}/status` | Atualiza status operacional do transporte. |
| `PATCH` | `/transportes/{publicId}/recebimento-final` | Confirma recebimento final pela receptora. |
| `PATCH` | `/transportes/{publicId}/recebimento-final/recusar` | Registra recusa de recebimento final pela receptora. |
| `GET` | `/transportes/lote/{lotePublicId}` | Lista transportes associados a um lote. |
| `GET` | `/transportes/{publicId}/manifesto` | Gera manifesto interno em PDF. |
| `GET` | `/dashboard/resumo` | Retorna indicadores resumidos para o dashboard. |
| `GET` | `/relatorios/resumo` | Retorna indicadores consolidados para relatorios. |
| `GET` | `/usuarios` | Lista usuarios ativos, restrito a administrador. |
| `DELETE` | `/usuarios/me` | Executa exclusao logica da propria conta. |
| `DELETE` | `/usuarios/{publicId}` | Executa exclusao logica administrativa. |

## Frontend

As telas principais do frontend estao em `front-end/src/views`:

- `LoginView.vue`: login, cadastro e aceite de termos.
- `DashboardView.vue`: indicadores e graficos resumidos.
- `EmpresasView.vue`: cadastro, busca, consulta de CNPJ, listagem e detalhe de empresas.
- `LotesView.vue`: cadastro, listagem, busca, detalhe, exportacao e acompanhamento de lotes.
- `TransportesView.vue`: solicitacao de coleta, acompanhamento de status, recebimento final, recusa e manifesto PDF.
- `RelatoriosView.vue`: indicadores consolidados e bases exportaveis.

Componentes e utilitarios relevantes:

- `services/api.js`: configuracao Axios, URL base e interceptadores de autenticacao.
- `services/dataCache.js`: cache simples de chamadas operacionais.
- `composables/useMascara.js`: mascaras de exibicao e entrada.
- `composables/useValidacao.js`: validacoes de formularios.
- `utils/exportCsv.js`: exportacao de dados em CSV.

## Seguranca e validacao

O projeto implementa medidas de seguranca, consistencia e rastreabilidade:

- senhas armazenadas com BCrypt;
- autenticacao JWT stateless;
- validacao de token a cada requisicao protegida;
- controle de acesso por perfil no Spring Security;
- CORS restrito a `localhost:5173`, `ecotrack.vercel.app` e `ecotrack-khaki.vercel.app`;
- variaveis de ambiente para segredos e configuracoes sensiveis;
- identificadores publicos em UUID para empresas, lotes, transportes e usuarios;
- criptografia em repouso de dados sensiveis como nome/e-mail de usuario e CNPJ/e-mail/telefone/endereco de empresas;
- hashes normalizados para busca e unicidade de e-mail e CNPJ sem depender do valor em texto claro;
- verificacao de usuario ativo no carregamento de autenticacao;
- aceite obrigatorio de termos de uso no cadastro;
- bloqueio de cadastro publico com perfil administrador;
- exclusao logica de usuario com inativacao e criptografia/anonimizacao de dados pessoais;
- resposta padronizada de erro em JSON no formato `{ "erro": "mensagem" }`;
- validacao de campos obrigatorios, tamanhos maximos e formatos esperados;
- rejeicao de tags HTML/scripts em campos textuais livres;
- bloqueio de quantidades negativas, zeradas ou acima da precisao aceita pelo banco.

## Banco de dados e migrations

O banco relacional PostgreSQL e versionado com Flyway. As migrations atuais cobrem:

- criacao das tabelas iniciais;
- criacao de transporte;
- observacao de transporte;
- exclusao logica de usuario;
- identificadores publicos (`public_id`);
- hashes para dados sensiveis;
- vinculo entre usuario e empresa;
- indices de performance;
- `public_id` de usuario;
- limpeza de hashes de e-mails de usuarios inativos;
- aceite de termos de uso.

## Estrutura do projeto

```text
.
|-- src/                       # Backend Spring Boot
|   |-- main/java/...           # Codigo-fonte da API
|   |-- main/resources/         # Configuracoes e migrations Flyway
|   `-- test/java/...           # Testes automatizados
|-- front-end/                  # Frontend Vue.js
|   |-- src/                    # Codigo-fonte do frontend
|   |-- public/                 # Imagens e arquivos publicos
|   `-- package.json
|-- scripts/                    # Scripts auxiliares
|-- pom.xml                     # Configuracao Maven do backend
`-- README.md
```

## Variaveis de ambiente

Backend:

```text
SPRING_DATASOURCE_URL
JWT_SECRET
JWT_EXPIRATION
LGPD_CRYPTO_SECRET
SPRING_PROFILES_ACTIVE
BRASILAPI_URL
RESEND_API_KEY
RESEND_FROM_EMAIL
```

Observacoes:

- `JWT_EXPIRATION` possui padrao de `86400000` ms quando nao informado.
- `LGPD_CRYPTO_SECRET` usa `JWT_SECRET` como fallback quando nao informado.
- `SPRING_PROFILES_ACTIVE` usa `dev` como fallback.
- `BRASILAPI_URL` usa `https://brasilapi.com.br/api` como fallback.

Frontend:

```text
VITE_API_BASE_URL
```

Quando `VITE_API_BASE_URL` nao e informada, o frontend usa a API publicada em `https://api-ecotrack.duckdns.org`.

## Execucao local

Backend:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

Frontend:

```bash
cd front-end
npm install
npm run dev
```

## Testes e validacoes

Backend:

```bash
./mvnw test
```

Frontend:

```bash
cd front-end
npm run build
npm audit --audit-level=moderate
```

O backend possui testes automatizados para controllers, services, seguranca, validacoes, manifesto PDF, indicadores e rotinas de apoio. O frontend possui validacoes de formulario, mascaras e verificacoes de sessao no proprio codigo da aplicacao.

## Observacoes academicas

O EcoTrack e uma solucao academica funcional voltada ao apoio da rastreabilidade operacional interna. O manifesto em PDF gerado pelo sistema e um documento interno de apoio e nao substitui o MTR oficial ou documentos emitidos por sistemas autorizados.

Como evolucoes futuras, o sistema pode receber isolamento completo por empresa em todos os fluxos, rate limiting em autenticacao, armazenamento de token em cookie HttpOnly/Secure e integracao com sistemas oficiais, caso exista disponibilidade tecnica e normativa para isso.
