package com.yeocak.wordpuzzle.ui.screen.score

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yeocak.wordpuzzle.data.local.DBHelper
import com.yeocak.wordpuzzle.databinding.FragmentScoreBinding
import com.yeocak.wordpuzzle.model.Score
import com.yeocak.wordpuzzle.ui.screen.GameFragmentDirections
import com.yeocak.wordpuzzle.ui.screen.StartFragmentArgs

class ScoreFragment : Fragment() {

    private var _binding: FragmentScoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentScoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments ?: return
        val args = StartFragmentArgs.fromBundle(bundle)
        btnPlayAgainClickListener(args.name)

        // ADAPTER SETTINGS
        val context = binding.root.context
        val database = DBHelper(context)
        val leaderboardList = database.readData()
        binding.recyclerViewLeaderboard.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewLeaderboard.adapter = ScoreAdapter(leaderboardList)
    }

    private fun btnPlayAgainClickListener(name: String) {
        binding.btnPlayAgain.setOnClickListener {
            navigateStartFragment(name)
        }
    }

    private fun navigateStartFragment(name: String) {
        val action = ScoreFragmentDirections.actionScoreFragmentToStartFragment(name)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}