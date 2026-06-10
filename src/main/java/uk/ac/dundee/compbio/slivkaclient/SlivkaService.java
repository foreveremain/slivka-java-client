package uk.ac.dundee.compbio.slivkaclient;

import java.net.URI;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SlivkaService {
  public static class Preset {
    public final String id;

    public final String name;

    public final String description;

    public final Map<String, Object> values;

    Preset(String id, String name, String description, Map<String, Object> values) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.values = java.util.Collections.unmodifiableMap(values);
    }
  }

  public static class Status {
    public final String statusString;

    public final String message;

    public final Date timestamp;

    Status(String status, String message, String time) {
      this.statusString = status;
      this.message = message;
      var parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      this.timestamp = parser.parse(time, new ParsePosition(0));
    }
  }

  public final URI url;

  public final String id;

  public final String name;

  public final String description;

  public final String author;

  public final String version;

  public final String license;

  public final List<String> classifiers;

  public final List<Parameter> parameters;

  public final List<Preset> presets;

  public final Status status;

  public SlivkaService(
      URI url, String id, String name,
      String description, String author, String version,
      String license, List<String> classifiers, List<Parameter> parameters,
      List<Preset> presets, Status status) {
    this.id = id;
    this.url = url;
    this.name = name;
    this.description = description;
    this.author = author;
    this.version = version;
    this.license = license;
    this.classifiers = Collections.unmodifiableList(classifiers);
    this.parameters = Collections.unmodifiableList(parameters);
    this.presets = Collections.unmodifiableList(presets);
    this.status = status;
  }

  public URI getUrl() { return url; }

  public String getId() { return id; }

  public String getName() { return name; }

  public String getDescription() { return description; }

  public String getAuthor() { return author; }

  public String getVersion() { return version; }

  public String getLicense() { return license; }

  public List<String> getClassifiers() { return classifiers; }

  public List<Parameter> getParameters() { return parameters; }

  public Parameter getParameter(String id) {
    for (Parameter param : getParameters()) {
      if (param.id.equals(id)) {
        return param;
      }
    }
    return null;
  }

  public List<Preset> getPresets() { return presets; }

  public Status getStatus() { return status; }
}
