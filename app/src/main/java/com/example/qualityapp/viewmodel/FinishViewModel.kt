package com.example.qualityapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.qualityapp.data.session.QuestionnaireSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FinishViewModel @Inject constructor(
    val session: QuestionnaireSession
) : ViewModel()