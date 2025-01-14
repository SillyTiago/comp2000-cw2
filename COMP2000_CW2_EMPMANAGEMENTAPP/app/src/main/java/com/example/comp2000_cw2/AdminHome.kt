package com.example.comp2000_cw2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button

// This serves as the main dashboard for administrators. It allows them to have quick access to various features such as adding, editing, deleting, or fetching a list of all users.
class AdminHome : Fragment(R.layout.fragment_adminhome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addEmployeeButton = view.findViewById<Button>(R.id.add_employee_button)

        // Binds buttons to their respective fragments, and handles navigation to them
        addEmployeeButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, AdminAddEmployeeFragment())
                .addToBackStack(null)
                .commit()
        }

        val editEmployeeButton = view.findViewById<Button>(R.id.edit_employee_button)
        editEmployeeButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, AdminEditEmployeeFragment())
                .addToBackStack(null)
                .commit()
        }

        val deleteEmployeeButton = view.findViewById<Button>(R.id.delete_employee_button)
        deleteEmployeeButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, AdminDeleteEmployeeFragment())
                .addToBackStack(null)
                .commit()
        }

        val getEmployeeListButton = view.findViewById<Button>(R.id.get_employee_list_button)
        getEmployeeListButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, AdminEmployeeListFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
