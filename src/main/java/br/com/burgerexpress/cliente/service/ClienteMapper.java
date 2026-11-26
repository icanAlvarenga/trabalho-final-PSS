package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.dto.ClienteRequest;
import br.com.burgerexpress.cliente.dto.ClienteResponse;
import br.com.burgerexpress.cliente.dto.ClienteResumo;
import br.com.burgerexpress.cliente.entity.Cliente;

/** Abstração (DIP) da conversão entre DTOs e a entidade Cliente. */
public interface ClienteMapper {

    /** Remove espaços e converte para minúsculas, para a unicidade não depender de caixa. */
    String normalizarEmail(String email);

    Cliente toEntity(ClienteRequest request);

    void atualizar(Cliente cliente, ClienteRequest request);

    ClienteResponse toResponse(Cliente cliente);

    ClienteResumo toResumo(Cliente cliente);
}
