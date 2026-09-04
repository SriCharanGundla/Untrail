package app.untrail;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure-Java safeguards for automatic edits; uncertain typing stays untouched. */
public final class DraftPolicy {
 private static final Pattern URL=Pattern.compile("https?://[^\\s<>\"]+",Pattern.CASE_INSENSITIVE);
 private final Set<String> restored=new HashSet<>();
 public void reset(){restored.clear();}
 public void suppress(String original){
  reset();Matcher m=URL.matcher(original);while(m.find())restored.add(m.group());
 }
 public boolean shouldClean(String text,int from,int added,int selectionStart,int selectionEnd){
  Set<String> present=new HashSet<>();Matcher m=URL.matcher(text);while(m.find())present.add(m.group());
  restored.retainAll(present);
  // Leave this draft alone while any restored URL remains unchanged.
  if(!restored.isEmpty() || text.isEmpty() || text.length()>100000)return false;
  if(selectionStart<0 || selectionStart!=selectionEnd)return false;
  if(from<0 || added<=0 || from>text.length()-added)return false;
  String inserted=text.substring(from,from+added);
  // A bulk insertion must contain a whole URL, not just a parameter fragment.
  Matcher pasted=URL.matcher(inserted);
  if(added>1 && pasted.find() && !Cleaner.cleanForShare(pasted.group()).text.isEmpty())return true;
  return selectionEnd==text.length() && Character.isWhitespace(text.charAt(text.length()-1));
 }
 public static int mapSelection(String before,String after,int position){
  if(position<0)return -1;
  int prefix=0,limit=Math.min(before.length(),after.length());
  while(prefix<limit && before.charAt(prefix)==after.charAt(prefix))prefix++;
  int suffix=0;
  while(suffix<limit-prefix && before.charAt(before.length()-1-suffix)==after.charAt(after.length()-1-suffix))suffix++;
  if(position<=prefix)return position;
  if(position>=before.length()-suffix)return Math.min(after.length(),position+after.length()-before.length());
  return after.length()-suffix;
 }
}
