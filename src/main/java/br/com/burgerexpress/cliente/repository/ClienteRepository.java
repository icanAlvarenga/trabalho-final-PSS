package br.com.burgerexpress.cliente.repository;

import br.com.burgerexpress.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

/** Padrão Repository: somente persistência/consulta; nenhuma decisão de negócio. */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
