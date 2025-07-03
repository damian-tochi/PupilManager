package com.bridge.androidtechnicaltest.ui

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bridge.androidtechnicaltest.BaseFragment
import com.bridge.androidtechnicaltest.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.bridge.androidtechnicaltest.db.Pupil
import com.bridge.androidtechnicaltest.interfaces.OnPupilSelect
import com.bridge.androidtechnicaltest.ui.adapters.PupilListAdapter
import com.bridge.androidtechnicaltest.viewmodel.PupilListViewModel


class PupilListFragment : BaseFragment() {

    private val viewModel: PupilListViewModel by viewModel()

    private lateinit var recyclerView: RecyclerView
    private lateinit var pupilAdd: ImageButton
    private lateinit var adapter: PupilListAdapter

    override fun setLayoutView(): Int {
        return R.layout.fragment_pupillist
    }

    override fun initialize(view: View, savedInstanceState: Bundle?) {
        val spanCount = calculateSpanCount()
        recyclerView = view.findViewById(R.id.pupil_list)
        pupilAdd = view.findViewById(R.id.pupil_add)
        adapter = PupilListAdapter(object : OnPupilSelect {
            override fun onSpotlightSelection(data: Pupil, pos: String) {
                sharedViewModel.selectPupil(data)
                openFragmentPage(PupilDetailFragment())
            }
        })
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        recyclerView.adapter = adapter

        observeViewModel()
        viewModel.loadPupils()

        pupilAdd.setOnClickListener {
            val data = Pupil(
                pupilId = 0,
                name = "",
                country = "",
                latitude = 0.0,
                longitude = 0.0,
                image = ""
            )
            sharedViewModel.selectPupil(data)
            openFragmentPage(PupilCreateFragment())
        }
    }

    private fun observeViewModel() {
        viewModel.pupils.observe(viewLifecycleOwner) { pupilList ->
            adapter.submitList(pupilList.items)
            Log.d("PupilListFragment", "Pupils: ${pupilList.items[0].country} | ${pupilList.items[0].latitude} | ${pupilList.items[0].longitude}")
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            Log.e("PupilListFragment", "Error: $errorMsg")
        }
    }

    private fun calculateSpanCount(): Int {
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return if (screenWidthDp > 600) 4 else 3
    }
}