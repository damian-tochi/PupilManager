package com.bridge.androidtechnicaltest.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bridge.androidtechnicaltest.R
import com.bridge.androidtechnicaltest.databinding.PupilListItemBinding
import com.bridge.androidtechnicaltest.db.Pupil
import com.bridge.androidtechnicaltest.interfaces.OnPupilSelect
import com.bumptech.glide.Glide


class PupilListAdapter(private val onPupilSelect: OnPupilSelect) : ListAdapter<Pupil, PupilListAdapter.ViewHolder>(PupilDiffCallback()) {

    inner class ViewHolder(val binding: PupilListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PupilListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pupil = getItem(position)

        with(holder.binding) {
            tvPupilName.text = pupil.name
            tvPupilCountry.text = pupil.country
            Glide.with(ivAlbum.context)
                .load(pupil.image)
                .placeholder(R.drawable.no_user_photo)
                .into(ivAlbum)

            ivParent.setOnClickListener {
                onPupilSelect.onSpotlightSelection(pupil, position.toString())
            }
        }
    }

}

class PupilDiffCallback : DiffUtil.ItemCallback<Pupil>() {
    override fun areItemsTheSame(oldItem: Pupil, newItem: Pupil): Boolean {
        return oldItem.pupilId == newItem.pupilId
    }

    override fun areContentsTheSame(oldItem: Pupil, newItem: Pupil): Boolean {
        return oldItem == newItem
    }
}
