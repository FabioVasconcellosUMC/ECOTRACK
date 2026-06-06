package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.HistoricoLote;
import com.ecotrack.ecotrack_api.entity.Lote;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.StatusLote;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.dto.PaginaResponse;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.HistoricoLoteRepository;
import com.ecotrack.ecotrack_api.repository.LoteRepository;
import com.ecotrack.ecotrack_api.repository.TransporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LoteService {

    private static final String OBSERVACAO_CRIACAO = "Lote criado";

    private final LoteRepository loteRepository;
    private final HistoricoLoteRepository historicoLoteRepository;
    private final EmpresaRepository empresaRepository;
    private final TransporteRepository transporteRepository;
    private final DadosPessoaisCriptografiaService criptografiaService;
    private final EscopoUsuarioService escopoUsuarioService;

    public Lote criar(Lote lote, Usuario usuario) {
        lote.setEmpresaGeradora(buscarEmpresaGeradora(lote));
        validarLoteDentroDoEscopo(lote, usuario);
        prepararNovoLote(lote, usuario);
        Lote salvo = loteRepository.save(lote);

        registrarHistorico(salvo, null, StatusLote.AGUARDANDO_COLETA, usuario, OBSERVACAO_CRIACAO);
        return salvo;
    }

    @Transactional(readOnly = true)
    public List<Lote> listarTodos() {
        return listarTodos(null, null);
    }

    @Transactional(readOnly = true)
    public List<Lote> listarTodos(String termoBusca, Integer limite) {
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
            return loteRepository.findAll().stream()
                    .map(this::descriptografarEmpresaGeradora)
                    .toList();
        }

        PageRequest pageRequest = PageRequest.of(0, limiteConsulta);
        if (termoBusca != null && !termoBusca.isBlank()) {
            return loteRepository.buscarPorTexto(termoBusca.trim(), pageRequest).stream()
                    .map(this::descriptografarEmpresaGeradora)
                    .toList();
        }

        return loteRepository.findRecentes(pageRequest).stream()
                .map(this::descriptografarEmpresaGeradora)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<Lote> listarPagina(String termoBusca, Integer pagina, Integer limite) {
        int limiteConsulta = limitePaginado(limite);
        int paginaConsulta = paginaNormalizada(pagina);
        PageRequest pageRequest = PageRequest.of(paginaConsulta, limiteConsulta + 1);

        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        List<Lote> lotes;
        if (!escopoUsuarioService.isAdmin(usuario)) {
            Empresa empresa = escopoUsuarioService.empresaVinculada(usuario);
            if (empresa == null) {
                return new PaginaResponse<>(List.of(), paginaConsulta, limiteConsulta, false, 0, totaisVazios());
            }

            lotes = listarPorEscopo(usuario.getPerfil(), empresa.getId(), termoBusca, pageRequest);
        } else if (termoBusca != null && !termoBusca.isBlank()) {
            lotes = loteRepository.buscarPorTexto(termoBusca.trim(), pageRequest).stream()
                    .map(this::descriptografarEmpresaGeradora)
                    .toList();
        } else {
            lotes = loteRepository.findRecentes(pageRequest).stream()
                    .map(this::descriptografarEmpresaGeradora)
                    .toList();
        }

        return montarPagina(lotes, paginaConsulta, limiteConsulta, usuario, termoBusca);
    }

    @Transactional(readOnly = true)
    public Lote buscarPorId(Long id) {
        Lote lote = buscarPorIdInterno(id);
        validarLeituraLote(lote);
        return descriptografarEmpresaGeradora(lote);
    }

    @Transactional(readOnly = true)
    public Lote buscarPorPublicId(UUID publicId) {
        Lote lote = buscarPorPublicIdInterno(publicId);
        validarLeituraLote(lote);
        return descriptografarEmpresaGeradora(lote);
    }

    public Lote alterarStatus(Long id, StatusLote novoStatus, String observacao, Usuario usuario) {
        return alterarStatus(buscarPorIdInterno(id), novoStatus, observacao, usuario);
    }

    public Lote alterarStatus(UUID publicId, StatusLote novoStatus, String observacao, Usuario usuario) {
        return alterarStatus(buscarPorPublicIdInterno(publicId), novoStatus, observacao, usuario);
    }

    @Transactional(readOnly = true)
    public List<HistoricoLote> buscarHistorico(Long loteId) {
        return historicoLoteRepository.findByLoteIdOrderByDataHoraDesc(loteId);
    }

    @Transactional(readOnly = true)
    public List<HistoricoLote> buscarHistoricoPorPublicId(UUID publicId) {
        Lote lote = buscarPorPublicId(publicId);
        return buscarHistorico(lote.getId());
    }

    private Lote alterarStatus(Lote lote, StatusLote novoStatus, String observacao, Usuario usuario) {
        validarLoteDentroDoEscopo(lote, usuario);
        validarTransicao(lote.getStatus(), novoStatus);

        StatusLote statusAnterior = lote.getStatus();
        lote.setStatus(novoStatus);
        loteRepository.save(lote);

        registrarHistorico(lote, statusAnterior, novoStatus, usuario, observacao);
        return lote;
    }

    private Lote buscarPorIdInterno(Long id) {
        return loteRepository.findByIdWithEmpresa(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));
    }

    private Lote buscarPorPublicIdInterno(UUID publicId) {
        return loteRepository.findByPublicIdWithEmpresa(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));
    }

    private void prepararNovoLote(Lote lote, Usuario usuario) {
        lote.setCriadoPor(usuario);
        lote.setStatus(StatusLote.AGUARDANDO_COLETA);
        lote.setCriadoEm(LocalDateTime.now());
    }

    private void validarTransicao(StatusLote atual, StatusLote novo) {
        if (atual == novo) {
            throw new RegraNegocioException("Lote já está com o status informado");
        }

        if (atual == StatusLote.DESCARTADO || atual == StatusLote.CANCELADO) {
            throw new RegraNegocioException("Lote já está em status final e não pode ser alterado");
        }
    }

    private void registrarHistorico(Lote lote, StatusLote anterior, StatusLote novo,
                                    Usuario usuario, String observacao) {
        HistoricoLote historico = new HistoricoLote();
        historico.setLote(lote);
        historico.setStatusAnterior(anterior);
        historico.setStatusNovo(novo);
        historico.setUsuario(usuario);
        historico.setObservacao(observacao);
        historico.setDataHora(LocalDateTime.now());
        historicoLoteRepository.save(historico);
    }

    private List<Lote> listarPorEscopo(Perfil perfil, Long empresaId, String termoBusca, Integer limite) {
        PageRequest pageRequest = PageRequest.of(0, limiteNormalizado(limite) == null ? 100 : limiteNormalizado(limite));
        return listarPorEscopo(perfil, empresaId, termoBusca, pageRequest);
    }

    private List<Lote> listarPorEscopo(Perfil perfil, Long empresaId, String termoBusca, PageRequest pageRequest) {
        if (termoBusca != null && !termoBusca.isBlank()) {
            String busca = termoBusca.trim();
            List<Lote> lotes = switch (perfil) {
                case GERADORA -> loteRepository.buscarPorTextoGeradora(empresaId, busca, pageRequest);
                case TRANSPORTADORA -> loteRepository.buscarPorTextoTransportadora(empresaId, busca, pageRequest);
                case RECEPTORA -> loteRepository.buscarPorTextoReceptora(empresaId, busca, pageRequest);
                default -> List.of();
            };
            return lotes.stream().map(this::descriptografarEmpresaGeradora).toList();
        }

        List<Lote> lotes = switch (perfil) {
            case GERADORA -> loteRepository.findByEmpresaGeradoraIdOrderByCriadoEmDesc(empresaId, pageRequest);
            case TRANSPORTADORA -> loteRepository.findRecentesPorTransportadora(empresaId, pageRequest);
            case RECEPTORA -> loteRepository.findRecentesPorReceptora(empresaId, pageRequest);
            default -> List.of();
        };
        return lotes.stream().map(this::descriptografarEmpresaGeradora).toList();
    }

    private PaginaResponse<Lote> montarPagina(List<Lote> lotes, int pagina, int limite, Usuario usuario, String termoBusca) {
        boolean temProxima = lotes.size() > limite;
        List<Lote> itens = temProxima ? lotes.subList(0, limite) : lotes;
        String busca = termoBusca == null ? "" : termoBusca.trim();
        return new PaginaResponse<>(itens, pagina, limite, temProxima, totalLotes(usuario, busca), totaisPorStatus(usuario, busca));
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

    private long totalLotes(Usuario usuario, String busca) {
        if (escopoUsuarioService.isAdmin(usuario)) {
            return busca.isBlank() ? loteRepository.count() : loteRepository.countPorTexto(busca);
        }

        Empresa empresa = escopoUsuarioService.empresaVinculada(usuario);
        if (empresa == null) {
            return 0;
        }

        return switch (usuario.getPerfil()) {
            case GERADORA -> busca.isBlank()
                    ? loteRepository.countByEmpresaGeradoraId(empresa.getId())
                    : loteRepository.countByEmpresaGeradoraIdAndDescricaoContainingIgnoreCaseOrEmpresaGeradoraIdAndTipoResiduoContainingIgnoreCase(
                            empresa.getId(), busca, empresa.getId(), busca);
            case TRANSPORTADORA -> busca.isBlank()
                    ? loteRepository.countPorTransportadora(empresa.getId())
                    : loteRepository.countPorTextoTransportadora(empresa.getId(), busca);
            case RECEPTORA -> busca.isBlank()
                    ? loteRepository.countPorReceptora(empresa.getId())
                    : loteRepository.countPorTextoReceptora(empresa.getId(), busca);
            default -> 0;
        };
    }

    private Map<String, Long> totaisPorStatus(Usuario usuario, String busca) {
        return Map.of(
                StatusLote.AGUARDANDO_COLETA.name(), totalLotesPorStatus(usuario, busca, StatusLote.AGUARDANDO_COLETA),
                StatusLote.EM_TRANSITO.name(), totalLotesPorStatus(usuario, busca, StatusLote.EM_TRANSITO),
                StatusLote.DESCARTADO.name(), totalLotesPorStatus(usuario, busca, StatusLote.DESCARTADO),
                StatusLote.CANCELADO.name(), totalLotesPorStatus(usuario, busca, StatusLote.CANCELADO)
        );
    }

    private Map<String, Long> totaisVazios() {
        return Map.of(
                StatusLote.AGUARDANDO_COLETA.name(), 0L,
                StatusLote.EM_TRANSITO.name(), 0L,
                StatusLote.DESCARTADO.name(), 0L,
                StatusLote.CANCELADO.name(), 0L
        );
    }

    private long totalLotesPorStatus(Usuario usuario, String busca, StatusLote status) {
        if (escopoUsuarioService.isAdmin(usuario)) {
            return busca.isBlank()
                    ? loteRepository.countByStatus(status)
                    : loteRepository.countByStatusAndDescricaoContainingIgnoreCaseOrStatusAndTipoResiduoContainingIgnoreCase(
                            status, busca, status, busca);
        }

        Empresa empresa = escopoUsuarioService.empresaVinculada(usuario);
        if (empresa == null) {
            return 0;
        }

        return switch (usuario.getPerfil()) {
            case GERADORA -> busca.isBlank()
                    ? loteRepository.countByEmpresaGeradoraIdAndStatus(empresa.getId(), status)
                    : loteRepository.countByEmpresaGeradoraIdAndStatusAndDescricaoContainingIgnoreCaseOrEmpresaGeradoraIdAndStatusAndTipoResiduoContainingIgnoreCase(
                            empresa.getId(), status, busca, empresa.getId(), status, busca);
            case TRANSPORTADORA -> busca.isBlank()
                    ? loteRepository.countPorTransportadoraEStatus(empresa.getId(), status)
                    : loteRepository.countPorTextoTransportadoraEStatus(empresa.getId(), busca, status);
            case RECEPTORA -> busca.isBlank()
                    ? loteRepository.countPorReceptoraEStatus(empresa.getId(), status)
                    : loteRepository.countPorTextoReceptoraEStatus(empresa.getId(), busca, status);
            default -> 0;
        };
    }

    private void validarLoteDentroDoEscopo(Lote lote, Usuario usuario) {
        if (usuario.getPerfil() == Perfil.ADMIN) {
            return;
        }

        Empresa empresa = escopoUsuarioService.empresaObrigatoria(usuario);
        if (usuario.getPerfil() != Perfil.GERADORA || !lote.getEmpresaGeradora().getId().equals(empresa.getId())) {
            throw new RegraNegocioException("Lote deve ser criado pela empresa geradora vinculada ao usuario");
        }
    }

    private void validarLeituraLote(Lote lote) {
        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        if (escopoUsuarioService.isAdmin(usuario)) {
            return;
        }

        Empresa empresa = escopoUsuarioService.empresaObrigatoria(usuario);
        boolean permitido = switch (usuario.getPerfil()) {
            case GERADORA -> lote.getEmpresaGeradora() != null && lote.getEmpresaGeradora().getId().equals(empresa.getId());
            case TRANSPORTADORA -> transporteVinculado(lote, empresa.getId(), Perfil.TRANSPORTADORA);
            case RECEPTORA -> transporteVinculado(lote, empresa.getId(), Perfil.RECEPTORA);
            default -> false;
        };

        if (!permitido) {
            throw new RecursoNaoEncontradoException("Lote nao encontrado");
        }
    }

    private boolean transporteVinculado(Lote lote, Long empresaId, Perfil perfil) {
        return switch (perfil) {
            case TRANSPORTADORA -> transporteRepository.existsByLoteIdAndTransportadoraId(lote.getId(), empresaId);
            case RECEPTORA -> transporteRepository.existsByLoteIdAndReceptoraId(lote.getId(), empresaId);
            default -> false;
        };
    }

    private Integer limiteNormalizado(Integer limite) {
        if (limite == null || limite <= 0) {
            return null;
        }

        return Math.min(limite, 100);
    }

    private Empresa buscarEmpresaGeradora(Lote lote) {
        if (lote.getEmpresaGeradora() == null) {
            throw new RegraNegocioException("Empresa geradora e obrigatoria para criar lote");
        }

        if (lote.getEmpresaGeradora().getId() != null) {
            return empresaRepository.findById(lote.getEmpresaGeradora().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa geradora nao encontrada"));
        }

        if (lote.getEmpresaGeradora().getPublicId() == null) {
            throw new RegraNegocioException("Empresa geradora e obrigatoria para criar lote");
        }

        return empresaRepository.findByPublicId(lote.getEmpresaGeradora().getPublicId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa geradora nao encontrada"));
    }

    private Lote descriptografarEmpresaGeradora(Lote lote) {
        if (lote.getEmpresaGeradora() != null) {
            Empresa empresa = lote.getEmpresaGeradora();
            empresa.setCnpj(criptografiaService.descriptografar(empresa.getCnpj()));
            empresa.setEmail(criptografiaService.descriptografar(empresa.getEmail()));
            empresa.setTelefone(criptografiaService.descriptografar(empresa.getTelefone()));
            empresa.setEndereco(criptografiaService.descriptografar(empresa.getEndereco()));
        }
        return lote;
    }
}
