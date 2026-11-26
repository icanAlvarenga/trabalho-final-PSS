package br.com.burgerexpress.catalogo.service;

import br.com.burgerexpress.catalogo.dto.CategoriaRequest;
import br.com.burgerexpress.catalogo.dto.CategoriaResponse;
import br.com.burgerexpress.catalogo.entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapperPadrao implements CategoriaMapper {

    @Override
    public Categoria paraEntidade(CategoriaRequest request) {
        String nome = request.nome().trim();
        String descricao = request.descricao() != null ? request.descricao().trim() : null;

        Categoria categoria = new Categoria(nome, descricao);
        if (request.ativa() != null) {
            categoria.setAtiva(request.ativa());
        }
        return categoria;
    }

    @Override
    public CategoriaResponse paraResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.isAtiva());
    }
}