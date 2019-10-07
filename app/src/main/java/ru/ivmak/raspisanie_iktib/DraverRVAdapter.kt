package ru.ivmak.raspisanie_iktib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.draver_rv_item.view.*

class DraverRVAdapter(private var data: ArrayList<Choice>, val listener: OnItemClickListener) : RecyclerView.Adapter<DraverRVAdapter.DraverViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraverViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.draver_rv_item, parent, false)

        return DraverViewHolder(view)
    }

    interface OnItemClickListener {
        fun onItemClick(data: Choice)
    }

    override fun getItemCount(): Int = data.size

    fun setData(data: ArrayList<Choice>) {
        this.data.clear()
        this.data.addAll(data)
    }

    override fun onBindViewHolder(holder: DraverViewHolder, position: Int) {
        holder.bind(data[position], listener)
    }

    class DraverViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val caption = view.caption

        fun bind(data: Choice, listener: OnItemClickListener) {
            caption.text = data.name
            view.setOnClickListener {
                listener.onItemClick(data)
            }
        }
    }
}