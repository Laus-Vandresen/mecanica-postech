package br.com.mecanica.controller;

import br.com.mecanica.api.OrdemServicoV1Api;
import br.com.mecanica.api.dto.AprovacaoOrdemServicoDTOV1;
import br.com.mecanica.api.dto.DiagnosticoOrdemServicoDTOV1;
import br.com.mecanica.api.dto.EnvioOrcamentoDTOV1;
import br.com.mecanica.api.dto.MotivoOrdemServicoDTOV1;
import br.com.mecanica.api.dto.OrdemServicoRequestDTOV1;
import br.com.mecanica.api.dto.OrdemServicoResponseDTOV1;
import br.com.mecanica.api.dto.TempoMedioFinalizacaoResponseDTOV1;
import br.com.mecanica.controller.mapper.OrdemServicoMapper;
import br.com.mecanica.service.OrdemServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrdemServicoController implements OrdemServicoV1Api {

    private final OrdemServicoService service;

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> cadastrarOrdemServicoV1(OrdemServicoRequestDTOV1 request) {
        var response = OrdemServicoMapper.toResponse(service.criar(
                request.getVeiculoId(), request.getDescricao(), request.getServicoIds(),
                request.getProdutoIds()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<List<OrdemServicoResponseDTOV1>> listaOrdensServicoV1(
            UUID clienteId, UUID veiculoId, String status, String fila, Integer pagina, Integer tamanho) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.listar(
                clienteId, veiculoId, OrdemServicoMapper.toStatus(status), OrdemServicoMapper.toFila(fila),
                pagina, tamanho)));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> buscarOrdemServicoV1(UUID id) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.buscar(id)));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> iniciarDiagnosticoOrdemServicoV1(UUID id) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.iniciarDiagnostico(id)));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> registrarDiagnosticoOrdemServicoV1(
            UUID id, DiagnosticoOrdemServicoDTOV1 request) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.diagnosticar(
                id, request.getDiagnostico(), request.getServicoIds(), request.getProdutoIds())));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> enviarOrcamentoOrdemServicoV1(
            UUID id, EnvioOrcamentoDTOV1 request) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(
                service.enviarOrcamento(id, request.getOrcamentoId())));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> aprovarOrdemServicoV1(
            UUID id, AprovacaoOrdemServicoDTOV1 request) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.aprovar(
                id, request.getClienteId(), OrdemServicoMapper.toDomain(request.getCliente()))));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> recusarOrdemServicoV1(
            UUID id, MotivoOrdemServicoDTOV1 request) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(
                service.recusar(id, request.getMotivo())));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> iniciarExecucaoOrdemServicoV1(UUID id) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.iniciarExecucao(id)));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> concluirExecucaoOrdemServicoV1(UUID id) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.concluirExecucao(id)));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> finalizarOrdemServicoV1(UUID id) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.finalizar(id)));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> entregarOrdemServicoV1(UUID id) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.entregar(id)));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> cancelarOrdemServicoV1(
            UUID id, MotivoOrdemServicoDTOV1 request) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(
                service.cancelar(id, request.getMotivo())));
    }

    @Override
    public ResponseEntity<TempoMedioFinalizacaoResponseDTOV1> buscarTempoMedioFinalizacaoOrdemServicoV1() {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.calcularTempoMedioFinalizacao()));
    }
}
