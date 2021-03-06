package com.farsheel.mypos.view.payment.cash

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.library.baseAdapters.BR
//import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.farsheel.mypos.R
import com.farsheel.mypos.databinding.CashPaymentFragmentBinding
import org.koin.android.viewmodel.ext.android.viewModel

class CashPaymentFragment : Fragment() {

    companion object {
        fun newInstance() = CashPaymentFragment()
    }

    private lateinit var binding: CashPaymentFragmentBinding
    private val cashPaymentViewModel: CashPaymentViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CashPaymentFragmentBinding.bind(
            inflater.inflate(
                R.layout.cash_payment_fragment,
                container,
                false
            )
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewmodel = cashPaymentViewModel


        cashPaymentViewModel.amountToPay.observe(viewLifecycleOwner, Observer {
            cashPaymentViewModel.amountEntered.set(cashPaymentViewModel.amountToPay.value.toString())
        })


        cashPaymentViewModel.lesserAmountEntered.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let {
                val builder = AlertDialog.Builder(context)
                builder.setMessage(getString(R.string.entered_a_lesser_amount_message))
                builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    dialog.dismiss()
                    cashPaymentViewModel.amountEntered.set(cashPaymentViewModel.amountToPay.value.toString())
                }
                builder.setNeutralButton(getString(R.string.cancel), null)
                val dialog = builder.show()
                context?.let {
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                        .setBackgroundColor(ContextCompat.getColor(it, R.color.darkColor))
                }
            }
        })

        cashPaymentViewModel.navigateToCompleted.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { balance ->
                val action = cashPaymentViewModel.amountToPay.value?.let { it1 ->
                    CashPaymentFragmentDirections.actionCashPaymentFragmentToPaymentCompletedFragment(
                        cashPaymentViewModel.orderId,
                        it1.toFloat(), balance.toFloat()
                    )
                }
                if (action != null) {
                    view?.findNavController()
                        ?.navigate(action)
                }
            }
        })

        cashPaymentViewModel.errorMessage.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { message ->
                context?.let {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(it)
                    builder.setMessage(message)
                    builder.show()
                }
            }
        })
    }


}
