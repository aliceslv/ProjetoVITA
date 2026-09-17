package com.example.vita.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vita.data.network.FoodItem
import com.example.vita.databinding.ItemAlimentoBinding

class AlimentoAdapter(
    private var listaAlimentos: List<FoodItem>,
    private val onItemClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<AlimentoAdapter.AlimentoViewHolder>() {

    inner class AlimentoViewHolder(val binding: ItemAlimentoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentoViewHolder {
        val binding = ItemAlimentoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AlimentoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlimentoViewHolder, position: Int) {
        val alimento = listaAlimentos[position]

        // Atribui apenas o nome do alimento ao TextView existente no seu XML
        holder.binding.txtNomeAlimento.text = alimento.foodName

        holder.itemView.setOnClickListener {
            onItemClick(alimento)
        }
    }

    override fun getItemCount(): Int = listaAlimentos.size

    fun atualizarLista(novaLista: List<FoodItem>) {
        listaAlimentos = novaLista
        notifyDataSetChanged()
    }
}