package stock.price.alert.application.ui.search

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_search.*
import stock.price.alert.application.R
import stock.price.alert.application.ui.notifications.NotificationBuilder
import stock.price.alert.application.ui.stock.TickerExploreActivity


class SearchActivity : AppCompatActivity() {
    private lateinit var searchAdaptor : ArrayAdapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_search)

        initResultListView()
        initSearchLogic()

        val channel_name  = "Test Notification"
        val channel_id = "test_notification_channel"
        val channel_desc = "Test Notificatio Channel"
        NotificationBuilder().CreateNotificationChannel(this@SearchActivity, channel_name, channel_id, channel_desc)

        val notification_id = "test_notification"
        val builder = NotificationBuilder().CreateNotification(this, channel_id)


        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(11, builder.build())
        }

    }

    private fun initResultListView() {
        result_listview.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList<String>())
        result_listview.emptyView = empty_textView
        result_listview.divider = null
        result_listview.dividerHeight = 0

        result_listview.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val ticket_symbol = parent?.getItemAtPosition(position) as String
                ticket_symbol.let{
                    val intent =
                        Intent(Intent(this@SearchActivity, TickerExploreActivity::class.java))
                    intent.putExtra("name", ticket_symbol.split(" : ")[0])
                    intent.putExtra("symbol", ticket_symbol.split(" : ")[1])
                    startActivity(intent)
                }
            }
    }


    private fun initSearchLogic() {

        val searchBar: SearchView = findViewById(R.id.searchBar)
        searchBar.setIconifiedByDefault(false)
        searchBar.onActionViewExpanded()

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val requestQueue = Volley.newRequestQueue(this@SearchActivity)
                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.GET, QueryAPI()
                        .ProbTicker(newText), null,
                    Response.Listener { response->
                        // populate search array
                        searchAdaptor = ArrayAdapter(
                            this@SearchActivity,
                            android.R.layout.simple_list_item_1, QueryAPI()
                                .ParseProbResponse(response))
                        result_listview.adapter = searchAdaptor
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



