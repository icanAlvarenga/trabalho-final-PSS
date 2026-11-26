package br.com.burgerexpress.cliente.repository;

import br.com.burgerexpress.cliente.entity.Endereco;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Padrão Repository: somente persistência/consulta; nenhuma decisão de negócio. */
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findByClienteIdOrderByIdAsc(Long clienteId);

    Optional<Endereco> findByIdAndClienteId(Long id, Long clienteId);

    List<Endereco> findByClienteIdAndPrincipalTrue(Long clienteId);
}
