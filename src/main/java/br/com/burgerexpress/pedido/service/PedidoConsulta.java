package br.com.burgerexpress.pedido.service;

import br.com.burgerexpress.pedido.dto.PedidoResponse;
import java.util.List;

/** Consultas de pedidos para os módulos que só precisam dos dados de leitura. */
public interface PedidoConsulta {

    /** Retorna o pedido ou lança RecursoNaoEncontradoException. */
    PedidoResponse buscarPorId(Long pedidoId);

    /** Retorna os pedidos do cliente em ordem decrescente de ID, ou uma lista vazia. */
    List<PedidoResponse> listarPorCliente(Long clienteId);
}
