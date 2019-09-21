package ru.ivmak.raspisanie_iktib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView


class MainActivity : AppCompatActivity() {

    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager>(R.id.viewpager)

        val tabLayout = findViewById<TabLayout>(R.id.sliding_tabs)

        viewPager.adapter = SimpleFragmentPagerAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)

        getData("КТмо1-4")

    }

    fun getData(group: String) {
        val viewModel: MainViewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }
        GlobalScope.launch {
            var timeTable = viewModel.getTimeTable(group)
            withContext(Dispatchers.Main) {
                viewModel.timeTable.value = timeTable
                menu?.let { it.getItem(1).title = timeTable.table.week.toString() + " неделя" }
                title = timeTable.table.name
            }
        }
    }

    fun getData(group: String, week: Int) {
        val viewModel: MainViewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }
        GlobalScope.launch {
            var timeTable = viewModel.getTimeTableByWeek(group, week)
            withContext(Dispatchers.Main) {
                viewModel.timeTable.value = timeTable
                menu?.let { it.getItem(1).title = timeTable.table.week.toString() + " неделя" }
                title = timeTable.table.name
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        val myActionMenuItem = menu!!.findItem(R.id.search)
        val searchView = myActionMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Toast like print
//                UserFeedback.show("SearchOnQueryTextSubmit: $query")
                if (!searchView.isIconified()) {
                    searchView.setIconified(true)
                }
                myActionMenuItem.collapseActionView()
                getData(query)
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                // UserFeedback.show( "SearchOnQueryTextChanged: " + s);
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.select_week -> {
                val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
                val popup =
                    PopupMenu(this@MainActivity, this@MainActivity.findViewById(R.id.select_week)) //item!!.actionView
                for (i in viewModel.timeTable.value!!.weeks) {
                    popup.menu.add("$i неделя")
                }
                popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
                popup.setOnMenuItemClickListener {
                    getData(
                        viewModel.timeTable.value!!.table.group,
                        Integer.parseInt(it.title.substring(0, it.title.indexOf(' ')))
                    )
                    true
                }

                popup.show()
            }
//            R.id.search -> {
//
//            }
        }
        return super.onOptionsItemSelected(item)
    }


}
