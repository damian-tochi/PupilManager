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
import com.bridge.androidtechnicaltest.viewmodel.PupilListViewModel
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.imageview.ShapeableImageView
import org.koin.androidx.viewmodel.ext.android.viewModel


class PupilEditFragment : BaseFragment() {

    private lateinit var pupilImage: ShapeableImageView
    private lateinit var imageUrl: String
    private val viewModel: PupilListViewModel by viewModel()

    override fun setLayoutView(): Int {
        return R.layout.fragment_pupil_edit
    }

    override fun initialize(view: View, savedInstanceState: Bundle?) {

        val pupilId = view.findViewById<EditText>(R.id.pupil_id)
        val pupilName = view.findViewById<EditText>(R.id.pupil_name)
        pupilImage = view.findViewById(R.id.pupil_image)
        val latLng = view.findViewById<TextView>(R.id.latLng)
        val editLatLng = view.findViewById<ImageView>(R.id.editLatLng)
        val submitBtn = view.findViewById<AppCompatButton>(R.id.submit)
        val backButton = view.findViewById<ImageView>(R.id.back_button)
        val deleteButton = view.findViewById<ImageView>(R.id.delete_button)
        val countryDropdown = view.findViewById<AutoCompleteTextView>(R.id.countryDropdown)

        val countries = listOf("Kenya", "Nigeria", "Rwanda", "Ghana", "South Africa", "Uganda", "Tanzania")

        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item, countries)
        countryDropdown.setAdapter(adapter)

        countryDropdown.setOnItemClickListener { _, _, position, _ ->
            countries[position]
        }

        val pupil = sharedViewModel.selectedPupil.value
        if (pupil != null) {
            pupilId.setText(pupil.pupilId.toString())
            pupilName.setText(pupil.name)
            countryDropdown.setText(pupil.country)
            latLng.text = "Lat: ${pupil.latitude}, Lng: ${pupil.longitude}"
            imageUrl = pupil.image

            context?.let {
                Glide.with(it)
                    .load(imageUrl)
                    .placeholder(R.drawable.no_user_photo)
                    .into(pupilImage)
            }
        }

        pupilImage.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        submitBtn.setOnClickListener {
            val id = pupilId.text.toString()
            val name = pupilName.text.toString()
            val country = countryDropdown.text.toString()
            val lat = sharedViewModel.selectedPupil.value?.latitude ?: 0.0
            val lng = sharedViewModel.selectedPupil.value?.longitude ?: 0.0

            if (id.isEmpty() || name.isEmpty() || country.isEmpty()) {
                showToast("Please fill all fields")
                return@setOnClickListener
            }
            val pupilData = Pupil(
                pupilId = id.toLong(),
                name = name,
                country = country,
                image = imageUrl,
                longitude = lng,
                latitude = lat,
                imageSynced = sharedViewModel.selectedPupil.value?.imageSynced?:false
            )
            observeViewModel()
            viewModel.updatePupil(id.toInt(), pupilData)
            sharedViewModel.updatePupil(id.toInt(), name, country, imageUrl)
        }

        backButton.setOnClickListener {
            backToPrevious()
        }

        deleteButton.setOnClickListener {
            pupil?.let {
                observeViewModel()
                viewModel.deletePupil(it)
            } ?: run {
                showToast("No pupil selected for deletion")
            }
        }

        editLatLng.setOnClickListener {
            val lat = pupil?.latitude ?: 0.0
            val lng = pupil?.longitude ?: 0.0
            sharedViewModel.updateLatLng(lat, lng)
            openFragmentPage(MapsFragment())
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

        viewModel.deleteSuccess.observe(viewLifecycleOwner) { deleteMsg ->
            viewModel.refreshPupils()
            showToast(deleteMsg)
            backToRoot()
        }

    }

}