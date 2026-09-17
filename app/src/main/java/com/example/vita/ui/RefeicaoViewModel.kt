package com.example.vita

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vita.data.model.AlimentoConsumido

class RefeicaoViewModel : ViewModel() {

    private val _listaAlimentos = MutableLiveData<MutableList<AlimentoConsumido>>(mutableListOf())
    val listaAlimentos: LiveData<MutableList<AlimentoConsumido>> get() = _listaAlimentos

    fun adicionarAlimento(alimento: AlimentoConsumido) {
        val listaAtual = _listaAlimentos.value ?: mutableListOf()
        listaAtual.add(alimento)
        _listaAlimentos.value = listaAtual
    }

    fun removerAlimento(alimento: AlimentoConsumido) {
        val listaAtual = _listaAlimentos.value ?: mutableListOf()
        listaAtual.remove(alimento)
        _listaAlimentos.value = listaAtual
    }

    fun limparCarrinho() {
        _listaAlimentos.value = mutableListOf()
    }

    fun obterTotalCalorias(): Float = _listaAlimentos.value?.sumOf { it.calorias.toDouble() }?.toFloat() ?: 0f
}