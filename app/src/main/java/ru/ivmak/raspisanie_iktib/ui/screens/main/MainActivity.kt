package ru.ivmak.raspisanie_iktib.ui.screens.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
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
import ru.ivmak.raspisanie_iktib.*
import ru.ivmak.raspisanie_iktib.data.Choice
import ru.ivmak.raspisanie_iktib.data.TimeTable
import ru.ivmak.raspisanie_iktib.ui.rv_adapters.DraverRvAdapter
import ru.ivmak.raspisanie_iktib.ui.screens.settings.SettingsActivity
import ru.ivmak.raspisanie_iktib.utils.Constants
import ru.ivmak.raspisanie_iktib.utils.Functions

class MainActivity : AppCompatActivity(),
    DraverRvAdapter.OnItemClickListener {

    private var menu: Menu? = null
    lateinit var toolbar: Toolbar
    private var adapter = DraverRvAdapter(arrayListOf(),this)
    private lateinit var draverRV: RecyclerView

    private lateinit var sPref: SharedPreferences

    private lateinit var selectWeekBtn: Button
    private lateinit var nextWeekBtn: ImageButton
    private lateinit var privWeekBtn: ImageButton

    val dataSingleton = MainDataSingleton.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sPref = getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
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

        viewPager.adapter =
            SimpleFragmentPagerAdapter(
                supportFragmentManager
            )
        tabLayout.setupWithViewPager(viewPager)

        draverRV = findViewById(R.id.draver_rv)
        draverRV.layoutManager = LinearLayoutManager(this)
        draverRV.adapter = adapter

        dataSingleton.choices.observe(this, Observer {
            val textIsEmpty = findViewById<TextView>(R.id.text_empty)
            if (it.isEmpty()) {
                textIsEmpty.visibility = View.VISIBLE
            } else {
                textIsEmpty.visibility = View.INVISIBLE
            }
            adapter.setData(it)
            adapter.notifyDataSetChanged()
        })

        dataSingleton.timeTable.observe(this, Observer {
            initAppBar(it)
        })

        val searchEditText = findViewById<TextInputEditText>(R.id.search_edit_text)
        searchEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchGroups(s.toString())
            }

        })

        nextWeekBtn = findViewById<ImageButton>(R.id.next_week_btn)
        privWeekBtn = findViewById<ImageButton>(R.id.priv_week_btn)
        selectWeekBtn = findViewById<Button>(R.id.select_week_btn)

        selectWeekBtn.isEnabled = false
        nextWeekBtn.isEnabled = false
        privWeekBtn.isEnabled = false

        nextWeekBtn.setOnClickListener {
            selectWeekBtn.isEnabled = false
            nextWeekBtn.isEnabled = false
            privWeekBtn.isEnabled = false
            getData(
                dataSingleton.timeTable.value!!.table!!.group,
                dataSingleton.timeTable.value!!.weeks!![dataSingleton.timeTable.value!!.weeks!!.indexOf(dataSingleton.timeTable.value!!.table!!.week) + 1]
            )
        }
        privWeekBtn.setOnClickListener {
            selectWeekBtn.isEnabled = false
            nextWeekBtn.isEnabled = false
            privWeekBtn.isEnabled = false
            getData(
                dataSingleton.timeTable.value!!.table!!.group,
                dataSingleton.timeTable.value!!.weeks!![dataSingleton.timeTable.value!!.weeks!!.indexOf(dataSingleton.timeTable.value!!.table!!.week) - 1]
            )
        }
        selectWeekBtn.setOnClickListener {
            showWeekPopUp()
        }

        initNotifications()

        GlobalScope.launch {
            dataSingleton.initTimeTable()
        }
    }

    fun initNotifications() {
        val time = sPref.getString(Constants.PREF_NOTIF_TIME, "")
        if (time == "") {
            sPref.edit()
                .putString(
                    Constants.PREF_NOTIF_TIME,
                    Constants.DEF_NOTIF_TIME
                )
                .apply()

            Functions.scheduleNotification(
                this,
                Functions.getDuration(Constants.DEF_NOTIF_TIME)
            )
        }
    }

    fun showWeekPopUp() {
        selectWeekBtn.isEnabled = false
        nextWeekBtn.isEnabled = false
        privWeekBtn.isEnabled = false

        val popup =
            PopupMenu(this@MainActivity, this@MainActivity.findViewById(R.id.select_week)) //item!!.actionView
        for (i in dataSingleton.timeTable.value!!.weeks!!) {
            popup.menu.add("$i неделя")
        }
        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
        popup.setOnMenuItemClickListener {
            getData(
                dataSingleton.timeTable.value!!.table!!.group,
                Integer.parseInt(it.title.substring(0, it.title.indexOf(' ')))
            )
            true
        }

        popup.show()
    }

    override fun onChoiseItemClick(data: Choice) {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        getData(data.group)
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun initAppBar(timeTable: TimeTable) {
//        if (!timeTable.isOnline!!) {
//            title = title.toString() + " [offline]"
//        } else {
//            title = title.subSequence(0, title.length - 10)
//        }
        if (timeTable.table != null) {
            timeTable.table?.let { table ->
                menu?.let {
                    it.getItem(0).title = table.week.toString() + " неделя"
                }
            }
            timeTable.table?.let { title = it.name }

            val viewPager = findViewById<ViewPager>(R.id.viewpager)
            timeTable.table?.let { viewPager.currentItem = Functions.isDayOfWeekOpen(it) }

            timeTable.table?.let { table -> selectWeekBtn.text = table.week.toString() + " неделя" }
            selectWeekBtn.isEnabled = true
            if (timeTable.weeks!!.indexOf(timeTable.table!!.week) < timeTable.weeks!!.size - 1) {
                nextWeekBtn.isEnabled = true
            }

            if (timeTable.weeks!!.indexOf(timeTable.table!!.week) > 0) {
                privWeekBtn.isEnabled = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.select_week -> {
                showWeekPopUp()
            }
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun searchGroups(query: String) {
        GlobalScope.launch {
            dataSingleton.searchByQuery(query)
        }
    }

    fun getData(group: String) {
        GlobalScope.launch {
            dataSingleton.getTimeTable(group)
        }
    }

    fun getData(group: String, week: Int) {
        GlobalScope.launch {
            dataSingleton.getTimeTableByWeek(group, week)
        }
    }
}
