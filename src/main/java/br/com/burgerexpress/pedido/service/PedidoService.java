package br.com.burgerexpress.pedido.service;

import br.com.burgerexpress.cliente.service.ClienteConsulta;
import br.com.burgerexpress.cliente.service.EnderecoDoClienteValidador;
import br.com.burgerexpress.pedido.dto.ItemPedidoRequest;
import br.com.burgerexpress.pedido.dto.ItemPedidoResponse;
import br.com.burgerexpress.pedido.dto.PedidoRequest;
import br.com.burgerexpress.pedido.dto.PedidoResponse;
import br.com.burgerexpress.pedido.entity.ItemPedido;
import br.com.burgerexpress.pedido.entity.Pedido;
import br.com.burgerexpress.pedido.repository.PedidoRepository;
import br.com.burgerexpress.shared.exception.ConflitoException;
import br.com.burgerexpress.shared.exception.DadosInvalidosException;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService implements PedidoConsulta {

    private final PedidoRepository pedidoRepository;
    private final ClienteConsulta clienteConsulta;
    private final EnderecoDoClienteValidador enderecoValidador;
    private final ReservaEstoquePedido reservaEstoque;
    private final Validator validator;

    public PedidoService(PedidoRepository pedidoRepository, ClienteConsulta clienteConsulta,
                         EnderecoDoClienteValidador enderecoValidador, ReservaEstoquePedido reservaEstoque,
                         Validator validator) {
        this.pedidoRepository = pedidoRepository;
        this.clienteConsulta = clienteConsulta;
        this.enderecoValidador = enderecoValidador;
        this.reservaEstoque = reservaEstoque;
        this.validator = validator;
    }

    /** Cria o pedido e confirma todas as reservas de estoque na mesma transação. */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PedidoResponse criarPedido(PedidoRequest request) {
        validarPedido(request);
        clienteConsulta.buscarPorId(request.clienteId());
        enderecoValidador.validarEnderecoDoCliente(request.clienteId(), request.enderecoId());

        Pedido pedido = new Pedido(request.clienteId(), request.enderecoId(), request.observacoes());
        try {
            for (ItemPedidoRequest item : request.itens()) {
                BigDecimal preco = reservaEstoque.reservarEstoque(item.produtoId(), item.quantidade());
                pedido.adicionarItem(item.produtoId(), item.quantidade(), preco);
            }
            return paraResponse(pedidoRepository.saveAndFlush(pedido));
        } catch (ConcurrencyFailureException ex) {
            throw new ConflitoException("O estoque foi alterado durante a criação do pedido. Tente novamente.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long pedidoId) {
        validarId(pedidoId, "pedidoId");
        Optional<Pedido> pedido = pedidoRepository.findById(pedidoId);
        if (pedido.isEmpty()) {
            throw new RecursoNaoEncontradoException("Pedido com id " + pedidoId + " não encontrado.");
        }
        return paraResponse(pedido.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorCliente(Long clienteId) {
        validarId(clienteId, "clienteId");
        List<PedidoResponse> respostas = new ArrayList<>();
        for (Pedido pedido : pedidoRepository.findByClienteIdOrderByIdDesc(clienteId)) {
            respostas.add(paraResponse(pedido));
        }
        return List.copyOf(respostas);
    }

    private void validarPedido(PedidoRequest request) {
        if (request == null) {
            throw new DadosInvalidosException("Informe os dados do pedido.");
        }
        Set<ConstraintViolation<PedidoRequest>> violacoes = validator.validate(request);
        if (!violacoes.isEmpty()) {
            List<String> mensagens = new ArrayList<>();
            for (ConstraintViolation<PedidoRequest> violacao : violacoes) {
                mensagens.add(violacao.getPropertyPath() + ": " + violacao.getMessage());
            }
            Collections.sort(mensagens);
            throw new DadosInvalidosException(String.join(", ", mensagens));
        }
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new DadosInvalidosException(campo + " deve ser um número inteiro positivo.");
        }
    }

    private PedidoResponse paraResponse(Pedido pedido) {
        List<ItemPedidoResponse> itens = new ArrayList<>();
        for (ItemPedido item : pedido.getItens()) {
            itens.add(new ItemPedidoResponse(item.getId(), item.getProdutoId(), item.getQuantidade(),
                    item.getPrecoUnitario(), item.getSubtotal()));
        }
        return new PedidoResponse(pedido.getId(), pedido.getClienteId(), pedido.getEnderecoId(),
                pedido.getDataCriacao(), pedido.getStatus(), pedido.getObservacoes(), pedido.getTotal(),
                List.copyOf(itens));
    }
}
