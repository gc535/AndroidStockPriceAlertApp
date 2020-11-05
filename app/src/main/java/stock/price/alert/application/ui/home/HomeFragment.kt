package stock.price.alert.application.ui.home

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_search.*
import stock.price.alert.application.MainViewModel
import stock.price.alert.application.R
import stock.price.alert.application.ui.search.SearchFragmentDirections

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mainViewModel : MainViewModel

    private lateinit var rootView : View

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        mainViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        rootView = inflater.inflate(R.layout.fragment_home, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchButton: Button = rootView.findViewById(R.id.goToSearchActivity)
        searchButton.setOnClickListener {
            val goto_search_action = HomeFragmentDirections
                .actionNavigationHomeToNavigationTickerSearch().setForceSearch(true)
            findNavController().navigate(goto_search_action)
        }

        initWatchListView()

    }


    private fun initWatchListView() {
        val wathlist_listview : ListView = rootView.findViewById(R.id.wathlist_listview)

        val test_list : ArrayList<String> = arrayListOf("CocaCola : KO : 40 : 55 : 56", "Global Jets : JETS : 14 : 17 : 1", "Amazon : AMZN : 2900 : 3300 : 2800")
        wathlist_listview.adapter =
            WatchListViewAdapter(requireContext(), test_list)
        wathlist_listview.emptyView = rootView.findViewById(R.id.empty_watchlist_textview)
        wathlist_listview.dividerHeight = 2

        wathlist_listview.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val ticket_symbol = parent?.getItemAtPosition(position) as String
                ticket_symbol.let{
                    val ticker_name = ticket_symbol.split(" : ")[0]
                    val ticker_symbol = ticket_symbol.split(" : ")[1]
                    mainViewModel.ChangeTicker(ticker_name, ticker_symbol)
                    mainViewModel.mHasHistory = true

                    // navigate with args to ticker explorer fragment

                }
            }
    }

}