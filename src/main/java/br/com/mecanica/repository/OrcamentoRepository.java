package br.com.mecanica.repository;

import java.util.UUID;
import br.com.mecanica.entity.OrcamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrcamentoRepository extends JpaRepository<OrcamentoEntity, UUID> {
}
