package com.github.iwle.ausm

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.iwle.ausm.adapter.ReviewListAdapter
import com.github.iwle.ausm.model.Establishment

class ReviewFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewListAdapter: ReviewListAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    companion object {
        fun newInstance() : ReviewFragment {
            return ReviewFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_review, container, false)
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener(this)
        // Set refresh animation colour cycle
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorSecondary)
        fetchData()
        recyclerView = v.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = reviewListAdapter
        }
        return v
    }

    private fun fetchData() {
        val data = ArrayList<Establishment>()
        reviewListAdapter = ReviewListAdapter(data)

        // Stop the refresh animation
        val runnable = Runnable {
            if(swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
        Handler().postDelayed(runnable, 1000)
    }

    override fun onRefresh() {
        fetchData()
    }
}