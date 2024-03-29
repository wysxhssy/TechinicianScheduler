package com.example.mario.techinicianscheduler.Manager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mario.techinicianscheduler.R;

/**
 * A customized fragment class for side menu.
 * The code is from the libarary.
 */

public class MyFragment extends Fragment {
    private View view;
    private int res;

    public MyFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.myfragment, container, false);
        setImage(getRes());
        return view;
    }

    public void setImage(int res) {
        ImageView imageView = (ImageView) view.findViewById(R.id.loop_image);
        imageView.setImageResource(res);
    }


    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }
}
