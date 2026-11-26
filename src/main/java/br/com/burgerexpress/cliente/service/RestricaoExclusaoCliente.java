package br.com.burgerexpress.cliente.service;

/**
 * Ponto de extensão (OCP/DIP): outros módulos (ex.: Pedidos) registram uma implementação como bean
 * para impedir a exclusão de clientes com histórico, sem alterar o ClienteService.
 * Enquanto nenhuma implementação existir, nenhuma restrição é aplicada.
 */
public interface RestricaoExclusaoCliente {

    boolean impedeExclusao(Long clienteId);
}
