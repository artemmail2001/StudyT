package com.example.artik.studyt;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MarkerDialog extends DialogFragment implements View.OnClickListener {
    private static final String DATE = "date";
    private static final String Title = "title";
    private static final String Thumb = "thumb";
    private static final String Score = "score";
    private static final String Number = "number";
    private static final String KEY = "key";
    public String qwerty;
    private MarkerDialog mMarkerDialog;
    public static final String EXTRA_DATE = "LOL";
    public static MarkerDialog newInstance(String thumb, String title, String date, String score, String number, String key) {
        Bundle args = new Bundle();
        args.putSerializable(DATE, date);
        args.putSerializable(Title, title);
        args.putSerializable(Thumb, thumb);
        args.putSerializable(Score, score);
        args.putSerializable(Number, number);
        args.putSerializable(KEY, key);
        MarkerDialog d = new MarkerDialog();
        d.setArguments(args);
        return d;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String date = (String)getArguments().getSerializable(DATE);
        String title = (String)getArguments().getSerializable(Title);
        String thumb = (String)getArguments().getSerializable(Thumb);
        String score = (String)getArguments().getSerializable(Score);
        String number = (String)getArguments().getSerializable(Number);
        String key = (String)getArguments().getSerializable(KEY);
        qwerty = key;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment, null);
        TextView mTitle = (TextView)view.findViewById(R.id.title_news);
        TextView mDate = (TextView)view.findViewById(R.id.date_news);
        TextView mScore = (TextView)view.findViewById(R.id.score_news);
        TextView mNumber = (TextView)view.findViewById(R.id.number_people_news);
        CircleImageView mImageCircle = (CircleImageView)view.findViewById(R.id.circle_news);
        mNumber.setText("осталось " + number + " мест(-а)");
        if (!thumb.equals("null")) {
            Picasso.get().load(thumb).into(mImageCircle);
        }
        mTitle.setText(title);
        mScore.setText(score + " points");
        mDate.setText(date);
        view.setOnClickListener(this);
        builder.setView(view);

        return builder.create();
    }

    @Override
    public void onClick(View v) {
        if (!qwerty.equals(null)) {
            Intent profileIntent = new Intent(getActivity(), NewsActivity.class);
            profileIntent.putExtra("key", qwerty);
            startActivity(profileIntent);
        }
    }
}
