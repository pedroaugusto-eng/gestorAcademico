# Gestor Acadêmico

Sistema de gestão acadêmica em Java 17 + Spring Boot 3.5 (Spring Security 6.5), com front-end em HTML, CSS e JavaScript puro servido pela própria aplicação.

O aluno cria o acesso, entra com o **RA ou o CPF**, matricula-se nas disciplinas, lança as notas e registra as faltas. O painel mostra quantos pontos faltam para a aprovação e quantas aulas ainda pode faltar.

## Como rodar

Requisito: JDK 17 ou mais novo.

```bash
cd gestorAcademico-backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Abra http://localhost:8080, clique em **Criar acesso**, cadastre um aluno e use o painel.
O banco é H2 em memória: os dados somem quando a aplicação para. As disciplinas vêm do `data.sql`.

Para demonstrar sem cadastrar ninguém, suba no perfil `demo`. Ele cria um aluno de teste com o RA que está em `src/main/resources/application-demo.properties`. A senha vem da variável de ambiente `GESTOR_DEMO_SENHA` (sem ela, vale o texto de exemplo do arquivo):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

O perfil `demo` fica desligado por padrão e nunca deve ser usado em produção.

Testes:

```bash
./mvnw test
```

## O que mudou

### 1. Spring Security 6

- `pom.xml` saiu do Spring Boot 4.1.2 (que traz o Security 7, a linha mais nova) para o **Boot 3.5.16 com Security 6.5**.
- O `maven-compiler-plugin` estava com `source/target 6`, o que não compila no Java 17; a versão agora vem de `java.version`.
- Configuração em `security/SecurityConfig.java`:
  - Login por formulário em `POST /api/auth/login` (campos `identificador` e `senha`), respondendo JSON em vez de redirecionar.
  - Sessão no servidor: cookie `JSESSIONID` com `HttpOnly` e `SameSite=Strict`, expira em 30 min.
  - Proteção CSRF por cookie `XSRF-TOKEN` (lido pelo JavaScript e devolvido no cabeçalho `X-XSRF-TOKEN`).
  - 401, 403 e 429 em JSON no formato Problem Details.
  - Cabeçalhos de segurança: Content-Security-Policy restrita a `'self'`, `X-Frame-Options: DENY`, `Referrer-Policy`.
  - Proteção contra força bruta: 5 senhas erradas para o mesmo aluno bloqueiam o login por 15 minutos. RA e CPF somam na mesma contagem. Identificadores inexistentes também são contados, para não revelar quem está cadastrado.

### 2. Senha com BCrypt

- `BCryptPasswordEncoder` com custo 12. A coluna guarda só o hash (`Aluno.senhaHash`).
- A senha nunca sai na resposta, não aparece no `toString()` do DTO e o hash é apagado da sessão depois do login (`CredentialsContainer`).
- Limite de 72 bytes validado no cadastro (é o máximo que o BCrypt considera).

### 3. Login por RA ou CPF

- `security/AlunoUserDetailsService.java`: tenta primeiro como RA; se não achar e o texto tiver 11 dígitos, tenta como CPF.
- CPF aceito com ou sem pontuação (guardado só com dígitos). RA sem espaços e em maiúsculas.
- O cadastro impede que o RA de um aluno seja igual ao CPF de outro, para o login nunca ficar ambíguo.
- CPF validado com dígito verificador (`@CPF` do Hibernate Validator).

### 4. Estrutura e padrões de projeto

```
br.edu.ucsal.pooalab.gestoracademico
├── controller     REST, sem regra de negócio e sem try/catch
├── service        regras e transações
├── repository     Spring Data JPA
├── domain
│   ├── entity     entidades com comportamento (Matricula.registrarFalta, Disciplina.limiteDeFaltas)
│   ├── dtos       records de entrada e saída, com Bean Validation
│   ├── mapper     conversão entidade ↔ DTO
│   ├── strategy   regra de aprovação (CalculadoraMediaStrategy)
│   └── Identificadores.java   normalização de RA, CPF e e-mail
├── security       Spring Security
└── exception      exceções de negócio e o tratador global
```

| Padrão | Onde | Para quê |
|---|---|---|
| Camadas (Controller → Service → Repository) | pacotes acima | cada classe com uma responsabilidade |
| DTO | `domain/dtos` | a API nunca expõe entidade nem senha |
| Mapper | `domain/mapper` | conversão num lugar só |
| Strategy | `domain/strategy` | trocar a regra de aprovação sem mexer no resto |
| Adapter | `security/AlunoAutenticado` | adapta o `Aluno` ao `UserDetails` do Spring |
| Modelo de domínio rico | `Matricula`, `Disciplina` | a regra de faltas mora na entidade |
| Tratamento centralizado de erros | `exception/TratadorGlobalDeErros` | substitui os `try/catch` repetidos nos controllers |

Outras melhorias:

- `@Valid` em todas as entradas (antes a validação dos DTOs não era aplicada).
- Erros inesperados não devolvem mais `e.getMessage()` ao cliente; a mensagem vai só para o log.
- `@EntityGraph` na listagem de matrículas (evita o problema N+1) e `open-in-view=false`.
- Matrícula repetida na mesma disciplina é barrada (regra no service e restrição única no banco).
- `spring.sql.init.encoding=UTF-8`: no Java 17 do Windows, o `data.sql` era lido em Cp1252 e os acentos das disciplinas quebravam.
- Removidos `AvaliacaoService` e `AvaliacaoRepository`, que não eram usados; a `Matricula` cria a `Avaliacao`.

### 5 e 6. Front-end

Em `src/main/resources/static`, sem framework e sem build:

- `index.html` + `js/entrar.js`: entrar (RA ou CPF) e criar acesso, com erros por campo, máscara de CPF e botão para mostrar a senha.
- `painel.html` + `js/painel.js`: o portal do aluno, com três telas escolhidas pelo endereço (o botão voltar do navegador funciona):
  - `#catalogo`: todas as disciplinas com curso, carga horária, total de aulas e limite de faltas, e o botão **Matricular-se**. Quem ainda não tem matrícula começa por aqui.
  - `#minhas`: lista curta das disciplinas matriculadas, com a situação e o botão **Ver detalhes**.
  - `#disciplina/{id}`: notas agrupadas por unidade (com **Alterar** e **Excluir** na própria linha), média de 0 a 10 com a marca da média 6, faltas com confirmação antes da falta que reprova e **Desfazer última falta**.

## Regras acadêmicas

- **Notas:** o semestre tem 2 unidades. Cada avaliação tem `unidade` (1 ou 2) e a nota da unidade é a soma do que foi lançado nela, até **10 pontos** (ex.: prova 7 + atividade 3, ou uma prova de 10 sozinha). Cada unidade aceita no máximo uma prova e uma atividade; para corrigir, usa-se Alterar.
- **Média:** (1ª unidade + 2ª unidade) / 2, de 0 a 10. Aprovação com **média 6**. Com 4,5 na 1ª unidade, o aluno precisa de 7,5 na 2ª. A regra fica em `CalculadoraUcsalStrategy`.
- **Faltas:** cada falta registrada conta 2 aulas; o limite é 25% da carga horária (60h = 12 aulas, 90h = 18). A falta **pode passar do limite** para o registro ficar completo, e aí a matrícula fica **reprovada por falta**, mesmo que a média tenha sido atingida.
- `js/api.js`: cliente `fetch` da API (sessão, CSRF e erros num lugar só).
- `css/estilo.css`: layout responsivo, funciona no celular.

Tudo que vem do servidor entra na tela por `textContent` (nunca `innerHTML`), o que evita XSS. Não há script nem estilo inline, para respeitar a CSP.

## Rotas da API

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/alunos` | público | cadastro (`nome`, `cpf`, `ra`, `email`, `senha`) |
| POST | `/api/auth/login` | público | formulário `identificador` (RA ou CPF) + `senha` |
| POST | `/api/auth/logout` | logado | encerra a sessão |
| GET | `/api/auth/csrf` | público | garante o cookie `XSRF-TOKEN` |
| GET | `/api/alunos/me` | logado | dados do aluno logado |
| GET | `/api/disciplinas` | logado | disciplinas disponíveis |
| GET | `/api/matriculas` | logado | matrículas do aluno logado |
| POST | `/api/matriculas` | logado | `{ "disciplinaId": 2 }` |
| POST | `/api/matriculas/{id}/avaliacoes` | logado | `{ "nota": 8.5, "tipo": "PROVA", "unidade": 1 }` |
| PUT | `/api/matriculas/{id}/avaliacoes/{avaliacaoId}` | logado | corrige uma nota lançada errada |
| DELETE | `/api/matriculas/{id}/avaliacoes/{avaliacaoId}` | logado | exclui uma nota |
| POST | `/api/matriculas/{id}/faltas` | logado | registra 2 aulas de falta (pode passar do limite) |
| DELETE | `/api/matriculas/{id}/faltas` | logado | desfaz a última falta registrada |

O aluno é sempre o da sessão: o id dele não vai mais na URL. Matrícula de outro aluno responde 404.

**Atenção, mudança de contrato:** as rotas antigas (`/aluno`, `/matricula/{alunoId}/{disciplinaId}`, etc.) foram trocadas pelas de cima. O antigo `GET /aluno` listava todos os alunos com CPF para qualquer pessoa e foi substituído por `GET /api/alunos/me`. O `GET /matricula/{aluno}` também tinha um bug: o nome da variável da rota não batia com o `@PathVariable alunoId`.

## Testes (26, todos passando)

- `SegurancaIntegracaoTest`: BCrypt no banco, login por RA, login por CPF com e sem pontuação, senha errada, mesma mensagem para aluno inexistente, bloqueio por força bruta (inclusive alternando RA e CPF), logout, rotas exigindo login, CSRF, cabeçalhos de segurança, isolamento entre alunos, matrícula, notas por unidade (máximo de 10, uma prova e uma atividade por unidade), média, faltas além do limite com reprovação, desfazer falta, alterar e excluir nota (inclusive a tentativa de mexer na nota de outro aluno) e os dados do catálogo.
- `FluxoDoNavegadorTest`: sobe o servidor de verdade e repete o caminho do front com cookies reais.
- `TentativasDeLoginTest`: bloqueio e liberação após 15 minutos.
- `CalculadoraUcsalStrategyTest`: o teste que já existia, mais o da média.

## Pontos para confirmar

1. **Classes da Strategy:** `CalculadoraMediaStrategy` e `CalculadoraUcsalStrategy` não estavam no repositório, só o teste. Elas foram escritas a partir dele e da regra da UCSAL (2 unidades de 10, média 6). Se essas classes existirem na sua máquina, compare antes de juntar.
2. **Banco existente:** a tabela `tb_avaliacoes` ganhou a coluna `unidade`. Com H2 em memória isso é automático; num banco persistente, avaliações antigas precisam receber a unidade.
3. **`data.sql`:** duas disciplinas estão com o mesmo código `CST309` (Lógica e POO). Foi mantido como estava.
4. **Console do H2:** a dependência `spring-boot-h2console` era do Boot 4 e foi retirada. Para usar o console no Boot 3, adicione `spring.h2.console.enabled=true` e libere `/h2-console/**` no `SecurityConfig`, só em desenvolvimento.

## Antes de ir para produção

- Rodar atrás de HTTPS e ligar `server.servlet.session.cookie.secure=true`.
- Trocar o H2 em memória por um banco persistente e `ddl-auto` por migrações (Flyway).
- O bloqueio de força bruta fica em memória: com mais de uma instância, mover para Redis ou para o banco.
