using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Org.Unidal.Cat.Util
{
    class MapEncoding
    {
        private const string MAP_PREFIX = "@{";
        private const string MAP_SUFFIX = "@}";
        private const string MAP_ENTRY_SEPARATOR = "@,";
        private const string MAP_KEY_VALUE_SEPARATOR = "=";
        private const string NULL_STRING = "@NULL";

        public static String toString(IDictionary<string, string> map)  {
            StringBuilder sb = new StringBuilder();
            sb.Append(MAP_PREFIX);
            bool isFirst = true;
            foreach (KeyValuePair<string, string> entry in map) {
                if (!isFirst) {
                    sb.Append(MAP_ENTRY_SEPARATOR);
                }
                string key = entry.Key;
                if (key == null) {
                    key = NULL_STRING;
                }
                string value = entry.Value;
                if (value == null) {
                    value = NULL_STRING;
                }
                sb.Append(key);
                sb.Append(MAP_KEY_VALUE_SEPARATOR);
                sb.Append(value);
                isFirst = false;
            }
            sb.Append(MAP_SUFFIX);
            return sb.ToString();
        }
    }
}
