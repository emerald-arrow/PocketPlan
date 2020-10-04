package com.pocket_plan.j7_003.data.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.system_interaction.handler.share.BackUpActivity
import kotlinx.android.synthetic.main.fragment_settings_main.view.*

class SettingsMainFr : Fragment() {

    private lateinit var clSettingNotes: ConstraintLayout
    private lateinit var clSettingShopping: ConstraintLayout

    private lateinit var clSettingBackup: ConstraintLayout
    private lateinit var clSettingAbout: ConstraintLayout
    private lateinit var clSettingAppearance: ConstraintLayout

    //V2 ADD NAVIGATION OPTIONS
    //private lateinit var clSettingNavigation: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_settings_main, container, false)

        initializeComponents(myView)
        initializeListeners()

        return myView
    }

    private fun initializeComponents(myView: View) {
        clSettingAbout = myView.clSEttingAbout
        clSettingBackup = myView.clSettingBackup
        clSettingShopping = myView.clSettingShopping
        clSettingNotes = myView.clSettingNotes
        clSettingAppearance = myView.clSettingAppearance

        //V2 ADD NAVIGATION OPTIONS
        //clSettingNavigation = myView.clSettingNavigation
    }

    private fun initializeListeners() {
        clSettingNotes.setOnClickListener { MainActivity.act.changeToFragment(FT.SETTINGS_NOTES) }
        clSettingBackup.setOnClickListener {
            MainActivity.act.startActivity(Intent(MainActivity.act, BackUpActivity::class.java))
        }
        clSettingShopping.setOnClickListener { MainActivity.act.changeToFragment(FT.SETTINGS_SHOPPING) }
        clSettingAbout.setOnClickListener { MainActivity.act.changeToFragment(FT.SETTINGS_ABOUT) }
        clSettingAppearance.setOnClickListener { MainActivity.act.changeToFragment(FT.SETTINGS_APPEARANCE) }

        //V2 ADD NAVIGATION OPTIONS
        //clSettingNavigation.setOnClickListener { MainActivity.act.changeToFragment(FT.SETTINGS_NAVIGATION)}
    }

}