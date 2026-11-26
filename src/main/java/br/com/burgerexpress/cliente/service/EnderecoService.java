package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.dto.EnderecoRequest;
import br.com.burgerexpress.cliente.dto.EnderecoResponse;
import br.com.burgerexpress.cliente.dto.EnderecoResumo;
import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.cliente.entity.Endereco;
import br.com.burgerexpress.cliente.repository.EnderecoRepository;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de endereço. Só orquestra: depende de abstrações (localizador de cliente, mapper e
 * política de endereço principal) e não conhece as regras de cada uma (DIP e SRP).
 */
@Service
public class EnderecoService implements EnderecoDoClienteValidador {

    private final EnderecoRepository enderecoRepository;
    private final ClienteLocalizador clienteLocalizador;
    private final EnderecoMapper mapper;
    private final PoliticaEnderecoPrincipal politicaEnderecoPrincipal;

    public EnderecoService(EnderecoRepository enderecoRepository, ClienteLocalizador clienteLocalizador,
                           EnderecoMapper mapper, PoliticaEnderecoPrincipal politicaEnderecoPrincipal) {
        this.enderecoRepository = enderecoRepository;
        this.clienteLocalizador = clienteLocalizador;
        this.mapper = mapper;
        this.politicaEnderecoPrincipal = politicaEnderecoPrincipal;
    }

    @Transactional
    public EnderecoResponse criar(Long clienteId, EnderecoRequest request) {
        Cliente cliente = clienteLocalizador.localizar(clienteId);
        Endereco novo = mapper.toEntity(cliente, request);
        politicaEnderecoPrincipal.aplicar(clienteId, novo);
        return mapper.toResponse(enderecoRepository.saveAndFlush(novo));
    }

    @Transactional(readOnly = true)
    public List<EnderecoResponse> listar(Long clienteId) {
        clienteLocalizador.localizar(clienteId);
        List<Endereco> enderecos = enderecoRepository.findByClienteIdOrderByIdAsc(clienteId);
        List<EnderecoResponse> respostas = new ArrayList<>();
        for (Endereco endereco : enderecos) {
            respostas.add(mapper.toResponse(endereco));
        }
        return respostas;
    }

    /** Contrato público: usado pelo módulo de Pedidos; devolve DTO. */
    @Override
    @Transactional(readOnly = true)
    public EnderecoResumo validarEnderecoDoCliente(Long clienteId, Long enderecoId) {
        clienteLocalizador.localizar(clienteId);
        Optional<Endereco> encontrado = enderecoRepository.findByIdAndClienteId(enderecoId, clienteId);
        if (encontrado.isEmpty()) {
            throw new RecursoNaoEncontradoException(
                    "Endereço com id " + enderecoId + " não encontrado para o cliente " + clienteId + ".");
        }
        return mapper.toResumo(encontrado.get());
    }
}
