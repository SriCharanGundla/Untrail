package app.untrail;
import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;

public class MainActivity extends UiActivity {
 private EditText input; private TextView result, status; private Cleaner.Result cleaned;
 private String before="";
 private TextView removedDetail;
 private LinearLayout outputPanel;
 private Button whatsappButton, othersButton;
 private ImageButton clearButton, undoButton, copyButton;
 private boolean canUndo;
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  LinearLayout body=page();
  LinearLayout header=row(body);header.setGravity(Gravity.CENTER_VERTICAL);
  TextView title=new TextView(this);title.setText("Untrail");title.setTextColor(ink);title.setTextSize(38);title.setTypeface(Typeface.create(face,700,false));title.setLetterSpacing(-0.045f);title.setAccessibilityHeading(true);
  header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  compactIcon(header,"Settings",R.drawable.ic_settings,this::showSettings);
  space(body,30);
  label(body,"Link or message",14,muted);
  LinearLayout editor=new LinearLayout(this);editor.setOrientation(1);editor.setPadding(dp(8),dp(8),dp(8),dp(8));editor.setBackground(surface());body.addView(editor,new LinearLayout.LayoutParams(-1,-2));
  input=new EditText(this);input.setTextColor(ink);input.setHintTextColor(muted);input.setTextSize(17);input.setTypeface(face);input.setGravity(Gravity.TOP);input.setMinLines(4);input.setMaxLines(8);input.setHint("Paste a link here");input.setContentDescription("Link or message to clean");input.setPadding(dp(12),dp(12),dp(12),dp(12));input.setBackgroundColor(Color.TRANSPARENT);
  input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);input.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
  editor.addView(input,new LinearLayout.LayoutParams(-1,-2));
  input.setOnFocusChangeListener((v,focused)->{GradientDrawable background=surface();if(focused)background.setStroke(dp(1),accent);editor.setBackground(background);});
  LinearLayout tools=row(editor);tools.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
  tools.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
  compactIcon(tools,"Paste",R.drawable.ic_paste,()->{ClipboardManager c=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(c.hasPrimaryClip()&&c.getPrimaryClip().getItemCount()>0){remember();input.setText(c.getPrimaryClip().getItemAt(0).coerceToText(this));}else Toast.makeText(this,"Clipboard is empty",Toast.LENGTH_SHORT).show();});
  clearButton=compactIcon(tools,"Clear",R.drawable.ic_clear,()->{remember();input.setText("");});
  undoButton=compactIcon(tools,"Undo",R.drawable.ic_undo,()->{String restore=before;before=input.getText().toString();input.setText(restore);});
  space(body,20);
  outputPanel=new LinearLayout(this);outputPanel.setOrientation(1);body.addView(outputPanel,new LinearLayout.LayoutParams(-1,-2));
  LinearLayout outputHeader=row(outputPanel);outputHeader.setGravity(Gravity.CENTER_VERTICAL);
  status=new TextView(this);status.setTextColor(accent);status.setTypeface(Typeface.create(face,600,false));status.setTextSize(14);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);outputHeader.addView(status,new LinearLayout.LayoutParams(0,-2,1));
  copyButton=compactIcon(outputHeader,"Copy cleaned text",R.drawable.ic_copy,()->{if(hasText()){((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Untrail",cleaned.text));Toast.makeText(this,"Copied",Toast.LENGTH_SHORT).show();}});
  result=label(outputPanel,"",16,ink);result.setTextIsSelectable(true);result.setPadding(0,dp(6),0,dp(10));
  removedDetail=label(outputPanel,"",12,muted);space(outputPanel,16);
  whatsappButton=button(body,"WhatsApp",()->send(true));
  othersButton=button(body,"Others",()->send(false));
  input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){} public void onTextChanged(CharSequence s,int a,int b,int c){update();}public void afterTextChanged(Editable e){}});
  String initial=state==null?getIntent().getStringExtra(Intent.EXTRA_TEXT):state.getString("text");if(initial!=null)input.setText(initial);if(state!=null){before=state.getString("before","");canUndo=state.getBoolean("canUndo",false);}update();
 }
 private boolean hasText(){if(cleaned==null||cleaned.text.trim().isEmpty()){Toast.makeText(this,"Paste a link or message first",Toast.LENGTH_SHORT).show();return false;}return true;}
 private void send(boolean whatsapp){if(hasText())ShareActivity.share(this,cleaned.text,whatsapp);}
 private void remember(){before=input.getText().toString();canUndo=true;}
 private void update(){
  String raw=input.getText().toString();cleaned=Cleaner.clean(raw);boolean available=!raw.trim().isEmpty()&&raw.length()<=100000;
  outputPanel.setVisibility(raw.isEmpty()?View.GONE:View.VISIBLE);
  result.setText(cleaned.text);
  status.setText(raw.length()>100000?"Text too long · unchanged":cleaned.removed.isEmpty()?(cleaned.changes.isEmpty()?"No known trackers found":"Link simplified"):cleaned.removed.size()+" tracking parameter"+(cleaned.removed.size()==1?"":"s")+" removed");
  String detail=cleaned.removed.isEmpty()?"":"Removed: "+String.join(", ",new java.util.LinkedHashSet<>(cleaned.removed));
  if(!cleaned.changes.isEmpty())detail+=(detail.isEmpty()?"":"\n")+String.join(" · ",new java.util.LinkedHashSet<>(cleaned.changes));
  removedDetail.setText(detail);removedDetail.setVisibility(detail.isEmpty()?View.GONE:View.VISIBLE);
  setAvailable(whatsappButton,available);setAvailable(othersButton,available);setAvailable(copyButton,available);setAvailable(clearButton,!raw.isEmpty());setAvailable(undoButton,canUndo);
 }
 private void setAvailable(View view,boolean enabled){view.setEnabled(enabled);view.setAlpha(enabled?1f:0.38f);}
 private void showSettings(){
  ((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);
  startActivity(new Intent(this,SettingsActivity.class));
 }
 @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("text",input.getText().toString());out.putString("before",before);out.putBoolean("canUndo",canUndo);}
}
