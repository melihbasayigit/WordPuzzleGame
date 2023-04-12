package com.yeocak.wordpuzzle.ui.screen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.yeocak.wordpuzzle.R
import com.yeocak.wordpuzzle.data.local.DBHelper
import com.yeocak.wordpuzzle.databinding.FragmentPopUpBinding
import com.yeocak.wordpuzzle.model.Score
import com.yeocak.wordpuzzle.ui.screen.score.ScoreFragmentDirections

class PopUpFragment(val name: String, val score: Int) : DialogFragment() {

    private var _binding: FragmentPopUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPopUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPlayAgain.setOnClickListener {
            dismiss()
            navigateGameFragment(name)
        }

        binding.btnSaveScore.setOnClickListener {
            //addScoreToDB(name, score)
            dismiss()
            navigateScoreFragment(name)
        }
        val scoreString = "Your Score : "
        binding.txtYourScore.text = scoreString + score.toString()

    }

    private fun addScoreToDB(name: String, score: Int) {
        val db = DBHelper(this.requireContext())
        val lastScore = Score(1, name, score)
        db.insertData(lastScore)
    }

    private fun navigateScoreFragment(name: String) {
        val action = GameFragmentDirections.actionGameFragmentToScoreFragment(name)
        findNavController().navigate(action)
    }

    private fun navigateGameFragment(name: String) {
        val action = GameFragmentDirections.actionGameFragmentSelf(name)
        findNavController().navigate(action)
    }

}