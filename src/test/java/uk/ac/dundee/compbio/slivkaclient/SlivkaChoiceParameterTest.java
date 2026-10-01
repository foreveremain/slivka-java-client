package uk.ac.dundee.compbio.slivkaclient;

import org.json.JSONObject;
import org.testng.annotations.Test;

import uk.ac.dundee.compbio.slivkaclient.Parameter.ChoiceParameter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SlivkaChoiceParameterTest
{
  @Test
  public void testChoiceOfTrueAndFalse()
  {
    String yaml = "{\n\"array\": false,\n\"choices\": [\ntrue,\nfalse\n],\n\"default\": null,\n\"description\": \"\",\n\"id\": \"sprotein-sequence\",\n\"name\": \"sprotein_sequence\",\n\"required\": false,\n\"type\": \"choice\"\n}";
    ChoiceParameter choice = new ChoiceParameter();
    choice.readJSON(new JSONObject(yaml));
    assertEquals(choice.getChoices().size(), 2);
    assertTrue(choice.getChoices().contains("true"));
    assertTrue(choice.getChoices().contains("false"));

  }
}
