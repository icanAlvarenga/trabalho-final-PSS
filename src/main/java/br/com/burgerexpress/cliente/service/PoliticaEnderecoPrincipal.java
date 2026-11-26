package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.entity.Endereco;

/**
 * Regra de qual endereço de um cliente é o principal. Isolada do EnderecoService (SRP) e
 * substituível por outra política sem alterar o cadastro de endereços (OCP).
 * Contrato: deve ser chamada ANTES de salvar o novo endereço, dentro da transação do cadastro.
 */
public interface PoliticaEnderecoPrincipal {

    void aplicar(Long clienteId, Endereco novo);
}
