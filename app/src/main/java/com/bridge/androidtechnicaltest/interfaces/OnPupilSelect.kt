package com.bridge.androidtechnicaltest.interfaces

import com.bridge.androidtechnicaltest.db.Pupil

interface OnPupilSelect {
    fun onSpotlightSelection(data: Pupil, pos: String)
}