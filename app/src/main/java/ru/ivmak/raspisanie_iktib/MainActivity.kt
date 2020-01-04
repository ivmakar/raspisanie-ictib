package ru.ivmak.raspisanie_iktib

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson

const val LAST_TT = "last_time_table"

class MainActivity : AppCompatActivity(), DraverRVAdapter.OnItemClickListener {

    private var menu: Menu? = null
    lateinit var toolbar: Toolbar
    private var adapter = DraverRVAdapter(arrayListOf(), this)
    private lateinit var draverRV: RecyclerView

    fun verifyAvailableNetwork():Boolean{
        val viewModel: MainViewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo= connectivityManager.activeNetworkInfo
        val isConnect = networkInfo!=null && networkInfo.isConnected
        val textIsConnect = findViewById<TextView>(R.id.text_unconnect)
        if (!isConnect) {
            textIsConnect.visibility = View.VISIBLE
            adapter.setData(arrayListOf())
            viewModel.isConnection = false
        } else {
            textIsConnect.visibility = View.INVISIBLE
            viewModel.isConnection = true
        }
        return isConnect
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) { }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) { }

            override fun onDrawerOpened(drawerView: View) { }

            override fun onDrawerClosed(drawerView: View) {
                val imm = this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(this@MainActivity.window.decorView.windowToken, 0)
            }

        })
        toggle.syncState()

        val viewPager = findViewById<ViewPager>(R.id.viewpager)

        val tabLayout = findViewById<TabLayout>(R.id.sliding_tabs)

        viewPager.adapter = SimpleFragmentPagerAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)



        draverRV = findViewById(R.id.draver_rv)
        draverRV.layoutManager = LinearLayoutManager(this)
        draverRV.adapter = adapter

        val viewModel: MainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel.choices.observe(this, Observer {
            val textIsEmpty = findViewById<TextView>(R.id.text_empty)
            if (it.isEmpty()) {
                textIsEmpty.visibility = View.VISIBLE
            } else {
                textIsEmpty.visibility = View.INVISIBLE
            }
            adapter.setData(it)
            adapter.notifyDataSetChanged()
        })

        viewModel.timeTable.observe(this, Observer {
            initAppBar(it)
            saveText(Gson().toJson(it))
        })

        val searchEditText = findViewById<TextInputEditText>(R.id.search_edit_text)
        searchEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getData(s.toString())
            }

        })

        GlobalScope.launch {
            viewModel.initTimeTable(loadText())

        }

        verifyAvailableNetwork()

    }

    override fun onItemClick(data: Choice) {
        val viewModel: MainViewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        if (!verifyAvailableNetwork()) {
            return
        }
        GlobalScope.launch {
            viewModel.getTimeTable(data.group)
            withContext(Dispatchers.Main) {
                initAppBar(viewModel.timeTable.value!!)
            }
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun getData(query: String) {
        if (!verifyAvailableNetwork()) {
            return
        }
        val viewModel: MainViewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }
        GlobalScope.launch {
            viewModel.searchByQuery(query)
        }
    }

    fun getData(group: String, week: Int) {
        if (!verifyAvailableNetwork()) {
            return
        }
        val viewModel: MainViewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }
        GlobalScope.launch {
            viewModel.getTimeTableByWeek(group, week)
        }
    }

    private fun initAppBar(timeTable: TimeTable) {
        timeTable.table?.let { table -> menu?.let { it.getItem(0).title = table.week.toString() + " неделя" } }
        timeTable.table?.let { title = it.name }
        val viewModel: MainViewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }
        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        timeTable.table?.let { viewPager.currentItem = viewModel.isDayOfWeekOpen(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.select_week -> {
                val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
                val popup =
                    PopupMenu(this@MainActivity, this@MainActivity.findViewById(R.id.select_week)) //item!!.actionView
                for (i in viewModel.timeTable.value!!.weeks!!) {
                    popup.menu.add("$i неделя")
                }
                popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
                popup.setOnMenuItemClickListener {
                    getData(
                        viewModel.timeTable.value!!.table!!.group,
                        Integer.parseInt(it.title.substring(0, it.title.indexOf(' ')))
                    )
                    true
                }

                popup.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun saveText(data: String) {
        val sPref = getPreferences(MODE_PRIVATE);
        val ed: SharedPreferences.Editor = sPref.edit();
        ed.putString(LAST_TT, data);
        ed.commit();
    }

    fun loadText(): String {
        val sPref = getPreferences(MODE_PRIVATE);
        val savedText: String = sPref.getString(LAST_TT, "{\"result\": \"no_entries\"}");
        return savedText
    }
}
