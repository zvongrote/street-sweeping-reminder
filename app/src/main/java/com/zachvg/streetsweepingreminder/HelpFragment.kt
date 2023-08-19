package com.zachvg.streetsweepingreminder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import com.zachvg.streetsweepingreminder.databinding.FragmentHelpBinding

/*
Navigates the user to the User Guide, Terms of Service, and open source licenses.
The open source licenses are handled by the Google OSS license plugin:
https://developers.google.com/android/guides/opensource
 */

class HelpFragment : Fragment() {

    private lateinit var binding: FragmentHelpBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentHelpBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonUserGuide.setOnClickListener {
            // Open the help web page on my site. Show an error message if for some reason there is no
            // activity that can handle displaying a web page.
                val viewHelpIntent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(HELP_URL))
                if (viewHelpIntent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(viewHelpIntent)
                } else {
                    // This should never happen since every version of android should have some kind of web browser,
                    // but it's included just in case.
                    Snackbar.make(binding.root, R.string.help_error_message, Snackbar.LENGTH_SHORT).show()
                }
        }

        binding.buttonTermsOfService.setOnClickListener {
            // Open the terms of service web page on my site. Show an error message if for some reason there is no
            // activity that can handle displaying a web page.
            val viewTermsOfServiceIntent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(TERMS_OF_SERVICE_URL))
            if (viewTermsOfServiceIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(viewTermsOfServiceIntent)
            } else {
                // This should never happen since every version of android should have some kind of web browser,
                // but it's included just in case.
                Snackbar.make(binding.root, R.string.help_error_message, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.buttonLicenses.setOnClickListener {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.open_source_licenses))
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
        }

    }
}