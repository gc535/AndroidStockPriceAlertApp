package stock.price.alert.application.ui.stock

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.fragment_set_alert.*
import stock.price.alert.application.Data.WatchListDBHandler
import stock.price.alert.application.R

class SetAlertDialogFragment : DialogFragment() {
    private lateinit var watchlistDBHandler : WatchListDBHandler
    private lateinit var symbol : String
    private lateinit var name : String
    private var ubPrice : Float? = null
    private var lbPrice : Float? = null
    private val UNINIT_FLOAT = -1.0f
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return inflater.inflate(R.layout.fragment_set_alert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get ticker info from bundle
        watchlistDBHandler = WatchListDBHandler(requireContext())
        symbol = arguments?.getString("symbol")!!
        name = arguments?.getString("name")!!
        ubPrice = watchlistDBHandler.GetUpperBound(symbol)
        lbPrice = watchlistDBHandler.GetLowerBound(symbol)

        Log.d("POP", "get lb: ${lbPrice.toString()}, ub: ${ubPrice.toString()}")


        ubPrice?.let{
            editUpperBoundText.setText(ubPrice.toString())
        }
        lbPrice?.let{
            editLowerBoundText.setText(lbPrice.toString())
        }

        initEditTextLogic()
        initSubmitButton()
    }

    // todo: should accept callback for updating the view of caller fragment
    private fun initSubmitButton() {
        setButton.setOnClickListener (object : View.OnClickListener {
            override fun onClick(view: View) {
                val lb_val = if (!editLowerBoundText.text.toString().isEmpty())
                    editLowerBoundText.text.toString().toFloat() else null
                if (lb_val != lbPrice) {
                    watchlistDBHandler.PutLowerBound(symbol, name, lb_val)
                }

                val ub_val = if (!editUpperBoundText.text.toString().isEmpty())
                    editUpperBoundText.text.toString().toFloat() else null
                if (ub_val != ubPrice) {
                    watchlistDBHandler.PutUpperBound(symbol, name, ub_val)
                }

                // logging only
                val lb_string = if (lb_val!=null) lb_val.toString() else ""
                val ub_string = if (ub_val!=null) ub_val.toString() else ""
                Log.d("POP", "user input lb: $lb_string, ub: $ub_string")
                Log.d("POP", "probe db entry: $symbol: "+watchlistDBHandler.GetVal(symbol))
                dismiss()
            }
        })
    }


    private fun initEditTextLogic() {
        editUpperBoundText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(str: Editable?) {
            }

            override fun beforeTextChanged(str: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(str: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }




}