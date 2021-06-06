package com.oksik.github_app.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.oksik.github_app.R
import com.oksik.github_app.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val adapter = CustomAdapter(CommitItemListener { viewModel.onItemClicked(it) })
        binding.recyclerView.adapter = adapter
        viewModel.commits.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        viewModel.commitsInformationToShare.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                val intent = ShareCompat.IntentBuilder.from(requireActivity()).setText(it)
                    .setType("text/plain").intent
                startActivity(intent)
                viewModel.commitInformationSent()
            }
        })

        viewModel.snackbarMessage.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                Snackbar.make(binding.main, it, Snackbar.LENGTH_LONG).show()
                viewModel.snackbarMessageShowed()
            }
        })

        return binding.root
    }
}