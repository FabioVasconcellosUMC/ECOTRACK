package com.ecotrack.ecotrack_api.security;

import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.repository.UsuarioRepository;
import com.ecotrack.ecotrack_api.service.DadosPessoaisCriptografiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final DadosPessoaisCriptografiaService criptografiaService;

    @Override
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {
        return buscarUsuario(identificador)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado ou inativo: " + identificador));
    }

    private Optional<Usuario> buscarUsuario(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return Optional.empty();
        }

        if (identificador.contains("@")) {
            String emailNormalizado = criptografiaService.normalizarEmail(identificador);
            String emailHash = criptografiaService.hashBusca(emailNormalizado);
            return usuarioRepository.findByEmailHash(emailHash)
                    .or(() -> usuarioRepository.findByEmail(emailNormalizado));
        }

        return usuarioRepository.findByEmailHash(identificador);
    }
}
