package com.dianping.cat.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Splitters {
   public static StringSplitter by(char delimiter) {
      return new StringSplitter(delimiter);
   }

   public static MapSplitter by(char pairSeparator, char keyValueSeparator) {
      return new MapSplitter(pairSeparator, keyValueSeparator);
   }

   public static StringSplitter by(String delimiter) {
      return new StringSplitter(delimiter);
   }

   public static class MapSplitter {
      private char m_pairSeparator;

      private char m_keyValueSeparator;

      private boolean m_trim;

      MapSplitter(char pairSeparator, char keyValueSeparator) {
         m_pairSeparator = pairSeparator;
         m_keyValueSeparator = keyValueSeparator;
      }

      protected void doCharSplit(String str, Map<String, String> map) {
         int len = str.length();
         StringBuilder key = new StringBuilder(len);
         StringBuilder value = new StringBuilder(len);
         boolean inKey = true;

         for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);

            if (ch == m_keyValueSeparator && inKey) {
               inKey = false;
            } else if (ch == m_pairSeparator) {
               if (key.length() > 0) {
                  if (m_trim) {
                     map.put(key.toString().trim(), value.toString().trim());
                  } else {
                     map.put(key.toString(), value.toString());
                  }
               }

               key.setLength(0);
               value.setLength(0);
               inKey = true;
            } else {
               if (inKey) {
                  key.append(ch);
               } else {
                  value.append(ch);
               }
            }
         }

         if (key.length() > 0) {
            if (m_trim) {
               map.put(key.toString().trim(), value.toString().trim());
            } else {
               map.put(key.toString(), value.toString());
            }
         }
      }

      public Map<String, String> split(String str) {
         return split(str, new LinkedHashMap<String, String>());
      }

      public Map<String, String> split(String str, Map<String, String> map) {
         if (str != null) {
            doCharSplit(str, map);
         }

         return map;
      }

      public MapSplitter trim() {
         m_trim = true;
         return this;
      }
   }

   public static class StringSplitter {
      private char m_charDelimeter;

      private String m_stringDelimeter;

      private boolean m_trim;

      private boolean m_noEmptyItem;

      StringSplitter(char delimiter) {
         m_charDelimeter = delimiter;
      }

      StringSplitter(String delimiter) {
         m_stringDelimeter = delimiter;
      }

      protected void doCharSplit(String str, List<String> list) {
         char delimiter = m_charDelimeter;
         int len = str.length();
         StringBuilder sb = new StringBuilder(len);

         for (int i = 0; i < len + 1; i++) {
            char ch = i == len ? delimiter : str.charAt(i);

            if (ch == delimiter) {
               String item = sb.toString();

               sb.setLength(0);

               if (m_trim) {
                  item = item.trim();
               }

               if (m_noEmptyItem && item.length() == 0) {
                  continue;
               }

               list.add(item);
            } else {
               sb.append(ch);
            }
         }
      }

      protected void doStringSplit(String source, List<String> list) {
         String delimiter = m_stringDelimeter;
         int len = delimiter.length();
         int offset = 0;
         int index = source.indexOf(delimiter, offset);

         while (true) {
            String part;

            if (index == -1) { // last part
               part = source.substring(offset);
            } else {
               part = source.substring(offset, index);
            }

            if (m_trim) {
               part = part.trim();
            }

            if (!m_noEmptyItem || part.length() > 0) {
               list.add(part);
            }

            if (index == -1) { // last part
               break;
            } else {
               offset = index + len;
               index = source.indexOf(delimiter, offset);
            }
         }
      }

      public StringSplitter noEmptyItem() {
         m_noEmptyItem = true;
         return this;
      }

      public List<String> split(String str) {
         return split(str, new ArrayList<String>());
      }

      public List<String> split(String str, List<String> list) {
         if (str != null) {
            if (m_charDelimeter > 0) {
               doCharSplit(str, list);
            } else if (m_stringDelimeter != null) {
               doStringSplit(str, list);
            }
         }

         return list;
      }

      public StringSplitter trim() {
         m_trim = true;
         return this;
      }
   }
}
