import app.untrail.DraftPolicy;
public class DraftPolicyTest {
 static int count;
 static void expect(boolean value){if(!value)throw new AssertionError("Draft check "+(count+1));count++;}
 public static void main(String[] args){
  DraftPolicy p=new DraftPolicy();String url="https://example.org/?utm_source=x";
  for(int i=1;i<=url.length();i++)expect(!p.shouldClean(url.substring(0,i),i-1,1,i,i));
  expect(p.shouldClean(url,0,url.length(),url.length(),url.length()));
  String done=url+" ";expect(p.shouldClean(done,done.length()-1,1,done.length(),done.length()));
  expect(!p.shouldClean(url,url.indexOf("utm_"),4,url.length(),url.length()));
  expect(!p.shouldClean(url,0,url.length(),-1,-1));
  expect(!p.shouldClean(url,0,url.length(),1,3));
  p.suppress(url);
  expect(!p.shouldClean(done,done.length()-1,1,done.length(),done.length()));
  String message=url+" check this out ";
  expect(!p.shouldClean(message,message.length()-1,1,message.length(),message.length()));
  String edited=url+"2 ";expect(p.shouldClean(edited,edited.length()-1,1,edited.length(),edited.length()));
  p.suppress(url);p.shouldClean("",0,0,0,0);
  expect(p.shouldClean(url,0,url.length(),url.length(),url.length()));
  String before=url+" hello",after="https://example.org/ hello";
  expect(DraftPolicy.mapSelection(before,after,before.length())==after.length());
  expect(DraftPolicy.mapSelection(before,after,3)==3);
  expect(DraftPolicy.mapSelection(before,after,url.length()-2)=="https://example.org/".length());
  expect(DraftPolicy.mapSelection(before,after,-1)==-1);
  System.out.println(count+" draft policy checks passed");
 }
}
