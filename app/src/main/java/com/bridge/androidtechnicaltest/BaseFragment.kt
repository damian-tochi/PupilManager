package com.bridge.androidtechnicaltest

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bridge.androidtechnicaltest.viewmodel.PupilSharedViewModel
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import android.Manifest




abstract class BaseFragment : Fragment() {
    protected open val scope = CoroutineScope(Job() + Dispatchers.IO)
    val sharedViewModel: PupilSharedViewModel by activityViewModels()
    private val REQUEST_PERMISSIONS = 1001

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(setLayoutView(), container, false)
        return view
    }

    protected abstract fun setLayoutView(): Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize(view, savedInstanceState)
    }

    protected abstract fun initialize(view: View, savedInstanceState: Bundle?)

    fun openFragmentPage(fragment: Fragment) {
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.addToBackStack(null)
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        ft.replace(R.id.container, fragment).commit()
    }

   fun backToPrevious() {
       requireActivity().supportFragmentManager.popBackStack()
    }

    fun backToRoot() {
        requireActivity().supportFragmentManager.popBackStackImmediate(null, android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun requestPermissionsIfNeeded(): Boolean {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), REQUEST_PERMISSIONS)
            return true
        } else {
            return false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {

            } else {
                Toast.makeText(requireContext(), "Permissions required to pick or take a photo", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}