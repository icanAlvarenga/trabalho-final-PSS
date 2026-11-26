package br.com.burgerexpress.catalogo.service.regra;

import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.shared.exception.DadosInvalidosException;
import org.springframework.stereotype.Component;

@Component
public class RegraProdutoDisponivel implements RegraBaixaEstoque {

    @Override
    public void validar(Produto produto, int quantidade) {
        if (!produto.isDisponivel()) {
            throw new DadosInvalidosException(
                    "O produto '" + produto.getNome() + "' está indisponível para baixa de estoque.");
        }
    }
}