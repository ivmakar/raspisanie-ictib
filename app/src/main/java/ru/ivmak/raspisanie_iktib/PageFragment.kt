package ru.ivmak.raspisanie_iktib


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PAGE = "ARG_PAGE"


/**
 * A simple [Fragment] subclass.
 *
 */
class PageFragment : Fragment() {


    private var mPage: Int = 0
    private lateinit var table: Table
    private var adapter = MyAdapter(arrayListOf())
    private lateinit var rv: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_page, container, false)

        val viewModel: MainViewModel = activity?.let { ViewModelProviders.of(it).get(MainViewModel::class.java) }!!

        val textView = view.findViewById<TextView>(R.id.textView)

        rv = view.findViewById(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(this.context)
        rv.adapter = adapter

        viewModel.timeTable.observe(this, Observer {
            textView.text = it.table.table[mPage + 2][0]
            var data = ArrayList<DisplayData>()
            data.add(DisplayData())
            for (i in 1..7) {
                if (it.table.table[mPage + 2][i] == "") {
                    if (data.last().isEmpty) {
                        data.last().windowCount++

                    } else {
                        data.add(DisplayData())
                        data.last().windowCount++
                    }
                } else {
                    data.add(DisplayData(it.table.table[1][i], it.table.table[mPage + 2][i], i, false, 0))
                }
            }
            adapter.setData(data)
            adapter.notifyDataSetChanged()
        })

        return view
    }

    companion object {

        fun newInstance(page: Int): PageFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = PageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mPage = arguments!!.getInt(ARG_PAGE)
        }
    }

}
