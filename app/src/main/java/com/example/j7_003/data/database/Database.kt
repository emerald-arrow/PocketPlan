package com.example.j7_003.data.database

import com.example.j7_003.data.NoteColors
import com.example.j7_003.data.database.database_objects.Birthday
import com.example.j7_003.data.database.database_objects.Note
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDate
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple handler to manage the interaction of different objects
 * with a similar structure.
 */
class Database {
    companion object {
        lateinit var birthdayList: ArrayList<Birthday>

        private const val BLIST = "BIRTHDAYLIST"

        /**
         * Used to initialize the Database, i.e. setting up the storage
         * and loading existing data from the file. Also sorts the taskList and
         * birthdayList.
         */
        fun init() {
            initStorage()
            initLists()
            sortBirthday()
        }

        //--------------------------------------------------------------------------------------------//
        //-----------------------------------birthdayList handling------------------------------------//
        //--------------------------------------------------------------------------------------------//
        //debug here will be the birthday functionality
        /**
         * Adds a birthday to the birthdayList and saves the birthdayList.
         * @param name The name of the created birthday
         * @param parMonth The month of the birthday
         * @param parDay The day of the birthday
         */
        fun addBirthday(name: String, day: Int, month: Int, year: Int, daysToRemind: Int, expanded: Boolean) {
            birthdayList.add(Birthday(name, day, month, year, daysToRemind, expanded))
            sortAndSaveBirthdays()
        }

        /**
         * Helper Function to add a full birthday object, used for undoing deletions
          */
        fun addFullBirthday(birthday: Birthday): Int{
            birthdayList.add(birthday)
            sortAndSaveBirthdays()
            return birthdayList.indexOf(birthday)
        }

        /**
         * Deletes the Birthday at a given index in the birthdayList
         * @param index The position of the birthday in the array list
         */
        fun deleteBirthday(index: Int) {
            birthdayList.removeAt(index)
            sortAndSaveBirthdays()
        }

        /**
         * Grabs a birthday object and changes its attributes according to the parameters.
         * @param name Name of the Person.
         * @param parDay Day of the birthday.
         * @param parMonth Month of the birthday.
         * @param parReminder Days to be reminded at prior to the birthday.
         * @param parPosition Position of the birthday object int he list.
         */
        fun editBirthday(name: String, parDay: Int, parMonth: Int, parYear: Int, parReminder: Int, parPosition: Int) {
            val editableBirthday: Birthday =
                getBirthday(
                    parPosition
                )

            editableBirthday.name = name
            editableBirthday.day = parDay
            editableBirthday.month = parMonth
            editableBirthday.year = parYear
            editableBirthday.daysToRemind = parReminder

            sortAndSaveBirthdays()

        }

        fun sortAndSaveBirthdays(){
            sortBirthday()
            save(BLIST, birthdayList)
        }

        fun deleteBirthdayObject(birthday: Birthday){
            birthdayList.remove(birthday)
            sortBirthday()
            save(BLIST, birthdayList)
        }

        /**
         * Returns a birthday from arraylist at given index
         * @return Returns requested birthday object
         */
        fun getBirthday(position: Int): Birthday = birthdayList[position]

        private fun manageLabels() {
            val months = arrayListOf<Int>()
            var n = 0
            while (n < birthdayList.size) {
                if (birthdayList[n].daysToRemind < 0) birthdayList.remove(birthdayList[n])
                else n++
            }

            val today = LocalDate.now()
            var beforeMonth = false
            var afterMonth = false
            birthdayList.forEach { m ->
                if(!months.contains(m.month) && m.month != today.monthValue){
                    months.add(m.month)
                }

                if (m.month == today.monthValue && m.day < today.dayOfMonth) beforeMonth = true
                else if (m.month == today.monthValue && m.day >= today.dayOfMonth) afterMonth = true
            }

            if (beforeMonth) {
                birthdayList.add(
                    Birthday(
                        today.month.toString().toLowerCase().capitalize(),
                        1,
                        today.monthValue,
                        0,
                        -1 * today.monthValue,
                        false
                    )
                )
            }

            if (afterMonth) {
                birthdayList.add(
                    Birthday(
                        today.month.toString().toLowerCase().capitalize(),
                        today.dayOfMonth,
                        today.monthValue,
                        0,
                        -1 * today.monthValue,
                        false
                    )
                )
            }

            months.forEach { m ->
                val name = LocalDate.of(2020, m, 1).month.toString()
                birthdayList.add(Birthday(name.toLowerCase().capitalize(), 0, m,0, -1*m, false))
            }
        }

        private fun sortBirthday() {
            manageLabels()
            val localDate = LocalDate.now()
            val day = localDate.dayOfMonth
            val month = localDate.month.value
            val cacheList = ArrayList<Birthday>()
            birthdayList.sortWith(compareBy({ it.month }, { it.day }, {it.daysToRemind >= 0}, { it.name }))

            var i = 0
            val spacerBirthday = Birthday("---    ${localDate.year + 1}    ---", 1, 1,0, -200, false)
            cacheList.add(spacerBirthday)
            while(i < birthdayList.size) {
                if (getBirthday(i).month < month ||
                    (getBirthday(i).month == month && getBirthday(i).day < day)) {
                    cacheList.add(getBirthday(i))
                    birthdayList.remove(getBirthday(i))
                } else {
                    i++
                }
            }

            birthdayList.sortWith(compareBy(
                { it.month },
                { it.day },
                { it.daysToRemind >= 0},
                { it.name })
            )

            if (cacheList.size == 1) {
                cacheList.remove(spacerBirthday)
            }

            cacheList.forEach { n ->
                birthdayList.add(n)
            }
        }

        /**
         * Collects all birthdays that are happening on the current day and returns
         * them as an list.
         * @return List of today's birthdays.
         */
        fun getRelevantCurrentBirthdays(): ArrayList<Birthday> {
            val currentBirthdays = ArrayList<Birthday>()
            val localDate = LocalDate.now()
            birthdayList.forEach { n ->
                if (n.month == localDate.monthValue  &&
                    n.day == localDate.dayOfMonth &&
                    n.daysToRemind >= 0
                ) {
                    currentBirthdays.add(n)
                }
            }
            return currentBirthdays
        }

        /**
         * Collects all birthdays which's reminder corresponds to the current day
         * @see getRelevantCurrentBirthdays for current birthdays.
         * @return List of birthdays to be reminded of on the current day.
         */
        fun getRelevantUpcomingBirthdays(): ArrayList<Birthday> {
            val upcomingBirthdays = ArrayList<Birthday>()
            val localDate = LocalDate.now()
            birthdayList.forEach { n ->
                if (n.month == localDate.monthValue + 1 && (n.day - n.daysToRemind) ==
                    localDate.dayOfMonth && n.daysToRemind != 0)
                {
                    upcomingBirthdays.add(n)
                }
            }
            return upcomingBirthdays
        }

        /**
         * Returns the x next birthdays from the birthdayList as a list. If the requested
         * size is larger than the birthdayList size the whole list is returned.
         * @param index Amount of birthdays to return.
         * @return The requested amount of birthdays or the whole birthdayList.
         */
        fun getXNextBirthdays(index: Int): ArrayList<Birthday> {
            var min = index

            if (index > birthdayList.size) {
                min = birthdayList.size
            }

            val xNextBirthdays = ArrayList<Birthday>()

            for (i in 0..min) {
                xNextBirthdays.add(
                    getBirthday(
                        i
                    )
                )
            }

            return xNextBirthdays
        }

        /**
         * Fetches the data from the birthdayList's file and returns it as a list.
         * @return The loaded version of the birthdayList.
         */
        fun fetchBList() : ArrayList<Birthday> {
            val jsonString = StorageHandler.files[BLIST]?.readText()

            return GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<Birthday>>() {}.type)
        }

        //--------------------------------------------------------------------------------------------//
        //---------------------------------noteList handling------------------------------------------//
        //--------------------------------------------------------------------------------------------//

        /**
         * Creates a note with the given parameters and saves it to file.
         * @param title Displayed title of the note.
         * @param content Contents of the note.
         * @param color Color of the note.
         */
        //--------------------------------------------------------------------------------------------//
        //-------------------------------Database internal handling-----------------------------------//
        //--------------------------------------------------------------------------------------------//

        private fun initStorage() {
            StorageHandler.createJsonFile(
                BLIST,
                "BirthdayList.json"
            )
        }

        private fun initLists() {
            birthdayList =
                fetchBList()
        }

        private fun save(identifier: String, collection: Collection<Any>) {
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[identifier],
                collection
            )
        }
    }
}