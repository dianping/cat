using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Org.Unidal.Cat.Util
{
    class SizeUtils
    {
        private static int[] sizeTable = {  1 << 6,  1 << 7,  1 << 8,  1 << 9, 
                                               1 << 10, 1 << 11, 1 << 12, 1 << 13, 1 << 14, 1 << 15, 1 << 16, 1 << 17, 1 << 18, 1 << 19, 
                                               1 << 20, 1 << 21, 1 << 22, 1 << 23, 1 << 24, 1 << 25, 1 << 26, 1 << 27, 1 << 28};
        private static string[] valueTable = {    "64",   "128",   "256",   "512", 
                                                  "1K",    "2K",    "4K",    "8K",   "16K",   "32K",   "64K",  "128K",  "256K",  "512K", 
                                                  "1M",    "2M",    "4M",    "8M",   "16M",   "32M",   "64M", "128M", "256M" };

        public static string GetSizeScale(long size)
        {
            if (size < sizeTable[0])
                return "0~" + valueTable[0];

            for (int i = 1; i < sizeTable.Length; i++)
            {
                if (size < sizeTable[i])
                    return valueTable[i - 1] + "~" + valueTable[i];
            }

            return ">=" + valueTable[valueTable.Length - 1];
        }
    }
}
