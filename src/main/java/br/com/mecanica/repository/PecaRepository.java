package br.com.mecanica.repository;

import br.com.mecanica.entity.PecaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PecaRepository extends JpaRepository<PecaEntity, Long> {
}
