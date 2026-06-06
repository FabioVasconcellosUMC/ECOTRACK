package com.ecotrack.ecotrack_api.service;

import com.ecotrack.ecotrack_api.entity.Empresa;
import com.ecotrack.ecotrack_api.entity.Perfil;
import com.ecotrack.ecotrack_api.entity.Usuario;
import com.ecotrack.ecotrack_api.exception.RegraNegocioException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class EscopoUsuarioService {

    public Usuario usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new RegraNegocioException("Usuario autenticado nao identificado");
        }

        return usuario;
    }

    public boolean isAdmin(Usuario usuario) {
        return usuario.getPerfil() == Perfil.ADMIN;
    }

    public Empresa empresaObrigatoria(Usuario usuario) {
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null || empresa.getId() == null) {
            throw new RegraNegocioException("Cadastre uma empresa antes de acessar dados operacionais");
        }

        return empresa;
    }

    public Empresa empresaVinculada(Usuario usuario) {
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null || empresa.getId() == null) {
            return null;
        }

        return empresa;
    }
}
