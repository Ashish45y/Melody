package dev.ashish.melody.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import dev.ashish.melody.R
import dev.ashish.melody.adapter.SongAdapter
import dev.ashish.melody.other.Status
import dev.ashish.melody.ui.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home)
{
    lateinit var mainViewModel: MainViewModel

    @Inject

    lateinit var songAdapter: SongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

    }
    private fun setupRecyclerView() = {

    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){ result ->
            when(result.status){
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING ->Unit
            }

        }
    }
}
