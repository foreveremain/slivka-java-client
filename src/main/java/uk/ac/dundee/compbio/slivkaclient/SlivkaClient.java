package uk.ac.dundee.compbio.slivkaclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import javajs.http.HttpClient;

public interface SlivkaClient {

  public static class Version {
    public final String server, API;

    public Version(String server, String API) {
      this.server = server;
      this.API = API;
    }

    @Override
    public String toString() {
      return String.format("Version(server=%s, API=%s)", server, API);
    }
  }

  public static SlivkaClient newInstance(HttpClient httpClient, URI url) {
    return new SlivkaClientImpl(httpClient, url);
  }

  public static SlivkaClient newInstance(URI address) {
    return new SlivkaClientImpl(address);
  }

  public static SlivkaClient newInstance(String address) {
    return new SlivkaClientImpl(address);
  }

  HttpClient getHttpClient();

  void setHttpClient(HttpClient httpClient);

  URI getUrl();

  URI urlFor(String path);

  Version getVersion() throws IOException;

  List<SlivkaService> getServices() throws IOException;

  SlivkaService getService(String id) throws IOException;

  RemoteFile uploadFile(File file) throws IOException;

  RemoteFile uploadFile(InputStream stream) throws IOException;

  String submitJob(SlivkaService service, RequestValues parameters)
      throws IOException;

  @Deprecated
  default Job getJob(String id) throws IOException {
    return fetchJobInfo(id);
  }

  Job fetchJobInfo(String id) throws IOException;

  Job.Status fetchJobStatus(String id) throws IOException;

  Collection<RemoteFile> fetchFilesList(String id) throws IOException;

  void writeFileTo(RemoteFile file, OutputStream out) throws IOException;

  InputStream getFileStream(RemoteFile file) throws IOException;
}
