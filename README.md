# EcoTrack

Sistema web para gestao e rastreabilidade operacional de residuos solidos, desenvolvido como Projeto Final de Curso em Bacharelado em Sistemas de Informacao.

O EcoTrack centraliza o cadastro de empresas geradoras, transportadoras e receptoras, o controle de lotes de residuos, o registro de transportes, a confirmacao de recebimento final, a geracao de manifesto interno em PDF e a visualizacao de indicadores gerenciais.

## Objetivo

O objetivo do projeto e oferecer uma solucao web para apoiar o acompanhamento interno do ciclo operacional de residuos solidos, desde a geracao do lote ate a confirmacao de recebimento pela empresa receptora.

A Politica Nacional de Residuos Solidos (PNRS) e utilizada como referencia conceitual para contextualizar a importancia da responsabilidade compartilhada, da gestao integrada e da rastreabilidade. O EcoTrack nao substitui sistemas oficiais, como SINIR/MTR, nem tem a finalidade de comprovar atendimento legal.

## Acessos

- Frontend: https://ecotrack-khaki.vercel.app
- Backend API: https://ecotrack-d5i0.onrender.com

As credenciais de acesso nao sao versionadas neste repositorio. Usuarios e senhas devem ser gerenciados apenas pelos responsaveis do projeto ou pelo ambiente de producao.

## Tecnologias

- Backend: Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA e Flyway.
- Frontend: Vue.js, Tailwind CSS, Axios, Chart.js e Vite.
- Banco de dados: PostgreSQL em nuvem.
- Hospedagem: Render para a API e Vercel para o frontend.
- Integracoes: BrasilAPI para consulta de CNPJ e Resend para notificacoes operacionais por e-mail.
- Documentos: OpenPDF para geracao de manifesto interno em PDF.

## Arquitetura

O projeto segue uma arquitetura em camadas:

- `controller`: recebe as requisicoes HTTP e expoe os endpoints REST.
- `service`: concentra as regras de negocio.
- `repository`: realiza o acesso ao banco de dados por meio do Spring Data JPA.
- `entity`: representa as tabelas e relacionamentos do banco.
- `security`: implementa autenticacao JWT e controle de acesso.

O frontend consome a API REST autenticada e armazena o token JWT para enviar nas requisicoes protegidas.

## Funcionalidades

- Autenticacao por e-mail e senha com token JWT.
- Cadastro publico de usuarios operacionais, sem liberacao de perfil administrador.
- Controle de acesso por perfis: Administrador, Geradora, Transportadora e Receptora.
- Cadastro e consulta de empresas.
- Consulta de CNPJ via BrasilAPI no frontend.
- Cadastro de lotes de residuos.
- Historico de alteracoes de status dos lotes.
- Cadastro de transportes vinculando lote, transportadora e receptora.
- Notificacao operacional por e-mail quando um transporte e criado.
- Confirmacao de recebimento final pela empresa receptora.
- Atualizacao automatica do lote para descartado ao concluir o recebimento.
- Geracao de manifesto interno de transporte em PDF.
- Dashboard e relatorios operacionais.
- Exclusao logica de usuario com inativacao e criptografia de dados pessoais.

## Perfis de usuario

- `ADMIN`: possui visao ampla do sistema e acesso administrativo.
- `GERADORA`: pode cadastrar lotes e iniciar transportes.
- `TRANSPORTADORA`: acompanha transportes e atualiza status operacionais.
- `RECEPTORA`: confirma o recebimento final do residuo.

## Fluxo operacional

1. O usuario autentica-se no sistema.
2. A empresa geradora cadastra um lote de residuo.
3. O sistema registra o lote com status inicial `AGUARDANDO_COLETA`.
4. A geradora cria um transporte vinculando lote, transportadora e receptora.
5. A transportadora atualiza o transporte para `EM_TRANSITO`.
6. O lote passa automaticamente para `EM_TRANSITO`.
7. A receptora confirma o recebimento final.
8. O transporte passa para `CONCLUIDO` e o lote para `DESCARTADO`.
9. O historico do lote registra as movimentacoes relevantes.
10. O manifesto interno em PDF pode ser gerado para apoio operacional.

## Seguranca e validacao

O projeto implementa medidas basicas de seguranca e consistencia:

- senhas armazenadas com BCrypt;
- autenticacao JWT;
- controle de acesso por perfil no Spring Security;
- CORS restrito a origens conhecidas;
- variaveis de ambiente para segredos e configuracoes sensiveis;
- identificadores publicos em UUID para empresas, lotes e transportes expostos pela API;
- bloqueio de cadastro publico com perfil administrador;
- exclusao logica de usuario com criptografia de dados pessoais;
- resposta padronizada de erro em JSON no formato `{ "erro": "mensagem" }`;
- validacao de campos obrigatorios, tamanhos maximos e formatos esperados;
- rejeicao de tags HTML/scripts em campos textuais livres;
- bloqueio de quantidades negativas, zeradas ou acima da precisao aceita pelo banco.

## Estrutura do projeto

```text
.
|-- src/                       # Backend Spring Boot
|   |-- main/java/...           # Codigo-fonte da API
|   |-- main/resources/         # Configuracoes e migrations Flyway
|   `-- test/java/...           # Testes automatizados
|-- front-end/                 # Frontend Vue.js
|   |-- src/                   # Codigo-fonte do frontend
|   |-- public/                # Imagens e arquivos publicos
|   `-- package.json
|-- scripts/                   # Scripts auxiliares
|-- pom.xml                    # Configuracao Maven do backend
`-- README.md
```

## Variaveis de ambiente

Backend:

```text
SPRING_DATASOURCE_URL
JWT_SECRET
JWT_EXPIRATION
RESEND_API_KEY
RESEND_FROM_EMAIL
SPRING_PROFILES_ACTIVE
```

Frontend:

```text
VITE_API_BASE_URL
```

Quando `VITE_API_BASE_URL` nao e informada, o frontend usa a API publicada no Render.

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

## Testes

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

## Observacoes academicas

O EcoTrack e uma solucao academica funcional voltada ao apoio da rastreabilidade operacional interna. O manifesto em PDF gerado pelo sistema e um documento interno de apoio e nao substitui o MTR oficial ou documentos emitidos por sistemas autorizados.

Como evolucoes futuras, o sistema pode receber isolamento completo por empresa, rate limiting em autenticacao, armazenamento de token em cookie HttpOnly/Secure e integracao com sistemas oficiais, caso exista disponibilidade tecnica e normativa para isso.
