package br.com.mecanica.repository;

import java.util.Optional;
import java.util.UUID;
import br.com.mecanica.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, UUID> {

    // UPDATE atomico: o banco trava a linha, serializando movimentacoes concorrentes sem perder escrita.
    // Vazio quando o produto nao existe ou o estoque e insuficiente
    @Query(value = "UPDATE produto SET quantidade_estoque = quantidade_estoque + :delta "
            + "WHERE id = :id AND quantidade_estoque + :delta >= 0 RETURNING quantidade_estoque", nativeQuery = true)
    Optional<Integer> movimentarEstoque(@Param("id") UUID id, @Param("delta") int delta);
}
