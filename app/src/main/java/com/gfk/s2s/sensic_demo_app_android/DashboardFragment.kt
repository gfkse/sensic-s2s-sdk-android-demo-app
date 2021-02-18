package com.gfk.s2s.sensic_demo_app_android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class DashboardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_dashboard, container, false)
        view.findViewById<Button>(R.id.vodButton).setOnClickListener {
            findNavController().navigate(R.id.actionDashBoardFragment_toVODFragment)
        }
        return view

    }

}