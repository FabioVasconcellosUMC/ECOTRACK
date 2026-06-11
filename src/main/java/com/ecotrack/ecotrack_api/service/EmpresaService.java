package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.dto.EmpresaPaginaResponse;
import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.TipoEmpresa;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RecursoNaoEncontradoException;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import com.ecotrack.ecotrack_api.repository.EmpresaRepository;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.validation.TextoSeguro;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private static final String SEM_TAGS = "^[^<>]*$";
    private static final String CNPJ_VALIDO = "^[0-9./-]+$";
    private static final String TELEFONE_VALIDO = "^[0-9()+\\-\\s.]*$";
    private static final String EMAIL_VALIDO = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DadosPessoaisCriptografiaService criptografiaService;
    private final EscopoUsuarioService escopoUsuarioService;

    @Transactional(readOnly = true)
    public List<Empresa> listar() {
        return listar(null, null);
    }

    @Transactional(readOnly = true)
    public List<Empresa> listar(String termoBusca, Integer limite) {
        Usuario usuario = usuarioAtual();

        if (limiteNormalizado(limite) == null) {
            if (termoBusca == null || termoBusca.isBlank()) {
                return empresaRepository.findAll().stream()
                        .filter(empresa -> podeListarEmpresa(empresa, usuario))
                        .map(empresa -> prepararEmpresaParaResposta(empresa, usuario))
                        .toList();
            }

            return empresaRepository.findByRazaoSocialContainingIgnoreCaseOrderByCriadoEmDesc(
                            termoBusca.trim(),
                            PageRequest.of(0, Integer.MAX_VALUE)
                    ).stream()
                    .filter(empresa -> podeListarEmpresa(empresa, usuario))
                    .map(empresa -> prepararEmpresaParaResposta(empresa, usuario))
                    .toList();
        }

        PageRequest pageRequest = PageRequest.of(0, limiteNormalizado(limite));
        if (termoBusca != null && !termoBusca.isBlank()) {
            return empresaRepository.findByRazaoSocialContainingIgnoreCaseOrderByCriadoEmDesc(termoBusca.trim(), pageRequest).stream()
                    .filter(empresa -> podeListarEmpresa(empresa, usuario))
                    .map(empresa -> prepararEmpresaParaResposta(empresa, usuario))
                    .toList();
        }

        return empresaRepository.findAllByOrderByCriadoEmDesc(pageRequest).stream()
                .filter(empresa -> podeListarEmpresa(empresa, usuario))
                .map(empresa -> prepararEmpresaParaResposta(empresa, usuario))
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpresaPaginaResponse listarPagina(String termoBusca, Integer pagina, Integer limite) {
        Usuario usuario = usuarioAtual();
        int limiteConsulta = limitePaginado(limite);
        int paginaConsulta = paginaNormalizada(pagina);
        PageRequest pageRequest = PageRequest.of(paginaConsulta, limiteConsulta + 1);
        String busca = termoBusca == null ? "" : termoBusca.trim();

        List<Empresa> empresas = busca.isBlank()
                ? empresaRepository.findAllByOrderByCriadoEmDesc(pageRequest)
                : empresaRepository.findByRazaoSocialContainingIgnoreCaseOrderByCriadoEmDesc(busca, pageRequest);

        boolean temProxima = empresas.size() > limiteConsulta;
        List<Empresa> itens = (temProxima ? empresas.subList(0, limiteConsulta) : empresas).stream()
                .filter(empresa -> podeListarEmpresa(empresa, usuario))
                .map(empresa -> prepararEmpresaParaResposta(empresa, usuario))
                .toList();

        return new EmpresaPaginaResponse(
                itens,
                paginaConsulta,
                limiteConsulta,
                temProxima,
                totalEmpresas(busca),
                totalEmpresasPorTipo(busca, TipoEmpresa.GERADORA),
                totalEmpresasPorTipo(busca, TipoEmpresa.TRANSPORTADORA),
                totalEmpresasPorTipo(busca, TipoEmpresa.RECEPTORA)
        );
    }

    @Transactional(readOnly = true)
    public Empresa buscarPorId(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada"));
        return prepararEmpresaParaResposta(empresa, usuarioAtual());
    }

    @Transactional(readOnly = true)
    public Empresa buscarPorPublicId(UUID publicId) {
        Empresa empresa = empresaRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada"));
        Usuario usuario = usuarioAtual();
        if (!escopoUsuarioService.isAdmin(usuario) && !empresa.isAtiva()) {
            throw new RecursoNaoEncontradoException("Empresa nao encontrada");
        }
        return prepararEmpresaParaResposta(empresa, usuario);
    }

    public Empresa salvar(Empresa empresa) {
        validarDadosAbertos(empresa);
        Usuario usuario = escopoUsuarioService.usuarioAutenticado();
        validarCadastroPermitidoParaPerfil(empresa, usuario);
        prepararDadosSensiveis(empresa);
        Empresa salva = empresaRepository.save(empresa);
        vincularEmpresaAoUsuario(salva, usuario);
        return descriptografarDadosSensiveis(salva);
    }

    public void deletar(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada"));
        excluirLogicamente(empresa);
    }

    public void deletarPorPublicId(UUID publicId) {
        Empresa empresa = empresaRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada"));
        excluirLogicamente(empresa);
    }

    private void prepararDadosSensiveis(Empresa empresa) {
        String cnpjNormalizado = criptografiaService.normalizarCnpj(empresa.getCnpj());
        String cnpjHash = criptografiaService.hashBusca(cnpjNormalizado);

        if (cnpjHash != null && empresaRepository.existsByCnpjHash(cnpjHash)) {
            throw new RegraNegocioException("CNPJ ja cadastrado");
        }

        empresa.setCnpjHash(cnpjHash);
        empresa.setEmailHash(criptografiaService.hashBusca(criptografiaService.normalizarEmail(empresa.getEmail())));
        empresa.setCnpj(criptografiaService.criptografar(cnpjNormalizado));
        empresa.setEmail(criptografiaService.criptografar(criptografiaService.normalizarEmail(empresa.getEmail())));
        empresa.setTelefone(criptografiaService.criptografar(empresa.getTelefone()));
        empresa.setEndereco(criptografiaService.criptografar(empresa.getEndereco()));
    }

    private void validarDadosAbertos(Empresa empresa) {
        TextoSeguro.validar(empresa.getRazaoSocial(), "Razao social");
        TextoSeguro.validar(empresa.getEndereco(), "Endereco");
        validarObrigatorio(empresa.getCnpj(), "CNPJ e obrigatorio");
        validarTamanho(empresa.getCnpj(), 18, "CNPJ deve ter no maximo 18 caracteres");
        validarPadrao(empresa.getCnpj(), CNPJ_VALIDO, "CNPJ deve conter apenas numeros, pontos, barra e hifen");
        if (empresa.getTipo() == null) {
            throw new RegraNegocioException("Tipo da empresa e obrigatorio");
        }
        validarTamanho(empresa.getEndereco(), 300, "Endereco deve ter no maximo 300 caracteres");
        validarPadrao(empresa.getEndereco(), SEM_TAGS, "Endereco nao pode conter tags HTML ou scripts");
        validarTamanho(empresa.getEmail(), 150, "E-mail deve ter no maximo 150 caracteres");
        validarPadrao(empresa.getEmail(), EMAIL_VALIDO, "E-mail invalido");
        validarTamanho(empresa.getTelefone(), 20, "Telefone deve ter no maximo 20 caracteres");
        validarPadrao(empresa.getTelefone(), TELEFONE_VALIDO, "Telefone deve conter apenas numeros e caracteres telefonicos");
    }

    private void validarObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private void validarTamanho(String valor, int tamanhoMaximo, String mensagem) {
        if (valor != null && valor.length() > tamanhoMaximo) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private void validarPadrao(String valor, String padrao, String mensagem) {
        if (valor != null && !valor.matches(padrao)) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private void validarCadastroPermitidoParaPerfil(Empresa empresa, Usuario usuario) {
        Perfil perfil = usuario.getPerfil();

        if (perfil == Perfil.ADMIN) {
            return;
        }

        if (usuario.getEmpresa() != null) {
            throw new RegraNegocioException("Usuario ja possui empresa vinculada");
        }

        TipoEmpresa tipoPermitido = switch (perfil) {
            case GERADORA -> TipoEmpresa.GERADORA;
            case TRANSPORTADORA -> TipoEmpresa.TRANSPORTADORA;
            case RECEPTORA -> TipoEmpresa.RECEPTORA;
            default -> throw new RegraNegocioException("Perfil sem permissao para cadastrar empresas");
        };

        if (empresa.getTipo() != tipoPermitido) {
            throw new RegraNegocioException("Seu perfil permite cadastrar apenas empresa do tipo " + tipoPermitido.name());
        }
    }

    private void vincularEmpresaAoUsuario(Empresa empresa, Usuario usuario) {
        if (usuario.getPerfil() == Perfil.ADMIN) {
            return;
        }

        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);
    }

    private Integer limiteNormalizado(Integer limite) {
        if (limite == null || limite <= 0) {
            return null;
        }

        return Math.min(limite, 100);
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

    private long totalEmpresas(String busca) {
        if (busca == null || busca.isBlank()) {
            return empresaRepository.count();
        }

        return empresaRepository.countByRazaoSocialContainingIgnoreCase(busca);
    }

    private long totalEmpresasPorTipo(String busca, TipoEmpresa tipo) {
        if (busca == null || busca.isBlank()) {
            return empresaRepository.countByTipo(tipo);
        }

        return empresaRepository.countByRazaoSocialContainingIgnoreCaseAndTipo(busca, tipo);
    }

    private Usuario usuarioAtual() {
        return escopoUsuarioService.usuarioAutenticado();
    }

    private boolean podeListarEmpresa(Empresa empresa, Usuario usuario) {
        return escopoUsuarioService.isAdmin(usuario) || empresa.isAtiva();
    }

    private Empresa prepararEmpresaParaResposta(Empresa empresa, Usuario usuario) {
        if (escopoUsuarioService.isAdmin(usuario) || empresaDoUsuario(empresa, usuario)) {
            return descriptografarDadosSensiveis(empresa);
        }

        return resumoOperacional(empresa);
    }

    private boolean empresaDoUsuario(Empresa empresa, Usuario usuario) {
        return usuario != null
                && usuario.getEmpresa() != null
                && usuario.getEmpresa().getId() != null
                && empresa.getId() != null
                && usuario.getEmpresa().getId().equals(empresa.getId());
    }

    private Empresa resumoOperacional(Empresa empresa) {
        Empresa resumo = new Empresa();
        resumo.setPublicId(empresa.getPublicId());
        resumo.setRazaoSocial(empresa.getRazaoSocial());
        resumo.setTipo(empresa.getTipo());
        resumo.setAtiva(empresa.isAtiva());
        resumo.setCriadoEm(empresa.getCriadoEm());
        return resumo;
    }

    private void excluirLogicamente(Empresa empresa) {
        if (!empresa.isAtiva()) {
            throw new RegraNegocioException("Empresa ja esta inativa");
        }

        String sufixo = empresa.getPublicId() == null
                ? String.valueOf(empresa.getId())
                : empresa.getPublicId().toString().substring(0, 8);

        empresa.setAtiva(false);
        empresa.setRazaoSocial("Empresa excluida " + sufixo);
        empresa.setCnpjHash(null);
        empresa.setEmailHash(null);
        empresa.setCnpj(criptografiaService.criptografar("cnpj-removido-" + sufixo));
        empresa.setEmail(criptografiaService.criptografar("empresa-removida-" + sufixo + "@anonimo.local"));
        empresa.setTelefone(criptografiaService.criptografar("telefone-removido"));
        empresa.setEndereco(criptografiaService.criptografar("endereco-removido"));
        empresaRepository.save(empresa);
    }

    private Empresa descriptografarDadosSensiveis(Empresa empresa) {
        empresa.setCnpj(criptografiaService.descriptografar(empresa.getCnpj()));
        empresa.setEmail(criptografiaService.descriptografar(empresa.getEmail()));
        empresa.setTelefone(criptografiaService.descriptografar(empresa.getTelefone()));
        empresa.setEndereco(criptografiaService.descriptografar(empresa.getEndereco()));
        return empresa;
    }
}
