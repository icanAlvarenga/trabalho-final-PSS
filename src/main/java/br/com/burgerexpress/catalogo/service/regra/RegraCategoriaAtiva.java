package br.com.burgerexpress.catalogo.service.regra;

import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.shared.exception.ConflitoException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class RegraCategoriaAtiva implements RegraReservaEstoque {

    @Override
    public void validar(Produto produto, Integer quantidade) {
        if (!produto.getCategoria().isAtiva()) {
            throw new ConflitoException(
                    "A categoria do produto '" + produto.getNome() + "' está inativa.");
        }
    }
}