package br.com.mecanica.service;

import br.com.mecanica.entity.mapper.PecaEntityMapper;
import br.com.mecanica.domain.entity.Peca;
import br.com.mecanica.entity.PecaEntity;
import br.com.mecanica.repository.*;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PecaService {
    private final PecaRepository repository;

    public List<Peca> listar() {
        return repository.findAll(Sort.by("id")).stream().map(PecaEntityMapper::toDomain).toList();
    }

    public Peca buscar(Long id) {
        return PecaEntityMapper.toDomain(encontrar(id));
    }

    @Transactional
    public Peca criar(Peca peca) {
        return PecaEntityMapper.toDomain(repository.saveAndFlush(PecaEntityMapper.toEntity(peca)));
    }

    @Transactional
    public Peca atualizar(Long id, Peca peca) {
        var entity = encontrar(id);
        PecaEntityMapper.updateEntity(peca, entity);
        return PecaEntityMapper.toDomain(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(encontrar(id));
        repository.flush();
    }

    private PecaEntity encontrar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peca nao encontrado"));
    }
}
