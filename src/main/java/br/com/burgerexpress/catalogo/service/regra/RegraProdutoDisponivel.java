package br.com.burgerexpress.catalogo.service.regra;

import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.shared.exception.ConflitoException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class RegraProdutoDisponivel implements RegraReservaEstoque {

    @Override
    public void validar(Produto produto, Integer quantidade) {
        if (!produto.isDisponivel()) {
            throw new ConflitoException(
                    "O produto '" + produto.getNome() + "' está indisponível para reserva.");
        }
    }
}