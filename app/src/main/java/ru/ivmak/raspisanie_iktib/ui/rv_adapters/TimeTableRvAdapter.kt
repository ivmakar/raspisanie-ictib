package ru.ivmak.raspisanie_iktib.ui.rv_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rv_item.view.*
import ru.ivmak.raspisanie_iktib.data.DisplayData
import ru.ivmak.raspisanie_iktib.R

class TimeTableRvAdapter(private var data: ArrayList<DisplayData>) : RecyclerView.Adapter<TimeTableRvAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item, parent, false)

        return MyViewHolder(
            view
        )
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
        val num = view.num_of_pair

        fun bind(data: DisplayData) {
            if (data.isEmpty) {
                body.visibility = View.GONE
                time.text = "Окно"
            } else {
                body.visibility = View.VISIBLE
                time.text = data.time
                body.text = data.body
            }
            num.text = data.numOfPair.toString()
        }
    }
}