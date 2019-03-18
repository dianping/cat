using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Org.Unidal.Cat.Util
{
    class MD5Util
    {
        public static string Compute(string str)
        {
            string hash = "";
            using (System.Security.Cryptography.MD5 md5 = System.Security.Cryptography.MD5.Create())
            {
                hash = BitConverter.ToString(
                  md5.ComputeHash(Encoding.UTF8.GetBytes(str))
                ).Replace("-", String.Empty);
            }
            return hash;
        }
    }
}
