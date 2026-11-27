package br.com.burgerexpress.catalogo.service.regra;

import br.com.burgerexpress.catalogo.entity.Produto;

public interface RegraReservaEstoque {

    void validar(Produto produto, Integer quantidade);
}