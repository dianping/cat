package com.site.lookup.util;

import java.util.Collection;

public class StringUtils {
   public static final boolean isEmpty(String str) {
      return str == null || str.length() == 0;
   }

   public static final boolean isNotEmpty(String str) {
      return str != null && str.length() > 0;
   }

   public static final String join(String[] array, String separator) {
      StringBuilder sb = new StringBuilder(1024);
      boolean first = true;

      for (String item : array) {
         if (first) {
            first = false;
         } else {
            sb.append(separator);
         }

         sb.append(item);
      }

      return sb.toString();
   }

   public static final String join(Collection<String> list, String separator) {
      StringBuilder sb = new StringBuilder(1024);
      boolean first = true;

      for (String item : list) {
         if (first) {
            first = false;
         } else {
            sb.append(separator);
         }

         sb.append(item);
      }

      return sb.toString();
   }

   public static final String normalizeSpace(String str) {
      int len = str.length();
      StringBuilder sb = new StringBuilder(len);
      boolean space = false;

      for (int i = 0; i < len; i++) {
         char ch = str.charAt(i);

         switch (ch) {
         case ' ':
         case '\t':
         case '\r':
         case '\n':
            space = true;
            break;
         default:
            if (space) {
               sb.append(' ');
               space = false;
            }

            sb.append(ch);
         }
      }

      return sb.toString();
   }

   public static final String trimAll(String str) {
      if (str == null) {
         return str;
      }

      int len = str.length();
      StringBuilder sb = new StringBuilder(len);

      for (int i = 0; i < len; i++) {
         char ch = str.charAt(i);

         switch (ch) {
         case ' ':
         case '\t':
         case '\r':
         case '\n':
            break;
         default:
            sb.append(ch);
         }
      }

      return sb.toString();
   }
}
