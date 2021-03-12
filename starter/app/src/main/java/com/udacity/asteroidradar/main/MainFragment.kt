    package com.udacity.asteroidradar.main

import android.R.attr
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.picasso.Picasso
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.AsteroidRowBinding
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ExecutionException


    class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private var viewModelAdapter: AsteroidsAdapter? = null

    private val viewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(this, MainViewModel.Factory(activity.application)).get(MainViewModel::class.java)
        //ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentMainBinding.inflate(inflater)
        //binding.lifecycleOwner = this


       binding.setLifecycleOwner(viewLifecycleOwner)



     //   Log.d("FLUX", "onCreateView " + (viewModel.asteroids.value?.size ?: "NO SIZE"))

        setHasOptionsMenu(true)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel


        viewModelAdapter = AsteroidsAdapter(AsteroidClick {
            Log.d("FLUX", "clicked")
            viewModel.onAsteroidClicked(it)
        })

         binding.asteroidRecycler.adapter = viewModelAdapter


        Log.d("FLUX", "onViewCreated " + (viewModel.asteroids.value?.size ?: "NO SIZE"))
        viewModel.asteroids.observe(viewLifecycleOwner, Observer<List<Asteroid>> { asteroids ->
            asteroids?.apply {
                Log.d("FLUX", "SIZE adapter " + asteroids.size)
                viewModelAdapter?.asteroids = asteroids
            }
        })



        viewModel.dbAsteroids.observe(viewLifecycleOwner, Observer { dbAsteroids ->
            dbAsteroids?.apply {
                Log.d("FLUX", "observe dbAsteroids " + dbAsteroids.size)
            }
        })

        viewModel.navigateToDetail.observe(viewLifecycleOwner, Observer { asteroid ->
            asteroid?.let {
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(asteroid))
                viewModel.onDetailNavigated()
            }

        })

        viewModel.imageOfTheDay.observe(viewLifecycleOwner, Observer { image ->
            if (image.mediaType == "image")
                Picasso.with(context)
                        .load(image.url)
                        .into(binding.activityMainImageOfTheDay)
            binding.activityMainImageOfTheDay.contentDescription = image.title
        })


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }

}

class AsteroidClick(val block: (Asteroid) -> Unit) {
    /**
     * Called when a video is clicked
     *
     * @param video the video that was clicked
     */
    fun onClick(asteroid: Asteroid) = block(asteroid)
}

class AsteroidsAdapter(val callback: AsteroidClick) : RecyclerView.Adapter<AsteroidViewHolder>() {
    var asteroids: List<Asteroid> = emptyList()
        set(value) {
            Log.d("FLUX", "Notify data set changed")
            field = value
            // For an extra challenge, update this to use the paging library.

            // Notify any registered observers that the data set has changed. This will cause every
            // element in our RecyclerView to be invalidated.
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsteroidViewHolder {
        Log.d("FLUX", "onCreateViewHolder")
        return AsteroidViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AsteroidViewHolder, position: Int) {
        //Log.d("FLUX", "onBindViewHolder")
        holder.bind(holder, asteroids[position])
        holder.binding.videoCallback = callback
    }

    override fun getItemCount(): Int {
        //Log.d("FLUX", "SIZE  item count " + asteroids.size)
        return asteroids.size
    }

    class AsteroidListener(val clickListener: (asteroid: Asteroid) -> Unit){
        fun onClick(asteroid: Asteroid) = clickListener(asteroid)
    }

}

class AsteroidViewHolder private constructor(val binding: AsteroidRowBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(holder: AsteroidViewHolder, item: Asteroid){
        binding.name.setText(item.codename)
        binding.date.setText(item.closeApproachDate)
        binding.asteroid = item
        //binding.clickListener = clickListener

        holder.itemView.setOnClickListener(View.OnClickListener { view: View ->

        })
    }

    companion object {
        fun from(parent: ViewGroup): AsteroidViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = AsteroidRowBinding.inflate(layoutInflater, parent, false)
            //val view = layoutInflater.inflate(R.layout.asteroid_row,parent,false)
            return AsteroidViewHolder(binding)
        }
    }
}