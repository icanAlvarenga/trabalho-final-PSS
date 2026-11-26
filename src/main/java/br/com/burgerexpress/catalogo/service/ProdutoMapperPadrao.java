package br.com.burgerexpress.catalogo.service;

import br.com.burgerexpress.catalogo.dto.ProdutoRequest;
import br.com.burgerexpress.catalogo.dto.ProdutoResponse;
import br.com.burgerexpress.catalogo.dto.ProdutoResumo;
import br.com.burgerexpress.catalogo.entity.Categoria;
import br.com.burgerexpress.catalogo.entity.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoMapperPadrao implements ProdutoMapper {

    @Override
    public Produto paraEntidade(ProdutoRequest request, Categoria categoria) {
        String nome = request.nome().trim();
        String descricao = request.descricao() != null ? request.descricao().trim() : null;

        Produto produto = new Produto(
                nome,
                descricao,
                request.preco(),
                request.estoque(),
                categoria);

        if (request.disponivel() != null) {
            produto.setDisponivel(request.disponivel());
        }
        return produto;
    }

    @Override
    public ProdutoResponse paraResponse(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.isDisponivel(),
                produto.getCategoria().getId(),
                produto.getCategoria().getNome());
    }

    @Override
    public ProdutoResumo paraResumo(Produto produto) {
        return new ProdutoResumo(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.isDisponivel(),
                produto.getCategoria().getId());
    }
}