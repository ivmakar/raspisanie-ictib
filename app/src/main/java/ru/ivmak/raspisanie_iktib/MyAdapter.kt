package ru.ivmak.raspisanie_iktib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rv_item.view.*

class MyAdapter(private var data: ArrayList<DisplayData>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item, parent, false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    fun setData(data: ArrayList<DisplayData>) {
        this.data.clear()
        this.data.addAll(data)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(data[position])
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val time = view.time
        val body = view.body

        fun bind(data: DisplayData) {
            if (data.isEmpty) {
                body.visibility = View.GONE
                if (data.windowCount == 1) {
                    time.text = data.windowCount.toString() + " окно"
                }
                if (data.windowCount > 4) {
                    time.text = data.windowCount.toString() + " окон"
                }
                if (data.windowCount > 1 && data.windowCount < 5) {
                    time.text = data.windowCount.toString() + " окна"
                }
            } else {
                body.visibility = View.VISIBLE
                time.text = data.time
                body.text = data.body
            }
        }
    }
}