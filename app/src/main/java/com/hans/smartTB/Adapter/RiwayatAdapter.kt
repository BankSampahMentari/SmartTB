package com.hans.smartTB.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.hans.smartTB.Model.dataRiwayat
import com.hans.smartTB.R
import com.hans.smartTB.detailRiwayat
import org.w3c.dom.Text


class RiwayatAdapter(private val context: Context, private var ListRiwayat: MutableList<dataRiwayat>) : RecyclerView.Adapter<RiwayatAdapter.MyViewHolder>() {

    class MyViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {
        val namaNasabah :TextView = itemView.findViewById(R.id.recNamaNasabah)
        val tanggal : TextView  = itemView.findViewById(R.id.recTanggal)
        val status: TextView = itemView.findViewById(R.id.recstatus)
        val pendapatan : TextView = itemView.findViewById(R.id.recPendapatan)
        val email : TextView = itemView.findViewById(R.id.recEmail)
        val cardRiwayat: TextView = itemView.findViewById(R.id.recCardRiwayat)
        val docID : TextView = itemView.findViewById(R.id.docID)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val CardListPesanan =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_riwayat, parent, false)
        return RiwayatAdapter.MyViewHolder(CardListPesanan)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.cardRiwayat.setOnClickListener {
            val intent = Intent(context, detailRiwayat::class.java)
//            intent.putExtra("docID", ListRiwayat[position].docID)
            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return ListRiwayat.size
    }
}