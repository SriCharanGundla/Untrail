package app.untrail;
import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.widget.Toast;

public class ShareActivity extends UiActivity {
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  if(state!=null) {finish();return;}
  Intent incoming=getIntent();
  boolean process=Intent.ACTION_PROCESS_TEXT.equals(incoming.getAction());
  CharSequence raw=incoming.getCharSequenceExtra(process?Intent.EXTRA_PROCESS_TEXT:Intent.EXTRA_TEXT);
  if(raw==null || raw.length()==0) {Toast.makeText(this,"No link text was shared",Toast.LENGTH_LONG).show();finish();return;}
  if(raw.length()>100000) {Toast.makeText(this,"Text is too long to clean",Toast.LENGTH_LONG).show();finish();return;}
  String cleaned=process?Cleaner.clean(raw.toString()).text:raw.toString();
  if(process) {
   if(!incoming.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY,true)) {
    setResult(RESULT_OK,new Intent().putExtra(Intent.EXTRA_PROCESS_TEXT,cleaned)); finish();
   } else {startActivity(new Intent(this,MainActivity.class).putExtra(Intent.EXTRA_TEXT,cleaned));finish();}
   return;
  }
  boolean whatsapp=incoming.getComponent()!=null && incoming.getComponent().getClassName().endsWith("WhatsAppShare");
  share(this,cleaned,whatsapp); finish();
 }
 static void share(Activity activity,String text,boolean whatsapp) {
  String links=Cleaner.cleanForShare(text).text;
  if(links.isEmpty()){Toast.makeText(activity,"No web link found to share",Toast.LENGTH_LONG).show();return;}
  Intent send=new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,links);
  if(whatsapp) {
   for(String pkg:new String[]{"com.whatsapp","com.whatsapp.w4b"}) {
    try {activity.startActivity(new Intent(send).setPackage(pkg));return;} catch(ActivityNotFoundException ignored) {}
   }
   Toast.makeText(activity,"WhatsApp unavailable. Choose another app.",Toast.LENGTH_LONG).show();
  }
  Intent chooser=Intent.createChooser(send,"Share clean text");
  chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS,new ComponentName[]{new ComponentName(activity,ShareActivity.class),new ComponentName(activity,"app.untrail.WhatsAppShare")});
  try {activity.startActivity(chooser);} catch(ActivityNotFoundException e) {Toast.makeText(activity,"No sharing app available",Toast.LENGTH_LONG).show();}
 }
}
