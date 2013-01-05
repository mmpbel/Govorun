/*
 *  1. timer
 *  2. statistics
 *  3. START and STOP button
 *  
 *  
 */
package by.misha.govorun;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnInitListener {
	

	//voice recognition and general variables
	//variable for checking Voice Recognition support on user device
	private static final int VR_REQUEST = 999;
	//ListView for displaying suggested words
	private ListView wordList;

	//TTS variables
	//variable for checking TTS engine data on user device
	private int MY_DATA_CHECK_CODE = 0;
	//Text To Speech instance
	private TextToSpeech myTTS;
	
	private int maxNumber = 10;
	private int number1;
	private int number2;
	
	//Log tag for output information
	private static final String TAG = "myLogs";

	//create the Activity
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        	
  		//get a reference to the button element listed in the XML layout
    	Button speakButton = (Button)findViewById(R.id.speak);
    	//gain reference to speak button
    	Button speechBtn = (Button) findViewById(R.id.listen_btn);
    	//gain reference to word list
    	wordList = (ListView) findViewById(R.id.word_list);
   		//listen for clicks
    	speakButton.setOnClickListener(this);

		//check for TTS data
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        
        //find out whether speech recognition is supported
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> intActivities = packManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (intActivities.size() != 0) {
            //speech recognition is supported - detect user button clicks
            speechBtn.setOnClickListener(this);
        }
        else
        {
            //speech recognition not supported, disable button and output message
            speechBtn.setEnabled(false);
            Toast.makeText(this, "Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }
        //detect user clicks of suggested words
        wordList.setOnItemClickListener(new OnItemClickListener() {
            //click listener for items within list
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //cast the view
                TextView wordView = (TextView)view;
                //retrieve the chosen word
                String wordChosen = (String) wordView.getText();
                //output for debugging
                Log.v(TAG, "chosen: "+wordChosen);
                //output Toast message
                Toast.makeText(MainActivity.this, "You said: "+wordChosen, Toast.LENGTH_SHORT).show();//**alter for your Activity name***
            }
        });
    }
	
	//respond to button clicks
	public void onClick(View v) {
/*
	    if (v.getId() == R.id.listen_btn) {
	        //listen for results
	        listenToSpeech();
	    }
	    else if (v.getId() == R.id.speak) {
			//get the text entered
	    	EditText enteredText = (EditText)findViewById(R.id.enter);
//	    	String words = enteredText.getText().toString();
//	    	speakWords(words);
	    	
	    	generateNewTask();
	    	enteredText.setText(new String(number1 + " + " + number2 + " = ?"));
	    }
*/
		// generate a new task
    	generateNewTask();
		//get the text entered
    	EditText enteredText = (EditText)findViewById(R.id.enter);
    	String task = new String(number1 + " + " + number2 + " = ?");
    	enteredText.setText(task);
        //listen for results
        listenToSpeech(task);
	}
	
	//speak the user text
	private void speakWords(String speech) {

		//speak straight away
    	myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    	myTTS.playSilence(200, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	//speak the user text
	private void generateNewTask() {
		
		Random r = new Random();
		number1 = r.nextInt(maxNumber);
		number2 = r.nextInt(maxNumber);
		speakWords(new String(number1 + " plus " + number2 + " equals?"));
	}
	
	// Instruct the app to listen for user speech input
	private void listenToSpeech(String task) {
	    //start the speech recognition intent passing required data
	    Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	    //indicate package
	    listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
	    //message to display while listening
	    listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, task);
	    //set speech model
	    listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	    //specify number of results to retrieve
	    listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
	    //specify language
//	    listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
	    listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
	    //start listening
	    startActivityForResult(listenIntent, VR_REQUEST);
	}
	
	//setup TTS
	public void onInit(int initStatus) {
	
		//check for successful instantiation
		if (initStatus == TextToSpeech.SUCCESS) {
			if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
			{
				myTTS.setLanguage(Locale.US);
			}
			else
			{
				myTTS.setLanguage(Locale.getDefault());
			}
		}
		else if (initStatus == TextToSpeech.ERROR) {
			Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
     * onActivityResults handles:
     *  - retrieving results of speech recognition listening
     *  - retrieving result of TTS data check
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check speech recognition result
        if (requestCode == VR_REQUEST && resultCode == RESULT_OK)
        {
            //store the returned word list as an ArrayList
            ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            for (String s : suggestedWords)
            {
                try  
                {  
              	  int result = Integer.parseInt(s);
              	  if (result == number1 + number2)
              	  {
              		  Toast.makeText(this, "Good job!", Toast.LENGTH_LONG).show();
              		  Log.d(TAG, result + "is a correct answer");
              	  }
              	  else
              	  {
              		  Log.d(TAG, result + "is an incorrect answer");
              	  }
              	  break;
                }  
                catch(NumberFormatException nfe)
                {  
                }  
            }
            //set the retrieved list to display in the ListView using an ArrayAdapter
//            wordList.setAdapter(new ArrayAdapter<String> (this, R.layout.word, suggestedWords));
        }
        //tts code here
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				//the user has the necessary data - create the TTS
				myTTS = new TextToSpeech(this, this);
			}
			else {
					//no data - install it now
				Intent installTTSIntent = new Intent();
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		}
        //call superclass method
        super.onActivityResult(requestCode, resultCode, data);
    }
}


