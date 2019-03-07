package pl.noritoshi_scarlett.pathflytha.fragments_main;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.noritoshi_scarlett.pathflytha.R;


public class InfoFragment extends Fragment {

    public InfoFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        FloatingActionButton floatBtnSendEmail = view.findViewById(R.id.floatBtnSendEmail);
        floatBtnSendEmail.setBackgroundTintList(getResources().getColorStateList(R.color.s_fab_colors_teal));
        floatBtnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                        new String[] { getResources().getString(R.string.main_info_email) });
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        getResources().getString(R.string.main_info_subject));
                startActivity(Intent.createChooser(emailIntent,
                        getResources().getString(R.string.main_info_email_sending)));
            }
        });


        return view;
    }

}
