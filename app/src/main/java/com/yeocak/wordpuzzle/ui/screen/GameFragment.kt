package com.yeocak.wordpuzzle.ui.screen

import android.graphics.ColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.yeocak.wordpuzzle.R
import com.yeocak.wordpuzzle.databinding.FragmentGameBinding
import com.yeocak.wordpuzzle.model.Directions
import com.yeocak.wordpuzzle.model.GameState
import com.yeocak.wordpuzzle.utils.Letters
import com.yeocak.wordpuzzle.utils.WordListUtils

class GameFragment : Fragment() {

	private var _binding: FragmentGameBinding? = null
	private val binding get() = _binding!!

	private var health: Int = 2
	private var score: Int = 0
	private var defaultColorFilter: ColorFilter? = null
	private var gameSpeedLock: Boolean = false

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		// Inflate the layout for this fragment
		_binding = FragmentGameBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		// KODUNU BURAYA YAZ
		defaultColorFilter = binding.imgHealBarLeft.colorFilter
		// AFTER DEBUG DELETE THE BELLOW LINE
		gameSpeedLock = true
		//
		binding.gameView.setOnGameStateChangeListener { state ->
			if (state == GameState.FINISHED) {
				val bundle = arguments
				val args = bundle?.let { it1 -> GameFragmentArgs.fromBundle(it1) }
				if (args != null) {
					showPopupWindow(args.name)
				}
			}
		}
		binding.gameView.setOnCurrentWordChangeListener { word ->
			binding.txtTypedWord.text = word
		}
		binding.gameView.setOnSwipeListener { direction ->
			if (direction == Directions.RIGHT || direction == Directions.LEFT) {
				approve()
			}
		}
		onClickListeners()
	}

	private fun showPopupWindow(name: String) {
		val showPopUp = PopUpFragment(name, score)
		showPopUp.show(
			(activity as AppCompatActivity).supportFragmentManager,
			"showPopUp"
		)
	}

	private fun onClickListeners() {
		btnPauseClickListener()
		btnApproveClickListener()
		btnRefuseClickListener()
	}

	private fun btnPauseClickListener() {
		binding.btnPause.setOnClickListener {
			if (binding.gameView.gameState != GameState.FINISHED) {
				if (binding.gameView.gameState == GameState.RUNNING) {
					binding.gameView.pauseGame()
					binding.btnPause.setImageResource(android.R.drawable.ic_media_play)
				} else if (binding.gameView.gameState == GameState.PAUSED) {
					binding.gameView.unpauseGame()
					binding.btnPause.setImageResource(android.R.drawable.ic_media_pause)
				}
			}
		}
	}

	private fun btnApproveClickListener() {
		binding.btnApprove.setOnClickListener {
			approve()
		}
	}

	private fun approve() {
		if (binding.gameView.gameState == GameState.RUNNING) {
			val word = binding.gameView.currentWord
			val result = WordListUtils.isWordAvailable(word, requireActivity().application)
			if (result) {
				val wordPoint = getWordPoint(word)
				score += wordPoint
				binding.gameView.popCurrentWord()

			} else {
				binding.gameView.cancelCurrentWord()
				wrongWordAction()
			}
			binding.txtScore.text = score.toString()
			if (!gameSpeedLock) {
				changeGameSpeed()
			}
		}
	}

	private fun wrongWordAction() {
		if (health != 0) {
			health -= 1
		} else {
			health = 2
			binding.gameView.sendLetterRow()
		}
		updateHealBarUI()
	}

	private fun updateHealBarUI() {
		when (health) {
			1 -> {
				deActiveHealBarLeft(true)
			}

			0 -> {
				deActiveHealBarRight(true)
			}

			else -> {
				deActiveHealBarRight(false)
				deActiveHealBarLeft(false)
			}
		}
	}

	private fun checkWord(word: String): Boolean {
		// CHANGE THIS FUNCTION
		if (word.contains('A') || word.contains('E')) {
			return true
		}
		return false
	}

	private fun getWordPoint(word: String): Int {
		val chars = word.toCharArray()
		var point = 0
		for (char in chars) {
			point += Letters.valueOf(char.toString()).point
		}
		return point
	}

	private fun changeGameSpeed() {
		if (score < 100) {
			binding.gameView.letterAddingFrequency = 5000L
		} else if (score < 200) {
			binding.gameView.letterAddingFrequency = 4000L
		} else if (score < 300) {
			binding.gameView.letterAddingFrequency = 3000L
		} else if (score < 400) {
			binding.gameView.letterAddingFrequency = 2000L
		} else {
			binding.gameView.letterAddingFrequency = 1000L
		}
	}

	private fun btnRefuseClickListener() {
		binding.btnRefuse.setOnClickListener {
			binding.gameView.cancelCurrentWord()
		}
	}

	private fun deActiveHealBarLeft(deActive: Boolean) {
		if (!deActive) {
			binding.imgHealBarLeft.colorFilter = defaultColorFilter
		} else {
			binding.imgHealBarLeft.setColorFilter(
				ContextCompat.getColor(requireContext(), R.color.dim_gray),
				android.graphics.PorterDuff.Mode.MULTIPLY
			)
		}
	}

	private fun deActiveHealBarRight(deActive: Boolean) {
		if (!deActive) {
			binding.imgHealBarRight.colorFilter = defaultColorFilter
		} else {
			binding.imgHealBarRight.setColorFilter(
				ContextCompat.getColor(requireContext(), R.color.dim_gray),
				android.graphics.PorterDuff.Mode.MULTIPLY
			)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

}