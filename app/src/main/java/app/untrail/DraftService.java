package app.untrail;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.*;
import android.view.*;
import android.widget.*;
import android.os.*;
import android.graphics.Color;

/** Opt-in prototype; only WhatsApp's known composer resource may be modified. */
public class DraftService extends AccessibilityService {
 private final Handler handler=new Handler(Looper.getMainLooper());
 private View undo; private Runnable clear;
 private final DraftPolicy policy=new DraftPolicy();
 private int composerWindow=-1;
 private String composerPackage="";
 @Override public void onAccessibilityEvent(AccessibilityEvent event) {
  if(!getSharedPreferences("untrail",0).getBoolean("draft",false)){policy.reset();dismiss();return;}
  if(event.getEventType()!=AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED)return;
  AccessibilityNodeInfo node=event.getSource();
  if(!eligible(node))return;
  String old=node.getText()==null?"":node.getText().toString();
  String pkg=String.valueOf(node.getPackageName());
  if(composerWindow!=node.getWindowId() || !composerPackage.equals(pkg))policy.reset();
  composerWindow=node.getWindowId();composerPackage=pkg;
  int start=node.getTextSelectionStart(),end=node.getTextSelectionEnd();
  if(!policy.shouldClean(old,event.getFromIndex(),event.getAddedCount(),start,end))return;
  Cleaner.Result result=Cleaner.clean(old);
  if(old.equals(result.text))return;
  Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,result.text);
  if(!node.refresh() || !eligible(node) || !old.contentEquals(node.getText()==null?"":node.getText()))return;
  if(node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args)){
   select(node,DraftPolicy.mapSelection(old,result.text,start),DraftPolicy.mapSelection(old,result.text,end));
   showUndo(old,result.text,node.getWindowId(),start,end);
  }
 }
 private boolean eligible(AccessibilityNodeInfo n) {
  if(n==null||!n.isEditable()||!n.isFocused()||n.isPassword())return false;
  String pkg=String.valueOf(n.getPackageName()),id=n.getViewIdResourceName();
  return (pkg.equals("com.whatsapp")||pkg.equals("com.whatsapp.w4b")) && (pkg+":id/entry").equals(id);
 }
 private void select(AccessibilityNodeInfo node,int start,int end){
  if(start<0 || end<0)return;
  Bundle selection=new Bundle();selection.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT,start);selection.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT,end);
  node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION,selection);
 }
 private void showUndo(String original,String replacement,int windowId,int start,int end) {
  dismiss();
  Button b=new Button(this);b.setText("Link cleaned · Undo");String mode=getSharedPreferences("untrail",0).getString("appearance","system");boolean dark=mode.equals("dark")||mode.equals("system")&&(getResources().getConfiguration().uiMode&android.content.res.Configuration.UI_MODE_NIGHT_MASK)==android.content.res.Configuration.UI_MODE_NIGHT_YES;
  b.setTextColor(dark?0xFF121212:0xFFF2F2F2);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dark?0xFFF2F2F2:0xFF161616));
  WindowManager.LayoutParams p=new WindowManager.LayoutParams(-2,-2,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,android.graphics.PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;p.y=120;
  b.setOnClickListener(v->{AccessibilityNodeInfo root=getRootInActiveWindow();AccessibilityNodeInfo focused=root==null?null:root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
   if(eligible(focused)&&focused.getWindowId()==windowId&&replacement.contentEquals(focused.getText()==null?"":focused.getText())) {
    policy.suppress(original);Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,original);if(focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args))select(focused,start,end);else policy.reset();
   }dismiss();});
  undo=b;try{((WindowManager)getSystemService(WINDOW_SERVICE)).addView(b,p);}catch(RuntimeException e){undo=null;return;}
  clear=this::dismiss;handler.postDelayed(clear,7000);
 }
 private void dismiss(){if(clear!=null)handler.removeCallbacks(clear);if(undo!=null){try{((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(undo);}catch(RuntimeException ignored){}undo=null;}}
 @Override public void onInterrupt(){policy.reset();dismiss();}
 @Override public void onDestroy(){dismiss();handler.removeCallbacksAndMessages(null);super.onDestroy();}
}
