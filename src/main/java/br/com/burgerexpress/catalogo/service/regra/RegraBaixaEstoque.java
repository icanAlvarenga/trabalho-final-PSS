package br.com.burgerexpress.catalogo.service.regra;

import br.com.burgerexpress.catalogo.entity.Produto;

public interface RegraBaixaEstoque {
    void validar(Produto produto, int quantidade);
}
