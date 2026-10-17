package br.com.mecanica.repository;

import br.com.mecanica.entity.InsumoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsumoRepository extends JpaRepository<InsumoEntity, Long> {
}
