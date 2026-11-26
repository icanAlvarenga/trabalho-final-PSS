package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.dto.ClienteRequest;
import br.com.burgerexpress.cliente.dto.ClienteResponse;
import br.com.burgerexpress.cliente.dto.ClienteResumo;
import br.com.burgerexpress.cliente.entity.Cliente;
import org.springframework.stereotype.Component;

/** Implementação padrão do ClienteMapper. */
@Component
public class ClienteMapperImpl implements ClienteMapper {

    @Override
    public String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    @Override
    public Cliente toEntity(ClienteRequest request) {
        Cliente cliente = new Cliente(request.nome().trim(), normalizarEmail(request.email()), request.telefone());
        cliente.setAtivo(request.ativo() == null || request.ativo());
        return cliente;
    }

    @Override
    public void atualizar(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.nome().trim());
        cliente.setEmail(normalizarEmail(request.email()));
        cliente.setTelefone(request.telefone());
        if (request.ativo() != null) {
            cliente.setAtivo(request.ativo());
        }
    }

    @Override
    public ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getEmail(),
                cliente.getTelefone(), cliente.isAtivo());
    }

    @Override
    public ClienteResumo toResumo(Cliente cliente) {
        return new ClienteResumo(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.isAtivo());
    }
}
