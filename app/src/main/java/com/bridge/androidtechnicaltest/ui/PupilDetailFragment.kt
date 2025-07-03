package com.bridge.androidtechnicaltest.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bridge.androidtechnicaltest.BaseFragment
import com.bridge.androidtechnicaltest.R
import com.bumptech.glide.Glide


class PupilDetailFragment : BaseFragment() {

    override fun setLayoutView(): Int {
        return R.layout.fragment_pupildetail
    }

    override fun initialize(view: View, savedInstanceState: Bundle?) {
        val pupilImage = view.findViewById<ImageView>(R.id.pupil_image)
        val pupilId = view.findViewById<TextView>(R.id.pupil_id)
        val pupilName = view.findViewById<TextView>(R.id.pupil_name)
        val pupilCountry = view.findViewById<TextView>(R.id.country)
        val locate = view.findViewById<AppCompatButton>(R.id.locate)
        val backButton = view.findViewById<ImageView>(R.id.back_button)
        val editPupil = view.findViewById<ImageView>(R.id.pupil_edit)

        val pupil = sharedViewModel.selectedPupil.value
        if (pupil != null) {
            pupilId.text = pupil.pupilId.toString()
            pupilName.text = pupil.name
            pupilCountry.text = pupil.country

            context?.let {
                Glide.with(it)
                    .load(pupil.image)
                    .placeholder(R.drawable.no_user_photo)
                    .into(pupilImage)
            }

            locate.setOnClickListener {
                if (pupil.latitude == null || pupil.longitude == null) {
                    showToast("Location not available")
                    return@setOnClickListener
                }
                val latitude = pupil.latitude
                val longitude = pupil.longitude
                val uri = Uri.parse("geo:${latitude},${longitude}?q=${latitude},${longitude}(Location)")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")

                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    showToast("No Maps app installed")
                }
            }
        }

        backButton.setOnClickListener {
            backToPrevious()
        }

        editPupil.setOnClickListener {
            openFragmentPage(PupilEditFragment())
        }

    }
}