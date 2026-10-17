package br.com.mecanica.repository;

import br.com.mecanica.entity.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<VeiculoEntity, Long> {
}
