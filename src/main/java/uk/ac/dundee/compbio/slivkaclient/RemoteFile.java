package uk.ac.dundee.compbio.slivkaclient;

import java.net.URI;

import org.json.JSONObject;

public class RemoteFile
{
  private final URI url;

  private final URI contentUrl;

  private final String id;

  private final String jobId;

  private final String path;

  private final String label;

  private final String mediaType;

  public URI getUrl()
  {
    return url;
  }

  public URI getContentUrl()
  {
    return contentUrl;
  }

  public String getId()
  {
    return id;
  }

  public String getJobId()
  {
    return jobId;
  }

  public String getPath()
  {
    return path;
  }

  public String getLabel()
  {
    return label;
  }

  public String getMediaType()
  {
    return mediaType;
  }

  public RemoteFile(URI url, URI contentUrl, String id, String jobId,
          String path, String label, String mediaType)
  {
    this.url = url;
    this.contentUrl = contentUrl;
    this.id = id;
    this.jobId = jobId;
    this.path = path;
    this.label = label;
    this.mediaType = mediaType;
  }

  static RemoteFile fromJSON(URI baseURL, JSONObject obj)
  {
    return new RemoteFile(baseURL.resolve(obj.getString("@url")),
            baseURL.resolve(obj.getString("@content")), obj.getString("id"),
            obj.optString("jobId"), obj.optString("path"),
            obj.optString("label"), obj.optString("mediaType"));
  }

  @Override
  public String toString()
  {
    return id;
  }
}
