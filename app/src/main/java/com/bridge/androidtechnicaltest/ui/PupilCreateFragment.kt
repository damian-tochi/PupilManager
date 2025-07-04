package com.bridge.androidtechnicaltest.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bridge.androidtechnicaltest.BaseFragment
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.db.Pupil
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.bridge.androidtechnicaltest.viewmodel.PupilListViewModel
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.imageview.ShapeableImageView


class PupilCreateFragment : BaseFragment() {

    private val viewModel: PupilListViewModel by viewModel()
    private lateinit var pupilImage: ShapeableImageView
    private lateinit var imageUrl: String

    override fun setLayoutView(): Int {
        return R.layout.fragment_pupilcreate
    }

    override fun initialize(view: View, savedInstanceState: Bundle?) {

        val backBtn = view.findViewById<ImageView>(R.id.back_button)
        val submitBtn = view.findViewById<AppCompatButton>(R.id.saveEdit)
        val pupilName = view.findViewById<EditText>(R.id.pupil_name_edit_text)
        pupilImage = view.findViewById(R.id.profImg)

        val pupilLatLng = view.findViewById<TextView>(R.id.latLng_name_edit_text)

        val countries = listOf("Kenya", "Nigeria", "Rwanda", "Ghana", "South Africa", "Uganda", "Tanzania")

        var selectedCountry: String

        val countryDropdown = view.findViewById<AutoCompleteTextView>(R.id.countryDropdown)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item, countries)
        countryDropdown.setAdapter(adapter)
        countryDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedCountry = countries[position]
            sharedViewModel.updateCountry(countryDropdown.text.toString())
        }

        val pupil = sharedViewModel.selectedPupil.value
        if (pupil != null) {
            if (pupil.name.isNotEmpty()) {
                pupilName.setText(pupil.name)
            }
            if (pupil.country.isNotEmpty()) {
                selectedCountry = pupil.country
                countryDropdown.setText(pupil.country)
            }

            if (pupil.latitude != 0.0 && pupil.longitude != 0.0) {
                pupilLatLng.text = "Lat: ${pupil.latitude}, Lng: ${pupil.longitude}"
            }

            if (pupil.image.isNotEmpty()) {
                imageUrl = pupil.image
                context?.let {
                    Glide.with(it)
                        .load(imageUrl)
                        .placeholder(R.drawable.no_user_photo)
                        .into(pupilImage)
                }
            }
        }

        backBtn.setOnClickListener {
            backToPrevious()
        }

        pupilLatLng.setOnClickListener {
            openFragmentPage(MapsFragment())
        }

        pupilImage.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        submitBtn.setOnClickListener {
            sharedViewModel.updateCountry(countryDropdown.text.toString())
            val name = pupilName.text.toString()
            val id = pupil?.pupilId ?: 0
            val lat = pupil?.latitude
            val lng = pupil?.longitude
            imageUrl = pupil?.image.toString()
            selectedCountry = pupil?.country.toString()


            if (name.isNotEmpty() && selectedCountry.isNotEmpty() && (lat != null && lat != 0.0) && (lng != null && lng != 0.0) && ::imageUrl.isInitialized) {
                val pupilData = Pupil(
                    pupilId = id,
                    name = name,
                    country = selectedCountry,
                    image = imageUrl,
                    longitude = lng,
                    latitude = lat
                )
                observeViewModel()
                viewModel.createPupil(pupilData)
            } else {
                showToast("Please fill all fields")
            }
        }
    }

    @Deprecated("Deprecated in Android API 30, use Activity Result API instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri = data.data!!
            imageUrl = uri.toString()
            context?.let {
                Glide.with(it)
                    .load(imageUrl)
                    .placeholder(R.drawable.no_user_photo)
                    .into(pupilImage)
            }
            sharedViewModel.updateImage(imageUrl)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            showToast("Error: " + ImagePicker.getError(data))
        } else {
            showToast("Task Cancelled")
        }
    }


    private fun observeViewModel() {
        viewModel.updateSuccess.observe(viewLifecycleOwner) { successMsg ->
            viewModel.refreshPupils()
            backToPrevious()
            showToast(successMsg)
        }

        viewModel.updateError.observe(viewLifecycleOwner) { errorMsg ->
            showToast(errorMsg)
        }

    }


}