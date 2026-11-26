package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.entity.Cliente;

/**
 * Contrato INTERNO do módulo cliente (visibilidade de pacote de propósito): devolve a entidade JPA
 * para as classes do próprio módulo (ex.: EnderecoService, que precisa associar o endereço ao cliente).
 * Outros módulos não conseguem usá-lo; eles consomem ClienteConsulta, que devolve DTO.
 * Contrato: retorna o cliente existente ou lança RecursoNaoEncontradoException (404); nunca retorna null.
 */
interface ClienteLocalizador {

    Cliente localizar(Long clienteId);
}
