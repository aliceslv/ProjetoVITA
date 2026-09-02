package com.example.vita

import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    // Autenticação
    var nome: String = ""
    var email: String = ""
    var senha: String = ""

    // Onboarding / Saúde
    var meta: String = ""
    var peso: String = ""
    var altura: String = ""
    var pesoMeta: String = ""
    var sexo: String = ""
    var nascimento: String = ""
    var nivelAtividade: String = ""
    var idrCalculado: Int = 0
}