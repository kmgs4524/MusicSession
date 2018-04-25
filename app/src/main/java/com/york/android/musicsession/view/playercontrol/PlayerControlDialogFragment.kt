package com.york.android.musicsession.view.playercontrol

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.york.android.musicsession.R;

/**
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can create this modal bottom sheet from your activity like this:
 * <pre>
 *    PlayerControlDialogFragment.newInstance(30).create(supportFragmentManager, "dialog")
 * </pre>
 *
 * You activity (or fragment) needs to implement [PlayerControlDialogFragment.Listener].
 */
class PlayerControlDialogFragment : BottomSheetDialogFragment() {

    private var mListener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_playercontrol_list_dialog, container, false)


//        view.layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
//        val params = view.layoutParams
//        params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
//        params.height = ConstraintLayout.layout
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog

        if (dialog != null) {
            // change background window for activity to no dimAmount(transparent background)
            val windowParams = dialog.window.attributes
            windowParams.dimAmount = 0.0f   // dimAmount is
            dialog.window.attributes = windowParams
            // change design_bottom_sheet to full height
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            Log.d("DialogFragment", "bottomSheet: ${bottomSheet}")
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = BottomSheetBehavior.from(bottomSheet)
            // let bottomSheet can not be hidden
            behavior.isHideable = false
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment
        if (parent != null) {
            mListener = parent as Listener
        } else {
            mListener = context as Listener
        }
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    interface Listener {
        fun onPlayerControlClicked(position: Int)
    }

    companion object {

        // TODO: Customize parameters
        fun newInstance(): PlayerControlDialogFragment =
                PlayerControlDialogFragment().apply {
                    arguments = Bundle().apply {
//                        putInt(ARG_ITEM_COUNT, itemCount)
                    }
                }

    }
}
