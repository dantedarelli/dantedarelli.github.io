package br.com.viajarelegal.airbnb.data

/**
 * Autenticação de demonstração: as credenciais são fixas no código, como
 * combinado para este exemplo. Não há cadastro, servidor nem armazenamento
 * de sessão — trocar isto por um provedor real é o único ponto a mexer.
 */
object Auth {

    private const val USUARIO = "dante"
    private const val SENHA = "dante123"

    const val NOME_EXIBICAO = "Dante Darelli"
    const val CARGO = "Analista de dados · Viajar é Legal"
    const val EMAIL = "dante.darelli@hotmail.com"

    sealed interface Resultado {
        data object Ok : Resultado
        data class Erro(val mensagem: String) : Resultado
    }

    fun entrar(usuario: String, senha: String): Resultado {
        val u = usuario.trim()
        return when {
            u.isEmpty() -> Resultado.Erro("Informe o usuário.")
            senha.isEmpty() -> Resultado.Erro("Informe a senha.")
            !u.equals(USUARIO, ignoreCase = true) || senha != SENHA ->
                Resultado.Erro("Usuário ou senha inválidos.")
            else -> Resultado.Ok
        }
    }

    /** Iniciais para o avatar do cabeçalho. */
    val iniciais: String
        get() = NOME_EXIBICAO.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
}
