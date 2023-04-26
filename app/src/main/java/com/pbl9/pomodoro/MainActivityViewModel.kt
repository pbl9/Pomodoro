package com.pbl9.pomodoro

import androidx.lifecycle.ViewModel
import com.pbl9.pomodoro.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val journalRepository: JournalRepository) : ViewModel() {

}