package dev.ashish.melody.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.ashish.melody.adapter.SongAdapter
import dev.ashish.melody.databinding.FragmentHomeBinding
import dev.ashish.melody.other.Status
import dev.ashish.melody.ui.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(){
    private lateinit var binding: FragmentHomeBinding
    lateinit var mainViewModel: MainViewModel
    @Inject
    lateinit var songAdapter: SongAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
       setupRecyclerView()
        subscribeToObservers()
       songAdapter.setOnItemClickListener {
           mainViewModel.playOrToggleSong(it)
       }
    }
    private fun setupRecyclerView() =  binding.rvAllSongs.apply{
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())

    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){ result ->
            when(result.status){
                Status.SUCCESS -> {
                    binding.allSongsProgressBar.isVisible= false
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> binding.allSongsProgressBar.isVisible=true

            }

        }
    }
}
