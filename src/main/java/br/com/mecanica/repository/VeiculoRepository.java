package br.com.mecanica.repository;

import java.util.UUID;
import br.com.mecanica.entity.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<VeiculoEntity, UUID> {
}
