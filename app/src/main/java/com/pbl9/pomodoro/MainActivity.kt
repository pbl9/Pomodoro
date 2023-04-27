package com.pbl9.pomodoro

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pbl9.pomodoro.databinding.ActivityMainBinding
import com.pbl9.pomodoro.models.states.AppState
import com.pbl9.pomodoro.utils.SimpleVibrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private val simpleVibrator by lazy {
        SimpleVibrator(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionButton()
        observeAppState()
        observeSideEffects()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.remainedTime.collect {
                    binding.timerTextView.text = it
                }
            }
        }
    }

    private fun setupActionButton() {
        binding.actionButton.setOnClickListener {
            viewModel.doAction()
        }
    }

    private fun observeSideEffects() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effectFlow.collect {
                    when(it) {
                        Effect.SESSION_BREAK_END -> simpleVibrator.vibrate()
                        Effect.WORK_END -> playSound()
                    }
                }
            }
        }
    }

    private fun observeAppState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appState.collect {
                    bindAppStateToView(it)
                }
            }
        }
    }

    private fun bindAppStateToView(appState: AppState) {
        binding.sessionTextView.text = if(appState.sessionNumber > 0) getString(R.string.session_number_format, appState.sessionNumber) else ""
        binding.stateInfoTextView.text = getString(appState.stateInfoTextRes)

        val textColor = ContextCompat.getColor(this, appState.color)
        binding.sessionTextView.setTextColor(textColor)
        binding.timerTextView.setTextColor(textColor)

        binding.actionButton.text = getString(appState.actionButtonTextRes)
    }

    private fun playSound() {
        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(this, notification)
        r.play()
    }
}

