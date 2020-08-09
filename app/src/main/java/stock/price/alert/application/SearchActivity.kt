package stock.price.alert.application

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley


class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_search)

        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { // query ->
                //doMySearch(query)
            }
        }
        val searchBar: SearchView = findViewById(R.id.searchBar)
        searchBar.setIconifiedByDefault(false)
        searchBar.onActionViewExpanded()
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {


                val url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=ko&interval=1min&apikey=5B4ZQG1WXCAB5L1N"
                val url2 = "https://www.google.com"
                val textView: TextView = findViewById(R.id.textView)
                val requestQueue = Volley.newRequestQueue(this@SearchActivity)
                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.GET, url, null,
                    Response.Listener { response->
                        textView.text = "Response: %s".format(response.toString())
                        Toast.makeText(applicationContext, "success", Toast.LENGTH_SHORT).show()
                    },
                    Response.ErrorListener { error ->
                        // TODO: Handle error
                        textView.text = "Response: %s".format(error.toString())
                        Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                    }
                )

                // Access the RequestQueue through your singleton class.
                requestQueue.add(jsonObjectRequest)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

    }







}



