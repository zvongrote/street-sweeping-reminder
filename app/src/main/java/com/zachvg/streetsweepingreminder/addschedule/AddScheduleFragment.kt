package com.zachvg.streetsweepingreminder.addschedule

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.zachvg.streetsweepingreminder.NO_WEEKS_SELECTED_DIALOG_TAG
import com.zachvg.streetsweepingreminder.R
import com.zachvg.streetsweepingreminder.WeekRequiredDialogFragment
import com.zachvg.streetsweepingreminder.database.SweepingSchedule
import kotlinx.android.synthetic.main.fragment_add_schedule.*
import kotlinx.android.synthetic.main.fragment_add_schedule.view.*
import org.threeten.bp.DayOfWeek

/*
Shown when the user clicks the add schedule button from the summary fragment.
The edit schedule fragment is almost an exact replica of this.
They should have been made into one, but I didn't think about it at the time.
 */
class AddScheduleFragment : Fragment() {
    private lateinit var addScheduleViewModel: AddScheduleViewModel
    private lateinit var radioButtons: List<RadioButton>
    private lateinit var checkboxes: List<CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Needed to inflate the toolbar menu
        setHasOptionsMenu(true)

        // Get the fragment view model
        addScheduleViewModel = ViewModelProvider(this)[AddScheduleViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_add_schedule, container, false)

        setupRadioButton(rootView)

        setupCheckBoxes(rootView)

        rootView.button_every_week.setOnClickListener {
            addScheduleViewModel.selectEveryWeek()
            checkboxes.forEach {
                it.isChecked = true
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hides the keyboard when the schedule name text view loses focus
        text_schedule_name.setOnFocusChangeListener { textView, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(textView.windowToken, 0)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_schedule_actions, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.accept -> {
                // Make sure that at least one week is selected
                for (checkbox in checkboxes) {
                    if(checkbox.isChecked) {
                        addSchedule()
                        return true
                    }
                }

                // If it gets to this point then there are no weeks selected, so show the error dialog.
                activity?.supportFragmentManager?.let {
                val errorDialogFragment = WeekRequiredDialogFragment()
                    errorDialogFragment.show(it, NO_WEEKS_SELECTED_DIALOG_TAG)
                }

                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    private fun setupRadioButton(rootView: View) {
        // Create the list of radio buttons
        radioButtons = listOf(rootView.rb_monday,
                rootView.rb_tuesday,
                rootView.rb_wednesday,
                rootView.rb_thursday,
                rootView.rb_friday)

        // Set the on click listener for each button so that only one can be selected at a time,
        // then sets the day variable in the view model.
        radioButtons.forEach { listRadioButton ->
            listRadioButton.setOnClickListener { clickedRadioButton ->

                // Hide the soft keyboard if it shown
                val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(clickedRadioButton.windowToken, 0)

                radioButtons.forEach {
                    it.isChecked = clickedRadioButton.id == it.id
                    if (it.isChecked) {
                        addScheduleViewModel.dayOfWeek = when (it.id) {
                            R.id.rb_monday -> DayOfWeek.MONDAY
                            R.id.rb_tuesday -> DayOfWeek.TUESDAY
                            R.id.rb_wednesday -> DayOfWeek.WEDNESDAY
                            R.id.rb_thursday -> DayOfWeek.THURSDAY
                            R.id.rb_friday -> DayOfWeek.FRIDAY
                            else -> DayOfWeek.MONDAY
                        }
                    }
                }
            }
        }

        // Set the initial day radio button.
        when (addScheduleViewModel.dayOfWeek) {
            DayOfWeek.MONDAY -> rootView.rb_monday.isChecked = true
            DayOfWeek.TUESDAY -> rootView.rb_tuesday.isChecked = true
            DayOfWeek.WEDNESDAY -> rootView.rb_wednesday.isChecked = true
            DayOfWeek.THURSDAY -> rootView.rb_thursday.isChecked = true
            DayOfWeek.FRIDAY -> rootView.rb_friday.isChecked = true
            else -> rootView.rb_monday.isChecked = true
        }
    }

    private fun setupCheckBoxes(rootView: View) {
        // Create the list of checkboxes
        checkboxes = listOf(rootView.checkBox1,
                rootView.checkBox2,
                rootView.checkBox3,
                rootView.checkBox4,
                rootView.checkBox5)

        // Set the on click listener for each checkbox
        checkboxes.forEach { checkbox ->
            checkbox.setOnClickListener {
                it as CheckBox
                when (it.id) {
                    R.id.checkBox1 -> addScheduleViewModel.weeksOfMonth[0] = !it.isChecked
                    R.id.checkBox2 -> addScheduleViewModel.weeksOfMonth[1] = !it.isChecked
                    R.id.checkBox3 -> addScheduleViewModel.weeksOfMonth[2] = !it.isChecked
                    R.id.checkBox4 -> addScheduleViewModel.weeksOfMonth[3] = !it.isChecked
                    R.id.checkBox5 -> addScheduleViewModel.weeksOfMonth[4] = !it.isChecked
                }
            }
        }

        // Set the initial check boxes
        checkboxes.forEachIndexed { index, checkbox -> checkbox.isChecked = addScheduleViewModel.weeksOfMonth[index] }

    }

    // Add schedule to the view model
    private fun addSchedule() {
        // Create a list of ints based on which checkboxes are selected
        val weeksSelected = mutableListOf<Int>()
        checkboxes.forEachIndexed { index, checkbox ->
            if (checkbox.isChecked) {
                weeksSelected.add(index + 1)
            }
        }

        // Figure out if the user provided a name for the schedule.
        // If so, then remove any unnecessary whitespace.
        val scheduleNameFromUser = text_schedule_name.text.toString()
        val scheduleName =
                if (scheduleNameFromUser.isBlank()) {
                    "Unnamed Schedule"
                } else {
                    scheduleNameFromUser.trimStart().trimEnd()
                }

        // Create a new schedule with selected data
        val newSchedule = SweepingSchedule(scheduleName, addScheduleViewModel.dayOfWeek, weeksSelected)

        // Add to the list of schedules
        addScheduleViewModel.insert(newSchedule)

        // Navigate back to the main fragment
        findNavController().navigateUp()

    }
}