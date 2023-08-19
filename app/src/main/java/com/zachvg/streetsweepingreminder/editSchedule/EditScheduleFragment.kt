package com.zachvg.streetsweepingreminder.editSchedule

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.zachvg.streetsweepingreminder.R
import com.zachvg.streetsweepingreminder.WeekRequiredDialogFragment
import com.zachvg.streetsweepingreminder.database.SweepingSchedule
import kotlinx.android.synthetic.main.fragment_edit_schedule.*
import org.threeten.bp.DayOfWeek

/*
Allows a schedule to be edited.
This is almost identical to the Add Schedule fragment. They should have been combined
into one, but I wasn't thinking about it at the time.
 */
class EditScheduleFragment : Fragment() {

    private val args: EditScheduleFragmentArgs by navArgs()

    private lateinit var editScheduleViewModel: EditScheduleViewModel
    private lateinit var editScheduleViewModelFactory: EditScheduleViewModelFactory
    private lateinit var radioButtons: List<RadioButton>
    private lateinit var checkboxes: List<CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Needed to inflate the toolbar menu
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_edit_schedule, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.application?.run {
            editScheduleViewModelFactory = EditScheduleViewModelFactory(this, args.scheduleId)
        }

        editScheduleViewModel = ViewModelProvider(this, editScheduleViewModelFactory)[EditScheduleViewModel::class.java]

        editScheduleViewModel.schedule.observe(viewLifecycleOwner) { schedule ->
            text_schedule_name.setText(schedule.name)
            setupRadioButton(schedule)
            setupCheckBoxes(schedule)
        }

        // Hides the keyboard when the schedule name text view loses focus
        text_schedule_name.setOnFocusChangeListener { textView, hasFocus ->
            if (!hasFocus) {
                val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(textView.windowToken, 0)
            }
        }

        button_every_week.setOnClickListener {
            editScheduleViewModel.selectEveryWeek()
            checkboxes.forEach {
                it.isChecked = true
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
                    if (checkbox.isChecked) {
                        updateSchedule()
                        return true
                    }
                }

                // If it gets to this point then there are no weeks selected, so show the error dialog.
                activity?.supportFragmentManager?.let {
                    val errorDialogFragment = WeekRequiredDialogFragment()
                    errorDialogFragment.show(it, "weeks_error")
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateSchedule() {
        editScheduleViewModel.schedule.value?.let { oldSchedule ->
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

            // Update to all the new values
            // The day is already changed in the radio button click listener
            oldSchedule.apply {
                name = scheduleName
                dayNumbersOfMonth.clear()
                dayNumbersOfMonth.addAll(weeksSelected)
            }

            editScheduleViewModel.updateSchedule(oldSchedule)
        }

        findNavController().navigateUp()
    }


    private fun setupCheckBoxes(schedule: SweepingSchedule) {
        // Create the list of checkboxes
        checkboxes = listOf(checkBox1, checkBox2, checkBox3, checkBox4, checkBox5)

        editScheduleViewModel.setWeeks(schedule.dayNumbersOfMonth)

        // Set the on click listener for each checkbox
        checkboxes.forEach { checkbox ->
            checkbox.setOnClickListener {
                it as CheckBox
                when (it.id) {
                    R.id.checkBox1 -> editScheduleViewModel.weeksOfMonth[0] = !it.isChecked
                    R.id.checkBox2 -> editScheduleViewModel.weeksOfMonth[1] = !it.isChecked
                    R.id.checkBox3 -> editScheduleViewModel.weeksOfMonth[2] = !it.isChecked
                    R.id.checkBox4 -> editScheduleViewModel.weeksOfMonth[3] = !it.isChecked
                    R.id.checkBox5 -> editScheduleViewModel.weeksOfMonth[4] = !it.isChecked
                }
            }
        }

        // Set the initial check boxes
        checkboxes.forEachIndexed { index, checkbox -> checkbox.isChecked = editScheduleViewModel.weeksOfMonth[index] }

    }

    private fun setupRadioButton(schedule: SweepingSchedule) {
        // Create the list of radio buttons
        radioButtons = listOf(rb_monday, rb_tuesday, rb_wednesday, rb_thursday, rb_friday)

        // Set the day
        when (schedule.day) {
            DayOfWeek.MONDAY -> rb_monday.isChecked = true
            DayOfWeek.TUESDAY -> rb_tuesday.isChecked = true
            DayOfWeek.WEDNESDAY -> rb_wednesday.isChecked = true
            DayOfWeek.THURSDAY -> rb_thursday.isChecked = true
            else -> rb_friday.isChecked = true
        }

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
                        schedule.day = when (it.id) {
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

    }
}

private class EditScheduleViewModelFactory(private val application: Application, private val scheduleId: Int) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditScheduleViewModel::class.java)) {
            return EditScheduleViewModel(application, scheduleId) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
