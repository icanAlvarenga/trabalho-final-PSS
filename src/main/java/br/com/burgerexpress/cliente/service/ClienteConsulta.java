package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.dto.ClienteResumo;

/**
 * Contrato PÚBLICO (ISP) do módulo cliente, consumido pelo módulo de Pedidos.
 * Devolve um DTO: entidades JPA não saem do módulo.
 * Contrato: retorna o resumo do cliente existente ou lança RecursoNaoEncontradoException (404);
 * nunca retorna null.
 */
public interface ClienteConsulta {

    ClienteResumo buscarPorId(Long clienteId);
}
