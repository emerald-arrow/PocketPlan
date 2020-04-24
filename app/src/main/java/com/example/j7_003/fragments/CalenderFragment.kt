package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.CalendarManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_calender.view.*
import kotlinx.android.synthetic.main.row_term.view.*


/**
 * A simple [Fragment] subclass.
 */
class CalenderFragment : Fragment() {

    lateinit var myView: View
    lateinit var btnAddTerm: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        CalendarManager.init()
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_calender, container, false)
        btnAddTerm = myView.btnAddTerm
        btnAddTerm.setOnClickListener {
            changeToCreateTermFragment()
        }

        val myRecycler = myView.recycler_view_calendar
        val myAdapter = TermAdapter()

        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(MainActivity.myActivity)
        myRecycler.setHasFixedSize(true)

        val swipeHelperLeft = ItemTouchHelper(SwipeLeftToDeleteTerm(myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)

        val swipeHelperRight = ItemTouchHelper(SwipeRightToDeleteTerm(myAdapter))
        swipeHelperRight.attachToRecyclerView(myRecycler)



        return myView
    }

    fun changeToCreateTermFragment() {
        MainActivity.myActivity.changeToCreateTerm()
    }


}

class SwipeRightToDeleteTerm(var adapter: TermAdapter):
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)
    }
}

class SwipeLeftToDeleteTerm(private var adapter: TermAdapter):
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)
    }
}

class TermAdapter() :
    RecyclerView.Adapter<TermAdapter.TermViewHolder>() {

    fun deleteItem(position: Int) {
        CalendarManager.deleteAppointment(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TermViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_term, parent, false)
        return TermViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: TermViewHolder, position: Int) {

        val currentTerm = CalendarManager.getAppointment(position)

        holder.itemView.setOnClickListener() {
            //todo handle editing of terms here onclick
        }

        holder.tvTitle.text = currentTerm.title
        holder.tvInfo.text = currentTerm.addInfo
    }

    //todo get size of termlist here
    override fun getItemCount() = CalendarManager.calendar.size

    class TermViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * One instance of this class will contain one "instance" of row_term and meta data
         * like position, it also holds references to views inside of the layout
         */
        val tvTitle = itemView.tvTermItemTitel
        val tvInfo = itemView.tvTermItemInfo
        //var myView = itemView
    }

}
