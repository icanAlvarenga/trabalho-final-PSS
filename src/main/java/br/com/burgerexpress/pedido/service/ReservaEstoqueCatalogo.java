package br.com.burgerexpress.pedido.service;

import br.com.burgerexpress.catalogo.service.ProdutoService;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
class ReservaEstoqueCatalogo implements ReservaEstoquePedido {

    private final ProdutoService produtoService;

    ReservaEstoqueCatalogo(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public BigDecimal reservarEstoque(Long produtoId, int quantidade) {
        return produtoService.reservarEstoque(produtoId, quantidade).preco();
    }
}
