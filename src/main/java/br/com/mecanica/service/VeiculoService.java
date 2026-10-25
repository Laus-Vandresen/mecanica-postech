package br.com.mecanica.service;

import java.util.UUID;
import br.com.mecanica.entity.mapper.VeiculoEntityMapper;
import br.com.mecanica.domain.entity.Veiculo;
import br.com.mecanica.entity.ClienteEntity;
import br.com.mecanica.entity.VeiculoEntity;
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
public class VeiculoService {
    private final VeiculoRepository repository;
    private final ClienteRepository clienteRepository;

    public List<Veiculo> listar() {
        return repository.findAll(Sort.by("id")).stream().map(VeiculoEntityMapper::toDomain).toList();
    }

    public Veiculo buscar(UUID id) {
        return VeiculoEntityMapper.toDomain(encontrar(id));
    }

    @Transactional
    public Veiculo criar(Veiculo veiculo) {
        var cliente = veiculo.getClienteId() == null ? null : encontrarCliente(veiculo.getClienteId());
        var entity = VeiculoEntityMapper.toEntity(veiculo);
        entity.setCliente(cliente);
        return VeiculoEntityMapper.toDomain(repository.saveAndFlush(entity));
    }

    @Transactional
    public Veiculo atualizar(UUID id, Veiculo veiculo) {
        var entity = encontrar(id);
        VeiculoEntityMapper.updateEntity(veiculo, entity);
        if (veiculo.getClienteId() != null) entity.setCliente(encontrarCliente(veiculo.getClienteId()));
        return VeiculoEntityMapper.toDomain(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(UUID id) {
        repository.delete(encontrar(id));
        repository.flush();
    }

    private VeiculoEntity encontrar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado"));
    }

    private ClienteEntity encontrarCliente(UUID clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado"));
    }
}
