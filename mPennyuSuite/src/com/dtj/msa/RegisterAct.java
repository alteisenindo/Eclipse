package com.dtj.msa;

import com.dtj.email.MailConstants;
import com.dtj.email.TinyDB;

import r1.util.INIFile;
import r1.util.R1Util;
import r1.util.iCCConstants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class RegisterAct extends Activity 
{
	boolean m_bPasswordMask = true;
	
	@Override
  protected void onCreate(Bundle savedInstanceState) 
  {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.registeract);
	  
	  EditText tbWorkstation = (EditText)findViewById(R.id.tbWorkstation);
		EditText tbUsername = (EditText)findViewById(R.id.tbUsername);
		EditText tbEmailAddress = (EditText)findViewById(R.id.tbEmailAddress);
		EditText tbEmailPassword = (EditText)findViewById(R.id.tbEmailPassword);
		
		/*tbWorkstation.setText("175");
		tbUsername.setText("Iwan");
		tbEmailAddress.setText("test@gmail.com");
		tbEmailPassword.setText("123");*/
		
		// DEBUG ONLY
		//tbWorkstation.setText("GI04");
		/*tbWorkstation.setText("TESTPC-02");
		tbUsername.setText("iwan");
		tbEmailAddress.setText("test-01@pennyugroup.com");
		tbEmailPassword.setText("pennyutest01");*/
		
		/*tbWorkstation.setText("DEV-W03");
		tbUsername.setText("testAR");
		tbEmailAddress.setText("test-02@pennyugroup.com");
		tbEmailPassword.setText("pennyutest02");*/
		
		/*tbWorkstation.setText("DEV-PM02");
		tbUsername.setText("puma2");
		tbEmailAddress.setText("test-02@pennyugroup.com");
		tbEmailPassword.setText("pennyutest02");*/
		
		TinyDB db = new TinyDB(getApplicationContext());
  	db.putInt("DisplayingRegisterAct", 1);
		
		((ImageView) findViewById(R.id.imgTogglePwd)).setOnClickListener(new View.OnClickListener() 
    {
			public void onClick(View v) 
    	{
				try
				{
					EditText tbEmailPassword = (EditText)findViewById(R.id.tbEmailPassword);
					EditText tbEmailPasswordShow = (EditText)findViewById(R.id.tbEmailPasswordShow);
					if (m_bPasswordMask)
					{						
						m_bPasswordMask = false;
						
						tbEmailPasswordShow.setText(tbEmailPassword.getText().toString());
						tbEmailPassword.setVisibility(View.INVISIBLE);
						tbEmailPasswordShow.setVisibility(View.VISIBLE);
						
						tbEmailPasswordShow.requestFocus();
						tbEmailPasswordShow.setSelection(tbEmailPasswordShow.getText().length());
						
						//tbEmailPassword.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
						//tbEmailPassword.setSelection(tbEmailPassword.getText().length());
						
						ImageView im = (ImageView) findViewById(R.id.imgTogglePwd);
						int resImgId = RegisterAct.this.getResources().getIdentifier("com.dtj.msa:drawable/eye_open", null, null);
						im.setImageResource(resImgId);
					}
					else
					{
						m_bPasswordMask = true;
						
						tbEmailPassword.setText(tbEmailPasswordShow.getText().toString());
						tbEmailPassword.setVisibility(View.VISIBLE);						
						tbEmailPasswordShow.setVisibility(View.INVISIBLE);
						
						tbEmailPassword.requestFocus();
						tbEmailPassword.setSelection(tbEmailPassword.getText().length());
						
						//tbEmailPassword.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						//tbEmailPassword.setSelection(tbEmailPassword.getText().length());
						
						ImageView im = (ImageView) findViewById(R.id.imgTogglePwd);
						int resImgId = RegisterAct.this.getResources().getIdentifier("com.dtj.msa:drawable/eye_closed", null, null);
						im.setImageResource(resImgId);						
					}
				}
				catch (Exception ex)
				{}
    	}
    });
	  
	  ((Button) findViewById(R.id.btnRegister)).setOnClickListener(new View.OnClickListener() 
    {			
    	public void onClick(View v) 
    	{	      		      	   
    		EditText tbEmailPassword = (EditText)findViewById(R.id.tbEmailPassword);
    		EditText tbEmailPasswordShow = (EditText)findViewById(R.id.tbEmailPasswordShow);
    		if (!m_bPasswordMask)
    			tbEmailPassword.setText(tbEmailPasswordShow.getText().toString());
    		
    		EditText tbWorkstation = (EditText)findViewById(R.id.tbWorkstation);
    		EditText tbUsername = (EditText)findViewById(R.id.tbUsername);
    		EditText tbEmailAddress = (EditText)findViewById(R.id.tbEmailAddress);    		
    		
    		String sTemp = tbWorkstation.getText().toString().trim();
    		String sWorkstation = "MOB-" + tbWorkstation.getText().toString().trim();
    		String sUsername = tbUsername.getText().toString().trim();
    		String sEmailAddress = tbEmailAddress.getText().toString().trim();
    		String sEmailPassword = tbEmailPassword.getText().toString().trim();
    		
    		if (sTemp.length() <= 0)
    		{
    			Toast.makeText(RegisterAct.this, getText(R.string.errWorkstation1), Toast.LENGTH_SHORT).show();
    			return;
    		}
    		
    		if (sUsername.length() <= 0)
    		{
    			Toast.makeText(RegisterAct.this, getText(R.string.errUsername1), Toast.LENGTH_SHORT).show();
    			return;
    		}
    		
    		if (sEmailAddress.length() <= 4 || sEmailAddress.indexOf("@") < 0)
    		{
    			Toast.makeText(RegisterAct.this, getText(R.string.errEmailAddress1), Toast.LENGTH_SHORT).show();
    			return;
    		}
    		
    		if (sEmailPassword.length() <= 0)
    		{
    			Toast.makeText(RegisterAct.this, getText(R.string.errEmailPassword1), Toast.LENGTH_SHORT).show();
    			return;
    		}
    		
    		// save settings
    		String sINIPath = getFilesDir().getPath() + "/msa/Ini/Settings.ini";
      	INIFile settings = R1Util.ReadEncryptedSettings(getApplicationContext(), sINIPath);      	
      	settings.setStringProperty("MSASettings", "AppWId", sWorkstation , null);
      	settings.setStringProperty("MSASettings", "AppWSName", sWorkstation , null);
      	settings.setStringProperty("MSASettings", "AppUserName", sUsername , null);
      	settings.setStringProperty("MSASettings", "MailXMPP", sEmailAddress , null);
      	settings.setStringProperty("MSASettings", "MailXMPPPwd", sEmailPassword , null);
      	R1Util.SaveEncryptedSettings(settings, sINIPath);
      	
      	// set timer
      	TinyDB db = new TinyDB(getApplicationContext());      	
    		db.putInt("RegisterWaitTimer", MailConstants.WAIT_COUNTER);
    		
    		Intent hnd = new Intent();
				hnd.setClass(RegisterAct.this, WaitActivity.class);
	    	hnd.addFlags(iCCConstants.FLAG_ACTIVITY_REORDER_TO_FRONT);	    	
	    	startActivity(hnd);
	    	
	    	RegisterAct.this.finish();
      }
    });
  }
	
	@Override
  public void onDestroy()
	{
  	super.onDestroy();

  	TinyDB db = new TinyDB(getApplicationContext());
  	db.putInt("DisplayingRegisterAct", 0);
	}
}
