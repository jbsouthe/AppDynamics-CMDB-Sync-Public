package com.appdynamics.controller.apidata.synthetic.page;

import java.util.ArrayList;
import java.util.List;

public class SyntheticPageListRequest {
    /**'{   "requestFilter":{"applicationId":50,"fetchSyntheticData":true},
     *      "resultColumns":["PAGE_TYPE","PAGE_NAME","TOTAL_REQUESTS","END_USER_RESPONSE_TIME"],
     *      "offset":0,"limit":-1,"searchFilters":[],
     *      "columnSorts":[
     *          {"column":"TOTAL_REQUESTS","direction":"DESC"}
 *          ],
     *      "timeRangeStart":1693513031046,"timeRangeEnd":1693516631046}' \
    */

    public RequestFilter requestFilter;
    public List<String> resultColumns = new ArrayList<>();
    public int offset = 0, limit = -1;
    public List<String> searchFilters = new ArrayList<>();
    public List<ColumnSort> columnSorts = new ArrayList<>();
    public long timeRangeStart, timeRangeEnd;

    public SyntheticPageListRequest( long applicationId ) {
         this.requestFilter = new RequestFilter(applicationId, false);
         this.resultColumns.addAll(List.of(new String[]{"PAGE_TYPE", "PAGE_NAME", "TOTAL_REQUESTS", "END_USER_RESPONSE_TIME"}));
         this.columnSorts.add(new ColumnSort());
         timeRangeEnd = System.currentTimeMillis();
         timeRangeStart = timeRangeEnd - 3600000;
    }
}
