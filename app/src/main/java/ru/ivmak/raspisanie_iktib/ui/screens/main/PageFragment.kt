package ru.ivmak.raspisanie_iktib.ui.screens.main


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.ivmak.raspisanie_iktib.data.DisplayData
import ru.ivmak.raspisanie_iktib.ui.rv_adapters.TimeTableRvAdapter
import ru.ivmak.raspisanie_iktib.R
import ru.ivmak.raspisanie_iktib.data.Table


private const val ARG_PAGE = "ARG_PAGE"


/**
 * A simple [Fragment] subclass.
 *
 */
class PageFragment : Fragment() {


    private var mPage: Int = 0
    private lateinit var table: Table
    private var adapter = TimeTableRvAdapter(arrayListOf())
    private lateinit var rv: RecyclerView

//    val dataSingleton = MainViewModel.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_page, container, false)

        val textView = view.findViewById<TextView>(R.id.textView)

        rv = view.findViewById(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(this.context)
        rv.adapter = adapter

//        dataSingleton.timeTable.observe(viewLifecycleOwner, Observer {
//            if (it.table != null) {
//                textView.text = it.table!!.table[mPage + 2][0]
//                var data = ArrayList<DisplayData>()
//                for (i in 1..7) {
//                    if (it.table!!.table[mPage + 2][i] == "") {
//                        data.add(DisplayData())
//                    } else {
//                        data.add(
//                            DisplayData(
//                                it.table!!.table[1][i],
//                                it.table!!.table[mPage + 2][i],
//                                i,
//                                false,
//                                0
//                            )
//                        )
//                    }
//                    data.last().numOfPair = i
//                }
//                adapter.setData(data)
//                adapter.notifyDataSetChanged()
//            }
//        })

        return view
    }

    companion object {

        fun newInstance(page: Int): PageFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment =
                PageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mPage = requireArguments().getInt(ARG_PAGE)
        }
    }

}
