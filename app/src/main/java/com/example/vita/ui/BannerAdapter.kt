package com.example.vita.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vita.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.NumberFormat
import java.util.Locale

class BannerAdapter(
    private var listaBanners: List<BannerData>,
    private val onSetaClique: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BANNER_1 = 1
        private const val VIEW_TYPE_BANNER_2 = 2
    }

    override fun getItemViewType(position: Int): Int {
        return listaBanners[position].tipo
    }

    // ViewHolder para o Banner 1 (Metas de Calorias)
    inner class Banner1ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCaloriasRestantesValor: TextView = itemView.findViewById(R.id.txtCaloriasRestantesValor)
        private val progressCalorias: CircularProgressIndicator = itemView.findViewById(R.id.progressCalorias)
        private val txtPorcentagemCalorias: TextView = itemView.findViewById(R.id.txtPorcentagemCalorias)
        private val txtConsumidas: TextView = itemView.findViewById(R.id.txtConsumidas)
        private val txtMeta: TextView = itemView.findViewById(R.id.txtMeta)
        private val icVerDetalhes: View? = itemView.findViewById(R.id.icVerDetalhes)

        fun bind(item: BannerData) {
            val nf = NumberFormat.getNumberInstance(Locale("pt", "BR"))

            txtCaloriasRestantesValor.text = nf.format(item.restantes)
            txtConsumidas.text = nf.format(item.consumidas)
            txtMeta.text = nf.format(item.metaCalorias)
            txtPorcentagemCalorias.text = "${item.porcentagem}%"
            progressCalorias.progress = item.porcentagem

            icVerDetalhes?.setOnClickListener {
                onSetaClique()
            }
        }
    }

    // ViewHolder para o Banner 2 (HemoConecta)
    inner class Banner2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icVerDetalhes: View? = itemView.findViewById(R.id.icVerDetalhes)

        fun bind() {
            icVerDetalhes?.setOnClickListener {
                onSetaClique()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_BANNER_2) {
            val view = inflater.inflate(R.layout.banner2, parent, false)
            Banner2ViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.banner1, parent, false)
            Banner1ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = listaBanners[position]
        if (holder is Banner1ViewHolder) {
            holder.bind(item)
        } else if (holder is Banner2ViewHolder) {
            holder.bind()
        }
    }

    override fun getItemCount(): Int = listaBanners.size

    fun atualizarDados(novaLista: List<BannerData>) {
        listaBanners = novaLista
        notifyDataSetChanged()
    }
}