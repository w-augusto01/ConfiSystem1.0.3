package br.com.confisystem.security;

import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class Sessao {

    private static final String SENHA_ADMIN = "adm26";

    private static final Set<Modulo> MODULOS_OPERADOR =
            EnumSet.of(Modulo.ORDEM_PRODUCAO, Modulo.CONFINAMENTO);

    private Perfil perfilAtual;

    public boolean entrar(Perfil perfil, String senha) {

        if (perfil == null) {
            return false;
        }

        if (perfil.exigeSenha() && !SENHA_ADMIN.equals(senha)) {
            return false;
        }

        this.perfilAtual = perfil;
        return true;
    }

    public void sair() {
        this.perfilAtual = null;
    }

    public Perfil getPerfilAtual() {
        return perfilAtual;
    }

    public boolean isAdministrador() {
        return perfilAtual == Perfil.ADMINISTRADOR;
    }

    public boolean podeAcessar(Modulo modulo) {
        if (perfilAtual == null) {
            return false;
        }
        return isAdministrador() || MODULOS_OPERADOR.contains(modulo);
    }
}
