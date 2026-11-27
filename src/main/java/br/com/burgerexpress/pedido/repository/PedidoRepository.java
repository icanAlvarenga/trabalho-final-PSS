package br.com.burgerexpress.pedido.repository;

import br.com.burgerexpress.pedido.entity.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Override
    @EntityGraph(attributePaths = "itens")
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = "itens")
    List<Pedido> findByClienteIdOrderByIdDesc(Long clienteId);

    boolean existsByClienteId(Long clienteId);
}
