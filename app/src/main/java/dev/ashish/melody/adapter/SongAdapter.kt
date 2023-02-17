package dev.ashish.melody.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import dev.ashish.melody.R
import dev.ashish.melody.data.entities.Song
import javax.inject.Inject

import androidx.appcompat.widget.AppCompatTextView;


class SongAdapter @Inject constructor(
    private val glide: RequestManager
): RecyclerView.Adapter<SongAdapter.SongViewHolder>(){
    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val diffCallback = object :DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return  oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this,diffCallback)

    var songs: List<Song>
    get() = differ.currentList
    set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,
        false))
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
       val song = songs[position]
        holder.apply {
            glide.load(song.imageUrl).into(itemView.findViewById(R.id.ivItemImage))
            itemView.findViewById<AppCompatTextView>(R.id.tvPrimary).text = song.title
            itemView.findViewById<AppCompatTextView>(R.id.tvSecondary).text = song.subtitle
            itemView.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }
    private var onItemClickListener: ((Song) -> Unit)? = null

    fun setOnItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }
    override fun getItemCount(): Int {
        return songs.size
    }
}