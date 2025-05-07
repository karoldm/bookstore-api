# Bookstore API

API de Gerenciamento de livrarias! Esta API foi desenvolvida utilizando Spring Boot e oferece funcionalidades para gerenciar os livros da loja e adicionar funcionários que podem fazer login na loja para ajudar no controle de estoque.

- URL do Swagger: [swagger](https://bookstore-api-0pfv.onrender.com/swagger-ui.html)

## Funcionalidades

### Autenticação e Autorização
- **Cadastro de Usuários**: Os usuários podem se cadastrar fornecendo informações básicas como nome de usuário, nome, senha e informações da loja (que incluem banner, nome da loja, slogan, etc).
- **Login**: Autenticação segura utilizando Spring Security e JWT (JSON Web Tokens) pelos administradores da loja ou pelo seus funcionários.

### Gerenciamento de Perfil
- **Atualizar Perfil**: Os usuários podem alterar o nome
- **Atualizar Senha**: Os usuários pode trocar de senha

### Gerenciamento de Livros
- Administradores da loja podem cadastrar, editar ou excluir livros da livraria.

### Gerenciamento de Funcionários
- Administradores podem cadastrar novos funcionários na loja. Esses funcionários tem um login próprio e poodem visulizar os livros e controler o estoque.

### Documentação com Swagger
- **Swagger UI**: A API está documentada utilizando Swagger, permitindo uma fácil visualização e teste dos endpoints diretamente no navegador.

### Acesso baseado em Roles
- Todas as operações são são seguras e exigem autenticação do usuário, validação da role atribuída ao usuário, bem como se ele possui relação com a loja que está operando.

## Tecnologias e Ferramentas Utilizadas

- **Spring Boot**: Framework principal para desenvolvimento da API.
- **Spring Security**: Para autenticação e autorização.
- **Spring Data JPA**: Para persistência de dados e interação com o banco de dados.
- **JWT (JSON Web Tokens)**: Para autenticação segura.
- **Swagger**: Para documentação da API.
- **Banco de Dados**: Foi utilizado um banco de dados relacional PostgreSQL para armazenar as informações necessárias.

## Como Executar o Projeto

### Pré-requisitos
- Java 17
- Maven
- Banco de dados PostgreSQL configurado com nome bookstore

### Configuração

1. **Clone o repositório**:
   ```bash
   git clone git@github.com:karoldm/bookstore-api.git
   cd bookstore-api
   ```

2. **Configure o arquivo application.properties:**
    ```bash
    spring.application.name=bookstore
    server.port=9000
    spring.datasource.url=jdbc:postgresql://localhost:5432/bookstore
    spring.datasource.username=username
    spring.datasource.password=password
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
    spring.jpa.show-sql=true
    spring.web.resources.static-locations=classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/
    api.security.token.secret=jwt_token
   ```
3. **Excecute o projeto**
   - A API estará disponível em http://localhost:9000.
   - A documentação Swagger estará disponível em http://localhost:9000/swagger-ui.html.
   - Os testes estão todos dentro da pasta /test separados por camada (controllers, Services, etc).


## Métricas do Sonarqube

![image](https://github.com/user-attachments/assets/c6935aa5-07d7-40aa-bb2d-b18c4d56f91e)

- Se você possui o Sonarqube instalado pode verificar a qualidade do sistema com o comando
```bash
  mvn clean verify sonar:sonar -Dsonar.projectKey=[project-key] -Dsonar.projectName='[project-name]' -Dsonar.host.url=http://localhost:9001 -Dsonar.token=[project-token] -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml -Dsonar.java.binaries=target/classes -Dsonar.junit.reportPaths=target/surefire-reports -Dsonar.verbose=true 
```
- Saiba mais em: [SonarQube](https://www.sonarsource.com/)!


## Diagramas do Sistema

### Casos de uso

![image](https://github.com/user-attachments/assets/21baf1ca-6247-4983-aa3e-45e26208ac4a)

### Modelagem

![image](https://github.com/user-attachments/assets/73528a27-029f-4ecd-a1d2-9f6307ce024f)


## Deploy
API hospedada no [Render](https://render.com/)

Banco de dados hospedado no [Koyeb](https://app.koyeb.com/)


## Contribuição
Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e pull request ❤️
