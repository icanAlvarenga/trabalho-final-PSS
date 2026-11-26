package br.com.burgerexpress.catalogo.repository;

import br.com.burgerexpress.catalogo.entity.Produto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsByNomeIgnoreCaseAndCategoriaId(String nome, Long categoriaId);

    boolean existsByNomeIgnoreCaseAndCategoriaIdAndIdNot(String nome, Long categoriaId, Long id);

    @Override
    @EntityGraph(attributePaths = "categoria")
    List<Produto> findAll();

    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Produto> findById(Long id);

    @EntityGraph(attributePaths = "categoria")
    List<Produto> findByCategoriaId(Long categoriaId);

    @EntityGraph(attributePaths = "categoria")
    List<Produto> findByDisponivel(boolean disponivel);

    @EntityGraph(attributePaths = "categoria")
    List<Produto> findByCategoriaIdAndDisponivel(Long categoriaId, boolean disponivel);
}