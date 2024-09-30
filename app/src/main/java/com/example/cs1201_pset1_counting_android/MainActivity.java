package com.example.cs1201_pset1_counting_android;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String[] textFiles = {"textOne.txt", "textTwo.txt", "Animal+Farm.txt", "Nineteen+eighty-four.txt"};
    TextView displayText;
    TextView information;
    Spinner dropdown;
    Button topWord;
    Button topWords;
    String filename, contents;
    boolean singleWord = true;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // https://developer.android.com/develop/ui/views/components/spinner#java
        displayText = findViewById(R.id.displayText);
        information = findViewById(R.id.information);
        dropdown = findViewById(R.id.dropdownSelection);
        topWord = findViewById(R.id.mostCommonWord);
        topWords = findViewById(R.id.topCommonWords);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.filesArray,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                try {
                    filename = textFiles[pos];
                    contents = new String(getAssets().open(textFiles[pos]).readAllBytes());
                    countUnplugged();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        topWord.setOnClickListener(view -> {
            singleWord = true;
            try {
                countUnplugged();
            } catch (IOException e) {
                System.out.println(e);
            }
        });

        topWords.setOnClickListener(view -> {
            singleWord = false;
            try {
                countUnplugged();
            } catch (IOException e) {
                System.out.println(e);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void countUnplugged() throws IOException {
        // Read common words and split by ', ' (to get each word without whitespace or punctuation)
        String[] words = new String(getAssets().open("commonWords.txt").readAllBytes())
                .split(", ");

        // Store common words in ArrayList (for contains() method convenience)
        // and transform all words to lowercase.
        ArrayList<String> commonWords = new ArrayList<>(List.of(words));
        commonWords.replaceAll(String::toLowerCase);

        // Read text file and replace all new lines, punctuation, and repeated spaces with a single space.
        String[] file = contents
                .replaceAll("[\r\n”“?!,.\"]", " ")
                .replaceAll(" ['] ", " ")
                .replaceAll("([ ]+)", " ")
                .split(" ");

        // Store an ArrayList of Word objects that represents distinct words and their number of occurrences
        ArrayList<Word> wordFrequencies = new ArrayList<>();

        // Loop through words from file and add to distinct words occurrence array
        for (String word : file) {
            // If the case-insensitive word is in the list of common words, skip it.
            if (commonWords.contains(word.toLowerCase())) continue;

            // If our list of case-insensitive distinct words contains the word, then increment the occurrence count.
            if (wordFrequencies.contains(new Word(word.toLowerCase())))
                wordFrequencies.get(wordFrequencies.indexOf(new Word(word.toLowerCase()))).count++;
            else // Otherwise add the lower-case representation of the word to our list
                wordFrequencies.add(new Word(word.toLowerCase()));
        }

        // Sort ArrayList of Word objects, using compareTo function from the Word class
        Collections.sort(wordFrequencies);

        if (singleWord) {
            displayText.setText("Most common word in “" + filename + "” is ");
            information.setText("“" + wordFrequencies.get(0).word + "” with " + wordFrequencies.get(0).count + " occurrences.");
        } else {
            displayText.setText("Top 5 common words in “" + filename + "” are ");
            information.setText("");
            for (int i = 0; i < 5; i++) {
                information.append((i + 1) + ". “" + wordFrequencies.get(i).word + "” with " + wordFrequencies.get(i).count + " occurrences.\n");
            }
        }
    }
}