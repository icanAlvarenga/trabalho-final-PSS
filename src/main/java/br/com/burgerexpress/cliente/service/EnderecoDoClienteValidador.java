package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.dto.EnderecoResumo;

/**
 * Contrato PÚBLICO (ISP) do módulo cliente, consumido pelo módulo de Pedidos.
 * Devolve um DTO: entidades JPA não saem do módulo.
 * Contrato: retorna o resumo do endereço se ele existir E pertencer ao cliente informado;
 * caso contrário lança RecursoNaoEncontradoException (404); nunca retorna null.
 */
public interface EnderecoDoClienteValidador {

    EnderecoResumo validarEnderecoDoCliente(Long clienteId, Long enderecoId);
}
