package com.yeocak.wordpuzzle.ui.screen

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.yeocak.wordpuzzle.data.local.DBHelper
import com.yeocak.wordpuzzle.databinding.FragmentPopUpBinding
import com.yeocak.wordpuzzle.model.Score

class PopUpFragment() : DialogFragment() {

	private var name: String? = null
	private var score: Int? = null

	constructor(name: String, score: Int) : this() {
		this.name = name
		this.score = score
	}

	private var _binding: FragmentPopUpBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		// Inflate the layout for this fragment
		_binding = FragmentPopUpBinding.inflate(inflater, container, false)
		if (dialog != null && dialog?.window != null) {
			dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
		}
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		this.isCancelable = false
		binding.btnPlayAgain.setOnClickListener {
			dismiss()
			navigateGameFragment()
		}

		binding.btnSaveScore.setOnClickListener {
			addScoreToDB()
			dismiss()
			navigateScoreFragment()
		}
		val scoreString = "Your Score : "
		binding.txtYourScore.text = scoreString + score.toString()

	}

	private fun addScoreToDB() {
		val safeName = name ?: return
		val safeScore = score ?: return

		val db = DBHelper(this.requireContext())
		val lastScore = Score(1, safeName, safeScore)
		db.insertData(lastScore)
	}

	private fun navigateScoreFragment() {
		name?.let { safeName ->
			val action = GameFragmentDirections.actionGameFragmentToScoreFragment(safeName)
			findNavController().navigate(action)
		} ?: run {
			findNavController().navigateUp()
		}
	}

	private fun navigateGameFragment() {
		name?.let { safeName ->
			val action = GameFragmentDirections.actionGameFragmentSelf(safeName)
			findNavController().navigate(action)
		} ?: run {
			findNavController().navigateUp()
		}
	}

}