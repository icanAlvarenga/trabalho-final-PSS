package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.dto.EnderecoRequest;
import br.com.burgerexpress.cliente.dto.EnderecoResponse;
import br.com.burgerexpress.cliente.dto.EnderecoResumo;
import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.cliente.entity.Endereco;
import org.springframework.stereotype.Component;

/** Implementação padrão do EnderecoMapper. */
@Component
public class EnderecoMapperImpl implements EnderecoMapper {

    @Override
    public Endereco toEntity(Cliente cliente, EnderecoRequest request) {
        return new Endereco(cliente, request.cep(), request.rua().trim(), request.numero().trim(),
                request.bairro().trim(), request.cidade().trim(), request.complemento(),
                Boolean.TRUE.equals(request.principal()));
    }

    @Override
    public EnderecoResponse toResponse(Endereco endereco) {
        return new EnderecoResponse(endereco.getId(), endereco.getCliente().getId(), endereco.getCep(),
                endereco.getRua(), endereco.getNumero(), endereco.getBairro(), endereco.getCidade(),
                endereco.getComplemento(), endereco.isPrincipal());
    }

    @Override
    public EnderecoResumo toResumo(Endereco endereco) {
        return new EnderecoResumo(endereco.getId(), endereco.getCliente().getId(), endereco.getCep(),
                endereco.getRua(), endereco.getNumero(), endereco.getBairro(), endereco.getCidade(),
                endereco.getComplemento(), endereco.isPrincipal());
    }
}
