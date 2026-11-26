package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.dto.ClienteRequest;
import br.com.burgerexpress.cliente.dto.ClienteResponse;
import br.com.burgerexpress.cliente.dto.ClienteResumo;
import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.cliente.repository.ClienteRepository;
import br.com.burgerexpress.shared.exception.ConflitoException;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de cliente. Depende de abstrações (repository, mapper, restrições) via construtor.
 * Implementa o contrato público ClienteConsulta (DTO, para outros módulos) e o contrato interno
 * ClienteLocalizador (entidade, só para este módulo).
 */
@Service
public class ClienteService implements ClienteConsulta, ClienteLocalizador {

    private static final String EMAIL_DUPLICADO = "Já existe um cliente cadastrado com o e-mail informado.";

    private final ClienteRepository clienteRepository;
    private final ClienteMapper mapper;
    private final List<RestricaoExclusaoCliente> restricoesExclusao;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper mapper,
                          List<RestricaoExclusaoCliente> restricoesExclusao) {
        this.clienteRepository = clienteRepository;
        this.mapper = mapper;
        this.restricoesExclusao = restricoesExclusao;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        if (clienteRepository.existsByEmailIgnoreCase(mapper.normalizarEmail(request.email()))) {
            throw new ConflitoException(EMAIL_DUPLICADO);
        }
        Cliente salvo = clienteRepository.saveAndFlush(mapper.toEntity(request));
        return mapper.toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        List<Cliente> clientes = clienteRepository.findAll();
        List<ClienteResponse> respostas = new ArrayList<>();
        for (Cliente cliente : clientes) {
            respostas.add(mapper.toResponse(cliente));
        }
        return respostas;
    }

    @Transactional(readOnly = true)
    public ClienteResponse obter(Long id) {
        return mapper.toResponse(localizar(id));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = localizar(id);
        if (clienteRepository.existsByEmailIgnoreCaseAndIdNot(mapper.normalizarEmail(request.email()), id)) {
            throw new ConflitoException(EMAIL_DUPLICADO);
        }
        mapper.atualizar(cliente, request);
        return mapper.toResponse(clienteRepository.saveAndFlush(cliente));
    }

    @Transactional
    public void remover(Long id) {
        Cliente cliente = localizar(id);
        for (RestricaoExclusaoCliente restricao : restricoesExclusao) {
            if (restricao.impedeExclusao(id)) {
                throw new ConflitoException(
                        "Não é possível excluir o cliente: ele possui pedidos associados. Desative-o em vez de excluir.");
            }
        }
        clienteRepository.delete(cliente);
    }

    /** Contrato público: usado por outros módulos; devolve DTO. */
    @Override
    @Transactional(readOnly = true)
    public ClienteResumo buscarPorId(Long clienteId) {
        return mapper.toResumo(localizar(clienteId));
    }

    /** Contrato interno: usado só dentro do módulo cliente; devolve a entidade. */
    @Override
    @Transactional(readOnly = true)
    public Cliente localizar(Long clienteId) {
        Optional<Cliente> encontrado = clienteRepository.findById(clienteId);
        if (encontrado.isEmpty()) {
            throw new RecursoNaoEncontradoException("Cliente com id " + clienteId + " não encontrado.");
        }
        return encontrado.get();
    }
}
