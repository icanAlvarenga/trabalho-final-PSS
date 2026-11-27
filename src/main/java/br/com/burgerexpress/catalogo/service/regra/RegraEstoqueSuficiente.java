package br.com.burgerexpress.catalogo.service.regra;

import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.shared.exception.ConflitoException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class RegraEstoqueSuficiente implements RegraReservaEstoque {

    @Override
    public void validar(Produto produto, Integer quantidade) {
        if (produto.getEstoque() < quantidade) {
            throw new ConflitoException(
                    "Estoque insuficiente para o produto '" + produto.getNome()
                            + "'. Disponível: " + produto.getEstoque()
                            + ", solicitado: " + quantidade + ".");
        }
    }
}