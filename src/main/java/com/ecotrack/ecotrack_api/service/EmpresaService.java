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
        if (limiteNormalizado(limite) == null) {
            if (termoBusca == null || termoBusca.isBlank()) {
                return empresaRepository.findAll().stream()
                        .map(this::descriptografarDadosSensiveis)
                        .toList();
            }

            return empresaRepository.findByRazaoSocialContainingIgnoreCaseOrderByCriadoEmDesc(
                            termoBusca.trim(),
                            PageRequest.of(0, Integer.MAX_VALUE)
                    ).stream()
                    .map(this::descriptografarDadosSensiveis)
                    .toList();
        }

        PageRequest pageRequest = PageRequest.of(0, limiteNormalizado(limite));
        if (termoBusca != null && !termoBusca.isBlank()) {
            return empresaRepository.findByRazaoSocialContainingIgnoreCaseOrderByCriadoEmDesc(termoBusca.trim(), pageRequest).stream()
                    .map(this::descriptografarDadosSensiveis)
                    .toList();
        }

        return empresaRepository.findAllByOrderByCriadoEmDesc(pageRequest).stream()
                .map(this::descriptografarDadosSensiveis)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpresaPaginaResponse listarPagina(String termoBusca, Integer pagina, Integer limite) {
        int limiteConsulta = limitePaginado(limite);
        int paginaConsulta = paginaNormalizada(pagina);
        PageRequest pageRequest = PageRequest.of(paginaConsulta, limiteConsulta + 1);
        String busca = termoBusca == null ? "" : termoBusca.trim();

        List<Empresa> empresas = busca.isBlank()
                ? empresaRepository.findAllByOrderByCriadoEmDesc(pageRequest)
                : empresaRepository.findByRazaoSocialContainingIgnoreCaseOrderByCriadoEmDesc(busca, pageRequest);

        boolean temProxima = empresas.size() > limiteConsulta;
        List<Empresa> itens = (temProxima ? empresas.subList(0, limiteConsulta) : empresas).stream()
                .map(this::descriptografarDadosSensiveis)
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
        return descriptografarDadosSensiveis(empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada")));
    }

    @Transactional(readOnly = true)
    public Empresa buscarPorPublicId(UUID publicId) {
        return descriptografarDadosSensiveis(empresaRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada")));
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
        if (!empresaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Empresa não encontrada");
        }

        empresaRepository.deleteById(id);
    }

    public void deletarPorPublicId(UUID publicId) {
        Empresa empresa = empresaRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada"));
        empresaRepository.delete(empresa);
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

    private Empresa descriptografarDadosSensiveis(Empresa empresa) {
        empresa.setCnpj(criptografiaService.descriptografar(empresa.getCnpj()));
        empresa.setEmail(criptografiaService.descriptografar(empresa.getEmail()));
        empresa.setTelefone(criptografiaService.descriptografar(empresa.getTelefone()));
        empresa.setEndereco(criptografiaService.descriptografar(empresa.getEndereco()));
        return empresa;
    }
}
