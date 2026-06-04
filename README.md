# EcoTrack

Sistema web para gestao e rastreabilidade de residuos solidos, desenvolvido como Projeto Final de Curso em Bacharelado em Sistemas de Informacao.

O EcoTrack apoia o cadastro de empresas geradoras, transportadoras e receptoras, o controle de lotes de residuos, o registro de transportes, a confirmacao de recebimento final pela receptora, a emissao de manifesto em PDF e a consulta de indicadores gerenciais.

## Acessos

- Frontend: https://ecotrack-khaki.vercel.app
- Backend API: https://ecotrack-d5i0.onrender.com

As credenciais de acesso nao sao versionadas neste repositorio. Usuarios e senhas devem ser gerenciados no ambiente de producao ou informados apenas pelos responsaveis do projeto.

## Tecnologias

- Backend: Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA e Flyway.
- Frontend: Vue.js, Tailwind CSS, Axios, Chart.js e Vite.
- Banco de dados: PostgreSQL em nuvem.
- Hospedagem: Render para a API e Vercel para o frontend.
- Integracoes: BrasilAPI para consulta de CNPJ e Resend para envio de e-mails operacionais.

## Funcionalidades

- Autenticacao por e-mail e senha com token JWT.
- Cadastro publico de usuarios operacionais, sem liberacao de perfil administrador.
- Controle de acesso por perfis: Administrador, Geradora, Transportadora e Receptora.
- Cadastro e consulta de empresas.
- Consulta de CNPJ via BrasilAPI no frontend.
- Cadastro de lotes de residuos com historico de status.
- Cadastro de transportes vinculando lote, transportadora e receptora.
- Confirmacao de recebimento final pela empresa receptora.
- Atualizacao automatica do lote para descartado ao concluir o recebimento.
- Manifesto de transporte em PDF.
- Dashboard e relatorios operacionais.
- Exclusao logica de usuario com inativacao e criptografia de dados pessoais.

## Estrutura do projeto

```text
.
|-- src/                       # Backend Spring Boot
|-- front-end/                 # Frontend Vue.js ativo
|   |-- src/                   # Codigo-fonte do frontend
|   |-- public/                # Imagens e arquivos publicos
|   `-- package.json
|-- scripts/                   # Scripts auxiliares
|-- pom.xml                    # Configuracao Maven do backend
`-- README.md
```

## Como executar o backend

Configure as variaveis de ambiente necessarias:

```text
SPRING_DATASOURCE_URL
JWT_SECRET
JWT_EXPIRATION
RESEND_API_KEY
RESEND_FROM_EMAIL
SPRING_PROFILES_ACTIVE
```

Depois execute:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

## Como executar o frontend

Opcionalmente, configure a URL da API:

```text
VITE_API_BASE_URL=https://ecotrack-d5i0.onrender.com
```

```bash
cd front-end
npm install
npm run dev
```

Para gerar build de producao:

```bash
cd front-end
npm run build
```

## Testes e validacao

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

## Branches de entrega

- `main`: codigo principal do projeto.
- `frontend-deploy`: branch sincronizada para publicacao do frontend na Vercel.

Para a entrega final, o repositorio deve ser congelado apos a validacao e o ZIP deve ser gerado a partir da versao final.
