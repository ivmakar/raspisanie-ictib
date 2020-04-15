package ru.ivmak.raspisanie_iktib

import com.google.gson.Gson
import org.junit.Test

import org.junit.Assert.*
import ru.ivmak.raspisanie_iktib.data.TimeTable
import ru.ivmak.raspisanie_iktib.utils.Functions
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    val jsonStr = "{\n" +
            "  \"table\": {\n" +
            "    \"type\": \"Расписание занятий учебной группы\",\n" +
            "    \"name\": \"КТмо1-4\",\n" +
            "    \"week\": 3,\n" +
            "    \"group\": \"20.htm\",\n" +
            "    \"table\": [\n" +
            "      [\"Пары\", \"1-я\", \"2-я\", \"3-я\", \"4-я\", \"5-я\", \"6-я\", \"7-я\"],\n" +
            "      [\"Время\", \"08:00-09:35\", \"09:50-11:25\", \"11:55-13:30\", \"13:45-15:20\", \"15:50-17:25\", \"17:40-19:15\", \"19:30-21:05\"],\n" +
            "      [\"Пнд,16 сентября\", \"\", \"\", \"\", \"\", \"лек.Современные компьютерные технологии Кравченко Ю. А. Г-301 Целых А. А. Г-302\", \"Иностранный язык Опрышко А. А. Д-511 Писаренко В. И. Д-519 Сальная Л. К. Д-507 Буренко Л. В. Д-514а Хорешман В. С. Д-514 Заблоцкая О. А. Д-516 Овчаренко В. П. Д-521 Андриенко А. С. Д-518 Вакансия ИКТИБ Д-506 Вакансия 1 Д-522\", \"лаб.Системная интеграция и корпоративные информационные системы- 1 п/г Беликов А. Н. Г-128\"],\n" +
            "      [\"Втр,17 сентября\", \"\", \"\", \"\", \"\", \"пр.Современные компьютерные технологии Запорожец Д. Ю. Г-027\", \"лек.Психология управления личностными ресурсами Эксакусто Т. В. Г-301\", \"лек.Сетевые и Web-технологии Самойлов А. Н. Г-401\"],\n" +
            "      [\"Срд,18 сентября\", \"\", \"\", \"\", \"\", \"лек.Методология научной и проектной деятельности Папченко Е. В. Г-217\", \"\", \"\"],\n" +
            "      [\"Чтв,19 сентября\", \"\", \"\", \"\", \"\", \"пр.Психология управления личностными ресурсами Эксакусто Т. В. И-116\", \"пр.Современные компьютерные технологии Целых А. А. Г-127\", \"лек.Системная интеграция и корпоративные информационные системы Кучеров С. А. Г-217\"],\n" +
            "      [\"Птн,20 сентября\", \"\", \"\", \"\", \"\", \"пр.Технологии BigData Кравченко Ю. А. Г-027\", \"\", \"\"],\n" +
            "      [\"Сбт,21 сентября\", \"\", \"пр.Сетевые и Web-технологии Волошин А. В. Г-413\", \"лаб.Технологии BigData- 2 п/г Кулиев Э. В. Г-221\", \"лаб.Системная интеграция и корпоративные информационные системы- 2 п/г Беликов А. Н. Г-128\", \"\", \"\", \"\"]\n" +
            "    ]\n" +
            "  },\n" +
            "  \"weeks\": [1, 2, 3, 4, 5, 6, 7, 8]\n" +
            "}"

    val jsonStr2 = "{\"result\": \"no_entries\"}"

    val jsonStr3 = "{\"choices\": [{\"name\": \"КТмо1-10\", \"id\": \"5d9ad5477dba0ef2f6bc1bee\", \"group\": \"26.htm\"}, {\"name\": \"КТмо1-12\", \"id\": \"5d9ad5477dba0ef2f6bc1e82\", \"group\": \"28.htm\"}, {\"name\": \"КТмо1-1\", \"id\": \"5d9ad5477dba0ef2f6bc20df\", \"group\": \"17.htm\"}, {\"name\": \"КТмо1-11\", \"id\": \"5d9ad5477dba0ef2f6bc2a69\", \"group\": \"27.htm\"}]}"

    @Test
    fun addition_isCorrect() {
        var gson = Gson()
        var myTimeTable = gson.fromJson<TimeTable>(jsonStr, TimeTable::class.java)
        assertEquals("20.htm", myTimeTable.table!!.group)
        assertEquals("Пары", myTimeTable.table!!.table[0][0])
        assertEquals(1, myTimeTable.weeks!![0])
    }

    @Test
    fun addition_isParse() {
        var str = "1 неделя"
        assertEquals(1, Integer.parseInt(str.substring(0, str.indexOf(' '))))
    }

    @Test
    fun testRegEx() {
        var str = "лек.Современные компьютерные технологии Кравченко Ю. А. Г-301 Целых А. А. Г-302"
        val teachAndRoom = Regex("""([А-Я][а-я]+\s[А-Я].\s[А-Я].)\s?([А-Я]-[0-9]+)\s?""")
        val arr = teachAndRoom.findAll(str, 0)
        val (teach, room) = arr.toList()[1].destructured

        str = teachAndRoom.replace(str, "")

        val type = Regex("""пр.|лаб.|лек.""")
        val t = type.find(str)!!.value
        assertEquals("лек.", t)

        str = type.replace(str, "")

        val lesson = Regex("""[[A-Za-zА-Яа-я]+\s]+""")
        val l = lesson.find(str)!!.value
        assertEquals("Современные компьютерные технологии ", l)

    }

    @Test
    fun testDateRegEx() {
        var str = "Пнд,30  сентября"
        val dayRegex = Regex("""([А-Я][а-я][а-я]),([0-9]+)\s+([а-я]+)""")
        val arr = dayRegex.findAll(str, 0)
        val (day, num, month) = arr.toList()[0].destructured

        assertEquals("Пнд", day)
        assertEquals("30", num)
        assertEquals("сентября", month)

        val date = Date()
        assertEquals(4, date.date)
        assertEquals(0, date.month)
    }

    @Test
    fun testJson() {
        var obj = Gson().fromJson<TimeTable>(jsonStr, TimeTable::class.java)
        obj = Gson().fromJson<TimeTable>(jsonStr2, TimeTable::class.java)
        obj = Gson().fromJson<TimeTable>(jsonStr3, TimeTable::class.java)
    }

    @Test
    fun testGetDuration() {
        assertEquals(60*60*1000, Functions.getDuration("07:00"))
        assertEquals(24*60*60*1000, Functions.getDuration("06:00"))
        assertEquals(23*60*60*1000, Functions.getDuration("05:00"))
        assertEquals(60*1000, Functions.getDuration("06:01"))

    }

    @Test
    fun testSetTitle() {
        val title = "КТмо1-4 [offline]"
        assertEquals("КТмо1-4", title.subSequence(0, title.length - 10))
    }
}
