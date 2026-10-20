package br.com.mecanica.repository;

import java.util.UUID;
import br.com.mecanica.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, UUID> {
}
