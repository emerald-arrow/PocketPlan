package com.pocket_plan.j7_003.data.todolist

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.R.*
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.fragment_todo.view.*
import kotlinx.android.synthetic.main.row_task.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*

/**
 * A simple [Fragment] subclass.
 */

class TodoFr : Fragment() {
    lateinit var myMenu: Menu

    companion object {
        lateinit var myFragment: TodoFr
        lateinit var myAdapter: TodoTaskAdapter
        lateinit var myRecycler: RecyclerView

        val todoListInstance: TodoList = TodoList()
        var deletedTask: Task? = null
        var deletedTaskList: ArrayList<Task> = arrayListOf()

        var offsetTop: Int = 0
        var firstPos: Int = 0
        lateinit var layoutManager: LinearLayoutManager

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tasks, menu)
        myMenu = menu

        updateDeleteTaskIcon()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_tasks_delete_checked -> {
                //delete checked tasks and update the undoTask icon
                myFragment.manageCheckedTaskDeletion()
                updateUndoTaskIcon()
            }
            R.id.item_tasks_undo -> {

                //undo deletion of last deleted task (or multiple deleted tasks, if
                //sweep delete button was used
                if (deletedTaskList.size > 0) {
                    deletedTaskList.forEach { task ->
                        val newPos = todoListInstance.addFullTask(task)
                        myAdapter.notifyItemInserted(newPos)
                    }
                    deletedTaskList.clear()
                } else {
                    val newPos = todoListInstance.addFullTask(deletedTask!!)
                    deletedTask = null
                    myAdapter.notifyItemInserted(newPos)
                }
                updateUndoTaskIcon()
                updateDeleteTaskIcon()
            }
        }
        return super.onOptionsItemSelected(item)
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

        /**
         * Connecting Adapter, Layout-Manager and Swipe Detection to UI elements
         */

        myAdapter = TodoTaskAdapter()
        myRecycler.adapter = myAdapter

        layoutManager = LinearLayoutManager(activity)
        myRecycler.layoutManager = layoutManager
        myRecycler.setHasFixedSize(true)

        val swipeHelperLeft = ItemTouchHelper(SwipeToDeleteTask(ItemTouchHelper.LEFT, myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)
        val swipeHelperRight = ItemTouchHelper(SwipeToDeleteTask(ItemTouchHelper.RIGHT, myAdapter))
        swipeHelperRight.attachToRecyclerView(myRecycler)

        return myView
    }

    fun updateUndoTaskIcon() {
        if (deletedTask != null || deletedTaskList.size > 0) {
            myMenu.findItem(R.id.item_tasks_undo)?.setIcon(drawable.ic_action_undo)
            myMenu.findItem(R.id.item_tasks_undo)?.isVisible = true
        } else {
            myMenu.findItem(R.id.item_tasks_undo)?.isVisible = false
        }
    }

    fun prepareForMove() {
        firstPos = layoutManager.findFirstCompletelyVisibleItemPosition()
        offsetTop = 0
        if (firstPos >= 0) {
            val firstView = layoutManager.findViewByPosition(firstPos)
            offsetTop =
                layoutManager.getDecoratedTop(firstView!!) - layoutManager.getTopDecorationHeight(
                    firstView
                )
        }
    }

    fun reactToMove() {
        layoutManager.scrollToPositionWithOffset(
            firstPos,
            offsetTop
        )
    }

    fun updateDeleteTaskIcon() {
        val checkedTasks = todoListInstance.filter { t -> t.isChecked }.size
        myMenu.findItem(R.id.item_tasks_delete_checked)?.isVisible = checkedTasks > 0
    }

    //Deletes all checked tasks and animates the deletion
    private fun manageCheckedTaskDeletion() {
        deletedTaskList.clear()
        deletedTask = null
        val oldSize = todoListInstance.size
        val newSize = todoListInstance.deleteCheckedTasks()
        myAdapter.notifyItemRangeRemoved(newSize, oldSize)
        updateDeleteTaskIcon()
    }

    fun dialogAddTask(){
        //inflate the dialog with custom view
        val myDialogView =
            LayoutInflater.from(MainActivity.act).inflate(R.layout.dialog_add_task, null)

        //AlertDialogBuilder
        val myBuilder =
            MainActivity.act.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        myBuilder?.setCustomTitle(
            layoutInflater.inflate(
                R.layout.title_dialog_add_task,
                null
            )
        )

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
                val title = myDialogView.etxTitleAddTask.text.toString()
                if (title.isEmpty()) {
                    val animationShake =
                        AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake)
                    myDialogView.etxTitleAddTask.startAnimation(animationShake)
                    @Suppress("LABEL_NAME_CLASH")
                    return@setOnClickListener
                } else {
                    val newPos =
                        todoListInstance.addFullTask(
                            Task(
                                title,
                                index + 1,
                                false
                            )
                        )
                    if (newPos == todoListInstance.size - 1) {
                        myRecycler.adapter?.notifyDataSetChanged()
                    } else {
                        myRecycler.adapter?.notifyItemInserted(newPos)
                    }
                }
                myAlertDialog?.dismiss()
            }
        }

        myDialogView.etxTitleAddTask.requestFocus()
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
        if (viewHolder.adapterPosition == TodoFr.todoListInstance.size) {
            return 0
        }
        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)
        TodoFr.myFragment.updateDeleteTaskIcon()
    }
}

class TodoTaskAdapter : RecyclerView.Adapter<TodoTaskAdapter.TodoTaskViewHolder>() {
    private val listInstance = TodoFr.todoListInstance

    fun deleteItem(position: Int) {
        TodoFr.deletedTaskList.clear()
        TodoFr.deletedTask = listInstance.getTask(position)
        TodoFr.myFragment.updateUndoTaskIcon()
        listInstance.deleteTask(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoTaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(layout.row_task, parent, false)
        return TodoTaskViewHolder(itemView)
    }


    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: TodoTaskViewHolder, position: Int) {

        if (position == TodoFr.todoListInstance.size) {
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.tvName.setOnLongClickListener { true }
            holder.itemView.tapField.setOnClickListener { }
            holder.itemView.layoutParams.height = 280
            return
        }
        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.visibility = View.VISIBLE

        val currentTask = listInstance.getTask(holder.adapterPosition)
        val activity = MainActivity.act

        //changes design of task based on priority and being checked
        holder.itemView.tvName.text = currentTask.title

        //resets scale, that got animated
        holder.itemView.scaleX = 1f
        holder.itemView.scaleY = 1f

        if (listInstance.getTask(holder.adapterPosition).isChecked) {
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

        holder.itemView.tvName.setOnLongClickListener {

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
            editTitle.tvDialogTitle.text = MainActivity.act.resources.getText(string.menuTitleTasks)
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
            myDialogView.etxTitleAddTask.setText(listInstance.getTask(holder.adapterPosition).title)
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
                    if (myDialogView.etxTitleAddTask.text.toString() == "") {
                        val animationShake =
                            AnimationUtils.loadAnimation(MainActivity.act, anim.shake)
                        myDialogView.etxTitleAddTask.startAnimation(animationShake)
                        return@setOnClickListener
                    }
                    val newPos = listInstance.editTask(
                        holder.adapterPosition, index + 1,
                        myDialogView.etxTitleAddTask.text.toString(),
                        listInstance.getTask(holder.adapterPosition).isChecked
                    )
                    this.notifyItemChanged(holder.adapterPosition)
                    TodoFr.myFragment.prepareForMove()
                    this.notifyItemMoved(holder.adapterPosition, newPos)
                    TodoFr.myFragment.reactToMove()
                    myAlertDialog.dismiss()

                }
            }
            true
        }

        //reacts to the user checking a task
        holder.itemView.tapField.setOnClickListener {
            val checkedStatus = !listInstance.getTask(holder.adapterPosition).isChecked
            holder.itemView.cbTask.isChecked = checkedStatus
            val task = listInstance.getTask(holder.adapterPosition)
            val newPos = listInstance.editTask(
                holder.adapterPosition, task.priority,
                task.title, checkedStatus
            )
            TodoFr.myFragment.updateDeleteTaskIcon()

            notifyItemChanged(holder.adapterPosition)
            if (holder.adapterPosition != newPos) {
                TodoFr.myFragment.prepareForMove()
                notifyItemMoved(holder.adapterPosition, newPos)
                TodoFr.myFragment.reactToMove()
            }
            //delays item change until list is reordered
        }
    }

    override fun getItemCount() = TodoFr.todoListInstance.size + 1

    class TodoTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}



