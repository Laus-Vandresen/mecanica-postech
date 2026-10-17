package br.com.mecanica.service;

import br.com.mecanica.entity.mapper.InsumoEntityMapper;
import br.com.mecanica.domain.entity.Insumo;
import br.com.mecanica.entity.InsumoEntity;
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
public class InsumoService {
    private final InsumoRepository repository;

    public List<Insumo> listar() {
        return repository.findAll(Sort.by("id")).stream().map(InsumoEntityMapper::toDomain).toList();
    }

    public Insumo buscar(Long id) {
        return InsumoEntityMapper.toDomain(encontrar(id));
    }

    @Transactional
    public Insumo criar(Insumo insumo) {
        return InsumoEntityMapper.toDomain(repository.saveAndFlush(InsumoEntityMapper.toEntity(insumo)));
    }

    @Transactional
    public Insumo atualizar(Long id, Insumo insumo) {
        var entity = encontrar(id);
        InsumoEntityMapper.updateEntity(insumo, entity);
        return InsumoEntityMapper.toDomain(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(encontrar(id));
        repository.flush();
    }

    private InsumoEntity encontrar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Insumo nao encontrado"));
    }
}
