package com.github.a1k28.dynamodbparser.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.a1k28.dynamodbparser.Deserializer;
import com.github.a1k28.dynamodbparser.deserializer.model.RequestItem;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeserializerTest {

    @Test
    public void test_deserialize() throws Exception {
        String request = """
                {
                  "ID": {
                    "S": "de2be9fc-bf77-4837-b4c6-4d25389f42bb"
                  },
                  "CREATED_AT": {
                    "S": "2024-11-17T21:52:45.087714"
                  },
                  "HTML_DATA": {
                    "S": ""
                  },
                  "INITIALIZED": {
                    "BOOL": true
                  },
                  "LAST_UPDATED_AT": {
                    "S": "2024-11-17T21:52:45.087714"
                  },
                  "N_PAGES": {
                    "N": "1"
                  },
                  "PAGE": {
                    "N": "1"
                  },
                  "PROPERTIES": {
                    "L": [
                      {
                        "M": {
                          "ACTIVE": {
                            "BOOL": false
                          },
                          "DESCRIPTION": {
                            "S": "The name of the client"
                          },
                          "NAME": {
                            "S": "client_name"
                          },
                          "TRY_COUNT": {
                            "N": "0"
                          },
                          "TYPE": {
                            "S": "TEXT"
                          },
                          "XPATHS": {
                            "L": []
                          }
                        }
                      },
                      {
                        "M": {
                          "ACTIVE": {
                            "BOOL": true
                          },
                          "DESCRIPTION": {
                            "S": "the image of the client"
                          },
                          "NAME": {
                            "S": "image_url"
                          },
                          "TRY_COUNT": {
                            "N": "3"
                          },
                          "TYPE": {
                            "S": "IMAGE"
                          },
                          "XPATHS": {
                               "L": [
                                 {
                                   "S": "xpath1"
                                 },
                                 {
                                   "S": "xpath2"
                                 }
                               ]
                             }
                        }
                      },
                      {
                        "M": {
                          "ACTIVE": {
                            "BOOL": false
                          },
                          "DESCRIPTION": {
                            "S": "the price of the apartment"
                          },
                          "NAME": {
                            "S": "price"
                          },
                          "TRY_COUNT": {
                            "N": "0"
                          },
                          "TYPE": {
                            "S": "TEXT"
                          },
                          "XPATHS": {
                            "L": []
                          }
                        }
                      }
                    ]
                  },
                  "STATUS": {
                    "S": "ACTIVE"
                  },
                  "TRY_COUNT": {
                    "N": "0"
                  },
                  "USER_ID": {
                    "S": "a5a166df-519b-45db-8cf9-69b36f3aa49d"
                  },
                  "WEB_URL": {
                    "S": "https://www.myhome.ge/en/pr/19664601/iyideba-3-otaxiani-bina-saburtaloze/"
                  }
                }
                """;
        Map map = new ObjectMapper().readValue(request, Map.class);
        RequestItem item = Deserializer.deserialize(map, RequestItem.class);

        assertTrue(item.getInitialized());
        assertEquals("de2be9fc-bf77-4837-b4c6-4d25389f42bb", item.getId());
        assertEquals("2024-11-17T21:52:45.087714", item.getCreatedAt());
        assertEquals(1, item.getPage());
        assertEquals(1, item.getNPages());
        assertEquals(3, item.getProperties().size());
        assertTrue(item.getProperties().get(1).getActive());
        assertEquals(2, item.getProperties().get(1).getXpaths().size());
        assertEquals("xpath1", item.getProperties().get(1).getXpaths().getFirst());
        assertEquals("xpath2", item.getProperties().get(1).getXpaths().getLast());
        assertEquals(0, item.getTryCount());
    }
}