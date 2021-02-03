/**
  * Copyright 2021 bejson.com 
  */
package bean.tuling.response;
import java.util.List;

/**
 * Auto-generated: 2021-02-02 14:43:14
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class TuLingResponseBean {

    private Intent intent;
    private List<Results> results;
    public void setIntent(Intent intent) {
         this.intent = intent;
     }
     public Intent getIntent() {
         return intent;
     }

    public void setResults(List<Results> results) {
         this.results = results;
     }
     public List<Results> getResults() {
         return results;
     }

}