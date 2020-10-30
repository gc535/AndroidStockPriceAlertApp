package stock.price.alert.application.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import stock.price.alert.application.MainViewModel
import stock.price.alert.application.R


class SearchFragment : Fragment() {
    private lateinit var searchAdapter : ArrayAdapter<*>
    private lateinit var rootView : View
    private lateinit var mainViewModel : MainViewModel
    private val homeArgs : SearchFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        rootView = inflater.inflate(R.layout.fragment_search, container, false)

        initResultListView()
        initSearchLogic()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // show user the ticker information if there are previous searched ticker
        if (!homeArgs.forceSearch
            && mainViewModel.mCur_ticker_name.value != null
            && mainViewModel.mCur_ticker_symbol.value != null) {
            val name : String = mainViewModel.mCur_ticker_name.value.toString()
            val symbol : String = mainViewModel.mCur_ticker_symbol.value.toString()

            // navigate with args
            val goto_ticker_explorer_action = SearchFragmentDirections
                .actionNavigationTickerSearchToTickerExploreFragment(name, symbol)
            findNavController().navigate(goto_ticker_explorer_action)
        }

    }

    private fun initResultListView() {
        val result_listview : ListView= rootView.findViewById(R.id.result_listview)

        result_listview.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ArrayList<String>())
        result_listview.emptyView = rootView.findViewById(R.id.empty_textView)
        result_listview.divider = null
        result_listview.dividerHeight = 0

        result_listview.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val ticket_symbol = parent?.getItemAtPosition(position) as String
                ticket_symbol.let{
                    val ticker_name = ticket_symbol.split(" : ")[0]
                    val ticker_symbol = ticket_symbol.split(" : ")[1]
                    mainViewModel.ChangeTicker(ticker_name, ticker_symbol)
                    mainViewModel.mHasHistory = true

                    // navigate with args
                    val goto_ticker_explorer_action = SearchFragmentDirections
                        .actionNavigationTickerSearchToTickerExploreFragment(ticker_name, ticker_symbol)
                    findNavController().navigate(goto_ticker_explorer_action)

                }
            }
    }


    private fun initSearchLogic() {

        val searchBar: SearchView = rootView.findViewById(R.id.searchBar)
        searchBar.setIconifiedByDefault(false)
        searchBar.onActionViewExpanded()

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val requestQueue = Volley.newRequestQueue(requireContext())
                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.GET, QueryAPI()
                        .ProbTicker(newText), null,
                    Response.Listener { response->
                        // populate search array
                        searchAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            QueryAPI().ParseProbResponse(response)
                        )
                        val result_listview : ListView= rootView.findViewById(R.id.result_listview)
                        result_listview.adapter = searchAdapter
                    },
                    Response.ErrorListener { error ->
                        // TODO: Handle error
                    }
                )

                // Access the RequestQueue through your singleton class.
                requestQueue.add(jsonObjectRequest)
                return true
            }
        })
    }
}



