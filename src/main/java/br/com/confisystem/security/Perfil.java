package br.com.confisystem.security;

public enum Perfil {

    OPERADOR("Operador", false),
    ADMINISTRADOR("Administrador", true);

    private final String descricao;
    private final boolean exigeSenha;

    Perfil(String descricao, boolean exigeSenha) {
        this.descricao = descricao;
        this.exigeSenha = exigeSenha;
    }

    public boolean exigeSenha() {
        return exigeSenha;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
