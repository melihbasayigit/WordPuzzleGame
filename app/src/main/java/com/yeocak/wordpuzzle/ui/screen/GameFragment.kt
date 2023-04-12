package com.yeocak.wordpuzzle.ui.screen

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.ColorFilter
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yeocak.wordpuzzle.R
import com.yeocak.wordpuzzle.data.local.DBHelper
import com.yeocak.wordpuzzle.databinding.FragmentGameBinding
import com.yeocak.wordpuzzle.model.GameState
import com.yeocak.wordpuzzle.model.Score
import com.yeocak.wordpuzzle.utils.Letters

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    lateinit var gestureDetector: GestureDetector
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
        gestureDetector = GestureDetector(
            this.requireContext(),
            GestureDetector.SimpleOnGestureListener()
        )
        defaultColorFilter = binding.imgHealBarLeft.colorFilter
        // AFTER DEBUG DELETE THE BELLOW LINE
        gameSpeedLock = true
        // DO NOT OPEN THE BELLOW LINE ON DEBUG
        //binding.gameView.letterAddingFrequency = 5000L
        binding.gameView.setOnGameStateChangeListener { state ->
            if (state == GameState.FINISHED) {
                val bundle = arguments
                var args = bundle?.let { it1 -> GameFragmentArgs.fromBundle(it1) }
                if (args != null) {
                    showPopupWindow(args.name)
                }
            }
        }
        binding.gameView.setOnCurrentWordChangeListener { word ->
            binding.txtTypedWord.text = word
        }
        onClickListeners()
    }
    private fun showPopupWindow(name: String) {
        val showPopUp = PopUpFragment(name, score)
         showPopUp.show((activity as AppCompatActivity).supportFragmentManager,
                            "showPopUp")
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
            if (binding.gameView.gameState == GameState.RUNNING) {
                val word = binding.gameView.currentWord
                val result = checkWord(word)
                if (result) {
                    val wordPoint = getWordPoint(word)
                    score += wordPoint
                    binding.gameView.popCurrentWord()

                } else {
                    binding.gameView.cancelCurrentWord()
                    wrongWordAction(word)
                }
                binding.txtScore.text = score.toString()
                if (!gameSpeedLock) {
                    changeGameSpeed()
                }
            }
        }
    }

    private fun wrongWordAction(word: String) {
        when (health) {
            2 -> {
                health = 1
            }
            1 -> {
                health = 0
            }
            0 -> {
                health = 2
                binding.gameView.sendLetterRow()
            }
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