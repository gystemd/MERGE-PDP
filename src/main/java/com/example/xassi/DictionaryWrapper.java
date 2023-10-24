package com.example.xassi;

import java.util.HashMap;
import java.util.Map;

public class DictionaryWrapper {
   private Map<String, String> dictionary = new HashMap<String, String>();

   public DictionaryWrapper(){}

   public DictionaryWrapper(Map<String, String> dictionary){
      this.dictionary = dictionary;
   }

   public void setDictionary(Map<String, String> dictionary){
      this.dictionary = dictionary;
   }

   public void getDictionary(Map<String, String> dictionary){
      this.dictionary = dictionary;
   }
}
