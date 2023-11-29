package com.example.stockwise;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button btnAnalyze, btnClear;
    EditText editTextInput;
    TextView txtSentimentMessage;
    public NewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsFragment newInstance(String param1, String param2) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View newsView = inflater.inflate(R.layout.fragment_news, container, false);
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(newsView.getContext()));
        }

        final Python py = Python.getInstance();

        btnAnalyze = newsView.findViewById(R.id.btnAnalyze);
        editTextInput = newsView.findViewById(R.id.editTextInput);
        txtSentimentMessage = newsView.findViewById(R.id.txtSentimentMessage);

        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt = editTextInput.getText().toString();
                PyObject pyo = py.getModule("sentiment_analysis");
                PyObject obj = pyo.callAttr("analyze_article", txt);
                int sentimentScore = obj.toInt();
                System.out.println(sentimentScore);
                String sentimentText = processSentimentScore(sentimentScore);
                txtSentimentMessage.setText(sentimentText);
                if (sentimentScore > 2) {
                    txtSentimentMessage.setTextColor(Color.parseColor("#008000"));
                } else if (sentimentScore < 2) {
                    txtSentimentMessage.setTextColor(Color.parseColor("#FF0000"));
                } else {
                    txtSentimentMessage.setTextColor(Color.parseColor("#000000"));
                }
            }
        });

        btnClear = newsView.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtSentimentMessage.setText("");
                editTextInput.setText("");
            }
        });

        return newsView;
    }

    public String processSentimentScore(int sentimentScore) {
        String sentimentText = "";
        String veryBadEmoji = new String(Character.toChars(0x1F61E));
        String badEmoji = new String(Character.toChars(0x1F62A));
        String neutralEmoji = new String(Character.toChars(0x1F610));
        String goodEmoji = new String(Character.toChars(0x1F642));
        String veryGoodEmoji = new String(Character.toChars(0x1F600));
        String failedEmoji = new String(Character.toChars(0x1F937));

        switch(sentimentScore) {
            case 0:
                sentimentText = veryBadEmoji + " Very Bad News " + veryBadEmoji;
                break;
            case 1:
                sentimentText = badEmoji + " Bad News " + badEmoji;
                break;
            case 2:
                sentimentText = neutralEmoji + " Neutral News " + neutralEmoji;
                break;
            case 3:
                sentimentText = goodEmoji + " Good News " + goodEmoji;
                break;
            case 4:
                sentimentText = veryGoodEmoji + " Very Good News " + veryGoodEmoji;
                break;
            default:
                sentimentText = failedEmoji + " Sentiment Analysis Failed " + failedEmoji;
        }

        return sentimentText;
    }
}