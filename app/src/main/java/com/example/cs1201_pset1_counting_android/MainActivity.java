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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Stores file names in order on the dropdown
    String[] textFiles = {"textOne.txt", "textTwo.txt", "Animal+Farm.txt", "Nineteen+eighty-four.txt"};

    // Global variables for components visible on the screen
    TextView displayText; // Displays large text overviewing what the information displayed is about
    TextView information; // Displays the most frequent word(s)
    Spinner dropdown;     // Allows user to select the text file to read from
    Button topWord;       // Button click leads to display of most common word
    Button topWords;      // Button click leads to display of top 5 common words
    String filename, contents; // Caches the filename and contents of the text file for access in method
    boolean singleWord = true; // Stores the last clicked button preference (single/top common word[s])

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU) // For using readAllBytes()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main); // Set view screen to one designed in activity_main.xml
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // https://developer.android.com/develop/ui/views/components/spinner#java

        // Bind variables representing components with actual components based off id
        // This step is a bit like JS's document.querySelector or document.getElementById
        displayText = findViewById(R.id.displayText);
        information = findViewById(R.id.information);
        dropdown = findViewById(R.id.dropdownSelection);
        topWord = findViewById(R.id.mostCommonWord);
        topWords = findViewById(R.id.topCommonWords);

        // Initialize our dropdown selection with string values stored in strings.xml
        // Create an array representing labels of filenames
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.filesArray,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

        // Add item selection listener
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                try {
                    // Get the filename from the 0-indexed position of selected item in the dropdown.
                    filename = textFiles[pos];

                    // Save the contents of that file from the corresponding file in the folder src/main/assets
                    contents = new String(getAssets().open(textFiles[pos]).readAllBytes());

                    // Update display
                    countUnplugged();
                } catch (IOException e) { // Error logging (appears in Logcat)
                    System.out.println(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing.
            }
        });

        // Add button event listeners
        topWord.setOnClickListener(view -> {
            singleWord = true;    // Set the preference to display the top word
            try {
                countUnplugged(); // Update display
            } catch (IOException e) {
                System.out.println(e);
            }
        });

        topWords.setOnClickListener(view -> {
            singleWord = false;   // Set the preference to display the top 5 words
            try {
                countUnplugged(); // Update display
            } catch (IOException e) {
                System.out.println(e);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void countUnplugged() throws IOException {
        // Adapted from CountingPlugged source code
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
        ArrayList<String> wordsList = new ArrayList<>(Arrays.asList(file));

        // Note this section is relatively inefficient, optimizations can be made
        // to make running this function on text files such as Orwell's 1984 faster.
        // Loop through words from file and add to distinct words occurrence array
        while (!wordsList.isEmpty()) {
            String word = wordsList.get(0);

            // If the case-insensitive word is in the list of common words, skip it.
            if (commonWords.contains(word.toLowerCase())) {
                wordsList.removeAll(Collections.singleton(word));
                continue;
            }

            wordFrequencies.add(new Word(word, Collections.frequency(wordsList, word)));
            wordsList.removeAll(Collections.singleton(word));
        }

        // Sort ArrayList of Word objects, using compareTo function from the Word class
        Collections.sort(wordFrequencies);

        // Update text display based on preference
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