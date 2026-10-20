package br.com.mecanica.repository;

import java.util.UUID;
import br.com.mecanica.entity.PecaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PecaRepository extends JpaRepository<PecaEntity, UUID> {
}
