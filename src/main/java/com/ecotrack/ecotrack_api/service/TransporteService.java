package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.dto.PaginaResponse;
import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.StatusTransporte;
import com.ecotrack.ecotrack_api.entity.TipoEmpresa;
import com.ecotrack.ecotrack_api.entity.Transporte;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.HistoricoLoteRepository;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import com.ecotrack.ecotrack_api.repository.TransporteRepository;
import com.ecotrack.ecotrack_api.validation.TextoSeguro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransporteService {

    private static final List<StatusTransporte> STATUS_TRANSPORTE_ABERTOS = List.of(
            StatusTransporte.PENDENTE,
            StatusTransporte.ACEITO,
            StatusTransporte.EM_TRANSITO
    );

    private final TransporteRepository transporteRepository;
    private final LoteRepository loteRepository;
    private final EmpresaRepository empresaRepository;
    private final HistoricoLoteRepository historicoLoteRepository;
    private final EmailService emailService;
    private final DadosPessoaisCriptografiaService criptografiaService;
    private final EscopoUsuarioService escopoUsuarioService;

    @Transactional(readOnly = true)
    public List<Transporte> listar() {
        return listar(null, null);
    }

    @Transactional(readOnly = true)
    public List<Transporte> listar(String termoBusca, Integer limite) {
        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        if (!escopoUsuarioService.isAdmin(usuario)) {
            Empresa empresa = escopoUsuarioService.empresaVinculada(usuario);
            if (empresa == null) {
                return List.of();
            }

            return listarPorEscopo(usuario.getPerfil(), empresa.getId(), termoBusca, limite);
        }

        Integer limiteConsulta = limiteNormalizado(limite);
        if (limiteConsulta == null) {
            return transporteRepository.findAll().stream()
                    .map(this::descriptografarEmpresas)
                    .toList();
        }

        PageRequest pageRequest = PageRequest.of(0, limiteConsulta);
        if (termoBusca != null && !termoBusca.isBlank()) {
            return transporteRepository.buscarPorTexto(termoBusca.trim(), pageRequest).stream()
                    .map(this::descriptografarEmpresas)
                    .toList();
        }

        return transporteRepository.findAllByOrderByCriadoEmDesc(pageRequest).stream()
                .map(this::descriptografarEmpresas)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<Transporte> listarPagina(String termoBusca, Integer pagina, Integer limite) {
        int limiteConsulta = limitePaginado(limite);
        int paginaConsulta = paginaNormalizada(pagina);
        PageRequest pageRequest = PageRequest.of(paginaConsulta, limiteConsulta + 1);

        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        List<Transporte> transportes;
        if (!escopoUsuarioService.isAdmin(usuario)) {
            Empresa empresa = escopoUsuarioService.empresaVinculada(usuario);
            if (empresa == null) {
                return new PaginaResponse<>(List.of(), paginaConsulta, limiteConsulta, false, 0, totaisVazios());
            }

            transportes = listarPorEscopo(usuario.getPerfil(), empresa.getId(), termoBusca, pageRequest);
        } else if (termoBusca != null && !termoBusca.isBlank()) {
            transportes = transporteRepository.buscarPorTexto(termoBusca.trim(), pageRequest).stream()
                    .map(this::descriptografarEmpresas)
                    .toList();
        } else {
            transportes = transporteRepository.findAllByOrderByCriadoEmDesc(pageRequest).stream()
                    .map(this::descriptografarEmpresas)
                    .toList();
        }

        return montarPagina(transportes, paginaConsulta, limiteConsulta, usuario, termoBusca);
    }

    @Transactional(readOnly = true)
    public Transporte buscarPorId(Long id) {
        Transporte transporte = buscarPorIdInterno(id);
        validarLeituraTransporte(transporte);
        return descriptografarEmpresas(transporte);
    }

    @Transactional(readOnly = true)
    public Transporte buscarPorPublicId(UUID publicId) {
        Transporte transporte = buscarPorPublicIdInterno(publicId);
        validarLeituraTransporte(transporte);
        return descriptografarEmpresas(transporte);
    }

    public Transporte criar(Transporte transporte) {
        validarTextoTransporte(transporte);
        Lote lote = buscarLote(transporte);
        validarCriacaoDentroDoEscopo(lote);
        validarLoteDisponivel(lote);

        Empresa transportadora = buscarEmpresa(transporte.getTransportadora(), "Transportadora nao encontrada");
        Empresa receptora = buscarEmpresa(transporte.getReceptora(), "Receptora nao encontrada");
        validarTipoEmpresa(transportadora, TipoEmpresa.TRANSPORTADORA, "Empresa selecionada nao e transportadora");
        validarTipoEmpresa(receptora, TipoEmpresa.RECEPTORA, "Empresa selecionada nao e receptora");

        transporte.setLote(lote);
        transporte.setTransportadora(transportadora);
        transporte.setReceptora(receptora);
        transporte.setStatus(StatusTransporte.PENDENTE);
        transporte.setCriadoEm(LocalDateTime.now());

        Transporte salvo = transporteRepository.save(transporte);
        log.info("Transporte {} criado para a transportadora {}", salvo.getId(), transportadora.getRazaoSocial());
        enviarEmailParaTransportadora(salvo, lote);

        return salvo;
    }

    public Transporte alterarStatus(Long id, StatusTransporte novoStatus, String observacao) {
        return alterarStatus(buscarPorIdInterno(id), novoStatus, observacao);
    }

    public Transporte alterarStatus(UUID publicId, StatusTransporte novoStatus, String observacao) {
        return alterarStatus(buscarPorPublicIdInterno(publicId), novoStatus, observacao);
    }

    public Transporte confirmarRecebimentoFinal(Long id, String observacao) {
        return confirmarRecebimentoFinal(buscarPorIdInterno(id), observacao);
    }

    public Transporte confirmarRecebimentoFinal(UUID publicId, String observacao) {
        return confirmarRecebimentoFinal(buscarPorPublicIdInterno(publicId), observacao);
    }

    public Transporte recusarRecebimentoFinal(Long id, String observacao) {
        return recusarRecebimentoFinal(buscarPorIdInterno(id), observacao);
    }

    public Transporte recusarRecebimentoFinal(UUID publicId, String observacao) {
        return recusarRecebimentoFinal(buscarPorPublicIdInterno(publicId), observacao);
    }

    @Transactional(readOnly = true)
    public List<Transporte> buscarPorLote(Long loteId) {
        return transporteRepository.findByLoteId(loteId);
    }

    @Transactional(readOnly = true)
    public List<Transporte> buscarPorLotePublicId(UUID lotePublicId) {
        return transporteRepository.findByLote_PublicId(lotePublicId);
    }

    private Transporte alterarStatus(Transporte transporte, StatusTransporte novoStatus, String observacao) {
        TextoSeguro.validar(observacao, "Observacao");
        validarAlteracaoDentroDoEscopo(transporte);
        StatusTransporte statusAnterior = transporte.getStatus();

        validarAlteracaoStatus(statusAnterior, novoStatus);

        transporte.setStatus(novoStatus);
        transporte.setObservacao(observacao);

        aplicarImpactoNoLote(transporte, statusAnterior, novoStatus, observacao);

        return transporteRepository.save(transporte);
    }

    private Transporte buscarPorIdInterno(Long id) {
        return transporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transporte nao encontrado"));
    }

    private Transporte buscarPorPublicIdInterno(UUID publicId) {
        return transporteRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transporte nao encontrado"));
    }

    private Transporte confirmarRecebimentoFinal(Transporte transporte, String observacao) {
        TextoSeguro.validar(observacao, "Observacao");
        validarRecebimentoDentroDoEscopo(transporte);
        validarRecebimentoFinal(transporte);

        StatusTransporte statusAnterior = transporte.getStatus();
        transporte.setStatus(StatusTransporte.CONCLUIDO);
        transporte.setDataEntrega(LocalDateTime.now());
        transporte.setObservacao(observacao);

        aplicarImpactoNoLote(transporte, statusAnterior, StatusTransporte.CONCLUIDO,
                montarObservacaoRecebimentoFinal(observacao));

        return transporteRepository.save(transporte);
    }

    private Transporte recusarRecebimentoFinal(Transporte transporte, String observacao) {
        TextoSeguro.validar(observacao, "Observacao");
        validarRecebimentoDentroDoEscopo(transporte);
        validarRecebimentoFinal(transporte);

        StatusTransporte statusAnterior = transporte.getStatus();
        transporte.setStatus(StatusTransporte.RECEBIMENTO_RECUSADO);
        transporte.setObservacao(observacao);

        aplicarImpactoNoLote(transporte, statusAnterior, StatusTransporte.RECEBIMENTO_RECUSADO,
                montarObservacaoRecebimentoRecusado(observacao));

        return transporteRepository.save(transporte);
    }

    private void validarAlteracaoStatus(StatusTransporte statusAnterior, StatusTransporte novoStatus) {
        if (novoStatus == StatusTransporte.CONCLUIDO) {
            throw new RegraNegocioException("Recebimento final deve ser confirmado pela empresa receptora");
        }

        if (novoStatus == StatusTransporte.RECEBIMENTO_RECUSADO) {
            throw new RegraNegocioException("Recusa de recebimento deve ser registrada pela empresa receptora");
        }

        if (statusFinal(statusAnterior)) {
            throw new RegraNegocioException("Transporte ja esta em status final");
        }

        if (statusAnterior == novoStatus) {
            throw new RegraNegocioException("Transporte ja esta no status informado");
        }

        if (!transicaoPermitida(statusAnterior, novoStatus)) {
            throw new RegraNegocioException("Transicao de status nao permitida para o transporte");
        }
    }

    private void validarRecebimentoFinal(Transporte transporte) {
        if (statusFinal(transporte.getStatus())) {
            throw new RegraNegocioException("Transporte ja esta em status final");
        }

        if (transporte.getStatus() != StatusTransporte.EM_TRANSITO) {
            throw new RegraNegocioException("Recebimento final so pode ser confirmado com transporte em transito");
        }
    }

    private String montarObservacaoRecebimentoFinal(String observacao) {
        if (observacao != null && !observacao.isBlank()) {
            return "Recebimento final confirmado pela receptora. " + observacao;
        }

        return "Recebimento final confirmado pela receptora";
    }

    private String montarObservacaoRecebimentoRecusado(String observacao) {
        if (observacao != null && !observacao.isBlank()) {
            return "Recebimento final recusado pela receptora. " + observacao;
        }

        return "Recebimento final recusado pela receptora";
    }

    private boolean statusFinal(StatusTransporte status) {
        return status == StatusTransporte.CONCLUIDO
                || status == StatusTransporte.CANCELADO
                || status == StatusTransporte.RECUSADO
                || status == StatusTransporte.RECEBIMENTO_RECUSADO;
    }

    private boolean transicaoPermitida(StatusTransporte statusAnterior, StatusTransporte novoStatus) {
        return switch (statusAnterior) {
            case PENDENTE -> novoStatus == StatusTransporte.ACEITO
                    || novoStatus == StatusTransporte.RECUSADO
                    || novoStatus == StatusTransporte.CANCELADO;
            case ACEITO -> novoStatus == StatusTransporte.EM_TRANSITO
                    || novoStatus == StatusTransporte.CANCELADO;
            case EM_TRANSITO -> novoStatus == StatusTransporte.CANCELADO;
            default -> false;
        };
    }

    private void aplicarImpactoNoLote(Transporte transporte, StatusTransporte statusAnterior,
                                      StatusTransporte novoStatus, String observacao) {
        if (novoStatus == StatusTransporte.EM_TRANSITO) {
            transporte.setDataColeta(LocalDateTime.now());
            atualizarStatusLote(transporte, StatusLote.EM_TRANSITO, observacao);
        }

        if (novoStatus == StatusTransporte.CONCLUIDO) {
            transporte.setDataEntrega(LocalDateTime.now());
            atualizarStatusLote(transporte, StatusLote.DESCARTADO, observacao);
        }

        if ((novoStatus == StatusTransporte.CANCELADO && statusAnterior == StatusTransporte.EM_TRANSITO)
                || novoStatus == StatusTransporte.RECUSADO
                || novoStatus == StatusTransporte.RECEBIMENTO_RECUSADO) {
            atualizarStatusLote(transporte, StatusLote.AGUARDANDO_COLETA, observacao);
        }
    }

    private void atualizarStatusLote(Transporte transporte, StatusLote novoStatus, String observacao) {
        Lote lote = transporte.getLote();
        StatusLote statusAnterior = lote.getStatus();

        if (statusAnterior == novoStatus) {
            return;
        }

        lote.setStatus(novoStatus);
        loteRepository.save(lote);
        registrarHistoricoLote(transporte, lote, statusAnterior, novoStatus, observacao);
    }

    private void registrarHistoricoLote(Transporte transporte, Lote lote, StatusLote statusAnterior,
                                        StatusLote statusNovo, String observacao) {
        HistoricoLote historico = new HistoricoLote();
        historico.setLote(lote);
        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(statusNovo);
        historico.setUsuario(lote.getCriadoPor());
        historico.setObservacao(montarObservacaoHistorico(transporte, observacao));
        historico.setDataHora(LocalDateTime.now());
        historicoLoteRepository.save(historico);
    }

    private List<Transporte> listarPorEscopo(Perfil perfil, Long empresaId, String termoBusca, Integer limite) {
        PageRequest pageRequest = PageRequest.of(0, limiteNormalizado(limite) == null ? 100 : limiteNormalizado(limite));
        return listarPorEscopo(perfil, empresaId, termoBusca, pageRequest);
    }

    private List<Transporte> listarPorEscopo(Perfil perfil, Long empresaId, String termoBusca, PageRequest pageRequest) {
        if (termoBusca != null && !termoBusca.isBlank()) {
            String busca = termoBusca.trim();
            List<Transporte> transportes = switch (perfil) {
                case GERADORA -> transporteRepository.buscarPorTextoGeradora(empresaId, busca, pageRequest);
                case TRANSPORTADORA -> transporteRepository.buscarPorTextoTransportadora(empresaId, busca, pageRequest);
                case RECEPTORA -> transporteRepository.buscarPorTextoReceptora(empresaId, busca, pageRequest);
                default -> List.of();
            };
            return transportes.stream().map(this::descriptografarEmpresas).toList();
        }

        List<Transporte> transportes = switch (perfil) {
            case GERADORA -> transporteRepository.findByLote_EmpresaGeradoraIdOrderByCriadoEmDesc(empresaId, pageRequest);
            case TRANSPORTADORA -> transporteRepository.findByTransportadoraIdOrderByCriadoEmDesc(empresaId, pageRequest);
            case RECEPTORA -> transporteRepository.findByReceptoraIdOrderByCriadoEmDesc(empresaId, pageRequest);
            default -> List.of();
        };
        return transportes.stream().map(this::descriptografarEmpresas).toList();
    }

    private PaginaResponse<Transporte> montarPagina(List<Transporte> transportes, int pagina, int limite, Usuario usuario, String termoBusca) {
        boolean temProxima = transportes.size() > limite;
        List<Transporte> itens = temProxima ? transportes.subList(0, limite) : transportes;
        String busca = termoBusca == null ? "" : termoBusca.trim();
        return new PaginaResponse<>(itens, pagina, limite, temProxima, totalTransportes(usuario, busca), totaisPorStatus(usuario, busca));
    }

    private int paginaNormalizada(Integer pagina) {
        if (pagina == null || pagina < 0) {
            return 0;
        }

        return pagina;
    }

    private int limitePaginado(Integer limite) {
        Integer normalizado = limiteNormalizado(limite);
        return normalizado == null ? 20 : normalizado;
    }

    private long totalTransportes(Usuario usuario, String busca) {
        if (escopoUsuarioService.isAdmin(usuario)) {
            return busca.isBlank() ? transporteRepository.count() : transporteRepository.countPorTexto(busca);
        }

        Empresa empresa = escopoUsuarioService.empresaVinculada(usuario);
        if (empresa == null) {
            return 0;
        }

        return switch (usuario.getPerfil()) {
            case GERADORA -> busca.isBlank()
                    ? transporteRepository.countByLote_EmpresaGeradoraId(empresa.getId())
                    : transporteRepository.countPorTextoGeradora(empresa.getId(), busca);
            case TRANSPORTADORA -> busca.isBlank()
                    ? transporteRepository.countByTransportadoraId(empresa.getId())
                    : transporteRepository.countPorTextoTransportadora(empresa.getId(), busca);
            case RECEPTORA -> busca.isBlank()
                    ? transporteRepository.countByReceptoraId(empresa.getId())
                    : transporteRepository.countPorTextoReceptora(empresa.getId(), busca);
            default -> 0;
        };
    }

    private Map<String, Long> totaisPorStatus(Usuario usuario, String busca) {
        return Map.of(
                StatusTransporte.PENDENTE.name(), totalTransportesPorStatus(usuario, busca, StatusTransporte.PENDENTE),
                StatusTransporte.ACEITO.name(), totalTransportesPorStatus(usuario, busca, StatusTransporte.ACEITO),
                StatusTransporte.EM_TRANSITO.name(), totalTransportesPorStatus(usuario, busca, StatusTransporte.EM_TRANSITO),
                StatusTransporte.CONCLUIDO.name(), totalTransportesPorStatus(usuario, busca, StatusTransporte.CONCLUIDO),
                StatusTransporte.RECUSADO.name(), totalTransportesPorStatus(usuario, busca, StatusTransporte.RECUSADO),
                StatusTransporte.RECEBIMENTO_RECUSADO.name(), totalTransportesPorStatus(usuario, busca, StatusTransporte.RECEBIMENTO_RECUSADO),
                StatusTransporte.CANCELADO.name(), totalTransportesPorStatus(usuario, busca, StatusTransporte.CANCELADO)
        );
    }

    private Map<String, Long> totaisVazios() {
        return Map.of(
                StatusTransporte.PENDENTE.name(), 0L,
                StatusTransporte.ACEITO.name(), 0L,
                StatusTransporte.EM_TRANSITO.name(), 0L,
                StatusTransporte.CONCLUIDO.name(), 0L,
                StatusTransporte.RECUSADO.name(), 0L,
                StatusTransporte.RECEBIMENTO_RECUSADO.name(), 0L,
                StatusTransporte.CANCELADO.name(), 0L
        );
    }

    private long totalTransportesPorStatus(Usuario usuario, String busca, StatusTransporte status) {
        if (escopoUsuarioService.isAdmin(usuario)) {
            return busca.isBlank()
                    ? transporteRepository.countByStatus(status)
                    : transporteRepository.countPorTextoEStatus(busca, status);
        }

        Empresa empresa = escopoUsuarioService.empresaVinculada(usuario);
        if (empresa == null) {
            return 0;
        }

        return switch (usuario.getPerfil()) {
            case GERADORA -> busca.isBlank()
                    ? transporteRepository.countByLote_EmpresaGeradoraIdAndStatus(empresa.getId(), status)
                    : transporteRepository.countPorTextoGeradoraEStatus(empresa.getId(), busca, status);
            case TRANSPORTADORA -> busca.isBlank()
                    ? transporteRepository.countByTransportadoraIdAndStatus(empresa.getId(), status)
                    : transporteRepository.countPorTextoTransportadoraEStatus(empresa.getId(), busca, status);
            case RECEPTORA -> busca.isBlank()
                    ? transporteRepository.countByReceptoraIdAndStatus(empresa.getId(), status)
                    : transporteRepository.countPorTextoReceptoraEStatus(empresa.getId(), busca, status);
            default -> 0;
        };
    }

    private void validarCriacaoDentroDoEscopo(Lote lote) {
        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        if (escopoUsuarioService.isAdmin(usuario)) {
            return;
        }

        Empresa empresa = escopoUsuarioService.empresaObrigatoria(usuario);
        if (usuario.getPerfil() != Perfil.GERADORA
                || lote.getEmpresaGeradora() == null
                || !lote.getEmpresaGeradora().getId().equals(empresa.getId())) {
            throw new RegraNegocioException("Transporte deve ser criado pela empresa geradora vinculada ao usuario");
        }
    }

    private void validarTextoTransporte(Transporte transporte) {
        TextoSeguro.validar(transporte.getResponsavel(), "Responsavel");
        TextoSeguro.validar(transporte.getObservacao(), "Observacao");
    }

    private void validarLeituraTransporte(Transporte transporte) {
        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        if (escopoUsuarioService.isAdmin(usuario)) {
            return;
        }

        Empresa empresa = escopoUsuarioService.empresaObrigatoria(usuario);
        if (!transporteDentroDoEscopo(transporte, usuario.getPerfil(), empresa.getId())) {
            throw new RecursoNaoEncontradoException("Transporte nao encontrado");
        }
    }

    private void validarAlteracaoDentroDoEscopo(Transporte transporte) {
        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        if (escopoUsuarioService.isAdmin(usuario)) {
            return;
        }

        Empresa empresa = escopoUsuarioService.empresaObrigatoria(usuario);
        if (usuario.getPerfil() != Perfil.TRANSPORTADORA
                || transporte.getTransportadora() == null
                || !transporte.getTransportadora().getId().equals(empresa.getId())) {
            throw new RegraNegocioException("Apenas a transportadora vinculada pode alterar o transporte");
        }
    }

    private void validarRecebimentoDentroDoEscopo(Transporte transporte) {
        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        if (escopoUsuarioService.isAdmin(usuario)) {
            return;
        }

        Empresa empresa = escopoUsuarioService.empresaObrigatoria(usuario);
        if (usuario.getPerfil() != Perfil.RECEPTORA
                || transporte.getReceptora() == null
                || !transporte.getReceptora().getId().equals(empresa.getId())) {
            throw new RegraNegocioException("Apenas a receptora vinculada pode confirmar o recebimento final");
        }
    }

    private boolean transporteDentroDoEscopo(Transporte transporte, Perfil perfil, Long empresaId) {
        return switch (perfil) {
            case GERADORA -> transporte.getLote() != null
                    && transporte.getLote().getEmpresaGeradora() != null
                    && transporte.getLote().getEmpresaGeradora().getId().equals(empresaId);
            case TRANSPORTADORA -> transporte.getTransportadora() != null
                    && transporte.getTransportadora().getId().equals(empresaId);
            case RECEPTORA -> transporte.getReceptora() != null
                    && transporte.getReceptora().getId().equals(empresaId);
            default -> false;
        };
    }

    private Integer limiteNormalizado(Integer limite) {
        if (limite == null || limite <= 0) {
            return null;
        }

        return Math.min(limite, 100);
    }

    private String montarObservacaoHistorico(Transporte transporte, String observacao) {
        if (observacao != null && !observacao.isBlank()) {
            return observacao;
        }

        return "Status do lote atualizado automaticamente pelo transporte #" + transporte.getId();
    }

    private Lote buscarLote(Transporte transporte) {
        if (transporte.getLote() == null) {
            throw new RegraNegocioException("Lote e obrigatorio para criar transporte");
        }

        if (transporte.getLote().getId() != null) {
            return loteRepository.findById(transporte.getLote().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Lote nao encontrado"));
        }

        if (transporte.getLote().getPublicId() == null) {
            throw new RegraNegocioException("Lote e obrigatorio para criar transporte");
        }

        return loteRepository.findByPublicId(transporte.getLote().getPublicId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote nao encontrado"));
    }

    private Empresa buscarEmpresa(Empresa empresa, String mensagemErro) {
        if (empresa == null) {
            throw new RegraNegocioException(mensagemErro);
        }

        if (empresa.getId() != null) {
            return empresaRepository.findById(empresa.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(mensagemErro));
        }

        if (empresa.getPublicId() == null) {
            throw new RegraNegocioException(mensagemErro);
        }

        return empresaRepository.findByPublicId(empresa.getPublicId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(mensagemErro));
    }

    private void validarLoteDisponivel(Lote lote) {
        if (lote.getStatus() != StatusLote.AGUARDANDO_COLETA) {
            throw new RegraNegocioException("Lote nao esta disponivel para transporte");
        }

        if (transporteRepository.existsByLoteIdAndStatusIn(lote.getId(), STATUS_TRANSPORTE_ABERTOS)) {
            throw new RegraNegocioException("Lote ja possui transporte em andamento");
        }
    }

    private void validarTipoEmpresa(Empresa empresa, TipoEmpresa tipoEsperado, String mensagem) {
        if (!empresa.isAtiva()) {
            throw new RegraNegocioException("Empresa selecionada esta inativa");
        }

        if (empresa.getTipo() != tipoEsperado) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private void enviarEmailParaTransportadora(Transporte transporte, Lote lote) {
        Empresa transportadora = transporte.getTransportadora();

        if (transportadora == null || transportadora.getEmail() == null || transportadora.getEmail().isBlank()) {
            log.warn("E-mail da transportadora nao cadastrado para o transporte {}", transporte.getId());
            return;
        }

        try {
            log.info("Preparando notificacao do transporte {} para {}", transporte.getId(), transportadora.getEmail());
            emailService.enviarNotificacaoTransporte(
                    criptografiaService.descriptografar(transportadora.getEmail()),
                    transportadora.getRazaoSocial(),
                    lote.getId(),
                    lote.getDescricao()
            );
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail de notificacao: {}", e.getMessage());
        }
    }

    private Transporte descriptografarEmpresas(Transporte transporte) {
        descriptografarEmpresa(transporte.getTransportadora());
        descriptografarEmpresa(transporte.getReceptora());
        if (transporte.getLote() != null) {
            descriptografarEmpresa(transporte.getLote().getEmpresaGeradora());
        }
        return transporte;
    }

    private void descriptografarEmpresa(Empresa empresa) {
        if (empresa == null) {
            return;
        }

        empresa.setCnpj(criptografiaService.descriptografar(empresa.getCnpj()));
        empresa.setEmail(criptografiaService.descriptografar(empresa.getEmail()));
        empresa.setTelefone(criptografiaService.descriptografar(empresa.getTelefone()));
        empresa.setEndereco(criptografiaService.descriptografar(empresa.getEndereco()));
    }
}
