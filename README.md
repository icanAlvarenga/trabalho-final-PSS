Burger Express API
API REST de uma hamburgueria (Sistema de Delivery) — Projeto de Sistemas de Software, Tema 2: Arquitetura REST com Spring Boot e Postman. Somente backend; a demonstração é feita pelo Postman.

Pagamento é apenas simulado. Nenhum dado real de cartão, CVV, senha ou dado bancário é coletado ou armazenado.

Tecnologias
Java 21
Apache Maven 3.9+
Spring Boot 3.4.x (Web, Data JPA, Validation)
H2 (banco em memória) — os dados são recriados a cada execução
JUnit 5, Mockito e Spring Boot Test
Pré-requisitos
java -version   # deve indicar 21
mvn -version    # Maven 3.9+ usando Java 21

Como executar
git clone https://github.com/icanAlvarenga/trabalho-final-PSS.git
cd trabalho-final-PSS
mvn spring-boot:run
A API sobe em http://localhost:8080. URL base: http://localhost:8080/api/v1. Para reiniciar o banco (H2 em memória), basta parar e subir a aplicação novamente.

Como testar
mvn test

Endpoints implementados (Parte 1 — Clientes e Endereços)
Método	Rota	Resultado
POST	/api/v1/clientes	201 Created
GET	/api/v1/clientes	200 OK
GET	/api/v1/clientes/{id}	200 OK ou 404
PUT	/api/v1/clientes/{id}	200 OK, 400, 404 ou 409 (e-mail duplicado)
DELETE	/api/v1/clientes/{id}	204, 404 ou 409 (cliente com histórico)
POST	/api/v1/clientes/{id}/enderecos	201 Created, 400 ou 404
GET	/api/v1/clientes/{id}/enderecos	200 OK ou 404
As demais partes (catálogo, pedidos, workflow, pagamentos e entregas) complementam esta tabela.

Exemplo
POST http://localhost:8080/api/v1/clientes
Content-Type: application/json

{ "nome": "Maria Silva", "email": "maria@example.com", "telefone": "27999998888" }
Formato padronizado de erro
{
  "status": 409,
  "title": "Conflito de estado",
  "detail": "Já existe um cliente cadastrado com o e-mail informado.",
  "path": "/api/v1/clientes"
}
Postman
A coleção ficará em postman/BurgerExpress.postman_collection.json (consolidada pela Pessoa 6), com a variável baseUrl = http://localhost:8080/api/v1.

Limitações conhecidas
Sem autenticação/autorização.
Pagamento simulado (sem integração real).
Banco H2 em memória: dados não persistem entre execuções.
