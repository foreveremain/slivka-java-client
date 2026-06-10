package uk.ac.dundee.compbio.slivkaclient;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javajs.http.ClientProtocolException;
import javajs.http.HttpClient;
import javajs.http.HttpClientFactory;
import javajs.http.HttpResponseException;

public class SlivkaClientImpl implements SlivkaClient {

  private URI url;
  private HttpClient httpClient;
  private List<SlivkaService> services;

  public SlivkaClientImpl(HttpClient httpClient, URI url) {
    this.httpClient = httpClient;
    this.url = url;
  }

  public SlivkaClientImpl(URI address) {
    this(HttpClientFactory.getClient(null), address);
  }

  public SlivkaClientImpl(String address) {
    this(URI.create(address));
  }

  public SlivkaClientImpl(String host, int port, String path)
      throws URISyntaxException {
    this(new URI("http", null, host, port, path, null, null));
  }

  public SlivkaClientImpl(String host, int port) throws URISyntaxException {
    this(host, port, "/");
  }

  @Override
  public HttpClient getHttpClient() { return httpClient; }

  @Override
  public void setHttpClient(final HttpClient httpClient) { this.httpClient = httpClient; }

  @Override
  public URI getUrl() { return url; }

  @Override
  public URI urlFor(String path) {
    return url.resolve(path);
  }

  @Override
  public Version getVersion() throws IOException {
    HttpClient.HttpResponse response = getHttpClient()
        .get(getUrl().resolve("api/version")).execute();
    try (response) {
      int statusCode = response.getStatusCode();
      if (statusCode == 200) {
        try {
          JSONObject json = new JSONObject(response.getText());
          return new Version(
              json.getString("slivkaVersion"),
              json.getString("APIVersion"));
        }
        catch (JSONException e) {
          throw new ClientProtocolException("Unprocessable server response.", e);
        }
      }
      else {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase());
      }
    }
  }

  @Override
  public List<SlivkaService> getServices() throws IOException {
    if (this.services != null) {
      return this.services;
    }
    HttpClient.HttpResponse response = getHttpClient()
        .get(urlFor("api/services")).execute();
    try (response) {
      if (response.getStatusCode() != 200) {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase());
      }
      JSONObject jsonData = new JSONObject(response.getText());
      ArrayList<SlivkaService> services = new ArrayList<>();
      JSONArray servicesArray = jsonData.getJSONArray("services");
      for (int i = 0; i < servicesArray.length(); ++i) {
        JSONObject srvc = servicesArray.getJSONObject(i);
        ArrayList<String> classifiers = new ArrayList<>();
        for (Object obj : srvc.getJSONArray("classifiers")) {
          classifiers.add(String.valueOf(obj));
        }
        ArrayList<Parameter> parameters = new ArrayList<>();
        JSONArray prmArray = srvc.getJSONArray("parameters");
        for (int j = 0; j < prmArray.length(); ++j) {
          parameters.add(Parameter.fromJSON(prmArray.getJSONObject(j)));
        }
        ArrayList<SlivkaService.Preset> presets = new ArrayList<>();
        JSONArray presetsArray = srvc.getJSONArray("presets");
        for (int j = 0; j < presetsArray.length(); ++j) {
          JSONObject obj = presetsArray.getJSONObject(j);
          presets.add(new SlivkaService.Preset(
              obj.getString("id"), obj.getString("name"),
              obj.getString("description"), obj.getJSONObject("values").toMap()));
        }
        var status = new SlivkaService.Status(
            srvc.getJSONObject("status").getString("status"),
            srvc.getJSONObject("status").getString("errorMessage"),
            srvc.getJSONObject("status").getString("timestamp"));
        services.add(new SlivkaService(
            urlFor(srvc.getString("@url")),
            srvc.getString("id"),
            srvc.getString("name"),
            srvc.getString("description"),
            srvc.getString("author"),
            srvc.getString("version"),
            srvc.getString("license"),
            classifiers,
            parameters,
            presets,
            status));
      }
      this.services = services;
    }
    catch (JSONException e) {
      throw new ClientProtocolException("Unprocessable server response.", e);
    }
    return this.services;
  }

  @Override
  public SlivkaService getService(String id) throws IOException {
    for (SlivkaService service : getServices()) {
      if (id.equals(service.getId())) {
        return service;
      }
    }
    return null;
  }

  @Override
  public RemoteFile uploadFile(File file) throws IOException {
    return uploadFile(new FileInputStream(file));
  }

  @Override
  public RemoteFile uploadFile(InputStream stream) throws IOException {
    HttpClient.HttpResponse response = getHttpClient()
        .post(getUrl().resolve("api/files")).addFilePart("file", stream).execute();
    try (response) {
      int statusCode = response.getStatusCode();
      if (statusCode == 201) {
        JSONObject obj = new JSONObject(response.getText());
        return RemoteFile.fromJSON(this.url, obj);
      }
      else {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase());
      }
    }
    catch (JSONException e) {
      throw new ClientProtocolException("Unprocessable server response.", e);
    }
  }

  @Override
  public String submitJob(SlivkaService service, RequestValues parameters)
      throws IOException {
    URI url = getUrl().resolve(format("api/services/%s/jobs", service.getId()));
    var request = getHttpClient().post(url);
    for (var entry : parameters.getData()) {
      request = request.addFormPart(entry.getKey(), entry.getValue());
    }
    for (var entry : parameters.getFiles()) {
      request = request.addFilePart(entry.getKey(), entry.getValue());
    }
    for (var entry : parameters.getStreams()) {
      request = request.addFilePart(entry.getKey(), entry.getValue());
    }
    var response = request.execute();
    try (response) {
      if (response.getStatusCode() == 202) {
        JSONObject obj = new JSONObject(response.getText());
        return obj.getString("id");
      }
      else if (response.getStatusCode() == 422) {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase(), response.getText());
      }
      else {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase());
      }
    }
    catch (JSONException e) {
      throw new ClientProtocolException("Unprocessable server response.", e);
    }
  }

  @Override
  public Job fetchJobInfo(String id) throws IOException {
    URI url = getUrl().resolve(format("api/jobs/%s", id));
    HttpClient.HttpResponse response = getHttpClient().get(url).execute();
    try (response) {
      if (response.getStatusCode() == 200) {
        JSONObject obj = new JSONObject(response.getText());
        return Job.fromJSON(this, obj);
      }
      else {
        throw new HttpResponseException(
            response.getStatusCode(), response.getReasonPhrase());
      }
    }
    catch (JSONException e) {
      throw new ClientProtocolException("Unprocessable server response.", e);
    }
  }

  @Override
  public Job.Status fetchJobStatus(String id) throws IOException {
    URI url = getUrl().resolve(format("api/jobs/%s", id));
    var response = getHttpClient().get(url).execute();
    try (response) {
      if (response.getStatusCode() == 200) {
        try {
          JSONObject obj = new JSONObject(response.getText());
          var status = obj.getString("status");
          return Job.Status.valueOf(status.toUpperCase());
        }
        catch (JSONException e) {
          throw new ClientProtocolException("Unprocessable server response.", e);
        }
      }
      else {
        throw new HttpResponseException(response.getStatusCode(), response.getReasonPhrase());
      }
    }
  }

  @Override
  public Collection<RemoteFile> fetchFilesList(String id) throws IOException {
    URI url = getUrl().resolve(format("api/jobs/%s/files", id));
    var response = getHttpClient().get(url).execute();
    try (response) {
      if (response.getStatusCode() == 200) {
        Collection<RemoteFile> files = new ArrayList<>();
        try {
          JSONArray arr = new JSONObject(response.getText()).getJSONArray("files");
          for (int i = 0; i < arr.length(); i++) {
            files.add(RemoteFile.fromJSON(this.url, arr.getJSONObject(i)));
          }
        }
        catch (JSONException e) {
          throw new ClientProtocolException("Unprocessable server response.", e);
        }
        return files;
      }
      else {
        throw new HttpResponseException(response.getStatusCode(), response.getReasonPhrase());
      }
    }
  }

  @Override
  public void writeFileTo(RemoteFile file, OutputStream out) throws IOException {
    try (var stream = getFileStream(file)) {
      stream.transferTo(out);
    }
  }

  @Override
  public InputStream getFileStream(RemoteFile file) throws IOException {
    HttpClient.HttpResponse response =
        getHttpClient().get(file.getContentUrl()).execute();
    if (response.getStatusCode() == 200) {
      return response.getContent();
    }
    else {
      throw new HttpResponseException(
          response.getStatusCode(), response.getReasonPhrase());
    }
  }

  @Override
  public String toString() {
    return format("SlivkaClient(%s)", url.toString());
  }
}
