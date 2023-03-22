using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Org.Unidal.Cat;

namespace CatClientTest
{
    class PrintExceptionTest
    {
        public static void Test()
        {
            try
            {
                DoWork();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                Cat.LogError(ex);
            }
        }

        private static void DoWork()
        {
            try
            {
                DoInternalWork();
            }
            catch (Exception ex)
            {
                throw new Exception("Exception in DoWork()", ex);
            }
        }

        private static void DoInternalWork()
        {
            throw new InvalidOperationException("my inner exception");
        }
    }
}
