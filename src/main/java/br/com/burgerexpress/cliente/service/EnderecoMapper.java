package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.dto.EnderecoRequest;
import br.com.burgerexpress.cliente.dto.EnderecoResponse;
import br.com.burgerexpress.cliente.dto.EnderecoResumo;
import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.cliente.entity.Endereco;

/** Abstração (DIP) da conversão entre DTOs e a entidade Endereco. */
public interface EnderecoMapper {

    Endereco toEntity(Cliente cliente, EnderecoRequest request);

    EnderecoResponse toResponse(Endereco endereco);

    EnderecoResumo toResumo(Endereco endereco);
}
