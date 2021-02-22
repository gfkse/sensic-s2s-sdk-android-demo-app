package com.gfk.s2s.ui.videolist.videoparams;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.gfk.s2s.demo.s2s.R;

import java.util.HashMap;

public class VideoParamsDialog extends DialogFragment {

    private VideoParamManager paramManager;
    static final int NEW_PARAM_REQUEST =0x1;
    public static final String PARAMS_EXTRA ="currentParams";
    static final String PARAM_KEY ="paramKey";
    static final String PARAM_VALUE ="paramValue";
    private VideoParamsAdapter paramsAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        paramManager = (VideoParamManager) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_simple_listview, container,false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(8,8,8,8);
        rootView.setLayoutParams(layoutParams);
        ListView listview = rootView.findViewById(R.id.simple_listview);
        TextView emptyView = rootView.findViewById(R.id.simple_listview_emptyview);
        listview.setEmptyView(emptyView);
        Button buttonOk = rootView.findViewById(R.id.buttonOK);
        Button buttonCancel = rootView.findViewById(R.id.buttonCancel);
        Button buttonNew = rootView.findViewById(R.id.buttonNew);
        buttonOk.setOnClickListener(view -> {
            paramManager.writeVideoParamsToPreferences();
            getDialog().dismiss();
        });
        buttonCancel.setOnClickListener(view -> getDialog().dismiss());
        buttonNew.setOnClickListener(view -> {
            SimpleEditDialog editDialog = new SimpleEditDialog();
            editDialog.setTargetFragment(VideoParamsDialog.this, NEW_PARAM_REQUEST);
            editDialog.show(getActivity().getSupportFragmentManager(), "simpleEdit");
        });
        paramsAdapter = new VideoParamsAdapter(getActivity(), paramManager.getVideoParams().entrySet(),paramManager);
        listview.setAdapter(paramsAdapter);

        if(paramManager.getVideoParams() == null || paramManager.getVideoParams().isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            listview.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            listview.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_Alert);
        }
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        dialog.setTitle("Content-Parameter");
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_PARAM_REQUEST) {
            String key = data.getStringExtra(PARAM_KEY);
            String value = data.getStringExtra(PARAM_VALUE);
            paramManager.getVideoParams().put(key, value);
            paramsAdapter.updateItems(paramManager.getVideoParams().entrySet());
        }
    }

    public interface VideoParamManager {
        HashMap<String,String> getVideoParams();

        void writeVideoParamsToPreferences();
    }
}
