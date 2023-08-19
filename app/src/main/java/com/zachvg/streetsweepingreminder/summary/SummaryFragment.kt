package com.zachvg.streetsweepingreminder.summary

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.TimePicker
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zachvg.streetsweepingreminder.*
import com.zachvg.streetsweepingreminder.database.SweepingSchedule
import com.zachvg.streetsweepingreminder.databinding.FragmentMainBinding

/*
Shown an summary of all the schedules, and allows scheduling of the reminder and alarm.
 */
class SummaryFragment : Fragment(), RecyclerViewClickHandler, TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: FragmentMainBinding
    private val summaryViewModel: SummaryViewModel by viewModels()
    private var notificationTimeClicked = false
    private var alarmTimeClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout and set up the data binder
        binding = FragmentMainBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = summaryViewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        summaryViewModel.checkedNotificationRadioButton.value?.let { id ->
            val actualId = if (id == NOTIFICATION_RADIO_BUTTON_DAY_OF) {
                R.id.notification_radioButton_day_of
            } else {
                R.id.notification_radioButton_day_before
            }
            binding.radioGroupNotificationDay.check(actualId)
        }

        summaryViewModel.notificationSwitchIsChecked.value?.let { isChecked ->
            binding.notificationSwitch.isChecked = isChecked
        }

        // Set up on click handlers
        binding.addScheduleFab.setOnClickListener {
            it.findNavController().navigate(R.id.action_mainFragment_to_addScheduleFragment)
        }

        binding.notificationSwitch.setOnClickListener {
            it as SwitchCompat
            summaryViewModel.notificationSwitchChanged(it.isChecked)
        }

        binding.alarmSwitch.setOnClickListener {
            it as SwitchCompat
            summaryViewModel.alarmSwitchChanged(it.isChecked)
        }

        binding.notificationTimeValue.setOnClickListener {
            notificationTimeClicked = true
            summaryViewModel.notificationTime.value?.let { initialTime ->
                activity?.let { activity ->
                    TimePickerFragment(initialTime, this).show(activity.supportFragmentManager, "timePicker")
                }
            }
        }

        binding.alarmTimeValue.setOnClickListener {
            alarmTimeClicked = true
            summaryViewModel.alarmTime.value?.let { initialTime ->
                activity?.let { activity ->
                    TimePickerFragment(initialTime, this).show(activity.supportFragmentManager, "timePicker")
                }
            }
        }

        binding.radioGroupNotificationDay.setOnCheckedChangeListener { radioGroup, id ->
            val notificationRB = if (id == R.id.notification_radioButton_day_before) {
                NOTIFICATION_RADIO_BUTTON_DAY_BEFORE
            } else {
                NOTIFICATION_RADIO_BUTTON_DAY_OF
            }

            summaryViewModel.notificationRadioButtonChanged(notificationRB)
        }

        binding.radioGroupAlarmDay.setOnCheckedChangeListener { radioGroup, id ->
            val alarmRB = if (id == R.id.alarm_radioButton_day_before) {
                ALARM_RADIO_BUTTON_DAY_BEFORE
            } else {
                ALARM_RADIO_BUTTON_DAY_OF
            }

            summaryViewModel.alarmRadioButtonChanged(alarmRB)
        }

        // Set up the recycler view
        val scheduleAdapter = ScheduleAdapter(this)

        binding.schedulesRecyclerview.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = scheduleAdapter
        }

        // Set up observers
        summaryViewModel.schedules.observe(viewLifecycleOwner) { schedules ->
            schedules.let {
                // Update recycler view
                scheduleAdapter.submitList(schedules)
            }
        }

        summaryViewModel.alarmSwitchIsChecked.observe(viewLifecycleOwner) { isChecked ->
            binding.alarmSwitch.isChecked = isChecked
        }

        // Enable or disable the notification and alarm controls depending on if there are any schedules available
        summaryViewModel.hasSchedules.observe(viewLifecycleOwner) { enable ->
            with(binding) {
                notificationSwitch.isEnabled = enable
                notificationLabel.isEnabled = enable

                alarmSwitch.isEnabled = enable
                alarmLabel.isEnabled = enable
            }
        }

        summaryViewModel.notificationTimeString.observe(viewLifecycleOwner
        ) { binding.notificationTimeValue.text = it }

        summaryViewModel.showNotificationViews.observe(viewLifecycleOwner) {
            setNotificationViewsEnable(it)
            setNotificationViewsVisibility(it)
        }

        summaryViewModel.showAlarmViews.observe(viewLifecycleOwner) {
            setAlarmViewEnable(it)
            setAlarmViewsVisibility(it)
        }

        summaryViewModel.alarmTimeString.observe(viewLifecycleOwner
        ) { binding.alarmTimeValue.text = it }

        summaryViewModel.checkedAlarmRadioButton.observe(viewLifecycleOwner) {
            val selectedDay = if (it == ALARM_RADIO_BUTTON_DAY_BEFORE) {
                R.id.alarm_radioButton_day_before
            } else {
                R.id.alarm_radioButton_day_of
            }

            binding.radioGroupAlarmDay.check(selectedDay)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.summary_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.settings -> {
                findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
                true
            }
            R.id.help-> {
                findNavController().navigate(R.id.action_mainFragment_to_helpFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRecyclerDeleteClicked(schedule: SweepingSchedule) {
        summaryViewModel.delete(schedule)
    }

    override fun onRecyclerEditClicked(scheduleId: Int) {
        val action = SummaryFragmentDirections.actionMainFragmentToEditScheduleFragment(scheduleId)
        findNavController().navigate(action)
    }

    override fun onTimeSet(picker: TimePicker?, hour: Int, minute: Int) {
        if (notificationTimeClicked) {
            summaryViewModel.notificationTimeChanged(hour, minute)
            notificationTimeClicked = false
        } else if (alarmTimeClicked) {
            summaryViewModel.alarmTimeChanged(hour, minute)
            alarmTimeClicked = false
        }
    }

    private fun setNotificationViewsEnable(enabled: Boolean) {
        with(binding) {
            notificationWhenLabel.isEnabled = enabled
            notificationTimeLabel.isEnabled = enabled
            radioGroupNotificationDay.isEnabled = enabled
            notificationTimeValue.isEnabled = enabled
            notificationRadioButtonDayBefore.isEnabled = enabled
            notificationRadioButtonDayOf.isEnabled = enabled
            notificationDayDivider.isEnabled = enabled
            notificationTimeDivider.isEnabled = enabled
        }
    }

    private fun setNotificationViewsVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE

        with(binding) {
            notificationWhenLabel.visibility = visibility
            notificationTimeLabel.visibility = visibility
            radioGroupNotificationDay.visibility = visibility
            notificationTimeValue.visibility = visibility
            notificationRadioButtonDayBefore.visibility = visibility
            notificationRadioButtonDayOf.visibility = visibility
            notificationDayDivider.visibility = visibility
            notificationTimeDivider.visibility = visibility
        }
    }
    
    private fun setAlarmViewEnable(enabled: Boolean) {
        with(binding) {
            alarmWhenLabel.isEnabled = enabled
            alarmTimeLabel.isEnabled = enabled
            radioGroupAlarmDay.isEnabled = enabled
            alarmTimeValue.isEnabled = enabled
            alarmRadioButtonDayBefore.isEnabled = enabled
            alarmRadioButtonDayOf.isEnabled = enabled
            alarmDayDivider.isEnabled = enabled
            alarmTimeDivider.isEnabled = enabled
        }
    }
    
    private fun setAlarmViewsVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        
        with(binding) {
            alarmWhenLabel.visibility = visibility
            alarmTimeLabel.visibility = visibility
            radioGroupAlarmDay.visibility = visibility
            alarmTimeValue.visibility = visibility
            alarmRadioButtonDayBefore.visibility = visibility
            alarmRadioButtonDayOf.visibility = visibility
            alarmDayDivider.visibility = visibility
            alarmTimeDivider.visibility = visibility
        }
    }
}
