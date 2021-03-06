package stock.price.alert.application.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.fragment_search.*
import stock.price.alert.application.MainViewModel
import stock.price.alert.application.R
import stock.price.alert.application.ui.search.SearchFragmentDirections
import stock.price.alert.application.ui.stock.PricePloter

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mainViewModel : MainViewModel

    private lateinit var rootView : View
    private lateinit var watchlistView : ListView


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mainViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)


        rootView = inflater.inflate(R.layout.fragment_home, container, false)
        initWatchListView()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        val searchButton: Button = rootView.findViewById(R.id.goToSearchActivity)
        searchButton.setOnClickListener {
            val goto_search_action = HomeFragmentDirections
                .actionNavigationHomeToNavigationTickerSearch().setForceSearch(true)
            findNavController().navigate(goto_search_action)
        }

    }

    // ListView Update can be move to a dedicated function defines all observe behaviours
    private fun initWatchListView() {
        watchlistView = rootView.findViewById(R.id.wathlist_listview)
        watchlistView.adapter =
            WatchListViewAdapter(requireContext(), homeViewModel.mTickersWatchListLiveData.value!!)
        watchlistView.emptyView = rootView.findViewById(R.id.empty_watchlist_textview)
        watchlistView.dividerHeight = 2

        watchlistView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val watchlistEntry = parent?.getItemAtPosition(position) as WatchListEntry
                watchlistEntry.let{
                    val ticker_name = watchlistEntry.getName()
                    val ticker_symbol = watchlistEntry.getSymbol()
                    mainViewModel.ChangeTicker(ticker_name, ticker_symbol)
                    mainViewModel.mHasHistory = true
                    Toast.makeText(requireContext(), "selected ticker is: $ticker_name : $ticker_symbol", Toast.LENGTH_SHORT).show()

                    // navigate with args to ticker explorer fragment
                    val goto_ticker_explorer_action = HomeFragmentDirections
                        .actionNavigationHomeToNavigationTickerExplorer(ticker_name, ticker_symbol)
                    findNavController().navigate(goto_ticker_explorer_action)
                }
            }

        // subscribe ui update to livedata
        homeViewModel.mTickersWatchListLiveData.observe(
            viewLifecycleOwner, Observer { tickersWatchList ->
                (watchlistView.adapter as WatchListViewAdapter).notifyDataSetChanged()
                // update plot
                Log.d("OB", tickersWatchList.toString())
            }
        )

        homeViewModel.StartBackgroundUpdate(requireContext())
    }

    // since ViewModel is only detached not destroyed, we need to destroy it to prevent it keep
    // probing price data in the background.
    override fun onDestroyView() {
        super.onDestroyView()
        homeViewModel.Clear()
    }

}