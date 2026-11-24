package com.example.vita.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vita.ui.AlimentoConsumido

class RefeicaoViewModel : ViewModel() {

    private val _listaAlimentos =
        MutableLiveData<List<AlimentoConsumido>>(emptyList())

    val listaAlimentos: LiveData<List<AlimentoConsumido>>
        get() = _listaAlimentos

    fun adicionarAlimento(alimento: AlimentoConsumido) {
        val listaAtual = _listaAlimentos.value.orEmpty()
        _listaAlimentos.value = listaAtual + alimento
    }

    fun removerAlimento(alimento: AlimentoConsumido) {
        val listaAtual = _listaAlimentos.value.orEmpty()
        _listaAlimentos.value = listaAtual - alimento
    }

    fun limparCarrinho() {
        _listaAlimentos.value = emptyList()
    }

    fun obterTotalCalorias(): Float {
        return _listaAlimentos.value
            .orEmpty()
            .sumOf { it.calorias.toDouble() }
            .toFloat()
    }
}
