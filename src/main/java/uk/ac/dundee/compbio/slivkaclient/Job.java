package uk.ac.dundee.compbio.slivkaclient;

import java.net.URI;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Job {
  public enum Status {
    PENDING, REJECTED, ACCEPTED, QUEUED, RUNNING, COMPLETED, INTERRUPTED,
    DELETED, FAILED, ERROR, UNKNOWN;
  }

  private static final SimpleDateFormat dateFmt =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  protected final URI url;
  protected final String id;
  protected final String service;
  protected final List<Map.Entry<String, String>> parameters;
  protected final Date submissionTime;
  protected Date completionTime;
  protected Status status;

  public Job(
      URI url, String id, String service,
      List<Map.Entry<String, String>> parameters, String submissionTime,
      String completionTime, String status) {
    this.url = url;
    this.id = id;
    this.service = service;
    this.parameters = parameters;
    this.submissionTime = dateFmt.parse(submissionTime, new ParsePosition(0));
    setCompletionTime(completionTime);
    setStatus(status);
  }

  static Job fromJSON(SlivkaClient client, JSONObject obj) throws JSONException {
    JSONObject paramsObj = obj.getJSONObject("parameters");
    List<Map.Entry<String, String>> parameters = new ArrayList<>();
    for (String key : paramsObj.keySet()) {
      Object val = paramsObj.get(key);
      if (JSONObject.NULL.equals(val)) {
        continue;
      }
      else if (val instanceof String) {
        parameters.add(new AbstractMap.SimpleEntry<>(key, (String) val));
      }
      else if (val instanceof JSONArray) {
        for (Object it : (JSONArray) val) {
          if (!JSONObject.NULL.equals(it)) {
            parameters.add(new AbstractMap.SimpleEntry<>(key, String.valueOf(it)));
          }
        }
      }
      else {
        throw new JSONException("JSONObject[" + JSONObject.quote(key) +
            "] not a string or array of strings.");
      }
    }
    return new Job(
        client.getUrl().resolve(obj.getString("@url")),
        obj.getString("id"),
        obj.getString("service"),
        parameters,
        obj.getString("submissionTime"),
        obj.optString("completionTime", null),
        obj.getString("status"));
  }

  public URI getUrl() { return url; }

  public String getId() { return id; }

  public String getService() { return service; }

  public List<Map.Entry<String, String>> getParameters() { return parameters; }

  public Date getSubmissionTime() { return submissionTime; }

  public Date getCompletionTime() { return completionTime; }

  void setCompletionTime(Date date) { completionTime = date; }

  void setCompletionTime(String date) {
    if (date != null) {
      setCompletionTime(dateFmt.parse(date, new ParsePosition(0)));
    }
  }

  private void setStatus(String status) {
    this.status = Status.valueOf(status.toUpperCase());
  }
}
