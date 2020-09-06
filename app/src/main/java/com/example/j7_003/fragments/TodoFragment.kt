package com.example.j7_003.fragments

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity
import com.example.j7_003.R.*
import com.example.j7_003.data.database.Database
import com.example.j7_003.data.database.database_objects.Task
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.fragment_todo.view.*
import kotlinx.android.synthetic.main.row_task.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*

/**
 * A simple [Fragment] subclass.
 */

class TodoFragment : Fragment() {

    companion object {
        lateinit var myFragment: TodoFragment
        lateinit var myAdapter: TodoTaskAdapter
        lateinit var myRecycler: RecyclerView

        var deletedTask: Task? = null
        var deletedTaskList: ArrayList<Task> = arrayListOf()

        var allowSwipe: Boolean = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myView = inflater.inflate(layout.fragment_todo, container, false)
        myRecycler = myView.recycler_view_todo
        myFragment = this

        /**
         * Adding Task via floating action button
         * Onclick-Listener opening the add-task dialog
         */
        myView.btnAddTodoTask.setOnClickListener {
            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(layout.dialog_add_task, null)

            //AlertDialogBuilder
            val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            myBuilder?.setCustomTitle(layoutInflater.inflate(layout.title_dialog_add_task, null))

            //show dialog
            val myAlertDialog = myBuilder?.create()
            myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog?.show()

            //adds listeners to confirmButtons in addTaskDialog
            val taskConfirmButtons = arrayListOf<Button>(
                myDialogView.btnConfirm1,
                myDialogView.btnConfirm2,
                myDialogView.btnConfirm3
            )

            taskConfirmButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    myAlertDialog?.dismiss()
                    val title = myDialogView.etxTitleAddTask.text.toString()
                    if (title.isEmpty()) {
                        Toast.makeText(
                            MainActivity.act,
                            "Can't create an empty task!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        myRecycler.adapter?.notifyItemInserted(
                            Database.addFullTask(
                                Task(
                                    title,
                                    index + 1,
                                    false
                                )
                            )
                        )
                    }
                }
            }

            myDialogView.etxTitleAddTask.requestFocus()
        }

        /**
         * Connecting Adapter, Layout-Manager and Swipe Detection to UI elements
         */

        myAdapter = TodoTaskAdapter()
        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)

        val swipeHelperLeft = ItemTouchHelper(SwipeToDeleteTask(ItemTouchHelper.LEFT, myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)
        val swipeHelperRight = ItemTouchHelper(SwipeToDeleteTask(ItemTouchHelper.RIGHT, myAdapter))
        swipeHelperRight.attachToRecyclerView(myRecycler)

        return myView
    }

    //Deletes all checked tasks and animates the deletion
    fun manageCheckedTaskDeletion() {
        deletedTaskList.clear()
        deletedTask = null
        val oldSize = Database.taskList.size
        val newSize = Database.deleteCheckedTasks()
        allowSwipe = false
        for (i in newSize until oldSize) {
            val v =
                myRecycler.findViewHolderForAdapterPosition(i) as TodoTaskAdapter.TodoTaskViewHolder
            if (i == oldSize - 1) {
                v.itemView.animate().scaleX(0f).setDuration(300).scaleY(0f).withEndAction {
                    myAdapter.notifyItemRangeRemoved(newSize, oldSize)
                    allowSwipe = true
                }
            } else {
                v.itemView.animate().scaleX(0f).scaleY(0f).duration = 300
            }

        }
        MainActivity.act.updateDeleteTaskIcon()
    }
}

class SwipeToDeleteTask(direction: Int, val adapter: TodoTaskAdapter) : ItemTouchHelper
.SimpleCallback(0, direction) {
    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (!TodoFragment.allowSwipe) {
            return 0
        }
        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)
        MainActivity.act.updateDeleteTaskIcon()
    }
}

class TodoTaskAdapter :
    RecyclerView.Adapter<TodoTaskAdapter.TodoTaskViewHolder>() {

    fun deleteItem(position: Int) {
        TodoFragment.deletedTaskList.clear()
        TodoFragment.deletedTask = Database.getTask(position)
        MainActivity.act.updateUndoTaskIcon()
        Database.deleteTask(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoTaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(layout.row_task, parent, false)
        return TodoTaskViewHolder(itemView)
    }


    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onBindViewHolder(holder: TodoTaskViewHolder, position: Int) {

        val currentTask = Database.getTask(holder.adapterPosition)
        val activity = MainActivity.act

        //changes design of task based on priority and being checked
        holder.itemView.tvName.text = currentTask.title

        //resets scale, that got animated
        holder.itemView.scaleX = 1f
        holder.itemView.scaleY = 1f
        if (Database.getTask(holder.adapterPosition).isChecked) {
            holder.itemView.cbTask.isChecked = true
            holder.itemView.tvName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.tvName.setTextColor(
                ContextCompat.getColor(
                    MainActivity.act,
                    color.colorHint
                )
            )
            holder.itemView.setBackgroundResource(drawable.round_corner_gray)
        } else {
            holder.itemView.cbTask.isChecked = false
            holder.itemView.tvName.paintFlags = 0
            holder.itemView.tvName.setTextColor(
                ContextCompat.getColor(
                    MainActivity.act,
                    color.colorOnBackGround
                )
            )
            when (currentTask.priority) {
                1 -> holder.itemView.setBackgroundResource(drawable.round_corner1)
                2 -> holder.itemView.setBackgroundResource(drawable.round_corner2)
                3 -> holder.itemView.setBackgroundResource(drawable.round_corner3)
            }
        }

        //User Interactions with Task List Item below
        /**
         * EDITING task
         * Onclick-Listener on List items, opening the edit-task dialog
         */

        holder.itemView.tvName.setOnClickListener {

            if (!TodoFragment.allowSwipe) {
                return@setOnClickListener
            }
            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(
                layout.dialog_add_task,
                null
            )

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(activity).setView(myDialogView)
            val editTitle = LayoutInflater.from(activity).inflate(
                layout.title_dialog_add_task,
                null
            )
            editTitle.tvDialogTitle.text = "Edit task"
            myBuilder.setCustomTitle(editTitle)

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(
                WindowManager
                    .LayoutParams.SOFT_INPUT_STATE_VISIBLE
            )
            myAlertDialog.show()

            //write current task to textField
            myDialogView.etxTitleAddTask.requestFocus()
            myDialogView.etxTitleAddTask.setText(Database.getTask(holder.adapterPosition).title)
            myDialogView.etxTitleAddTask.setSelection(myDialogView.etxTitleAddTask.text.length)

            //adds listeners to confirmButtons in addTaskDialog
            val taskConfirmButtons = arrayListOf<Button>(
                myDialogView.btnConfirm1,
                myDialogView.btnConfirm2,
                myDialogView.btnConfirm3
            )

            //Three buttons to create tasks with priorities 1-3
            taskConfirmButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    myAlertDialog.dismiss()
                    val newPos = Database.editTask(
                        holder.adapterPosition, index + 1,
                        myDialogView.etxTitleAddTask.text.toString(),
                        Database.getTask(holder.adapterPosition).isChecked
                    )
                    this.notifyItemChanged(holder.adapterPosition)
                    this.notifyItemMoved(holder.adapterPosition, newPos)
                }
            }
        }

        //reacts to the user checking a task
        holder.itemView.tapField.setOnClickListener {
            if (!TodoFragment.allowSwipe) {
                return@setOnClickListener
            }
            val checkedStatus = !Database.getTask(holder.adapterPosition).isChecked
            holder.itemView.cbTask.isChecked = checkedStatus
            val task = Database.getTask(holder.adapterPosition)
            val newPos = Database.editTask(
                holder.adapterPosition, task.priority,
                task.title, checkedStatus
            )
            MainActivity.act.updateDeleteTaskIcon()

            notifyItemChanged(holder.adapterPosition)
            if (holder.adapterPosition != newPos) {
                notifyItemMoved(holder.adapterPosition, newPos)
            }
            //delays item change until list is reordered
        }
    }


    override fun getItemCount() = Database.taskList.size

    class TodoTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

