package com.udacity.asteroidradar.main

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import kotlinx.android.synthetic.main.asteroid_row.view.*

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

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
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        Log.d("FLUX","onCreateView "+ (viewModel.asteroids.value?.size ?:"NO SIZE" ))

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FLUX","onViewCreated "+ (viewModel.asteroids.value?.size ?:"NO SIZE" ))
        viewModel.asteroids.observe(viewLifecycleOwner, Observer<List<Asteroid>> {
            asteroids ->
                asteroids?.apply {
                    Log.d("FLUX","observe asteroids "+asteroids.size)
                    val adapter = AsteroidsAdapter(viewModel.asteroids.value!!)
                    binding.asteroidRecycler.adapter = adapter
                }
        })

        viewModel.dbAsteroids.observe(viewLifecycleOwner, Observer {
            dbAsteroids ->
                dbAsteroids?.apply {
                    Log.d("FLUX","observe dbAsteroids "+dbAsteroids.size)
                }
        })

        viewModel.imageOfTheDay.observe(viewLifecycleOwner, Observer {
            image ->
                if (image.mediaType == "image")
                    Picasso.with(context)
                        .load(image.url)
                        .into(binding.activityMainImageOfTheDay)
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

class AsteroidsAdapter(val asteroids: List<Asteroid>) : RecyclerView.Adapter<AsteroidViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsteroidViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.asteroid_row,parent,false)
        return AsteroidViewHolder(view)
    }

    override fun onBindViewHolder(holder: AsteroidViewHolder, position: Int) {
        val item = asteroids[position]
        holder.itemView.name.setText(item.codename)
        holder.itemView.date.setText(item.closeApproachDate)

        holder.itemView.setOnClickListener( View.OnClickListener { view: View ->
            view.findNavController().navigate(MainFragmentDirections.actionShowDetail(item))
        })

        var drawable = 0

        if (item.isPotentiallyHazardous){
            drawable = R.drawable.ic_status_potentially_hazardous
        }else{
            drawable = R.drawable.ic_status_normal
        }

        Picasso.with(holder.itemView.context)
                .load(R.drawable.ic_status_normal)
                .placeholder(drawable)
                .into(holder.itemView.status)
    }

    override fun getItemCount(): Int {
        return asteroids.size
    }

}

class AsteroidViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView
    val date: TextView
    val status: ImageView
    init {
        name = itemView.findViewById(R.id.name)
        date = itemView.findViewById(R.id.date)
        status = itemView.findViewById(R.id.status)
    }
}
